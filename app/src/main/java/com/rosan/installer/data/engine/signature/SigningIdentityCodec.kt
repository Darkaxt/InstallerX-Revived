// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.data.engine.signature

import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.util.Date
import org.bouncycastle.asn1.DERBMPString
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS12PfxPdu
import org.bouncycastle.pkcs.PKCS12PfxPduBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS12SafeBagBuilder
import org.bouncycastle.pkcs.jcajce.JcePKCS12MacCalculatorBuilder
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEOutputEncryptorBuilder

/** Use an explicit provider instance without changing Android's global providers. */
class SigningIdentityCodec {
    private val provider = BouncyCastleProvider()

    fun generate(): KeyStore.PrivateKeyEntry {
        val pair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val subject = X500Name("CN=InstallerX Personal APK Signing")
        val certificate = JcaX509CertificateConverter().setProvider(provider).getCertificate(
            JcaX509v3CertificateBuilder(
                subject,
                BigInteger(160, SecureRandom()).add(BigInteger.ONE),
                Date(0),
                Date(4102444800000L),
                subject,
                pair.public,
            ).build(JcaContentSignerBuilder("SHA256withRSA").setProvider(provider).build(pair.private)),
        )
        return KeyStore.PrivateKeyEntry(pair.private, arrayOf(certificate))
    }

    fun export(entry: KeyStore.PrivateKeyEntry, password: CharArray): ByteArray {
        require(password.isNotEmpty()) { "A backup password is required" }
        require(entry.privateKey.format == "PKCS#8") { "This Android Keystore key cannot be exported" }
        validate(entry)
        fun encryptor() = JcePKCSPBEOutputEncryptorBuilder(NISTObjectIdentifiers.id_aes256_CBC)
            .setProvider(provider).setPRF(AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA256))
            .setIterationCount(100_000).build(password)
        val keyId = JcaX509ExtensionUtils().createSubjectKeyIdentifier(entry.certificate.publicKey)
        val keyBag = JcaPKCS12SafeBagBuilder(entry.privateKey, encryptor())
            .addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, DERBMPString("personal"))
            .addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, keyId).build()
        val certBags = entry.certificateChain.mapIndexed { index, certificate ->
            JcaPKCS12SafeBagBuilder(certificate as java.security.cert.X509Certificate).apply {
                if (index == 0) {
                    addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, DERBMPString("personal"))
                    addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, keyId)
                }
            }.build()
        }
        return PKCS12PfxPduBuilder().addData(keyBag).addEncryptedData(encryptor(), certBags.toTypedArray())
            .build(JcePKCS12MacCalculatorBuilder(NISTObjectIdentifiers.id_sha256).setProvider(provider).setIterationCount(100_000), password)
            .encoded
    }

    fun import(bytes: ByteArray, password: CharArray): KeyStore.PrivateKeyEntry {
        require(bytes.size in 1..MAX_ARCHIVE_BYTES) { "Invalid signing backup size" }
        require(password.isNotEmpty()) { "A backup password is required" }
        require(PKCS12PfxPdu(bytes).hasMac()) { "A password-protected signing backup is required" }
        val store = KeyStore.getInstance("PKCS12", provider).apply { load(bytes.inputStream(), password) }
        val keys = store.aliases().toList().filter(store::isKeyEntry)
        require(keys.size == 1) { "The backup must contain exactly one private key" }
        val protection = KeyStore.PasswordProtection(password)
        val entry = try {
            store.getEntry(keys.single(), protection) as? KeyStore.PrivateKeyEntry
                ?: error("The backup does not contain a signing private key")
        } finally {
            protection.destroy()
        }
        validate(entry)
        return entry
    }

    fun validate(entry: KeyStore.PrivateKeyEntry) {
        require(entry.certificateChain.size in 1..16) { "Certificate chain must contain at most 16 certificates" }
        val certificate = entry.signingCertificate
        val publicKey = certificate.publicKey as? RSAPublicKey ?: error("An RSA signing identity is required")
        require(entry.privateKey.algorithm == "RSA" && publicKey.modulus.bitLength() >= 2048) { "RSA keys must be at least 2048 bits" }
        require(certificate.keyUsage?.firstOrNull() != false) { "Certificate does not allow digital signatures" }
        val challenge = ByteArray(32).also(SecureRandom()::nextBytes)
        val signature = Signature.getInstance("SHA256withRSA").apply {
            initSign(entry.privateKey)
            update(challenge)
        }.sign()
        require(
            Signature.getInstance("SHA256withRSA").run {
                initVerify(publicKey)
                update(challenge)
                verify(signature)
            },
        ) {
            "The private key does not match the certificate"
        }
    }

    fun fingerprint(entry: KeyStore.PrivateKeyEntry): String = MessageDigest.getInstance("SHA-256")
        .digest(entry.certificate.encoded).joinToString("") { "%02x".format(it) }

    fun publicCertificate(entry: KeyStore.PrivateKeyEntry): ByteArray {
        val body = java.util.Base64.getMimeEncoder(64, byteArrayOf(10)).encodeToString(entry.certificate.encoded)
        return "-----BEGIN CERTIFICATE-----\n$body\n-----END CERTIFICATE-----\n".toByteArray(Charsets.US_ASCII)
    }

    companion object {
        const val MAX_ARCHIVE_BYTES = 1024 * 1024
    }
}

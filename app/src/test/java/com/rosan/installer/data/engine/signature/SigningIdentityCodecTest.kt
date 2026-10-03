// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.data.engine.signature

import java.io.ByteArrayOutputStream
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.pkcs.Pfx
import org.bouncycastle.jce.provider.BouncyCastleProvider

class SigningIdentityCodecTest {
    private val codec = SigningIdentityCodec()

    @Test
    fun backupRestoresExactlyTheSameSigningIdentity() {
        val identity = codec.generate()
        val password = "my private backup password".toCharArray()
        val backup = codec.export(identity, password)
        assertEquals(NISTObjectIdentifiers.id_sha256, Pfx.getInstance(backup).macData.mac.algorithmId.algorithm)
        val restored = codec.import(backup, password)
        assertContentEquals(identity.privateKey.encoded, restored.privateKey.encoded)
        assertContentEquals(identity.certificate.encoded, restored.certificate.encoded)
        assertNotEquals(codec.fingerprint(identity), codec.fingerprint(codec.generate()))
        // Standard JDK PKCS#12 interoperability, independent of our import path.
        val standard = KeyStore.getInstance("PKCS12").apply { load(backup.inputStream(), password) }
        assertContentEquals(identity.privateKey.encoded, standard.getKey("personal", password).encoded)
        assertFails { codec.import(backup, "wrong".toCharArray()) }
        assertFails { codec.export(identity, charArrayOf()) }
        assertFails { codec.import(backup.copyOf(100), password) }
    }

    @Test
    fun importsExternallyGeneratedPkcs12() {
        val key = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(javaClass.getResourceAsStream("/resigning/rsa-2048.pk8")!!.use { it.readBytes() }))
        val cert = javaClass.getResourceAsStream("/resigning/rsa-2048.x509.pem")!!.use { CertificateFactory.getInstance("X.509").generateCertificate(it) }
        val password = "external password".toCharArray()
        val store = KeyStore.getInstance("PKCS12").apply {
            load(null, null)
            setKeyEntry("external", key, password, arrayOf(cert))
        }
        val bytes = ByteArrayOutputStream().also { store.store(it, password) }.toByteArray()
        val imported = codec.import(bytes, password)
        assertContentEquals(cert.encoded, imported.certificate.encoded)
        assertContentEquals(key.encoded, imported.privateKey.encoded)
    }

    @Test
    fun rejectsCertificateOnlyAmbiguousAndMismatchedKeys() {
        val first = codec.generate()
        val second = codec.generate()
        val password = "password".toCharArray()
        fun archive(block: KeyStore.() -> Unit): ByteArray {
            val store = KeyStore.getInstance("PKCS12", BouncyCastleProvider()).apply {
                load(null, null)
                block()
            }
            return ByteArrayOutputStream().also { store.store(it, password) }.toByteArray()
        }
        assertFails { codec.import(archive { setCertificateEntry("cert", first.certificate) }, password) }
        assertFails {
            codec.import(
                archive {
                    setKeyEntry("one", first.privateKey, password, first.certificateChain)
                    setKeyEntry("two", second.privateKey, password, second.certificateChain)
                },
                password,
            )
        }
        assertFails { codec.validate(KeyStore.PrivateKeyEntry(first.privateKey, second.certificateChain)) }
        assertFails { codec.import(ByteArray(SigningIdentityCodec.MAX_ARCHIVE_BYTES + 1), password) }
    }
}

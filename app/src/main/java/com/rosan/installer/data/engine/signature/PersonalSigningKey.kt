// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.data.engine.signature

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Exportable identities are AES-GCM encrypted; old non-exportable keys remain intact. */
class PersonalSigningKey(context: Context, private val codec: SigningIdentityCodec) {
    private val file = AtomicFile(File(context.noBackupFilesDir, "personal-signing-v2.enc"))

    @Synchronized
    fun existing(): KeyStore.PrivateKeyEntry? {
        // Recover interrupted writes; never fall back on corrupt ciphertext.
        if (hasStoredIdentity()) return decrypt(file.readFully())
        return legacy()
    }

    @Synchronized
    fun getOrCreate(): KeyStore.PrivateKeyEntry = existing() ?: codec.generate().also(::replace)

    @Synchronized
    fun legacy(): KeyStore.PrivateKeyEntry? {
        val store = keyStore()
        return if (store.containsAlias(LEGACY_ALIAS)) store.getEntry(LEGACY_ALIAS, null) as KeyStore.PrivateKeyEntry else null
    }

    @Synchronized
    fun replace(entry: KeyStore.PrivateKeyEntry) {
        codec.validate(entry)
        val encodedKey = requireNotNull(entry.privateKey.encoded) { "This key is not exportable" }
        val plain = ByteArrayOutputStream().also { output ->
            DataOutputStream(output).use { data ->
                data.writeInt(encodedKey.size)
                data.write(encodedKey)
                data.writeInt(entry.certificateChain.size)
                entry.certificateChain.forEach { certificate ->
                    val bytes = certificate.encoded
                    data.writeInt(bytes.size)
                    data.write(bytes)
                }
            }
        }.toByteArray()
        encodedKey.fill(0)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val encrypted = try {
            cipher.init(Cipher.ENCRYPT_MODE, wrappingKey(create = true))
            cipher.updateAAD(FORMAT)
            FORMAT + cipher.iv + cipher.doFinal(plain)
        } finally {
            plain.fill(0)
        }
        val stream = file.startWrite()
        try {
            stream.write(encrypted)
            file.finishWrite(stream)
            check(file.readFully().contentEquals(encrypted)) { "Cannot persist the signing identity" }
        } catch (error: Throwable) {
            file.failWrite(stream)
            throw error
        }
    }

    @Synchronized
    fun useLegacy() {
        check(legacy() != null) { "No original device-only key exists" }
        file.delete()
        check(!hasStoredIdentity()) { "Cannot switch to the original device-only key" }
    }

    private fun decrypt(bytes: ByteArray): KeyStore.PrivateKeyEntry {
        require(
            bytes.size in (FORMAT.size + IV_SIZE + 16)..SigningIdentityCodec.MAX_ARCHIVE_BYTES &&
                bytes.copyOfRange(0, FORMAT.size).contentEquals(FORMAT),
        ) { "Invalid local signing identity" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, wrappingKey(create = false), GCMParameterSpec(128, bytes, FORMAT.size, IV_SIZE))
        cipher.updateAAD(FORMAT)
        val plain = cipher.doFinal(bytes, FORMAT.size + IV_SIZE, bytes.size - FORMAT.size - IV_SIZE)
        return try {
            DataInputStream(plain.inputStream()).use { data ->
                fun readBytes(): ByteArray {
                    val size = data.readInt()
                    require(size in 1..data.available()) { "Invalid local signing identity" }
                    return ByteArray(size).also(data::readFully)
                }
                val keyBytes = readBytes()
                val key = try {
                    KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(keyBytes))
                } finally {
                    keyBytes.fill(0)
                }
                val count = data.readInt()
                require(count in 1..16) { "Invalid certificate chain" }
                val factory = CertificateFactory.getInstance("X.509")
                val chain = Array(count) { factory.generateCertificate(readBytes().inputStream()) }
                require(data.available() == 0) { "Trailing identity data" }
                KeyStore.PrivateKeyEntry(key, chain).also(codec::validate)
            }
        } finally {
            plain.fill(0)
        }
    }

    private fun wrappingKey(create: Boolean): SecretKey {
        val store = keyStore()
        (store.getKey(WRAPPING_ALIAS, null) as? SecretKey)?.let { return it }
        check(create) { "The signing identity encryption key is missing" }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(WRAPPING_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build(),
            )
        }.generateKey()
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun hasStoredIdentity(): Boolean = file.baseFile.exists() || File(file.baseFile.path + ".bak").exists()

    private companion object {
        const val LEGACY_ALIAS = "installerx.personal.apk.signing.v1"
        const val WRAPPING_ALIAS = "installerx.personal.apk.wrapping.v2"
        const val IV_SIZE = 12
        val FORMAT = byteArrayOf(73, 88, 75, 2)
    }
}

internal val KeyStore.PrivateKeyEntry.signingKey: PrivateKey get() = privateKey
internal val KeyStore.PrivateKeyEntry.signingCertificate: X509Certificate get() = certificate as X509Certificate

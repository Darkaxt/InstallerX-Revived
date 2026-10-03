// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.data.engine.signature

import android.content.ContextWrapper
import android.os.ParcelFileDescriptor
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.test.platform.app.InstrumentationRegistry
import com.rosan.installer.data.engine.repository.SigningIdentityRepositoryImpl
import java.io.File
import java.security.KeyPairGenerator
import java.security.KeyStore
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SigningIdentityBackupTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val codec = SigningIdentityCodec()

    @Test
    fun freshIdentityNeedsNoImportAndSurvivesRestart() = withStorage { context ->
        val key = PersonalSigningKey(context, codec)
        assertNull(key.existing())
        val generated = key.getOrCreate()
        assertEquals("PKCS#8", generated.privateKey.format)
        assertEquals(codec.fingerprint(generated), codec.fingerprint(PersonalSigningKey(context, codec).getOrCreate()))
    }

    @Test
    fun restoredBackupUpdatesRealInstallationAndEncryptedStorageFailsClosed() = withStorage { context ->
        val key = PersonalSigningKey(context, codec)
        key.replace(codec.generate())
        val repository = SigningIdentityRepositoryImpl(key, codec)
        val identity = key.getOrCreate()
        val fingerprint = codec.fingerprint(identity)
        val backup = repository.exportBackup("backup password".toCharArray())
        val base = asset(context.noBackupFilesDir, "base")
        val update = asset(context.noBackupFilesDir, "update")
        val signedBase = File(context.noBackupFilesDir, "signed-base.apk")
        val signedUpdate = File(context.noBackupFilesDir, "signed-update.apk")
        val signer = ApkResigner()
        try {
            shell("pm uninstall $PACKAGE")
            signer.sign(base, signedBase, identity.signingKey, identity.signingCertificate, 35)
            install(signedBase)
            shell("run-as $PACKAGE mkdir -p files")
            val descriptors = instrumentation.uiAutomation.executeShellCommandRwe("run-as $PACKAGE tee files/preserved")
            ParcelFileDescriptor.AutoCloseOutputStream(descriptors[1]).use { it.write("saved data".toByteArray()) }
            ParcelFileDescriptor.AutoCloseInputStream(descriptors[0]).use { it.readBytes() }
            ParcelFileDescriptor.AutoCloseInputStream(descriptors[2]).use { assertEquals("", it.readBytes().decodeToString()) }

            val localFile = File(context.noBackupFilesDir, "personal-signing-v2.enc")
            val encrypted = localFile.readBytes()
            val privateBytes = identity.privateKey.encoded
            assertFalse(
                (0..encrypted.size - privateBytes.size).any { offset ->
                    privateBytes.indices.all { encrypted[offset + it] == privateBytes[it] }
                },
            )
            assertEquals(fingerprint, codec.fingerprint(PersonalSigningKey(context, codec).getOrCreate()))
            val before = repository.info()
            try {
                repository.inspectBackup(backup, "wrong".toCharArray())
                error("Accepted wrong password")
            } catch (_: java.io.IOException) { }
            assertEquals(before, repository.info())
            // Simulate losing app-owned encrypted storage, then restore the portable backup.
            assertTrue(localFile.delete())
            repository.activate(repository.inspectBackup(backup, "backup password".toCharArray()))
            val restored = PersonalSigningKey(context, codec).getOrCreate()
            assertEquals(fingerprint, codec.fingerprint(restored))
            signer.sign(update, signedUpdate, restored.signingKey, restored.signingCertificate, 35)
            install(signedUpdate)
            assertEquals("saved data", shell("run-as $PACKAGE cat files/preserved").trim())
            assertEquals(2L, context.packageManager.getPackageInfo(PACKAGE, 0).longVersionCode)
            assertArrayEquals(identity.certificate.encoded, restored.certificate.encoded)

            val damaged = localFile.readBytes().apply { this[lastIndex] = (this[lastIndex].toInt() xor 1).toByte() }
            localFile.writeBytes(damaged)
            try {
                key.getOrCreate()
                error("Regenerated a corrupt identity")
            } catch (_: javax.crypto.AEADBadTagException) { }
            assertArrayEquals(damaged, localFile.readBytes())
        } finally {
            shell("pm uninstall $PACKAGE")
        }
    }

    @Test
    fun legacyIdentityIsPreservedAndCanBeSelectedAgain() = withStorage { context ->
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val created = !store.containsAlias(LEGACY)
        if (created) {
            KeyPairGenerator.getInstance("RSA", "AndroidKeyStore").apply {
                initialize(
                    KeyGenParameterSpec.Builder(LEGACY, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
                        .setKeySize(2048).setDigests("SHA-256", "SHA-512").setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1).build(),
                )
                generateKeyPair()
            }
        }
        try {
            val key = PersonalSigningKey(context, codec)
            val repo = SigningIdentityRepositoryImpl(key, codec)
            val original = requireNotNull(repo.info())
            assertFalse(original.exportable)
            assertEquals(original.fingerprint, codec.fingerprint(key.getOrCreate()))
            assertTrue(repo.exportCertificate().decodeToString().contains("BEGIN CERTIFICATE"))
            try {
                repo.exportBackup("password".toCharArray())
                error("Exported a non-exportable key")
            } catch (_: IllegalArgumentException) { }
            repo.activate(repo.generateCandidate())
            assertTrue(repo.info()!!.exportable)
            assertEquals(original.fingerprint, repo.info()!!.legacyFingerprint)
            assertNotNull(key.legacy())
            repo.useLegacy()
            assertEquals(original, repo.info())
        } finally {
            if (created) store.deleteEntry(LEGACY)
        }
    }

    private fun withStorage(block: (ContextWrapper) -> Unit) {
        val target = instrumentation.targetContext
        val directory = File(target.noBackupFilesDir, "backup-test").apply { mkdirs() }
        val context = object : ContextWrapper(target) {
            override fun getNoBackupFilesDir(): File = directory
        }
        try {
            block(context)
        } finally {
            directory.deleteRecursively()
        }
    }

    private fun asset(directory: File, name: String) = File(directory, "$name.apk").apply {
        instrumentation.context.assets.open("resigning/$name.apk").use { source -> outputStream().use { source.copyTo(it) } }
    }

    private fun install(file: File) {
        val descriptors = instrumentation.uiAutomation.executeShellCommandRwe("pm install -r -S ${file.length()}")
        ParcelFileDescriptor.AutoCloseOutputStream(descriptors[1]).use { output -> file.inputStream().use { it.copyTo(output) } }
        val output = ParcelFileDescriptor.AutoCloseInputStream(descriptors[0]).use { it.readBytes().decodeToString() }
        val errors = ParcelFileDescriptor.AutoCloseInputStream(descriptors[2]).use { it.readBytes().decodeToString() }
        assertTrue("$output $errors", output.trim().startsWith("Success"))
    }

    private fun shell(command: String): String = ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command))
        .use { it.readBytes().decodeToString() }

    private companion object {
        const val PACKAGE = "com.darkaxt.installerx.resigning.fixture"
        const val LEGACY = "installerx.personal.apk.signing.v1"
    }
}

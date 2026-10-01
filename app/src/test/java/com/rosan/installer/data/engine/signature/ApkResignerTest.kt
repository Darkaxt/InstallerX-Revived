// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.data.engine.signature

import com.android.apksig.ApkVerifier
import java.io.File
import java.nio.file.Files
import java.security.KeyFactory
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ApkResignerTest {
    private val directory = Files.createTempDirectory("apk-resigner-test").toFile()
    private val signer = ApkResigner()

    @AfterTest
    fun cleanup() {
        directory.deleteRecursively()
    }

    @Test
    fun `APK from a different signer is replaced and original bytes are preserved`() {
        val input = File(directory, "download.apk").apply { writeBytes(resource("original.apk")) }
        val downloadedBytes = input.readBytes()
        val first = File(directory, "first.apk")
        val update = File(directory, "update.apk")
        sign(input, first, "rsa-2048")
        sign(first, update, "rsa-2048_2")
        val result = ApkVerifier.Builder(update).setMinCheckedPlatformVersion(26).build().verify()
        assertTrue(result.isVerified)
        assertContentEquals(certificate("rsa-2048_2").encoded, result.signerCertificates.single().encoded)
        assertContentEquals(downloadedBytes, input.readBytes())
        assertTrue(ApkVerifier.Builder(first).setMinCheckedPlatformVersion(26).build().verify().isVerified)
    }

    @Test
    fun `same personal key signs every APK in a base and split selection`() {
        val input = File(directory, "download.apk").apply { writeBytes(resource("original.apk")) }
        val certificates = (1..3).map { index ->
            val output = File(directory, "part-$index.apk")
            sign(input, output, "rsa-2048")
            val verified = ApkVerifier.Builder(output).setMinCheckedPlatformVersion(35).build().verify()
            assertTrue(verified.isVerified)
            verified.signerCertificates.single().encoded.toList()
        }
        assertTrue(certificates.distinct().size == 1)
    }

    @Test
    fun `corrupt inputs and attempts to overwrite the original fail`() {
        val input = File(directory, "broken.apk").apply { writeText("invalid") }
        val bytes = input.readBytes()
        assertFails { sign(input, File(directory, "output.apk"), "rsa-2048") }
        assertContentEquals(bytes, input.readBytes())
        assertFails { sign(input, input, "rsa-2048") }
    }

    private fun sign(input: File, output: File, alias: String) {
        val key = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(resource("$alias.pk8")))
        signer.sign(input, output, key, certificate(alias), 35)
    }

    private fun certificate(alias: String) = CertificateFactory.getInstance("X.509")
        .generateCertificate(resource("$alias.x509.pem").inputStream()) as X509Certificate

    private fun resource(name: String) = requireNotNull(javaClass.getResourceAsStream("/resigning/$name")).use { it.readBytes() }
}

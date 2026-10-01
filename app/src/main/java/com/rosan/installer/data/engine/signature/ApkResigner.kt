// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.data.engine.signature

import com.android.apksig.ApkSigner
import com.android.apksig.ApkVerifier
import java.io.File
import java.security.PrivateKey
import java.security.cert.X509Certificate

/** Signs a copy; apksig replaces previous signers rather than adding another signer. */
class ApkResigner {
    fun sign(input: File, output: File, key: PrivateKey, certificate: X509Certificate, platformVersion: Int) {
        require(input.canonicalFile != output.canonicalFile) { "Signing must preserve the original APK" }
        val signer = ApkSigner.SignerConfig.Builder("personal", key, listOf(certificate)).build()
        ApkSigner.Builder(listOf(signer))
            .setInputApk(input)
            .setOutputApk(output)
            .setMinSdkVersion(26)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .setV4SigningEnabled(false)
            .setOtherSignersSignaturesPreserved(false)
            .build()
            .sign()
        val verified = ApkVerifier.Builder(output)
            .setMinCheckedPlatformVersion(platformVersion)
            .setMaxCheckedPlatformVersion(platformVersion)
            .build().verify()
        check(
            verified.isVerified && verified.signerCertificates.size == 1 &&
                verified.signerCertificates.single().encoded.contentEquals(certificate.encoded),
        ) { "Re-signed APK verification failed: ${verified.errors}" }
    }
}

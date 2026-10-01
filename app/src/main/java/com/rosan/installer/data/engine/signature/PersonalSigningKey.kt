// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.data.engine.signature

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal

/** Device-local identity; never share this key with the fork's release signing key. */
class PersonalSigningKey {
    @Synchronized
    fun existing(): KeyStore.PrivateKeyEntry? {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!store.containsAlias(ALIAS)) return null
        return store.getEntry(ALIAS, null) as KeyStore.PrivateKeyEntry
    }

    @Synchronized
    fun getOrCreate(): KeyStore.PrivateKeyEntry {
        existing()?.let { return it }
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore").apply {
            initialize(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
                    .setKeySize(2048)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setCertificateSubject(X500Principal("CN=InstallerX Personal APK Signing"))
                    .setCertificateSerialNumber(BigInteger(160, SecureRandom()).add(BigInteger.ONE))
                    .setCertificateNotBefore(Date(0))
                    .setCertificateNotAfter(Date(4102444800000L))
                    .build(),
            )
            generateKeyPair()
        }
        return requireNotNull(existing())
    }

    private companion object {
        const val ALIAS = "installerx.personal.apk.signing.v1"
    }
}

internal val KeyStore.PrivateKeyEntry.signingKey: PrivateKey get() = privateKey
internal val KeyStore.PrivateKeyEntry.signingCertificate: X509Certificate get() = certificate as X509Certificate

// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.domain.engine.repository

data class SigningIdentityInfo(val fingerprint: String, val exportable: Boolean, val legacyFingerprint: String?)

/** An inspected identity is activated only after the user confirms its fingerprint. */
interface SigningIdentityCandidate {
    val fingerprint: String
}

interface SigningIdentityRepository {
    fun info(): SigningIdentityInfo?
    fun exportBackup(password: CharArray): ByteArray
    fun exportCertificate(): ByteArray
    fun inspectBackup(bytes: ByteArray, password: CharArray): SigningIdentityCandidate
    fun inspectMorpheBackup(bytes: ByteArray, storePassword: CharArray, keyPassword: CharArray, alias: String): SigningIdentityCandidate
    fun generateCandidate(): SigningIdentityCandidate
    fun activate(candidate: SigningIdentityCandidate)
    fun useLegacy()

    companion object {
        const val MAX_BACKUP_BYTES = 1024 * 1024
    }
}

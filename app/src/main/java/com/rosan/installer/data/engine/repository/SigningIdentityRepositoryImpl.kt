// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.data.engine.repository

import com.rosan.installer.data.engine.signature.PersonalSigningKey
import com.rosan.installer.data.engine.signature.SigningIdentityCodec
import com.rosan.installer.domain.engine.repository.SigningIdentityCandidate
import com.rosan.installer.domain.engine.repository.SigningIdentityInfo
import com.rosan.installer.domain.engine.repository.SigningIdentityRepository
import java.security.KeyStore

class SigningIdentityRepositoryImpl(private val key: PersonalSigningKey, private val codec: SigningIdentityCodec) : SigningIdentityRepository {
    override fun info(): SigningIdentityInfo? = key.existing()?.let { entry ->
        SigningIdentityInfo(codec.fingerprint(entry), entry.privateKey.format == "PKCS#8", key.legacy()?.let(codec::fingerprint))
    }

    override fun exportBackup(password: CharArray): ByteArray = codec.export(requireNotNull(key.existing()) { "No signing identity exists" }, password)

    override fun exportCertificate(): ByteArray = codec.publicCertificate(requireNotNull(key.existing()) { "No signing identity exists" })

    override fun inspectBackup(bytes: ByteArray, password: CharArray): SigningIdentityCandidate = candidate(codec.import(bytes, password))

    override fun generateCandidate(): SigningIdentityCandidate = candidate(codec.generate())

    override fun activate(candidate: SigningIdentityCandidate) {
        require(candidate is Candidate) { "Invalid signing identity candidate" }
        key.replace(candidate.entry)
    }

    override fun useLegacy() = key.useLegacy()

    private fun candidate(entry: KeyStore.PrivateKeyEntry) = Candidate(codec.fingerprint(entry), entry)

    private class Candidate(override val fingerprint: String, val entry: KeyStore.PrivateKeyEntry) : SigningIdentityCandidate
}

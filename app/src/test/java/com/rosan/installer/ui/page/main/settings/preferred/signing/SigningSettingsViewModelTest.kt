// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.ui.page.main.settings.preferred.signing

import com.rosan.installer.domain.engine.repository.SigningIdentityCandidate
import com.rosan.installer.domain.engine.repository.SigningIdentityInfo
import com.rosan.installer.domain.engine.repository.SigningIdentityRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SigningSettingsViewModelTest {
    @Test
    fun importAndGenerationDoNotReplaceIdentityBeforeConfirmation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = Repository()
            val model = SigningSettingsViewModel(repository)
            model.state.first { !it.busy }
            model.generateCandidate()
            model.state.first { !it.busy }
            assertNotNull(model.state.value.candidate)
            assertEquals(0, repository.activations)
            model.cancelCandidate()
            assertNull(model.state.value.candidate)

            model.loadImport { byteArrayOf(1, 2, 3) }
            model.state.first { !it.busy }
            val wrong = "wrong".toCharArray()
            model.inspectImport(wrong)
            model.state.first { !it.busy }
            assertEquals(SigningMessage.IMPORT_FAILED, model.state.value.message)
            assertTrue(wrong.all { it == '\u0000' })
            assertEquals(0, repository.activations)
            assertTrue(model.state.value.importPasswordRequired)
            val password = "password".toCharArray()
            model.inspectImport(password)
            model.state.first { !it.busy }
            assertTrue(password.all { it == '\u0000' })
            assertEquals("selected", model.state.value.candidate!!.fingerprint)
            assertEquals(0, repository.activations)
            model.confirmCandidate()
            model.state.first { !it.busy }
            assertEquals(1, repository.activations)
            assertEquals("selected", model.state.value.info!!.fingerprint)
            assertNull(model.state.value.candidate)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun documentCancellationAndWriteFailureLeaveIdentityUnchanged() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = Repository()
            val model = SigningSettingsViewModel(repository)
            model.state.first { !it.busy }
            val password = "password".toCharArray()
            model.export(password)
            val request = model.exports.first()
            assertFalse(request.publicOnly)
            assertTrue(password.all { it == '\u0000' })
            assertTrue(model.state.value.busy)
            model.finishExport(null)
            model.state.first { !it.busy }
            assertEquals(0, repository.activations)
            model.export()
            assertTrue(model.exports.first().publicOnly)
            model.finishExport { error("Storage unavailable") }
            model.state.first { !it.busy }
            assertEquals(SigningMessage.EXPORT_FAILED, model.state.value.message)
            assertEquals("original", model.state.value.info!!.fingerprint)
            assertEquals(0, repository.activations)
            model.loadImport { byteArrayOf(1) }
            model.state.first { !it.busy }
            model.cancelImport()
            assertFalse(model.state.value.importPasswordRequired)
            assertNull(model.state.value.candidate)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun morpheImportRoutesCredentialsAndRequiresExplicitActivation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = Repository()
            val model = SigningSettingsViewModel(repository)
            model.state.first { !it.busy }
            model.loadImport { byteArrayOf(0, 0, 0, 2, 1) }
            model.state.first { !it.busy }
            assertTrue(model.state.value.importIsBks)
            val wrong = "wrong".toCharArray()
            val emptyStore = charArrayOf()
            model.inspectMorpheImport("Morphe", emptyStore, wrong)
            model.state.first { !it.busy }
            assertTrue(wrong.all { it == '\u0000' })
            assertEquals(SigningMessage.IMPORT_FAILED, model.state.value.message)
            assertEquals(0, repository.activations)
            val password = "password".toCharArray()
            model.inspectMorpheImport("Morphe", charArrayOf(), password)
            model.state.first { !it.busy }
            assertTrue(password.all { it == '\u0000' })
            assertNotNull(model.state.value.candidate)
            assertEquals(0, repository.activations)
            model.confirmCandidate()
            model.state.first { !it.busy }
            assertEquals(1, repository.activations)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class Repository : SigningIdentityRepository {
        var activations = 0
        private var current = "original"
        override fun info() = SigningIdentityInfo(current, true, null)
        override fun exportBackup(password: CharArray) = byteArrayOf(1, 2, 3)
        override fun exportCertificate() = byteArrayOf(4, 5)
        override fun inspectBackup(bytes: ByteArray, password: CharArray): SigningIdentityCandidate {
            require(password.concatToString() == "password")
            return generateCandidate()
        }
        override fun inspectMorpheBackup(bytes: ByteArray, storePassword: CharArray, keyPassword: CharArray, alias: String): SigningIdentityCandidate {
            require(alias == "Morphe" && keyPassword.concatToString() == "password" && storePassword.isEmpty())
            return generateCandidate()
        }
        override fun generateCandidate() = object : SigningIdentityCandidate {
            override val fingerprint = "selected"
        }
        override fun activate(candidate: SigningIdentityCandidate) {
            current = candidate.fingerprint
            activations++
        }
        override fun useLegacy() {
            error("No legacy identity")
        }
    }
}

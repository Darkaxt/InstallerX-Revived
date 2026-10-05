// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.ui.page.main.settings.preferred.signing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.installer.domain.engine.repository.SigningIdentityCandidate
import com.rosan.installer.domain.engine.repository.SigningIdentityInfo
import com.rosan.installer.domain.engine.repository.SigningIdentityRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SigningSettingsState(
    val info: SigningIdentityInfo? = null,
    val busy: Boolean = false,
    val importPasswordRequired: Boolean = false,
    val importIsBks: Boolean = false,
    val candidate: SigningIdentityCandidate? = null,
    val message: SigningMessage? = null,
)

enum class SigningMessage { READ_FAILED, IMPORT_FAILED, EXPORT_FAILED, CHANGE_FAILED, EXPORTED, CHANGED }

data class SigningExportRequest(val fileName: String, val publicOnly: Boolean)

class SigningSettingsViewModel(private val repository: SigningIdentityRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(SigningSettingsState())
    val state = mutableState.asStateFlow()
    private val exportRequests = Channel<SigningExportRequest>(Channel.BUFFERED)
    val exports = exportRequests.receiveAsFlow()
    private var importBytes: ByteArray? = null
    private var exportBytes: ByteArray? = null

    init {
        refresh()
    }

    fun refresh() = operation(SigningMessage.READ_FAILED) {
        val info = withContext(Dispatchers.IO) { repository.info() }
        mutableState.update { it.copy(info = info) }
    }

    fun loadImport(read: () -> ByteArray) = operation(SigningMessage.READ_FAILED) {
        clearImport()
        importBytes = withContext(Dispatchers.IO) { read() }
        val bytes = requireNotNull(importBytes)
        val isBks = bytes.size >= 4 && bytes[0] == 0.toByte() && bytes[1] == 0.toByte() && bytes[2] == 0.toByte() && bytes[3].toInt() in 1..2
        mutableState.update { it.copy(importPasswordRequired = true, importIsBks = isBks) }
    }

    fun inspectImport(password: CharArray) {
        if (state.value.busy) {
            password.fill('\u0000')
            return
        }
        operation(SigningMessage.IMPORT_FAILED) {
            try {
                val bytes = requireNotNull(importBytes)
                val candidate = withContext(Dispatchers.IO) { repository.inspectBackup(bytes, password) }
                clearImport()
                mutableState.update { it.copy(candidate = candidate, importPasswordRequired = false) }
            } finally {
                password.fill('\u0000')
            }
        }
    }

    fun inspectMorpheImport(alias: String, storePassword: CharArray, keyPassword: CharArray) {
        if (state.value.busy) {
            storePassword.fill('\u0000')
            keyPassword.fill('\u0000')
            return
        }
        operation(SigningMessage.IMPORT_FAILED) {
            try {
                val bytes = requireNotNull(importBytes)
                val candidate = withContext(Dispatchers.IO) { repository.inspectMorpheBackup(bytes, storePassword, keyPassword, alias) }
                clearImport()
                mutableState.update { it.copy(candidate = candidate, importPasswordRequired = false, importIsBks = false) }
            } finally {
                storePassword.fill('\u0000')
                keyPassword.fill('\u0000')
            }
        }
    }

    fun cancelImport() {
        if (state.value.busy) return
        clearImport()
        mutableState.update { it.copy(importPasswordRequired = false, message = null) }
    }

    fun generateCandidate() = operation(SigningMessage.CHANGE_FAILED) {
        val candidate = withContext(Dispatchers.IO) { repository.generateCandidate() }
        mutableState.update { it.copy(candidate = candidate) }
    }

    fun cancelCandidate() {
        if (!state.value.busy) mutableState.update { it.copy(candidate = null) }
    }

    fun confirmCandidate() = operation(SigningMessage.CHANGE_FAILED) {
        val candidate = requireNotNull(state.value.candidate)
        withContext(Dispatchers.IO) { repository.activate(candidate) }
        val info = withContext(Dispatchers.IO) { repository.info() }
        mutableState.update { it.copy(info = info, candidate = null, message = SigningMessage.CHANGED) }
    }

    fun confirmLegacy() = operation(SigningMessage.CHANGE_FAILED) {
        withContext(Dispatchers.IO) { repository.useLegacy() }
        val info = withContext(Dispatchers.IO) { repository.info() }
        mutableState.update { it.copy(info = info, message = SigningMessage.CHANGED) }
    }

    fun export(password: CharArray? = null) {
        if (state.value.busy) {
            password?.fill('\u0000')
            return
        }
        operation(SigningMessage.EXPORT_FAILED, keepBusy = true) {
            try {
                exportBytes = withContext(Dispatchers.IO) {
                    if (password == null) repository.exportCertificate() else repository.exportBackup(password)
                }
                val suffix = if (password == null) "pem" else "p12"
                val fingerprint = state.value.info?.fingerprint?.take(12).orEmpty()
                exportRequests.send(SigningExportRequest("installerx-signing-$fingerprint.$suffix", password == null))
            } finally {
                password?.fill('\u0000')
            }
        }
    }

    /** The document result belongs to the pending export; cancellation never changes identity. */
    fun finishExport(write: ((ByteArray) -> Unit)?) {
        val bytes = exportBytes
        exportBytes = null
        viewModelScope.launch {
            try {
                if (write != null) {
                    withContext(Dispatchers.IO) { write(requireNotNull(bytes)) }
                    mutableState.update { it.copy(message = SigningMessage.EXPORTED) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(message = SigningMessage.EXPORT_FAILED) }
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }.invokeOnCompletion { bytes?.fill(0) }
    }

    private fun operation(failure: SigningMessage, keepBusy: Boolean = false, block: suspend () -> Unit) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            var succeeded = false
            try {
                block()
                succeeded = true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(message = failure) }
            } finally {
                if (!keepBusy || !succeeded) mutableState.update { it.copy(busy = false) }
            }
        }
    }

    private fun clearImport() {
        importBytes?.fill(0)
        importBytes = null
    }

    override fun onCleared() {
        clearImport()
        exportBytes?.fill(0)
        exportBytes = null
        exportRequests.close()
    }
}

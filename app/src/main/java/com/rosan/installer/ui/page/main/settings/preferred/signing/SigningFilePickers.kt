// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.ui.page.main.settings.preferred.signing

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.rosan.installer.R
import com.rosan.installer.domain.engine.repository.SigningIdentityRepository
import java.io.ByteArrayOutputStream

/** Shared document workflow; pages keep their native controls and dialogs. */
@Composable
fun rememberSigningImportPicker(viewModel: SigningSettingsViewModel): () -> Unit {
    val context = LocalContext.current
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.loadImport {
                requireNotNull(context.contentResolver.openInputStream(uri)).use { input ->
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        require(output.size() + count <= SigningIdentityRepository.MAX_BACKUP_BYTES) { "Signing backup is too large" }
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray()
                }
            }
        }
    }
    val backupPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/x-pkcs12")) { uri ->
        viewModel.finishExport(
            uri?.let { destination ->
                { bytes -> requireNotNull(context.contentResolver.openOutputStream(destination, "wt")).use { it.write(bytes) } }
            },
        )
    }
    val certificatePicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/x-pem-file")) { uri ->
        viewModel.finishExport(
            uri?.let { destination ->
                { bytes -> requireNotNull(context.contentResolver.openOutputStream(destination, "wt")).use { it.write(bytes) } }
            },
        )
    }
    LaunchedEffect(viewModel) {
        viewModel.exports.collect { request ->
            try {
                if (request.publicOnly) certificatePicker.launch(request.fileName) else backupPicker.launch(request.fileName)
            } catch (_: Exception) {
                viewModel.finishExport { error("Cannot launch the document picker") }
            }
        }
    }
    return {
        try {
            importPicker.launch(arrayOf("*/*"))
        } catch (_: Exception) {
            viewModel.loadImport { error("Cannot launch the document picker") }
        }
    }
}

fun SigningMessage.stringResourceId(): Int = when (this) {
    SigningMessage.READ_FAILED -> R.string.signing_read_failed
    SigningMessage.IMPORT_FAILED -> R.string.signing_import_failed
    SigningMessage.EXPORT_FAILED -> R.string.signing_export_failed
    SigningMessage.CHANGE_FAILED -> R.string.signing_change_failed
    SigningMessage.EXPORTED -> R.string.signing_exported
    SigningMessage.CHANGED -> R.string.signing_changed
}

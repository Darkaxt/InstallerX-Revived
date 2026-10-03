// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.ui.page.main.settings.preferred.signing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rosan.installer.R
import com.rosan.installer.ui.icons.AppIcons
import com.rosan.installer.ui.navigation.LocalNavigator
import com.rosan.installer.ui.page.main.widget.setting.BaseWidget
import com.rosan.installer.ui.page.main.widget.setting.ExpressiveBackButton
import com.rosan.installer.ui.page.main.widget.setting.SegmentedColumn
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SigningSettingsPage(viewModel: SigningSettingsViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val importBackup = rememberSigningImportPicker(viewModel)
    var exportPassword by remember { mutableStateOf(false) }
    var restoreLegacy by remember { mutableStateOf(false) }
    val info = state.info

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.signing_settings)) }, navigationIcon = { ExpressiveBackButton { navigator.pop() } })
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = padding) {
            item {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.signing_identity_desc))
                    Text(stringResource(R.string.signing_fingerprint))
                    Text(info?.fingerprint ?: stringResource(R.string.signing_no_identity))
                    if (info != null) Text(stringResource(if (info.exportable) R.string.signing_portable else R.string.signing_legacy))
                    state.message?.let { Text(stringResource(it.stringResourceId())) }
                    if (state.busy) Text(stringResource(R.string.signing_busy))
                }
            }
            item {
                SegmentedColumn(title = stringResource(R.string.signing_backup)) {
                    item { BaseWidget(icon = AppIcons.Save, title = stringResource(R.string.signing_export_backup), description = stringResource(R.string.signing_export_backup_desc), enabled = !state.busy && info?.exportable == true, onClick = { exportPassword = true }) }
                    item { BaseWidget(icon = AppIcons.Download, title = stringResource(R.string.signing_import_backup), description = stringResource(R.string.signing_import_backup_desc), enabled = !state.busy, onClick = importBackup) }
                    item { BaseWidget(icon = AppIcons.Save, title = stringResource(R.string.signing_export_certificate), description = stringResource(R.string.signing_export_certificate_desc), enabled = !state.busy && info != null, onClick = { viewModel.export() }) }
                }
            }
            item {
                SegmentedColumn(title = stringResource(R.string.signing_identity)) {
                    item { BaseWidget(icon = AppIcons.Retry, title = stringResource(R.string.signing_generate), description = stringResource(R.string.signing_generate_desc), enabled = !state.busy, onClick = { viewModel.generateCandidate() }) }
                    if (info?.legacyFingerprint != null && info.exportable) {
                        item { BaseWidget(icon = AppIcons.Retry, title = stringResource(R.string.signing_restore_legacy), description = stringResource(R.string.signing_restore_legacy_desc), enabled = !state.busy, onClick = { restoreLegacy = true }) }
                    }
                }
            }
        }
    }
    if (exportPassword || state.importPasswordRequired) {
        SigningPasswordDialog(
            export = exportPassword,
            busy = state.busy,
            error = state.message?.takeIf { it == SigningMessage.IMPORT_FAILED },
            onDismiss = {
                if (!state.busy) {
                    exportPassword = false
                    viewModel.cancelImport()
                }
            },
            onConfirm = { password ->
                if (exportPassword) {
                    exportPassword = false
                    viewModel.export(password)
                } else {
                    viewModel.inspectImport(password)
                }
            },
        )
    }
    state.candidate?.let { candidate ->
        SigningReplacementDialog(
            current = info?.fingerprint,
            next = candidate.fingerprint,
            busy = state.busy,
            onDismiss = viewModel::cancelCandidate,
            onConfirm = { viewModel.confirmCandidate() },
        )
    }
    if (restoreLegacy) {
        SigningReplacementDialog(
            current = info?.fingerprint,
            next = info?.legacyFingerprint.orEmpty(),
            busy = state.busy,
            onDismiss = { if (!state.busy) restoreLegacy = false },
            onConfirm = {
                restoreLegacy = false
                viewModel.confirmLegacy()
            },
        )
    }
}

@Composable
private fun SigningPasswordDialog(export: Boolean, busy: Boolean, error: SigningMessage?, onDismiss: () -> Unit, onConfirm: (CharArray) -> Unit) {
    // Passwords deliberately use remember, never rememberSaveable.
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (export) R.string.signing_export_backup else R.string.signing_import_backup)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(if (export) R.string.signing_password_export_desc else R.string.signing_password_import_desc))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(stringResource(R.string.signing_password)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), enabled = !busy)
                if (export) OutlinedTextField(value = confirmation, onValueChange = { confirmation = it }, label = { Text(stringResource(R.string.signing_password_confirm)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), enabled = !busy)
                error?.let { Text(stringResource(it.stringResourceId())) }
            }
        },
        confirmButton = {
            TextButton(enabled = !busy && password.isNotEmpty() && (!export || password == confirmation), onClick = {
                val chars = password.toCharArray()
                password = ""
                confirmation = ""
                onConfirm(chars)
            }) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun SigningReplacementDialog(current: String?, next: String, busy: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.signing_replace_title)) },
        text = { Text(stringResource(R.string.signing_replace_warning, current ?: stringResource(R.string.signing_no_identity), next)) },
        confirmButton = { TextButton(enabled = !busy, onClick = onConfirm) { Text(stringResource(R.string.confirm)) } },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

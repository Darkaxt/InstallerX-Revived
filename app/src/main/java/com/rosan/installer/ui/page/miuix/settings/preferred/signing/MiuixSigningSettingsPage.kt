// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.ui.page.miuix.settings.preferred.signing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
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
import com.rosan.installer.ui.navigation.LocalNavigator
import com.rosan.installer.ui.page.main.settings.preferred.signing.SigningMessage
import com.rosan.installer.ui.page.main.settings.preferred.signing.SigningSettingsViewModel
import com.rosan.installer.ui.page.main.settings.preferred.signing.rememberSigningImportPicker
import com.rosan.installer.ui.page.main.settings.preferred.signing.stringResourceId
import com.rosan.installer.ui.page.miuix.widgets.MiuixBackButton
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun MiuixSigningSettingsPage(viewModel: SigningSettingsViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val importBackup = rememberSigningImportPicker(viewModel)
    var exportPassword by remember { mutableStateOf(false) }
    var restoreLegacy by remember { mutableStateOf(false) }
    val info = state.info
    Scaffold(topBar = {
        TopAppBar(title = stringResource(R.string.signing_settings), navigationIcon = { MiuixBackButton { navigator.pop() } })
    }) { padding ->
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
            item { SmallTitle(stringResource(R.string.signing_backup)) }
            item {
                Card(Modifier.padding(horizontal = 12.dp)) {
                    BasicComponent(title = stringResource(R.string.signing_export_backup), summary = stringResource(R.string.signing_export_backup_desc), enabled = !state.busy && info?.exportable == true, onClick = { exportPassword = true })
                    BasicComponent(title = stringResource(R.string.signing_import_backup), summary = stringResource(R.string.signing_import_backup_desc), enabled = !state.busy, onClick = importBackup)
                    BasicComponent(title = stringResource(R.string.signing_export_certificate), summary = stringResource(R.string.signing_export_certificate_desc), enabled = !state.busy && info != null, onClick = { viewModel.export() })
                }
            }
            item { SmallTitle(stringResource(R.string.signing_identity)) }
            item {
                Card(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                    BasicComponent(title = stringResource(R.string.signing_generate), summary = stringResource(R.string.signing_generate_desc), enabled = !state.busy, onClick = { viewModel.generateCandidate() })
                    if (info?.legacyFingerprint != null && info.exportable) {
                        BasicComponent(title = stringResource(R.string.signing_restore_legacy), summary = stringResource(R.string.signing_restore_legacy_desc), enabled = !state.busy, onClick = { restoreLegacy = true })
                    }
                }
            }
        }
    }
    if (exportPassword || state.importPasswordRequired) {
        MiuixSigningPasswordDialog(
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
        MiuixSigningReplacementDialog(
            current = info?.fingerprint,
            next = candidate.fingerprint,
            busy = state.busy,
            onDismiss = viewModel::cancelCandidate,
            onConfirm = { viewModel.confirmCandidate() },
        )
    }
    if (restoreLegacy) {
        MiuixSigningReplacementDialog(
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
private fun MiuixSigningPasswordDialog(export: Boolean, busy: Boolean, error: SigningMessage?, onDismiss: () -> Unit, onConfirm: (CharArray) -> Unit) {
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    WindowDialog(
        show = true,
        onDismissRequest = onDismiss,
        title = stringResource(if (export) R.string.signing_export_backup else R.string.signing_import_backup),
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(if (export) R.string.signing_password_export_desc else R.string.signing_password_import_desc))
                TextField(value = password, onValueChange = { password = it }, label = stringResource(R.string.signing_password), modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), enabled = !busy)
                if (export) TextField(value = confirmation, onValueChange = { confirmation = it }, label = stringResource(R.string.signing_password_confirm), modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), enabled = !busy)
                error?.let { Text(stringResource(it.stringResourceId())) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(text = stringResource(R.string.cancel), modifier = Modifier.weight(1f), enabled = !busy, onClick = onDismiss)
                    TextButton(text = stringResource(R.string.confirm), modifier = Modifier.weight(1f), enabled = !busy && password.isNotEmpty() && (!export || password == confirmation), colors = ButtonDefaults.textButtonColorsPrimary(), onClick = {
                        val chars = password.toCharArray()
                        password = ""
                        confirmation = ""
                        onConfirm(chars)
                    })
                }
            }
        },
    )
}

@Composable
private fun MiuixSigningReplacementDialog(current: String?, next: String, busy: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    WindowDialog(
        show = true,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.signing_replace_title),
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(stringResource(R.string.signing_replace_warning, current ?: stringResource(R.string.signing_no_identity), next))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(text = stringResource(R.string.cancel), modifier = Modifier.weight(1f), enabled = !busy, onClick = onDismiss)
                    TextButton(text = stringResource(R.string.confirm), modifier = Modifier.weight(1f), enabled = !busy, colors = ButtonDefaults.textButtonColorsPrimary(), onClick = onConfirm)
                }
            }
        },
    )
}

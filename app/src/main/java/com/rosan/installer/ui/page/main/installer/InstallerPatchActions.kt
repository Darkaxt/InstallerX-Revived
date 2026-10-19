// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.ui.page.main.installer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.rosan.installer.R
import com.rosan.installer.framework.morphe.MorpheHandoff
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton

/** Compact, labelled actions; availability is refreshed when returning from another app. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallerPatchActions(sourceUri: String?, canResign: Boolean, useMiuix: Boolean = false, onPatch: () -> Unit, onResign: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val morpheIcon by produceState<ImageBitmap?>(null, context, lifecycleOwner, sourceUri) {
        if (sourceUri == null) {
            value = null
            return@produceState
        }
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            value = withContext(Dispatchers.IO) {
                MorpheHandoff.receiver(context)?.loadIcon(context.packageManager)?.toBitmap(96, 96)?.asImageBitmap()
            }
            awaitCancellation()
        }
    }
    if ((sourceUri == null || morpheIcon == null) && !canResign) return
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (sourceUri != null && morpheIcon != null) {
            PatchIconButton(stringResource(R.string.patch_in_morphe), useMiuix, onPatch) {
                Image(requireNotNull(morpheIcon), contentDescription = stringResource(R.string.patch_in_morphe), modifier = Modifier.size(28.dp))
            }
        }
        if (canResign) {
            PatchIconButton(stringResource(R.string.resign), useMiuix, onResign) {
                if (useMiuix) {
                    MiuixIcon(Icons.Outlined.Healing, contentDescription = stringResource(R.string.resign), modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Outlined.Healing, contentDescription = stringResource(R.string.resign), modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatchIconButton(label: String, useMiuix: Boolean, onClick: () -> Unit, icon: @Composable () -> Unit) {
    TooltipBox(positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(), tooltip = { PlainTooltip { Text(label) } }, state = rememberTooltipState()) {
        if (useMiuix) {
            MiuixIconButton(onClick = onClick, modifier = Modifier.size(48.dp), content = icon)
        } else {
            IconButton(onClick = onClick, modifier = Modifier.size(48.dp), content = icon)
        }
    }
}

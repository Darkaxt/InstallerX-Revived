// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.framework.morphe

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Morphe Extended's existing exported APK-share contract; no patching happens here. */
object MorpheHandoff {
    const val PACKAGE_NAME = "app.morphe.extended"
    const val APK_MIME_TYPE = "application/vnd.android.package-archive"

    fun receiver(context: Context): ResolveInfo? {
        @Suppress("DEPRECATION")
        val resolved = context.packageManager.resolveActivity(
            Intent(Intent.ACTION_SEND).setPackage(PACKAGE_NAME).setType(APK_MIME_TYPE),
            PackageManager.MATCH_DEFAULT_ONLY,
        ) ?: return null
        return resolved.takeIf { it.activityInfo.exported && it.activityInfo.enabled && it.activityInfo.applicationInfo.enabled }
    }

    fun createIntent(context: Context, source: Uri): Intent {
        check(receiver(context) != null) { "Morphe Extended is not available" }
        val uri = when (source.scheme) {
            "content" -> source
            "file" -> FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(requireNotNull(source.path)))
            else -> error("Unsupported APK source")
        }
        return Intent(Intent.ACTION_SEND).apply {
            setPackage(PACKAGE_NAME)
            type = APK_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("APK", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

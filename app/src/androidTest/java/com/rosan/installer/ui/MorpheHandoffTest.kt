// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.ui

import android.content.Intent
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import com.rosan.installer.domain.settings.model.config.Authorizer
import com.rosan.installer.domain.settings.repository.AppSettingsRepository
import com.rosan.installer.domain.settings.repository.BooleanSetting
import com.rosan.installer.domain.settings.repository.StringSetting
import com.rosan.installer.framework.morphe.MorpheHandoff
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.core.context.GlobalContext

/** Run absentReceiver before installing Morphe, and installedReceiver afterwards. */
class MorpheHandoffTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /** Isolated-emulator setup only; actual handoff is exercised through native UI clicks. */
    @Test
    fun prepareRuntimeFixture() = runBlocking {
        val settings = GlobalContext.get().get<AppSettingsRepository>()
        settings.putString(StringSetting.Authorizer, Authorizer.None.value)
        settings.putBoolean(BooleanSetting.UiUseMiuix, InstrumentationRegistry.getArguments().getString("miuix") == "true")
        settings.putBoolean(BooleanSetting.DialogDisableNotificationOnDismiss, true)
        val file = File(context.filesDir, "morphe-runtime-base.apk")
        InstrumentationRegistry.getInstrumentation().context.assets.open("resigning/base.apk").use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
        InstrumentationRegistry.getInstrumentation().sendStatus(
            0,
            android.os.Bundle().apply {
                putString("stream", "MORPHE_FIXTURE uri=${FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)}\n")
            },
        )
    }

    @Test
    fun absentReceiverDoesNotOfferOrLaunchMorphe() {
        assertNull(MorpheHandoff.receiver(context))
        assertTrue(runCatching { MorpheHandoff.createIntent(context, "content://downloads/example.apk".toUri()) }.isFailure)
    }

    @Test
    fun installedReceiverUsesExactOriginalReadOnlyShareAndWrappedFiles() {
        assertNotNull(MorpheHandoff.receiver(context))
        val original = File(context.externalCacheDir, "morphe-handoff-contract.apk")
        val bytes = "Unmodified APK fixture bytes".toByteArray()
        original.writeBytes(bytes)
        try {
            val originalUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", original)
            for (uri in listOf(originalUri, original.toURI().toString().toUri())) {
                val intent = MorpheHandoff.createIntent(context, uri)
                assertEquals(Intent.ACTION_SEND, intent.action)
                assertEquals(MorpheHandoff.PACKAGE_NAME, intent.`package`)
                assertEquals(MorpheHandoff.APK_MIME_TYPE, intent.type)
                assertEquals(originalUri, intent.getParcelableExtra(Intent.EXTRA_STREAM, android.net.Uri::class.java))
                assertEquals(originalUri, intent.clipData!!.getItemAt(0).uri)
                assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION, intent.flags)
                assertArrayEquals(bytes, context.contentResolver.openInputStream(originalUri)!!.use { it.readBytes() })
            }
            assertTrue(runCatching { MorpheHandoff.createIntent(context, "https://example.com/file.apk".toUri()) }.isFailure)
        } finally {
            original.delete()
        }
    }
}

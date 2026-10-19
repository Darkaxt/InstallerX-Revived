// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.ui

import android.app.Activity
import android.app.UiAutomation
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import com.rosan.installer.R
import com.rosan.installer.domain.engine.repository.SigningIdentityRepository
import com.rosan.installer.domain.settings.provider.ThemeStateProvider
import com.rosan.installer.domain.settings.repository.AppSettingsRepository
import com.rosan.installer.domain.settings.repository.BooleanSetting
import com.rosan.installer.ui.activity.SettingsActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.core.context.GlobalContext

/** Uses native accessibility actions without suppressing existing accessibility services. */
class SigningSettingsUiTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val automation = instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)

    @Test
    fun bothInterfacesRenderSigningControlsAndCancelReplacement() = runBlocking {
        val settings = GlobalContext.get().get<AppSettingsRepository>()
        val identities = GlobalContext.get().get<SigningIdentityRepository>()
        val originalStyle = settings.getBoolean(BooleanSetting.UiUseMiuix).first()
        try {
            for (miuix in listOf(false, true)) {
                settings.putBoolean(BooleanSetting.UiUseMiuix, miuix)
                GlobalContext.get().get<ThemeStateProvider>().themeStateFlow.first { it.isLoaded && it.useMiuix == miuix }
                val originalFingerprint = identities.info()?.fingerprint
                var activity: Activity? = null
                try {
                    observe(R.string.preferred) {
                        activity = instrumentation.startActivitySync(
                            Intent(context, SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                    navigate(R.string.preferred, R.string.theme_settings)
                    scrollTo(R.string.signing_settings)
                    navigate(R.string.signing_settings, R.string.signing_fingerprint)
                    for (label in listOf(R.string.signing_export_backup, R.string.signing_import_backup)) {
                        scrollTo(label)
                        assertNotNull(find(currentRoot(), context.getString(label)))
                    }
                    scrollTo(R.string.signing_generate)
                    navigate(R.string.signing_generate, R.string.signing_replace_title)
                    navigate(R.string.cancel, R.string.signing_generate)
                    assertEquals(originalFingerprint, identities.info()?.fingerprint)
                    val bounds = Rect().also { requireNotNull(currentRoot()).getBoundsInScreen(it) }
                    instrumentation.sendStatus(
                        0,
                        Bundle().apply {
                            putString("stream", "SIGNING_UI style=${if (miuix) "miuix" else "material"} bounds=$bounds controls=present replacement=cancelled identity=preserved\n")
                        },
                    )
                } finally {
                    instrumentation.runOnMainSync { activity?.finish() }
                }
            }
        } finally {
            settings.putBoolean(BooleanSetting.UiUseMiuix, originalStyle)
        }
    }

    private fun navigate(label: Int, destination: Int) = observe(destination) {
        var node = requireNotNull(find(currentRoot(), context.getString(label)))
        while (!node.isClickable) node = requireNotNull(node.parent)
        assertTrue(node.isEnabled && node.isVisibleToUser)
        assertTrue(node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
    }

    private fun scrollTo(label: Int) {
        if (find(currentRoot(), context.getString(label)) != null) return
        fun scrollable(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            node ?: return null
            if (node.isVisibleToUser && node.actionList.any { it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id }) return node
            for (index in 0 until node.childCount) scrollable(node.getChild(index))?.let { return it }
            return null
        }
        observe(label) {
            assertTrue(requireNotNull(scrollable(currentRoot())).performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id))
        }
    }

    private fun observe(destination: Int, action: () -> Unit) {
        // UiAutomation mandates an observation timeout; expiry fails the test, never product logic.
        try {
            automation.executeAndWaitForEvent(
                action,
                { find(currentRoot(), context.getString(destination)) != null },
                30_000L,
            ).recycle()
        } catch (error: java.util.concurrent.TimeoutException) {
            // Accessibility events may be coalesced/omitted; actual destination state is authoritative.
            if (find(currentRoot(), context.getString(destination)) != null) return
            val nodes = JSONArray()
            fun snapshot(node: AccessibilityNodeInfo?) {
                node ?: return
                if (nodes.length() >= 200) return
                val bounds = Rect().also(node::getBoundsInScreen)
                nodes.put(
                    JSONObject().put("text", node.text).put("description", node.contentDescription)
                        .put("package", node.packageName).put("visible", node.isVisibleToUser)
                        .put("clickable", node.isClickable).put("bounds", bounds.toShortString()),
                )
                for (index in 0 until node.childCount) snapshot(node.getChild(index))
            }
            snapshot(currentRoot())
            instrumentation.sendStatus(0, Bundle().apply { putString("stream", "UI_TIMEOUT destination=${context.getString(destination)} nodes=$nodes\n") })
            throw error
        }
        assertNotNull(find(currentRoot(), context.getString(destination)))
    }

    private fun currentRoot(): AccessibilityNodeInfo? {
        if (Build.VERSION.SDK_INT >= 33) automation.clearCache()
        return automation.rootInActiveWindow
    }

    private fun find(node: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        node ?: return null
        if (node.packageName == context.packageName && node.isVisibleToUser &&
            (node.text?.lineSequence()?.any { it == text } == true || node.contentDescription?.toString() == text)
        ) {
            return node
        }
        for (index in 0 until node.childCount) find(node.getChild(index), text)?.let { return it }
        return null
    }
}

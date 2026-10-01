// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.domain.engine.usecase

import com.rosan.installer.domain.engine.model.install.ResigningPolicy
import com.rosan.installer.domain.session.model.InstallSourceConfidence
import com.rosan.installer.domain.settings.model.config.ConfigModel
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResigningPolicyTest {
    @Test
    fun `only exact current personal signer enables automatic signing`() {
        assertTrue(decide(installed = setOf("personal")))
        assertFalse(decide(installed = setOf("other")))
        assertFalse(decide(installed = setOf("personal", "other")))
        assertFalse(decide(installed = emptySet()))
        assertFalse(ResigningPolicy.shouldResign(ConfigModel.default, false, null, setOf("personal")))
        assertTrue(decide(explicit = true, installed = setOf("other")))
    }

    @Test
    fun `official stores exclude automatic and explicit signing`() {
        val stores = listOf(
            "com.android.vending",
            "com.sec.android.app.samsungapps",
            "com.amazon.venezia",
            "com.huawei.appmarket",
            "com.xiaomi.mipicks",
            "com.xiaomi.market",
        )
        stores.forEach { store ->
            listOf(
                InstallSourceConfidence.EXACT_CALLER,
                InstallSourceConfidence.LAUNCHED_FROM_UID,
                InstallSourceConfidence.TRUSTED_PROXY_ORIGINATING_UID,
                InstallSourceConfidence.PROVIDER_OWNER,
            ).forEach { confidence ->
                val config = ConfigModel.default.copy(initiatorPackageName = store, installSourceConfidence = confidence)
                assertTrue(ResigningPolicy.isOfficialSource(config))
                assertFalse(decide(config, installed = setOf("personal")))
                assertFalse(decide(config, explicit = true, installed = setOf("personal")))
            }
        }
    }

    @Test
    fun `unknown origin and spoofable referrers do not claim an official source`() {
        listOf(InstallSourceConfidence.UNKNOWN, InstallSourceConfidence.REFERRER_HEURISTIC).forEach { confidence ->
            val config = ConfigModel.default.copy(initiatorPackageName = "com.android.vending", installSourceConfidence = confidence)
            assertFalse(ResigningPolicy.isOfficialSource(config))
            assertTrue(decide(config, installed = setOf("personal")))
        }
        val browser = ConfigModel.default.copy(initiatorPackageName = "browser", installSourceConfidence = InstallSourceConfidence.EXACT_CALLER)
        assertTrue(decide(browser, installed = setOf("personal")))
    }

    private fun decide(config: ConfigModel = ConfigModel.default, explicit: Boolean = false, installed: Set<String>) = ResigningPolicy.shouldResign(config, explicit, "personal", installed)
}

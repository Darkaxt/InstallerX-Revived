// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.domain.engine.model.install

import com.rosan.installer.domain.session.model.InstallSourceConfidence
import com.rosan.installer.domain.settings.model.config.ConfigModel

/** Policy uses incoming source provenance, never the existing app's installer. */
object ResigningPolicy {
    private val officialStores = setOf(
        "com.android.vending",
        "com.sec.android.app.samsungapps",
        "com.amazon.venezia",
        "com.huawei.appmarket",
        "com.xiaomi.mipicks",
        "com.xiaomi.market",
    )

    fun isOfficialSource(config: ConfigModel): Boolean = config.initiatorPackageName in officialStores &&
        (
            config.installSourceConfidence.isTrustedForPlatformPolicy() ||
                config.installSourceConfidence == InstallSourceConfidence.PROVIDER_OWNER
            )

    fun canExplicitlyResign(
        config: ConfigModel,
        installed: Boolean,
        pendingSigners: Set<String>?,
        installedSigners: Set<String>?,
    ): Boolean = !isOfficialSource(config) &&
        (!installed || (!pendingSigners.isNullOrEmpty() && !installedSigners.isNullOrEmpty() && pendingSigners != installedSigners))

    fun shouldResign(
        config: ConfigModel,
        explicitlyResign: Boolean,
        personalSigner: String?,
        installedSigners: Set<String>,
    ): Boolean = !isOfficialSource(config) &&
        (explicitlyResign || (personalSigner != null && installedSigners == setOf(personalSigner)))
}

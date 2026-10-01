// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.domain.engine.repository

import com.rosan.installer.domain.engine.model.packageinfo.PackageAnalysisResult
import com.rosan.installer.domain.settings.model.config.ConfigModel
import java.io.Closeable

interface ApkResigningRepository {
    suspend fun prepare(
        results: List<PackageAnalysisResult>,
        explicitlyResign: Boolean,
        config: ConfigModel = ConfigModel.default,
        rememberedPackages: Set<String> = emptySet(),
    ): PreparedResigning
}

class PreparedResigning(
    val results: List<PackageAnalysisResult>,
    val resignedPackages: Set<String> = emptySet(),
    private val cleanup: () -> Unit = {},
) : Closeable {
    override fun close() = cleanup()
}

// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.domain.engine.model.install

import com.rosan.installer.domain.engine.model.packageinfo.AppEntity
import com.rosan.installer.domain.engine.model.packageinfo.PackageAnalysisResult
import com.rosan.installer.domain.engine.model.source.DataType

/** Only a standalone APK can safely use the original, unmodified source URI. */
object MorpheHandoffPolicy {
    fun sourceUri(results: List<PackageAnalysisResult>, sources: List<String>): String? {
        val source = sources.singleOrNull()?.takeIf { it.isNotBlank() } ?: return null
        if (source.substringBefore(':').lowercase() !in setOf("content", "file")) return null
        val selected = results.singleOrNull()?.appEntities?.filter { it.selected } ?: return null
        val base = selected.singleOrNull()?.app as? AppEntity.BaseEntity ?: return null
        return source.takeIf { base.sourceType == DataType.APK }
    }
}

// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.domain.engine.model.install

import com.rosan.installer.domain.engine.model.packageinfo.AppEntity
import com.rosan.installer.domain.engine.model.packageinfo.PackageAnalysisResult
import com.rosan.installer.domain.engine.model.packageinfo.PackageIdentityStatus
import com.rosan.installer.domain.engine.model.packageinfo.SignatureMatchStatus
import com.rosan.installer.domain.engine.model.source.DataEntity
import com.rosan.installer.domain.engine.model.source.DataType
import com.rosan.installer.domain.session.model.SelectInstallEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MorpheHandoffPolicyTest {
    private val uri = "content://downloads/document/17"
    private fun result(type: DataType = DataType.APK, selected: Boolean = true) = PackageAnalysisResult(
        packageName = "example.any.app",
        sessionMode = SessionMode.Single,
        appEntities = listOf(
            SelectInstallEntity(
                AppEntity.BaseEntity(
                    packageName = "example.any.app", sharedUserId = null,
                    data = DataEntity.FileEntity("/unused.apk"), versionCode = 1, versionName = "1",
                    label = null, icon = null, minSdk = null, targetSdk = null, size = 0, sourceType = type,
                ),
                selected,
            ),
        ),
        installedAppInfo = null,
        signatureCheckPerformed = false,
        signatureMatchStatus = SignatureMatchStatus.UNKNOWN_ERROR,
        identityStatus = PackageIdentityStatus.NOT_APPLICABLE,
    )

    @Test
    fun `any standalone package can be sent without signing or catalogue filtering`() {
        assertEquals(uri, MorpheHandoffPolicy.sourceUri(listOf(result()), listOf(uri)))
    }

    @Test
    fun `empty deselected batch and multiple source selections cannot be sent`() {
        assertNull(MorpheHandoffPolicy.sourceUri(emptyList(), listOf(uri)))
        assertNull(MorpheHandoffPolicy.sourceUri(listOf(result(selected = false)), listOf(uri)))
        assertNull(MorpheHandoffPolicy.sourceUri(listOf(result(), result()), listOf(uri)))
        assertNull(MorpheHandoffPolicy.sourceUri(listOf(result()), listOf(uri, uri)))
        assertNull(MorpheHandoffPolicy.sourceUri(listOf(result()), emptyList()))
        assertNull(MorpheHandoffPolicy.sourceUri(listOf(result()), listOf("https://example.com/app.apk")))
    }

    @Test
    fun `containers and modules do not accidentally forward the first input`() {
        for (type in DataType.entries.filter { it != DataType.APK }) {
            assertNull(MorpheHandoffPolicy.sourceUri(listOf(result(type)), listOf(uri)))
        }
        val split = AppEntity.SplitEntity("example.any.app", DataEntity.FileEntity("/unused.apk"), "config.en", null, null, arch = null, size = 0, sourceType = DataType.APK)
        assertNull(MorpheHandoffPolicy.sourceUri(listOf(result().copy(appEntities = listOf(SelectInstallEntity(split, true)))), listOf(uri)))
    }
}

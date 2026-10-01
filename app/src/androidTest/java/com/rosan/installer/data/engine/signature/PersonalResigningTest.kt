// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.data.engine.signature

import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry
import com.rosan.installer.data.engine.repository.ApkResigningRepositoryImpl
import com.rosan.installer.domain.engine.model.install.SessionMode
import com.rosan.installer.domain.engine.model.packageinfo.AppEntity
import com.rosan.installer.domain.engine.model.packageinfo.InstalledAppInfo
import com.rosan.installer.domain.engine.model.packageinfo.PackageAnalysisResult
import com.rosan.installer.domain.engine.model.packageinfo.PackageIdentityStatus
import com.rosan.installer.domain.engine.model.packageinfo.SignatureMatchStatus
import com.rosan.installer.domain.engine.model.source.DataEntity
import com.rosan.installer.domain.engine.model.source.DataType
import com.rosan.installer.domain.session.model.InstallSourceConfidence
import com.rosan.installer.domain.session.model.SelectInstallEntity
import com.rosan.installer.domain.settings.model.config.ConfigModel
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalResigningTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val formatter = CertificateFormatter()
    private val key = PersonalSigningKey()
    private val installedReader = InstalledPackageSignatureReader(context, formatter)
    private val analyzer = PendingApkSignatureAnalyzer(formatter, LightweightApkSignatureReader(formatter))
    private val repository = ApkResigningRepositoryImpl(context, key, ApkResigner(), analyzer, installedReader)

    @Test
    fun generatedKeySignsRealApkInstallsAndRetainsItsIdentity() = runBlocking {
        val directory = File(context.cacheDir, "personal-signing-test").apply { mkdirs() }
        val original = asset(directory, "base")
        val originalBytes = original.readBytes()
        try {
            val firstIdentity = formatter.format(key.getOrCreate().signingCertificate).sha256
            assertEquals(firstIdentity, formatter.format(PersonalSigningKey().getOrCreate().signingCertificate).sha256)
            var signedFile: File? = null
            repository.prepare(listOf(result(original)), true).use { prepared ->
                val app = prepared.results.single().appEntities.single().app as AppEntity.BaseEntity
                assertEquals(setOf(firstIdentity), app.signatureInfo!!.signerSha256Set)
                signedFile = File((app.data as DataEntity.FileEntity).path)
                install(signedFile!!)
                assertEquals(setOf(firstIdentity), installedReader.read(PACKAGE)!!.signerSha256Set)
            }
            assertFalse(signedFile!!.exists())
            assertArrayEquals(originalBytes, original.readBytes())
            shell("run-as $PACKAGE mkdir -p files")
            val marker = instrumentation.uiAutomation.executeShellCommandRwe("run-as $PACKAGE tee files/state")
            ParcelFileDescriptor.AutoCloseOutputStream(marker[1]).use { it.write("preserved".toByteArray()) }
            assertEquals("preserved", ParcelFileDescriptor.AutoCloseInputStream(marker[0]).use { it.readBytes().decodeToString() })
            assertEquals("", ParcelFileDescriptor.AutoCloseInputStream(marker[2]).use { it.readBytes().decodeToString() })
            assertEquals("preserved", shell("run-as $PACKAGE cat files/state").trim())
            val update = asset(directory, "update")
            val split = asset(directory, "split")
            val metadata = File(directory, "base.dm").apply { writeText("old metadata") }
            val updateResult = result(update).copy(
                installedAppInfo = InstalledAppInfo(
                    packageName = PACKAGE,
                    icon = null,
                    label = "Fixture",
                    versionCode = 1,
                    versionName = "1",
                    applicationInfo = null,
                    minSdk = 26,
                    targetSdk = 35,
                ),
                appEntities = result(update).appEntities + listOf(
                    SelectInstallEntity(
                        AppEntity.SplitEntity(
                            packageName = PACKAGE,
                            data = DataEntity.FileEntity(split.path),
                            splitName = "config.en",
                            minSdk = "26",
                            targetSdk = "35",
                            arch = null,
                            sourceType = DataType.APKS,
                        ),
                        true,
                    ),
                    SelectInstallEntity(
                        AppEntity.DexMetadataEntity(
                            packageName = PACKAGE,
                            data = DataEntity.FileEntity(metadata.path),
                            dmName = "base.dm",
                            minSdk = "26",
                            targetSdk = "35",
                            sourceType = DataType.APKS,
                        ),
                        true,
                    ),
                ),
            )
            val official = ConfigModel.default.copy(
                initiatorPackageName = "com.android.vending",
                installSourceConfidence = InstallSourceConfidence.EXACT_CALLER,
            )
            repository.prepare(listOf(updateResult), false, official).use { prepared ->
                assertSame(updateResult, prepared.results.single())
                assertTrue(prepared.resignedPackages.isEmpty())
            }
            val unrelatedBase = (updateResult.appEntities.first().app as AppEntity.BaseEntity).copy(packageName = "unrelated.app")
            val unrelated = updateResult.copy(
                packageName = "unrelated.app",
                appEntities = listOf(SelectInstallEntity(unrelatedBase, true)),
                installedAppInfo = null,
            )
            var remembered: Set<String> = emptySet()
            // Optional pre-install signature checks may be disabled; use actual installed signers.
            repository.prepare(listOf(updateResult.copy(signatureCheckPerformed = false), unrelated), false).use { prepared ->
                remembered = prepared.resignedPackages
                assertEquals(setOf(PACKAGE), remembered)
                assertSame(unrelated, prepared.results[1])
                assertEquals(SignatureMatchStatus.MATCH, prepared.results.first().signatureMatchStatus)
                val selected = prepared.results.first().appEntities.filter { it.selected }.map { it.app }
                assertEquals(2, selected.size)
                assertTrue(selected.all { analyzer.analyze((it.data as DataEntity.FileEntity).path).signerSha256Set == setOf(firstIdentity) })
                installSet(selected)
                assertEquals(2L, context.packageManager.getPackageInfo(PACKAGE, 0).longVersionCode)
                assertEquals("preserved", shell("run-as $PACKAGE cat files/state").trim())
            }
            val splitOnly = updateResult.copy(appEntities = updateResult.appEntities.drop(1))
            repository.prepare(listOf(splitOnly), false).use { prepared ->
                assertEquals(setOf(PACKAGE), prepared.resignedPackages)
                assertEquals(1, prepared.results.single().appEntities.count { it.selected })
                installSet(prepared.results.single().appEntities.filter { it.selected }.map { it.app }, inherit = true)
            }
            // The existing uninstall-and-retry path must retain the automatic signing choice.
            assertTrue(shell("pm uninstall $PACKAGE").contains("Success"))
            repository.prepare(listOf(updateResult), false, rememberedPackages = remembered).use { prepared ->
                assertEquals(SignatureMatchStatus.NOT_INSTALLED, prepared.results.single().signatureMatchStatus)
                installSet(prepared.results.single().appEntities.filter { it.selected }.map { it.app })
                assertEquals(setOf(firstIdentity), installedReader.read(PACKAGE)!!.signerSha256Set)
            }
        } finally {
            shell("pm uninstall $PACKAGE")
            directory.deleteRecursively()
        }
    }

    @Test
    fun failedSigningRemovesCopiesAndKeepsSource() = runBlocking {
        val input = File(context.cacheDir, "invalid-signing-test.apk").apply { writeText("invalid") }
        val previous = context.cacheDir.listFiles().orEmpty().filter { it.name.startsWith("resign-") }.toSet()
        try {
            var failed = false
            try {
                repository.prepare(listOf(result(input)), true).close()
            } catch (_: Exception) {
                failed = true
            }
            assertTrue(failed)
            assertEquals("invalid", input.readText())
            assertEquals(previous, context.cacheDir.listFiles().orEmpty().filter { it.name.startsWith("resign-") }.toSet())
        } finally {
            input.delete()
        }
    }

    private fun asset(directory: File, name: String): File = File(directory, "$name.apk").apply {
        instrumentation.context.assets.open("resigning/$name.apk").use { input -> outputStream().use { input.copyTo(it) } }
    }

    private fun result(file: File) = PackageAnalysisResult(
        packageName = PACKAGE,
        sessionMode = SessionMode.Single,
        appEntities = listOf(
            SelectInstallEntity(
                AppEntity.BaseEntity(
                    packageName = PACKAGE, sharedUserId = null, data = DataEntity.FileEntity(file.path),
                    versionCode = 1, versionName = "1", label = "Fixture", icon = null,
                    minSdk = "26", targetSdk = "35", sourceType = DataType.APK,
                    signatureInfo = analyzer.analyze(file.path),
                ),
                true,
            ),
        ),
        installedAppInfo = null,
        signatureCheckPerformed = true,
        signatureMatchStatus = SignatureMatchStatus.NOT_INSTALLED,
        identityStatus = PackageIdentityStatus.NOT_APPLICABLE,
    )

    private fun install(file: File) {
        writeApk("pm install -r -S ${file.length()}", file)
    }

    private fun installSet(apps: List<AppEntity>, inherit: Boolean = false) {
        val created = shell("pm install-create -r" + if (inherit) " -p $PACKAGE" else "")
        val session = Regex("\\[(\\d+)\\]").find(created)!!.groupValues[1]
        try {
            apps.forEach { app ->
                val file = File((app.data as DataEntity.FileEntity).path)
                writeApk("pm install-write -S ${file.length()} $session ${app.name} -", file)
            }
            assertEquals("Success", shell("pm install-commit $session").trim())
        } finally {
            shell("pm install-abandon $session")
        }
    }

    private fun writeApk(command: String, file: File) {
        val descriptors = instrumentation.uiAutomation.executeShellCommandRwe(command)
        ParcelFileDescriptor.AutoCloseOutputStream(descriptors[1]).use { output -> file.inputStream().use { it.copyTo(output) } }
        val output = ParcelFileDescriptor.AutoCloseInputStream(descriptors[0]).use { it.readBytes().decodeToString() }
        val errors = ParcelFileDescriptor.AutoCloseInputStream(descriptors[2]).use { it.readBytes().decodeToString() }
        assertTrue("$command: stdout=$output stderr=$errors", output.trim().startsWith("Success"))
    }

    private fun shell(command: String): String {
        val descriptors = instrumentation.uiAutomation.executeShellCommandRwe(command)
        descriptors[1].close()
        val output = ParcelFileDescriptor.AutoCloseInputStream(descriptors[0]).use { it.readBytes().decodeToString() }
        val errors = ParcelFileDescriptor.AutoCloseInputStream(descriptors[2]).use { it.readBytes().decodeToString() }
        return output + errors
    }

    private companion object {
        const val PACKAGE = "com.darkaxt.installerx.resigning.fixture"
    }
}

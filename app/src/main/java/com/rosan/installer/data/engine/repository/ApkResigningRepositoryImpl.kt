// SPDX-License-Identifier: GPL-3.0-only
package com.rosan.installer.data.engine.repository

import android.content.Context
import android.os.Build
import com.rosan.installer.data.engine.signature.ApkResigner
import com.rosan.installer.data.engine.signature.InstalledPackageSignatureReader
import com.rosan.installer.data.engine.signature.PendingApkSignatureAnalyzer
import com.rosan.installer.data.engine.signature.PersonalSigningKey
import com.rosan.installer.data.engine.signature.signingCertificate
import com.rosan.installer.data.engine.signature.signingKey
import com.rosan.installer.domain.engine.model.install.ResigningPolicy
import com.rosan.installer.domain.engine.model.packageinfo.AppEntity
import com.rosan.installer.domain.engine.model.packageinfo.PackageAnalysisResult
import com.rosan.installer.domain.engine.model.packageinfo.PackageIdentityStatus
import com.rosan.installer.domain.engine.model.packageinfo.analyzePackageSignatureMatch
import com.rosan.installer.domain.engine.model.packageinfo.analyzePackageSignatureSelection
import com.rosan.installer.domain.engine.model.source.DataEntity
import com.rosan.installer.domain.engine.repository.ApkResigningRepository
import com.rosan.installer.domain.engine.repository.PreparedResigning
import com.rosan.installer.domain.settings.model.config.ConfigModel
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.security.cert.X509Certificate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class ApkResigningRepositoryImpl(
    private val context: Context,
    private val personalKey: PersonalSigningKey,
    private val signer: ApkResigner,
    private val analyzer: PendingApkSignatureAnalyzer,
    private val installedReader: InstalledPackageSignatureReader,
) : ApkResigningRepository {
    override suspend fun explicitEligiblePackages(results: List<PackageAnalysisResult>, config: ConfigModel): Set<String> = withContext(Dispatchers.IO) {
        if (ResigningPolicy.isOfficialSource(config)) return@withContext emptySet()
        results.mapNotNull { result ->
            currentCoroutineContext().ensureActive()
            val base = result.appEntities.firstOrNull { it.selected && it.app is AppEntity.BaseEntity }?.app as? AppEntity.BaseEntity
                ?: return@mapNotNull null
            if (result.installedAppInfo == null) return@mapNotNull result.packageName
            val pending = try {
                base.signatureInfo?.takeIf { it.verified }
                    ?: analyzer.analyzeForResignEligibility(base.data, context.cacheDir.path)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                return@mapNotNull null
            }
            val installed = installedReader.read(result.packageName)
            result.packageName.takeIf {
                ResigningPolicy.canExplicitlyResign(config, true, pending.takeIf { it.verified }?.signerSha256Set, installed?.signerSha256Set)
            }
        }.toSet()
    }

    override suspend fun prepare(results: List<PackageAnalysisResult>, explicitlyResign: Boolean, config: ConfigModel, rememberedPackages: Set<String>): PreparedResigning {
        var prepared: PreparedResigning? = null
        try {
            return withContext(Dispatchers.IO) {
                prepareOnIo(results, explicitlyResign, config, rememberedPackages).also { prepared = it }
            }
        } catch (error: Throwable) {
            // withContext can cancel while returning a successfully prepared resource.
            prepared?.close()
            throw error
        }
    }

    private suspend fun prepareOnIo(results: List<PackageAnalysisResult>, explicitlyResign: Boolean, config: ConfigModel, rememberedPackages: Set<String>): PreparedResigning {
        if (ResigningPolicy.isOfficialSource(config)) return PreparedResigning(results)
        val existingKey = personalKey.existing()
        if (!explicitlyResign && rememberedPackages.isEmpty() && existingKey == null) return PreparedResigning(results)
        val personalSigner = existingKey?.signingCertificate?.sha256()
        val apkResults = results.filter { result ->
            result.appEntities.any { it.selected && (it.app is AppEntity.BaseEntity || it.app is AppEntity.SplitEntity) }
        }
        val installedSignatures = apkResults.associate { it.packageName to installedReader.read(it.packageName) }
        val packagesToSign = apkResults.filter { result ->
            ResigningPolicy.shouldResign(
                config,
                explicitlyResign || result.packageName in rememberedPackages,
                personalSigner,
                installedSignatures[result.packageName]?.signerSha256Set.orEmpty(),
            )
        }.mapTo(mutableSetOf()) { it.packageName }
        if (packagesToSign.isEmpty()) return PreparedResigning(results)
        val entry = existingKey ?: personalKey.getOrCreate()
        val certificate = entry.signingCertificate
        val directory = Files.createTempDirectory(context.cacheDir.toPath(), "resign-").toFile()
        return try {
            val updated = results.map { result ->
                if (result.packageName !in packagesToSign) return@map result
                val selected = result.appEntities.filter { it.selected }.map { it.app }
                val installedSignature = installedSignatures[result.packageName]
                val installedInfo = result.installedAppInfo?.takeIf { installedSignature != null }
                    ?.copy(signatureInfo = installedSignature, signatureHash = installedSignature?.primarySha256)
                if (selected.none { it is AppEntity.BaseEntity }) {
                    check(
                        installedInfo?.signatureInfo?.signerSha256Set == setOf(
                            certificate.sha256(),
                        ),
                    ) { "A split-only update requires a base already signed with the personal key" }
                }
                val entities = result.appEntities.map { selectable ->
                    currentCoroutineContext().ensureActive()
                    val app = selectable.app
                    when {
                        !selectable.selected -> selectable

                        app is AppEntity.DexMetadataEntity -> selectable.copy(selected = false)

                        app is AppEntity.BaseEntity || app is AppEntity.SplitEntity -> {
                            val input = File.createTempFile("input-", ".apk", directory)
                            val output = File.createTempFile("signed-", ".apk", directory)
                            try {
                                requireNotNull(app.data.getInstallInputStreamWhileNotEmpty()) { "Cannot open APK for signing" }.use { source ->
                                    input.outputStream().use { target ->
                                        val buffer = ByteArray(1024 * 1024)
                                        while (true) {
                                            currentCoroutineContext().ensureActive()
                                            val count = source.read(buffer)
                                            if (count < 0) break
                                            target.write(buffer, 0, count)
                                        }
                                    }
                                }
                                signer.sign(input, output, entry.signingKey, certificate, Build.VERSION.SDK_INT)
                            } finally {
                                input.delete()
                            }
                            val info = analyzer.analyze(output.path)
                            check(info.verified) { "Cannot analyze the re-signed APK" }
                            val data = DataEntity.FileEntity(output.path).apply { source = app.data }
                            val signedApp = when (app) {
                                is AppEntity.BaseEntity -> app.copy(
                                    data = data,
                                    size = output.length(),
                                    signatureInfo = info,
                                    signatureHash = info.primarySha256,
                                    fileHash = null,
                                )

                                is AppEntity.SplitEntity -> app.copy(data = data, size = output.length(), signatureInfo = info)
                            }
                            selectable.copy(app = signedApp)
                        }

                        else -> selectable
                    }
                }
                result.copy(
                    appEntities = entities,
                    installedAppInfo = installedInfo,
                    identityStatus = if (installedInfo == null) PackageIdentityStatus.NOT_APPLICABLE else PackageIdentityStatus.DIFFERENT,
                    signatureCheckPerformed = true,
                    signatureAnalysis = entities.analyzePackageSignatureSelection(installedInfo),
                    signatureMatchStatus = entities.analyzePackageSignatureMatch(
                        installedInfo,
                        installedReader::hasSigningCertificate,
                    ),
                )
            }
            PreparedResigning(updated, packagesToSign) { directory.deleteRecursively() }
        } catch (error: Throwable) {
            directory.deleteRecursively()
            throw error
        }
    }

    private fun X509Certificate.sha256(): String = MessageDigest.getInstance("SHA-256")
        .digest(encoded).joinToString("") { "%02x".format(it) }
}

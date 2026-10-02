# Execution and reconciliation ledger

Only one stage may be ACTIVE.

| Stage | Status | Requirements | Acceptance / evidence |
|---|---|---|---|
| 1. Explicit sign and install | COMPLETE | R1-R3,R6 | AC1 verified: host ApkResignerTest, Preview debug build, Android 15 PersonalResigningTest |
| 2. Automatic updates and source policy | COMPLETE | R2 source eligibility,R4-R6 | AC2 passed: policy/profile/session regressions, both UI builds, live Android automatic base/split updates and retry |
| 3. Verified fork delivery and weekly maintenance | COMPLETE | R7-R9 | AC3 passed: remote fork/source, final checks, independently verified published Stable APK, inspected ACTIVE weekly heartbeat |

## Reconciliation

- Stage 1: AC1 passed. `testUnstableDebugUnitTest --tests '*ApkResignerTest'`
  and `assemblePreviewDebug` passed. Android 15 (emulator-5580)
  `PersonalResigningTest` passed: Android Keystore identity reused, different-signer
  base+split update installed with retained private-data marker; old dex metadata
  deselected; input unchanged; signing copies cleaned after success and failure.
  Test helper defects (stdin option and shell quoting) were isolated and fixed;
  production signing behavior did not need a workaround.
- Stage 2: AC2 passed. Focused ResigningPolicy, ApkResigner, AnalyzeInstallState,
  SigningBlockProfilePolicy and InstallerSessionRepository tests passed. Preview
  and Unstable debug builds passed. Android 15 test passed automatic signing with
  optional checks disabled, official-store bypass, unrelated batch-item retention,
  real split-only inherit-session update, private-data retention and remembered
  signing after uninstall. Existing conflict UI/confirmation remains unchanged.
- Stage 3: remote fork created at https://github.com/Darkaxt/InstallerX-Revived;
  local `upstream` is wxxsfxyzm and `origin` is Darkaxt. Final `spotlessCheck`,
  full `testUnstableDebugUnitTest`, Preview/Unstable debug, Android test and
  optimized Stable release builds passed with the fork ID and persistent release
  key. Final Android 15 instrumentation passed. Real Miuix UI Resign -> native
  confirmation -> install succeeded; ordinary Install Anyway -> automatic
  re-sign -> native update succeeded, retaining the private-data marker and
  personal certificate (independently checked on pulled installed APKs).
  The weekly heartbeat `sync-and-release-installerx-resign` is ACTIVE with the
  saved Monday 09:00 recurrence and maintenance scope inspected.
  Optimized Stable installer update preserved the personal Keystore identity;
  another real ordinary-action update passed and retained private data.
  Normal release `26.10.1001` (version code 1554) is published from verified
  commit `deea0e9d7d3998e0ac56f93415e0030ad4dca876`; origin/main and the tag were
  checked against that commit. The published APK was independently downloaded,
  verified and compared byte-for-byte by SHA-256. Its package, version, release
  certificate, 16 KB alignment and updater feed were checked. Numeric weekly/
  monthly update-version ordering regressions also passed.
  Transactional cleanup completed: all 22,787 reviewed generated members removed,
  820,530,664 bytes reclaimed, no residuals/errors. Release artifact, evidence,
  retrace mapping, source fixtures, keys and reusable tooling remain preserved.
- Blockers: none.
- Tracked deferrals: none.

## Final specification reconciliation

| Requirements | Verified result |
|---|---|
| R1 | Generated Android Keystore key reused across instances and the installer release update; no production personal key/import dependency |
| R2 | Both UI implementations compile; real Miuix Resign action installed an APK through the existing native confirmation flow |
| R3 | Different-signer single/base/split and inherit-session update tests passed, originals retained, dex metadata omitted, copies cleaned on success/failure |
| R4 | Actual current signer triggers automatic updates with optional checks disabled; mixed batch and remembered uninstall retry passed; both session installation paths pass the signing state |
| R5 | Trusted store exclusion and unknown/referrer policy tests passed; source is the incoming request provenance |
| R6 | Produced signatures independently verified and effective profile analysis tested; existing conflict/uninstall UI preserved |
| R7 | Public fork/history/remotes, separate app ID/updater, protected persistent release key and GitHub secrets verified |
| R8 | Full host suite/style/both UI/optimized release passed; real Android 15 execution, published source/signature/package/version/checksum/alignment verified |
| R9 | Saved ACTIVE Monday 09:00 Europe/Berlin heartbeat and scope inspected; Windows host timezone matches Berlin; maintenance instructions and signing path established |

No physical-device or hosted manual-workflow execution is claimed. Android
verification used the isolated read-only emulator, which was shut down. The
initial publication used the verified local build. All required criteria are
satisfied; there are no unresolved blockers or deferrals.

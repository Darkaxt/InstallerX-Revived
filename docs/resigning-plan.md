# Execution and reconciliation ledger

Only one stage may be ACTIVE.

| Stage | Status | Requirements | Acceptance / evidence |
|---|---|---|---|
| 1. Explicit sign and install | COMPLETE | R1-R3,R6 | AC1 verified: host ApkResignerTest, Preview debug build, Android 15 PersonalResigningTest |
| 2. Automatic updates and source policy | COMPLETE | R2 source eligibility,R4-R6 | AC2 passed: policy/profile/session regressions, both UI builds, live Android automatic base/split updates and retry |
| 3. Verified fork delivery and weekly maintenance | ACTIVE | R7-R9 | AC3; fork identity/update route, signing/release, final checks, commit/push, scheduler |

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
  Release publication, remote commit verification and cleanup remain.
- Blockers: none identified yet.
- Tracked deferrals: none.
- Verification output/build artifacts belong to this task; preserve deliverable
  APKs and evidence; clean expendable task-owned intermediates at closure.

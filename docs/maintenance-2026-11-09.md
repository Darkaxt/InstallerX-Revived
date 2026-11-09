# Weekly synchronization — 2026-11-09

Authorization: already_authorized by the weekly heartbeat and standing
engineering instructions. The authoritative contracts remain
`fork-maintenance.md`, `resigning-spec.md`, `signing-backup-spec.md`,
`morphe-signing-spec.md`, `morphe-handoff-spec.md` and `branding-spec.md`.
Their existing feature ledgers remain applicable. This plan changes no product
requirement or identity.

Baseline: clean origin/main `9fc11d4b`, normal release 26.10.1008 (1590).
Upstream main `2bdd19dc` has fourteen missing commits: optional Shizuku non-ADB
installation, streamed APK/container analysis and name/icon preferences,
signature-setting consolidation, AMOLED/dark icon/theme fixes, install-button
click correction, translations and dependency updates. Merge main only; retain
all fork behavior and existing release/personal keys.

| Stage | Status | Requirements and acceptance |
|---|---|---|
| 1. Complete source integration | COMPLETE | Merge upstream without rebasing, resolve all fork/API/resource/test integration, preserve R1-R7/B1-B5/M1-M5/H1-H4/N1-N4. Review the complete source and prepare meaningful affected runtime verification. All required source work is ready before compilation. |
| 2. Integrated verification | ACTIVE | Fresh style, full host suite, Preview/both UI and Android-test compilation, optimized Stable packaging; real isolated emulator signing/backup/update and relevant changed installation/source workflows. Verify unchanged identity through an installer update, both native actions, package/version/code/certificate/alignment/SHA and numeric updater ordering. Failures return affected source and verification to this outcome before delivery. |
| 3. Verified public delivery | NOT STARTED | After all prerequisite checks pass, push completed source, tag the exact verified commit, publish normal 26.11.1001 with the required online APK/checksum, independently retrieve and verify delivery and the compiled updater against the actual feed. Clean reviewed expendables, preserve deliverables/evidence/keys and verify remote state. |

Requirements R8-R9/B6/H5-H6/N5 and integration acceptance are assigned to Stages
2-3. The November numeric version must compare greater than 26.10.1008 and its
actual Git-derived code must exceed 1590. A source checkpoint commit may be used
for reproducible exact-source builds; it does not establish validation or delivery.

Stage 1 source inspection passed: all merge conflicts resolved; all 28 resource
sets parse without duplicate strings or unsupported branding/links; Bouncy Castle
1.86 remains alongside upstream AGP/dependency updates. The key store, identity
codec, signing repository, handoff implementation and updater are unchanged.
Both patch-action rows remain and the disabled-long-press correction is applied.
Obsolete split-signature preferences are ignored without changing the independent
Resign signer comparison. New Android checks cover real HTTP Range split-source
analysis/signing with optional checks disabled and app-owned Shizuku installation.
Evidence: `source-readiness.json` and `merge-resolutions.json` in the evidence root.
Stage-closure design assessment found no material duplicated ownership or new
infrastructure; the merge reuses existing source, signing and install boundaries.

Current advancement blocker: Stage 2 compiled/runtime/artifact criteria remain
unverified (internal; resolve by the recorded gated checks and isolated workflow).
Tracked deferrals: none. Only Stage 2 is ACTIVE.

Storage: exact scratch `D:/Temp/installerx-weekly-2026-11-09` uses the D: exception
for sustained/small-file Gradle/native emulator I/O and a disposable AVD. Legacy
checkout/SDK bindings on C: stay in place; their exact generated build roots are
also registered. Cleanup transaction `3a237cdc49c5310478e99abf8865ad6d` began
before generation and covers seven exact roots. Current deliverable and required
verification evidence: `D:/Artifacts/InstallerX-Revived/26.11.1001`.
All emulator launch/control must use the installed Coordinator emulator gate;
all Gradle commands use the shared build gate. No physical device access.

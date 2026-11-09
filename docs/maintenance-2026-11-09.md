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
| 2. Integrated verification | COMPLETE | Fresh style, full host suite, Preview/both UI and Android-test compilation, optimized Stable packaging; real isolated emulator signing/backup/update and relevant changed installation/source workflows. Verify unchanged identity through an installer update, both native actions, package/version/code/certificate/alignment/SHA and numeric updater ordering. Failures return affected source and verification to this outcome before delivery. |
| 3. Verified public delivery | COMPLETE | After all prerequisite checks pass, push completed source, tag the exact verified commit, publish normal 26.11.1001 with the required online APK/checksum, independently retrieve and verify delivery and the compiled updater against the actual feed. Clean reviewed expendables, preserve deliverables/evidence/keys and verify remote state. |

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

Stage 2 evidence so far: the full gated style/host/Preview/Unstable/Android-test/
optimized Stable build passed with verified native daemon exit. Real Android
backup/import/update/legacy checks passed. A personal identity created natively
in published 26.10.1008 survived the testable upgrade byte-for-byte. The real
Shizuku server and its native authorization UI enabled an app-owned installation
with the expected personal signer. Existing personal signing/update and signer
visibility checks passed.

The new streaming check exposed an incorrect test-fixture assumption: bundled
base.apk uses signer fb5dbd3c, while update.apk and split.apk use 681b0e56. The
observed conflict correctly offered Resign. Its matching-signer setup now installs
update.apk first; the later personally signed base-to-streamed-update path stays
unchanged. Marker writing reuses the existing byte-pipe fixture technique.
`fixture-signers.json` and the original runtime receipt retain the failure evidence.
The test correction required fresh affected compilation/runtime checks and final
versioned packaging, which passed. The unchanged application host/UI compilation
remained valid.

Stage 2 closure: corrected streaming verification passed with real Range requests,
matching/conflicting signers, automatic personal signing, split installation and
retained app data. The real Shizuku app-owned installation also passed. Both native
signing settings interfaces verified replacement cancellation and preserved identity.
The optimized Stable APK showed the adjacent accessible actions in both styles,
hid Resign for matching certificates, handed off to the installed Morphe receiver
and retained each preparation on return. Normal install taps reached Android's
confirmation in both styles. Its native Resign action actually installed a fixture;
the pulled APK independently verified against the pre-existing personal certificate.
Original input bytes and encrypted identity remained unchanged throughout.

All builds used the installed gate with the required effective profile and verified
daemon exit. Host/Preview results remain applicable: the subsequent changes touched
only the Android fixture correction and this ledger. Fresh affected compilation,
style and optimized packaging passed. Both owned emulator workflows exited with
native code 0, verified whole-tree exit and released leases. The HTTP fixture exited.
No physical device was accessed. Evidence: `optimized-runtime.json`,
`optimized-resigning.json`, `runtime-receipts.jsonl`, build/gate logs and SDK checks.

Frozen application source: `b60e8a1047ced291223b284836663b380696b2e3`.
Verified Stable: 26.11.1001 (1608), expected existing release certificate, alignment
and SHA-256 `3238ced7cc059a74d4d0047b22c92708292bbc45bdf59e56ebb07e93e77ab30d`.
The compiled updater selects its online APK and orders it above 26.10.1008.
Only this task-owned documentation ledger may change during delivery; tag the
frozen verified application commit and commit final delivery evidence afterward.

Stage 3 closure: normal release [26.11.1001](https://github.com/Darkaxt/InstallerX-Revived/releases/tag/26.11.1001)
is published with `InstallerX-Resigned-online-26.11.1001.apk` and `SHA256SUMS.txt`.
The remote annotated tag resolves to the frozen verified application commit.
An anonymous download matched the local SHA-256, expected certificate, package,
version and alignment. The compiled Stable updater selected that actual latest
release asset. The maintained R8 mapping and verification evidence are retained.

All 23,048 reviewed generated members were deleted through the installed cleanup
helper: 2,858,972,575 file bytes, no exclusions, warnings or errors. All seven
registered generated roots are absent; sources, signing keys and retained release
assets remain. `final-verification.json` records delivery and cleanup reconciliation.
The automation remains ACTIVE on the same chat; Monday 10:00 on the verified
Bucharest host converts to Monday 09:00 Berlin in both winter and summer.

Current advancement blockers: none. Tracked deferrals: none. All three stages are
COMPLETE; no stage is ACTIVE. This final ledger-only commit does not change the
verified tagged application or require rebuilding it.

Storage: exact scratch `D:/Temp/installerx-weekly-2026-11-09` uses the D: exception
for sustained/small-file Gradle/native emulator I/O and a disposable AVD. Legacy
checkout/SDK bindings on C: stay in place; their exact generated build roots are
also registered. Cleanup transaction `3a237cdc49c5310478e99abf8865ad6d` began
before generation and covers seven exact roots. Current deliverable and required
verification evidence: `D:/Artifacts/InstallerX-Revived/26.11.1001`.
All emulator launch/control must use the installed Coordinator emulator gate;
all Gradle commands use the shared build gate. No physical device access.

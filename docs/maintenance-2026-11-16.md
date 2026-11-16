# Weekly synchronization — 2026-11-16

Authorization: already_authorized by the weekly heartbeat and standing engineering
instructions. Authoritative behavior remains in `fork-maintenance.md`,
`resigning-spec.md`, `signing-backup-spec.md`, `morphe-signing-spec.md`,
`morphe-handoff-spec.md` and `branding-spec.md`; this plan does not weaken them.

Baseline: clean main/origin `5ec9bc04`, normal release 26.11.1001 (1608).
Upstream main `caccb787` contains eight missing commits: Miuix OS4 glass settings,
the Material/Miuix switching crash correction, target-user-aware uninstall,
translations and dependency/wrapper updates. Integrate main only, retaining all
fork signing, import/export, source policy, branding, updater and handoff behavior.
Target the next numeric release 26.11.1002 with a code greater than 1608.

| Stage | Status | Requirements and acceptance |
|---|---|---|
| source-integration | COMPLETE | Merge upstream preserving R1-R7/B1-B5/M1-M5/H1-H4/N1-N4 and existing release/personal keys. Resolve resources and both UI families, prepare meaningful target-user and settings-switch/runtime checks, and reconcile all application source before compilation. Correct the existing R9 schedule for the verified Madrid host without changing its Berlin time, scope, target chat or quiet notification intent. |
| integrated-verification | COMPLETE | Fresh style/full host checks, Preview and both UI/Android-test compilation, optimized Stable packaging. Verify actual isolated signing/backup/update, target-user uninstall, Material/Miuix classic/glass switching and native fork actions. Preserve an identity across the published-baseline-to-new-installer update; verify package/version/code/certificate/alignment/SHA and updater ordering. Source or runtime failures remain acceptance blockers until corrected and reverified. |
| public-delivery | COMPLETE | After prerequisite checks pass, push the completed source, tag the exact verified commit and publish a normal signed 26.11.1002 with the online APK and checksum. Independently download and verify the public package and actual updater selection, verify schedule conversion/readback, transactionally clean spent outputs, and commit final reconciliation. |

R8-R9/B6/M4/H5-H6/N5 integration and delivery checks belong to the last two stages.
The native compilation readiness receipt will use these stage IDs and the complete
source/plan pins only after the source-integration criteria pass. No application
compilation or executable acceptance has run for this update.

Source closure: all nine conflicts were resolved. Resource parsing/duplicate and
branding inspection passed across 28 locales. The reorganized Miuix pages preserve
the signing route, fork documentation and unsupported-helper/proxy removals.
The fork signing page uses upstream's shared backdrop and classic/OS4 app bar;
its identity/password/confirmation logic is unchanged. Native signing-settings
verification now covers Material, Miuix OS3 and OS4, restoring previous preferences.
Existing signing/storage/codec, updater, Morphe transport and both preparation
implementations are byte-identical to the baseline. The settings-switching fix
is byte-identical to upstream; requested-user uninstall and signing retry wiring
were inspected. The owned native probe has an explicit target-user uninstall
request for proving that user 0's installation survives another user's removal.

`source-readiness.json` and `merge-resolutions.json` record source inspection;
there are no known implementation gaps. This is source ready for validation,
not compiled/runtime correctness. The source checkpoint will be committed for
exact Git-derived APK versioning; its revision and actual source hashes are
recorded in the native compilation readiness file. Its four finite checks are
`style`, `host-preview`, `android-runtime-apks` and `stable-release`, all bound to
source-integration on the original receipt. No application compilation has run.

Visual/interaction contract: retain native Material and Miuix controls, adjacent
labelled Morphe/bandage actions with at least 48 dp touch targets, readable signing
fingerprints and scrollable backup/activation controls. Check 360 dp phone and
800 dp expanded settings layouts, with actual screenshots; OS3/OS4/Material
switches must preserve navigation and identity without a crash or clipped actions.
The integration reuses existing component/state ownership; closure inspection
found no material design debt requiring a new abstraction or refactoring stage.

Verification checkpoint: source is frozen at `2524e47d` (code 1619). The complete
style, host/Preview, Unstable/Android-test and optimized Stable checks passed on
native receipt `e40c7a729802c5c64268120733c146ef`, attempts 13–16. Actual receipts
verify the shared mutex, two workers, no parallel projects, 3 GB Gradle heap, one
512 MB fail-fast test fork, daemon exit and native-tree exit. A live JVM census
also verified the Kotlin daemon's 3 GB heap. The source correction was one blank
line reported by Spotless. Initial launcher failures were resolved through the
same receipt: select the existing non-isolated Scoop Python, and scope the
private credential initialization to the root build rather than included builds.
The initialization reads existing signing/GitHub owners without secret copies.

Independent SDK checks passed for the optimized 26.11.1002 APK (1619), correct
package/branding, existing release certificate, non-debuggable configuration,
16 KB alignment and SHA-256
`a2cb8a37a83e389714d07ea96e5e0648069c25702501f921e6a9d4bdeed49f1e`.
The actual compiled Stable updater selects its candidate asset and orders 1002
above 1001. The correct `StableRelease` mapping and host evidence are retained.
Spent native-build log roots were archived and transactionally removed in
`849f5bfb7717167694bedbcc1a72e03f` and `32444ea3a94e4141ca6c065180278627`.

Current advancement blocker: isolated signing/update/uninstall and native UI
acceptance remains unverified (internal; resolve through the owned emulator
workflow). The original emulator ticket is
`98b0be08d371a92d94b64d9b1985d78e`, AVD `InstallerXWeekly20261116`, serial
`emulator-5588`. Tracked deferrals: none. Only integrated-verification is ACTIVE.

Runtime checkpoint: the verified published 26.11.1001 was installed on the new
owned AVD. Native generation, displayed candidate review and explicit activation
created fingerprint
`0265dd51e81acfc30d0b1757324e622b94acf2e06e356cd5032c66fe77a6b02a`.
After the signed 1619 Unstable update, encrypted identity file SHA-256 remains
`04b86e2ca212e43a33306b98ef4b7ab4034b68f3a7ddf5667fb6d8878735070f`.
No private identity was exported. The official Shizuku APK/starter is verified;
its real Manager UI granted this InstallerX instance access, independently read
back as API_V23 granted. Its home counter remained stale at zero; no successful
counter-refresh claim is made. The actual app-owned Shizuku installation test is
still required. Runtime suite client `54256-000000000000001c` is retained on the
original emulator session; no completion is inferred. The HTTP Range fixture's
owned endpoint is guest `10.0.2.2:61132`, with explicit `/stop` shutdown required
after its consumer completes. Remaining criteria are unchanged.

The retained runtime suite completed successfully with complete, gap-free output
(`signing-runtime-suite.json`). Real PKCS#12/BKS/legacy storage, unchanged signing
identity, signed installs/updates, streamed APK-set handling, optional-check-off
signer eligibility and actual non-ADB app-owned Shizuku installation passed.
Native certificate controls and replacement cancellation passed for Material,
Miuix OS3 and OS4; the missing-receiver contract passed. Identity ciphertext SHA
is still unchanged. The HTTP fixture was explicitly stopped and its original
process exited with code 0; its spent ZIP was transactionally removed under
`3dc4c18d81c114b787dfd8b2050d3beb`. The earlier Shizuku home-counter observation
does not affect the independently passed real Shizuku installation workflow.
Optimized native target-user uninstall, actual host theme switching, screenshots,
real Morphe consumption/return and final Stable identity checks remain required.

Optimized target-user acceptance passed: the real exported UninstallerActivity
accepted an explicit user-10 request, showed native confirmation/success and
removed only user 10. User 0's installation and its app-owned marker hash stayed
unchanged (`optimized-target-user-uninstall.json`). The first probe incorrectly
targeted InstallerActivity and was corrected to the manifest's uninstaller; this
was a verification-input correction, not an application defect or source change.

The exact optimized APK was read back from the emulator with the same SHA-256 as
the retained release. Its personal fingerprint decrypted to the original value.
Real SettingsActivity navigation completed Material -> Miuix OS3 -> OS4 ->
Material, preserving the signing route/identity without the upstream switching
crash. Six actual screenshots at 360 dp and 800 dp were inspected: readable
fingerprints, reachable controls and native classic/glass styling. Probe inputs
were corrected for Miuix's pointer-driven engine/version menus; no app behavior
was changed to accommodate the verification tool. Runtime suite signing/backup
and native target-user criteria are satisfied; actual Morphe consumption/return,
preparation actions, final identity/source checks and SDK shutdown remain required.

Storage: exact scratch `D:/Temp/installerx-weekly-2026-11-16` uses the D: exception
for sustained/small-file Gradle/native emulator I/O and a disposable AVD. Existing
checkout and SDK bindings remain. Cleanup transaction
`409904b9df1f14b3d7254e7585955584` registered this root and six exact generated
build roots before generation. Retain current release and required verification
evidence at `D:/Artifacts/InstallerX-Revived/26.11.1002`.

Integrated verification is COMPLETE (`optimized-runtime.json`). Both optimized
preparation styles handed the unchanged original private-source APK to the real
Morphe 2.5.0.8 receiver and its universal-patch selection screen, then returned to
the same preparation. Actual URI ownership is read-only, non-persistable, and
package-scoped; no install occurred during handoff. Morphe's explicit native
Expert-mode confirmation was enabled only in this disposable guest. Its initial
source synchronization completed before the original pending share was consumed;
the request was observed without replay. No patch or Morphe installation was run.
Both present/absent preparation screenshots were inspected. Matching signers hide
Resign; first installs show its accessible 48 dp bandage beside Morphe's icon.

The optimized Resign action then installed the fixture with the original personal
fingerprint, independently verified on the actual installed APK using SDK
apksigner. Source APK and identity ciphertext hashes remain unchanged, and the
native settings page still decrypts/displays the original fingerprint. Probe-only
assertions were corrected for a dynamic Morphe home caption, the exact lowercase
"Install success" text, and SDK 37's V3.0 signer-output prefix; no application
behavior changed, and no successful action was replayed. All retained clients
settled. The original emulator workflow ended with native/workflow exit 0, full
tree exit and lease release; no physical device was accessed. Current blockers:
none for integrated verification. Tracked deferrals: none. Only public-delivery
is ACTIVE; publication, independent public/updater checks and final cleanup remain.

The current Windows zone is Romance Standard Time (Europe/Madrid), matching Berlin
in winter and summer. The authorized automation update changes its saved local
hour from 10:00 to 09:00 and replaces the stale Bucharest conversion sentence.
Final readback/conversion verification remains required. Use the installed native
Coordinator launcher for Gradle and Android ownership, one emulator at a time,
without snapshots or physical-device access.

Delivery checkpoint: the exact verified source remains `2524e47d` (1619), with
only this reviewed ledger dirty. The reviewed `publish.py` at
`D:/Temp/installerx-weekly-2026-11-16` asserts runtime/signing prerequisites, pushes
main, tags that exact commit, verifies a downloaded draft and publishes the normal
26.11.1002 APK/checksum release. Its invocation preflight reports no local block;
that is diagnostic evidence, not approval. Existing release notes, APK, checksum,
mapping and verification are retained under `D:/Artifacts/InstallerX-Revived/26.11.1002`.
Spent AVD/emulator outputs were archived then transactionally removed in
`a066c4d8ccda861fca9fd8aaad0d5d89` after original native exit and lease release.
The next action is publication followed by anonymous SDK/updater verification;
no public delivery is claimed yet. Schedule readback passed for Madrid/Berlin
Monday 09:00 in winter and summer. Final residual scratch/build cleanup remains.

## Final reconciliation

All three stages are COMPLETE. The checkpoint statements above record the
original observations and corrections, rather than outstanding work.

Normal release [26.11.1002](https://github.com/Darkaxt/InstallerX-Revived/releases/tag/26.11.1002)
is published at the exact verified source `2524e47d6a12cd9f7af6446f5bb2762d83f4e3ad`,
code 1619. Its two required public assets are the online APK and SHA256SUMS.txt.
An independent anonymous download matched the retained APK SHA-256 and checksum;
SDK checks independently verified package, branding, version/code, the existing
release certificate, non-debuggable configuration and 16 KB alignment. The actual
compiled Stable updater selected the published asset and ordered 1002 above 1001.
The source tag remains exact; this final documentation commit changes only this
ledger. No installer or personal signing key was replaced.

R1-R9, signing-backup B1-B6, Morphe-signing M1-M5, handoff H1-H6 and branding
N1-N5 remain satisfied by source reconciliation, fresh host/build checks and the
recorded real Android workflows. Source integration reused existing ownership
and shared UI components; no material design debt or required consolidation
remains. All required integration and delivery checks passed. Remaining blockers:
none. Tracked deferrals: none.

The original emulator exited completely with code 0 and released its lease.
Reviewed cleanup `a066c4d8ccda861fca9fd8aaad0d5d89` removed spent AVD/capture
outputs after archival. Original transaction `409904b9df1f14b3d7254e7585955584`
removed the remaining scratch and six exact generated build roots; all seven
roots are absent, without exclusions or errors. Earlier spent gate/range
transactions are applied. Requested release APK, checksum, mapping, reproducible
scripts, inspected screenshots and verification evidence remain under
`D:/Artifacts/InstallerX-Revived/26.11.1002`; signing material is preserved.
The ACTIVE automation's saved Monday 09:00 Madrid time was read back and verified
to equal Monday 09:00 Berlin in winter and summer, retaining its original chat,
maintenance contract and quiet notification intent.

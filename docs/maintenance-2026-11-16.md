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
| integrated-verification | ACTIVE | Fresh style/full host checks, Preview and both UI/Android-test compilation, optimized Stable packaging. Verify actual isolated signing/backup/update, target-user uninstall, Material/Miuix classic/glass switching and native fork actions. Preserve an identity across the published-baseline-to-new-installer update; verify package/version/code/certificate/alignment/SHA and updater ordering. Source or runtime failures remain acceptance blockers until corrected and reverified. |
| public-delivery | NOT STARTED | After prerequisite checks pass, push the completed source, tag the exact verified commit and publish a normal signed 26.11.1002 with the online APK and checksum. Independently download and verify the public package and actual updater selection, verify schedule conversion/readback, transactionally clean spent outputs, and commit final reconciliation. |

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

Current advancement blocker: integrated compiled/runtime/artifact acceptance is
unverified (internal; resolve through the declared gated checks and isolated
workflow). Tracked deferrals: none. Only integrated-verification is ACTIVE.

Storage: exact scratch `D:/Temp/installerx-weekly-2026-11-16` uses the D: exception
for sustained/small-file Gradle/native emulator I/O and a disposable AVD. Existing
checkout and SDK bindings remain. Cleanup transaction
`409904b9df1f14b3d7254e7585955584` registered this root and six exact generated
build roots before generation. Retain current release and required verification
evidence at `D:/Artifacts/InstallerX-Revived/26.11.1002`.

The current Windows zone is Romance Standard Time (Europe/Madrid), matching Berlin
in winter and summer. The authorized automation update changes its saved local
hour from 10:00 to 09:00 and replaces the stale Bucharest conversion sentence.
Final readback/conversion verification remains required. Use the installed native
Coordinator launcher for Gradle and Android ownership, one emulator at a time,
without snapshots or physical-device access.

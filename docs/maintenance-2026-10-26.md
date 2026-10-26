# Weekly upstream synchronization — 2026-10-26

Authorization: already_authorized by the weekly maintenance task and standing
engineering instructions. Authoritative requirements: `fork-maintenance.md`,
`resigning-spec.md`, `signing-backup-spec.md`, `morphe-signing-spec.md`,
`morphe-handoff-spec.md` and `branding-spec.md`; their feature ledgers remain
applicable. This ledger does not change any product requirement.

Baseline: clean `c652939482632746d3fa54fe45829833fac0fc10`, synchronized with
origin/main; normal release 26.10.1007, code 1585. Upstream main
`35d7c7a0` adds verified AOSP signing-key badges, SHOW_APP_INFO routing to the
existing uninstall flow, and translations (three missing commits).

| Stage | Status | Requirements and acceptance |
|---|---|---|
| 1. Integrated upstream synchronization | COMPLETE | Merge all three commits without rebasing; preserve every fork behavior, branding/link/resource contracts and both native UIs. Fresh style, host suite, Preview compilation and relevant isolated Android signing/backup/install checks pass. Review actual merge and test evidence. |
| 2. Verified signed delivery | ACTIVE | Commit verified integration, build optimized Stable from that exact source with the next numeric version (26.10.1008) and code >1585. Verify APK/package/label/certificate/alignment/hash, updater and applicable optimized runtime behavior; push/tag, publish normal release with APK/checksum, independently verify public download/source/latest feed; clean reviewed expendables. |

Stage 1 evidence: automatic merge reviewed; style, full Unstable host tests,
Preview and Unstable/test APK builds pass (gate ticket 1781). The supervisor
verified its shared mutex, two workers, no parallel projects, 3 GB Gradle heap,
Kotlin in-process and one 512 MB test fork; single-use daemon exit is verified.
All resource XML has retained branding and no unsupported links/presets. Both
shared patch-action rows survive the merge; AOSP notice mapping is covered by
upstream recognition, selection, unverified-key and mapper regressions.

API 35 isolated emulator signing/backup integration passes: generated identity,
encrypted persistence/fail-closed corruption, PKCS12 round trip, Morphe BKS
import, real signed base/split updates, data retention and official-store bypass.
Actual SHOW_APP_INFO intents reach the existing target-specific uninstall
confirmation in Material and Miuix, and Cancel retains the installed fixture.
Both native preparation screens hide Resign for a matching installed signer,
show it for a first install and hide Morphe when its receiver is absent.
Evidence: `integration-audit.json`, `integration-gate.json`, host test XML,
`runtime-signing-backup.txt` and `runtime-integration.json` in the evidence folder.

Stage-closure assessment: upstream reuses the existing notice mapper and uninstall
session/permission/confirmation path; no duplicate signing state, obsolete path
or material design debt requiring consolidation was introduced.

Stage 1 blockers: none. Stage 2 advancement blockers: optimized artifact,
installer-update identity check and verified public delivery remain pending
(internal; resolve through the recorded final delivery workflow).

Tracked deferrals: none. Only Stage 2 is ACTIVE.

Integration inspection: clean automatic merge retains both shared patch-action
rows and signer policy. Corrected one existing Portuguese module-banner reference
to the old product name, as required by N1. The first launcher attempt exited
before Gradle started: Windows PowerShell treated the gate's stderr queue event
as a terminating NativeCommandError. Its exact supervisor PID exited and no
Gradle process/support record exists. The reviewed Python launcher now observes
the gate directly without that PowerShell pipeline; it preserves child exit and
full output. This was a launcher error, not a build or policy rejection.

Native diagnostic correction: the matching-version preparation correctly renders
`Reinstall`, while the reused UI probe awaited `Install`. The failure snapshot
shows the expected matching signer and absent Resign. Corrected the probe's
expected primary action and resumed only UI verification; the already passing
Android signing/backup run remains applicable. No product workaround was added.
The probe also now observes replacement accessibility roots after its one action,
and quotes arguments at ADB's remote-shell boundary. Both corrections address
captured harness failures, without delays or product changes. Final UI runs pass.

Scratch: `D:/Temp/installerx-weekly-2026-10-26`; durable release evidence:
`D:/Artifacts/InstallerX-Revived/26.10.1008`. Cleanup transaction
`004914309f0abff7d9608bc1e404186b` inventories that exact scratch root and five
generated build directories. Existing source, keys, deliverables and prior
releases remain protected. No physical device installation is authorized.

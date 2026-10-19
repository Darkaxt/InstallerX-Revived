# Per-APK Morphe handoff and compact signing actions

Authorization: already_authorized on 2026-10-20, including a new normal release.
This extends the existing resigning specification; signing and backup requirements
remain unchanged. The user's fork is Morphe Extended (`app.morphe.extended`).

- H1: Offer a per-APK action in both preparation interfaces, only when Morphe
  Extended is installed, enabled and accepts APK sharing. Any standalone APK is
  eligible; Morphe's universal patches mean no package catalogue restriction.
  Hide it for modules, split-only selections, containers, batch/multiple sources
  and empty selections. Re-evaluate selection and receiver at dispatch.
- H2: Show Morphe's installed application icon beside an outlined crossed-bandage
  Resign icon. Replace the Resign text action, preserve its exact existing
  eligibility and dispatch. Use labelled accessible buttons, at least 48 dp touch
  targets and tooltips; retain each interface's native button styling.
- H3: Send the original standalone APK URI using Morphe's existing ACTION_SEND
  contract, APK MIME type, EXTRA_STREAM, ClipData and a read-only URI grant,
  explicitly restricted to its package. Wrap file URIs with FileProvider. Do not
  transform, sign, delete or install the APK during handoff. Do not create a second
  install session or route patched results automatically back to Morphe.
- H4: Keep InstallerX's preparation session available after handoff so URI access
  and returning/cancelling remain valid. Failure or receiver removal leaves the
  input and signing identity intact and reports an actionable local error.
  Morphe owns patch selection and subsequent installation; its current external
  APK flow requires Expert mode. Document that prerequisite without enabling it
  silently. No Morphe source changes, network access or automatic routing setting.
- H5: Verify eligibility, intent target/URI/grants, no install/sign side effects,
  both UI styles, conditional visibility with Morphe absent/present, actual APK
  reading in Morphe on an isolated emulator, cancellation/return, and unchanged
  Resign signer rules. Preserve release identity and updater behavior.
- H6: Commit and publish the next greater signed normal release following
  fork-maintenance.md; independently verify the public APK, checksum, package,
  version, certificate, alignment and updater. Clean reviewed expendable outputs;
  preserve source, deliverables, evidence and signing keys. No physical-device
  installation.

## Execution and reconciliation

### Stage 1: complete per-APK workflow
Status: COMPLETE
Requirements: H1-H5. Acceptance: both native preparation interfaces expose the
specified actions and the actual original APK reaches Morphe through a read-only
grant, with no install/sign side effects and safe return/failure behavior. Focused
host tests, style, Preview compilation and isolated runtime verification pass.
Satisfied: H1-H4 integrated in both native interfaces. Morphe's exported ACTION_SEND
entry point and Expert-mode requirement verified in local source. Selection/event
regressions, signer rules, encrypted identity/backup/import host tests and style
passed; Preview and Unstable APKs compiled. Gate tickets 1519/1520 verified two
workers, no parallel projects, 3 GB Gradle heap and daemon exit; host tests used one
512 MB fork. No production signing/storage code changed.
Native evidence: API 35 isolated emulator, 360 dp phone. Both interfaces render
adjacent labelled Morphe/bandage controls with 48 dp targets; absent receiver
screens omit Morphe. Real native clicks in both interfaces deliver the original
APK to Morphe 1.35.0.9, which parses it and reports no patches for this fixture in
its unconfigured bundle environment. System Back restores the same preparation,
even with notification-on-dismiss disabled. Original APK SHA-256 stays unchanged.
Intent/grant checks and real Android signing/install/backup checks passed.
Evidence: D:/Artifacts/InstallerX-Revived/26.10.1007, including both consumed/return
logs, original hash, native hierarchies and inspected screenshots. No claim of a
successful patch/install inside Morphe is made; that flow remains Morphe-owned.
Certificate-settings regression passed in both styles on the phone viewport:
controls reachable, replacement cancelled and original identity preserved. The
test now targets vertical scrolling and keeps cancellation assertions on the
visible underlying control. Initial viewport/harness failures were resolved;
the optional wide emulator attempt hit a shader compiler failure and was stopped.
Stage-closure assessment: shared eligibility/intent code serves both current UIs;
the activity owns handoff lifecycle retention. No duplicated business rules,
obsolete signing path or material design debt requiring consolidation was found.
Advancement blockers: none.
Tracked deferrals: none.

### Stage 2: verified release
Status: ACTIVE
Requirements: H6 and integrated H1-H5. Acceptance: exact-source optimized signed
release, runtime checks applicable to optimized code, public delivery/updater and
cleanup verified. Final specification reconciliation has no remaining gaps.
Advancement blockers (internal): optimized signed build, optimized native checks,
public artifact/updater verification and reviewed cleanup remain. Resolve all
before release-stage closure and overall completion.
Tracked deferrals: none.

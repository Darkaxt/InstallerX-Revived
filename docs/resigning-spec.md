# InstallerX personal signing fork

Authorization: already_authorized by the user on 2026-10-02. This document is
the authoritative specification; the execution ledger does not override it.

## Required behavior

- R1: Generate an RSA private key and self-signed certificate locally on first
  signing use. Reuse the same identity across app restarts and fork updates.
  No mandatory import and no bundled universal APK re-signing key. Keep private
  material protected by Android Keystore. Optional portable backups and legacy
  identity preservation are specified by `signing-backup-spec.md` (authorized
  extension on 2026-10-03).
- R2: The authorized 2026-10-05 visibility extension in `morphe-signing-spec.md`
  restricts the action to first installs or confirmed certificate conflicts.
  Add a third action named `Resign` in both Material and Miuix install
  preparation UIs for eligible APK selections whose incoming request is not
  from a known official store. Unknown origin is eligible. The explicit action
  signs, verifies, and installs using the existing installation flow.
- R3: Support single APKs and selected base/split APK sets from supported APK
  containers. Sign all selected APKs with one identity. Preserve original input
  bytes, names, package identity, version, selection, and install configuration.
  Do not install stale APK-associated dex metadata after changing signatures.
  Leave modules untouched. Do not sign a split-only update to an installation
  with a different key. Clean task-owned signing copies on success/failure.
- R4: Detect an existing installation using the fork's generated certificate
  by querying its actual current signers (independent of optional signature
  display/check settings). Automatically re-sign its subsequent non-store
  updates without a new prompt in dialog, notification, automatic and batch
  flows. Remember an explicit signing choice through uninstall-and-retry.
- R5: Official-store incoming requests skip both the new action and automatic
  signing. Use caller provenance captured by existing source resolution,
  before user-configured installer metadata; do not confuse the installed
  app's installer with the incoming source. Recognize Google Play, Samsung
  Galaxy Store, Amazon Appstore, Huawei AppGallery and Xiaomi GetApps. Origin
  classification is best effort and is not a certificate authenticity claim.
- R6: Verify produced APK signatures and refresh effective signature information
  before existing profile gates. Reuse existing mismatch, failed-install,
  uninstall confirmation and retry behavior. Never uninstall automatically
  or bypass existing profile checks. Signing failure stops installation.
- R7: Maintain a public Darkaxt/InstallerX-Revived fork with upstream remote,
  distinct application ID and fork update endpoint. Keep GPL attribution,
  upstream history and fork behavior. Establish persistent *installer release*
  signing separately from the device-generated *APK re-signing* key.
- R8: Provide a verified signed release artifact and release maintenance path.
  Do not claim device verification without execution on an Android device or
  emulator. Verify host tests, both UI compilation, optimized release, APK
  identity, version, signature and checksum before delivery.
- R9: Create a weekly Monday 09:00 Europe/Berlin task attached to this task to
  fetch upstream main, merge new commits preserving the feature, verify and
  publish the next proper signed fork release. No changes/releases when no new
  commits exist. Preserve dirty work and keys, never force push, and report
  meaningful completion/failure or required action only.

## Acceptance criteria

- AC1 (R1-R3,R6): The explicit UI action reaches real signing, verification and
  installation code. A real APK signed by a different key is transformed to the
  personal key, original bytes stay unchanged, and output passes apksig.
  Base/splits share that key; failure, metadata and source cleanup are covered.
- AC2 (R4-R6): Tests prove automatic personal-key updates, official-store bypass,
  unknown origin, disabled optional checks, mixed batches and retry behavior.
  Signature/profile analysis uses the signed selection; existing conflict UI
  remains authoritative. Both UI families compile.
- AC3 (R7-R9): Fork remote/commits are verified, focused tests and required style
  checks pass, Preview debug and Stable release build, release APK is independently
  verified, weekly task exists with exact maintenance scope. Any unavailable
  Android execution is recorded honestly rather than inferred from host tests.

## Staged execution

See [resigning-plan.md](resigning-plan.md). Required blockers/deferrals must be
resolved before overall completion. The initial version did not require optional
import/export; the authorized extension is in `signing-backup-spec.md`.

# Fork release and weekly synchronization

Fork: https://github.com/Darkaxt/InstallerX-Revived
Upstream: https://github.com/wxxsfxyzm/InstallerX-Revived (main)
Application ID: `com.darkaxt.installerx.resign` (coexists with upstream).

## Signing identities

The fork's installer-release identity is persistent and different from the
personal APK identity generated separately on each device. Local release
credentials are stored outside Git in `D:\Keys\InstallerX-Revived\signing.json`
and `installerx-release.jks`. This directory is restricted to the current user
and SYSTEM. Never print, commit or replace this material during synchronization.
Future releases must use the same certificate to retain the installer UID and
its device-generated signing identity.

GitHub Actions uses the existing `SIGNING_KEY_STORE_BASE64`,
`SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS` and `SIGNING_KEY_PASSWORD` secrets.
`SIGNING_CERT_SHA256` is a public repository variable used to verify the release
certificate. If local credentials are unavailable, use the maintained manual
release workflow; do not generate a replacement release key.

Release certificate SHA-256:
`8dc0ebe83c9c991f6b05c0dd751f8d9673888b9e5f0a3aa93da0d8bfdc7655bf`.
The manual workflow verifies a signed draft; publish it only after inspecting
the artifact and completing the maintenance acceptance criteria. Automatic
upstream preview publishing is disabled for this fork.

## Verification and release

1. Preserve dirty/active work. Fetch `origin` and `upstream`, inspect exact missing
   commits, then merge upstream main without rebasing or force pushing. Use a
   clean temporary checkout under `D:\Temp` when the main checkout is active.
2. Reconcile [resigning-spec.md](resigning-spec.md) and retain generated-key
   signing, incoming-store exclusion, actual-current-signer detection,
   per-package retry retention, original-source handling and both UI actions.
   Keep the fork ID, update endpoint, version ordering and release identity.
3. Use JDK 25 and installed SDK/Build Tools 37.0.0. Export local credential
   values to the current process's `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`,
   `KEY_ALIAS` and `KEY_PASSWORD`; never put them in tracked Gradle properties.
4. Run `spotlessCheck`, `testUnstableDebugUnitTest`, `assemblePreviewDebug` and
   `assembleStableRelease -PVERSION_NAME=<next numeric fork version>`. Run
   focused Android signing/update tests on an isolated emulator when available,
   especially after signing/session/source-boundary changes. Never target Thor
   or another physical device automatically. Record unavailable device evidence.
5. Verify package ID, version name/code, expected certificate with
   `apksigner verify --verbose --print-certs`, and alignment with `zipalign -c
   -P 16 -v 4`. Compute SHA-256. Only after successful verification, commit and
   push the completed sync, tag that exact commit, and publish a normal fork
   release with the APK, checksum and concise source-change notes. Inspect the
   published asset/signature/checksum and ensure the in-app updater selects it.
6. Use numeric tags such as `26.10.1001`, then `26.10.1002`. When the date prefix
   advances, start its suffix at 1001. The version must compare greater than the
   latest fork release under `OnlineUpdatePolicy`; Git commit count must also
   advance. Filename: `InstallerX-Revived-online-<version>.apk`, retaining the
   `online` token required by the existing updater.
7. Leave tags/releases unchanged on unresolved conflicts or verification
   failure. Clean only reviewed task-generated expendable outputs, preserving
   deliverables, verification evidence and signing material.

## Schedule

The Codex task heartbeat runs weekly on Monday at 09:00 Europe/Berlin. It stays
quiet when no upstream changes require action and publishes only a fully
verified integrated update. Report meaningful completion, failure or required
user action. Do not create speculative releases or merge unmerged upstream PRs.

Automation ID: `sync-and-release-installerx-resign` (ACTIVE), attached to the
original implementation task. Its saved recurrence and scope were inspected.

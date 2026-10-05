# Morphe default credential import

Authorization: existing signing implementation and release workflow, extended by
the user's default-credential clarification on 2026-10-05. Authoritative behavior
is M5 of morphe-signing-spec.md; M1-M4 and existing R/B/N requirements remain.

## Stage 1: default import and fallback
Status: COMPLETE
Acceptance: BKS v2 defaults reach fingerprint review without credential entry;
failed defaults quietly retain bytes for manual retry; confirmation remains
mandatory; passwords clear and cancellation propagates; PKCS12 and BKS v1 remain
safe; both native interfaces exhibit success/fallback on an isolated emulator.
Satisfied: focused host regressions and codec checks pass after reproducing the
missing default attempt. Both native interfaces on API 35 reach the expected
fixture fingerprint directly with defaults and after custom credential entry;
cancellation preserves the original identity. PKCS12 and BKS v1 routing,
password clearing and coroutine cancellation are verified. Style and Preview
build pass. Evidence: D:\Artifacts\InstallerX-Revived\26.10.1005.
Remaining: none.
Blockers: none. Tracked deferrals: none.

## Stage 2: verified release and delivery
Status: COMPLETE
Acceptance: focused verification and optimized runtime checks; exact-source commit,
increasing signed release with existing package/identity; uploaded asset independently
verified and published as latest; specification reconciliation and transactional cleanup.
Satisfied: optimized Stable 26.10.1005/code 1568 built from source
941e277dd500784bfadf3d1ec84a05b93cd1e988; both native interfaces verified with real
BKS v2 fixtures using defaults and custom credentials. Confirmation/cancellation,
activation and encrypted identity persistence through restart pass. Existing
package, label, release certificate, v2/v3 signatures, alignment and checksum pass.
Tag points to the exact built source. Normal latest release published; independently
downloaded uploaded and anonymous public APKs match the local checksum/signature.
The actual compiled Stable updater selects the published APK/version/download URL.
Gate acquired for both successful builds: observed two workers, parallel=false,
3 GB Gradle heap; separate Kotlin daemon -Xmx3g observed; host tests one fork/512 MB.
No native compilation, memory failure or budget increase. Mapping and verification
evidence retained in D:\Artifacts\InstallerX-Revived\26.10.1005. Reviewed cleanup
applied without errors: 18864 expendable members / 715548165 bytes removed.
Remaining: none.
Blockers: none. Tracked deferrals: none.

## Final reconciliation

M5 is satisfied and verified end to end. M1-M4 and R/B/N requirements remain
preserved: this change only affects shared import inspection and its cancellation
state; signing, identity storage, backup cryptography, install policy, branding,
dependencies and release identity remain unchanged. Focused codec checks and real
optimized imports reconfirm the changed boundary. The prior 26.10.1004 evidence
continues to apply to unchanged installation/signature/source/backup behavior.
Upstream was fetched again; there were no commits missing from this fork.
The existing weekly automation follows the amended authoritative specification.
No physical device was modified and no actual user private keystore was accessed.
No required blocker or tracked deferral remains. This completion ledger is the only
post-tag source change.

Release: https://github.com/Darkaxt/InstallerX-Revived/releases/tag/26.10.1005
APK SHA-256: 1694f2d90b4a6189a5438c7a201f69ac074439f8a5d8f3a284b89b52981c6a03

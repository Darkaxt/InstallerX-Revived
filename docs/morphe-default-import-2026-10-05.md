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
Status: ACTIVE
Acceptance: focused verification and optimized runtime checks; exact-source commit,
increasing signed release with existing package/identity; uploaded asset independently
verified and published as latest; specification reconciliation and transactional cleanup.
Satisfied: none yet. Remaining: all stage acceptance criteria.
Blockers: none. Tracked deferrals: none.

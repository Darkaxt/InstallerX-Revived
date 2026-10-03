# Branding execution ledger

Source of truth: `branding-spec.md`; already_authorized.

## Stage 1 — product branding and supported links

Status: COMPLETE

Requirements: N1-N4, N5 source/host/UI verification.
Acceptance: old product branding removed from bundled UI, backup filename/banner
updated, both interfaces expose only maintained product destinations, unsupported
proxy removed with deterministic legacy normalization, renamed assets selected
by updater, style/host/Preview checks and emulator UI verification pass.
Satisfied: N1-N4 audited across all 27 bundled locales and product URL actions.
Style checks, full Unstable host suite and Preview compilation passed (2026-10-03).
Updater regressions verify old proxy normalization and renamed asset selection.
On Android 15/API 35, both Home/Preferences interfaces display the new branding,
both update dialogs expose GitHub only, and both network dialogs expose Official
and Custom only. Material About displays the new app name. The emulator does not
render the existing Miuix About header effect (also checked with blur disabled);
Miuix branding was verified through Home/Preferences and compiled resources. No
rendering behavior was changed. Weekly task and manual workflow naming updated.
Remaining: none.
Blockers: none. Tracked deferrals: none.

## Stage 2 — signed release and publication

Status: ACTIVE

Requirements: N5 commit, exact-source Stable build, publication and cleanup.
Acceptance: source committed, optimized artifact identity/signature/alignment
verified, normal release published with renamed APK/checksum, public download and
latest updater feed verified, task-generated expendables cleaned.
Satisfied: none. Remaining: all release criteria.
Blockers: none. Tracked deferrals: none.

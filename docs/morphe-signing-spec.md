# Morphe identity import and explicit resign visibility

Authorization: already_authorized by the user on 2026-10-05. This extends R2 of
resigning-spec.md and B3 of signing-backup-spec.md; other R/B/N requirements remain.

- M1: Both preparation interfaces show Resign only for a selected base APK with
  no installed app or a confirmed incoming-versus-installed certificate conflict.
  Hide the action for matching certificates, unknown comparison, modules,
  split-only inputs and official-store incoming requests. Compare actual signers
  even when optional signature-display checks are disabled. Passive personal-key
  signing remains automatic; first-install and conflict actions use existing flow.
- M2: Import Morphe Extended's existing BKS v2 .keystore/.bks into InstallerX, keeping
  its private key and certificate unchanged. Support Morphe's key alias, separate
  key password and optional empty outer-store password. Verify BKS integrity with
  the supplied password (including empty); explicitly verify the MAC before the provider parses
  the store, since its empty-password load skips integrity. Reject legacy BKS v1.
  Require a nonempty key password, explicit alias, RSA >=2048 and matching chain,
  size/chain bounds. Wrong password/alias, corrupt input and cancellation leave
  the active identity unchanged. No mandatory import or release-key changes.
- M3: Both native settings interfaces accept BKS through the existing document
  picker, detect the BKS header, request appropriate credentials privately, then
  review the candidate fingerprint and require explicit activation. Passwords
  and pending private material are never logged or saved through UI restoration.
  Existing encrypted persistence, PKCS12 export/import and legacy keys remain.
- M4: Prove matching/new/conflicting/store/split visibility contracts; BKS imports
  for empty and nonempty store passwords, separate key passwords, wrong
  credentials/tampering; unchanged fingerprint and APK signatures through BKS
  import and PKCS12 backup. Verify Android import persistence and both interfaces
  on an isolated emulator; do not replace physical-device identities or install
  on a physical device. Include the completed change in the authorized release.

Observed Morphe source: local Morphe-Extended KeystoreManager.export copies its
BKS bytes; key alias/password/store password are independent. Existing phone
YouTube Morphe signer differs from InstallerX personal identity; no actual user
private key was copied or activated during investigation.

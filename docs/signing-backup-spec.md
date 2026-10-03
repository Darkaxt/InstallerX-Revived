# Personal signing identity import and export

Authorization: already_authorized by the user on 2026-10-03. This is the
authoritative extension to `resigning-spec.md`; it changes the original R1
storage requirement to permit exportable keys encrypted at rest by Android
Keystore. The original installation, provenance and conflict requirements remain.

## Requirements and acceptance criteria

- B1: New installations still generate a unique RSA identity without mandatory
  import. Store exportable private material only in authenticated encrypted
  app-private, non-backed-up storage, protected by an Android Keystore AES key.
  Reuse that identity after restart. Corrupt storage fails closed without
  generating a replacement. Existing non-exportable Keystore identities remain
  active and unchanged until an explicit replacement is confirmed.
- B2: Export the active private key and certificate as password-protected PKCS#12
  (`.p12`). Require a nonempty password with confirmation. Export the public
  certificate separately as PEM (`.pem`), including for legacy identities.
  Clearly label certificate-only export as insufficient for restoring signing.
  No private export is possible for a legacy Android Keystore key.
- B3: Import password-protected PKCS#12 (`.p12`/`.pfx`) containing exactly one
  RSA private key (at least 2048 bits) and matching X.509 certificate chain.
  Validate the key/certificate pair before proposing replacement. Wrong password,
  malformed, oversized, ambiguous or certificate-only input leaves the identity
  unchanged. Use Android document pickers; handle cancellation and I/O failure.
- B4: Show the certificate SHA-256 fingerprint and backup capability in both
  Material and Miuix settings. Before importing or generating a replacement,
  show its fingerprint and warn that changing keys affects updates to apps signed
  with the old key. Preserve the legacy key and allow explicitly returning to it.
  Keep passwords and pending private material out of saved UI state and logs.
- B5: A restored backup signs APKs with the same certificate and can update a
  real installation signed before export. Preserve the existing automatic update,
  official-store bypass and uninstall/retry flow. An in-flight signing operation
  uses a single captured identity even if settings change concurrently.
- B6: Verify meaningful host crypto tests, Android encrypted persistence and
  export/import/sign/update integration, both settings interfaces, Preview debug
  compilation and optimized Stable packaging. Document the legacy limitation and
  recovery procedure. Commit the verified result. The initial implementation
  excluded publication; the user's subsequent instruction on 2026-10-03 authorizes
  publishing the verified release APK under `fork-maintenance.md` procedures.

## Scope

PKCS#12 is the optional private identity interchange format; standalone public
certificates cannot sign. Arbitrary JKS/PEM private-key import and a general
multi-identity manager are outside this request. The fork release signing key is
separate and must never be used for personal APK signing.

See `signing-backup-plan.md` for the execution ledger.

# Personal APK signing

Choose **Resign** on the installation preparation screen to sign a copy of the
selected APKs with a key generated locally by this installer, verify the result,
and continue through the normal installation workflow. No import is needed.
The action is available in both Material and Miuix interfaces only for a first
installation or a confirmed certificate conflict. It is hidden when the incoming
APK matches the installed certificate or when comparison is unavailable. Modules
and split-only selections do not offer this action. Optional signature-display
settings do not disable the actual signer comparison.

When an installed app's actual current certificate matches the generated key,
incoming updates are re-signed automatically. This also works for notification,
automatic and batch installation. In a batch, unrelated apps retain their
original signatures. The choice is retained through the existing uninstall and
retry workflow within the same session.

Known incoming sources from Google Play, Samsung Galaxy Store, Amazon Appstore,
Huawei AppGallery and Xiaomi GetApps skip re-signing. The policy uses the existing
caller/UID/proxy or content-provider provenance, not the installer recorded on
the installed app. An unknown source remains eligible. A referrer string alone
cannot assert official-store origin. A file copied from a store and later opened
through a file manager may have unknown origin; APKs do not reliably record their
original shop. This classification does not authenticate the APK publisher.

The base APK and selected splits receive the same signature. APK-associated
dex metadata is omitted because it belongs to the original APK bytes. Modules
are not re-signed. The downloaded source is not overwritten; the existing
profile's source auto-delete setting still applies after installation.

Existing signature mismatch warnings, profile restrictions and uninstall/retry
suggestions remain in effect. The first personally signed installation may
require uninstalling a version signed by someone else. Different mods may still
have incompatible saved data or certificate checks.

## Keeping the identity

New installations generate an exportable key and self-signed certificate locally
on first signing use, without requiring import. The private key is encrypted with
AES-256-GCM in app-private non-backed-up storage. Its encryption key stays in
Android Keystore. Updating this fork retains the identity.

Open **Preferences → Backup & Restore → APK signing certificate** in either UI.
The screen shows the active certificate's SHA-256 fingerprint.

- **Export signing backup** saves both the private key and certificate as a
  password-protected PKCS#12 `.p12` file. Choose a strong password, keep it
  separately, and store the file somewhere that survives clearing this app or
  replacing the device. Exports use AES-256 with PBKDF2-HMAC-SHA256 (100,000
  iterations) and a SHA-256 PKCS#12 MAC. Anyone with the file and password can
  sign APKs as this identity; do not publish it.
- **Import signing backup** accepts `.p12`/`.pfx` files containing exactly one
  RSA private key of at least 2048 bits with its matching X.509 certificate.
  Enter the backup password, check the fingerprint, then confirm activation.
  Invalid files, wrong passwords and cancellation do not replace the identity.
  Backups are limited to 1 MiB and certificate chains to 16 certificates.
- **Export public certificate** saves a PEM `.pem` certificate for inspection
  or identification. It contains no private key and cannot restore signing.
- **Generate exportable identity** creates a new candidate and asks for
  confirmation before activating it. Changing keys affects updates to apps
  signed with the previous key. Back up the current exportable identity first.

After reinstalling or changing devices, import your signing backup before
updating personally signed apps. Check that its fingerprint matches your old
identity. Automatic update signing then uses that restored key. Without a
backup, clearing app data or uninstalling the installer loses the identity.

### Use the existing Morphe Extended certificate

Export the signing keystore from Morphe Extended's settings. In InstallerX,
choose **Preferences → Backup & Restore → APK signing certificate → Import
signing backup**, then select the exported `.keystore`/`.bks`. InstallerX first
tries Morphe's defaults: alias `Morphe`, private-key password `Morphe`, and an empty
store password. When these work, it goes directly to fingerprint confirmation.
If they fail, enter the same key alias and key password used in Morphe, plus its
store password if one is set (otherwise leave that field empty).
Review the candidate SHA-256 fingerprint
against Morphe's signing key, then confirm activation. InstallerX encrypts its
own copy; Morphe keeps its existing key unchanged. PKCS#12 export remains available
for portable backups of the imported identity. A public certificate alone cannot
restore a signing key. No key or credential is automatically read from Morphe.

### Identities created by the first release

Version `26.10.1001` generated a non-exportable Android Keystore private key.
Updating the installer preserves that original identity and keeps it active.
Android does not allow recovering its private key for a backup; importing or
generating a new key cannot make the old one exportable. Its public certificate
can still be exported. **Do not clear data or uninstall this installer while
you need the original device-only key.**

You may explicitly activate an imported or newly generated exportable identity.
The original device-only key stays in Android Keystore, and **Use original
device-only key** switches back after confirmation. This replaces the current
exportable identity, so export that identity first if you will need it again.
Existing mismatch warnings and uninstall/retry remain authoritative; switching
keys never automatically uninstalls an app.

The installer APK's release signing certificate is separate from this personal
key. Release builds never contain a shared personal APK signing key.

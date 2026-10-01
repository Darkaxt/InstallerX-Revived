# Personal APK signing

Choose **Resign** on the installation preparation screen to sign a copy of the
selected APKs with a key generated locally by this installer, verify the result,
and continue through the normal installation workflow. No import is needed.
The action is available in both Material and Miuix interfaces.

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

Each installer installation creates its own key in Android Keystore. Updating
this fork retains the key. Clearing its data, uninstalling it, or changing devices
removes access to the identity. This initial version has no export/import UI.
Do not uninstall or clear this installer if you want to keep updating apps that
it has personally signed.

The installer APK's release signing certificate is separate from this personal
key. Release builds never contain a shared personal APK signing key.

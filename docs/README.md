# InstallerX Resigned

InstallerX Resigned is this maintained fork of InstallerX Revived. It adds optional
personal APK re-signing and portable signing-identity backups to the existing
Android installer. Both Material and Miuix interfaces are supported.

## Downloads and documentation

- [Latest signed release](https://github.com/Darkaxt/InstallerX-Revived/releases/latest)
- [Personal signing, backups and recovery](resigning.md)
- [Fork maintenance and release verification](fork-maintenance.md)
- [Report a reproducible problem](https://github.com/Darkaxt/InstallerX-Revived/issues)

The app uses package `com.darkaxt.installerx.resign`, so it can coexist with upstream.
Updates retain the same installer signing certificate and your existing personal
signing identities. Choose **Resign** for an eligible APK; later updates to apps
signed with the active identity are re-signed automatically. Import is optional.

Use **Preferences → Backup & Restore → APK signing certificate** to export a
password-protected `.p12` signing backup or import a `.p12`/`.pfx` backup. Public
`.pem` certificates contain no private key and cannot restore signing. Keys created
by version `26.10.1001` are device-only: Android prevents private-key export, but
upgrading preserves them. Read the recovery guide before changing an identity.

Source, releases, issues and the documentation in this repository are the fork's
maintained product resources. It has no dedicated Telegram channel, external
locker-helper recommendation or bundled third-party download proxy. Direct GitHub
downloads and a user-configured custom proxy are available.

## License and attribution

GPL-3.0-only. Copyright (C) iamr0s and InstallerX Revived contributors; fork changes
by Darkaxt. Original source and contributor attribution remain preserved. The
in-app open-source license screen retains third-party library credits and licenses.

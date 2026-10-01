These code-free fixture APKs were generated locally with AAPT2 (Build Tools
37.0.0) from the manifests in app/src/test/resources/resigning/manifests.
The package is com.darkaxt.installerx.resigning.fixture, min SDK 26, target 35.
base.apk is version 1; update.apk and the config.en split are version 2.

They were signed with the PUBLIC AOSP test keys in the host test resources:
base uses rsa-2048; update and split use rsa-2048_2. See the adjacent host
resource NOTICE.md for AOSP/Apache 2.0 attribution. These APKs are test inputs
only and are excluded from the production installer APK.

To regenerate, use `aapt2 link -I <sdk>/platforms/android-35/android.jar
--manifest <manifest> -o <unsigned.apk>`, then `apksigner sign --key
<public-test-key.pk8> --cert <public-test-cert.x509.pem> --out <asset.apk>
<unsigned.apk>`. Never substitute the installer release key or a personal key.

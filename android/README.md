# HomeLedger Android app

Native Android app built with Kotlin, Jetpack Compose Material 3, and Room. The local Room database stores expenses, categories, and preferences. The app has no account, Firebase, cloud sync, external API, or network permission.

## Open and build

Open the `android/` directory in Android Studio with JDK 17 and the Android 17 preview SDK (API 37) installed. The GitHub Actions workflows use Gradle 9.4.1 and AGP 9.2.0.

```sh
gradle -p android :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is produced at `android/app/build/outputs/apk/debug/app-debug.apk` for local development only. It is never published as the website download. To publish a release, configure the GitHub Actions secrets `ANDROID_SIGNING_KEYSTORE_BASE64` and `ANDROID_SIGNING_STORE_PASSWORD`, then run **Build and publish signed Android release** from the repository's `main` branch. The workflow runs unit tests, signs the release variant, verifies its signature/package/version/checksum, installs and launches it on an Android emulator, then publishes the APK and checksum as a GitHub Release. The workflow intentionally does not update website metadata; change `../src/config/release.json` only after the workflow succeeds and the release asset is present.

The keystore is excluded from Git. Back it up securely and keep it for every future update; Android rejects updates signed with a different key. The APK uses application ID `com.homeledger.app` and version values from `app/build.gradle.kts`. Core library desugaring provides the app's `java.time` APIs on supported Android versions.

## Local data and backup

Expenses are stored as integer minor units (pesewas for GHS) with their category and local calendar date. The Settings screen exports and restores a user-managed JSON file using Android's system document picker. Restore replaces the on-device ledger after an explicit confirmation. No network permission is declared.

## Current implementation notes

The installed app starts with an empty ledger rather than fake household records. Reports use real saved rows and are covered by pure Kotlin calculation tests. Review [../DESIGN_SPEC.md](../DESIGN_SPEC.md) for visual tokens, screen layouts, and interaction rules. Play Protect may classify a new direct-download APK as "Uncommon" because it has not seen the app before. A stable release signature does not guarantee that warning will disappear; do not disable Play Protect.

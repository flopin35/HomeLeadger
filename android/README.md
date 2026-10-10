# HomeLedger Android app

Native Android app built with Kotlin, Jetpack Compose Material 3, and Room. The local Room database stores expenses, categories, and preferences. The app has no account, Firebase, cloud sync, external API, or network permission.

## Open and build

Open the `android/` directory in Android Studio with JDK 17 and Android SDK 37 installed. The GitHub Actions workflow uses Gradle 9.4.1 and AGP 9.2.0.

```sh
gradle -p android :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is produced at `android/app/build/outputs/apk/debug/app-debug.apk`. It is signed with the standard debug key and is suitable for installation for evaluation, not a Play Store release. The website deployment workflow copies a successful build to `public/downloads/homeledger.apk`, marks that deployment's release metadata available, and publishes the APK with the static site. The committed release config remains `coming_soon` until CI has assembled and tested the APK.

## Local data and backup

Expenses are stored as integer minor units (pesewas for GHS) with their category and local calendar date. The Settings screen exports and restores a user-managed JSON file using Android's system document picker. Restore replaces the on-device ledger after an explicit confirmation. No network permission is declared.

## Current implementation notes

The installed app starts with an empty ledger rather than fake household records. Reports use real saved rows and are covered by pure Kotlin calculation tests. Review [../DESIGN_SPEC.md](../DESIGN_SPEC.md) for visual tokens, screen layouts, and interaction rules. A debug build is a development preview; sign and verify a release build separately before broader distribution.

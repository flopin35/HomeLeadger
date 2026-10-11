# HomeLedger website

A static-export Next.js landing page for the HomeLedger Android expense tracker. The download state defaults to **coming soon** and only becomes available after a signed APK is published and verified.

## Requirements and local development

- Node.js 20 or later
- npm

```sh
npm install
npm run dev
npm run typecheck
npm run lint
npm run build
```

Open `http://localhost:3000` during development. The production build exports a static site to `out/`. Preview it with `npx serve out`.

## Android release process

Edit `src/config/release.json`, the single source for download and release metadata:

- `status`: `coming_soon` (default), `available`, or `unavailable`.
- `downloadUrl`: the actual HTTPS GitHub Release APK asset URL.
- `version`: the Android `versionName` verified in that APK.
- `sha256`: the APK's verified SHA-256 checksum.
- `notes`: verified release notes.
- `supportContact`: optional `mailto:` link or HTTPS contact page.

The `android-release.yml` workflow builds a signed APK only when manually run from `main`. It reads the keystore and password from the `ANDROID_SIGNING_KEYSTORE_BASE64` and `ANDROID_SIGNING_STORE_PASSWORD` repository secrets, checks the signature, package, version, and checksum, installs and launches the APK on an Android emulator, then publishes the APK and checksum as a GitHub Release. Never commit a keystore or signing secret. Keep a secure backup of the release key; Android requires the same signing identity for future APK updates.

Only after the release asset exists and its signature, package, version, emulator install, and checksum have passed verification should `src/config/release.json` be changed from `coming_soon` to `available`. The production site build checks the HTTPS release asset and checksum before publishing the download button. Never add a placeholder APK.

Play Protect may show its "Uncommon" warning for a new direct-download app. A release signature provides a stable update identity; it does not create Play Protect reputation or imply Google review. Keep Play Protect enabled. If Google reports a harmful app or blocks the verified APK, stop distribution and follow Google's developer guidance rather than asking users to turn security checks off.

Device previews and Ghanaian cedi sample figures are illustrative artwork, not actual app screenshots or user data.

## Deploy

The GitHub Actions workflow builds and deploys the static export to GitHub Pages on pushes to `main`. In repository settings, choose **Pages > Build and deployment > Source > GitHub Actions**. The workflow sets `/HomeLeadger` as the base path for this repository. For another repository or a custom domain, update `NEXT_PUBLIC_BASE_PATH` in `.github/workflows/pages.yml` and the matching base path used by `next.config.ts`.

For another static host, publish the contents of `out/`. No database, API keys, or hosting secrets are required.

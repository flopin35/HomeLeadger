# HomeLedger website

A static-export Next.js landing page for the planned HomeLedger Android expense tracker. The APK release state defaults to **coming soon**. The site only renders a working download link after a real APK is configured and the release is explicitly marked available.

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

## Release configuration

Edit `src/config/release.json`, the single source for download and release metadata:

- `status`: `coming_soon` (default), `available`, or `unavailable`.
- `downloadUrl`: `/downloads/homeledger.apk` by default, or an absolute HTTPS URL for an externally hosted APK.
- `version`: leave blank until a real version is verified.
- `notes`: optional, verified release notes.
- `supportContact`: optional `mailto:` link or HTTPS contact page.

Before release, build and test the Android application separately. For a local APK, place the real file at `public/downloads/homeledger.apk`; the production build fails if status is `available` and the local file or verified version is missing. For an external HTTPS URL, verify the asset and test its download before switching the status. Never add a placeholder binary. `public/downloads/README.md` marks the intended location.

Feature, offline, local storage, and backup language describes design intent until verified against an actual app release. Update claims when app behavior is confirmed. Device previews and Ghanaian cedi sample figures are illustrative artwork, not actual app screenshots or user data.

## Deploy

The GitHub Actions workflow builds and deploys the static export to GitHub Pages on pushes to `main`. In repository settings, choose **Pages > Build and deployment > Source > GitHub Actions**. The workflow sets `/HomeLeadger` as the base path for this repository. For another repository or a custom domain, update `NEXT_PUBLIC_BASE_PATH` in `.github/workflows/pages.yml` and the matching base path used by `next.config.ts`.

For another static host, publish the contents of `out/`. No database, API keys, or hosting secrets are required.

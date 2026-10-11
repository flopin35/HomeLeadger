import { createHash } from 'node:crypto';
import { existsSync } from 'node:fs';
import { readFile } from 'node:fs/promises';
import { resolve, sep } from 'node:path';
const config = JSON.parse(await readFile(new URL('../src/config/release.json', import.meta.url), 'utf8'));
if (!['coming_soon', 'available', 'unavailable'].includes(config.status)) throw new Error('Invalid release status in src/config/release.json.');
if (config.status !== 'available') process.exit(0);
if (!config.version?.trim()) throw new Error('Set a verified release version before making the APK available.');
if (!/^[a-f0-9]{64}$/i.test(config.sha256 ?? '')) throw new Error('Set the verified APK SHA-256 in src/config/release.json.');

let apkBytes;
if (/^https:\/\//i.test(config.downloadUrl ?? '')) {
  const expectedUrl = `https://github.com/flopin35/HomeLeadger/releases/download/v${config.version}/HomeLedger-v${config.version}.apk`;
  if (config.downloadUrl !== expectedUrl) throw new Error(`The release URL must match the published HomeLedger ${config.version} GitHub Release asset.`);
  const response = await fetch(config.downloadUrl, { redirect: 'follow' });
  if (!response.ok) throw new Error(`The configured APK asset returned HTTP ${response.status}.`);
  apkBytes = Buffer.from(await response.arrayBuffer());
} else {
  if (!config.downloadUrl?.startsWith('/')) throw new Error('Use the HTTPS GitHub Release APK URL or a site-relative APK path.');
  const publicDir = resolve('public'), target = resolve(publicDir, `.${config.downloadUrl}`);
  if (!target.startsWith(`${publicDir}${sep}`) || !existsSync(target)) throw new Error(`APK marked available but no file exists at ${target}.`);
  apkBytes = await readFile(target);
}

if (apkBytes.byteLength < 1024) throw new Error('Configured APK is unexpectedly small.');
const actualSha256 = createHash('sha256').update(apkBytes).digest('hex');
if (actualSha256.toLowerCase() !== config.sha256.toLowerCase()) throw new Error('The configured APK SHA-256 does not match the published APK bytes.');
console.log(`Verified HomeLedger ${config.version} APK asset (${apkBytes.byteLength} bytes, SHA-256 ${actualSha256}).`);

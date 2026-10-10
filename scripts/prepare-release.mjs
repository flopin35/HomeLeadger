import { copyFile, readFile, stat, writeFile } from 'node:fs/promises';
import { resolve } from 'node:path';

const sourceApk = resolve('android/app/build/outputs/apk/debug/app-debug.apk');
const siteApk = resolve('public/downloads/homeledger.apk');
const apkInfo = await stat(sourceApk);
if (apkInfo.size < 1024) throw new Error('The assembled APK is unexpectedly small. Refusing to publish it.');
await copyFile(sourceApk, siteApk);
const gradle = await readFile('android/app/build.gradle.kts', 'utf8');
const version = gradle.match(/versionName\s*=\s*"([^"]+)"/)?.[1];
if (!version) throw new Error('Could not read the Android version from app/build.gradle.kts.');
const path = 'src/config/release.json';
const config = JSON.parse(await readFile(path, 'utf8'));
config.status = 'available';
config.version = `${version}-debug`;
config.notes = 'CI-generated debug-signed development preview. This is a prototype build, not a Play Store release.';
await writeFile(path, `${JSON.stringify(config, null, 2)}\n`);

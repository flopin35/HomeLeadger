import release from './release.json';
export type ReleaseStatus = 'coming_soon' | 'available' | 'unavailable';
const basePath = process.env.NEXT_PUBLIC_BASE_PATH || '';
export const site = { name: 'HomeLedger', tagline: 'Your home. Your expenses. All under control.', description: 'Track household bills, everyday expenses, and spending reports with HomeLedger for Android.', platform: 'Android', downloadUrl: release.downloadUrl.startsWith('https://') ? release.downloadUrl : `${basePath}${release.downloadUrl}`, releaseVersion: release.version, releaseNotes: release.notes, releaseStatus: release.status as ReleaseStatus, supportContact: release.supportContact };

import type { Metadata } from 'next';
import type { ReactNode } from 'react';
import './globals.css';
export const metadata: Metadata = { title: 'HomeLedger — Household Expenses Made Simple', description: 'Track household bills, everyday expenses, and spending reports with HomeLedger for Android.', applicationName: 'HomeLedger', openGraph: { title: 'HomeLedger — Household Expenses Made Simple', description: 'Keep track of household spending, understand where your money goes, and plan with confidence.', type: 'website', siteName: 'HomeLedger' }, twitter: { card: 'summary_large_image', title: 'HomeLedger', description: 'Your home. Your expenses. All under control.' }, themeColor: '#F6F8F6', icons: { icon: `${process.env.NEXT_PUBLIC_BASE_PATH || ''}/icon.svg`, apple: `${process.env.NEXT_PUBLIC_BASE_PATH || ''}/icon.svg` } };
export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) { return <html lang="en"><body>{children}</body></html>; }

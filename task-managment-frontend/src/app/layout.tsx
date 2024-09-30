'use client';

import localFont from 'next/font/local';
import { usePathname } from 'next/navigation';
import Providers from '@/app/Providers';
import Navbar from '@/app/common/components/Navbar';
import './globals.css';

const geistSans = localFont({
  src: './fonts/GeistVF.woff',
  variable: '--font-geist-sans',
  weight: '100 900',
});
const geistMono = localFont({
  src: './fonts/GeistMonoVF.woff',
  variable: '--font-geist-mono',
  weight: '100 900',
});

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const pathname = usePathname();
  const isAuthPage = pathname === '/login' || pathname === '/signup';

  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <Providers>
          {!isAuthPage && <Navbar />}
          <main className="min-h-screen bg-background">{children}</main>
        </Providers>
      </body>
    </html>
  );
}

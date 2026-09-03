import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import './globals.css';

const geistSans = Geist({
  variable: '--font-geist-sans',
  subsets: ['latin'],
});

const geistMono = Geist_Mono({
  variable: '--font-geist-mono',
  subsets: ['latin'],
});

export const metadata: Metadata = {
  metadataBase: new URL('http://localhost:3000'),
  title: '地牢生成实验台',
  description: '用区块网格逐步理解 Minecraft 地牢的房间与走廊生成算法。',
  openGraph: {
    title: '地牢生成实验台',
    description: '看懂 2×2 房间、1×1 走廊与 3×3 Boss 房如何生成。',
    images: [{ url: '/og.png', width: 1680, height: 945, alt: '地牢生成实验台区块布局' }],
  },
  twitter: {
    card: 'summary_large_image',
    title: '地牢生成实验台',
    description: '看懂 2×2 房间、1×1 走廊与 3×3 Boss 房如何生成。',
    images: ['/og.png'],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        {children}
      </body>
    </html>
  );
}

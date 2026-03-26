/**
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { siteConfig } from "@/config/site";
import { GlobalContextProvider } from "@/hooks/useGlobalContext";
import "@/styles/main.scss";
import { ColorSchemeScript } from "@mantine/core";
import { Metadata, Viewport } from "next";
import localFont from "next/font/local";
import Script from "next/script";
import { ReactNode } from "react";

export const metadata: Metadata = {
  title: {
    default: siteConfig.name,
    template: `%s - ${siteConfig.name}`,
  },
  description: siteConfig.description,
};

export const viewport: Viewport = {
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "white" },
    { media: "(prefers-color-scheme: dark)", color: "black" },
  ],
};

const googleSansText = localFont({
  src: [
    {
      path: "../public/fonts/GoogleSansText-Regular.ttf",
      weight: "400",
    },
    {
      path: "../public/fonts/GoogleSansText-Medium.ttf",
      weight: "500",
    },
    {
      path: "../public/fonts/GoogleSansText-Bold.ttf",
      weight: "600",
    },
    {
      path: "../public/fonts/GoogleSansText-Bold.ttf",
      weight: "700",
    },
    {
      path: "../public/fonts/GoogleSansText-Bold.ttf",
      weight: "800",
    },
  ],
  variable: "--font-google-sans-text",
});

const googleSans = localFont({
  src: [
    {
      path: "../public/fonts/GoogleSans-Regular.ttf",
      weight: "300",
    },
    {
      path: "../public/fonts/GoogleSans-Regular.ttf",
      weight: "400",
    },
    {
      path: "../public/fonts/GoogleSans-Medium.ttf",
      weight: "500",
    },
    {
      path: "../public/fonts/GoogleSans-Bold.ttf",
      weight: "600",
    },
    {
      path: "../public/fonts/GoogleSans-Bold.ttf",
      weight: "700",
    },
    {
      path: "../public/fonts/GoogleSans-Bold.ttf",
      weight: "800",
    },
  ],
  variable: "--font-google-sans",
});

const googleSansMono = localFont({
  src: [
    {
      path: "../public/fonts/GoogleSansMono-Regular.ttf",
      weight: "300",
    },
    {
      path: "../public/fonts/GoogleSansMono-Regular.ttf",
      weight: "400",
    },
    {
      path: "../public/fonts/GoogleSansMono-Medium.ttf",
      weight: "500",
    },
  ],
  variable: "--font-google-sans-mono",
});

const googleSansCode = localFont({
  src: [
    {
      path: "../public/fonts/GoogleSansCode[wght].ttf",
      weight: "300",
    },
    {
      path: "../public/fonts/GoogleSansCode[wght].ttf",
      weight: "400",
    },
    {
      path: "../public/fonts/GoogleSansCode[wght].ttf",
      weight: "500",
    },
  ],
  variable: "--font-google-sans-code",
});

const googleSansDisplay = localFont({
  src: [
    {
      path: "../public/fonts/GoogleSansDisplay-Medium.ttf",
      weight: "300",
    },
    {
      path: "../public/fonts/GoogleSansDisplay-Medium.ttf",
      weight: "400",
    },
    {
      path: "../public/fonts/GoogleSansDisplay-Medium.ttf",
      weight: "500",
    },
  ],
  variable: "--font-google-sans-display",
});

const materialSymbolsRounded = localFont({
  src: [
    {
      path: "../public/fonts/MaterialSymbolsRounded-Thin.ttf",
      weight: "100",
    },
    {
      path: "../public/fonts/MaterialSymbolsRounded-ExtraLight.ttf",
      weight: "200",
    },
    {
      path: "../public/fonts/MaterialSymbolsRounded-Light.ttf",
      weight: "300",
    },
    {
      path: "../public/fonts/MaterialSymbolsRounded-Regular.ttf",
      weight: "400",
    },
    {
      path: "../public/fonts/MaterialSymbolsRounded-Regular.ttf",
      weight: "500",
    },
    {
      path: "../public/fonts/MaterialSymbolsRounded-Medium.ttf",
      weight: "600",
    },
    {
      path: "../public/fonts/MaterialSymbolsRounded-Bold.ttf",
      weight: "700",
    },
    {
      path: "../public/fonts/MaterialSymbolsRounded-Bold.ttf",
      weight: "800",
    },
  ],
  variable: "--font-material-symbols-rounded",
  display: "auto",
});
const materialSymbolsOutlined = localFont({
  src: [
    {
      path: "../public/fonts/MaterialSymbolsOutlined_Filled-Medium.ttf",
      weight: "300",
    },
    {
      path: "../public/fonts/MaterialSymbolsOutlined_Filled-Medium.ttf",
      weight: "400",
    },
    {
      path: "../public/fonts/MaterialSymbolsOutlined_Filled-Medium.ttf",
      weight: "500",
    },
  ],
  variable: "--font-material-symbols-outlined",
});

export default async function RootLayout({
  children,
}: {
  children: ReactNode;
}) {
  const trustedTypesPolicy = `
    if(window.trustedTypes) {
       <!-- Do nothing to try out an enforcement Trusted Types header with CSP violations collected. -->
    }`;

  const GAID = process?.env?.NEXT_PUBLIC_GA_TAG_ID || "";
  const googleAnalyticsScript = `
      <!-- Google tag (gtag.js) -->
      window.dataLayer = window.dataLayer || [];
      function gtag(){dataLayer.push(arguments);}
      gtag('js', new Date());

      gtag('config', '${GAID}');
  `;
  const googleAnalyticsLink = `https://www.googletagmanager.com/gtag/js?id=${GAID}`;

  return (
    <html
      lang="en"
      suppressHydrationWarning
      className={`${googleSans.variable} ${materialSymbolsOutlined.variable} ${googleSansText.variable} ${googleSansMono.variable} ${materialSymbolsRounded.variable} ${googleSansCode.variable} ${googleSansDisplay.variable}`}
    >
      <head>
        <link rel="icon" href="/favicon.ico" />
        <link
          rel="icon"
          type="image/png"
          sizes="96x96"
          href="/favicon-96x96.png"
        />
        <link rel="icon" type="image/svg+xml" href="/favicon.svg" />
        <link
          rel="apple-touch-icon"
          sizes="180x180"
          href="/apple-touch-icon.png"
        />
        <link rel="shortcut icon" href="/favicon.ico" />

        <ColorSchemeScript />
        <meta charSet="UTF-8" />
        <meta
          name="viewport"
          content="minimum-scale=1, initial-scale=1, width=device-width, user-scalable=no"
        />

        <script
          dangerouslySetInnerHTML={{
            __html: trustedTypesPolicy,
          }}
        />
        {GAID && <script async src={googleAnalyticsLink} />}
        {GAID && (
          <script
            dangerouslySetInnerHTML={{
              __html: googleAnalyticsScript,
            }}
          />
        )}
        {process?.env?.NEXT_PUBLIC_FEEDBACK_PRODUCT_ID && (
          <Script
            type="text/javascript"
            src="https://support.google.com/inapp/api.js"
          />
        )}
        {process?.env?.NEXT_PUBLIC_HATS_API_KEY && (
          <Script
            type="text/javascript"
            src="https://www.gstatic.com/feedback/js/help/prod/service/lazy.min.js"
          />
        )}
      </head>
      <body className="min-h-screen bg-neutrals-950">
        <GlobalContextProvider>
          <div className="flex h-screen w-screen flex-row items-center justify-center">
            {children}
          </div>
        </GlobalContextProvider>
      </body>
    </html>
  );
}

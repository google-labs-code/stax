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

import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";

import { routes } from "./config/routes";

// Define the strict CSP for production

export function middleware(request: NextRequest) {
  // Handle API routes specifically to prevent caching
  if (request.nextUrl.pathname.startsWith("/api/")) {
    const response = NextResponse.next();

    // Set cache control headers for API routes
    response.headers.set(
      "Cache-Control",
      "no-cache, no-store, must-revalidate, private",
    );
    response.headers.set("Pragma", "no-cache");
    response.headers.set("Expires", "0");
    response.headers.set("X-Cache-Status", "disabled");

    return response;
  }

  const nonce = Buffer.from(crypto.randomUUID()).toString("base64");
  const cspHeader = `
    default-src 'self';
    script-src 'self' 'nonce-${nonce}' 'strict-dynamic' https: http:;
    style-src 'self' 'nonce-${nonce}';
    img-src 'self' blob: data:;
    font-src 'self';
    object-src 'none';
    base-uri 'self';
    form-action 'self';
    frame-ancestors 'none';
    upgrade-insecure-requests;
  `;

  const isDev = process?.env?.NODE_ENV === "development";
  // Replace newline characters and spaces
  const contentSecurityPolicyHeaderValue = cspHeader
    .replace(/\s{2,}/g, " ")
    .trim();

  const requestHeaders = new Headers(request.headers);
  requestHeaders.set("x-nonce", nonce);
  if (!isDev) {
    requestHeaders.set(
      "Content-Security-Policy-Report-Only",
      contentSecurityPolicyHeaderValue,
    );
  }

  const response = NextResponse.next({
    request: {
      headers: requestHeaders,
    },
  });
  if (!isDev) {
    response.headers.set(
      "Content-Security-Policy-Report-Only",
      contentSecurityPolicyHeaderValue,
    );
  }

  // --------- Authorization redirects ---------
  const pathname = request.nextUrl.pathname;

  if (pathname === routes.root) {
    // Add authorization code / error to the query params when user is redirected back to app
    let redirectRoute = routes.signin;
    if (request.nextUrl.searchParams.get("code")) {
      redirectRoute += "?code=" + request.nextUrl.searchParams.get("code");
    } else if (request.nextUrl.searchParams.get("error")) {
      redirectRoute += "?error=" + request.nextUrl.searchParams.get("error");
    } else {
      return response;
    }

    return NextResponse.redirect(new URL(redirectRoute, request.url));
  }

  // Default case - apply CSP and continue
  return response;
}

export const config = {
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico|.*\\.png$).*)"],
};

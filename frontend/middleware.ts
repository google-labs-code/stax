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
  const response = NextResponse.next();

  // Set cache control headers for all pages and API routes
  response.headers.set(
    "Cache-Control",
    "no-cache, no-store, must-revalidate, private",
  );
  response.headers.set("Pragma", "no-cache");
  response.headers.set("Expires", "0");
  response.headers.set("X-Cache-Status", "disabled");

  // Optional: only enforce strict CSP when explicitly enabled via env
  if (process.env.ENABLE_STRICT_CSP === "true") {
    const isHttps =
      request.headers.get("x-forwarded-proto") === "https" ||
      request.nextUrl.protocol === "https:";
    const upgradeInsecure = isHttps ? "upgrade-insecure-requests;" : "";
    const cspHeader = `
      default-src 'self';
      script-src 'self' 'unsafe-eval' 'unsafe-inline' https: http:;
      style-src 'self' 'unsafe-inline' https: http:;
      img-src 'self' blob: data: https:;
      font-src 'self' data: https:;
      connect-src 'self' https: http: ws: wss:;
      object-src 'none';
      base-uri 'self';
      form-action 'self';
      ${upgradeInsecure}
    `.replace(/\s{2,}/g, " ").trim();
    response.headers.set("Content-Security-Policy", cspHeader);
  }

  // --------- Authorization redirects ---------
  const pathname = request.nextUrl.pathname;

  if (pathname === routes.root) {
    if (process.env.NEXT_PUBLIC_AUTH_ENABLED !== "true") {
      return NextResponse.redirect(new URL(routes.projects, request.url));
    }
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

  if (pathname === routes.signin && process.env.NEXT_PUBLIC_AUTH_ENABLED !== "true") {
    return NextResponse.redirect(new URL(routes.projects, request.url));
  }

  return response;
}

export const config = {
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico|.*\\.png$).*)"],
};

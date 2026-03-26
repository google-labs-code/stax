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

/** @type {import('next').NextConfig} */
const nextConfig = {
  pageExtensions:
    process.env.NODE_ENV === "production"
      ? ["js", "jsx", "ts", "tsx"].filter((ext) => !ext.includes("dev"))
      : ["js", "jsx", "ts", "tsx"],
  webpack(config, { dev }) {
    if (!dev) {
      config.module.rules.push({
        test: /app\/\(unauthRoutes\)\/dev\//,
        loader: "ignore-loader",
      });
    }

    return config;
  },
  eslint: {
    // Warning: This allows production builds to successfully complete even if
    // your project has ESLint errors.
    ignoreDuringBuilds: true,
  },
  typescript: {
    // !! WARN !!
    // Dangerously allow production builds to successfully complete even if
    // your project has type errors.
    // !! WARN !!
    ignoreBuildErrors: true,
  },
  compiler: {
    removeConsole: process.env.NODE_ENV === "production",
  },
  reactStrictMode: false,
  async headers() {
    return [
      {
        source: "/(.*)", // Match all routes
        headers: [
          {
            key: "X-Frame-Options",
            value: "DENY",
          },
        ],
      },
      {
        source: "/api/(.*)", // Match all API routes
        headers: [
          {
            key: "Cache-Control",
            value: "no-cache, no-store, must-revalidate, private",
          },
          {
            key: "Pragma",
            value: "no-cache",
          },
          {
            key: "Expires",
            value: "0",
          },
          {
            key: "X-Cache-Status",
            value: "disabled",
          },
        ],
      },
    ];
  },
};

module.exports = nextConfig;

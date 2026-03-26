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

import axios, { AxiosRequestConfig } from "axios";
import { NextResponse } from "next/server";

// Force dynamic rendering to prevent caching
export const dynamic = "force-dynamic";
export const revalidate = 0;
export const fetchCache = "force-no-store";
export const runtime = "nodejs";
export const preferredRegion = "auto";

async function handle(request: Request) {
  try {
    // Add timestamp and random value to prevent caching
    const timestamp = Date.now();
    const randomId = Math.random().toString(36).substring(7);

    const relativeEndpoint = request.url.split("/api/")[1];
    const headers: Record<string, string> = {};
    if (request.headers.get("Authorization")) {
      headers["Authorization"] = request.headers.get("Authorization") || "";
    }
    if (request.headers.get("Content-Type")) {
      headers["Content-Type"] = request.headers.get("Content-Type") || "";
    }

    // Add cache-busting headers
    headers["X-Request-Timestamp"] = timestamp.toString();
    headers["X-Request-ID"] = randomId;

    const config: AxiosRequestConfig = {
      method: request.method.toUpperCase(),
      url: relativeEndpoint,
      baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
      headers,
      timeout: 180000, // 3 minutes timeout
    };

    let requestPayload = null;
    try {
      const contentType = request.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        requestPayload = await request.json();
      } else if (
        contentType &&
        contentType.includes("application/x-www-form-urlencoded")
      ) {
        const formData = await request.formData();
        requestPayload = Object.fromEntries(formData);
      } else {
        requestPayload = await request.text();
      }

      config.data = requestPayload;
    } catch {
      //
    }
    const response = await axios.request(config);

    // Create response with cache control headers to prevent caching
    const nextResponse = NextResponse.json(response.data);

    // Set headers to prevent caching at all levels
    nextResponse.headers.set(
      "Cache-Control",
      "no-cache, no-store, must-revalidate, private",
    );
    nextResponse.headers.set("Pragma", "no-cache");
    nextResponse.headers.set("Expires", "0");
    nextResponse.headers.set("X-Cache-Status", "disabled");

    return nextResponse;
  } catch (error: any) {
    let statusCode = 400;
    let errorMessage = "There was a problem with Next.js API route.";

    if (error.response) {
      statusCode = error.response.status;
      errorMessage =
        error.response.data?.error ||
        error.response.data?.message ||
        error.message;
    } else if (error.request) {
      errorMessage = error?.reason;
      statusCode = 503;
    } else {
      errorMessage = error.message || "Request setup failed";
    }

    const errorResponse = NextResponse.json(
      {
        error: errorMessage,
        status: statusCode,
      },
      { status: statusCode },
    );

    // Set cache control headers on error responses as well
    errorResponse.headers.set(
      "Cache-Control",
      "no-cache, no-store, must-revalidate, private",
    );
    errorResponse.headers.set("Pragma", "no-cache");
    errorResponse.headers.set("Expires", "0");
    errorResponse.headers.set("X-Cache-Status", "disabled");

    return errorResponse;
  }
}

export async function GET(request: Request) {
  return handle(request);
}

export async function POST(request: Request) {
  return handle(request);
}

export async function DELETE(request: Request) {
  return handle(request);
}

export async function PUT(request: Request) {
  return handle(request);
}

export async function PATCH(request: Request) {
  return handle(request);
}

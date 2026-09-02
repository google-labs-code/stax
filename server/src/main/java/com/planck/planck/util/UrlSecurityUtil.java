/*
 * Copyright 2026 Google LLC
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

package com.planck.planck.util;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

/** Utility class for validating URLs to prevent SSRF vulnerabilities. */
public final class UrlSecurityUtil {

  private UrlSecurityUtil() {}

  /**
   * Validates that the provided URL is a valid http/https URL and does not target internal hosts or
   * IP addresses.
   *
   * @param urlString the URL string to validate
   * @throws IllegalArgumentException if invalid or targeting internal endpoint
   */
  public static void validateExternalUrl(String urlString) {
    if (urlString == null || urlString.trim().isEmpty()) {
      return;
    }

    URI uri;
    try {
      uri = URI.create(urlString.trim());
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid URL format", e);
    }

    String scheme = uri.getScheme();
    if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
      throw new IllegalArgumentException("URL scheme must be http or https");
    }

    String host = uri.getHost();
    if (host == null || host.trim().isEmpty()) {
      throw new IllegalArgumentException("URL host cannot be empty");
    }

    String lowerHost = host.toLowerCase(Locale.ROOT);
    if (lowerHost.contains("metadata.google.internal")
        || lowerHost.equals("metadata")
        || lowerHost.equals("localhost")
        || lowerHost.endsWith(".localhost")
        || lowerHost.equals("169.254.169.254")) {
      throw new IllegalArgumentException("Access to internal endpoints is forbidden");
    }

    try {
      InetAddress[] addresses = InetAddress.getAllByName(host);
      for (InetAddress addr : addresses) {
        if (addr.isLoopbackAddress()
            || addr.isAnyLocalAddress()
            || addr.isLinkLocalAddress()
            || addr.isSiteLocalAddress()) {
          throw new IllegalArgumentException("Access to internal IP addresses is forbidden");
        }
        byte[] ip = addr.getAddress();
        if (ip.length == 4) {
          int first = ip[0] & 0xFF;
          int second = ip[1] & 0xFF;
          if (first == 169 && second == 254) {
            throw new IllegalArgumentException("Access to link-local addresses is forbidden");
          }
          if (first == 10) {
            throw new IllegalArgumentException("Access to private network is forbidden");
          }
          if (first == 172 && second >= 16 && second <= 31) {
            throw new IllegalArgumentException("Access to private network is forbidden");
          }
          if (first == 192 && second == 168) {
            throw new IllegalArgumentException("Access to private network is forbidden");
          }
          if (first == 127) {
            throw new IllegalArgumentException("Access to loopback address is forbidden");
          }
        }
      }
    } catch (UnknownHostException e) {
      // Host resolution failure - allowed to proceed if domain name is valid
    }
  }
}

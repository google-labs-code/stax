/*
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

package com.planck.planck.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DbResultParserUtils {

  // Private constructor to prevent instantiation of utility class
  private DbResultParserUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Safely converts an object from a database result set to a Long. Handles nulls, existing Long or
   * Number types, and String representations that might be parsable as Long or as Double (e.g.,
   * "123.0").
   *
   * @param obj The object to convert.
   * @return The Long value, or 0L if conversion fails or obj is null.
   */
  public static Long getLongFromResult(Object obj) {
    if (obj == null) {
      return 0L;
    }
    if (obj instanceof Long) {
      return (Long) obj;
    }
    if (obj instanceof Number) { // Catches Integer, Double, BigDecimal, etc.
      return ((Number) obj).longValue();
    }
    if (obj instanceof String) {
      String str = (String) obj;
      if (str.trim().isEmpty()) {
        log.warn("Empty string provided to getLongFromResult, returning 0L.");
        return 0L;
      }
      try {
        // Try parsing as Long directly first (most common and precise for integers)
        return Long.parseLong(str);
      } catch (NumberFormatException e1) {
        // If direct Long parsing fails, try parsing as Double and then converting to long.
        // This handles cases like "123.0" or even scientific notation if it represents an integer.
        try {
          Double doubleValue = Double.parseDouble(str);
          // Add a check if the double value has a fractional part that would be lost
          if (doubleValue % 1 == 0) {
            return doubleValue.longValue();
          } else {
            log.warn(
                "String '{}' parsed as Double has a fractional part ({}). "
                    + "Lossy conversion to Long; consider if this is intended. Returning truncated long.",
                str,
                doubleValue);
            return doubleValue.longValue(); // Or throw an error if precision loss is critical
          }
        } catch (NumberFormatException e2) {
          log.warn(
              "Could not parse string '{}' as Long or as Double to convert to Long. Returning 0L.",
              str,
              e2);
          return 0L;
        }
      }
    }

    log.warn(
        "Could not convert object '{}' of type {} to Long. Returning 0L.",
        obj,
        obj.getClass().getName());
    return 0L;
  }

  /**
   * Safely converts an object from a database result set to a Double. Handles nulls, existing
   * Double or Number types, and String representations.
   *
   * @param obj The object to convert.
   * @return The Double value, or 0.0 if conversion fails or obj is null.
   */
  public static Double getDoubleFromResult(Object obj) {
    if (obj == null) {
      return 0.0;
    }
    if (obj instanceof Double) {
      return (Double) obj;
    }
    if (obj instanceof Number) { // Catches Integer, Long, BigDecimal, etc.
      return ((Number) obj).doubleValue();
    }
    if (obj instanceof String) {
      String str = (String) obj;
      if (str.trim().isEmpty()) {
        log.warn("Empty string provided to getDoubleFromResult, returning 0.0.");
        return 0.0;
      }
      try {
        return Double.parseDouble(str);
      } catch (NumberFormatException e) {
        log.warn("Could not parse string '{}' as Double. Returning 0.0.", str, e);
        return 0.0;
      }
    }

    log.warn(
        "Could not convert object '{}' of type {} to Double. Returning 0.0.",
        obj,
        obj.getClass().getName());
    return 0.0;
  }
}

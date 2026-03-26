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

package com.planck.planck.enums;

import lombok.Getter;

@Getter
public enum AggregationWindow {
  PER_SECOND("per_second"),
  PER_15_SECONDS("per_15_seconds"),
  PER_MINUTE("per_minute"),
  HOURLY("hourly"),
  DAILY("daily"),
  WEEKLY("weekly"),
  MONTHLY("monthly");

  private final String value;

  AggregationWindow(String value) {
    this.value = value;
  }

  public static AggregationWindow fromValue(String text) {
    if (text == null) {
      return null;
    }
    for (AggregationWindow b : AggregationWindow.values()) {
      if (b.value.equalsIgnoreCase(text)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unknown aggregation window: " + text);
  }
}

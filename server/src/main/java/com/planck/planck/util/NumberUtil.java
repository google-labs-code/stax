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

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtil {

  private NumberUtil() {}

  public static BigDecimal handleNullOrReturnDefault(BigDecimal value, BigDecimal defaultValue) {

    return value == null ? defaultValue : value;
  }

  public static Integer handleNullOrReturnDefault(Integer value, Integer defaultValue) {

    return value == null ? defaultValue : value;
  }

  public static Long handleNullOrReturnDefault(Long value, Long defaultValue) {

    return value == null ? defaultValue : value;
  }

  public static Double handleNullOrReturnDefault(Double value, Double defaultValue) {

    return value == null ? defaultValue : value;
  }

  public static double round(double value, int places) {
    if (places < 0) {
      throw new IllegalArgumentException("places must be >= 0");
    }
    var bd = new BigDecimal(Double.toString(value));
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  public static Integer parseInteger(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}

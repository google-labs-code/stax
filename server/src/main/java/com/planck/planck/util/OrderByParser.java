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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrderByParser {
  public static List<OrderBy> parse(String orderBy) {
    if (orderBy == null || orderBy.isBlank()) return List.of();

    return Arrays.stream(orderBy.split(","))
        .map(String::trim)
        .map(OrderByParser::parseSingle)
        .collect(Collectors.toList());
  }

  private static OrderBy parseSingle(String item) {
    String[] parts = item.trim().split("\\s+");
    String field = parts[0];
    if (parts.length == 1) {
      return new OrderBy(field, false);
    }
    boolean desc = parts.length == 1 || parts[1].equalsIgnoreCase("desc");
    return new OrderBy(field, desc);
  }
}

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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilterParser {
  private static final Pattern CONDITION_PATTERN =
      Pattern.compile("(\\w+)\\s*(=|!=|<|<=|>|>=|:)?\\s*\"?([^\"]+)\"?", Pattern.CASE_INSENSITIVE);

  public static List<FilterCondition> parse(String filter) {
    if (filter == null || filter.isBlank()) return List.of();

    return Arrays.stream(filter.split("(?i)\\s+AND\\s+"))
        .map(String::trim)
        .map(FilterParser::parseSingle)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private static FilterCondition parseSingle(String condition) {
    Matcher matcher = CONDITION_PATTERN.matcher(condition);
    if (matcher.matches()) {
      String field = matcher.group(1);
      String operator = matcher.group(2) != null ? matcher.group(2) : "=";
      String value = matcher.group(3);
      return new FilterCondition(field, operator, value);
    } else {
      throw new IllegalArgumentException("Invalid filter condition: " + condition);
    }
  }
}

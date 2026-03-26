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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PromptUtil {

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");
  private static final String VARIABLE_FORMAT =
      "<variable color=\"var(--color-brand)\" value=\"%s\" tooltip=\"%s\"/>";

  public static boolean containsVariableInput(String text) {
    return VARIABLE_PATTERN.matcher(text).find();
  }

  public static String enrichText(String text, Map<String, String> variables) {
    if (text == null || variables == null || variables.isEmpty()) {
      return text;
    }

    Matcher matcher = VARIABLE_PATTERN.matcher(text);
    StringBuilder result = new StringBuilder();

    while (matcher.find()) {
      String key = matcher.group(1);
      String value = variables.get(key);

      if (value != null) {
        matcher.appendReplacement(result, Matcher.quoteReplacement(value));
      }
    }
    matcher.appendTail(result);

    return result.toString();
  }

  public static String enrichTextForResponse(String text, Map<String, String> variables) {
    if (text == null || variables == null || variables.isEmpty()) {
      return text;
    }

    StringBuilder result = new StringBuilder();
    Matcher matcher = VARIABLE_PATTERN.matcher(text);

    while (matcher.find()) {
      String key = matcher.group(1);
      String value = variables.getOrDefault(key, "VARIABLE_NOT_FOUND");
      String replacement = String.format(VARIABLE_FORMAT, value, key);
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);

    return result.toString();
  }
}

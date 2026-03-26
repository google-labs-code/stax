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

import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Util {

  private static final String EMPTY = "";
  private static final String REGEX_EMAIL = "\\.(?=[^@]*@)";
  private static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final int HALF_LENGTH = 5; // Change length as needed
  private static final SecureRandom random = new SecureRandom();
  private static final int SHIFT = 2; // Shift value for encoding/decoding

  private Util() {}

  public static String generateRandomId() {
    StringBuilder sb = new StringBuilder((2 * HALF_LENGTH) + 1);
    for (int i = 0; i < HALF_LENGTH; i++) {
      sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
    }
    sb.append('-');
    for (int i = 0; i < HALF_LENGTH; i++) {
      sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
    }
    return sb.toString();
  }

  public static String normalizeEmail(String email) {
    // Remove dots before '@' and convert to lowercase
    return email.replaceAll(REGEX_EMAIL, EMPTY).toLowerCase();
  }

  // Reversible ID transformation methods
  public static String encodeUserId(String uuid) {
    return transform(uuid, SHIFT);
  }

  private static String transform(String input, int shiftValue) {
    StringBuilder sb = new StringBuilder();
    for (char c : input.toCharArray()) {
      char shiftedChar = (char) (c + shiftValue);
      sb.append(shiftedChar);
    }
    return sb.toString();
  }
}

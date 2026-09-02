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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UrlSecurityUtilTest {

  @Test
  void validateExternalUrl_validHttpsUrl_shouldPass() {
    assertDoesNotThrow(() -> UrlSecurityUtil.validateExternalUrl("https://api.openai.com/v1"));
  }

  @Test
  void validateExternalUrl_metadataUrl_shouldThrow() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            UrlSecurityUtil.validateExternalUrl(
                "http://metadata.google.internal/computeMetadata/v1/"));
  }

  @Test
  void validateExternalUrl_localhostUrl_shouldThrow() {
    assertThrows(
        IllegalArgumentException.class,
        () -> UrlSecurityUtil.validateExternalUrl("http://localhost:8080/v1"));
  }

  @Test
  void validateExternalUrl_loopbackIp_shouldThrow() {
    assertThrows(
        IllegalArgumentException.class,
        () -> UrlSecurityUtil.validateExternalUrl("http://127.0.0.1:8080/v1"));
  }

  @Test
  void validateExternalUrl_metadataIp_shouldThrow() {
    assertThrows(
        IllegalArgumentException.class,
        () -> UrlSecurityUtil.validateExternalUrl("http://169.254.169.254/v1"));
  }

  @Test
  void validateExternalUrl_invalidScheme_shouldThrow() {
    assertThrows(
        IllegalArgumentException.class,
        () -> UrlSecurityUtil.validateExternalUrl("ftp://api.example.com"));
  }
}

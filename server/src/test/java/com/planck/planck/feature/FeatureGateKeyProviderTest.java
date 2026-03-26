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

package com.planck.planck.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.planck.planck.feature.config.FeatureGateKeyProvider;
import com.planck.planck.feature.config.FeatureGateProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeatureGateKeyProviderTest {

  @Mock private FeatureGateProperties mockProperties;

  private FeatureGateKeyProvider keyProvider;

  @BeforeEach
  void setUp() {
    when(mockProperties.getAppName()).thenReturn("stax");
    when(mockProperties.getAppEnv()).thenReturn("dev");

    keyProvider = new FeatureGateKeyProvider(mockProperties);
  }

  @Test
  void shouldReturnCorrectRedisHashKey() {
    String expectedKey = "feature_gates:stax:dev";
    assertEquals(expectedKey, keyProvider.getRedisHashKey());
  }

  @Test
  void shouldReturnCorrectMetadataRedisKey() {
    String expectedKey = "feature_gates:stax:dev:metadata";
    assertEquals(expectedKey, keyProvider.getMetadataRedisKey());
  }

  @Test
  void shouldReturnCorrectExpectedFileName() {
    String expectedFileName = "stax-features-dev.json";
    assertEquals(expectedFileName, keyProvider.getExpectedFileName());
  }
}

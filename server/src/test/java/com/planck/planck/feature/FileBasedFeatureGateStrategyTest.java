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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.planck.planck.feature.dto.Metadata;
import com.planck.planck.feature.service.FileBasedFeatureGateStrategy;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
class FileBasedFeatureGateStrategyTest {

  @Mock private ResourceLoader resourceLoader;

  @Mock private Resource mockResource;

  private FileBasedFeatureGateStrategy strategy;

  private final String VALID_JSON =
      """
            {
              "version": "1.0.0-default",
              "metadata": {
                "commit_hash": "default-commit",
                "author": "system",
                "deploy_time_utc": "1970-01-01T00:00:00Z"
              },
              "features": [
                { "key": "feature-enabled", "enabled": true },
                { "key": "feature-disabled", "enabled": false }
              ]
            }
            """;

  @Test
  void whenFileLoadsSuccessfully_thenCacheAndMetadataArePopulated() throws IOException {

    Resource validResource = new ByteArrayResource(VALID_JSON.getBytes());
    when(resourceLoader.getResource("classpath:default-feature-flags.json"))
        .thenReturn(validResource);

    strategy = new FileBasedFeatureGateStrategy(resourceLoader);
    strategy.initialize();

    assertTrue(strategy.isFeatureEnabled("feature-enabled"));
    assertFalse(strategy.isFeatureEnabled("feature-disabled"));
    assertFalse(strategy.isFeatureEnabled("non-existent-feature"));

    Optional<Metadata> metadataOpt = strategy.getCurrentMetadata();
    assertTrue(metadataOpt.isPresent());
    assertEquals("default-commit", metadataOpt.get().commitHash());
  }

  @Test
  void whenFileIsNotFound_thenCacheAndMetadataAreEmpty() throws IOException {

    when(resourceLoader.getResource("classpath:default-feature-flags.json"))
        .thenReturn(mockResource);
    when(mockResource.getInputStream()).thenThrow(new IOException("File not found"));

    strategy = new FileBasedFeatureGateStrategy(resourceLoader);

    assertFalse(strategy.isFeatureEnabled("any-feature"));
    assertTrue(strategy.getCurrentMetadata().isEmpty());
  }

  @Test
  void whenJsonIsInvalid_thenCacheAndMetadataAreEmpty() throws IOException {

    String invalidJson = "{ \"features\": [ {\"key\":\"test\" ";
    Resource invalidResource = new ByteArrayResource(invalidJson.getBytes());
    when(resourceLoader.getResource("classpath:default-feature-flags.json"))
        .thenReturn(invalidResource);

    strategy = new FileBasedFeatureGateStrategy(resourceLoader);

    assertFalse(strategy.isFeatureEnabled("test"));
    assertTrue(strategy.getCurrentMetadata().isEmpty());
  }

  @Test
  void forceSynchronize_shouldLogWarningAndDoNothing() throws IOException {

    Resource validResource = new ByteArrayResource(VALID_JSON.getBytes());
    when(resourceLoader.getResource("classpath:default-feature-flags.json"))
        .thenReturn(validResource);
    strategy = new FileBasedFeatureGateStrategy(resourceLoader);

    assertDoesNotThrow(() -> strategy.forceSynchronize());
    assertTrue(strategy.isFeatureEnabled("feature-enabled"));
  }
}

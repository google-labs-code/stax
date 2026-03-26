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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.planck.planck.feature.config.FeatureGateKeyProvider;
import com.planck.planck.feature.service.DirectRedisFeatureGateStrategy;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/** Unit tests for the DirectRedisFeatureGateStrategy. */
@ExtendWith(MockitoExtension.class)
class DirectRedisFeatureGateStrategyTest {

  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ResourceLoader resourceLoader;
  @Mock private FeatureGateKeyProvider keyProvider;
  @Mock private HashOperations<String, Object, Object> hashOperations;
  @Mock private ValueOperations<String, String> valueOperations;

  private DirectRedisFeatureGateStrategy strategy;

  private final String DEFAULT_JSON =
      """
            {
              "version": "1.0.0-default",
              "metadata": { "commit_hash": "default-commit" },
              "features": [
                { "key": "fallback-feature", "enabled": true },
                { "key": "redis-feature", "enabled": false }
              ]
            }
            """;

  @BeforeEach
  void setUp() throws IOException {

    when(keyProvider.getRedisHashKey()).thenReturn("test-hash-key");
    when(keyProvider.getMetadataRedisKey()).thenReturn("test-metadata-key");

    lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    when(resourceLoader.getResource("classpath:default-feature-flags.json"))
        .thenReturn(new ByteArrayResource(DEFAULT_JSON.getBytes()));

    strategy = new DirectRedisFeatureGateStrategy(redisTemplate, resourceLoader, keyProvider);
    strategy.initialize();
  }

  @Test
  void whenFeatureExistsInRedis_shouldReturnValueFromRedis() {

    when(hashOperations.get("test-hash-key", "redis-feature")).thenReturn("true");

    assertTrue(strategy.isFeatureEnabled("redis-feature"));
  }

  @Test
  void whenFeatureNotInRedis_shouldFallbackToDefaultFileCache() {

    when(hashOperations.get("test-hash-key", "fallback-feature")).thenReturn(null);

    assertTrue(strategy.isFeatureEnabled("fallback-feature"));
  }

  @Test
  void whenRedisFails_shouldFallbackToDefaultFileCache() {

    when(hashOperations.get(anyString(), anyString()))
        .thenThrow(new RuntimeException("Redis connection failed"));

    assertTrue(strategy.isFeatureEnabled("fallback-feature"));
    assertFalse(strategy.isFeatureEnabled("non-existent-feature"));
  }

  @Test
  void whenMetadataExistsInRedis_shouldReturnMetadata() {

    String metadataJson = "{\"commit_hash\":\"redis-commit\"}";
    when(valueOperations.get("test-metadata-key")).thenReturn(metadataJson);

    assertTrue(strategy.getCurrentMetadata().isPresent());
    assertEquals("redis-commit", strategy.getCurrentMetadata().get().commitHash());
  }

  @Test
  void whenMetadataNotInRedis_shouldReturnEmpty() {

    when(valueOperations.get("test-metadata-key")).thenReturn(null);

    assertTrue(strategy.getCurrentMetadata().isEmpty());
  }

  @Test
  void forceSynchronize_shouldDoNothing() {

    assertDoesNotThrow(() -> strategy.forceSynchronize());
  }
}

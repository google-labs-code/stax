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
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.feature.config.FeatureGateKeyProvider;
import com.planck.planck.feature.service.CachedFeatureGateStrategy;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class CachedFeatureGateStrategyTest {

  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ResourceLoader resourceLoader;
  @Mock private FeatureGateKeyProvider keyProvider;
  @Mock private HashOperations<String, Object, Object> hashOperations;
  @Mock private ValueOperations<String, String> valueOperations;
  @Mock private Resource mockResource;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private CachedFeatureGateStrategy strategy;

  private final String VALID_DEFAULT_JSON =
      """
        {
          "version": "1.0.0-default",
          "metadata": { "commit_hash": "default-commit", "author": "system", "deploy_time_utc": "1970-01-01T00:00:00Z" },
          "features": [
            { "key": "default-feature", "enabled": true }
          ]
        }
        """;

  @BeforeEach
  void setUp() {
    when(keyProvider.getRedisHashKey()).thenReturn("test-hash-key");
    when(keyProvider.getMetadataRedisKey()).thenReturn("test-metadata-key");
    when(redisTemplate.opsForHash()).thenReturn(hashOperations);

    lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    strategy = new CachedFeatureGateStrategy(redisTemplate, resourceLoader, keyProvider);
  }

  @Test
  void whenInitialize_andRedisHasData_thenLoadsFromRedis() throws Exception {

    when(hashOperations.entries("test-hash-key")).thenReturn(Map.of("feature-a", "true"));
    String metadataJson = "{\"commit_hash\":\"redis-commit\",\"author\":\"redis-author\"}";
    when(valueOperations.get("test-metadata-key")).thenReturn(metadataJson);

    strategy.initialize();

    assertTrue(strategy.isFeatureEnabled("feature-a"));
    assertEquals("redis-commit", strategy.getCurrentMetadata().get().commitHash());
    verify(resourceLoader, never()).getResource(any());
  }

  @Test
  void whenInitialize_andRedisFails_thenLoadsFromDefaultFile() throws IOException {

    when(hashOperations.entries("test-hash-key"))
        .thenThrow(new RuntimeException("Redis connection failed"));
    when(resourceLoader.getResource("classpath:default-feature-flags.json"))
        .thenReturn(new ByteArrayResource(VALID_DEFAULT_JSON.getBytes()));

    strategy.initialize();

    assertTrue(strategy.isFeatureEnabled("default-feature"));
    assertEquals("default-commit", strategy.getCurrentMetadata().get().commitHash());
  }

  @Test
  void whenInitialize_andBothRedisAndFileFail_thenCacheIsEmpty() throws IOException {

    when(hashOperations.entries("test-hash-key")).thenThrow(new RuntimeException("Redis down"));
    when(resourceLoader.getResource("classpath:default-feature-flags.json"))
        .thenReturn(mockResource);
    when(mockResource.getInputStream()).thenThrow(new IOException("File not found"));

    strategy.initialize();

    assertFalse(strategy.isFeatureEnabled("any-feature"));
    assertTrue(strategy.getCurrentMetadata().isEmpty());
  }

  @Test
  void forceSynchronize_whenRedisFails_retainsStaleCache() throws IOException {

    when(hashOperations.entries("test-hash-key")).thenReturn(Map.of("initial-feature", "true"));
    strategy.initialize();
    assertTrue(strategy.isFeatureEnabled("initial-feature"));

    when(hashOperations.entries("test-hash-key"))
        .thenThrow(new RuntimeException("Redis connection failed"));

    assertDoesNotThrow(() -> strategy.forceSynchronize());

    assertTrue(strategy.isFeatureEnabled("initial-feature"));
  }
}

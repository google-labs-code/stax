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

package com.planck.planck.feature.service;

import com.planck.planck.feature.config.FeatureGateKeyProvider;
import com.planck.planck.feature.dto.FeatureConfig;
import com.planck.planck.feature.dto.Metadata;
import com.planck.planck.util.ObjectMapperUtil;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;

public class DirectRedisFeatureGateStrategy implements FeatureGateStrategy {
  private static final Logger log = LoggerFactory.getLogger(DirectRedisFeatureGateStrategy.class);

  private final StringRedisTemplate redisTemplate;
  private final String redisHashKey;
  private final String metadataRedisKey;
  private final ConcurrentMap<String, Boolean> defaultCache = new ConcurrentHashMap<>();

  public DirectRedisFeatureGateStrategy(
      StringRedisTemplate redisTemplate,
      ResourceLoader resourceLoader,
      FeatureGateKeyProvider keyProvider) {
    this.redisTemplate = redisTemplate;
    this.redisHashKey = keyProvider.getRedisHashKey();
    this.metadataRedisKey = keyProvider.getMetadataRedisKey();
    loadDefaultFile(resourceLoader);
  }

  @Override
  public void initialize() {
    log.info("Initializing DirectRedisFeatureGateStrategy...");
  }

  @Override
  public boolean isFeatureEnabled(String featureKey) {
    try {
      Object valueFromRedis = redisTemplate.opsForHash().get(redisHashKey, featureKey);
      if (valueFromRedis != null) {
        return Boolean.parseBoolean(String.valueOf(valueFromRedis));
      }
    } catch (Exception e) {
      log.error("Failed to connect to Redis. Falling back to default cache.", e);
    }
    return defaultCache.getOrDefault(featureKey, false);
  }

  @Override
  public Optional<Metadata> getCurrentMetadata() {
    try {
      String metadataJson = redisTemplate.opsForValue().get(metadataRedisKey);
      if (metadataJson != null && !metadataJson.isEmpty()) {
        return Optional.of(
            ObjectMapperUtil.getObectMapperInstance().readValue(metadataJson, Metadata.class));
      }
    } catch (Exception e) {
      log.error("Failed to get metadata from Redis.", e);
    }
    return Optional.empty();
  }

  @Override
  public void forceSynchronize() {
    log.debug("forceSynchronize called, but no action is needed for DirectRedisStrategy.");
  }

  @Override
  public Map<String, Boolean> getFeatures() {
    try {
      Map<Object, Object> redisData = redisTemplate.opsForHash().entries(redisHashKey);
      if (!redisData.isEmpty()) {
        return redisData.entrySet().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    entry -> String.valueOf(entry.getKey()),
                    entry -> Boolean.parseBoolean(String.valueOf(entry.getValue()))));
      }
    } catch (Exception e) {
      log.error("Failed to get all features from Redis. Falling back to default cache.", e);
    }
    return Collections.unmodifiableMap(defaultCache);
  }

  private void loadDefaultFile(ResourceLoader resourceLoader) {
    try {
      Resource resource = resourceLoader.getResource("classpath:default-feature-flags.json");
      InputStream inputStream = resource.getInputStream();
      FeatureConfig config =
          ObjectMapperUtil.getObectMapperInstance().readValue(inputStream, FeatureConfig.class);
      config.features().forEach(feature -> defaultCache.put(feature.key(), feature.enabled()));
      log.info("Loaded {} default feature gates for fallback.", defaultCache.size());
    } catch (Exception e) {
      log.error("CRITICAL: Could not load default-feature-flags.json for fallback.", e);
    }
  }
}

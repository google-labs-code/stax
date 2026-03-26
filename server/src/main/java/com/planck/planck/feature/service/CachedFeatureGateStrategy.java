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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;

public class CachedFeatureGateStrategy implements FeatureGateStrategy {

  private static final Logger log = LoggerFactory.getLogger(CachedFeatureGateStrategy.class);
  private static final long REDIS_SYNC_INTERVAL_MS = 60_000;

  private final StringRedisTemplate redisTemplate;
  private final String redisHashKey;
  private final ResourceLoader resourceLoader;

  private final ConcurrentMap<String, Boolean> localCache = new ConcurrentHashMap<>();

  private final String metadataRedisKey;
  private volatile Metadata currentMetadata;
  private volatile long lastRedisSyncTime = 0;

  public CachedFeatureGateStrategy(
      StringRedisTemplate redisTemplate,
      ResourceLoader resourceLoader,
      FeatureGateKeyProvider keyProvider) {
    this.redisTemplate = redisTemplate;
    this.redisHashKey = keyProvider.getRedisHashKey();
    this.metadataRedisKey = keyProvider.getMetadataRedisKey();
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void initialize() {
    log.info("Performing initial load of feature gates...");
    try {
      synchronizeFromRedis();
    } catch (Exception e) {
      log.warn("Initial sync with Redis failed. Falling back to default feature file.", e);
      loadFromDefaultFile();
    }

    if (localCache.isEmpty()) {
      log.warn(
          "Local cache is empty after initial load. All features will be disabled by default.");
    }
  }

  @Override
  public Map<String, Boolean> getFeatures() {
    if (System.currentTimeMillis() - lastRedisSyncTime > REDIS_SYNC_INTERVAL_MS) {
      forceSynchronize();
    }
    return Collections.unmodifiableMap(localCache);
  }

  @Override
  public boolean isFeatureEnabled(String featureKey) {
    if (System.currentTimeMillis() - lastRedisSyncTime > REDIS_SYNC_INTERVAL_MS) {
      forceSynchronize();
    }
    return localCache.getOrDefault(featureKey, false);
  }

  @Override
  public Optional<Metadata> getCurrentMetadata() {
    return Optional.ofNullable(currentMetadata);
  }

  @Override
  public synchronized void forceSynchronize() {
    log.info("Forcing synchronization from Redis due to external event.");
    try {
      synchronizeFromRedis();
    } catch (Exception e) {
      log.error("Forced synchronization from Redis failed. Will continue using stale cache.", e);
    }
  }

  private synchronized void synchronizeFromRedis() throws Exception {
    log.debug("Attempting to synchronize feature gates from Redis...");
    try {
      Map<Object, Object> redisData = redisTemplate.opsForHash().entries(redisHashKey);

      if (!redisData.isEmpty()) {
        ConcurrentMap<String, Boolean> newCache = new ConcurrentHashMap<>();
        redisData.forEach(
            (key, value) ->
                newCache.put(String.valueOf(key), Boolean.parseBoolean(String.valueOf(value))));

        this.localCache.clear();
        this.localCache.putAll(newCache);
        log.info("Successfully synchronized {} feature gates from Redis.", newCache.size());
      } else {
        log.warn(
            "Redis hash '{}' is empty or does not exist. The local cache will not be updated.",
            redisHashKey);
      }

      String metadataJson = redisTemplate.opsForValue().get(metadataRedisKey);
      if (metadataJson != null && !metadataJson.isEmpty()) {
        this.currentMetadata =
            ObjectMapperUtil.getObectMapperInstance().readValue(metadataJson, Metadata.class);
        log.info(
            "Successfully synchronized metadata from commit: {}",
            this.currentMetadata.commitHash());
      } else {
        this.currentMetadata = null;
      }

      this.lastRedisSyncTime = System.currentTimeMillis();
    } catch (Exception e) {
      log.error("Failed to sync with Redis. Stale feature gate data will be used.", e);
      throw e;
    }
  }

  private void loadFromDefaultFile() {
    log.info("Loading feature gates from default-feature-flags.json");
    try {
      Resource resource = resourceLoader.getResource("classpath:default-feature-flags.json");
      InputStream inputStream = resource.getInputStream();
      FeatureConfig config =
          ObjectMapperUtil.getObectMapperInstance().readValue(inputStream, FeatureConfig.class);

      ConcurrentMap<String, Boolean> newCache = new ConcurrentHashMap<>();
      config.features().forEach(feature -> newCache.put(feature.key(), feature.enabled()));

      this.localCache.clear();
      this.localCache.putAll(newCache);
      log.info("Successfully loaded {} feature gates from the default file.", newCache.size());

      this.currentMetadata = config.metadata();
      if (this.currentMetadata != null) {
        log.info(
            "Loaded metadata from default file (commit: {})", this.currentMetadata.commitHash());
      }

    } catch (Exception e) {
      log.error(
          "CRITICAL: Could not load feature gates from default-feature-flags.json. The cache will be empty.",
          e);
    }
  }
}

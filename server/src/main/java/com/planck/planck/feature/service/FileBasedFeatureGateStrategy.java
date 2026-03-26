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

public class FileBasedFeatureGateStrategy implements FeatureGateStrategy {
  private static final Logger log = LoggerFactory.getLogger(FileBasedFeatureGateStrategy.class);

  private final ConcurrentMap<String, Boolean> localCache = new ConcurrentHashMap<>();
  private volatile Metadata currentMetadata;

  public FileBasedFeatureGateStrategy(ResourceLoader resourceLoader) {
    loadDefaultFile(resourceLoader);
  }

  @Override
  public void initialize() {
    log.info("Initializing FileBasedFeatureGateStrategy (Redis is disabled).");
  }

  @Override
  public boolean isFeatureEnabled(String featureKey) {
    return localCache.getOrDefault(featureKey, false);
  }

  @Override
  public Optional<Metadata> getCurrentMetadata() {
    return Optional.ofNullable(currentMetadata);
  }

  @Override
  public void forceSynchronize() {
    // No-op: This strategy cannot be synchronized from an external source.
    log.warn("forceSynchronize called, but no action is taken. Redis is disabled.");
  }

  @Override
  public Map<String, Boolean> getFeatures() {
    return Collections.unmodifiableMap(localCache);
  }

  private void loadDefaultFile(ResourceLoader resourceLoader) {
    log.info("Loading feature gates from default-feature-flags.json");
    try {
      Resource resource = resourceLoader.getResource("classpath:default-feature-flags.json");
      InputStream inputStream = resource.getInputStream();
      FeatureConfig config =
          ObjectMapperUtil.getObectMapperInstance().readValue(inputStream, FeatureConfig.class);

      config.features().forEach(feature -> localCache.put(feature.key(), feature.enabled()));
      this.currentMetadata = config.metadata();

      log.info(
          "Successfully loaded {} feature gates and metadata from the default file.",
          localCache.size());
    } catch (Exception e) {
      log.error("CRITICAL: Could not load feature gates from default-feature-flags.json.", e);
    }
  }
}

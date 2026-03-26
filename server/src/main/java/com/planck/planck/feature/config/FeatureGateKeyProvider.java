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

package com.planck.planck.feature.config;

import org.springframework.stereotype.Component;

@Component
public class FeatureGateKeyProvider {

  private final String redisHashKey;
  private final String metadataRedisKey;
  private final String expectedFileName;

  public FeatureGateKeyProvider(FeatureGateProperties properties) {
    this.redisHashKey =
        String.format("feature_gates:%s:%s", properties.getAppName(), properties.getAppEnv());
    this.metadataRedisKey =
        String.format(
            "feature_gates:%s:%s:metadata", properties.getAppName(), properties.getAppEnv());
    this.expectedFileName =
        String.format("%s-features-%s.json", properties.getAppName(), properties.getAppEnv());
  }

  public String getRedisHashKey() {
    return redisHashKey;
  }

  public String getMetadataRedisKey() {
    return metadataRedisKey;
  }

  public String getExpectedFileName() {
    return expectedFileName;
  }
}

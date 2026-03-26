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

import com.planck.planck.feature.service.CachedFeatureGateStrategy;
import com.planck.planck.feature.service.DirectRedisFeatureGateStrategy;
import com.planck.planck.feature.service.FeatureGateStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ConditionalOnProperty(
    name = "feature-gate.redis-enabled",
    havingValue = "true",
    matchIfMissing = false)
public class RedisFeatureGateConfig {

  @Bean
  @Primary
  @ConditionalOnProperty(
      name = "feature-gate.use-local-cache",
      havingValue = "true",
      matchIfMissing = true)
  public FeatureGateStrategy cachedStrategy(
      StringRedisTemplate redisTemplate,
      ResourceLoader resourceLoader,
      FeatureGateKeyProvider keyProvider) {
    return new CachedFeatureGateStrategy(redisTemplate, resourceLoader, keyProvider);
  }

  @Bean
  @ConditionalOnProperty(name = "feature-gate.use-local-cache", havingValue = "false")
  public FeatureGateStrategy directRedisStrategy(
      StringRedisTemplate redisTemplate,
      ResourceLoader resourceLoader,
      FeatureGateKeyProvider keyProvider) {
    return new DirectRedisFeatureGateStrategy(redisTemplate, resourceLoader, keyProvider);
  }
}

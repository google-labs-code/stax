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

import com.planck.planck.feature.dto.Metadata;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class FeatureGateService {

  private final FeatureGateStrategy activeStrategy;

  public FeatureGateService(FeatureGateStrategy activeStrategy) {
    this.activeStrategy = activeStrategy;
  }

  @PostConstruct
  public void initialLoad() {
    activeStrategy.initialize();
  }

  public boolean isFeatureEnabled(String featureKey) {
    return activeStrategy.isFeatureEnabled(featureKey);
  }

  public Optional<Metadata> getCurrentMetadata() {
    return activeStrategy.getCurrentMetadata();
  }

  public Map<String, Boolean> getFeatures() {
    return activeStrategy.getFeatures();
  }

  public void forceSynchronize() {
    activeStrategy.forceSynchronize();
  }
}

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

package com.planck.planck.feature.config.aspect;

import com.planck.planck.feature.dto.Metadata;
import com.planck.planck.feature.exception.FeatureDisabledException;
import com.planck.planck.feature.service.FeatureGateService;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class FeatureGateAspect {

  private static final Logger log = LoggerFactory.getLogger(FeatureGateAspect.class);

  private final FeatureGateService featureGateService;

  public FeatureGateAspect(FeatureGateService featureGateService) {
    this.featureGateService = featureGateService;
  }

  // methods interceptor
  @Around("@annotation(featureEnabled)")
  public Object checkMethodAnnotation(ProceedingJoinPoint joinPoint, FeatureEnabled featureEnabled)
      throws Throwable {
    return performCheck(joinPoint, featureEnabled.value());
  }

  // class interceptor
  @Around(
      "@within(featureEnabled) && !@annotation(com.planck.planck.feature.config.aspect.FeatureEnabled)")
  public Object checkClassAnnotation(ProceedingJoinPoint joinPoint, FeatureEnabled featureEnabled)
      throws Throwable {
    return performCheck(joinPoint, featureEnabled.value());
  }

  private Object performCheck(ProceedingJoinPoint joinPoint, String featureKey) throws Throwable {
    boolean isEnabled = featureGateService.isFeatureEnabled(featureKey);

    Optional<Metadata> metadataOpt = featureGateService.getCurrentMetadata();
    String commitHash = metadataOpt.map(Metadata::commitHash).orElse("N/A");
    log.info(
        "Feature check for key '{}' returning '{}'. Configuration from commit: {}",
        featureKey,
        isEnabled,
        commitHash);

    if (isEnabled) {
      return joinPoint.proceed();
    } else {
      log.warn("Access denied for feature: {}. Endpoint is disabled.", featureKey);
      throw new FeatureDisabledException("Feature '" + featureKey + "' is currently disabled.");
    }
  }
}

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

package com.planck.planck.feature.controller;

import com.planck.planck.feature.dto.FeatureStateDTO;
import com.planck.planck.feature.service.FeatureGateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/feature-gate")
@PreAuthorize("@membershipAuthorizer.isMemberOf('TODO_GROUP_ID')")
public class FeatureGateController {

  private final FeatureGateService featureGateService;

  public FeatureGateController(FeatureGateService featureGateService) {
    this.featureGateService = featureGateService;
  }

  @GetMapping
  public ResponseEntity<FeatureStateDTO> getFeatureGateStatus() {
    var features = featureGateService.getFeatures();
    var metadata = featureGateService.getCurrentMetadata();
    var response = new FeatureStateDTO(metadata, features);
    return ResponseEntity.ok(response);
  }
}

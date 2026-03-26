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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.planck.planck.base.IntegrationTestBase;
import com.planck.planck.feature.config.aspect.FeatureEnabled;
import com.planck.planck.feature.service.FeatureGateService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class FeatureGateAspectTestIT extends IntegrationTestBase {
  @MockBean private FeatureGateService featureGateService;

  @TestConfiguration
  static class TestControllerConfiguration {
    @RestController
    static class TestController {
      @GetMapping("/test-enabled")
      @FeatureEnabled("feature.enabled")
      public ResponseEntity<String> enabledEndpoint() {
        return ResponseEntity.ok("Success");
      }

      @GetMapping("/test-disabled")
      @FeatureEnabled("feature.disabled")
      public ResponseEntity<String> disabledEndpoint() {
        return ResponseEntity.ok("Should not be called");
      }
    }
  }

  @Test
  void whenFeatureIsEnabled_thenEndpointIsAccessible() throws Exception {

    when(featureGateService.isFeatureEnabled("feature.enabled")).thenReturn(true);

    mockMvc.perform(get("/test-enabled")).andExpect(status().isOk());
  }

  @Test
  void whenFeatureIsDisabled_thenEndpointReturnsNotFound() throws Exception {

    when(featureGateService.isFeatureEnabled("feature.disabled")).thenReturn(false);

    mockMvc.perform(get("/test-disabled")).andExpect(status().isNotFound());
  }
}

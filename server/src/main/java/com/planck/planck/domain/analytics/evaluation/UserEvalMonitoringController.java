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

package com.planck.planck.domain.analytics.evaluation;

import com.planck.planck.domain.analytics.evaluation.dto.UserEvalChartEntryDTO;
import com.planck.planck.domain.analytics.evaluation.dto.UserEvalMonitoringParams;
import com.planck.planck.entitities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Evaluation Monitoring", description = "API for managing evaluation monitoring")
@RequestMapping("/analytics/user-eval-monitoring")
public class UserEvalMonitoringController {

  @Autowired private UserEvalMonitoringService userEvalMonitoringService;

  @GetMapping()
  @Operation(
      summary =
          "Retrieve evaluation monitoring based on provided filters, including cost and variables",
      description =
          "Aggregated data by scrorer, with data points based on category and monitoring metrics.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, UserEvalChartEntryDTO>> getAnalyticsMonitoringData(
      @Valid UserEvalMonitoringParams params, @AuthenticationPrincipal User user) {
    Map<String, UserEvalChartEntryDTO> response =
        userEvalMonitoringService.getAnalyticsMonitoringData(user, params);
    return ResponseEntity.ok(response);
  }
}

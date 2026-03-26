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

package com.planck.planck.domain.analytics.inference;

import com.planck.planck.domain.analytics.inference.dto.TimeSeriesAnalyticsDTO;
import com.planck.planck.domain.analytics.inference.dto.TimeSeriesAnalyticsResponseDTO;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.AggregationWindow;
import com.planck.planck.enums.ModelProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.temporal.ChronoUnit; // Import ChronoUnit
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/analytics/inference-monitoring")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Inference Monitoring Analytics",
    description = "APIs for aggregated inference monitoring data for the authenticated user.")
@SecurityRequirement(name = "bearerAuth")
public class InferenceAnalyticsController {

  private final InferenceAnalyticsService inferenceAnalyticsService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get Authenticated User's Time-Series Inference Monitoring Analytics",
      description =
          "Retrieves summarized analytics data for the **currently authenticated user's** inference monitoring records, "
              + "grouped into time windows. Timestamps should be in RFC 3339 format (e.g., yyyy-MM-ddTHH:mm:ssZ). "
              + "If start_time and end_time are omitted, defaults to the last 15 days (end_time = now, start_time = end_time - 15 days). "
              + "If only start_time is omitted, start_time = end_time - 15 days. "
              + "If only end_time is omitted, end_time = now. "
              + "If end_time is in the future, it's adjusted to the current time. "
              + "If timestamps are provided but no window, the window is calculated dynamically. "
              + "An explicit aggregation window can also be specified.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved time-series analytics data",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array =
                        @ArraySchema(
                            schema = @Schema(implementation = TimeSeriesAnalyticsDTO.class))))
      })
  public ResponseEntity<TimeSeriesAnalyticsResponseDTO> getTimeSeriesAnalytics(
      @Parameter(hidden = true) @AuthenticationPrincipal User user,
      @Parameter(description = "Filter by specific Model", example = "model-12345")
          @RequestParam(required = false)
          String modelId,
      @Parameter(description = "Filter by specific Project ID", example = "proj-fghij")
          @RequestParam(required = false)
          String projectId,
      @Parameter(description = "Filter by Model Provider", example = "OPENAI")
          @RequestParam(required = false)
          ModelProvider modelProvider,
      @Parameter(
              description =
                  "Filter by start timestamp (inclusive). Format: RFC 3339 (e.g., yyyy-MM-ddTHH:mm:ssZ). Defaults to 15 days before end_time if omitted.",
              example = "2025-01-01T00:00:00Z")
          @RequestParam(name = "start_time", required = false)
          Instant startTime,
      @Parameter(
              description =
                  "Filter by end timestamp (inclusive). Format: RFC 3339 (e.g., yyyy-MM-ddTHH:mm:ssZ). Defaults to current time if omitted.",
              example = "2025-01-30T23:59:59Z")
          @RequestParam(name = "end_time", required = false)
          Instant endTime,
      @Parameter(
              description =
                  "Specify the time window for aggregation. If omitted, calculated dynamically or defaults to daily.",
              example = "daily",
              schema =
                  @Schema(
                      type = "string",
                      allowableValues = {
                        "per_second",
                        "per_15_seconds",
                        "per_minute",
                        "hourly",
                        "daily",
                        "weekly",
                        "monthly"
                      }))
          @RequestParam(required = false)
          String aggregateWindow,
      @Parameter(
              description =
                  "Optionally filter the analytics by a list of tagId. If provided, only chat turns matching all of the provided tag will be retrieved",
              required = false)
          @RequestParam(required = false)
          List<String> tagIds) {

    log.debug(
        "Received request with raw startTime: {}, raw endTime: {}, window: {}",
        startTime,
        endTime,
        aggregateWindow);

    // --- Initialize effective timestamps ---
    Instant effectiveStartTime = startTime;
    Instant effectiveEndTime = endTime;
    Instant now = Instant.now();

    // --- Apply default timestamp logic ---
    if (effectiveStartTime == null && effectiveEndTime == null) {
      // Both are null, default to last 15 days from now
      effectiveEndTime = now;
      effectiveStartTime = effectiveEndTime.minus(15, ChronoUnit.DAYS);
      log.info(
          "Both start_time and end_time are null. Defaulting to: start_time={}, end_time={}",
          effectiveStartTime,
          effectiveEndTime);
    } else if (effectiveStartTime == null) {
      // startTime is null, endTime is provided. Default startTime to 15 days before endTime.
      // Note: effectiveEndTime will be validated/adjusted later if it's in the future.
      effectiveStartTime = effectiveEndTime.minus(15, ChronoUnit.DAYS);
      log.info(
          "start_time is null. Defaulting start_time to 15 days before provided end_time ({}): {}",
          effectiveEndTime,
          effectiveStartTime);
    } else if (effectiveEndTime == null) {
      // endTime is null, startTime is provided. Default endTime to now.
      effectiveEndTime = now;
      log.info("end_time is null. Defaulting end_time to current time: {}", effectiveEndTime);
    }

    // --- Input Validation and Parsing for AggregationWindow ---
    AggregationWindow windowEnum = null;
    if (aggregateWindow != null && !aggregateWindow.trim().isEmpty()) {
      try {
        windowEnum = AggregationWindow.fromValue(aggregateWindow);
      } catch (IllegalArgumentException e) {
        log.warn("Invalid aggregation window provided: {}", aggregateWindow, e);
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Invalid aggregation_window value: " + aggregateWindow);
      }
    }

    // --- Timestamp Validation ---

    // 1. If effectiveEndTime (whether provided or defaulted) is in the future, adjust to current
    // time
    // This check is important to run *after* defaults that might set endTime to 'now',
    // and also for user-provided future endTimes.
    if (effectiveEndTime.isAfter(now)) {
      log.info(
          "Effective end_time ({}) is in the future. Adjusting to current time ({}).",
          effectiveEndTime,
          now);
      effectiveEndTime = now;
      // If startTime was calculated based on a future endTime, it might now be invalid w.r.t the
      // new endTime.
      // Recalculate startTime if it was defaulted based on an original (future) endTime and
      // startTime was originally null.
      if (startTime == null
          && endTime != null) { // Only if startTime was defaulted based on a user-provided (future)
        // endTime
        effectiveStartTime = effectiveEndTime.minus(15, ChronoUnit.DAYS);
        log.info(
            "Re-defaulting start_time ({}) based on adjusted end_time ({}).",
            effectiveStartTime,
            effectiveEndTime);
      }
    }

    // 2. Check if effectiveStartTime is after effectiveEndTime (now that both are finalized)
    if (effectiveStartTime.isAfter(effectiveEndTime)) {
      log.warn(
          "Validation Error: effective start_time ({}) is after effective end_time ({}).",
          effectiveStartTime,
          effectiveEndTime);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "start_time cannot be after end_time.");
    }

    log.debug(
        "Effective parameters for service: startTime: {}, endTime: {}, window: {}",
        effectiveStartTime,
        effectiveEndTime,
        windowEnum);

    List<TimeSeriesAnalyticsDTO> analytics =
        inferenceAnalyticsService.getInferenceTimeSeriesAnalytics(
            modelId,
            user,
            projectId,
            modelProvider,
            effectiveStartTime,
            effectiveEndTime,
            windowEnum,
            tagIds);

    TimeSeriesAnalyticsResponseDTO response = new TimeSeriesAnalyticsResponseDTO(analytics);
    return ResponseEntity.ok(response);
  }
}

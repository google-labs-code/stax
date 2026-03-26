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
import com.planck.planck.domain.inferencemonitoring.InferenceMonitoringRepository;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.AggregationWindow;
import com.planck.planck.enums.ModelProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InferenceAnalyticsServiceImpl implements InferenceAnalyticsService {

  private final InferenceMonitoringRepository inferenceMonitoringRepository;

  @Override
  public List<TimeSeriesAnalyticsDTO> getInferenceTimeSeriesAnalytics(
      String modelId,
      User user,
      String projectId,
      ModelProvider modelProvider,
      Instant startTime,
      Instant endTime,
      AggregationWindow window, // This is the 'aggregateWindow' from controller, can be null
      List<String> tagIds) {
    log.debug(
        "Service layer: getInferenceTimeSeriesAnalytics called with startTime={}, endTime={}, initialWindow={}",
        startTime,
        endTime,
        window);
    AggregationWindow effectiveWindow =
        determineEffectiveAggregationWindow(window, startTime, endTime);

    log.info("Calling repository");
    return inferenceMonitoringRepository.getAnalyticsTimeSeries(
        modelId,
        user.getId(),
        projectId,
        modelProvider,
        startTime,
        endTime,
        effectiveWindow,
        tagIds);
  }

  /**
   * Determines the effective aggregation window. If an initial window is provided, it's used.
   * Otherwise, a window is calculated dynamically based on the duration between startTime and
   * endTime.
   *
   * @param initialWindow The aggregation window possibly provided by the user (can be null).
   * @param startTime The start timestamp of the range.
   * @param endTime The end timestamp of the range.
   * @return The effective AggregationWindow to use.
   */
  private AggregationWindow determineEffectiveAggregationWindow(
      AggregationWindow initialWindow, Instant startTime, Instant endTime) {

    if (initialWindow != null) {
      log.debug("User-specified aggregation window provided: {}. Using it.", initialWindow);
      return initialWindow; // User-specified window takes precedence
    }

    // Calculate dynamic window based on duration
    Duration duration = Duration.between(startTime, endTime);
    log.info(
        "No aggregation window specified by user. Calculating dynamically based on duration: {}",
        duration);

    AggregationWindow calculatedWindow;
    if (duration.isNegative() || duration.isZero()) {
      // This case should ideally be prevented by controller validation (startTime > endTime)
      // or when startTime equals endTime. If duration is zero, few points make sense.
      log.warn("Duration is zero or negative ({}). Defaulting to DAILY for safety.", duration);
      calculatedWindow = AggregationWindow.DAILY;
    } else if (duration.compareTo(Duration.ofMinutes(15)) <= 0) { // Up to 15 minutes
      calculatedWindow = AggregationWindow.PER_SECOND;
    } else if (duration.compareTo(Duration.ofHours(1)) <= 0) { // Up to 1 hour
      calculatedWindow = AggregationWindow.PER_MINUTE;
    } else if (duration.compareTo(Duration.ofHours(6)) <= 0) { // Up to 6 hours
      calculatedWindow = AggregationWindow.PER_MINUTE;
    } else if (duration.compareTo(Duration.ofDays(2)) <= 0) { // Up to 2 days (48 hours)
      calculatedWindow = AggregationWindow.HOURLY;
    } else if (duration.compareTo(Duration.ofDays(30)) <= 0) { // Up to 30 days
      calculatedWindow = AggregationWindow.DAILY;
    } else if (duration.compareTo(Duration.ofDays(90)) <= 0) { // Up to 90 days (approx 3 months)
      calculatedWindow = AggregationWindow.WEEKLY;
    } else { // More than 90 days
      calculatedWindow = AggregationWindow.MONTHLY;
    }
    log.info("Dynamically determined aggregation window: {}", calculatedWindow);
    return calculatedWindow;
  }
}

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

package com.planck.planck.domain.analytics.inference.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Aggregated analytics data for a specific time window")
public class TimeSeriesAnalyticsDTO {

  @Schema(
      description = "Start timestamp of the aggregation window (ISO 8601 format)",
      required = true,
      example = "2025-04-21T15:00:00Z")
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  private Instant windowStart;

  @Schema(
      description =
          "Start timestamp of the aggregation window as milliseconds since the Unix epoch (UTC)",
      required = true,
      example = "1745247600000")
  private long windowStartEpochMillis;

  @Schema(description = "Number of inferences within this window", required = true, example = "50")
  private long totalInferences;

  @JsonProperty("total_turn_time_taken")
  private Double totalTurnTimeTaken;

  @JsonProperty("average_turn_time_taken")
  private Double averageTurnTimeTaken;

  @JsonProperty("total_turn_prompt_tokens")
  private Long totalTurnPromptTokens;

  @JsonProperty("total_turn_completion_tokens")
  private Long totalTurnCompletionTokens;

  @JsonProperty("total_turn_total_tokens")
  private Long totalTurnTotalTokens;

  @JsonProperty("average_avg_chat_latency")
  private Double averageAvgChatLatency;

  @JsonProperty("total_chat_prompt_tokens")
  private Long totalChatPromptTokens;

  @JsonProperty("total_chat_completion_tokens")
  private Long totalChatCompletionTokens;

  @JsonProperty("total_chat_total_tokens")
  private Long totalChatTotalTokens;

  public TimeSeriesAnalyticsDTO(
      Timestamp windowStartTimestamp,
      Long totalInferences,
      Double totalTurnTimeTaken,
      Double averageTurnTimeTaken,
      Long totalTurnPromptTokens,
      Long totalTurnCompletionTokens,
      Long totalTurnTotalTokens,
      Double averageAvgChatLatency,
      Long totalChatPromptTokens,
      Long totalChatCompletionTokens,
      Long totalChatTotalTokens) {

    // Convert Timestamp to Instant
    this.windowStart = (windowStartTimestamp != null) ? windowStartTimestamp.toInstant() : null;

    // --- Calculate epoch milliseconds from Instant --- // <-- New Logic
    if (this.windowStart != null) {
      this.windowStartEpochMillis = this.windowStart.toEpochMilli();
    } else {
      this.windowStartEpochMillis = 0L; // Or handle null case appropriately
    }

    // Rest of the constructor logic remains the same...
    this.totalInferences = totalInferences != null ? totalInferences : 0L;
    this.totalTurnTimeTaken = totalTurnTimeTaken == null ? 0.0 : totalTurnTimeTaken;
    this.averageTurnTimeTaken = averageTurnTimeTaken == null ? 0.0 : averageTurnTimeTaken;
    this.totalTurnPromptTokens = totalTurnPromptTokens == null ? 0L : totalTurnPromptTokens;
    this.totalTurnCompletionTokens =
        totalTurnCompletionTokens == null ? 0L : totalTurnCompletionTokens;
    this.totalTurnTotalTokens = totalTurnTotalTokens == null ? 0L : totalTurnTotalTokens;
    this.averageAvgChatLatency = averageAvgChatLatency == null ? 0.0 : averageAvgChatLatency;
    this.totalChatPromptTokens = totalChatPromptTokens == null ? 0L : totalChatPromptTokens;
    this.totalChatCompletionTokens =
        totalChatCompletionTokens == null ? 0L : totalChatCompletionTokens;
    this.totalChatTotalTokens = totalChatTotalTokens == null ? 0L : totalChatTotalTokens;
  }
}

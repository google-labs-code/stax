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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TimeSeriesAnalyticsResponseDTO {

  @JsonProperty("time_series_analytics")
  List<TimeSeriesAnalyticsDTO> timeSeriesAnalyticsDTO;

  @JsonProperty("overall_inferences")
  private long overallInferences;

  @JsonProperty("overall_average_latency")
  private Double overallAverageLatency = 0.0;

  @JsonProperty("overall_prompt_tokens")
  private long overallPromptTokens;

  @JsonProperty("overall_completion_tokens")
  private long overallCompletionTokens;

  @JsonProperty("overall_total_tokens")
  private long overallTotalTokens;

  public TimeSeriesAnalyticsResponseDTO(List<TimeSeriesAnalyticsDTO> timeSeriesAnalyticsDTOs) {

    this.timeSeriesAnalyticsDTO = timeSeriesAnalyticsDTOs;
    for (TimeSeriesAnalyticsDTO timeSeriesAnalyticsDTO : timeSeriesAnalyticsDTOs) {
      this.overallInferences += timeSeriesAnalyticsDTO.getTotalInferences();
      this.overallAverageLatency +=
          timeSeriesAnalyticsDTO.getTotalInferences()
              * timeSeriesAnalyticsDTO.getAverageAvgChatLatency();
      this.overallPromptTokens += timeSeriesAnalyticsDTO.getTotalChatPromptTokens();
      this.overallCompletionTokens += timeSeriesAnalyticsDTO.getTotalChatCompletionTokens();
      this.overallTotalTokens += timeSeriesAnalyticsDTO.getTotalChatTotalTokens();
    }

    this.overallAverageLatency /= this.overallInferences;
  }
}

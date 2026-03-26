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

package com.planck.planck.domain.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateEvaluationRequest {
  @Schema(description = "List of scorer IDs", example = "[\"scorer1\", \"scorer2\"]")
  @JsonProperty("scorers")
  private List<String> scorers;

  @Schema(description = "List of response IDs", example = "[\"response1\", \"response2\"]")
  @JsonProperty("response_ids")
  private List<String> responseIds;

  @Schema(
      description = "Map of scorer names to scores",
      example = "{\"scorer1\": \"score1\", \"scorer2\": \"score2\"}")
  @JsonProperty("score")
  private Map<String, String> scorer;

  @Schema(
      description = "Map of response IDs to response content",
      example = "{\"response1\": \"content1\", \"response2\": \"content2\"}")
  @JsonProperty("response")
  private Map<String, String> response;
}

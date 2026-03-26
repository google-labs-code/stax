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

package com.planck.planck.domain.importexport.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.domain.importexport.dto.message.BaseMessageExportDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SxsEvaluationPairExportDTO {

  @JsonProperty(value = "variables", index = 1)
  @Schema(description = "Variables associated with the side-by-side evaluation pair.")
  private Map<String, String> variables;

  @JsonProperty(value = "human_sxs_rating", index = 2)
  @Schema(
      description =
          "Human evaluation rating for pairs and the turns, based on sequence id of the chat turns (A_BETTER, B_BETTER, EQUAL, BOTH_BAD).")
  private List<SXSHumanEvalRatingExportDTO> humanSxsRatings;

  @JsonProperty(value = "expected_output", index = 3)
  @Schema(description = "Expected output.")
  private String expectedOutput;

  @JsonProperty(value = "chat_a", index = 4)
  @Schema(description = "Full conversation history for Chat A.")
  private List<BaseMessageExportDTO> chatA;

  @JsonProperty(value = "chat_b", index = 5)
  @Schema(description = "Full conversation history for Chat B.")
  private List<BaseMessageExportDTO> chatB;
}

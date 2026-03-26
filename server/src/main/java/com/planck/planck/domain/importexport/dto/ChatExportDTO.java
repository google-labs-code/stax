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
import com.planck.planck.domain.evaluation.dto.LLMEvaluationDTO;
import com.planck.planck.domain.importexport.dto.message.BaseMessageExportDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ChatExportDTO {

  @JsonProperty(value = "system_instruction", index = 1)
  private String systemInstruction;

  @JsonProperty(index = 2)
  private String input;

  @JsonProperty(index = 3)
  private String tags;

  @JsonProperty(index = 4)
  private int turns;

  @JsonProperty(value = "expected_output", index = 5)
  private String expectedOutput;

  @JsonProperty(value = "model_nickname", index = 6)
  private String modelNickname;

  @JsonProperty(value = "human_evaluation", index = 7)
  private int humanEvaluation;

  @JsonProperty(value = "human_evaluation_notes", index = 8)
  private String humanEvaluationNotes;

  @JsonProperty(index = 9)
  private String output;

  @JsonProperty(value = "inference_analytics", index = 10)
  private InferenceAnalyticsDTO inferenceAnalytics;

  @JsonProperty(value = "llm_evaluations", index = 11)
  @Schema(description = "LLM Scores grouped by evaluator name")
  private Map<String, LLMEvaluationDTO> llmEvaluations;

  @JsonProperty(index = 12)
  private Map<String, String> variables;

  @JsonProperty(index = 13)
  private List<BaseMessageExportDTO> chat;
}

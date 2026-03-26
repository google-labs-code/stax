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

package com.planck.planck.domain.importexport.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.domain.evaluation.dto.LLMEvaluationDTO;
import com.planck.planck.domain.model.dto.ModelTokens;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssistantMessageExportDTO extends BaseMessageExportDTO {

  @JsonProperty("model_nickname")
  private String modelNickname;

  @JsonProperty("tags")
  private String tags;

  @JsonProperty("inference_latency")
  private Double inferenceLatency;

  @JsonProperty("inference_tokens")
  private ModelTokens inferenceTokens;

  @JsonProperty("human_evaluation")
  private int humanEvaluation;

  @JsonProperty("human_evaluation_notes")
  private String humanEvaluationNotes;

  @JsonProperty("llm_evaluations")
  @Schema(description = "LLM Scores grouped by evaluator name")
  private Map<String, LLMEvaluationDTO> llmEvaluations;
}

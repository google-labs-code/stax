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

package com.planck.planck.domain.modelresponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.planck.planck.domain.analytics.inference.dto.InferenceMonitoringDTO;
import com.planck.planck.domain.evaluator.human.dto.HumanEvalScoreDTO;
import com.planck.planck.entitities.ModelResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(Include.NON_NULL)
@Schema(description = "Data Transfer Object for a Model Response")
public class ModelResponseDTO implements Serializable {

  @JsonProperty("id")
  @Schema(description = "Unique identifier of the model response", required = true)
  private String id;

  @JsonProperty("text")
  @Schema(description = "Text content of the model response", required = true)
  private String text;

  @JsonProperty("thinking")
  @Schema(description = "Thinking content of the model response")
  private String thinking;

  @JsonProperty("model_id")
  @Schema(
      description = "Unique identifier of the model used to generate this response",
      required = true)
  private String modelId;

  @JsonProperty("inference_monitoring")
  @Schema(description = "Inference monitoring data for this response", required = true)
  private InferenceMonitoringDTO inferenceMonitoringDTO;

  @JsonProperty("created_at")
  @Schema(description = "Timestamp of when the model response was created", required = true)
  private Timestamp createdAt;

  @JsonProperty("updated_at")
  @Schema(description = "Timestamp of when the model response was last updated", required = true)
  private Timestamp updatedAt;

  @JsonProperty("container_id")
  private String containerId;

  @JsonProperty("human_eval_scores")
  @Schema(description = "List of human evaluation scores for this chat turn")
  private List<HumanEvalScoreDTO> humanEvalScores;

  public ModelResponseDTO(ModelResponse modelResponse) {
    this.id = modelResponse.getId();
    this.text = modelResponse.getText();
    this.thinking = modelResponse.getThinking();
    if (modelResponse.getModel() != null) this.modelId = modelResponse.getModel().getId();

    if (modelResponse.getInferenceMonitoring() != null)
      this.inferenceMonitoringDTO =
          new InferenceMonitoringDTO(modelResponse.getInferenceMonitoring());
    this.createdAt = modelResponse.getCreatedAt();
    this.updatedAt = modelResponse.getUpdatedAt();

    this.containerId = modelResponse.getContainer().getId();

    if (modelResponse.getHumanEvalScores() != null
        && !modelResponse.getHumanEvalScores().isEmpty()) {
      this.humanEvalScores =
          modelResponse.getHumanEvalScores().stream()
              .map(HumanEvalScoreDTO::new)
              .collect(Collectors.toList());
    }
  }
}

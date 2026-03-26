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

package com.planck.planck.domain.analytics.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.tags.dto.Taggable;
import com.planck.planck.entitities.EvaluationMonitoring;
import com.planck.planck.enums.TagLinkTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Data Transfer Object for Inference Monitoring information")
@AllArgsConstructor
public class EvaluationMonitoringDTO implements Taggable {

  @JsonProperty("id")
  private String id;

  @JsonProperty("eval_model_id")
  private String evalModelId;

  @JsonProperty("project_id")
  private String project_id;

  @JsonProperty("evaluator_name")
  private String evaluatorName;

  @JsonProperty("evaluator_id")
  private String evaluatorId;

  @JsonProperty("time_taken")
  private Double timeTaken;

  @JsonProperty("prompt_tokens")
  private Integer promptTokens;

  @JsonProperty("completion_tokens")
  private Integer completionTokens;

  @JsonProperty("total_tokens")
  private Integer totalTokens;

  @JsonProperty("created_at")
  private Timestamp createdAt;

  @JsonProperty("updated_at")
  private Timestamp updatedAt;

  @JsonProperty("tags")
  private List<TagDTO> tags;

  public EvaluationMonitoringDTO(EvaluationMonitoring evaluationMonitoring) {
    this.id = evaluationMonitoring.getId();
    this.evalModelId = evaluationMonitoring.getModel().getId();
    this.project_id = evaluationMonitoring.getContainer().getId();
    this.evaluatorName = evaluationMonitoring.getEvaluator().getName();
    this.evaluatorId = evaluationMonitoring.getEvaluator().getId();
    this.timeTaken = evaluationMonitoring.getTimeTaken();
    this.promptTokens = evaluationMonitoring.getPromptTokens();
    this.completionTokens = evaluationMonitoring.getCompletionTokens();
    this.totalTokens = evaluationMonitoring.getTotalTokens();
    this.createdAt = evaluationMonitoring.getCreatedAt();
    this.updatedAt = evaluationMonitoring.getUpdatedAt();
  }

  @Override
  public TagLinkTargetType getTagLinkTargetType() {
    return TagLinkTargetType.EVALUATION_MONITORING;
  }
}

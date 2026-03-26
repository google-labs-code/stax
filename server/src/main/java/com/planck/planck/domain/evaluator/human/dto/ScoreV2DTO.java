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

package com.planck.planck.domain.evaluator.human.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.planck.planck.domain.analytics.evaluation.dto.EvaluationMonitoringDTO;
import com.planck.planck.entitities.ScoreV2;
import com.planck.planck.enums.ScoreType;
import java.io.Serializable;
import java.sql.Timestamp;
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
public class ScoreV2DTO implements Serializable {

  private static final long serialVersionUID = 2267168345222209112L;

  private String id;

  private Timestamp createdAt;

  private Timestamp updatedAt;

  private Double score;

  private ScoreType scoreType;

  private String scorer;

  private String llmResponse;

  private String llmOutputResponseDTO;

  private String sourceId;

  private String evalMonitoringId;

  @JsonProperty("eval_monitoring")
  private EvaluationMonitoringDTO evaluationMonitoringDTO;

  public ScoreV2DTO(ScoreV2 score) {
    this.id = score.getId();
    this.createdAt = score.getCreatedAt();
    this.updatedAt = score.getUpdatedAt();
    this.score = score.getScoreData();
    this.scoreType = score.getScoreType();
    this.scorer = score.getScorer();
    this.llmResponse = score.getLlmResponse();
    this.llmOutputResponseDTO = score.getLlmOutputResponseDTO();
    this.sourceId = score.getSourceId();
    this.evaluationMonitoringDTO = new EvaluationMonitoringDTO(score.getEvaluationMonitoring());
  }

  public void setEvaluationMonitoringDTO(EvaluationMonitoringDTO evaluationMonitoringDTO) {
    this.evaluationMonitoringDTO = evaluationMonitoringDTO;
  }
}

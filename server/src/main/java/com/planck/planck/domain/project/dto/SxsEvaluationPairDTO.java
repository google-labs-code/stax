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

package com.planck.planck.domain.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.planck.planck.domain.evaluation.dto.LLMEvaluationDTO;
import com.planck.planck.entitities.SxsEvaluationPair;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SxsEvaluationPairDTO {

  private final String id;
  private final String containerId;
  private final String chatIdA;
  private final String chatTurnIdA;
  private final String chatIdB;
  private final String chatTurnIdB;

  @JsonProperty("llm_evaluations")
  @Schema(description = "LLM Evaluation scores grouped by evaluator name")
  private Map<String, LLMEvaluationDTO> llmScores;

  public SxsEvaluationPairDTO(SxsEvaluationPair pair) {
    this.id = pair.getId();
    this.containerId = pair.getContainer().getId();
    this.chatIdA = pair.getChatA() != null ? pair.getChatA().getId() : null;
    this.chatTurnIdA = pair.getChatTurnA() != null ? pair.getChatTurnA().getId() : null;
    this.chatIdB = pair.getChatB() != null ? pair.getChatB().getId() : null;
    this.chatTurnIdB = pair.getChatTurnB() != null ? pair.getChatTurnB().getId() : null;
  }
}

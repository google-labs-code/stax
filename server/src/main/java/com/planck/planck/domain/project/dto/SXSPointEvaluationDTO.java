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

import com.planck.planck.domain.evaluation.dto.LLMEvaluationDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SXSPointEvaluationDTO {

  public SXSPointEvaluationDTO(LLMEvaluationDTO chatTurnA, LLMEvaluationDTO chatTurnB) {
    this.chatTurnA = chatTurnA;
    this.chatTurnB = chatTurnB;
    calculateDelta();
  }

  @Schema(description = "LLM evaluation for chat turn A")
  private LLMEvaluationDTO chatTurnA;

  @Schema(description = "LLM evaluation for chat turn B")
  private LLMEvaluationDTO chatTurnB;

  @Schema(description = "Delta between chatTurnA and chatTurnB scores (A - B)")
  private String delta;

  public void calculateDelta() {
    try {
      if (chatTurnA == null
          || chatTurnB == null
          || chatTurnA.getScore() == null
          || chatTurnB.getScore() == null) {
        this.delta = "N/A";
        return;
      }

      double scoreA = Double.parseDouble(chatTurnA.getScore());
      double scoreB = Double.parseDouble(chatTurnB.getScore());
      this.delta = String.valueOf(scoreA - scoreB);
    } catch (NumberFormatException e) {
      this.delta = "N/A";
    }
  }
}

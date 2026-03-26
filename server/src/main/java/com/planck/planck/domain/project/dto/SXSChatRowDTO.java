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
import com.planck.planck.domain.evaluation.EvaluationScoreHelper;
import com.planck.planck.domain.evaluation.dto.EvaluationPointwiseHeuristicScoreHelper;
import com.planck.planck.domain.evaluation.dto.LLMEvaluationDTO;
import com.planck.planck.domain.evaluation.dto.PairwiseScoreHelper;
import com.planck.planck.domain.evaluation.dto.PointwiseHeuristicEvaluationDTO;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.SXSHumanFeedback;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.enums.HumanSxsRating;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SXSChatRowDTO {
  @JsonProperty("id")
  @Schema(description = "ID of the SXS comparison", example = "uuid4")
  private String id;

  @JsonProperty("input")
  @Schema(description = "Input text for the SXS comparison", example = "Hello, how are you?")
  private String input;

  @JsonProperty("raw_input")
  @Schema(description = "Raw input text for the SXS comparison", example = "Hello, how are you?")
  private String rawInput;

  @JsonProperty("expected_output")
  @Schema(
      description = "Expected output text for the SXS comparison",
      example = "I'm doing well, thank you!")
  private String expectedOutput;

  @JsonProperty("human_sxs_rating")
  @Schema(description = "Human SXS rating for the SXS comparison", example = "A_IS_BETTER")
  private HumanSxsRating humanSxsRating;

  @JsonProperty("human_sxs_notes")
  @Schema(description = "Human SXS notes for the SXS comparison", example = "The response was good")
  private String humanSxsNotes;

  @JsonProperty("chat_turn_a")
  @Schema(description = "First chat turn comparison data")
  private SXSChatTurnWorkbookRowDTO chatTurnA;

  @JsonProperty("chat_turn_b")
  @Schema(description = "Second chat turn comparison data")
  private SXSChatTurnWorkbookRowDTO chatTurnB;

  @JsonProperty("point_evaluations")
  @Schema(description = "Point evaluations for the SXS comparison")
  private Map<String, SXSPointEvaluationDTO> pointEvaluations;

  @JsonProperty("sxs_evaluations")
  @Schema(description = "Pairwise evaluations for the SXS comparison")
  private Map<String, LLMEvaluationDTO> pairwiseEvaluations;

  @JsonProperty("point_heuristic_evaluations")
  @Schema(description = "Point Heuristic evaluations for the SXS comparison")
  private Map<String, SXSPointwiseHeuristicEvaluationDTO> pointHeuristicEvaluations;

  public SXSChatRowDTO(SxsEvaluationPair pair) {
    this(pair, null);
  }

  public SXSChatRowDTO(SxsEvaluationPair pair, SXSHumanFeedback humanFeedback) {
    this.id = pair.getId();
    this.pointEvaluations = new HashMap<>();
    this.pairwiseEvaluations = new HashMap<>();
    this.pointHeuristicEvaluations = new HashMap<>();
    this.expectedOutput = pair.getExpectedOutput();
    if (humanFeedback != null) {
      this.humanSxsRating = humanFeedback.getHumanSxsRating();
      this.humanSxsNotes = humanFeedback.getHumanSxsNotes();
    }

    if (pair.getChatTurnA() != null) {
      this.rawInput = pair.getChatTurnA().getRawInputText();
      this.input = pair.getChatTurnA().getEnrichedInputText();
      this.chatTurnA = new SXSChatTurnWorkbookRowDTO(pair.getChatTurnA());
    }

    if (pair.getChatTurnB() != null) {
      this.chatTurnB = new SXSChatTurnWorkbookRowDTO(pair.getChatTurnB());
    }

    buildPointEvaluationsFromScores(pair);
    buildPointwiseHeuristicEvaluationsFromScores(pair);
    buildPairwiseEvaluationsFromScores(pair);
  }

  private void buildPointEvaluationsFromScores(SxsEvaluationPair pair) {
    Map<String, LLMEvaluationDTO> scoresA = getLLMEvaluationsFromModelResponse(pair.getChatTurnA());
    Map<String, LLMEvaluationDTO> scoresB = getLLMEvaluationsFromModelResponse(pair.getChatTurnB());

    for (Map.Entry<String, LLMEvaluationDTO> entry : scoresA.entrySet()) {
      String evaluatorNormalizedName = entry.getKey();
      LLMEvaluationDTO evalA = entry.getValue();
      LLMEvaluationDTO evalB = scoresB.remove(evaluatorNormalizedName); // Remove if present

      this.pointEvaluations.put(evaluatorNormalizedName, new SXSPointEvaluationDTO(evalA, evalB));
    }

    for (Map.Entry<String, LLMEvaluationDTO> entry : scoresB.entrySet()) {
      String evaluatorNormalizedName = entry.getKey();
      LLMEvaluationDTO evalB = entry.getValue();

      this.pointEvaluations.put(evaluatorNormalizedName, new SXSPointEvaluationDTO(null, evalB));
    }
  }

  private void buildPointwiseHeuristicEvaluationsFromScores(SxsEvaluationPair pair) {
    Map<String, PointwiseHeuristicEvaluationDTO> scoresA =
        getPointwiseHeuristicEvaluationsFromModelResponse(pair.getChatTurnA());
    Map<String, PointwiseHeuristicEvaluationDTO> scoresB =
        getPointwiseHeuristicEvaluationsFromModelResponse(pair.getChatTurnB());

    for (Map.Entry<String, PointwiseHeuristicEvaluationDTO> entry : scoresA.entrySet()) {
      String evaluatorNormalizedName = entry.getKey();
      PointwiseHeuristicEvaluationDTO evalA = entry.getValue();
      PointwiseHeuristicEvaluationDTO evalB =
          scoresB.remove(evaluatorNormalizedName); // Remove if present

      this.pointHeuristicEvaluations.put(
          evaluatorNormalizedName, new SXSPointwiseHeuristicEvaluationDTO(evalA, evalB));
    }

    for (Map.Entry<String, PointwiseHeuristicEvaluationDTO> entry : scoresB.entrySet()) {
      String evaluatorNormalizedName = entry.getKey();
      PointwiseHeuristicEvaluationDTO evalB = entry.getValue();

      this.pointHeuristicEvaluations.put(
          evaluatorNormalizedName, new SXSPointwiseHeuristicEvaluationDTO(null, evalB));
    }
  }

  private void buildPairwiseEvaluationsFromScores(SxsEvaluationPair pair) {
    if (pair.getPairwiseScores() == null || pair.getPairwiseScores().isEmpty()) {
      return;
    }

    PairwiseScoreHelper helper =
        new PairwiseScoreHelper(pair.getPairwiseScores(), pair.getChatTurnA(), pair.getChatTurnB());
    Map<String, LLMEvaluationDTO> llmScores = helper.getPairwiseEvaluationScores();
    this.pairwiseEvaluations = llmScores;
  }

  private Map<String, LLMEvaluationDTO> getLLMEvaluationsFromModelResponse(ChatTurn chatTurn) {
    if (chatTurn == null
        || chatTurn.getModelResponse() == null
        || chatTurn.getModelResponse().getScores() == null
        || chatTurn.getModelResponse().getScores().isEmpty()) {
      return new HashMap<>();
    }

    EvaluationScoreHelper helper =
        new EvaluationScoreHelper(chatTurn.getModelResponse().getScores());
    return helper.getLLMScores();
  }

  private Map<String, PointwiseHeuristicEvaluationDTO>
      getPointwiseHeuristicEvaluationsFromModelResponse(ChatTurn chatTurn) {
    if (chatTurn == null
        || chatTurn.getModelResponse() == null
        || chatTurn.getModelResponse().getPointwiseHeuristicEvaluationScores() == null
        || chatTurn.getModelResponse().getPointwiseHeuristicEvaluationScores().isEmpty()) {
      return new HashMap<>();
    }

    EvaluationPointwiseHeuristicScoreHelper helper =
        new EvaluationPointwiseHeuristicScoreHelper(
            chatTurn.getModelResponse().getPointwiseHeuristicEvaluationScores());
    return helper.getPointwiseHeuristicScores();
  }
}

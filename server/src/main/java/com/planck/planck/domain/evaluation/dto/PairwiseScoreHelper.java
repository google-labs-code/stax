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

import static com.planck.planck.util.PlanckConstants.DEFAULT_EVALUATOR_CATEGORY_COLOR;

import com.planck.planck.domain.evaluator.dto.OutputCategoryDTO;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.PairwiseScore;
import com.planck.planck.enums.EvaluationStatusEnum;
import com.planck.planck.util.EvaluationPatternUtil;
import com.planck.planck.util.ObjectMapperUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import lombok.Getter;

@Getter
public class PairwiseScoreHelper {
  private Map<String, String> scores = new HashMap<>();
  private List<PairwiseScore> pairwiseScoresList;
  private Map<String, String> evaluationStatuses = new HashMap<>();

  public PairwiseScoreHelper(List<PairwiseScore> scores, ChatTurn chatTurnA, ChatTurn chatTurnB) {
    if (chatTurnA == null || chatTurnB == null) {
      this.pairwiseScoresList = new ArrayList<>();
      return;
    }

    this.pairwiseScoresList =
        scores.stream()
            .filter(
                score ->
                    (score.getChatTurnA() != null
                            && score.getChatTurnA().getId().equals(chatTurnA.getId()))
                        && (score.getChatTurnB() != null
                            && score.getChatTurnB().getId().equals(chatTurnB.getId())))
            .toList();

    for (PairwiseScore score : scores) {
      String evaluatorName = "Unknown Evaluator";
      String evaluationStatusValue = "";

      if (score.getEvaluationStatus() != null) {
        EvaluationStatus status = score.getEvaluationStatus();
        evaluationStatusValue = status.getStatus() != null ? status.getStatus().toString() : "";
        if (score.getLlmEvaluator() != null) {
          evaluatorName = score.getLlmEvaluator().getName();
        }
      }
      this.scores.put(
          evaluatorName, score.getScore() != null ? score.getScore() : score.getLlmResponse());
      this.evaluationStatuses.put(evaluatorName, evaluationStatusValue);
    }
  }

  public Map<String, LLMEvaluationDTO> getPairwiseEvaluationScores() {
    Map<String, LLMEvaluationDTO> llmScores = new HashMap<>();
    pairwiseScoresList.forEach(
        score -> {
          if (score.getLlmEvaluator() != null) {
            String evaluatorName = score.getLlmEvaluator().getName();
            LLMEvaluationDTO llmScoreDTO = new LLMEvaluationDTO();
            llmScoreDTO.setEvaluatorId(score.getLlmEvaluator().getId());

            String scoreValue = score.getScore();
            llmScoreDTO.setScore(scoreValue != null ? scoreValue : "NaN");

            llmScoreDTO.setLlmResponse(score.getLlmResponse());
            llmScoreDTO.setReasoning(getReasoning(score.getLlmResponse()));
            llmScoreDTO.setCategory(getCategory(score.getLlmResponse()));

            String color = resolveColor(score.getLlmEvaluator().getOutputCategories(), scoreValue);
            llmScoreDTO.setColor(color);

            String evaluationStatusValue = "";
            String evaluationStatusValueText = "";
            if (score.getEvaluationStatus() != null
                && score.getEvaluationStatus().getStatus() != null) {
              evaluationStatusValue = score.getEvaluationStatus().getStatus().toString();
              setFailureReason(score, llmScoreDTO);
              evaluationStatusValueText =
                  EvaluationStatusEnum.getValueByKey(score.getEvaluationStatus().getStatus());
            }
            llmScoreDTO.setEvaluationStatus(evaluationStatusValue);
            llmScoreDTO.setEvaluationStatusText(evaluationStatusValueText);
            llmScores.put(evaluatorName, llmScoreDTO);
          }
        });
    return llmScores;
  }

  private void setFailureReason(PairwiseScore score, LLMEvaluationDTO llmScoreDTO) {
    if (!score.getEvaluationStatus().getStatus().equals(EvaluationStatusEnum.FAILED.getKey())) {
      return;
    }

    if (score.getEvaluationStatus().getComments() != null) {
      llmScoreDTO.setFailureReason(score.getEvaluationStatus().getComments());
      if (llmScoreDTO.getReasoning() == null) {
        llmScoreDTO.setReasoning(score.getEvaluationStatus().getComments());
      }
      return;
    }

    llmScoreDTO.setFailureReason(score.getEvaluationStatus().getReason());
  }

  private String resolveColor(String outputCategoriesJson, String scoreValue) {
    if (outputCategoriesJson == null || scoreValue == null) {
      return DEFAULT_EVALUATOR_CATEGORY_COLOR;
    }

    List<OutputCategoryDTO> outputCategories =
        ObjectMapperUtil.convertJsonStringToList(outputCategoriesJson, OutputCategoryDTO.class);

    if (outputCategories == null) {
      return DEFAULT_EVALUATOR_CATEGORY_COLOR;
    }

    return outputCategories.stream()
        .filter(c -> scoreValue.equals(c.getValue()))
        .map(OutputCategoryDTO::getColor)
        .findFirst()
        .orElse(DEFAULT_EVALUATOR_CATEGORY_COLOR);
  }

  private String getReasoning(String llmResponse) {
    String reasoning = null;
    if (llmResponse == null || llmResponse.isEmpty()) {
      return reasoning;
    }

    Matcher categoryMatcher = EvaluationPatternUtil.CATEGORY_PATTERN.matcher(llmResponse);
    if (categoryMatcher.find() && categoryMatcher.groupCount() >= 2) {
      reasoning = categoryMatcher.group(2).trim();
    }

    return reasoning;
  }

  private String getCategory(String llmResponse) {

    String category = null;
    if (llmResponse == null || llmResponse.isEmpty()) {
      return category;
    }
    Matcher categoryMatcher = EvaluationPatternUtil.CATEGORY_PATTERN.matcher(llmResponse);
    if (categoryMatcher.find() && categoryMatcher.groupCount() >= 1) {
      category = categoryMatcher.group(1);
    }

    return category;
  }
}

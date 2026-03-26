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

import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.PointwiseHeuristicEvaluationScore;
import com.planck.planck.enums.EvaluationStatusEnum;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class EvaluationPointwiseHeuristicScoreHelper {

  private List<PointwiseHeuristicEvaluationScore> scoresList;
  private Map<String, String> evaluationStatuses = new HashMap<>();

  public EvaluationPointwiseHeuristicScoreHelper(List<PointwiseHeuristicEvaluationScore> scores) {
    this.scoresList = scores;

    for (PointwiseHeuristicEvaluationScore score : scores) {
      String evaluatorName = "Unknown Evaluator";
      String evaluationStatusValue = "";

      if (score.getEvaluationStatus() != null) {
        EvaluationStatus status = score.getEvaluationStatus();
        evaluationStatusValue = status.getStatus() != null ? status.getStatus().toString() : "";
        if (score.getPointwiseEvaluator() != null) {
          evaluatorName = score.getPointwiseEvaluator().getName();
        }
      }

      this.evaluationStatuses.put(evaluatorName, evaluationStatusValue);
    }
  }

  public Map<String, PointwiseHeuristicEvaluationDTO> getPointwiseHeuristicScores() {
    Map<String, PointwiseHeuristicEvaluationDTO> heuristicScores = new HashMap<>();
    scoresList.forEach(
        score -> {
          if (score.getPointwiseEvaluator() != null) {
            String evaluatorName = score.getPointwiseEvaluator().getName();
            PointwiseHeuristicEvaluationDTO heuristicScoreDTO =
                new PointwiseHeuristicEvaluationDTO();
            heuristicScoreDTO.setEvaluatorId(score.getPointwiseEvaluator().getId());
            heuristicScoreDTO.setMatch(score.isMatch());

            String evaluationStatusValue = "";
            String evaluationStatusValueText = "";
            if (score.getEvaluationStatus() != null
                && score.getEvaluationStatus().getStatus() != null) {
              evaluationStatusValue = score.getEvaluationStatus().getStatus().toString();
              setFailureReason(score, heuristicScoreDTO);
              evaluationStatusValueText =
                  EvaluationStatusEnum.getValueByKey(score.getEvaluationStatus().getStatus());
            }
            heuristicScoreDTO.setEvaluationStatus(evaluationStatusValue);
            heuristicScoreDTO.setEvaluationStatusText(evaluationStatusValueText);
            heuristicScores.put(evaluatorName, heuristicScoreDTO);
          }
        });
    return heuristicScores;
  }

  private void setFailureReason(
      PointwiseHeuristicEvaluationScore score, PointwiseHeuristicEvaluationDTO heuristicScoreDTO) {
    if (!score.getEvaluationStatus().getStatus().equals(EvaluationStatusEnum.FAILED.getKey())) {
      return;
    }

    // in case of exception, we'll have it in comments
    if (score.getEvaluationStatus().getComments() != null) {
      heuristicScoreDTO.setFailureReason(score.getEvaluationStatus().getComments());
      return;
    }

    heuristicScoreDTO.setFailureReason(score.getEvaluationStatus().getReason());
  }
}

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.planck.planck.entitities.HumanEvalScore;
import com.planck.planck.enums.ScoringMechanismType;
import com.planck.planck.util.ObjectMapperUtil;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class HumanEvalScoreDTO implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private Timestamp createdAt;

  private Timestamp updatedAt;

  private Double score;

  private String notes;

  @JsonProperty("model_response_id")
  private String modelResponseId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("human_evaluator_id")
  private String humanEvaluatorId;

  @JsonProperty("category_name")
  private String categoryName;

  public HumanEvalScoreDTO(HumanEvalScore score) {
    this.id = score.getId();
    this.createdAt = score.getCreatedAt();
    this.updatedAt = score.getUpdatedAt();
    this.score = score.getScore();
    this.notes = score.getNotes();
    this.modelResponseId = score.getModelResponse().getId();
    this.userId = score.getUser().getId();
    this.humanEvaluatorId = score.getHumanEvaluator().getId();

    // Get category name from human evaluator if it exists
    if (score.getHumanEvaluator() != null
        && score.getHumanEvaluator().getScoringMechanismType() == ScoringMechanismType.CATEGORY) {
      List<HumanCategoryOption> categories =
          ObjectMapperUtil.convertJsonStringToList(
              score.getHumanEvaluator().getCategories(), HumanCategoryOption.class);
      if (categories != null && !categories.isEmpty()) {
        // Find the category that matches the score
        for (HumanCategoryOption category : categories) {
          if (category.getScore().equals(score.getScore())) {
            this.categoryName = category.getCategoryName();
            break;
          }
        }
      }
    }
  }
}

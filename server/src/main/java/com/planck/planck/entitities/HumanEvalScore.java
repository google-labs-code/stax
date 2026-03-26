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

package com.planck.planck.entitities;

import com.planck.planck.domain.evaluator.human.dto.HumanCategoryOption;
import com.planck.planck.enums.ScoringMechanismType;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.util.ObjectMapperUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "human_scores")
@NoArgsConstructor
@Getter
@Setter
@Data
public class HumanEvalScore {

  @Transient private final String ID_PREFIX = "human-eval-score-";

  @Id
  @Column(unique = true, nullable = false)
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  @CreationTimestamp private Timestamp createdAt;

  @UpdateTimestamp private Timestamp updatedAt;

  @Column(name = "score", nullable = false)
  private Double score;

  @Column(name = "notes")
  private String notes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "model_response_id", referencedColumnName = "id", nullable = false)
  private ModelResponse modelResponse;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "human_evaluator_id", referencedColumnName = "id", nullable = false)
  private HumanEvaluator humanEvaluator;

  public Map<String, Object> getScoreData() {
    Map<String, Object> scoreData = new HashMap<>();
    scoreData.put("id", this.id);
    scoreData.put("score", this.score);
    scoreData.put("human_evaluator_id", this.humanEvaluator.getId());
    scoreData.put("created_at", this.createdAt);
    scoreData.put("updated_at", this.updatedAt);
    return scoreData;
  }

  public void setScore(Double score, HumanEvaluator humanEvaluator) {
    if (humanEvaluator.getScoringMechanismType() == ScoringMechanismType.CATEGORY) {
      List<HumanCategoryOption> categories =
          ObjectMapperUtil.convertJsonStringToList(
              humanEvaluator.getCategories(), HumanCategoryOption.class);
      if (categories != null && !categories.isEmpty()) {
        boolean validScore = false;
        for (HumanCategoryOption category : categories) {
          if (category.getScore().equals(score)) {
            validScore = true;
            break;
          }
        }
        if (!validScore) {
          throw new IllegalInputException(
              "Invalid score for category-based evaluator. Score must match one of the defined categories.");
        }
      }
    }
    this.humanEvaluator = humanEvaluator;
    this.score = score;
  }

  public HumanEvalScore deepCopy(ModelResponse modelResponse) {
    HumanEvalScore newScore = new HumanEvalScore();
    newScore.setModelResponse(modelResponse);
    newScore.setScore(this.score, this.humanEvaluator);
    newScore.setUser(this.user);
    newScore.setNotes(this.notes);
    return newScore;
  }

  public HumanEvalScore(
      double score, ModelResponse modelResponse, HumanEvaluator humanEval, User user) {
    this.score = score;
    this.modelResponse = modelResponse;
    this.humanEvaluator = humanEval;
    this.user = user;
  }
}

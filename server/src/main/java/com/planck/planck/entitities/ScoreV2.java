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

import com.planck.planck.enums.ScoreType;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.converter.ScorerTypeConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "Score")
@NoArgsConstructor
@Getter
@Setter
@Data
public class ScoreV2 {

  @Transient private final String ID_PREFIX = "score-";

  @Id
  @Column(unique = true, nullable = false)
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID();
    }
  }

  // TODO: Deprecated, potentially cleanup
  @Column(name = "response_id")
  private String responseId;

  // @ManyToOne
  // @JoinColumn(name = "chat_turn", nullable = false)
  // private ChatTurn chatTurn;

  @CreationTimestamp private Timestamp createdAt;

  @UpdateTimestamp private Timestamp updatedAt;

  @Column(name = "score")
  private Double score;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "model_response_id", nullable = false)
  private ModelResponse modelResponse;

  @Enumerated(EnumType.STRING)
  @Convert(converter = ScorerTypeConverter.class)
  private ScoreType scoreType;

  @Column(name = "scorer")
  private String scorer;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "llm_evaluator")
  private LLMEvaluator llmEvaluator;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "llm_response", columnDefinition = "TEXT")
  private String llmResponse;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "evaluation_status")
  private EvaluationStatus evaluationStatus;

  // TODO: Seems column is not really in use, can possibly be deleted when we start using liquibase
  @Lob
  @Column(name = "llm_output_response_dto", length = 12000)
  private String llmOutputResponseDTO;

  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
  @JoinColumn(name = "evaluation_monitoring_id", referencedColumnName = "id")
  private EvaluationMonitoring evaluationMonitoring;

  // TODO: Seems column is not really in use, can possibly be deleted when we start using liquibase
  private String sourceId;

  @Transient private String status;

  public Double getScoreData() {
    return score;
  }

  public Map<String, Object> getScore() {
    Map<String, Object> score = new HashMap<>();
    score.put("id", this.id);
    score.put("scorer", this.scorer);
    score.put("score", this.score);
    score.put("score_type", this.scoreType);
    score.put("status", this.status);

    if (this.llmResponse != null && !this.llmResponse.isEmpty())
      score.put("llm_response", this.llmResponse);
    if (this.llmEvaluator != null) score.put("llm_evaluator", this.llmEvaluator.getId());
    if (this.llmOutputResponseDTO != null && !this.llmOutputResponseDTO.isEmpty())
      score.put("llm_output_response_dto", ObjectMapperUtil.jsonToMap(llmOutputResponseDTO));
    return score;
  }

  /**
   * Creates a deep copy of a ScoreV2 for an input modelResponse. Evaluation monitoring and scores
   * have to be copied separately, as in the current setup Hibernate can't handle those
   *
   * @param modelResponse
   * @return The copied score
   */
  public ScoreV2 deepCopy(ModelResponse modelResponse) {
    ScoreV2 newScore = new ScoreV2();
    newScore.setModelResponse(modelResponse);
    newScore.setScore(this.score);
    newScore.setScoreType(this.scoreType);
    newScore.setScorer(this.scorer);
    newScore.setLlmEvaluator(this.llmEvaluator);
    newScore.setUserId(this.userId);
    newScore.setLlmResponse(this.llmResponse);
    newScore.setStatus(this.status);

    return newScore;
  }
}

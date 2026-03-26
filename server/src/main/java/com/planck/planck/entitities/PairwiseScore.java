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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "pairwise_score")
@Data
@NoArgsConstructor
public class PairwiseScore {
  @Transient private final String ID_PREFIX = "pairwise-score-";

  @Id
  @Column(unique = true, nullable = false)
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID();
    }
  }

  @CreationTimestamp private Timestamp createdAt;

  @UpdateTimestamp private Timestamp updatedAt;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "sxs_evaluation_pair", nullable = false)
  private SxsEvaluationPair pair;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "chat_turn_a", nullable = false)
  private ChatTurn chatTurnA;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "chat_turn_b", nullable = false)
  private ChatTurn chatTurnB;

  @Column(name = "score")
  private String score;

  @Enumerated(EnumType.STRING)
  @Convert(converter = ScorerTypeConverter.class)
  private ScoreType scoreType;

  @Column(name = "scorer")
  private String scorer;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "pairwise_llm_evaluator")
  private PairwiseLLMEvaluator llmEvaluator;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "llm_response", columnDefinition = "TEXT")
  private String llmResponse;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "evaluation_status")
  private EvaluationStatus evaluationStatus;

  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
  @JoinColumn(name = "evaluation_monitoring_id", referencedColumnName = "id")
  private EvaluationMonitoring evaluationMonitoring;

  @Transient private String status;
}

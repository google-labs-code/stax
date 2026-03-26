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

import com.planck.planck.enums.ModelProvider;
import com.planck.planck.util.converter.ModelProviderConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "evaluation_monitoring")
@Getter
@Setter
@NoArgsConstructor
public class EvaluationMonitoring {

  @Transient private static final String ID_PREFIX = "evaluation-monitoring-";

  @Id private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  @Column(name = "eval_model_provider", nullable = false)
  @Convert(converter = ModelProviderConverter.class)
  private ModelProvider modelProvider;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "eval_model_id", nullable = false)
  private Model model;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "container_id", nullable = false)
  private EvaluationContainer container;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "llm_evaluator_id", nullable = true)
  private LLMEvaluator evaluator;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pairwise_llm_evaluator_id", nullable = true)
  private PairwiseLLMEvaluator pairwiseEvaluator;

  @Column(name = "created_at")
  @CreationTimestamp
  private Timestamp createdAt;

  @Column(name = "updated_at")
  @UpdateTimestamp
  private Timestamp updatedAt;

  @Column(name = "time_taken", nullable = false)
  private Double timeTaken;

  private Integer promptTokens;

  private Integer completionTokens;

  private Integer totalTokens;

  public EvaluationMonitoring(
      Model model, User user, EvaluationContainer container, LLMEvaluator evaluator) {
    this.model = model;
    this.user = user;
    this.container = container;
    this.modelProvider = model.getProvider();
    this.evaluator = evaluator;
  }

  public EvaluationMonitoring(
      Model model, User user, EvaluationContainer container, PairwiseLLMEvaluator evaluator) {
    this.model = model;
    this.user = user;
    this.container = container;
    this.modelProvider = model.getProvider();
    this.pairwiseEvaluator = evaluator;
  }

  public EvaluationMonitoring(
      Model model,
      User user,
      EvaluationContainer container,
      LLMEvaluator evaluator,
      Double time_taken,
      Integer promptTokens,
      Integer completionTokens,
      Integer totalTokens) {
    this.model = model;
    this.user = user;
    this.container = container;
    this.modelProvider = model.getProvider();
    this.timeTaken = time_taken;
    this.promptTokens = promptTokens;
    this.completionTokens = completionTokens;
    this.totalTokens = totalTokens;
  }

  public EvaluationMonitoring deepCopy() {
    EvaluationMonitoring newEvaluation = new EvaluationMonitoring();
    newEvaluation.setModel(this.model);
    newEvaluation.setUser(this.user);
    newEvaluation.setTimeTaken(this.timeTaken);
    newEvaluation.setPromptTokens(this.promptTokens);
    newEvaluation.setCompletionTokens(this.completionTokens);
    newEvaluation.setTotalTokens(this.totalTokens);
    newEvaluation.setModelProvider(this.modelProvider);

    return newEvaluation;
  }
}

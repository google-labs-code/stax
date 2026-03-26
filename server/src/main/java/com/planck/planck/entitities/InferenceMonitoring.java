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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "inference_monitoring")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InferenceMonitoring {

  @Transient private static final String ID_PREFIX = "inference-monitoring-";

  @Transient InferenceMonitoring previousInferenceMonitoring;

  @Id private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  @Column(name = "model_provider", nullable = false)
  @Convert(converter = ModelProviderConverter.class)
  private ModelProvider modelProvider;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "model_id", nullable = false)
  private Model model;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "container_id", nullable = true)
  private EvaluationContainer container;

  @Column(name = "created_at")
  @CreationTimestamp
  private Timestamp createdAt;

  @Column(name = "updated_at")
  @UpdateTimestamp
  private Timestamp updatedAt;

  @Column(name = "turn_time_taken")
  private Double turnTimeTaken;

  @Column(name = "turn_prompt_tokens")
  private Integer turnPromptTokens;

  @Column(name = "turn_completion_tokens")
  private Integer turnCompletionTokens;

  @Column(name = "turn_total_tokens")
  private Integer turnTotalTokens;

  @Column(name = "avg_chat_latency")
  private Double avgChatLatency;

  @Column(name = "chat_total_prompt_tokens")
  private Integer chatTotalPromptTokens;

  @Column(name = "chat_total_completion_tokens")
  private Integer chatTotalCompletionTokens;

  @Column(name = "chat_total_tokens")
  private Integer chatTotalTokens;

  @Column(name = "sequence")
  private Integer sequence;

  public InferenceMonitoring(
      Model model,
      User user,
      EvaluationContainer container,
      InferenceMonitoring previousInferenceMonitoring) {
    this.model = model;
    this.user = user;
    this.container = container;
    this.modelProvider = model.getProvider();
    this.previousInferenceMonitoring = previousInferenceMonitoring;
    if (this.previousInferenceMonitoring != null) {
      this.sequence = this.previousInferenceMonitoring.getSequence() + 1;
    } else {
      this.sequence = 1;
    }
  }

  public void setTimetaken(double turnTimeTaken) {
    this.turnTimeTaken = turnTimeTaken;
    if (previousInferenceMonitoring != null)
      this.avgChatLatency =
          ((previousInferenceMonitoring.getAvgChatLatency()
                      * previousInferenceMonitoring.getSequence())
                  + turnTimeTaken)
              / this.sequence;
    else this.avgChatLatency = turnTimeTaken;
  }

  public void setPromptTokens(int turnPromptTokens) {
    this.turnPromptTokens = turnPromptTokens;
    if (previousInferenceMonitoring != null)
      this.chatTotalPromptTokens =
          previousInferenceMonitoring.getChatTotalPromptTokens() + turnPromptTokens;
    else this.chatTotalPromptTokens = turnPromptTokens;
  }

  public void setCompletionTokens(int turnCompletionTokens) {
    this.turnCompletionTokens = turnCompletionTokens;
    if (previousInferenceMonitoring != null)
      this.chatTotalCompletionTokens =
          previousInferenceMonitoring.getChatTotalCompletionTokens() + turnCompletionTokens;
    else this.chatTotalCompletionTokens = turnCompletionTokens;
  }

  public void setTotalTokens(int turnTotalTokens) {
    this.turnTotalTokens = turnTotalTokens;
    if (previousInferenceMonitoring != null)
      this.chatTotalTokens = previousInferenceMonitoring.getChatTotalTokens() + turnTotalTokens;
    else this.chatTotalTokens = turnTotalTokens;
  }

  public InferenceMonitoring deepCopy(EvaluationContainer container) {
    InferenceMonitoring copy = new InferenceMonitoring();
    copy.setAvgChatLatency(this.avgChatLatency);
    copy.setChatTotalCompletionTokens(this.chatTotalCompletionTokens);
    copy.setChatTotalPromptTokens(this.chatTotalPromptTokens);
    copy.setChatTotalTokens(this.chatTotalTokens);
    copy.setModel(this.model);
    copy.setModelProvider(this.modelProvider);
    copy.setContainer(container);
    copy.setSequence(this.sequence);
    copy.setUser(this.user);
    copy.setTurnCompletionTokens(this.turnCompletionTokens);
    copy.setTurnPromptTokens(this.turnPromptTokens);
    copy.setTurnTimeTaken(this.turnTimeTaken);
    copy.setTurnTotalTokens(this.turnTotalTokens);

    return copy;
  }
}

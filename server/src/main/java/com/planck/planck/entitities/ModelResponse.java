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

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "model_response")
@NoArgsConstructor
@Getter
@Setter
public class ModelResponse implements Tagable {

  @Transient private final String ID_PREFIX = "model-response-";

  @Id
  @JsonProperty("id")
  @Column(unique = true, nullable = false)
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  @JsonProperty("text")
  @Column(name = "text", columnDefinition = "longtext")
  private String text;

  @JsonProperty("thinking")
  @Column(name = "thinking", columnDefinition = "longtext")
  private String thinking;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "model_id")
  private Model model;

  @OneToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "inference_monitoring_id", referencedColumnName = "id")
  private InferenceMonitoring inferenceMonitoring;

  // private String sourceId;
  @CreationTimestamp public Timestamp createdAt;
  @UpdateTimestamp public Timestamp updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToMany(
      mappedBy = "modelResponse",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  private List<HumanEvalScore> humanEvalScores = new ArrayList<>();

  @OneToMany(
      mappedBy = "modelResponse",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  private List<ScoreV2> scores = new ArrayList<>();

  // TODO: This relationship seems obsolete, to be verified and potentially removed
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "container_id", nullable = true)
  private EvaluationContainer container;

  @OneToMany(
      mappedBy = "modelResponse",
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  private List<PointwiseHeuristicEvaluationScore> pointwiseHeuristicEvaluationScores =
      new ArrayList<>();

  public boolean validateResponseId(String responseId) {
    String id = ID_PREFIX + UUID.randomUUID().toString();
    if (responseId.length() == id.length()) if (responseId.startsWith(ID_PREFIX)) return true;
    return false;
  }

  @Override
  public String getEntityId() {
    return id;
  }

  public ModelResponse(String response, Model model, User user, EvaluationContainer container) {
    this.text = response;
    this.model = model;
    this.user = user;
    this.container = container;
  }

  public ModelResponse(
      String response,
      String thinking,
      Model model,
      User user,
      EvaluationContainer container,
      InferenceMonitoring inferenceMonitoring) {
    this.text = response;
    this.thinking = thinking;
    this.model = model;
    this.user = user;
    this.container = container;
    this.inferenceMonitoring = inferenceMonitoring;
  }

  public ModelResponse deepCopy(EvaluationContainer container) {
    ModelResponse newModelResponse = new ModelResponse();
    newModelResponse.setText(this.text);
    newModelResponse.setThinking(this.thinking);
    newModelResponse.setModel(this.model);

    newModelResponse.setUser(this.user);
    newModelResponse.setContainer(container);

    return newModelResponse;
  }
}

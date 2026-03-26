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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inference_status")
public class InferenceStatus {

  @Id private String id;

  @PrePersist
  public void prePersist() {
    this.id = "id-" + UUID.randomUUID().toString();
  }

  @Column(name = "status")
  private Integer status;

  @OneToOne
  @JoinColumn(name = "chat_turn_id", referencedColumnName = "id")
  private ChatTurn chatTurn;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "container_id")
  private String containerId;

  @Column(name = "created_at")
  @CreationTimestamp
  private Timestamp createdAt;

  @Column(name = "updated_at")
  @UpdateTimestamp
  private Timestamp updatedAt;

  @Column(name = "comments", columnDefinition = "longtext")
  private String comments;

  @Column(name = "job_id")
  private String jobId;

  @Column(name = "job_status_id")
  private String jobStatusId;

  @Column(name = "reason")
  private String reason;

  public InferenceStatus deepCopy(ChatTurn chatTurn) {
    InferenceStatus newInferenceStatus = new InferenceStatus();
    newInferenceStatus.setStatus(this.getStatus());
    newInferenceStatus.setChatTurn(chatTurn);
    newInferenceStatus.setUserId(this.getUserId());
    if (chatTurn.getChat().getContainer() != null) {
      newInferenceStatus.setContainerId(chatTurn.getChat().getContainer().getId());
    }
    newInferenceStatus.setComments(this.getComments());
    newInferenceStatus.setReason(this.getReason());
    return newInferenceStatus;
  }
}

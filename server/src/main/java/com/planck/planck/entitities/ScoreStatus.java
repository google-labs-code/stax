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
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "score_status")
public class ScoreStatus {

  @Id private String id;

  @PrePersist
  public void prePersist() {
    this.id = "id-" + UUID.randomUUID().toString();
  }

  @Column(name = "status")
  private Integer status;

  @Column(name = "reponse_id")
  private String responseId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = true)
  private Project project;

  @CreationTimestamp private Timestamp createdAt;

  @UpdateTimestamp private Timestamp updatedAt;

  @Column(name = "comments", columnDefinition = "longtext")
  private String comments;

  @Column(name = "job_id", nullable = false)
  private String jobId;

  @Column(name = "scorer")
  private String scorer;

  @Column(name = "job_status_id")
  private String jobStatusId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_status_id", insertable = false, updatable = false)
  private JobStatus jobStatus;
}

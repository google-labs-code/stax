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
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job_status")
public class JobStatus {

  @Id private String id;

  @PrePersist
  public void prePersist() {
    String uniqueId = "job_" + type;
    if (StringUtils.isNoneBlank(subType)) {
      uniqueId = uniqueId + "_" + subType;
    }
    uniqueId = uniqueId + "-" + UUID.randomUUID().toString();
    this.id = uniqueId;
    // id-> job_type_subtype_UUID (type-> scorer/inference/(bulk, subtype->
    // template/input/tag/response...))
  }

  // scorer/inference/bulk
  @Column(name = "type")
  private String type;

  // template/input/tag/response...
  @Transient private String subType;

  @Column(name = "status")
  private Integer status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = true)
  private Project project;

  @CreationTimestamp
  @Column(name = "start_time")
  private Timestamp startTime;

  @Column(name = "end_time")
  private Date endTime;

  @Column(name = "comments", columnDefinition = "longtext")
  private String comments;

  @Column(name = "pending", columnDefinition = "int default 0")
  private Integer pending;

  @Column(name = "in_progress", columnDefinition = "int default 0")
  private Integer inProgress;

  @Column(name = "failed", columnDefinition = "int default 0")
  private Integer failed;

  @Column(name = "successful", columnDefinition = "int default 0")
  private Integer successful;

  @Column(name = "stopped", columnDefinition = "int default 0")
  private Integer stopped;

  @Column(name = "total", columnDefinition = "int default 0")
  private Integer total;
}

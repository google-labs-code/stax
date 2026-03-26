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

import com.planck.planck.enums.EvaluationType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Table(
    uniqueConstraints = {
      @UniqueConstraint(
          name = "UniqueProjectNameByUser",
          columnNames = {"user_id", "name"})
    })
@Entity
@SuperBuilder
@DiscriminatorValue("PROJECT")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Project extends EvaluationContainer {

  @Transient private final String ID_PREFIX = "project-";

  // If true, the project can't be deleted by user
  @Column(name = "is_protected")
  private Boolean isProtected;

  // If true, the project is the default project for the user.
  @Column(name = "is_default")
  private Boolean isDefault;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
    if (evaluationType == null) {
      this.evaluationType = EvaluationType.POINTWISE;
    }
  }

  @Override
  public String getIdPrefix() {
    return this.ID_PREFIX;
  }

  public Boolean getIsProtected() {
    return Objects.isNull(isProtected) ? false : isProtected;
  }

  public Boolean getIsDefault() {
    return Objects.isNull(isDefault) ? false : isDefault;
  }
}

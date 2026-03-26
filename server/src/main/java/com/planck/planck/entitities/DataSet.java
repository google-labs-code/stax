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

import com.planck.planck.enums.DataSetType;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.util.converter.DataSetTypeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@DiscriminatorValue("DATASET")
@SuperBuilder
@Entity
public class DataSet extends EvaluationContainer {

  @Transient private static final String ID_PREFIX = "dataset-";

  @Column(name = "dataset_type")
  @Convert(converter = DataSetTypeConverter.class)
  private DataSetType type;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID();
    }
    if (evaluationType == null) {
      this.evaluationType = EvaluationType.POINTWISE;
    }
  }

  @Override
  public String getIdPrefix() {
    return ID_PREFIX;
  }
}

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

package com.planck.planck.domain.evaluator.human.dto;

import com.planck.planck.entitities.HumanEvaluator;
import com.planck.planck.enums.LinkedEntityType;
import com.planck.planck.enums.ScoringMechanismType;
import com.planck.planck.util.ObjectMapperUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HumanEvaluatorDTO {
  private String id;
  private String name;
  private String description;
  private ScoringMechanismType scoringMechanismType;
  // private HumanEvalRangeDataType pointScaleDataType;
  private String associatedEntityId;
  private String entityType;
  private List<HumanCategoryOption> categories;
  private LinkedEntityType linkedEntityType;

  // Range-related fields
  // private Double minValue;
  // private Double maxValue;

  public HumanEvaluatorDTO(HumanEvaluator evaluator) {
    this.id = evaluator.getId();
    this.name = evaluator.getName();
    this.description = evaluator.getDescription();
    this.scoringMechanismType = evaluator.getScoringMechanismType();
    this.associatedEntityId = evaluator.getAssociatedEntityId();
    this.entityType = evaluator.getLinkedEntityType().toString();
    this.linkedEntityType = evaluator.getLinkedEntityType();

    if (evaluator.getScoringMechanismType() == ScoringMechanismType.CATEGORY) {
      this.categories =
          ObjectMapperUtil.convertJsonStringToList(
              evaluator.getCategories(), HumanCategoryOption.class);
    } else if (evaluator.getScoringMechanismType() == ScoringMechanismType.RANGE) {
      HumanRangeOptionTbc rangeOption =
          ObjectMapperUtil.convertJsonStringToObject(
              evaluator.getRangeOptions(), HumanRangeOptionTbc.class);
      if (rangeOption != null) {
        // TODO: Fix these field assignments once fields are properly defined
        // this.minValue = rangeOption.getMinValue();
        // this.maxValue = rangeOption.getMaxValue();
        // this.pointScaleDataType =
        // HumanEvalRangeDataType.valueOf(rangeOption.getValueType().name());
      }
    }
  }
}

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

import com.planck.planck.enums.HumanEvalRangeDataType;
import com.planck.planck.enums.ScoringMechanismType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class HumanEvaluatorCreateRequestDTO {
  @NotBlank(message = "Name cannot be blank")
  @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
  private String name;

  @Size(max = 1000, message = "Description cannot exceed 1000 characters")
  private String description;

  @NotNull(message = "Scoring mechanism type cannot be null")
  private ScoringMechanismType scoringMechanismType;

  // Fields for POINT_SCALE
  private HumanEvalRangeDataType pointScaleDataType;

  @DecimalMin(value = "-100.0", inclusive = true, message = "Min value must be >= -100.0")
  @DecimalMax(value = "100.0", inclusive = true, message = "Min value must be <= 100.0")
  private Double minValue;

  @DecimalMin(value = "-100.0", inclusive = true, message = "Max value must be >= -100.0")
  @DecimalMax(value = "100.0", inclusive = true, message = "Max value must be <= 100.0")
  private Double maxValue;

  // Fields for BINARY
  @Valid private List<HumanCategoryFeedbackOptionDTO> binaryOptions;
}

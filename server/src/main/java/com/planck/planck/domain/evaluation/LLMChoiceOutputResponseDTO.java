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

package com.planck.planck.domain.evaluation;

import com.planck.planck.domain.evaluator.dto.OutputCategoryDTO;
import com.planck.planck.util.EvaluationPatternUtil;
import com.planck.planck.util.ObjectMapperUtil;
import java.util.List;
import java.util.regex.Matcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class LLMChoiceOutputResponseDTO {
  private List<OutputCategoryDTO> outputCategories;
  private String llmResponse;
  private String category;
  private String reasoning;
  private String score;

  public LLMChoiceOutputResponseDTO(String llmResponse, String outputCategoriesRaw) {
    this.llmResponse = llmResponse;

    try {
      this.outputCategories =
          ObjectMapperUtil.convertJsonStringToList(outputCategoriesRaw, OutputCategoryDTO.class);
    } catch (Exception e) {
      log.error("Error parsing output categories: {}", e.getMessage());
      this.outputCategories = List.of();
    }

    calculateScore();
  }

  private void calculateScore() {
    Matcher categoryMatcher = EvaluationPatternUtil.CATEGORY_PATTERN.matcher(llmResponse);

    if (categoryMatcher.find()) {
      category = categoryMatcher.group(1);
      reasoning = categoryMatcher.group(2).trim();
      for (OutputCategoryDTO outputCategory : outputCategories) {
        if (outputCategory.getName().equalsIgnoreCase(category)) {
          this.score = outputCategory.getValue();
          return;
        }
      }
    } else {
      log.warn("No category found in LLM response: {}", llmResponse);
      this.score = String.valueOf(Double.NaN);
    }
  }
}

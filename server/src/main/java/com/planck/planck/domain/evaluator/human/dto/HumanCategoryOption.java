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

public class HumanCategoryOption {
  private String categoryName; // e.g. "Thumbs Up", "Thumbs Down", "Neutral"
  private Double score; // e.g. 1.0, -1.0, 0.0
  private String description; // Optional description of what this category means
  private String id;

  public HumanCategoryOption() {}

  public HumanCategoryOption(String categoryName, Double score) {
    this.categoryName = categoryName;
    this.score = score;
  }

  public HumanCategoryOption(String categoryName, Double score, String description) {
    this.categoryName = categoryName;
    this.score = score;
    this.description = description;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public Double getScore() {
    return score;
  }

  public void setScore(Double score) {
    this.score = score;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}

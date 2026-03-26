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

package com.planck.planck.domain.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/** Describes a model property with metadata. */
@Getter
@Setter
public class ModelPropertyDescriptor {

  @Schema(
      description = "Type of the property",
      example = "DOUBLE",
      allowableValues = {"DOUBLE", "INTEGER", "STRING", "BOOLEAN", "STRING_ARRAY"})
  public enum PropertyType {
    DOUBLE,
    INTEGER,
    STRING,
    BOOLEAN,
    STRING_ARRAY;

    public String getName() {
      return name().toLowerCase();
    }
  }

  // Refers to the model property.
  @Schema(description = "The key of the property", example = "temperature")
  private String key;

  @Schema(description = "The type of the property", example = "double")
  private PropertyType type;

  @Schema(description = "The default value of the property", example = "1.0")
  private Object defaultValue;

  @Schema(description = "The minimum value of the property", example = "0.0")
  private Object minValue;

  @Schema(description = "The maximum value of the property", example = "2.0")
  private Object maxValue;

  @Schema(description = "Whether the property can be edited by users", example = "false")
  private Boolean editable;

  @Schema(description = "The human readable label of the property", example = "Temperature")
  private String label;

  @Schema(description = "The description of the property", example = "Temperature for the model.")
  private String description;

  // Provide a default constructor for Jackson
  public ModelPropertyDescriptor() {}

  public ModelPropertyDescriptor(
      String key,
      PropertyType type,
      Object defaultValue,
      Object minValue,
      Object maxValue,
      boolean editable,
      String label,
      String description) {
    this.key = key;
    this.type = type;
    this.defaultValue = defaultValue;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.editable = editable;
    this.label = label;
    this.description = description;
  }

  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("type", type.name().toLowerCase());
    map.put("default_value", defaultValue);
    map.put("min_value", minValue);
    map.put("max_value", maxValue);
    map.put("editable", editable);
    map.put("label", label);
    map.put("description", description);
    return map;
  }

  // TODO: Add these helper methods if you still need comparable functionality
  public Comparable<?> getComparableMinValue() {
    if (minValue instanceof Comparable) {
      return (Comparable<?>) minValue;
    }
    return null;
  }

  public Comparable<?> getComparableMaxValue() {
    if (maxValue instanceof Comparable) {
      return (Comparable<?>) maxValue;
    }
    return null;
  }
}

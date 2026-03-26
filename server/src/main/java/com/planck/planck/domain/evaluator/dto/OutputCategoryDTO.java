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

package com.planck.planck.domain.evaluator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.annotation.NumberRange;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutputCategoryDTO {

  @Schema(description = "Name of the output category", example = "High Quality")
  @JsonInclude(Include.NON_NULL)
  public String name;

  @Schema(description = "Color associated with the output category", example = "#008000")
  @JsonInclude(Include.NON_NULL)
  public String color;

  @Schema(description = "Color name derived from the hex color code", example = "Green")
  @JsonInclude(Include.NON_NULL)
  @JsonProperty("color_name")
  public String colorName;

  @Schema(description = "Value associated with the output category", example = "high")
  @JsonInclude(Include.NON_NULL)
  @NumberRange
  public String value;

  @JsonProperty("start_range")
  @Schema(description = "Start range for the output category", example = "0.8")
  @JsonInclude(Include.NON_NULL)
  private String startRange;

  @Schema(description = "End range for the output category", example = "1.0")
  @JsonInclude(Include.NON_NULL)
  @JsonProperty("end_range")
  private String endRange;

  public void setColor(String color) {
    this.color = color;
    this.colorName = deriveColorName(color);
  }

  public String getColorName() {
    if (colorName != null) {
      return colorName;
    }
    return deriveColorName(color);
  }

  private String deriveColorName(String color) {
    if (color == null) {
      return null;
    }

    String normalizedColor = color.toLowerCase().replace("#", "");

    // CSS Variable mappings
    if (color.equals("var(--color-red)")) {
      return "Red";
    }
    if (color.equals("var(--color-green)")) {
      return "Green";
    }
    if (color.equals("var(--color-blue)")) {
      return "Blue";
    }
    if (color.equals("var(--color-purple)")) {
      return "Purple";
    }
    if (color.equals("var(--color-pink)")) {
      return "Pink";
    }
    if (color.equals("var(--color-orange)")) {
      return "Orange";
    }
    if (color.equals("var(--color-yellow)")) {
      return "Yellow";
    }

    // Hex code mappings
    switch (normalizedColor) {
      case "f84464":
        return "Red";
      case "03c89a":
        return "Green";
      case "3d95fc":
        return "Blue";
      case "b66eff":
        return "Purple";
      case "ffa2af":
        return "Pink";
      case "f99c07":
        return "Orange";
      case "5f5bff":
        return "Violet";
      case "8595ab":
        return "Grey";
      case "737284":
        return "Slate Gray";
      case "ffe3e3":
        return "Light Red";
      case "fff3bf":
        return "Light Yellow";
      case "d3f9d8":
        return "Light Green";
      default:
        return null;
    }
  }
}

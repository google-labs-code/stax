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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.util.DocumentationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NewModelRegistrationWithCustomProperitesRequest {
  @Schema(description = "The label of the new model", example = "Gemini 2.0 Flash - Custom")
  @JsonProperty("new_label")
  private String label;

  @Schema(
      description = "The description of the new model",
      example = "A fast and efficient model from Google - Custom")
  @JsonProperty("new_description")
  private String description;

  @Schema(
      description = "Comments about the new model",
      example = "This model is good for quick responses - Custom")
  @JsonProperty("new_comments")
  private String comments;

  @Schema(description = "Descriptors of the new model")
  @JsonProperty("new_descriptors")
  private Map<String, ModelPropertyDescriptor> descriptors;

  @Schema(
      description = "Properties of the new model",
      example = DocumentationConstants.MODEL_PROPERTIES_EXAMPLE_VALUE)
  @JsonProperty("new_properties")
  private Map<String, Object> newModelproperties;
}

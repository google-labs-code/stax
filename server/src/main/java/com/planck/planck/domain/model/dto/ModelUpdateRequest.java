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
public class ModelUpdateRequest {

  @Schema(description = "The label of the model", example = "Gemini 2.0 Flash")
  @JsonProperty("label")
  private String label;

  @Schema(
      description = "The description of the model",
      example = "A fast and efficient model from Google")
  @JsonProperty("description")
  private String description;

  @Schema(
      description = "Comments about the model",
      example = "This model is good for quick responses")
  @JsonProperty("comments")
  private String comments;

  @Schema(
      description = "Descriptors of the model",
      example = DocumentationConstants.MODEL_DESCRIPTORS_EXAMPLE_VALUE)
  @JsonProperty("descriptors")
  private Map<String, Object> descriptors;

  @Schema(
      description = "The API key of the model, applicable only for custom endpoints",
      example = "sk-1234567890")
  @JsonProperty("api_key")
  private String apiKey;

  @Schema(
      description = "The API URL of the model, applicable only for custom endpoints",
      example = "https://api.openai.com/v1/chat/completions")
  @JsonProperty("url")
  private String url;

  @Schema(
      description = "The custom headers of the model, applicable only for custom endpoints",
      example = "{\"Header1\": \"Value1\", \"Header2\": \"Value2\"}")
  @JsonProperty("additional_headers")
  private Map<String, String> additionalHeaders;
}

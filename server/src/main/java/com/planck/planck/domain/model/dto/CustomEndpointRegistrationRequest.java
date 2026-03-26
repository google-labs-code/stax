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
import com.planck.planck.enums.ModelProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomEndpointRegistrationRequest {

  @Schema(description = "The name of the custom model", example = "my-custom-model")
  @JsonProperty("name")
  private String name;

  @Schema(description = "The label/display name of the custom model", example = "My Custom Model")
  @JsonProperty("label")
  @NotBlank(message = "Model label is required")
  private String label;

  @Schema(
      description = "The custom API endpoint URL",
      example = "https://api.example.com/v1/chat/completions")
  @JsonProperty("url")
  @NotBlank(message = "URL is required")
  private String url;

  @Schema(
      description =
          "The API key to access the custom endpoint (optional, required only for private endpoints)",
      example = "sk-1234567890abcdef")
  @JsonProperty("api_key")
  private String apiKey;

  @Schema(
      description = "The provider format this endpoint supports",
      example = "OPENAI",
      allowableValues = {"OPENAI", "MISTRAL", "GOOGLE", "ANTHROPIC"})
  @JsonProperty("supported_provider")
  @NotNull(message = "Supported provider is required")
  private ModelProvider supportedProvider;

  @Schema(
      description = "Optional description of the model",
      example = "A custom model for specific use cases")
  @JsonProperty("description")
  private String description;

  @Schema(
      description = "Optional comments about the model",
      example = "This model is optimized for code generation")
  @JsonProperty("comments")
  private String comments;

  @Schema(
      description = "Optional additional headers to include in requests",
      example = "{\"X-Custom-Header\": \"value\"}")
  @JsonProperty("additional_headers")
  private Map<String, String> additionalHeaders;

  @Schema(
      description = "Model properties like temperature, max_tokens, etc.",
      example = "{\"temperature\": 0.7, \"max_tokens\": 1000}")
  @JsonProperty("properties")
  private Map<String, Object> properties;
}

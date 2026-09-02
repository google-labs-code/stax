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
import com.planck.planck.entitities.Model;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.enums.ModelType;
import com.planck.planck.util.DocumentationConstants;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.PlanckConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModelDTO {

  @Schema(description = "The unique identifier of the model", example = "model-uuid4")
  @JsonProperty("id")
  private String id;

  @Schema(description = "The name of the model", example = "Gemini 2.0 Flash")
  @JsonProperty("name")
  private String name;

  @Schema(description = "The provider of the model", example = "GOOGLE")
  @JsonProperty("provider")
  private ModelProvider provider;

  // Used as display name on UI.
  @Deprecated
  @Schema(description = "The label of the model", example = "Gemini 2.0 Flash")
  @JsonProperty("label")
  private String label;

  @Schema(description = "The nickname of the model", example = "Gemini 2.0 Flash")
  @JsonProperty("nickname")
  private String nickname;

  @Schema(
      description = "The URL associated with the model",
      example = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent")
  @JsonProperty("url")
  private String url;

  @Schema(description = "The tag associated with the model", example = "gemini-2.5-flash (SYSTEM)")
  @JsonProperty("tag")
  private String modelTag;

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
      description = "Properties of the model",
      example = DocumentationConstants.MODEL_PROPERTIES_EXAMPLE_VALUE)
  @JsonProperty("properties")
  private Map<String, Object> properties;

  @Schema(description = "Pricing information for the model")
  @JsonProperty("pricing")
  private Map<String, Object> pricing;

  @Schema(
      description = "Descriptors of the model",
      example = DocumentationConstants.MODEL_DESCRIPTORS_EXAMPLE_VALUE)
  @JsonProperty("descriptors")
  private Map<String, Object> descriptors;

  @Schema(
      description = "Indicates if an API key is present for the model's provider",
      example = "true")
  @JsonProperty("is_api_key_present")
  private boolean isApiKeyPresent;

  @Schema(description = "Indicates if the model is deprecated", example = "false")
  @JsonProperty("is_deprecated")
  private boolean isDeprecated;

  @Schema(
      description = "The type of the model",
      example = "SYSTEM",
      allowableValues = {"SYSTEM", "USER"})
  @JsonProperty("model_type")
  private ModelType modelType;

  @Schema(description = "The release date of the model", example = "2024-02-15T00:00:00.000Z")
  @JsonProperty("release_date")
  private Timestamp releaseDate;

  @Schema(description = "Whether the model is a custom endpoint", example = "false")
  @JsonProperty("is_custom_endpoint")
  private Boolean isCustomEndpoint;

  @Schema(
      description = "The API key of the model. Applicable only for custom endpoints",
      example = "sk-1234567890")
  @JsonProperty("api_key")
  private String apiKey;

  @Schema(
      description = "The additional headers of the model. Applicable only for custom endpoints",
      example = "{\"Header1\": \"Value1\", \"Header2\": \"Value2\"}")
  @JsonProperty("additional_headers")
  private Map<String, String> additionalHeaders;

  public ModelDTO(Model model, Set<ModelProvider> apiKeysPresentSet, Map<String, Object> pricing) {
    this.id = model.getId();
    this.name = model.getName();
    this.provider = model.getProvider();
    this.label = model.getLabel();
    this.nickname = model.getLabel();
    this.url = model.getUrl();
    this.comments = model.getComments();
    this.description = model.getDescription();
    this.properties = ObjectMapperUtil.convertJsonStringToMap(model.getProperties());
    this.descriptors = ObjectMapperUtil.convertJsonStringToMap(model.getDescriptors());
    this.pricing = pricing;
    this.isCustomEndpoint = model.isCustomEndpoint();
    this.isApiKeyPresent =
        !this.isCustomEndpoint && apiKeysPresentSet.contains(model.getProvider());
    this.isDeprecated = model.isDeprecated();
    this.modelType = model.getType();
    this.releaseDate = model.getReleaseDate();
    setCustomEndpointAttributes(model);
  }

  public ModelDTO(Model model) {
    this.id = model.getId();
    this.name = model.getName();
    this.provider = model.getProvider();
    this.label = model.getLabel();
    this.nickname = model.getLabel();
    this.url = model.getUrl();
    this.comments = model.getComments();
    this.description = model.getDescription();
    this.properties = ObjectMapperUtil.convertJsonStringToMap(model.getProperties());
    this.descriptors = ObjectMapperUtil.convertJsonStringToMap(model.getDescriptors());
    this.isDeprecated = model.isDeprecated();
    this.modelType = model.getType();
    this.releaseDate = model.getReleaseDate();
    this.isCustomEndpoint = model.isCustomEndpoint();
    setCustomEndpointAttributes(model);
  }

  @SuppressWarnings("unchecked")
  private void setCustomEndpointAttributes(Model model) {
    if (!isCustomEndpoint) {
      return;
    }

    Map<String, Object> properties = ObjectMapperUtil.convertJsonStringToMap(model.getProperties());
    if (properties != null) {
      if (properties.containsKey(PlanckConstants.API_KEY_PROPERTY_NAME)) {
        this.apiKey = (String) properties.get(PlanckConstants.API_KEY_PROPERTY_NAME);
      }
      if (properties.containsKey(PlanckConstants.ADDITIONAL_HEADERS)) {
        this.additionalHeaders =
            (Map<String, String>) properties.get(PlanckConstants.ADDITIONAL_HEADERS);
      }
    }

    // always set to true, so it can show in drop downs even when the API is not secure and doesn't
    // need API key
    this.isApiKeyPresent = true;
  }
}

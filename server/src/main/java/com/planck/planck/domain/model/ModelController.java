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

package com.planck.planck.domain.model;

import com.planck.planck.domain.model.dto.CustomEndpointRegistrationRequest;
import com.planck.planck.domain.model.dto.CustomEndpointRegistrationResponse;
import com.planck.planck.domain.model.dto.ModelDTO;
import com.planck.planck.domain.model.dto.ModelUpdateRequest;
import com.planck.planck.domain.model.dto.NewModelRegistrationWithCustomProperitesRequest;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.enums.MonitoringType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("model")
@Validated
@Slf4j
@Tag(name = "Model API", description = "API for managing models")
public class ModelController {

  @Autowired private ModelService modelService;

  @Operation(
      summary = "Retrieve models",
      description = "Get all models for the user",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("list")
  public List<ModelDTO> getModelsList(
      @AuthenticationPrincipal User user,
      @RequestParam(value = "projectId", required = false) String projectId,
      @RequestParam(value = "type", required = false) MonitoringType type) {
    return modelService.getModelsList(user, projectId, type);
  }

  @Operation(
      summary = "Get model providers",
      description = "Get all available model providers",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples =
                        @io.swagger.v3.oas.annotations.media.ExampleObject(
                            value =
                                "{\n  \"OPENAI\": \"OpenAI\",\n  \"MISTRAL\": \"Mistral\",\n  \"GOOGLE\": \"Google\",\n  \"ANTHROPIC\": \"Anthropic\",\n  \"GROK\": \"Grok\",\n  \"DEEPSEEK\": \"DeepSeek\"\n}"))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("providers")
  public Map<String, String> getModelProviders() {
    return Arrays.stream(ModelProvider.values())
        .collect(Collectors.toMap(Enum::name, ModelProvider::getValue));
  }

  @Operation(
      summary = "Update model",
      description = "Update model details",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Model not found for this user"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PatchMapping({"/{modelId}"})
  public ModelDTO updateModel(
      @Parameter(description = "ID of the model", example = "model-uuid4") @PathVariable
          String modelId,
      @Validated @RequestBody ModelUpdateRequest modelUpdateRequest,
      @AuthenticationPrincipal User user) {
    return modelService.updateModel(
        modelId,
        user,
        modelUpdateRequest.getLabel(),
        modelUpdateRequest.getDescription(),
        modelUpdateRequest.getComments(),
        modelUpdateRequest.getDescriptors(),
        modelUpdateRequest.getApiKey(),
        modelUpdateRequest.getUrl(),
        modelUpdateRequest.getAdditionalHeaders());
  }

  @Operation(
      summary = "Deprecate model",
      description = "Deprecate model by id",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid payload provided"),
        @ApiResponse(responseCode = "404", description = "Model not found for this user"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PatchMapping({"deprecate/{modelId}"})
  public ModelDTO deprecateModel(
      @Parameter(description = "ID of the model", example = "model-uuid4") @PathVariable
          String modelId,
      @AuthenticationPrincipal User user) {
    return modelService.deprecateModel(modelId, user);
  }

  @Operation(
      summary = "Duplicate and modify model",
      description = "Duplicate and modify model",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid payload provided"),
        @ApiResponse(responseCode = "404", description = "Model not found for this user"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/{modelId}/duplicate-and-modify")
  public ModelDTO registerNewModelWithCustomProperties(
      @Parameter(description = "ID of the model", example = "model-uuid4") @PathVariable
          String modelId,
      @Validated @RequestBody NewModelRegistrationWithCustomProperitesRequest request,
      @AuthenticationPrincipal User user) {
    return modelService.registerNewModelWithCustomProperties(request, modelId, user);
  }

  @Operation(
      summary = "Register custom endpoint",
      description =
          "Register a custom endpoint for an existing provider (OPENAI, MISTRAL, GOOGLE, ANTHROPIC). "
              + "This allows users to use internet-based endpoints that support any of the given formats. "
              + "The API key is encrypted and stored securely, never returned in responses. "
              + "Release date is automatically set to the current date when the model is added.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Custom endpoint registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload provided"),
        @ApiResponse(responseCode = "409", description = "Model with same label already exists"),
        @ApiResponse(
            responseCode = "429",
            description = "User quota exceeded (max 50 custom models per user)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/custom-endpoint")
  public CustomEndpointRegistrationResponse registerCustomEndpoint(
      @Validated @RequestBody CustomEndpointRegistrationRequest request,
      @AuthenticationPrincipal User user) {
    return new CustomEndpointRegistrationResponse(
        modelService.registerCustomEndpoint(request, user));
  }
}

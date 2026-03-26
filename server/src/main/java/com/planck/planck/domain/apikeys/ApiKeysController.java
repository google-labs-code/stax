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

package com.planck.planck.domain.apikeys;

import com.planck.planck.Status200Response;
import com.planck.planck.annotation.RateLimited;
import com.planck.planck.domain.apikeys.dto.ApiKeysRequestDTO;
import com.planck.planck.domain.apikeys.dto.ApiKeysResponseDTO;
import com.planck.planck.domain.apikeys.service.ApiKeysService;
import com.planck.planck.entitities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-keys")
@Tag(name = "ApiKeysController", description = "API for managing API keys")
public class ApiKeysController {

  @Autowired private final ApiKeysService apiKeyService;

  public ApiKeysController(ApiKeysService apiKeyService) {
    this.apiKeyService = apiKeyService;
  }

  @GetMapping("/get-keys-by-user")
  @Operation(
      summary = "Get API keys status for the current user",
      description =
          "Retrieves the status of API keys (present/not present) for the authenticated user.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved API keys status"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @RateLimited
  public ResponseEntity<ApiKeysResponseDTO> getApiKeysByUser(@AuthenticationPrincipal User user) {
    return ResponseEntity.ok(apiKeyService.getApiKeysStatus(user));
  }

  @PostMapping("/set-key")
  @RateLimited
  @Operation(
      summary = "Set API key for a provider",
      description =
          "Sets or updates an API key for a specific provider for the authenticated user.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully set API key",
            content = @Content(schema = @Schema(implementation = Status200Response.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<Status200Response> setKey(
      @Validated @RequestBody ApiKeysRequestDTO request, @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        apiKeyService.setApiKey(user, request.getProvider(), request.getKey()));
  }

  @DeleteMapping("/delete-key")
  @RateLimited
  @Operation(
      summary = "Delete API key for a provider",
      description = "Deletes the API key for a specific provider for the authenticated user.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully deleted API key"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<Status200Response> deleteApiKeyByUser(
      @RequestBody ApiKeysRequestDTO request, @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(apiKeyService.deleteApiKey(user, request.getProvider()));
  }
}

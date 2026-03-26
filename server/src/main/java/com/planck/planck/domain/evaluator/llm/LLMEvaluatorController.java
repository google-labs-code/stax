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

package com.planck.planck.domain.evaluator.llm;

import com.planck.planck.Status200Response;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorResponseDTO;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorUpdateDTO;
import com.planck.planck.domain.evaluator.llm.service.NewLLMEvaluatorService;
import com.planck.planck.entitities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/llm_evaluator"})
@AllArgsConstructor
@Tag(name = "LLM Evaluator API", description = "API for managing LLM evaluators")
public class LLMEvaluatorController {
  @Autowired private NewLLMEvaluatorService llmEvaluatorService;

  @GetMapping("/{evaluatorId}")
  @Operation(
      summary = "Get LLM evaluator by ID",
      description = "Retrieves an LLM evaluator by its unique identifier.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the LLM evaluator"),
        @ApiResponse(responseCode = "404", description = "LLM evaluator not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<LLMEvaluatorResponseDTO> getEvaluator(
      @AuthenticationPrincipal User user, @PathVariable String evaluatorId) {
    return ResponseEntity.ok(llmEvaluatorService.getEvaluatorById(user, evaluatorId));
  }

  @GetMapping
  public ResponseEntity<List<LLMEvaluatorResponseDTO>> getEvaluator(
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(llmEvaluatorService.getEvaluatorByUserOrSystemType(user));
  }

  @GetMapping("custom")
  @Operation(
      summary = "Get custom LLM evaluators",
      description = "Retrieves all custom LLM evaluators for the authenticated user.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved custom LLM evaluators"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<List<LLMEvaluatorResponseDTO>> getCustomEvaluator(
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(llmEvaluatorService.getEvaluatorByUser(user));
  }

  @GetMapping("system")
  public ResponseEntity<List<LLMEvaluatorResponseDTO>> getSystemEvaluator() {
    return ResponseEntity.ok(llmEvaluatorService.getEvaluatorBySystemType());
  }

  @PostMapping
  @Operation(
      summary = "Create an LLM evaluator",
      description = "Creates a new LLM evaluator.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully created the LLM evaluator"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<LLMEvaluatorResponseDTO> createLLMEvaluator(
      @Validated @RequestBody LLMEvaluatorRequestDTO request, @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(llmEvaluatorService.createLLMEvaluator(request, user));
  }

  @DeleteMapping("/{evaluatorId}")
  @Operation(
      summary = "Remove an LLM evaluator",
      description =
          "Removes an LLM evaluator. Evaluators with prior usage are deprecated, and those"
              + " without it are deleted.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully removed the LLM evaluator"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "LLM evaluator not found")
      })
  public ResponseEntity<Status200Response> removeLLMEvaluatorById(
      @AuthenticationPrincipal User user, @PathVariable String evaluatorId) {
    llmEvaluatorService.removeLLMEvaluatorById(user, evaluatorId);
    return ResponseEntity.ok(
        Status200Response.builder().message(evaluatorId + " removed successfully").build());
  }

  @PatchMapping("/{evaluatorId}")
  @Operation(
      summary = "Update an LLM evaluator",
      description = "Updates an existing LLM evaluator.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully updated the LLM evaluator"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "LLM evaluator not found")
      })
  public ResponseEntity<LLMEvaluatorResponseDTO> updateLLMEvaluator(
      @PathVariable String evaluatorId,
      @Validated @RequestBody LLMEvaluatorUpdateDTO request,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(llmEvaluatorService.updateLLMEvaluator(request, evaluatorId, user));
  }
}

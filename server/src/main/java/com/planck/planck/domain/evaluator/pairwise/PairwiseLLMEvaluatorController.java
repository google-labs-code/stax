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

package com.planck.planck.domain.evaluator.pairwise;

import com.planck.planck.domain.evaluator.pairwise.dto.PairwiseLLMEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.pairwise.dto.PairwiseLLMEvaluatorResponseDTO;
import com.planck.planck.domain.evaluator.pairwise.service.PairwiseLLMEvaluatorService;
import com.planck.planck.entitities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("pairwise-llm-evaluators")
@AllArgsConstructor
@Tag(name = "Pairwise LLM Evaluators", description = "APIs for managing Pairwise LLM evaluators")
public class PairwiseLLMEvaluatorController {

  private final PairwiseLLMEvaluatorService pairwiseLlmEvaluatorService;

  @PostMapping
  @Operation(
      summary = "Create a new Pairwise LLM evaluator",
      description = "Creates a new Pairwise LLM evaluator for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Pairwise LLM evaluator created successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PairwiseLLMEvaluatorResponseDTO.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "409", description = "Evaluator name already exists")
      })
  public ResponseEntity<PairwiseLLMEvaluatorResponseDTO> createPairwiseLLMEvaluator(
      @Valid @RequestBody PairwiseLLMEvaluatorRequestDTO request,
      @AuthenticationPrincipal User user) {
    PairwiseLLMEvaluatorResponseDTO response =
        pairwiseLlmEvaluatorService.createPairwiseLLMEvaluator(request, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @Operation(
      summary = "Get all Pairwise LLM evaluators",
      description =
          "Retrieves all Pairwise LLM evaluators available to the user (both user and system evaluators)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved Pairwise LLM evaluators",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PairwiseLLMEvaluatorResponseDTO.class))
            }),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<List<PairwiseLLMEvaluatorResponseDTO>> getAllPairwiseLLMEvaluators(
      @AuthenticationPrincipal User user) {
    List<PairwiseLLMEvaluatorResponseDTO> evaluators =
        pairwiseLlmEvaluatorService.getEvaluatorByUserOrSystemType(user);
    return ResponseEntity.ok(evaluators);
  }

  @GetMapping("/{evaluatorId}")
  @Operation(
      summary = "Get a specific Pairwise LLM evaluator",
      description = "Retrieves a specific Pairwise LLM evaluator by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved Pairwise LLM evaluator",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PairwiseLLMEvaluatorResponseDTO.class))
            }),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Evaluator not found")
      })
  public ResponseEntity<PairwiseLLMEvaluatorResponseDTO> getPairwiseLLMEvaluatorById(
      @Parameter(description = "ID of the Pairwise LLM evaluator") @PathVariable String evaluatorId,
      @AuthenticationPrincipal User user) {
    PairwiseLLMEvaluatorResponseDTO evaluator =
        pairwiseLlmEvaluatorService.getEvaluatorById(user, evaluatorId);
    return ResponseEntity.ok(evaluator);
  }

  @PatchMapping("/{evaluatorId}")
  @Operation(
      summary = "Update a Pairwise LLM evaluator",
      description = "Updates an existing user-created Pairwise LLM evaluator")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Evaluator updated successfully",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PairwiseLLMEvaluatorResponseDTO.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Evaluator not found"),
        @ApiResponse(responseCode = "409", description = "Evaluator name already exists")
      })
  public ResponseEntity<PairwiseLLMEvaluatorResponseDTO> updatePairwiseLLMEvaluator(
      @Parameter(description = "ID of the Pairwise LLM evaluator to update") @PathVariable
          String evaluatorId,
      @RequestBody PairwiseLLMEvaluatorRequestDTO request,
      @AuthenticationPrincipal User user) {
    PairwiseLLMEvaluatorResponseDTO updatedEvaluator =
        pairwiseLlmEvaluatorService.updatePairwiseLLMEvaluator(user, evaluatorId, request);
    return ResponseEntity.ok(updatedEvaluator);
  }

  @DeleteMapping("/{evaluatorId}")
  @Operation(
      summary = "Delete a Pairwise LLM evaluator",
      description =
          "Deletes a user-created Pairwise LLM evaluator (system evaluators cannot be deleted)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Evaluator deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Evaluator not found"),
        @ApiResponse(responseCode = "400", description = "Cannot delete system evaluator")
      })
  public ResponseEntity<Void> deletePairwiseLLMEvaluator(
      @Parameter(description = "ID of the Pairwise LLM evaluator to delete") @PathVariable
          String evaluatorId,
      @AuthenticationPrincipal User user) {
    pairwiseLlmEvaluatorService.removePairwiseLLMEvaluatorById(user, evaluatorId);
    return ResponseEntity.noContent().build();
  }
}

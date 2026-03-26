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

package com.planck.planck.domain.evaluator.heuristic;

import com.planck.planck.Status200Response;
import com.planck.planck.domain.evaluator.dto.PointwiseHeuristicEvaluatorUpdateDTO;
import com.planck.planck.domain.evaluator.heuristic.dto.PointwiseHeuristicEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.heuristic.dto.PointwiseHeuristicEvaluatorResponseDTO;
import com.planck.planck.domain.evaluator.heuristic.service.PointwiseHeuristicEvaluatorService;
import com.planck.planck.entitities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pointwise-heuristic")
@AllArgsConstructor
@Tag(
    name = "Heuristic Evaluator API",
    description = "API for managing Pointwise Heuristic evaluators")
public class PointwiseHeuristicEvaluatorController {

  private final PointwiseHeuristicEvaluatorService pointwiseHeuristicEvaluatorService;

  @PostMapping
  @Operation(
      summary = "Create a new heuristic evaluator",
      description = "Creates a new heuristic evaluator for the authenticated user.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully created the evaluator"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<PointwiseHeuristicEvaluatorResponseDTO> createEvaluator(
      @Valid @RequestBody PointwiseHeuristicEvaluatorRequestDTO requestDTO,
      @AuthenticationPrincipal User user) {

    PointwiseHeuristicEvaluatorResponseDTO response =
        pointwiseHeuristicEvaluatorService.createEvaluator(requestDTO, user);

    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(
      summary = "Get all heuristic evaluators",
      description =
          "Retrieves a list of all non deprecated heuristic evaluators for the authenticated user.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved evaluators"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<List<PointwiseHeuristicEvaluatorResponseDTO>> getAllEvaluators(
      @AuthenticationPrincipal User user) {

    List<PointwiseHeuristicEvaluatorResponseDTO> evaluators =
        pointwiseHeuristicEvaluatorService.getEvaluatorsByUser(user);

    return ResponseEntity.ok(evaluators);
  }

  @GetMapping("/{evaluatorId}")
  @Operation(
      summary = "Get a heuristic evaluator by ID",
      description = "Retrieves an evaluator by its unique identifier for the authenticated user.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the evaluator"),
        @ApiResponse(responseCode = "404", description = "Evaluator not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  public ResponseEntity<PointwiseHeuristicEvaluatorResponseDTO> getEvaluatorById(
      @PathVariable String evaluatorId, @AuthenticationPrincipal User user) {

    PointwiseHeuristicEvaluatorResponseDTO response =
        pointwiseHeuristicEvaluatorService.getEvaluatorById(evaluatorId, user);

    return ResponseEntity.ok(response);
  }

  @PutMapping("/{evaluatorId}")
  @Operation(
      summary = "Update an existing heuristic evaluator",
      description = "Updates an existing heuristic evaluator by its unique identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully updated the evaluator"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Evaluator not found")
      })
  public ResponseEntity<PointwiseHeuristicEvaluatorResponseDTO> updateEvaluator(
      @PathVariable String evaluatorId,
      @Valid @RequestBody PointwiseHeuristicEvaluatorUpdateDTO request,
      @AuthenticationPrincipal User user) {

    PointwiseHeuristicEvaluatorResponseDTO response =
        pointwiseHeuristicEvaluatorService.updateEvaluator(evaluatorId, request, user);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{evaluatorId}")
  @Operation(
      summary = "Remove a heuristic evaluator",
      description =
          "Removes an Pointwise Heuristic evaluator. Evaluators with prior usage are deprecated, and those"
              + " without it are deleted.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully removed the heuristic evaluator"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Evaluator not found")
      })
  public ResponseEntity<Status200Response> removePointwiseHeuristicEvaluatorById(
      @PathVariable String evaluatorId, @AuthenticationPrincipal User user) {

    pointwiseHeuristicEvaluatorService.removePointwiseHeuristicEvaluatorById(evaluatorId, user);

    return ResponseEntity.ok(
        Status200Response.builder().message(evaluatorId + " removed successfully").build());
  }
}

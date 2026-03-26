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

package com.planck.planck.domain.evaluator.human;

import com.planck.planck.domain.chatturn.service.ChatTurnService;
import com.planck.planck.domain.evaluator.human.dto.HumanEvalScoreDTO;
import com.planck.planck.domain.evaluator.human.dto.HumanEvaluatorDTO;
import com.planck.planck.domain.evaluator.human.dto.HumanEvaluatorUpdateDTO;
import com.planck.planck.domain.evaluator.human.dto.HumanRangeOption;
import com.planck.planck.domain.evaluator.human.dto.ScoreV2DTO;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorService;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.HumanEvaluator;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.LinkedEntityType;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoringMechanismType;
import com.planck.planck.exceptions.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("human-evaluators")
@Tag(
    name = "Human Evaluator",
    description = "APIs for managing human evaluators and their feedback")
public class HumanEvaluatorController {

  @Autowired private HumanEvaluatorService evaluatorService;
  @Autowired private ChatTurnService chatTurnService;

  @Operation(
      summary = "Create a new human evaluator",
      description = "Creates a new human evaluator with the provided configuration")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Evaluator created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HumanEvaluatorDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @PostMapping
  public ResponseEntity<HumanEvaluatorDTO> createEvaluator(
      @Parameter(description = "Human evaluator configuration") @RequestBody HumanEvaluatorDTO dto,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        new HumanEvaluatorDTO(
            evaluatorService.createEvaluator(
                user,
                dto.getName(),
                dto.getDescription(),
                dto.getScoringMechanismType(),
                dto.getAssociatedEntityId(),
                dto.getEntityType(),
                dto.getCategories(),
                dto.getLinkedEntityType())));
  }

  @Operation(
      summary = "Get a human evaluator by ID",
      description = "Retrieves a specific human evaluator by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Evaluator found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HumanEvaluatorDTO.class))),
        @ApiResponse(responseCode = "404", description = "Evaluator not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @GetMapping("/{evaluatorId}")
  public ResponseEntity<HumanEvaluatorDTO> getEvaluatorById(
      @Parameter(description = "ID of the evaluator to retrieve") @PathVariable String evaluatorId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        new HumanEvaluatorDTO(evaluatorService.findByIdAndUser(evaluatorId, user)));
  }

  @Operation(
      summary = "Get evaluators by entity ID",
      description = "Retrieves all human evaluators associated with a specific entity ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of evaluators found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HumanEvaluatorDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @GetMapping("/entity/{entityId}")
  public ResponseEntity<List<HumanEvaluatorDTO>> getEvaluatorsByEntityId(
      @Parameter(description = "ID of the entity") @PathVariable String entityId) {
    return ResponseEntity.ok(
        evaluatorService.getEvaluatorsByEntityId(entityId).stream()
            .map(HumanEvaluatorDTO::new)
            .collect(Collectors.toList()));
  }

  @Operation(
      summary = "Get evaluators by entity type",
      description = "Retrieves all human evaluators associated with a specific entity type")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of evaluators found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HumanEvaluatorDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @GetMapping("/type/{entityType}")
  public ResponseEntity<List<HumanEvaluatorDTO>> getEvaluatorsByEntityType(
      @Parameter(description = "Type of the entity") @PathVariable LinkedEntityType entityType) {
    return ResponseEntity.ok(
        evaluatorService.getEvaluatorsByEntityType(entityType).stream()
            .map(HumanEvaluatorDTO::new)
            .collect(Collectors.toList()));
  }

  @Operation(
      summary = "Get all evaluators with optional filters",
      description =
          "Retrieves all human evaluators for the authenticated user with optional filtering by scope type and scoring mechanism type")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of evaluators found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HumanEvaluatorDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @GetMapping
  public ResponseEntity<List<HumanEvaluatorDTO>> getEvaluators(
      @Parameter(hidden = true) @AuthenticationPrincipal User user,
      @Parameter(description = "Filter by scope type (USER or SYSTEM)")
          @RequestParam(required = false)
          ScopeType scopeType,
      @Parameter(description = "Filter by scoring mechanism type (CATEGORY, RANGE, etc.)")
          @RequestParam(required = false)
          ScoringMechanismType scoringMechanismType) {
    return ResponseEntity.ok(
        evaluatorService.getEvaluators(user, scopeType, scoringMechanismType).stream()
            .map(HumanEvaluatorDTO::new)
            .collect(Collectors.toList()));
  }

  @Operation(
      summary = "Get evaluators for a specific user",
      description = "Retrieves all human evaluators associated with a specific user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of evaluators found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HumanEvaluatorDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @GetMapping("/users/{userId}")
  public ResponseEntity<List<HumanEvaluatorDTO>> getUserEvaluators(
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        evaluatorService.getUserEvaluators(user).stream()
            .map(HumanEvaluatorDTO::new)
            .collect(Collectors.toList()));
  }

  @Operation(
      summary = "Create feedback for a chat turn",
      description = "Creates feedback for a specific chat turn using the specified evaluator")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Feedback created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScoreV2DTO.class))),
        @ApiResponse(responseCode = "404", description = "Chat turn or evaluator not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @PostMapping("/{evaluatorId}/chat-turns/{chatTurnId}/feedback")
  public ResponseEntity<HumanEvalScoreDTO> createChatTurnFeedback(
      @Parameter(description = "ID of the evaluator") @PathVariable String evaluatorId,
      @Parameter(description = "ID of the chat turn") @PathVariable String chatTurnId,
      @Parameter(description = "Score value for the feedback") @RequestParam Double score,
      @Parameter(description = "Additional notes for the feedback") @RequestParam(required = false)
          String notes,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {

    // Fetch and validate chat turn
    ChatTurn chatTurn = chatTurnService.getChatTurn(chatTurnId, user);
    if (chatTurn == null) {
      throw new NotFoundException("ChatTurn not found");
    }

    ModelResponse modelResponse = chatTurn.getModelResponse();

    if (modelResponse == null) {
      throw new NotFoundException("ModelResponse not found");
    }

    // Fetch and validate evaluator
    HumanEvaluator evaluator = evaluatorService.findByIdAndUser(evaluatorId, user);

    return ResponseEntity.ok(
        new HumanEvalScoreDTO(
            evaluatorService.createChatTurnFeedback(modelResponse, evaluator, user, score, notes)));
  }

  @Operation(
      summary = "Get feedback for a chat turn",
      description = "Retrieves all feedback associated with a specific chat turn")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of feedback found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScoreV2DTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @GetMapping("/chat-turns/{chatTurnId}/feedback")
  public ResponseEntity<List<ScoreV2DTO>> getChatTurnFeedbacks(
      @Parameter(description = "ID of the chat turn") @PathVariable String chatTurnId) {
    return ResponseEntity.ok(evaluatorService.getChatTurnFeedbacks(chatTurnId));
  }

  @Operation(
      summary = "Delete feedback",
      description = "Deletes feedback given by user for a specific human score id")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Feedback not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @DeleteMapping("/feedback/{scoreId}")
  public ResponseEntity<Void> deleteHumanEvalScore(
      @PathVariable String scoreId, @AuthenticationPrincipal User user) {
    evaluatorService.deleteHumanEvalScoreById(scoreId, user);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Update a human evaluator",
      description =
          "Updates an existing human evaluator. Note: Categories and range settings cannot be modified for CATEGORY and RANGE type evaluators")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Evaluator updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HumanEvaluatorDTO.class))),
        @ApiResponse(responseCode = "404", description = "Evaluator not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
      })
  @PutMapping("/{evaluatorId}")
  public ResponseEntity<HumanEvaluatorDTO> updateEvaluator(
      @Parameter(description = "ID of the evaluator to update") @PathVariable String evaluatorId,
      @Parameter(description = "Updated evaluator data") @RequestBody
          HumanEvaluatorUpdateDTO updateDTO,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        new HumanEvaluatorDTO(
            evaluatorService.updateEvaluator(
                evaluatorId,
                user,
                updateDTO.getName(),
                updateDTO.getDescription(),
                updateDTO.getComments(),
                updateDTO.getScoringMechanismType(),
                updateDTO.getLinkedEntityType())));
  }

  @Operation(
      summary = "Create a range-based evaluator",
      description = "Creates a new human evaluator with range-based scoring")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Range evaluator created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HumanEvaluatorDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
      })
  @PostMapping("/range")
  public ResponseEntity<HumanEvaluatorDTO> createRangeEvaluator(
      @Parameter(description = "Name of the evaluator") @RequestParam String name,
      @Parameter(description = "Description of the evaluator") @RequestParam String description,
      @Parameter(description = "Range options for the evaluator") @RequestBody
          HumanRangeOption rangeOption,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        new HumanEvaluatorDTO(
            evaluatorService.createRangeEvaluator(name, description, rangeOption, user)));
  }

  @Operation(
      summary = "Deprecate a human evaluator",
      description = "Marks a human evaluator as deprecated. This operation cannot be undone.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Evaluator deprecated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HumanEvaluatorDTO.class))),
        @ApiResponse(responseCode = "404", description = "Evaluator not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input - cannot deprecate system evaluators")
      })
  @PostMapping("/{evaluatorId}/deprecate")
  public ResponseEntity<HumanEvaluatorDTO> deprecateEvaluator(
      @Parameter(description = "ID of the evaluator to deprecate") @PathVariable String evaluatorId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        new HumanEvaluatorDTO(evaluatorService.deprecateEvaluator(evaluatorId, user)));
  }
}

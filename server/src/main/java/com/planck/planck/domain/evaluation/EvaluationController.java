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

package com.planck.planck.domain.evaluation;

import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.evaluation.dto.ChatTurnEvaluationRequest;
import com.planck.planck.domain.evaluation.dto.ChatTurnsEvaluationRequest;
import com.planck.planck.domain.evaluation.dto.EvaluationBulkRequest;
import com.planck.planck.domain.evaluation.dto.ScorerResponseDTO;
import com.planck.planck.domain.evaluation.serivce.EvaluationService;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusService;
import com.planck.planck.domain.project.dto.SXSChatRowDTO;
import com.planck.planck.entitities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Evaluation", description = "APIs related to evaluation")
@RestController
@RequestMapping("evaluations/projects/{project_id}")
public class EvaluationController {

  @Autowired private EvaluationService evaluationService;
  @Autowired private EvaluationStatusService evaluationStatusService;

  @Operation(
      summary = "Create chat turns or SXS Pairs evaluation",
      description =
          "Creates an evaluation for a set of chat turns or SXS Pairs. In case of SXS Pairs and pointwise evaluator(s) being used, the evaluation is created for each chat turn in the pair(s).")
  @ApiResponse(responseCode = "200", description = "Evaluation created successfully")
  @ApiResponse(responseCode = "400", description = "Invalid input")
  @PostMapping("/chat-turns")
  // @PreAuthorize("hasPermission(#projectId, 'PROJECT', 'write')")
  public ResponseEntity<?> createChatTurnsEvaluation(
      @Valid @RequestBody ChatTurnsEvaluationRequest request,
      @PathVariable("project_id") String projectId,
      @AuthenticationPrincipal User user) {

    ScorerResponseDTO scorerResponseDTO =
        evaluationService.generateScoresFromChatTurns(
            request.getChatTurnIds(),
            request.getPairEvaluations(),
            request.getEvaluatorIds(),
            projectId,
            user);
    return ResponseEntity.ok(scorerResponseDTO);
  }

  @Operation(
      summary = "Create chat turn evaluation or SXS Pair",
      description =
          "Creates an evaluation for a single chat turn or SXS Pair. In case of SXS Pairs and pointwise evaluator(s) being used, the evaluation is created for each chat turn in the pair(s).")
  @ApiResponse(
      responseCode = "200",
      description = "Evaluation created successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(oneOf = {ChatTurnDTO.class, SXSChatRowDTO.class})))
  @ApiResponse(responseCode = "400", description = "Invalid input")
  @PostMapping("/chat-turn")
  public ResponseEntity<?> createChatTurnEvaluation(
      @Valid @RequestBody ChatTurnEvaluationRequest request,
      @PathVariable("project_id") String projectId,
      @AuthenticationPrincipal User user) {

    Object result = evaluationService.generateScore(request, projectId, user);

    return ResponseEntity.ok(result);
  }

  @Operation(
      summary =
          "Create chat turns evaluation for all chat turns or SXS Pairs contained in a project",
      description =
          "Creates an evaluation for all chat turns or SXS Pairs contained in a project, depending on the project and evaluator type(s) used in the payload.  In case of SXS Pairs and pointwise evaluator(s) being used, the evaluation is created for each chat turn in the pair(s).")
  @ApiResponse(responseCode = "200", description = "Evaluation created successfully")
  @ApiResponse(responseCode = "400", description = "Invalid input")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @PostMapping()
  @Valid
  public ResponseEntity<ScorerResponseDTO> createFullProjectEvaluation(
      @RequestBody EvaluationBulkRequest request,
      @PathVariable("project_id") String projectId,
      @AuthenticationPrincipal User user) {
    ScorerResponseDTO scorerResponseDTO =
        evaluationService.generateScoresFromProject(projectId, request.getEvaluatorIds(), user);
    return ResponseEntity.ok(scorerResponseDTO);
  }

  @Operation(summary = "Stop specific evaluation in a project")
  @ApiResponse(responseCode = "200", description = "Evaluation stopped successfully")
  @ApiResponse(responseCode = "400", description = "Invalid input")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @PostMapping("/{id}/stop")
  public ResponseEntity<Void> stopEvaluationTask(
      @PathVariable("id") String evaluationStatusId,
      @PathVariable("projectId") String projectId,
      @AuthenticationPrincipal User user) {

    evaluationStatusService.stopEvaluation(evaluationStatusId, projectId, user);
    return ResponseEntity.ok().build();
  }
}

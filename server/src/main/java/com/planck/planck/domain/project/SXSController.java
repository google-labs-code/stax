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

package com.planck.planck.domain.project;

import com.planck.planck.annotation.RateLimited;
import com.planck.planck.domain.analytics.SXSMetricsService;
import com.planck.planck.domain.evaluation.SXSHumanFeedbackService;
import com.planck.planck.domain.importexport.SxsEvaluationPairImportService;
import com.planck.planck.domain.importexport.SxsExportService;
import com.planck.planck.domain.importexport.dto.ImportResultDTO;
import com.planck.planck.domain.importexport.dto.SxsEvaluationPairExportDTO;
import com.planck.planck.domain.importexport.dto.SxsImportRequest;
import com.planck.planck.domain.inference.dto.ContinueSxsRequestDTO;
import com.planck.planck.domain.inference.dto.GenerateTurnBRequestDTO;
import com.planck.planck.domain.inference.dto.SXSGenerateOutputs;
import com.planck.planck.domain.inference.dto.SXSGenerateOutputsBase;
import com.planck.planck.domain.inference.dto.SXSGenerateOutputsResponse;
import com.planck.planck.domain.inference.outputs.SXSGenerateOutputsService;
import com.planck.planck.domain.inference.service.SxsPlaygroundInferenceService;
import com.planck.planck.domain.project.dto.SXSChatRowDTO;
import com.planck.planck.domain.project.dto.SXSEvaluationAnalyticsDTO;
import com.planck.planck.domain.project.dto.SXSHumanEvalMetricsDTO;
import com.planck.planck.domain.project.dto.SXSHumanFeedbackRequestDTO;
import com.planck.planck.domain.project.dto.SXSPairwiseEvaluationMetricsDTO;
import com.planck.planck.domain.project.dto.SXSProjectInferenceMonitoringSummaryDTO;
import com.planck.planck.domain.project.dto.SxsCreateRequest;
import com.planck.planck.domain.project.dto.SxsEvaluationPairDTO;
import com.planck.planck.domain.project.dto.SxsEvaluationPairUpdateDTO;
import com.planck.planck.domain.project.dto.SxsInferenceRequestDTO;
import com.planck.planck.domain.project.dto.SxsPairsDeleteRequest;
import com.planck.planck.domain.workbook.dto.SXSWorkbookDTO;
import com.planck.planck.entitities.SXSHumanFeedback;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.util.DocumentationConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/sxs/containers/{containerId}")
@Tag(
    name = "Evaluation Container SXS API",
    description = "API for managing container side-by-side comparisons")
public class SXSController {
  final SxsEvaluationPairService sxsEvaluationPairService;
  final SxsExportService sxsExportService;
  private final SxsPlaygroundInferenceService sxsPlaygroundService;
  private final SXSMetricsService sxsMetricsService;
  private final SXSGenerateOutputsService sxsGenerateOutputsService;
  private final SxsEvaluationPairImportService sxsEvaluationPairImportService;
  private final SXSHumanFeedbackService sxsHumanFeedbackService;

  public SXSController(
      SxsEvaluationPairService sxsEvaluationPairService,
      SxsExportService sxsExportService,
      SxsPlaygroundInferenceService sxsPlaygroundService,
      SXSMetricsService sxsMetricsService,
      SXSGenerateOutputsService sxsGenerateOutputsService,
      SxsEvaluationPairImportService sxsEvaluationPairImportService,
      SXSHumanFeedbackService sxsHumanFeedbackService) {
    this.sxsEvaluationPairService = sxsEvaluationPairService;
    this.sxsExportService = sxsExportService;
    this.sxsPlaygroundService = sxsPlaygroundService;
    this.sxsMetricsService = sxsMetricsService;
    this.sxsGenerateOutputsService = sxsGenerateOutputsService;
    this.sxsEvaluationPairImportService = sxsEvaluationPairImportService;
    this.sxsHumanFeedbackService = sxsHumanFeedbackService;
  }

  @GetMapping("/workbook")
  @RateLimited
  @Operation(
      summary = "Retrieve SXS comparisons",
      description =
          "Get all SXS comparisons for the user under the provided container ID. Limited to 50 requests per minute",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OK - Returns a paginated list of SXS comparisons.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SXSWorkbookDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Invalid Argument - If page_size is negative or other parameters are invalid."),
        @ApiResponse(responseCode = "404", description = "Project not found for this user"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<SXSWorkbookDTO> getSXS(
      @Parameter(description = "ID of the container", example = "project-uuid4") @PathVariable
          String containerId,
      @Parameter(
              description =
                  "Maximum number of rows to return. The service may return fewer. If unspecified, defaults to 20. If less then 1 - 10000 will be applied. The maximum value is 10000; values above 10000 will be coerced to 10000.",
              required = false)
          @RequestParam(required = false, name = "page_size", defaultValue = "20")
          int pageSize,
      @Parameter(
              description =
                  "A page token, received from a previous list call. Provide this to retrieve the subsequent page. When paginating, all other parameters provided must match the call that provided the page token.",
              required = false)
          @RequestParam(required = false, name = "page_token", defaultValue = "0")
          int pageToken,
      @Parameter(
              description =
                  "Sort order conforming to AIP-132 (e.g., 'input asc,created_at desc, inference_tokens'). Use snake_case for field names. Default value is  \"created_at desc\"",
              required = false)
          @RequestParam(required = false, name = "order_by", defaultValue = "created_at desc")
          String orderBy,
      @Parameter(
              description =
                  "Filter expression conforming to AIP-160 (e.g., 'input = \"value\" AND inference_latency > 0.5'). Use snake_case for field names. Supports logical (AND, OR, NOT, -), comparison (=, !=, <, >, <=, >=), traversal (.), and has (:) operators.",
              required = false)
          @RequestParam(required = false)
          String filter,
      @Parameter(
              description =
                  "Optionally filter the SXS comparisons by a list of tagId. If provided, only SXS comparisons matching all of the provided tag will be retrieved",
              required = false)
          @RequestParam(required = false)
          List<String> tagIds,
      @AuthenticationPrincipal User user) {
    SXSWorkbookDTO response =
        sxsEvaluationPairService.getSXSRows(
            user, containerId, pageSize, pageToken, orderBy, filter, tagIds);

    sxsHumanFeedbackService.setHumanFeedbackForSxsRows(response.getSxsRows(), user);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/workbook/{pairId}")
  @Operation(
      summary = "Retrieve SXS comparisons for a specific pair",
      description =
          "Get all SXS comparisons for the user under the provided container ID. Limited to 50 requests per minute")
  public ResponseEntity<List<SXSChatRowDTO>> getSXSForPair(
      @Parameter(description = "ID of the container", example = "project-uuid4") @PathVariable
          String containerId,
      @Parameter(description = "ID of the SxS pair", example = "sxs-pair-uuid4") @PathVariable
          String pairId,
      @AuthenticationPrincipal User user) {
    List<SXSChatRowDTO> response =
        sxsEvaluationPairService.getSXSRowsForPair(user, containerId, pairId);

    sxsHumanFeedbackService.setHumanFeedbackForSxsRows(response, user);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{pairId}/human-feedback")
  @Operation(
      summary = "Submit human feedback for an SxS pair",
      description =
          "Create or update human feedback for an SxS pair. A null value in either of the fields will delete the respective content of it.")
  public ResponseEntity<SXSChatRowDTO> submitHumanFeedback(
      @AuthenticationPrincipal User user,
      @PathVariable String containerId,
      @PathVariable String pairId,
      @Valid @RequestBody SXSHumanFeedbackRequestDTO request) {
    SXSHumanFeedback humanFeedback =
        sxsHumanFeedbackService.submitHumanFeedback(user, containerId, pairId, request);
    SxsEvaluationPair pair = sxsEvaluationPairService.getSxsPairById(pairId, user);
    return ResponseEntity.ok(new SXSChatRowDTO(pair, humanFeedback));
  }

  @DeleteMapping("/{pairId}/human-feedback")
  @Operation(
      summary = "Delete human feedback for an SxS pair",
      description = "Delete human feedback for an SxS pair")
  public ResponseEntity<Void> deleteHumanFeedback(
      @AuthenticationPrincipal User user,
      @PathVariable String containerId,
      @PathVariable String pairId) {
    sxsHumanFeedbackService.deleteHumanFeedback(user, containerId, pairId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{pairId}/human-feedback/{chatTurnA}/{chatTurnB}")
  @Operation(
      summary = "Delete human feedback for an SxS pair for specific chat turns",
      description = "Delete human feedback for an SxS pair for specific chat turns")
  public ResponseEntity<Void> deleteHumanFeedbackForChatTurn(
      @AuthenticationPrincipal User user,
      @PathVariable String containerId,
      @PathVariable String pairId,
      @PathVariable String chatTurnA,
      @PathVariable String chatTurnB) {
    sxsHumanFeedbackService.deleteHumanFeedbackForChatTurn(
        user, containerId, pairId, chatTurnA, chatTurnB);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{pairId}/generate-b")
  @Operation(
      summary = "Generate and set Side B for an SxS pair",
      description =
          "Takes an existing Side-by-Side (SxS) evaluation pair that only has a Side A conversation. "
              + "In case there is no Side B, it duplicates the entire conversation history from Side A, "
              + "with the specified new model (modelId), and populates the result as the Side B conversation. "
              + "In case there is a Side B, it will just assign the new model to the existing Side B provided that there is no response yet.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "OK - Side B was successfully generated and the updated SxS pair is returned.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SxsEvaluationPairDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - The request body is invalid (e.g., modelId is missing)."),
        @ApiResponse(
            responseCode = "404",
            description =
                "Not Found - The specified container or SxS pair does not exist for this user."),
        @ApiResponse(
            responseCode = "500",
            description =
                "Internal Server Error - An unexpected error occurred during inference or processing.")
      })
  public ResponseEntity<SxsEvaluationPairDTO> generateTurnB(
      @AuthenticationPrincipal User user,
      @Parameter(description = "ID of the container", example = "project-uuid4") @PathVariable
          String containerId,
      @Parameter(description = "ID of the SxS pair to modify", example = "sxs-pair-uuid4")
          @PathVariable
          String pairId,
      @Valid @RequestBody GenerateTurnBRequestDTO request) {

    SxsEvaluationPair updatedPair =
        sxsEvaluationPairService.generateAndSetTurnB(
            user, containerId, pairId, request.getModelId(), true);

    return ResponseEntity.ok(new SxsEvaluationPairDTO(updatedPair));
  }

  @PostMapping("/inference")
  @Operation(
      summary = "Start a new SxS comparison",
      description =
          "Creates a new Side-by-Side (SxS) evaluation pair by running an initial inference for two models. "
              + "The request must provide IDs for both Model A and Model B, along with the initial prompts. "
              + "The service will generate the first conversation turn for each model based on the prompts and return the newly created SxS pair.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description =
                "Created - The new SxS pair was successfully created and is returned in the response body as SXSChatRowDTO.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SXSChatRowDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Bad Request - The request body is invalid (e.g., modelIdA, modelIdB, or prompts are missing)."),
        @ApiResponse(
            responseCode = "404",
            description =
                "Not Found - The specified container or one of the model IDs does not exist."),
        @ApiResponse(
            responseCode = "500",
            description =
                "Internal Server Error - An unexpected error occurred during the inference process.")
      })
  public ResponseEntity<SXSChatRowDTO> startSxsInference(
      @AuthenticationPrincipal User user,
      @Parameter(description = "ID of the container", example = "project-uuid4") @PathVariable
          String containerId,
      @Valid @RequestBody SxsInferenceRequestDTO request) {

    SxsEvaluationPair pair =
        sxsPlaygroundService.runInitialSxsInference(user, containerId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(new SXSChatRowDTO(pair, null));
  }

  @PostMapping("/{pairId}/continue")
  @Operation(
      summary = "Continue a conversation in an SxS pair",
      description =
          "Continues a conversation within an SxS pair by adding a new turn based on the provided prompts.\n\n"
              + "* **Side A:** Is always continued with the new prompt.\n"
              + "* **Side B:** If Side B already exists, it is also continued with the new prompt.\n"
              + "* **Dynamic Side B Creation:** If Side B does not exist, you can provide an optional `modelIdB` in the request. "
              + "If provided, the system will first generate Side B by replaying Side A's history against the new model, "
              + "and then continue both conversations with the new prompt. "
              + "This allows for creating a full comparison on-the-fly from a single-sided conversation.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description =
                "OK - The conversation was successfully continued and the updated SxS pair is returned.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SXSChatRowDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - The request body is invalid (e.g., prompts are missing)."),
        @ApiResponse(
            responseCode = "404",
            description =
                "Not Found - The specified container or SxS pair does not exist for this user."),
        @ApiResponse(
            responseCode = "500",
            description =
                "Internal Server Error - An unexpected error occurred during inference or processing.")
      })
  public ResponseEntity<SXSChatRowDTO> continueSxsEvaluation(
      @AuthenticationPrincipal User user,
      @Parameter(description = "ID of the container", example = "project-uuid4") @PathVariable
          String containerId,
      @Parameter(description = "ID of the SxS pair to continue", example = "sxs-pair-uuid4")
          @PathVariable
          String pairId,
      @Valid @RequestBody ContinueSxsRequestDTO request) {

    SxsEvaluationPair updatedPair =
        sxsPlaygroundService.continueSxsEvaluation(user, containerId, pairId, request);

    return ResponseEntity.ok(new SXSChatRowDTO(updatedPair, null));
  }

  @GetMapping("/human-eval-metrics")
  @Operation(
      summary = "Get human evaluation metrics for an SxS container",
      description =
          "Get human evaluation metrics for an SxS container, indicating which side has better performance")
  public ResponseEntity<SXSHumanEvalMetricsDTO> getHumanEvalMetrics(
      @Parameter(description = "ID of the container", example = "project-uuid4") @PathVariable
          String containerId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(sxsMetricsService.getHumanEvalMetrics(containerId, user));
  }

  @GetMapping("/sxs-eval-metrics")
  @Operation(
      summary = "Get SxS evaluation metrics for an SxS container",
      description =
          "Get SxS evaluation metrics for an SxS container, indicating which side has better performance")
  public ResponseEntity<List<SXSPairwiseEvaluationMetricsDTO>> getSxsEvalMetrics(
      @Parameter(description = "ID of the container", example = "project-uuid4") @PathVariable
          String containerId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(sxsMetricsService.getSxsEvalMetrics(containerId, user));
  }

  @GetMapping("/inference-metrics")
  @Operation(
      summary = "Get inference monitoring metrics for an SxS container",
      description =
          "Get inference monitoring metrics for an SxS container, separated by side A and side B")
  public ResponseEntity<SXSProjectInferenceMonitoringSummaryDTO> getInferenceMetrics(
      @Parameter(description = "ID of the container", example = "project-uuid4") @PathVariable
          String containerId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(sxsMetricsService.getInferenceMetrics(containerId, user));
  }

  @GetMapping("/evaluation-analytics/{scorerId}")
  @Operation(
      summary = "Get evaluation analytics for an SxS container by scorer",
      description =
          "Retrieves evaluation analytics for the specified SxS container and scorer, separated by side A and side B with delta calculations")
  public ResponseEntity<SXSEvaluationAnalyticsDTO> getEvaluationAnalyticsByScorer(
      @AuthenticationPrincipal User user,
      @PathVariable String containerId,
      @PathVariable String scorerId) {
    return ResponseEntity.ok(
        sxsMetricsService.getEvaluationAnalyticsByScorer(containerId, scorerId, user));
  }

  @GetMapping("/evaluation-analytics")
  @Operation(
      summary = "Get evaluation analytics for all scorers in an SxS container",
      description =
          "Retrieves evaluation analytics for all available scorers in the specified SxS container, separated by side A and side B with delta calculations")
  public ResponseEntity<List<SXSEvaluationAnalyticsDTO>> getEvaluationAnalyticsForAllScorers(
      @AuthenticationPrincipal User user, @PathVariable String containerId) {
    return ResponseEntity.ok(
        sxsMetricsService.getEvaluationAnalyticsForAllScorers(containerId, user));
  }

  @GetMapping("/{pairId}/export")
  @Operation(
      summary = "Export a single SxS evaluation pair",
      description =
          "Retrieves and formats a side-by-side evaluation pair for export, including full chat histories.")
  public SxsEvaluationPairExportDTO exportSxsPair(
      @Parameter(
              description = "ID of the SxS evaluation pair to export",
              example = "sxs-pair-12345")
          @PathVariable
          String pairId,
      @PathVariable String containerId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return sxsExportService.exportSxsPair(pairId, user, containerId);
  }

  @GetMapping("/export")
  @Operation(
      summary = "Bulk export of SxS evaluation pairs for the whole container",
      description =
          "Exports all SxS pairs in a container of type 'SXS'. Each pair is exported with its full chat histories. Returns a list of exported pairs.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully exported the SxS pairs",
            content =
                @Content(
                    mediaType = "application/json",
                    array =
                        @ArraySchema(
                            schema = @Schema(implementation = SxsEvaluationPairExportDTO.class)))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request (e.g., container is not of type 'SXS')"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public List<SxsEvaluationPairExportDTO> exportSxsPairs(
      @PathVariable String containerId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return sxsExportService.exportSxsPairsForProject(containerId, user);
  }

  @PostMapping("/generate-outputs/all")
  @Operation(
      summary = "Generate outputs for all SXS pairs under the container.",
      description = DocumentationConstants.SXS_GENERATE_OUTPUTS_DESCRIPTION)
  public ResponseEntity<SXSGenerateOutputsResponse> generateAllOutputs(
      @Valid @RequestBody SXSGenerateOutputsBase request,
      @Parameter(description = "ID of the container", example = "project-uuid4") @PathVariable
          String containerId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        sxsGenerateOutputsService.generateAllOutputs(containerId, request, user));
  }

  @PostMapping("/generate-outputs")
  @Operation(
      summary = "Generate outputs for SXS pairs",
      description = DocumentationConstants.SXS_GENERATE_OUTPUTS_DESCRIPTION)
  public ResponseEntity<SXSGenerateOutputsResponse> generateOutputs(
      @Valid @RequestBody SXSGenerateOutputs request,
      @Parameter(description = "ID of the container", example = "project-uuid4") @PathVariable
          String containerId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(sxsGenerateOutputsService.generateOutputs(containerId, request, user));
  }

  @PostMapping("/export")
  @Operation(
      summary = "Bulk export of SxS pairs by ID list",
      description =
          "Exports multiple SxS evaluation pairs from a specific container by their unique identifiers. "
              + "Each pair is exported with its full chat histories.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully exported the SxS pairs",
            content =
                @Content(
                    mediaType = "application/json",
                    array =
                        @ArraySchema(
                            schema = @Schema(implementation = SxsEvaluationPairExportDTO.class)))),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public List<SxsEvaluationPairExportDTO> exportSxsPairsByIds(
      @Parameter(description = "ID of the container containing the pairs") @PathVariable
          String containerId,
      @Parameter(
              description =
                  "List of SxS pair IDs to export. Sent as a JSON array in the request body.",
              example = "[\"sxs-pair-123\", \"sxs-pair-456\"]")
          @RequestBody
          List<String> pairIds,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return sxsExportService.exportSxsPairsByIds(containerId, pairIds, user);
  }

  @PatchMapping("/{pairId}")
  @Operation(
      summary = "Update an SxS evaluation pair",
      description =
          "Partially updates an SxS pair and its related chats (A and B). "
              + "Only include the fields you want to change. "
              + "Updating 'input' is only allowed if neither chat turn has a model response yet.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully updated the pair"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public SXSChatRowDTO patchSxsPair(
      @Parameter(description = "ID of the container containing the pairs") @PathVariable
          String containerId,
      @Parameter(description = "ID of the SxS evaluation pair to update") @PathVariable
          String pairId,
      @RequestBody SxsEvaluationPairUpdateDTO updateRequest,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    SxsEvaluationPair sxsEvaluationPair =
        sxsEvaluationPairService.updateSxsPair(
            pairId,
            containerId,
            user,
            updateRequest.getInput(),
            updateRequest.getExpectedOutput(),
            updateRequest.getVariables(),
            updateRequest.getTags());
    SXSHumanFeedback humanFeedback = sxsHumanFeedbackService.getHumanFeedback(user, pairId);
    return new SXSChatRowDTO(sxsEvaluationPair, humanFeedback);
  }

  @Operation(
      summary = "Create a new side-by-side pair",
      description =
          "Creates an SxsPair with two associated chats (A and B) and their initial chat turns. "
              + "The initial prompt can be constructed from an optional input and/or expected output.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true)))
      })
  @PostMapping
  public ResponseEntity<SxsEvaluationPairDTO> createSxsPair(
      @AuthenticationPrincipal User user,
      @PathVariable String containerId,
      @RequestBody SxsCreateRequest request) {
    Prompt prompt = (request != null) ? request.getPrompt() : null;
    String expectedOutput = (request != null) ? request.getExpectedOutput() : null;

    SxsEvaluationPair pair =
        sxsEvaluationPairService.createSxsPair(user, containerId, prompt, expectedOutput);

    return ResponseEntity.ok(new SxsEvaluationPairDTO(pair));
  }

  @DeleteMapping("/{pairId}")
  @Operation(
      summary = "Delete a single SxS evaluation pair",
      description = "Deletes a specific Side-by-Side pair and all its associated data.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<Void> deleteSxsPair(
      @Parameter(description = "ID of the container") @PathVariable String containerId,
      @Parameter(description = "ID of the SxS pair to delete") @PathVariable String pairId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    sxsEvaluationPairService.deleteSxsPair(pairId, user);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/all")
  @Operation(
      summary = "Bulk delete SxS evaluation pairs by container",
      description =
          "Deletes multiple Side-by-Side pairs. ALL pairs within the container are deleted.",
      responses = {
        @ApiResponse(
            responseCode = "204",
            description = "All the pairs by container were successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<Void> deleteSxsPairs(
      @Parameter(description = "ID of the container") @PathVariable String containerId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {

    sxsEvaluationPairService.deleteSxsPairsByContainer(containerId, user);

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  @Operation(
      summary = "Delete multiple SxS evaluation pairs by a list of IDs",
      description =
          "Deletes a specific set of Side-by-Side pairs using a list of IDs provided in the request body.",
      responses = {
        @ApiResponse(
            responseCode = "204",
            description = "Successfully deleted the specified pairs"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<Void> deleteSxsPairsByBody(
      @Parameter(description = "ID of the container") @PathVariable String containerId,
      @Valid @RequestBody SxsPairsDeleteRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {

    sxsEvaluationPairService.deleteSxsPairsByIds(request.getPairIds(), user);

    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/import")
  @Operation(
      summary = "Import SxS evaluation pairs from a CSV file",
      description =
          "Imports multiple SxS pairs from a CSV file. Requires columns for Chat A and optionally for Chat B.")
  public ResponseEntity<ImportResultDTO> importSxsPairs(
      @RequestParam("file") MultipartFile file,
      @Parameter(hidden = true) @AuthenticationPrincipal User user,
      @PathVariable String containerId,
      @RequestParam(name = "chat_a_column_name", required = false) String chatAColumnName,
      @RequestParam(name = "chat_b_column_name", required = false) String chatBColumnName,
      @RequestParam(name = "input_column_name", required = false) String inputColumnName,
      @RequestParam(name = "system_instruction_a_column_name", required = false)
          String systemInstructionAColumnName,
      @RequestParam(name = "system_instruction_b_column_name", required = false)
          String systemInstructionBColumnName,
      @RequestParam(name = "output_a_column_name", required = false) String outputAColumnName,
      @RequestParam(name = "output_b_column_name", required = false) String outputBColumnName,
      @RequestParam(name = "model_label_a_column_name", required = false)
          String modelLabelAColumnName,
      @RequestParam(name = "model_label_b_column_name", required = false)
          String modelLabelBColumnName,
      @RequestParam(name = "tags_column_name", required = false) String tagsColumnName,
      @RequestParam(name = "llm_evaluations_a_column_name", required = false)
          String llmEvaluationsAColumnName,
      @RequestParam(name = "llm_evaluations_b_column_name", required = false)
          String llmEvaluationsBColumnName,
      @RequestParam(name = "inference_analytics_a_column_name", required = false)
          String inferenceAnalyticsAColumnName,
      @RequestParam(name = "inference_analytics_b_column_name", required = false)
          String inferenceAnalyticsBColumnName,
      @RequestParam(name = "variables_column_name", required = false) String variablesColumnName,
      @RequestParam(name = "human_sxs_rating_column_name", required = false)
          String ratingColumnName,
      @RequestParam(name = "human_sxs_notes_column_name", required = false) String notesColumnName,
      @RequestParam(name = "expected_output_column_name", required = false)
          String expectedOutputColumnName) {

    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body(new ImportResultDTO());
    }

    try {

      SxsImportRequest importRequest =
          new SxsImportRequest(
              file,
              user,
              containerId,
              chatAColumnName,
              chatBColumnName,
              inputColumnName,
              systemInstructionAColumnName,
              systemInstructionBColumnName,
              outputAColumnName,
              outputBColumnName,
              modelLabelAColumnName,
              modelLabelBColumnName,
              tagsColumnName,
              llmEvaluationsAColumnName,
              llmEvaluationsBColumnName,
              inferenceAnalyticsAColumnName,
              inferenceAnalyticsBColumnName,
              variablesColumnName,
              ratingColumnName,
              notesColumnName,
              expectedOutputColumnName);

      ImportResultDTO result = sxsEvaluationPairImportService.importSxsPairs(importRequest);

      return ResponseEntity.ok(result);

    } catch (RuntimeException e) {

      return ResponseEntity.badRequest().body(new ImportResultDTO());
    }
  }
}

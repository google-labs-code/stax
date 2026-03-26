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

import com.planck.planck.Status200Response;
import com.planck.planck.domain.analytics.evaluation.dto.ProjectEvaluationAnalyticsByScorer;
import com.planck.planck.domain.analytics.inference.dto.ProjectInferenceMonitoringSummaryDTO;
import com.planck.planck.domain.importexport.ChatExportService;
import com.planck.planck.domain.importexport.ChatImportService;
import com.planck.planck.domain.importexport.dto.ChatExportDTO;
import com.planck.planck.domain.importexport.dto.ChatImportRequest;
import com.planck.planck.domain.importexport.dto.ImportResultDTO;
import com.planck.planck.domain.project.dto.CreateProject;
import com.planck.planck.domain.project.dto.CreateProjectCommand;
import com.planck.planck.domain.project.dto.HumanEvalMetricsDTO;
import com.planck.planck.domain.project.dto.ListProjectsResponse;
import com.planck.planck.domain.project.dto.ProjectDTO;
import com.planck.planck.domain.project.dto.UpdateProjectCommand;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ChatTurnContainerType;
import com.planck.planck.enums.EvaluationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/projects"})
@AllArgsConstructor
@Tag(name = "Project API", description = "API for managing projects")
public class ProjectController {

  @Autowired private ProjectService projectService;
  @Autowired private ChatExportService chatExportService;
  @Autowired private ChatImportService chatImportService;
  @Autowired private EvaluationContainerService evaluationContainerService;

  @GetMapping({"", "/"})
  @Operation(
      summary = "List projects",
      description = "Retrieves a paginated list of projects for the authenticated user.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved projects"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<ListProjectsResponse> listProjects(
      @AuthenticationPrincipal User user,
      @RequestParam(name = "page_size", defaultValue = "10")
          @Validated
          @Min(value = 1, message = "page_size should be positive.")
          @Max(value = 200, message = "page_size exceeds 200.")
          int pageSize,
      @Parameter(description = "Page number for pagination", schema = @Schema(defaultValue = "0"))
          @RequestParam(name = "page_number", defaultValue = "0")
          @Validated
          @Min(value = 0, message = "page_number is negative.")
          int pageNumber,
      @Parameter(description = "Optional filter by project type (POINTWISE or SXS)")
          @RequestParam(name = "type", required = false)
          EvaluationType type,
      @Parameter(
              description =
                  "Comma-separated list of fields to include. Valid values: 'modelProviders', 'inferenceMetrics', 'humanEvalMetrics', 'latestEvalScore', 'jobStatuses', or 'all' to include all fields. Example: 'modelProviders,inferenceMetrics' or 'all'")
          @RequestParam(name = "include", required = false)
          String include) {

    ListProjectsResponse listProjectResponse =
        projectService.getAllProjectsSortedByIsDefaultAndUpdatedAt(
            user, pageSize, pageNumber, type, include);
    return ResponseEntity.ok(listProjectResponse);
  }

  @GetMapping("/{projectId}")
  @Operation(
      summary = "Get project by ID",
      description = "Retrieves a project by its ID for the authenticated user.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved project"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<ProjectDTO> getProject(
      @AuthenticationPrincipal User user,
      @PathVariable String projectId,
      @Parameter(
              description =
                  "Comma-separated list of fields to include. Valid values: 'modelProviders', 'inferenceMetrics', 'humanEvalMetrics', 'jobStatuses', or 'all' to include all fields. Example: 'modelProviders,inferenceMetrics' or 'all'")
          @RequestParam(name = "include", required = false)
          String include) {

    ProjectDTO project = projectService.getProject(user, projectId, include);
    return ResponseEntity.ok(project);
  }

  @PostMapping({"", "/"})
  @Operation(
      summary = "Create a new project",
      description = "Creates a new project for the authenticated user.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully created project"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<ProjectDTO> createProject(
      @Validated(CreateProject.class) @RequestBody ProjectDTO projectDTO,
      @AuthenticationPrincipal User user) {
    var command =
        new CreateProjectCommand(
            projectDTO.getProjectId(),
            projectDTO.getName(),
            projectDTO.getDescription(),
            false,
            projectDTO.getType());

    return ResponseEntity.ok(projectService.createProject(user, command));
  }

  @DeleteMapping("/{projectId}")
  @Operation(
      summary = "Delete project by ID",
      description = "Deletes a project by its ID for the authenticated user.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully deleted project"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<Status200Response> deleteProject(
      @AuthenticationPrincipal User user, @PathVariable String projectId) {

    evaluationContainerService.deleteContainer(user, projectId);
    return ResponseEntity.ok(
        Status200Response.builder().message(projectId + " was deleted successfully").build());
  }

  @PatchMapping({"/{projectId}"})
  @Operation(
      summary = "Update project by ID",
      description = "Updates a project by its ID for the authenticated user.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully updated project"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<ProjectDTO> updateProject(
      @AuthenticationPrincipal User user,
      @PathVariable String projectId,
      @Validated @RequestBody ProjectDTO project) {

    var command = new UpdateProjectCommand(project.getName(), project.getDescription());

    ProjectDTO updatedProject =
        projectService.updateProjectIgnoreOutputOnlyFields(projectId, command, user);

    return ResponseEntity.ok(updatedProject);
  }

  @GetMapping("/{projectId}/export")
  @Operation(
      summary = "Bulk export of chats for the whole project",
      description =
          "Exports all chats in project. Each chat is exported in the same structure as a single chat export. Returns a list of exported chats.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully exported the chats",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChatExportDTO.class))),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public List<ChatExportDTO> exportChats(
      @PathVariable String projectId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return chatExportService.exportAllChats(projectId, user);
  }

  @GetMapping("/{projectId}/metrics-summary")
  @Operation(
      summary = "Get aggregated inference monitoring summary for a project",
      description = "Returns aggregated inference monitoring metrics for the specified project.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved metrics summary",
            content =
                @Content(
                    schema = @Schema(implementation = ProjectInferenceMonitoringSummaryDTO.class))),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<ProjectInferenceMonitoringSummaryDTO> getProjectMetricsSummary(
      @AuthenticationPrincipal User user, @PathVariable String projectId) {
    ProjectInferenceMonitoringSummaryDTO summary =
        projectService.getProjectInferenceMonitoringSummary(user, projectId);
    return ResponseEntity.ok(summary);
  }

  @PostMapping(
      value = "/{projectId}/import",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  @Operation(
      summary = "Import chats from a CSV/JSON file",
      description =
          "Imports multiple chats from a CSV/JSON file where each row represents one chat. The file should be sent as 'multipart/form-data' with the key 'file'. Depends on the format (JSON or CSV) the file will be processed accordingly",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully imported the chats. Returns the number of imported chats."),
        @ApiResponse(
            responseCode = "400",
            description =
                "Bad Request: File is empty, not a valid CSV/JSON, or content is malformed."),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden: User does not have permission."),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error during the import process.")
      })
  public ResponseEntity<ImportResultDTO> importChats(
      @RequestParam("file") MultipartFile file,
      @Parameter(hidden = true) @AuthenticationPrincipal User user,
      @PathVariable String projectId,
      @RequestParam(name = "chat_column_name", required = false) String chatColumnName,
      @RequestParam(name = "variables_column_names", required = false)
          String variablesColumnNamesCsv,
      @RequestParam(name = "input_column_name", required = false) String inputColumnName,
      @RequestParam(name = "output_column_name", required = false) String outputColumnName,
      @RequestParam(name = "expected_output_name", required = false)
          String expectedOutputColumnName,
      @RequestParam(name = "tags_column_name", required = false) String tagsColumnName,
      @RequestParam(name = "system_instruction_column_name", required = false)
          String systemInstructionColumnName,
      @RequestParam(name = "model_label_column_name", required = false) String modelLabelColumnName,
      @RequestParam(name = "human_eval_score_column_name", required = false)
          String humanEvalScoreColumnName,
      @RequestParam(name = "human_eval_score_notes_column_name", required = false)
          String humanEvalScoreNotesColumnName,
      @RequestParam(name = "inference_analytics_column_name", required = false)
          String inferenceAnalyticsColumnName,
      @RequestParam(name = "llm_evaluations_column_name", required = false)
          String llmEvaluationColumnName) {
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body(new ImportResultDTO());
    }

    List<String> variablesColumnNames = new ArrayList<>();
    if (variablesColumnNamesCsv != null && !variablesColumnNamesCsv.isBlank()) {
      variablesColumnNames = Arrays.asList(variablesColumnNamesCsv.split("\\s*,\\s*"));
    }

    if (variablesColumnNames.size() > 10) {
      throw new IllegalArgumentException("The number of variables columns cannot exceed 10.");
    }

    try {
      ChatImportRequest importRequest =
          new ChatImportRequest(
              file,
              user,
              ChatTurnContainerType.PROJECT,
              projectId,
              chatColumnName,
              variablesColumnNames,
              inputColumnName,
              outputColumnName,
              expectedOutputColumnName,
              tagsColumnName,
              systemInstructionColumnName,
              modelLabelColumnName,
              humanEvalScoreColumnName,
              humanEvalScoreNotesColumnName,
              inferenceAnalyticsColumnName,
              llmEvaluationColumnName);

      ImportResultDTO importedChats = chatImportService.importChatsFromFile(importRequest);

      return ResponseEntity.ok(importedChats);

    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new ImportResultDTO());
    }
  }

  @GetMapping("/{projectId}/human-eval-pass-rate")
  @Operation(
      summary = "Get human evaluation pass rate for a project",
      description =
          "Retrieves only the human evaluation metrics (pass rate and score counts) for the specified project.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved human evaluation metrics",
            content = @Content(schema = @Schema(implementation = HumanEvalMetricsDTO.class))),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<HumanEvalMetricsDTO> getHumanEvalPassRate(
      @AuthenticationPrincipal User user, @PathVariable String projectId) {
    return ResponseEntity.ok(projectService.getHumanEvalMetrics(user, projectId));
  }

  @GetMapping("/{projectId}/eval-analytics/{scorerId}")
  @Operation(
      summary = "Get evaluation analytics for a project",
      description = "Retrieves evaluation analytics for the specified project and scorer.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved evaluation analytics"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<ProjectEvaluationAnalyticsByScorer> getProjectEvaluationAnalyticsByScorer(
      @AuthenticationPrincipal User user,
      @PathVariable String projectId,
      @PathVariable String scorerId) {
    return ResponseEntity.ok(
        projectService.getProjectEvaluationAnalyticsByScorer(user, projectId, scorerId));
  }

  @GetMapping("/{projectId}/eval-analytics")
  @Operation(
      summary = "Get evaluation analytics for all scorers in a project",
      description =
          "Retrieves evaluation analytics for all available scorers in the specified project.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved evaluation analytics for all scorers"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<List<ProjectEvaluationAnalyticsByScorer>>
      getProjectEvaluationAnalyticsForAllScorers(
          @AuthenticationPrincipal User user, @PathVariable String projectId) {
    return ResponseEntity.ok(
        projectService.getProjectEvaluationAnalyticsForAllScorers(user, projectId));
  }
}

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

package com.planck.planck.domain.workbook;

import com.planck.planck.annotation.RateLimited;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.workbook.dto.ChatTurnUpdateRequest;
import com.planck.planck.domain.workbook.dto.ChatTurnWorkbookRowDTO;
import com.planck.planck.domain.workbook.dto.GetWorkbookResponseDTO;
import com.planck.planck.domain.workbook.dto.WorkBookClearResultsRequest;
import com.planck.planck.domain.workbook.dto.WorkBookDeleteRowsRequest;
import com.planck.planck.domain.workbook.dto.WorkBookRowCreateRequest;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.User;
import com.planck.planck.llmproviders.dto.Prompt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workbook/projects/{projectId}")
@Tag(name = "Project Workbook API", description = "API for managing project workbooks")
public class WorkbookController {

  @Autowired WorkbookService workbookService;

  @Autowired ChatService chatService;

  @GetMapping
  @RateLimited
  @Operation(
      summary = "Retrieve workbooks",
      description =
          "Get all workbooks for the user under the provided project ID. Limited to 50 requests per minute",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OK - Returns a paginated list of workbook rows.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GetWorkbookResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Invalid Argument - If page_size is negative or other parameters are invalid."),
        @ApiResponse(responseCode = "404", description = "Project not found for this user"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<GetWorkbookResponseDTO> getWorkbook(
      @Parameter(description = "ID of the project", example = "project-uuid4") @PathVariable
          String projectId,
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
                  "Optionally filter the chat turns by a list of tagId. If provided, only chat turns matching all of the provided tag will be retrieved",
              required = false)
          @RequestParam(required = false)
          List<String> tagIds,
      @AuthenticationPrincipal User user) {
    GetWorkbookResponseDTO response =
        workbookService.getData(user, projectId, pageSize, pageToken, orderBy, filter, tagIds);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Create project row with prompt only and optional model",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "Project not found for this user",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true)))
      })
  @PostMapping("/row")
  public ResponseEntity<ChatTurnWorkbookRowDTO> createProjectRow(
      @AuthenticationPrincipal User user,
      @RequestBody WorkBookRowCreateRequest input,
      @PathVariable String projectId) {
    Prompt prompt = (input != null) ? input.getPrompt() : null;
    String modelId = prompt != null ? input.getModelId() : null;
    ChatTurn chatTurn = chatService.createChatTurn(user, projectId, null, prompt, null, modelId);
    return ResponseEntity.ok(new ChatTurnWorkbookRowDTO(chatTurn));
  }

  @Operation(
      summary = "Update project row with input or model.",
      description =
          "Updates input and model id if provided. Model Id can be updated only if no response output is generated.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "ChatTurn not found",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true)))
      })
  @PatchMapping("/rows/{chatTurnId}")
  public ResponseEntity<ChatTurnWorkbookRowDTO> updateProjectRow(
      @AuthenticationPrincipal User user,
      @PathVariable String projectId,
      @PathVariable String chatTurnId,
      @Validated @RequestBody ChatTurnUpdateRequest request) {

    ChatTurn updatedChatTurn =
        chatService.updateTurn(
            user,
            projectId,
            chatTurnId,
            request.getPrompt(),
            request.getModelResponse(),
            request.getModelId(),
            request.getExpectedOutput());
    return ResponseEntity.ok(new ChatTurnWorkbookRowDTO(updatedChatTurn));
  }

  @DeleteMapping("/delete-rows")
  @RateLimited
  @Operation(
      summary = "Delete chat rows",
      description = "Deletes provided list of chat ids from the database",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid payload provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<?> deleteChats(
      @Validated @RequestBody WorkBookDeleteRowsRequest request,
      @Parameter(description = "ID of the project", example = "project-uuid4") @PathVariable
          String projectId,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(workbookService.getDeleteChats(user, projectId, request.getChatIds()));
  }

  @DeleteMapping("/delete-rows/all")
  @RateLimited
  @Operation(
      summary = "Delete chat rows",
      description = "Deletes provided list of chat ids from the database",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid payload provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<?> deleteAllChats(
      @Parameter(description = "ID of the project", example = "project-uuid4") @PathVariable
          String projectId,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(workbookService.deleteChatsByProject(user, projectId));
  }

  @DeleteMapping("/clear-results")
  @RateLimited
  @Operation(
      summary = "Clear outputs for chat rows",
      description =
          "Deletes LLM Outputs, evaluation and inference related data from the provided chat turns",
      responses = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted"),
        @ApiResponse(responseCode = "400", description = "Invalid payload provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public void clearResults(
      @Validated @RequestBody WorkBookClearResultsRequest request,
      @Parameter(description = "ID of the project", example = "project-uuid4") @PathVariable
          String projectId,
      @AuthenticationPrincipal User user) {
    chatService.deleteTurnResultsByTurnIds(user, projectId, request.getChatTurnIds());
  }

  @DeleteMapping("/clear-results/all")
  @RateLimited
  @Operation(
      summary = "Clear results for all of the latest project chat rows",
      description =
          "Deletes LLM Results, evaluation and inference related data from the latest chat turns of the project",
      responses = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted"),
        @ApiResponse(responseCode = "400", description = "Invalid payload provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public void clearResultsForProject(
      @Parameter(description = "ID of the project", example = "project-uuid4") @PathVariable
          String projectId,
      @AuthenticationPrincipal User user) {
    chatService.deleteTurnResultsByProject(user, projectId);
  }
}

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

package com.planck.planck.domain.inference;

import com.planck.planck.annotation.RateLimited;
import com.planck.planck.domain.inference.dto.BulkInferenceForProjectRequest;
import com.planck.planck.domain.inference.dto.BulkInferenceRequest;
import com.planck.planck.domain.inference.dto.QuickCompareInferenceRequest;
import com.planck.planck.domain.inference.service.InferenceService;
import com.planck.planck.domain.inference.service.PlaygroundInferenceService;
import com.planck.planck.domain.inference.service.StreamingInferenceService;
import com.planck.planck.domain.inferencestatus.InferenceStatusService;
import com.planck.planck.entitities.User;
import com.planck.planck.llmproviders.dto.StreamingChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = {"/inference/projects/{projectId}"})
@Tag(description = "APIs related to inference", name = "Inference")
@Slf4j
class InferenceController {

  @Autowired private InferenceService inferenceService;
  @Autowired InferenceStatusService inferenceStatusService;
  @Autowired private PlaygroundInferenceService inferenceHistoryService;
  @Autowired private StreamingInferenceService streamingInferenceService;

  @Operation(
      summary = "Quick compare chat completion",
      description = "Quick compare chat completion")
  @ApiResponse(responseCode = "200", description = "Chat completion run successfully")
  @PostMapping("/chat")
  @RateLimited
  public ResponseEntity<?> quickCompareChatCompletion(
      @PathVariable String projectId,
      @AuthenticationPrincipal User user,
      @RequestBody QuickCompareInferenceRequest request)
      throws Exception {
    return ResponseEntity.ok(
        inferenceHistoryService.runInferenceWithConversation(
            user,
            projectId,
            request.getModelId(),
            request.getPreviousChatTurnId(),
            request.getPrompts(),
            request.getVariables()));
  }

  @Operation(
      summary = "Bulk chat completion",
      description =
          "Run bulk chat completion with multiple chat turn IDs. If model_ids are provided, inference will be (re)run with those. If the input model is not used on an input chat turn, the chat will be duplicated with new output generated for the last prompt. If no model IDs are provided, the output will be regenerated.")
  @ApiResponse(responseCode = "200", description = "Bulk chat inference")
  @PostMapping("/bulk")
  @RateLimited
  public ResponseEntity<?> bulkChatCompletion(
      @PathVariable String projectId,
      @AuthenticationPrincipal User user,
      @RequestBody BulkInferenceRequest request)
      throws Exception {
    return ResponseEntity.ok(
        inferenceService.runReinference(
            user, projectId, request.getChatTurnIds(), request.getModelIds()));
  }

  @Operation(
      summary = "Bulk chat completion of all chats in a project",
      description = "Run bulk chat completion for all chats in a project")
  @ApiResponse(responseCode = "200", description = "Bulk chat inference")
  @PostMapping("/bulk/all")
  @RateLimited
  public ResponseEntity<?> bulkChatCompletionAll(
      @PathVariable String projectId,
      @RequestBody BulkInferenceForProjectRequest request,
      @AuthenticationPrincipal User user)
      throws Exception {
    return ResponseEntity.ok(
        inferenceService.runReinferenceForProject(user, projectId, request.getModelIds()));
  }

  @Operation(summary = "Stop specific inference in a project by chat turn id")
  @ApiResponse(responseCode = "200", description = "Inference stopped successfully")
  @ApiResponse(responseCode = "400", description = "Invalid input")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @PostMapping("/{chatTurnId}/stop")
  public ResponseEntity<Void> stopInferenceTask(
      @PathVariable("chatTurnId") String chatTurnId,
      @PathVariable("projectId") String projectId,
      @AuthenticationPrincipal User user) {

    inferenceStatusService.stopInferenceStatusByChatTurnIdAndProjectId(chatTurnId, projectId, user);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Stream chat completion",
      description = "Stream chat completion responses for the given prompts and model")
  @ApiResponse(
      responseCode = "200",
      description = "Streaming chat completion started successfully",
      content =
          @Content(
              mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
              schema = @Schema(implementation = StreamingChatResponse.class)))
  @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @RateLimited
  public Flux<StreamingChatResponse> streamChatCompletion(
      @Parameter(description = "Project ID", example = "project-123") @PathVariable
          String projectId,
      @Parameter(description = "Request payload for streaming chat completion") @RequestBody
          QuickCompareInferenceRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {

    return streamingInferenceService.streamChatCompletion(
        user,
        projectId,
        request.getModelId(),
        request.getPreviousChatTurnId(),
        request.getPrompts());
  }
}

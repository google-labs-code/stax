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

package com.planck.planck.domain.datatransfer;

import com.planck.planck.domain.datatransfer.dto.CopyAllChatsRequestDTO;
import com.planck.planck.domain.datatransfer.dto.CopyChatsResponseDTO;
import com.planck.planck.domain.datatransfer.dto.CopySelectedChatsRequestDTO;
import com.planck.planck.domain.datatransfer.dto.MoveAllChatsRequestDTO;
import com.planck.planck.domain.datatransfer.dto.MoveChatsRequestDTO;
import com.planck.planck.domain.datatransfer.dto.MoveChatsResponseDTO;
import com.planck.planck.domain.datatransfer.service.DataTransferOperationsService;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ChatTurnContainerType;
import com.planck.planck.exceptions.IllegalInputException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/data-transfer")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Data Transfer Operations",
    description = "APIs for transferring data, such as copying chats between projects.")
@SecurityRequirement(name = "bearerAuth")
public class DataTransferOperationsController {
  private final DataTransferOperationsService dataTransferOperationsService;

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary =
          "Copy specified chats from a source project or dataset to a target project or dataset.",
      description =
          "This operation copies a list of chats, including their content, from a specified source project/dataset to a target project/dataset. "
              + "New unique IDs will be generated for the copied chats and their associated data in the target project/dataset. "
              + "The user must have read access to the source project/dataset and chats, and write access to the target project/dataset.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Chats copied successfully (or partially successfully). Check response body for details.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CopyChatsResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Invalid request parameters (e.g., source and target projects are the same, missing IDs, or validation errors on request body)."),
        @ApiResponse(
            responseCode = "403",
            description =
                "User does not have sufficient permissions for the source or target project/dataset."),
        @ApiResponse(
            responseCode = "404",
            description =
                "Source project/dataset, target project/dataset, or one or more source chats not found."),
        @ApiResponse(
            responseCode = "500",
            description = "An internal error occurred during the copy process.")
      })
  public ResponseEntity<CopyChatsResponseDTO> copyChat(
      @Parameter(hidden = true) @AuthenticationPrincipal User user,
      @Valid @RequestBody CopySelectedChatsRequestDTO copyRequest) {

    validateSourceAndTarget(
        copyRequest.getSourceId(),
        copyRequest.getTargetId(),
        copyRequest.getSourceType(),
        copyRequest.getTargetType());

    log.info("User '{}' initiating specific chat copy request: {}", user.getId(), copyRequest);

    CopyChatsResponseDTO response =
        dataTransferOperationsService.copySelectedChatsFromProjectToProject(
            copyRequest.getSourceId(), copyRequest.getTargetId(), copyRequest.getChatIds(), user);

    return buildCopyResponse(response, user, copyRequest);
  }

  @PostMapping(
      path = "/all",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Copy all chats from a source project or dataset to a target project or dataset.",
      description =
          "This operation copies all the chats from a specified source project/dataset to a target project/dataset. "
              + "New unique IDs will be generated for the copied chats and their associated data in the target project/dataset. "
              + "The user must have read access to the source project/dataset and chats, and write access to the target project/dataset.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Chats copied successfully (or partially successfully). Check response body for details.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CopyChatsResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Invalid request parameters (e.g., source and target are the same, missing IDs, or validation errors on request body)."),
        @ApiResponse(
            responseCode = "403",
            description =
                "User does not have sufficient permissions for the source or target project/dataset."),
        @ApiResponse(
            responseCode = "404",
            description =
                "Source project/dataset, target project/dataset, or one or more source chats not found."),
        @ApiResponse(
            responseCode = "500",
            description = "An internal error occurred during the copy process.")
      })
  public ResponseEntity<CopyChatsResponseDTO> copyAllChats(
      @AuthenticationPrincipal User user, @Valid @RequestBody CopyAllChatsRequestDTO request) {

    validateSourceAndTarget(
        request.getSourceId(),
        request.getTargetId(),
        request.getSourceType(),
        request.getTargetType());
    log.info("User '{}' initiating all chats copy request: {}", user.getId(), request);

    CopyChatsResponseDTO response =
        dataTransferOperationsService.copyAllChatsFromProjectToProject(
            request.getSourceId(), request.getTargetId(), user);

    return buildCopyResponse(response, user, request);
  }

  @PostMapping(
      path = "/move",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary =
          "Move selected chats from a source project or dataset to a target project or dataset. Moving between project and Dataset is not supported.",
      description =
          "This operation move all the chats from a specified source project/dataset to a target project/dataset. "
              + "New unique IDs will be generated for the copied chats and their associated data in the target project/dataset. "
              + "The user must have read access to the source project/dataset and chats, and write access to the target project/dataset."
              + "Moving between project and Dataset is not supported.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Chats moved successfully (or partially successfully). Check response body for details.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = MoveChatsResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Invalid request parameters (e.g., source and target are the same, missing IDs, or validation errors on request body)."),
        @ApiResponse(
            responseCode = "403",
            description =
                "User does not have sufficient permissions for the source or target project/dataset."),
        @ApiResponse(
            responseCode = "404",
            description =
                "Source project/dataset, target project/dataset, or one or more source chats not found."),
        @ApiResponse(
            responseCode = "500",
            description = "An internal error occurred during the move process.")
      })
  public ResponseEntity<MoveChatsResponseDTO> moveChats(
      @AuthenticationPrincipal User user, @Valid @RequestBody MoveChatsRequestDTO request) {
    validateMoveType(request.getSourceType(), request.getTargetType());
    validateSourceAndTarget(
        request.getSourceId(),
        request.getTargetId(),
        request.getSourceType(),
        request.getTargetType());

    MoveChatsResponseDTO response =
        dataTransferOperationsService.moveChatsFromSourceToTarget(
            request.getSourceId(), request.getTargetId(), request.getChatIds(), user);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PostMapping(
      path = "/move/all",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary =
          "Move all chats from a source project or dataset to a target project or dataset. Moving between project and Dataset is not supported.",
      description =
          "This operation move all the chats from a specified source project/dataset to a target project/dataset. "
              + "New unique IDs will be generated for the copied chats and their associated data in the target project/dataset. "
              + "The user must have read access to the source project/dataset and chats, and write access to the target project/dataset."
              + "Moving between project and Dataset is not supported.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Chats moved successfully (or partially successfully). Check response body for details.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = MoveChatsResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Invalid request parameters (e.g., source and target are the same, missing IDs, or validation errors on request body)."),
        @ApiResponse(
            responseCode = "403",
            description =
                "User does not have sufficient permissions for the source or target project/dataset."),
        @ApiResponse(
            responseCode = "404",
            description =
                "Source project/dataset, target project/dataset, or one or more source chats not found."),
        @ApiResponse(
            responseCode = "500",
            description = "An internal error occurred during the move process.")
      })
  public ResponseEntity<MoveChatsResponseDTO> moveAllChats(
      @AuthenticationPrincipal User user, @Valid @RequestBody MoveAllChatsRequestDTO request) {
    validateMoveType(request.getSourceType(), request.getTargetType());
    validateSourceAndTarget(
        request.getSourceId(),
        request.getTargetId(),
        request.getSourceType(),
        request.getTargetType());

    MoveChatsResponseDTO response =
        dataTransferOperationsService.moveChatsFromSourceToTarget(
            request.getSourceId(), request.getTargetId(), null, user);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  private void validateMoveType(
      ChatTurnContainerType sourceType, ChatTurnContainerType targetType) {
    if (sourceType.equals(ChatTurnContainerType.PROJECT)
        && targetType.equals(ChatTurnContainerType.DATASET)) {
      throw new IllegalInputException(
          "Moving between PROJECT and DATASET is currently not supported");
    }
  }

  private void validateSourceAndTarget(
      String sourceId, String targetId, Object sourceType, Object targetType) {
    if (sourceId.equals(targetId) && sourceType.equals(targetType)) {
      log.warn("Attempt to copy chats within the same container: {}", sourceId);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Source and target IDs cannot be the same.");
    }
  }

  private ResponseEntity<CopyChatsResponseDTO> buildCopyResponse(
      CopyChatsResponseDTO response, User user, Object request) {
    if (response.getFailedToCopyCount() > 0 && response.getSuccessfullyCopiedCount() == 0) {
      log.warn(
          "Chat copy operation for user '{}' resulted in all requested chats failing to copy. Request: {}",
          user.getId(),
          request);
      return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    log.info(
        "Chat copy operation for user '{}' completed. Success: {}, Failed: {}",
        user.getId(),
        response.getSuccessfullyCopiedCount(),
        response.getFailedToCopyCount());
    return ResponseEntity.ok(response);
  }
}

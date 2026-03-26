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

package com.planck.planck.domain.dataset;

import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.dataset.dto.ChatTurnGroupCreateRequest;
import com.planck.planck.domain.dataset.dto.DataSetDTO;
import com.planck.planck.domain.dataset.dto.DataSetListResponse;
import com.planck.planck.domain.dataset.dto.DataSetRowDeleteByChatIdRequest;
import com.planck.planck.domain.dataset.dto.DataSetRowDeleteRequest;
import com.planck.planck.domain.dataset.dto.DataSetRowUpdateRequest;
import com.planck.planck.domain.dataset.dto.DataSetUpdateRequest;
import com.planck.planck.domain.dataset.dto.RowCreateRequest;
import com.planck.planck.domain.dataset.service.DataSetService;
import com.planck.planck.domain.importexport.ChatExportService;
import com.planck.planck.domain.importexport.ChatImportServiceImpl;
import com.planck.planck.domain.importexport.dto.ChatExportDTO;
import com.planck.planck.domain.importexport.dto.ChatImportRequest;
import com.planck.planck.domain.importexport.dto.ImportResultDTO;
import com.planck.planck.domain.project.EvaluationContainerService;
import com.planck.planck.domain.workbook.WorkbookService;
import com.planck.planck.domain.workbook.dto.ChatTurnWorkbookRowDTO;
import com.planck.planck.domain.workbook.dto.GetWorkbookResponseDTO;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.DataSet;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ChatTurnContainerType;
import com.planck.planck.exceptions.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/datasets")
@Tag(name = "Dataset Controller", description = "APIs related to Datasets")
@Validated
public class DataSetController {
  @Autowired private DataSetService dataSetService;
  @Autowired private ChatExportService chatExportService;
  @Autowired private ChatImportServiceImpl chatImportService;
  @Autowired private ChatService chatService;
  @Autowired private WorkbookService workbookService;
  @Autowired private EvaluationContainerService evaluationContainerService;

  @Operation(
      summary = "Get all datasets for the user",
      description = "Get all datasets for the user from the database",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true)))
      })
  @GetMapping
  public ResponseEntity<DataSetListResponse> getAllDatasets(
      @AuthenticationPrincipal User user,
      @RequestParam(name = "include_hidden", defaultValue = "false", required = false)
          Boolean includeHidden) {
    return ResponseEntity.ok(dataSetService.getAllDataSets(user, includeHidden));
  }

  @Operation(
      summary = "Get dataset by ID",
      description = "Get specific for the user from the database",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true)))
      })
  @GetMapping("/{id}")
  public ResponseEntity<DataSetDTO> getDatasetById(
      @AuthenticationPrincipal User user, @PathVariable String id) {
    return ResponseEntity.ok(dataSetService.getDataSetById(user, id));
  }

  @Operation(
      summary = "Create a new dataset",
      description =
          "Create a new dataset, name is defaulted to Untitled + incremental number if not provided.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "429",
            description = "Too many datasets per user due to the limit",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @PostMapping
  public ResponseEntity<DataSetDTO> createDataset(
      @AuthenticationPrincipal User user, @RequestBody DataSetDTO datasetDTO) {
    DataSet createdDataset =
        dataSetService.createDataSet(
            user, datasetDTO.getName(), datasetDTO.getDescription(), datasetDTO.getType());
    return ResponseEntity.status(HttpStatus.CREATED).body(new DataSetDTO(createdDataset));
  }

  @Operation(
      summary = "Delete specific dataset by id",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDataset(
      @AuthenticationPrincipal User user, @PathVariable String id) {
    evaluationContainerService.deleteContainer(user, id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Update an existing dataset",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @PatchMapping("/{id}")
  public ResponseEntity<DataSetDTO> updateDataset(
      @AuthenticationPrincipal User user,
      @PathVariable String id,
      @RequestBody DataSetUpdateRequest request) {
    DataSetDTO updatedDataset =
        dataSetService.updateDataset(user, id, request.getName(), request.getDescription());
    return ResponseEntity.ok(updatedDataset);
  }

  @Operation(
      summary = "Retrieve rows from DataSet as array",
      description = "Get all rows for the user under the provided data set ID and chat id.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @GetMapping("/{id}/{chatId}/rows")
  public ResponseEntity<List<ChatTurnWorkbookRowDTO>> getDatasetRowsByChatId(
      @AuthenticationPrincipal User user, @PathVariable String id, @PathVariable String chatId) {
    List<ChatTurn> turns = chatService.getChat(chatId, user, null);
    List<ChatTurnWorkbookRowDTO> chatInWorkBookDTO = new ArrayList<>();
    for (ChatTurn chatTurn : turns) {
      chatInWorkBookDTO.add(new ChatTurnWorkbookRowDTO(chatTurn));
    }
    return new ResponseEntity<>(chatInWorkBookDTO, HttpStatus.OK);
  }

  @Operation(
      summary = "Retrieve rows from DataSet paginated",
      description = "Get latest rows for the user under the provided data set ID.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OK - Returns a paginated list of dataset rows.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GetWorkbookResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Invalid Argument - If page_size is negative or other parameters are invalid.",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have access to this dataset.",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "404",
            description = "Dataset not found for this user",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true)))
      })
  @GetMapping("/{id}/rows")
  public ResponseEntity<GetWorkbookResponseDTO> getDatasetRows(
      @Parameter(description = "ID of the dataset", example = "dataset-uuid4") @PathVariable
          String id,
      @Parameter(
              description =
                  "Maximum number of rows to return. The service may return fewer. If unspecified, defaults to 20. If less then 1 - 10000 will be applied. The maximum value is 10000; values above 10000 will be coerced to 10000.",
              required = false,
              example = "20")
          @RequestParam(required = false, name = "page_size", defaultValue = "20")
          int pageSize,
      @Parameter(
              description =
                  "A page token, received from a previous list call. Provide this to retrieve the subsequent page. When paginating, all other parameters provided must match the call that provided the page token.",
              required = false,
              example = "0")
          @RequestParam(required = false, name = "page_token", defaultValue = "0")
          int pageToken,
      @Parameter(
              description =
                  "Sort order conforming to AIP-132 (e.g., 'input asc,created_at desc, inference_tokens'). Use snake_case for field names. Default value is  \"created_at desc\"",
              required = false,
              example = "created_at desc")
          @RequestParam(required = false, name = "order_by", defaultValue = "created_at desc")
          String orderBy,
      @Parameter(
              description =
                  "Filter expression conforming to AIP-160 (e.g., 'input = \"value\" AND inference_latency > 0.5'). Use snake_case for field names. Supports logical (AND, OR, NOT, -), comparison (=, !=, <, >, <=, >=), traversal (.), and has (:) operators.",
              required = false,
              example = "input = \"value\"")
          @RequestParam(required = false)
          String filter,
      @AuthenticationPrincipal User user) {
    GetWorkbookResponseDTO dto =
        workbookService.getData(user, id, pageSize, pageToken, orderBy, filter, null);
    return ResponseEntity.ok(dto);
  }

  @Operation(
      summary = "Create dataset row",
      description =
          "Creates a new row within the specified dataset. The `chat_id` in the request body is optional. "
              + "If `chat_id` is provided, the new row will be assigned a sequence number incremented relative to the existing chat. "
              + "If `chat_id` is omitted, a new chat will be created, and the sequence number will start at 0.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @PostMapping("/{id}/row")
  public ResponseEntity<ChatTurnDTO> createDatasetRow(
      @AuthenticationPrincipal User user,
      @RequestBody RowCreateRequest request,
      @PathVariable String id) {
    ChatTurn chatTurn =
        chatService.createChatTurn(
            user,
            id,
            request.getChatId(),
            request.getModelPrompt(),
            request.getModelResponse(),
            request.getModelId());
    return ResponseEntity.ok(new ChatTurnDTO(chatTurn));
  }

  @Operation(
      summary = "Create dataset rows in bulk by chat ids",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @PostMapping("/{id}/rows")
  public ResponseEntity<List<ChatTurnDTO>> createDatasetRows(
      @AuthenticationPrincipal User user,
      @RequestBody List<ChatTurnGroupCreateRequest> request,
      @PathVariable String id) {
    List<ChatTurnDTO> result = chatService.addTurnsInBulk(user, id, request);
    return ResponseEntity.ok(result);
  }

  @Operation(
      summary = "Delete all dataset rows",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @DeleteMapping("/{id}/rows")
  public ResponseEntity<Void> deleteDatasetRows(
      @AuthenticationPrincipal User user, @PathVariable String id) {
    chatService.deleteChats(user, id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Delete dataset rows by the provided specific chat turn ids",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @DeleteMapping("/{id}/rows/bulk")
  public ResponseEntity<Void> deleteDatasetRowsBulk(
      @AuthenticationPrincipal User user,
      @PathVariable String id,
      @RequestBody DataSetRowDeleteRequest request) {
    EvaluationContainer container = evaluationContainerService.getContainerForUser(user, id);
    if (container == null) {
      throw new NotFoundException("Evaluation container not found");
    }
    chatService.delete(user, request.getChatTurnIds());
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Delete dataset rows by the provided specific chat ids",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @DeleteMapping("/{id}/rows/bulk-by-chat-id")
  public ResponseEntity<Void> deleteDatasetRowsBulk(
      @AuthenticationPrincipal User user,
      @PathVariable String id,
      @RequestBody DataSetRowDeleteByChatIdRequest request) {
    chatService.deleteChats(request.getChatIds(), user, id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Update specific dataset row",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @PatchMapping("/{id}/rows/{rowId}")
  public ResponseEntity<ChatTurnDTO> updateDatasetRow(
      @AuthenticationPrincipal User user,
      @PathVariable String id,
      @PathVariable String rowId,
      @RequestBody DataSetRowUpdateRequest request) {
    ChatTurn updated =
        chatService.updateTurn(
            user,
            id,
            rowId,
            request.getModelPrompt(),
            request.getModelResponse(),
            request.getModelId(),
            request.getExpectedOutput());
    return ResponseEntity.ok(new ChatTurnDTO(updated));
  }

  @Operation(
      summary = "Delete specific dataset row",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @DeleteMapping("/{id}/rows/{rowId}")
  public ResponseEntity<Void> deleteDatasetRow(
      @AuthenticationPrincipal User user, @PathVariable String id, @PathVariable String rowId) {
    chatService.delete(user, rowId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/export")
  @Operation(
      summary = "Bulk export of chats for the whole dataset",
      description =
          "Exports all chats in dataset. Each chat is exported in the same structure as a single chat export. Returns a list of exported chats.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully exported the chats",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChatExportDTO.class))),
        @ApiResponse(responseCode = "404", description = "DataSet not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public List<ChatExportDTO> exportChats(
      @PathVariable String id, @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return chatExportService.exportAllChats(id, user);
  }

  @PostMapping(
      value = "/{id}/import",
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
      @PathVariable String id,
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
              ChatTurnContainerType.DATASET,
              id,
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
}

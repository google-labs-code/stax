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

package com.planck.planck.domain.chat;

import com.planck.planck.domain.chat.dto.ChatDTO;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.importexport.ChatExportService;
import com.planck.planck.domain.importexport.dto.ChatExportDTO;
import com.planck.planck.domain.workbook.dto.ChatTurnWorkbookRowDTO;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("chat")
@Tag(name = "Chat API", description = "API for managing chats")
public class ChatController {

  @Autowired private ChatService chatService;

  @Autowired private ChatExportService chatExportService;

  @GetMapping("/{chatId}")
  @Operation(
      summary = "Get chat by ID",
      description = "Retrieves a chat by its unique identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the chat"),
        @ApiResponse(responseCode = "404", description = "Chat not found")
      })
  public ResponseEntity<ChatDTO> getChat(
      @PathVariable String chatId,
      @Parameter(
              description =
                  "Optionally filter the chat turns by a list of tagId. If provided, only chat turns matching all of the provided tag will be retrieved",
              required = false)
          @RequestParam(required = false)
          List<String> tagIds,
      @AuthenticationPrincipal User user) {
    ChatDTO chat = new ChatDTO(chatService.getChat(chatId, user, tagIds));
    return new ResponseEntity<>(chat, HttpStatus.OK);
  }

  @GetMapping("/{chatId}/turns/{turnId}")
  @Operation(
      summary = "Get chat by ID up to a specific turn",
      description =
          "Retrieves chat history from the beginning up to and including the specified turn.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the chat history"),
        @ApiResponse(responseCode = "404", description = "Chat or Turn not found")
      })
  public ResponseEntity<ChatDTO> getChatUntilTurn(
      @PathVariable String chatId,
      @PathVariable String turnId,
      @AuthenticationPrincipal User user) {

    List<ChatTurn> chatTurns = chatService.getChatUntilTurn(chatId, turnId, user);

    ChatDTO chat = new ChatDTO(chatTurns);

    return new ResponseEntity<>(chat, HttpStatus.OK);
  }

  @GetMapping("/workbook/{chatId}")
  @Operation(
      summary = "Get chat by ID as WorkBook Rows",
      description = "Retrieves a chat by its unique identifier and returns it as a WorkBook Rows",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the chat"),
        @ApiResponse(responseCode = "404", description = "Chat not found")
      })
  public ResponseEntity<List<ChatTurnWorkbookRowDTO>> getChatAsWorkBookRows(
      @PathVariable String chatId,
      @Parameter(
              description =
                  "Optionally filter the chat turns by a list of tagId. If provided, only chat turns matching all of the provided tag will be retrieved",
              required = false)
          @RequestParam(required = false)
          List<String> tagIds,
      @AuthenticationPrincipal User user) {
    List<ChatTurn> chatTurns = chatService.getChat(chatId, user, tagIds);
    List<ChatTurnWorkbookRowDTO> chatInWorkBookDTO = new ArrayList<>();
    for (ChatTurn chatTurn : chatTurns) {
      chatInWorkBookDTO.add(new ChatTurnWorkbookRowDTO(chatTurn));
    }
    return new ResponseEntity<>(chatInWorkBookDTO, HttpStatus.OK);
  }

  @GetMapping("/{chatId}/export")
  @Operation(
      summary = "Export a chat in machine-readable format",
      description =
          "Exports all turns of the specified chat, including user and assistant messages, model labels, human evaluation scores, and tags. Messages are returned in the original chat sequence.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully exported the chat",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChatExportDTO.class))),
        @ApiResponse(responseCode = "404", description = "Chat not found")
      })
  public ChatExportDTO exportChat(
      @Parameter(description = "Unique identifier of the chat", example = "chat-123") @PathVariable
          String chatId,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return chatExportService.exportChat(chatId, user);
  }

  @PostMapping("/export")
  @Operation(
      summary = "Bulk export of chats by ID list",
      description =
          "Exports multiple chats by their unique identifiers. Each chat is exported in the same structure as a single chat export. Returns a list of exported chats.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully exported the chats",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChatExportDTO.class)))
      })
  public List<ChatExportDTO> exportChats(
      @Parameter(
              description =
                  "List of chat IDs to export. Should be sent as a JSON array in the request body.",
              example = "[\"chat-123\", \"chat-456\"]")
          @RequestBody
          List<String> chatIds,
      @Parameter(hidden = true) @AuthenticationPrincipal User user) {
    return chatExportService.exportChats(chatIds, user);
  }

  @GetMapping("/{chatId}/variables")
  @Operation(
      summary = "Get chat variables",
      description = "Retrieves all key-value pairs from the variables of a specific chat.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved variables",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(
                        implementation = Map.class,
                        example = "{\"source\": \"web\", \"version\": \"1.2\"}"))),
    @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
  })
  public ResponseEntity<Map<String, String>> getChatVariables(
      @Parameter(description = "Unique identifier of the chat", example = "chat-123") @PathVariable
          String chatId,
      @AuthenticationPrincipal User user) {

    Map<String, String> variables = chatService.getVariables(chatId, user);
    return ResponseEntity.ok(variables);
  }

  @GetMapping("/{chatId}/variables/keys")
  @Operation(
      summary = "Get chat variables keys",
      description =
          "Retrieves the first-level keys from the variables of a specific chat as an array.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved variables keys",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(
                        implementation = List.class,
                        example = "[\"source\", \"version\", \"category\"]"))),
    @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
  })
  public ResponseEntity<List<String>> getChatVariablesKeys(
      @Parameter(description = "Unique identifier of the chat", example = "chat-123") @PathVariable
          String chatId,
      @AuthenticationPrincipal User user) {

    List<String> variablesKeys = chatService.getVariablesKeys(chatId, user);
    return ResponseEntity.ok(variablesKeys);
  }

  @PutMapping("/{chatId}/variables")
  @Operation(
      summary = "Replace chat variables",
      description =
          "Completely replaces the existing variables of a chat with the provided map. Any existing keys not in the new map will be removed.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successfully replaced variables",
        content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
    @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
  })
  public ResponseEntity<Map<String, String>> replaceChatVariables(
      @Parameter(description = "Unique identifier of the chat", example = "chat-123") @PathVariable
          String chatId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "A map containing the new variables.",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = Map.class),
                      examples = @ExampleObject(value = "{\"new_key\": \"new_value\"}")))
          @RequestBody
          Map<String, String> newVariables,
      @AuthenticationPrincipal User user) {

    Chat updatedChat = chatService.replaceVariables(chatId, user, newVariables);
    return ResponseEntity.ok(updatedChat.getVariables());
  }

  @PatchMapping("/{chatId}/variables")
  @Operation(
      summary = "Update chat variables",
      description =
          "Adds new key-value pairs or updates existing ones in the chat's variables. Keys not included in the request will remain unchanged.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successfully updated variables",
        content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
    @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
  })
  public ResponseEntity<Map<String, String>> updateChatVariables(
      @Parameter(description = "Unique identifier of the chat", example = "chat-123") @PathVariable
          String chatId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "A map containing keys to add or update.",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = Map.class),
                      examples =
                          @ExampleObject(
                              value =
                                  "{\"existing_key\": \"updated_value\", \"added_key\": \"new_value\"}")))
          @RequestBody
          Map<String, String> variablesUpdates,
      @AuthenticationPrincipal User user) {

    Chat updatedChat = chatService.addOrUpdateVariables(chatId, user, variablesUpdates);
    return ResponseEntity.ok(updatedChat.getVariables());
  }

  @DeleteMapping("/{chatId}/variables/{key}")
  @Operation(
      summary = "Delete a variables key",
      description =
          "Removes a single key-value pair from the chat's variables, identified by its key.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successfully deleted the key",
        content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Chat or variables key not found",
        content = @Content)
  })
  public ResponseEntity<Map<String, String>> removeChatVariablesKey(
      @Parameter(description = "Unique identifier of the chat", example = "chat-123") @PathVariable
          String chatId,
      @Parameter(description = "The key to be deleted from the variables", example = "source")
          @PathVariable
          String key,
      @AuthenticationPrincipal User user) {

    Chat updatedChat = chatService.removeVariablesKey(chatId, user, key);
    return ResponseEntity.ok(updatedChat.getVariables());
  }
}

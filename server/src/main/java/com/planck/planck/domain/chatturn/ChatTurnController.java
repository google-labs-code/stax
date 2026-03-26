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

package com.planck.planck.domain.chatturn;

import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.chatturn.dto.UpdateChatTurnRequestDTO;
import com.planck.planck.domain.chatturn.service.ChatTurnService;
import com.planck.planck.entitities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("chat-turn")
@Tag(name = "Chat API", description = "API for managing chats")
public class ChatTurnController {

  @Autowired private ChatTurnService chatTurnService;

  @GetMapping("/{chatTurnId}")
  @Operation(
      summary = "Get chat turn by ID",
      description = "Retrieves a chat by its unique identifier.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the cha Turn"),
        @ApiResponse(responseCode = "404", description = "Chat Turn not found")
      })
  public ResponseEntity<ChatTurnDTO> getChat(
      @PathVariable String chatTurnId, @AuthenticationPrincipal User user) {
    ChatTurnDTO chatTurnDTO = new ChatTurnDTO(chatTurnService.getChatTurn(chatTurnId, user));
    return new ResponseEntity<>(chatTurnDTO, HttpStatus.OK);
  }

  @PutMapping("/{chatTurnId}")
  @Operation(
      summary = "Update Expected output in a Chat Turn",
      description =
          "This api allows you to update the expected output oof a Chat Turn. It will link the expected Output to the last Prompt of Model Input Message",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully updated the chat turn"),
        @ApiResponse(responseCode = "404", description = "Chat turn not found")
      })
  public ResponseEntity<?> getChat(
      @PathVariable String chatTurnId,
      @AuthenticationPrincipal User user,
      @RequestBody UpdateChatTurnRequestDTO request) {
    chatTurnService.updateExpectedOutput(chatTurnId, user, request.getExpectedOutput());
    return new ResponseEntity<>(HttpStatus.OK);
  }
}

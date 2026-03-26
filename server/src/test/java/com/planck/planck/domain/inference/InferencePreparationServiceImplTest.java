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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.inference.dto.InferenceContext;
import com.planck.planck.domain.inference.dto.InferenceDTO;
import com.planck.planck.domain.inference.service.InferencePreparationServiceImpl;
import com.planck.planck.domain.inferencestatus.InferenceStatusService;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelresponse.ModelResponseRepository;
import com.planck.planck.domain.user.UserService;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.InferenceStatus;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InferenceStatusEnum;
import com.planck.planck.exceptions.NotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InferencePreparationServiceImplTest {

  @Mock private UserService userService;
  @Mock private ChatService chatService;
  @Mock private ModelService modelService;
  @Mock private InferenceStatusService inferenceStatusService;
  @Mock private ModelResponseRepository modelResponseRepository;

  @InjectMocks private InferencePreparationServiceImpl service;

  private User user;
  private Model model;
  private InferenceDTO dto;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId("user-123");

    model = new Model();
    model.setId("model-gpt4");

    dto = new InferenceDTO();
    dto.setUserId("user-123");
    dto.setChatTurnId("turn-abc");
  }

  @Nested
  @DisplayName("Tests for prepareUsingExistingModel")
  class PrepareUsingExistingModelTests {

    @Test
    @DisplayName("Should prepare context when chat turn has a model response")
    void prepareUsingExistingModel_Success_WithModelResponse() {
      // Arrange
      ModelResponse modelResponse = new ModelResponse();
      modelResponse.setModel(model);

      ChatTurn chatTurn = new ChatTurn();
      chatTurn.setModelResponse(modelResponse);

      when(userService.findByUserId("user-123")).thenReturn(user);

      // Act
      InferenceContext context = service.prepareUsingExistingModel(chatTurn, dto);

      // Assert
      assertNotNull(context);
      assertEquals(model, context.model());
      assertEquals("model-gpt4", dto.getModelId());
      assertTrue(context.payloadJson().contains("\"model_id\":\"model-gpt4\""));
    }

    @Test
    @DisplayName("Should fallback to previous turn's model if current has none")
    void prepareUsingExistingModel_Success_FallbackToPreviousTurn() {
      // Arrange
      Chat chat = new Chat();
      chat.setId("chat-1");

      ModelResponse prevModelResponse = new ModelResponse();
      prevModelResponse.setModel(model);

      ChatTurn previousTurn = new ChatTurn();
      previousTurn.setSequenceId(1);
      previousTurn.setModelResponse(prevModelResponse);

      ChatTurn currentTurn = new ChatTurn();
      currentTurn.setSequenceId(2);
      currentTurn.setChat(chat);
      currentTurn.setModelResponse(null); // No model response on current turn

      when(userService.findByUserId("user-123")).thenReturn(user);
      when(chatService.getChat(chat.getId(), user))
          .thenReturn(Arrays.asList(previousTurn, currentTurn));

      // Act
      InferenceContext context = service.prepareUsingExistingModel(currentTurn, dto);

      // Assert
      assertNotNull(context);
      assertEquals(model, context.model());
      assertEquals("model-gpt4", dto.getModelId());
    }

    @Test
    @DisplayName("Should throw NotFoundException if chat turn is null")
    void prepareUsingExistingModel_Error_ChatTurnIsNull() {
      // Arrange
      when(userService.findByUserId("user-123")).thenReturn(user);

      // Act & Assert
      NotFoundException exception =
          assertThrows(NotFoundException.class, () -> service.prepareUsingExistingModel(null, dto));
      assertEquals("Chat turn not found for user", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException on first turn with no model")
    void prepareUsingExistingModel_Error_FirstTurnNoModel() {
      // Arrange
      ChatTurn chatTurn = new ChatTurn();
      chatTurn.setSequenceId(1);
      chatTurn.setModelResponse(null);

      when(userService.findByUserId("user-123")).thenReturn(user);

      // Act & Assert
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> service.prepareUsingExistingModel(chatTurn, dto));
      assertTrue(exception.getMessage().contains("Can't run inference for chat turn:"));
    }
  }

  @Nested
  @DisplayName("Tests for prepareWithExplicitModel")
  class PrepareWithExplicitModelTests {

    @Test
    @DisplayName("Should duplicate chat when model is different")
    void prepareWithExplicitModel_ShouldDuplicate_DifferentModel() {
      // Arrange
      String newModelId = "model-claude";
      Model newModel = new Model();
      newModel.setId(newModelId);

      ModelResponse existingResponse = new ModelResponse();
      existingResponse.setModel(model); // old model
      existingResponse.setText("Some response");

      ChatTurn originalTurn = new ChatTurn();
      originalTurn.setId("original-turn-id");
      originalTurn.setModelResponse(existingResponse);

      ChatTurn newTurn = new ChatTurn();
      newTurn.setId("new-turn-id");

      when(userService.findByUserId(dto.getUserId())).thenReturn(user);
      when(modelService.getModelForUser(user, newModelId)).thenReturn(newModel);
      when(chatService.duplicateChatNewLastModelResponse(originalTurn, newModel))
          .thenReturn(List.of(newTurn));

      // Act
      InferenceContext context =
          service.prepareWithExplicitModel(originalTurn, dto, newModelId, false);

      // Assert
      assertNotNull(context);
      assertEquals(newModel, context.model());
      assertEquals(newModelId, dto.getModelId());
      assertEquals("new-turn-id", dto.getChatTurnId()); // DTO is updated with new turn ID
      verify(chatService).duplicateChatNewLastModelResponse(originalTurn, newModel);
      verify(modelResponseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update model on chat turn if model response is null")
    void prepareWithExplicitModel_ShouldUpdateModel_NoResponse() {
      // Arrange
      Chat chat = new Chat();
      chat.setId("chat-id");
      chat.setContainer(new Project());
      ChatTurn chatTurn = new ChatTurn();
      chatTurn.setId("turn-1");
      chatTurn.setModelResponse(null);
      chatTurn.setChat(chat);

      when(userService.findByUserId(dto.getUserId())).thenReturn(user);
      when(modelService.getModelForUser(user, model.getId())).thenReturn(model);
      when(modelResponseRepository.save(any(ModelResponse.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      InferenceContext context =
          service.prepareWithExplicitModel(chatTurn, dto, model.getId(), false);

      // Assert
      assertNotNull(context);
      assertEquals(model, context.model());
      verify(modelResponseRepository).save(any(ModelResponse.class));
      verify(chatService).saveChatTurn(eq(chatTurn), eq(user));
      assertNotNull(chatTurn.getModelResponse());
    }

    @Test
    @DisplayName("Should not duplicate if models are same and has response")
    void prepareWithExplicitModel_NoAction_SameModel() {
      // Arrange
      ModelResponse response = new ModelResponse();
      response.setModel(model);
      response.setText("some text");

      ChatTurn chatTurn = new ChatTurn();
      chatTurn.setModelResponse(response);

      when(userService.findByUserId(dto.getUserId())).thenReturn(user);
      when(modelService.getModelForUser(user, model.getId())).thenReturn(model);

      // Act
      service.prepareWithExplicitModel(chatTurn, dto, model.getId(), false);

      // Assert
      verify(chatService, never()).duplicateChatNewLastModelResponse(any(), any());
      verify(modelResponseRepository, never()).save(any());
      verify(chatService, never()).saveChatTurn(any(), any());
    }

    @Test
    @DisplayName("Should throw NotFoundException if chat turn is null")
    void prepareWithExplicitModel_Error_ChatTurnIsNull() {
      // Arrange
      when(userService.findByUserId(dto.getUserId())).thenReturn(user);
      when(modelService.getModelForUser(user, model.getId())).thenReturn(model);

      // Act & Assert
      NotFoundException exception =
          assertThrows(
              NotFoundException.class,
              () -> service.prepareWithExplicitModel(null, dto, model.getId(), false));
      assertEquals("Chat turn not found", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("Tests for createInferenceStatus")
  class CreateInferenceStatusTests {

    @Test
    @DisplayName("Should create status and update DTO")
    void createInferenceStatus_Success() {
      // Arrange
      InferenceStatus status = new InferenceStatus();
      String statusId = UUID.randomUUID().toString();
      status.setId(statusId);

      when(inferenceStatusService.createOrUpdateInferenceStatus(dto, InferenceStatusEnum.PENDING))
          .thenReturn(status);

      // Act
      InferenceDTO resultDto = service.createInferenceStatus(dto);

      // Assert
      assertNotNull(resultDto);
      assertEquals(statusId, resultDto.getInferenceStatusId());
      verify(inferenceStatusService)
          .createOrUpdateInferenceStatus(dto, InferenceStatusEnum.PENDING);
    }
  }
}

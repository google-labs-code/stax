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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.evaluation.SxsEvaluationPairRepository;
import com.planck.planck.domain.inference.dto.ContinueSxsRequestDTO;
import com.planck.planck.domain.inference.service.PlaygroundInferenceService;
import com.planck.planck.domain.inference.service.SxsPlaygroundInferenceServiceImpl;
import com.planck.planck.domain.project.ProjectRepository;
import com.planck.planck.domain.project.SxsEvaluationPairService;
import com.planck.planck.domain.project.dto.SxsInferenceRequestDTO;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SxsPlaygroundInferenceServiceImpl Tests")
class SxsPlaygroundInferenceServiceImplTest {

  @Mock private PlaygroundInferenceService playgroundInferenceService;
  @Mock private SxsEvaluationPairRepository sxsEvaluationPairRepository;
  @Mock private ProjectRepository projectRepository;
  @Mock private ChatTurnRepository chatTurnRepository;
  @Mock private ChatService chatService;
  @Mock private SxsEvaluationPairService sxsEvaluationPairService;

  @InjectMocks private SxsPlaygroundInferenceServiceImpl service;

  private User testUser;
  private Project sxsProject;
  private final String projectId = "sxs-project-1";
  private final String userId = "user-1";

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(userId);

    sxsProject = new Project();
    sxsProject.setId(projectId);
    sxsProject.setEvaluationType(EvaluationType.SXS);
    sxsProject.setUser(testUser);
  }

  @Nested
  @DisplayName("runInitialSxsInference tests")
  class RunInitialSxsInference {

    @Test
    @DisplayName("Should run inference for both models and create SxS pair")
    void shouldRunInferenceForBothModelsAndCreatePair() {

      SxsInferenceRequestDTO request = new SxsInferenceRequestDTO();
      request.setModelIdA("model-A");
      request.setModelIdB("model-B");
      request.setModelAInstruction("System A");
      request.setPrompts(List.of(new Prompt(InputRole.USER, "Hello")));
      request.setVariables(Map.of("key", "value"));

      ChatTurn turnA = createChatTurn("turn-A", "chat-A");
      ChatTurn turnB = createChatTurn("turn-B", "chat-B");
      ChatTurnDTO turnADto = new ChatTurnDTO();
      turnADto.setId("turn-A");
      ChatTurnDTO turnBDto = new ChatTurnDTO();
      turnBDto.setId("turn-B");

      when(projectRepository.findByUserAndId(testUser, projectId))
          .thenReturn(Optional.of(sxsProject));
      when(playgroundInferenceService.runInferenceWithConversation(
              eq(testUser), eq(projectId), eq("model-A"), isNull(), anyList(), any()))
          .thenReturn(turnADto);
      when(playgroundInferenceService.runInferenceWithConversation(
              eq(testUser), eq(projectId), eq("model-B"), isNull(), anyList(), any()))
          .thenReturn(turnBDto);
      when(chatTurnRepository.findById("turn-A")).thenReturn(Optional.of(turnA));
      when(chatTurnRepository.findById("turn-B")).thenReturn(Optional.of(turnB));
      when(sxsEvaluationPairRepository.save(any(SxsEvaluationPair.class)))
          .thenAnswer(i -> i.getArgument(0));

      SxsEvaluationPair result = service.runInitialSxsInference(testUser, projectId, request);

      assertNotNull(result);
      assertEquals(turnA, result.getChatTurnA());
      assertEquals(turnB, result.getChatTurnB());
      assertEquals(sxsProject, result.getContainer());
      assertEquals("value", result.getVariables().get("key"));

      verify(playgroundInferenceService, times(2))
          .runInferenceWithConversation(any(), any(), anyString(), isNull(), anyList(), any());
      verify(sxsEvaluationPairRepository).save(any(SxsEvaluationPair.class));
    }

    @Test
    @DisplayName("Should only run inference for model A if model B is not provided")
    void shouldRunInferenceForOnlyModelA() {

      SxsInferenceRequestDTO request = new SxsInferenceRequestDTO();
      request.setModelIdA("model-A");

      ChatTurn turnA = createChatTurn("turn-A", "chat-A");
      ChatTurnDTO turnADto = new ChatTurnDTO();
      turnADto.setId("turn-A");

      when(projectRepository.findByUserAndId(testUser, projectId))
          .thenReturn(Optional.of(sxsProject));
      when(playgroundInferenceService.runInferenceWithConversation(
              any(), any(), any(), any(), any(), any()))
          .thenReturn(turnADto);
      when(chatTurnRepository.findById("turn-A")).thenReturn(Optional.of(turnA));
      when(sxsEvaluationPairRepository.save(any(SxsEvaluationPair.class)))
          .thenAnswer(i -> i.getArgument(0));

      SxsEvaluationPair result = service.runInitialSxsInference(testUser, projectId, request);

      assertNotNull(result);
      assertEquals(turnA, result.getChatTurnA());
      assertNull(result.getChatTurnB());
      verify(playgroundInferenceService, times(1))
          .runInferenceWithConversation(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw NotFoundException if project not found")
    void shouldThrowExceptionWhenProjectNotFound() {
      when(projectRepository.findByUserAndId(testUser, projectId)).thenReturn(Optional.empty());
      assertThrows(
          NotFoundException.class,
          () -> service.runInitialSxsInference(testUser, projectId, new SxsInferenceRequestDTO()));
    }

    @Test
    @DisplayName("Should throw IllegalInputException for non-SXS projects")
    void shouldThrowExceptionForNonSxsProject() {
      sxsProject.setEvaluationType(EvaluationType.POINTWISE);
      when(projectRepository.findByUserAndId(testUser, projectId))
          .thenReturn(Optional.of(sxsProject));
      assertThrows(
          IllegalInputException.class,
          () -> service.runInitialSxsInference(testUser, projectId, new SxsInferenceRequestDTO()));
    }
  }

  @Nested
  @DisplayName("continueSxsEvaluation tests")
  class ContinueSxsEvaluation {

    private final String pairId = "sxs-pair-1";

    @Test
    @DisplayName("Should continue conversation, synchronize, and update both turns")
    void shouldContinueAndSynchronizeBothTurns() {

      ChatTurn turnA = createChatTurnWithModel("turn-A", "chat-A", "model-A");
      ChatTurn turnB = createChatTurnWithModel("turn-B", "chat-B", "model-B");
      SxsEvaluationPair pair =
          SxsEvaluationPair.builder()
              .id(pairId)
              .user(testUser)
              .chatTurnA(turnA)
              .chatTurnB(turnB)
              .build();

      ContinueSxsRequestDTO request = new ContinueSxsRequestDTO();
      request.setModelIdA("model-A");
      request.setModelIdB("model-B");
      request.setPrompts(List.of(new Prompt(InputRole.USER, "Continue")));

      ChatTurn newTurnA = createChatTurn("new-turn-A", "chat-A");
      ChatTurn newTurnB = createChatTurn("new-turn-B", "chat-B");
      ChatTurnDTO newTurnADto = new ChatTurnDTO();
      newTurnADto.setId("new-turn-A");
      ChatTurnDTO newTurnBDto = new ChatTurnDTO();
      newTurnBDto.setId("new-turn-B");

      when(sxsEvaluationPairRepository.findByIdAndUser(pairId, testUser))
          .thenReturn(Optional.of(pair));

      when(playgroundInferenceService.runInferenceWithConversation(
              eq(testUser), eq(projectId), eq("model-A"), eq("turn-A"), anyList(), isNull()))
          .thenReturn(newTurnADto);
      when(playgroundInferenceService.runInferenceWithConversation(
              eq(testUser), eq(projectId), eq("model-B"), eq("turn-B"), anyList(), isNull()))
          .thenReturn(newTurnBDto);
      when(chatTurnRepository.findById("new-turn-A")).thenReturn(Optional.of(newTurnA));
      when(chatTurnRepository.findById("new-turn-B")).thenReturn(Optional.of(newTurnB));
      when(sxsEvaluationPairRepository.save(any(SxsEvaluationPair.class)))
          .thenAnswer(i -> i.getArgument(0));

      SxsEvaluationPair result =
          service.continueSxsEvaluation(testUser, projectId, pairId, request);

      verify(sxsEvaluationPairService)
          .synchronizeSxsPairChats(pair, testUser, "model-A", "model-B");

      verify(sxsEvaluationPairService, never())
          .generateAndSetTurnB(
              any(User.class), anyString(), anyString(), anyString(), anyBoolean());

      assertEquals(newTurnA, result.getChatTurnA());
      assertEquals(newTurnB, result.getChatTurnB());
    }

    @Test
    @DisplayName("Should generate turn B if it doesn't exist")
    void shouldGenerateTurnBIfMissing() {

      ChatTurn turnA = createChatTurnWithModel("turn-A", "chat-A", "model-A");
      SxsEvaluationPair pair =
          SxsEvaluationPair.builder()
              .id(pairId)
              .user(testUser)
              .chatTurnA(turnA)
              .chatTurnB(null)
              .build();

      ContinueSxsRequestDTO request = new ContinueSxsRequestDTO();
      request.setModelIdB("model-B");
      request.setPrompts(List.of(new Prompt(InputRole.USER, "Continue")));

      when(sxsEvaluationPairRepository.findByIdAndUser(pairId, testUser))
          .thenReturn(Optional.of(pair));

      setupMocksForNewTurns();

      service.continueSxsEvaluation(testUser, projectId, pairId, request);

      verify(sxsEvaluationPairService, never()).synchronizeSxsPairChats(any(), any(), any(), any());
      verify(sxsEvaluationPairService)
          .generateAndSetTurnB(testUser, projectId, pairId, "model-B", false);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException on model ID conflict")
    void shouldThrowOnModelConflict() {

      ChatTurn turnA = createChatTurnWithModel("turn-A", "chat-A", "existing-model");
      SxsEvaluationPair pair =
          SxsEvaluationPair.builder().id(pairId).user(testUser).chatTurnA(turnA).build();

      ContinueSxsRequestDTO request = new ContinueSxsRequestDTO();
      request.setModelIdA("different-model");
      request.setPrompts(List.of(new Prompt(InputRole.USER, "Continue")));

      when(sxsEvaluationPairRepository.findByIdAndUser(pairId, testUser))
          .thenReturn(Optional.of(pair));

      assertThrows(
          IllegalArgumentException.class,
          () -> service.continueSxsEvaluation(testUser, projectId, pairId, request));
    }
  }

  private ChatTurn createChatTurn(String turnId, String chatId) {
    Chat chat = new Chat();
    chat.setId(chatId);
    ChatTurn turn = new ChatTurn();
    turn.setId(turnId);
    turn.setChat(chat);
    return turn;
  }

  private ChatTurn createChatTurnWithModel(String turnId, String chatId, String modelId) {
    ChatTurn turn = createChatTurn(turnId, chatId);
    Model model = new Model();
    model.setId(modelId);
    ModelResponse response = new ModelResponse();
    response.setModel(model);
    turn.setModelResponse(response);
    return turn;
  }

  private void setupMocksForNewTurns() {
    ChatTurn newTurnA = createChatTurn("new-turn-A", "chat-A");
    ChatTurnDTO newTurnADto = new ChatTurnDTO();
    newTurnADto.setId("new-turn-A");

    when(playgroundInferenceService.runInferenceWithConversation(
            any(), any(), any(), any(), any(), any()))
        .thenReturn(newTurnADto);
    when(chatTurnRepository.findById(anyString())).thenReturn(Optional.of(newTurnA));
    when(sxsEvaluationPairRepository.save(any(SxsEvaluationPair.class)))
        .thenAnswer(i -> i.getArgument(0));
  }
}

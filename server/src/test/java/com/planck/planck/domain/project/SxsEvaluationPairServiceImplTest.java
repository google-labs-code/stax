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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.planck.planck.domain.chat.ChatRepository;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.chatturn.service.ChatTurnService;
import com.planck.planck.domain.evaluation.SXSHumanFeedbackRepository;
import com.planck.planck.domain.evaluation.SxsEvaluationPairRepository;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.ModelInputRepository;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.domain.workbook.dto.SXSWorkbookDTO;
import com.planck.planck.entitities.*;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.InputRole;
import com.planck.planck.enums.TagLinkTargetType;
import com.planck.planck.exceptions.IllegalArgumentException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SxsEvaluationPairServiceImplTest {

  @Mock private SxsEvaluationPairRepository sxsEvaluationPairRepository;
  @Mock private ChatTurnRepository chatTurnRepository;
  @Mock private ChatService chatService;
  @Mock private ModelService modelService;
  @Mock private ChatRepository chatRepository;
  @Mock private ModelInputRepository modelInputRepository;
  @Mock private TagLinkService tagLinkService;
  @Mock private ChatTurnService chatTurnService;
  @Mock private SXSHumanFeedbackRepository sxsHumanFeedbackRepository;
  @Mock private EvaluationContainerRepository evaluationContainerRepository;

  @InjectMocks private SxsEvaluationPairServiceImpl sxsEvaluationPairService;

  private User user;
  private EvaluationContainer sxsContainer;
  private EvaluationContainer pointwiseContainer;
  private Model model;
  private ChatTurn turnA, turnB, newTurnB;
  private Chat chatA, chatB, newChatB;
  private SxsEvaluationPair pair;
  private Prompt prompt;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId("test-user");

    sxsContainer = new Project();
    sxsContainer.setId("sxs-container");
    sxsContainer.setEvaluationType(EvaluationType.SXS);
    sxsContainer.setUser(user);

    pointwiseContainer = new Project();
    pointwiseContainer.setId("pointwise-container");
    pointwiseContainer.setEvaluationType(EvaluationType.POINTWISE);
    pointwiseContainer.setUser(user);

    model = new Model();
    model.setId("test-model");

    chatA = new Chat();
    chatA.setId("chat-A");
    chatA.setContainer(sxsContainer);

    chatB = new Chat();
    chatB.setId("chat-B");
    chatB.setContainer(sxsContainer);

    newChatB = new Chat();
    newChatB.setId("new-chat-B");

    turnA = new ChatTurn();
    turnA.setId("turn-A");
    turnA.setChat(chatA);
    turnA.setUser(user);
    turnA.setInputs(
        new ArrayList<>(List.of(new ModelInput(new Prompt(InputRole.USER, "Prompt A"), user))));
    turnA.setSequenceId(1);

    turnB = new ChatTurn();
    turnB.setId("turn-B");
    turnB.setChat(chatB);
    turnB.setUser(user);
    turnB.setInputs(
        new ArrayList<>(List.of(new ModelInput(new Prompt(InputRole.USER, "Prompt B"), user))));
    turnB.setSequenceId(1);

    newTurnB = new ChatTurn();
    newTurnB.setId("new-turn-B");
    newTurnB.setChat(newChatB);
    newTurnB.setSequenceId(1);

    pair =
        SxsEvaluationPair.builder()
            .id("pair-1")
            .container(sxsContainer)
            .user(user)
            .chatA(chatA)
            .chatTurnA(turnA)
            .chatB(chatB)
            .chatTurnB(turnB)
            .build();

    prompt = new Prompt(InputRole.USER, "Test Prompt");
  }

  @Test
  void createSxsPair_fromPrompt_Success() {
    when(evaluationContainerRepository.findByIdAndUser("sxs-container", user))
        .thenReturn(Optional.of(sxsContainer));
    when(modelInputRepository.save(any(ModelInput.class))).thenAnswer(inv -> inv.getArgument(0));
    when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));
    when(sxsEvaluationPairRepository.save(any(SxsEvaluationPair.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // Act
    SxsEvaluationPair result =
        sxsEvaluationPairService.createSxsPair(user, "sxs-container", prompt, "Expected");

    // Assert
    assertNotNull(result);
    assertNotNull(result.getChatTurnA());
    assertNotNull(result.getChatTurnB());
    assertEquals("Expected", result.getExpectedOutput());
    assertEquals("Test Prompt", result.getChatTurnA().getLastUserInput().getText());
    assertEquals("Test Prompt", result.getChatTurnB().getLastUserInput().getText());
    verify(chatRepository, times(2)).save(any(Chat.class));
    verify(sxsEvaluationPairRepository).save(any(SxsEvaluationPair.class));
  }

  @Test
  void createSxsPair_fromPrompt_ContainerNotSXS_ThrowsException() {
    when(evaluationContainerRepository.findByIdAndUser("pointwise-container", user))
        .thenReturn(Optional.of(pointwiseContainer));

    // Act & Assert
    assertThrows(
        NotFoundException.class,
        () -> {
          sxsEvaluationPairService.createSxsPair(user, "pointwise-container", prompt, null);
        });
  }

  @Test
  void createSxsPair_fromTurns_Success() {
    when(evaluationContainerRepository.findByIdAndUser("sxs-container", user))
        .thenReturn(Optional.of(sxsContainer));
    when(chatTurnRepository.findByIdAndUser("turn-A", user)).thenReturn(turnA);
    when(chatTurnRepository.findByIdAndUser("turn-B", user)).thenReturn(turnB);
    when(sxsEvaluationPairRepository.save(any(SxsEvaluationPair.class))).thenReturn(pair);

    // Act
    SxsEvaluationPair result =
        sxsEvaluationPairService.createSxsPair(
            user, "sxs-container", "turn-A", "turn-B", "Expected", null);

    // Assert
    assertNotNull(result);
    assertEquals(turnA, result.getChatTurnA());
    assertEquals(turnB, result.getChatTurnB());
    verify(sxsEvaluationPairRepository).save(any(SxsEvaluationPair.class));
  }

  @Test
  void createSxsPair_fromTurns_TurnANotFound_ThrowsException() {
    when(evaluationContainerRepository.findByIdAndUser("sxs-container", user))
        .thenReturn(Optional.of(sxsContainer));
    when(chatTurnRepository.findByIdAndUser("turn-A", user)).thenReturn(null);

    // Act & Assert
    assertThrows(
        NotFoundException.class,
        () -> {
          sxsEvaluationPairService.createSxsPair(
              user, "sxs-container", "turn-A", "turn-B", null, null);
        });
  }

  @Test
  void getSxsPairById_Success() {
    when(sxsEvaluationPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.of(pair));

    // Act
    SxsEvaluationPair result = sxsEvaluationPairService.getSxsPairById("pair-1", user);

    // Assert
    assertEquals(pair, result);
  }

  @Test
  void getSxsPairById_NotFound_ThrowsException() {
    when(sxsEvaluationPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        NotFoundException.class,
        () -> {
          sxsEvaluationPairService.getSxsPairById("pair-1", user);
        });
  }

  @Test
  void deleteSxsPair_Success() {
    when(sxsEvaluationPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.of(pair));
    when(chatTurnService.findByChatIn(anyList())).thenReturn(List.of(turnA, turnB));

    // Act
    sxsEvaluationPairService.deleteSxsPair("pair-1", user);

    // Assert
    verify(sxsEvaluationPairRepository).findByIdAndUser("pair-1", user);
    verify(chatTurnService).findByChatIn(List.of(chatA, chatB));
    verify(chatService).delete(List.of(turnA, turnB));
    verify(sxsHumanFeedbackRepository).deleteByPairIdList(List.of("pair-1"));
  }

  @Test
  void generateAndSetTurnB_TurnBExists_AssignsModel() {
    turnB.setModelResponse(null);
    pair.setChatTurnB(turnB);

    when(sxsEvaluationPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.of(pair));
    when(modelService.getModelForUser(user, "test-model")).thenReturn(model);
    when(chatTurnRepository.save(any(ChatTurn.class))).thenReturn(turnB);
    when(sxsEvaluationPairRepository.save(any(SxsEvaluationPair.class))).thenReturn(pair);

    // Act
    SxsEvaluationPair result =
        sxsEvaluationPairService.generateAndSetTurnB(
            user, "sxs-container", "pair-1", "test-model", false);

    // Assert
    assertNotNull(result.getChatTurnB().getModelResponse());
    assertEquals(model, result.getChatTurnB().getModelResponse().getModel());
    verify(chatTurnRepository).save(turnB);
    verify(chatService, never()).duplicateChatNewLastModelResponse(any(), any());
  }

  @Test
  void generateAndSetTurnB_TurnBNew_CreateEmptyLastResponse() {
    pair.setChatTurnB(null);
    pair.setChatB(null);

    when(sxsEvaluationPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.of(pair));
    when(modelService.getModelForUser(user, "test-model")).thenReturn(model);
    when(chatService.duplicateChatNewLastModelResponse(turnA, model)).thenReturn(List.of(newTurnB));
    when(sxsEvaluationPairRepository.save(any(SxsEvaluationPair.class))).thenReturn(pair);

    // Act
    SxsEvaluationPair result =
        sxsEvaluationPairService.generateAndSetTurnB(
            user, "sxs-container", "pair-1", "test-model", true);

    // Assert
    assertEquals(newTurnB, result.getChatTurnB());
    assertEquals(newChatB, result.getChatB());
    verify(chatService).duplicateChatNewLastModelResponse(turnA, model);
    verify(chatService, never()).duplicateChatWithNewModel(any(), any());
  }

  @Test
  void generateAndSetTurnB_TurnBNew_DuplicateWithNewModel() {
    pair.setChatTurnB(null);
    pair.setChatB(null);

    when(sxsEvaluationPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.of(pair));
    when(modelService.getModelForUser(user, "test-model")).thenReturn(model);
    when(chatService.duplicateChatWithNewModel(turnA, model)).thenReturn(List.of(newTurnB));
    when(sxsEvaluationPairRepository.save(any(SxsEvaluationPair.class))).thenReturn(pair);

    // Act
    SxsEvaluationPair result =
        sxsEvaluationPairService.generateAndSetTurnB(
            user, "sxs-container", "pair-1", "test-model", false);

    // Assert
    assertEquals(newTurnB, result.getChatTurnB());
    assertEquals(newChatB, result.getChatB());
    verify(chatService, never()).duplicateChatNewLastModelResponse(any(), any());
    verify(chatService).duplicateChatWithNewModel(turnA, model);
  }

  @Test
  void generateAndSetTurnB_SideBHasResponse_ThrowsException() {
    ModelResponse responseB = new ModelResponse();
    responseB.setText("I already have text");
    turnB.setModelResponse(responseB);

    when(sxsEvaluationPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.of(pair));

    // Act & Assert
    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () -> {
              sxsEvaluationPairService.generateAndSetTurnB(
                  user, "sxs-container", "pair-1", "test-model", false);
            });
    assertEquals("Side B already has a model with response", ex.getMessage());
  }

  @Test
  void getSXSRows_Success() {
    when(evaluationContainerRepository.findByIdAndUser("sxs-container", user))
        .thenReturn(Optional.of(sxsContainer));
    Page<SxsEvaluationPair> page = new PageImpl<>(List.of(pair), PageRequest.of(0, 10), 1);
    when(sxsEvaluationPairRepository.findByContainerWithLatestTurns(
            eq(sxsContainer), eq(user), any(Pageable.class)))
        .thenReturn(page);

    // Act
    SXSWorkbookDTO result =
        sxsEvaluationPairService.getSXSRows(user, "sxs-container", 10, 0, null, null, null);

    // Assert
    assertEquals(1, result.getTotalSize());
    assertEquals(1, result.getSxsRows().size());
    assertNull(result.getNextPageToken());
  }

  @Test
  void updateSxsPair_Success() {
    when(sxsEvaluationPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.of(pair));
    when(modelInputRepository.save(any(ModelInput.class))).thenAnswer(inv -> inv.getArgument(0));
    when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));
    when(sxsEvaluationPairRepository.save(any(SxsEvaluationPair.class))).thenReturn(pair);

    Map<String, String> newVars = Map.of("key", "value");
    List<String> newTags = List.of("tag1");

    // Act
    SxsEvaluationPair result =
        sxsEvaluationPairService.updateSxsPair(
            "pair-1", "sxs-container", user, "New Input", "New Output", newVars, newTags);

    // Assert
    assertEquals("New Output", result.getExpectedOutput());
    assertEquals(newVars, result.getVariables());

    ArgumentCaptor<ModelInput> inputCaptor = ArgumentCaptor.forClass(ModelInput.class);
    verify(modelInputRepository, times(4)).save(inputCaptor.capture());

    assertTrue(
        inputCaptor.getAllValues().stream().allMatch(mi -> mi.getText().equals("New Input")));
    assertTrue(
        inputCaptor.getAllValues().stream()
            .allMatch(mi -> mi.getExpectedOutput().equals("New Output")));

    verify(chatRepository, times(2)).save(any(Chat.class));
    verify(tagLinkService)
        .updateTagsForEntity(user, newTags, TagLinkTargetType.CHAT, chatA.getId());
    verify(tagLinkService)
        .updateTagsForEntity(user, newTags, TagLinkTargetType.CHAT, chatB.getId());
  }

  @Test
  void updateSxsPair_InputWhenResponseExists_ThrowsException() {
    ModelResponse responseA = new ModelResponse();
    responseA.setText("Response exists");
    turnA.setModelResponse(responseA);

    when(sxsEvaluationPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.of(pair));

    // Act & Assert
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              sxsEvaluationPairService.updateSxsPair(
                  "pair-1", "sxs-container", user, "New Input", null, null, null);
            });
    assertTrue(ex.getMessage().contains("Cannot update input: a model response already exists"));
  }

  @Test
  void synchronizeSxsPairChats_AppendToB_Success() {
    ChatTurn turnA_seq2 = new ChatTurn();
    turnA_seq2.setId("turn-A2");

    when(chatService.getFullChatHistory(turnA)).thenReturn(List.of(turnA, turnA_seq2));
    when(chatService.getFullChatHistory(turnB)).thenReturn(List.of(turnB));
    when(chatService.appendMissingTurns(chatB, List.of(turnA, turnA_seq2), sxsContainer, 1))
        .thenReturn(newTurnB);

    // Act
    SxsEvaluationPair result =
        sxsEvaluationPairService.synchronizeSxsPairChats(pair, user, null, null);

    // Assert
    verify(chatService).appendMissingTurns(chatB, List.of(turnA, turnA_seq2), sxsContainer, 1);
    assertEquals(newTurnB, result.getChatTurnB());
  }

  @Test
  void synchronizeSxsPairChats_CopyResponseToB_Success() {
    ModelResponse responseA = new ModelResponse();
    responseA.setText("Text A");
    turnA.setModelResponse(responseA);
    turnB.setModelResponse(null);

    when(chatService.getFullChatHistory(turnA)).thenReturn(List.of(turnA));
    when(chatService.getFullChatHistory(turnB)).thenReturn(List.of(turnB));
    when(modelService.getModelForUser(user, "model-B")).thenReturn(model);

    // Act
    sxsEvaluationPairService.synchronizeSxsPairChats(pair, user, null, "model-B");

    // Assert
    verify(chatService, never()).appendMissingTurns(any(), any(), any(), anyInt());
    verify(chatService).copyResponseToTurn(turnA, turnB, model);
  }

  @Test
  void createSxsEvaluationPairs_Success() {
    when(chatService.duplicateChatsForSideB(List.of(chatA), user))
        .thenReturn(Map.of(chatA.getId(), chatB));

    // Act
    sxsEvaluationPairService.createSxsEvaluationPairs(user, List.of(chatA), sxsContainer);

    // Assert
    verify(chatService).duplicateChatsForSideB(List.of(chatA), user);
    verify(sxsEvaluationPairRepository).saveAll(anyList());
  }
}

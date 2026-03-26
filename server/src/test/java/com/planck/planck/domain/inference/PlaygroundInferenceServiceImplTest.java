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
import static org.mockito.Mockito.*;

import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.inference.service.InferenceService;
import com.planck.planck.domain.inference.service.PlaygroundInferenceServiceImpl;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.service.ModelInputService;
import com.planck.planck.domain.modelresponse.service.ModelResponseService;
import com.planck.planck.domain.project.ProjectService;
import com.planck.planck.entitities.*;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaygroundInferenceServiceImplTest {

  @Mock private ChatService chatService;
  @Mock private ModelInputService modelInputService;
  @Mock private ModelResponseService modelResponseService;
  @Mock private ModelService modelService;
  @Mock private ProjectService projectService;
  @Mock private InferenceService inferenceService;

  @InjectMocks private PlaygroundInferenceServiceImpl playgroundInferenceService;

  private User user;
  private String projectId;
  private String modelId;
  private Project mockProject;
  private Model mockModel;
  private Chat mockChat;
  private ChatTurn mockLatestTurn;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId("user-1");
    projectId = "project-1";
    modelId = "model-1";

    mockProject = new Project();
    mockProject.setId(projectId);
    mockProject.setName("Test Project");

    mockModel = new Model();
    mockModel.setId(modelId);
    mockModel.setName("Test Model");

    mockChat = new Chat();
    mockChat.setId("mockChatId");
    mockChat.setVariables(new HashMap<>());
    mockChat.setTurns(new ArrayList<>());

    mockLatestTurn = new ChatTurn();
    mockLatestTurn.setSequenceId(1);
    mockLatestTurn.setChat(mockChat);
    mockLatestTurn.setInputs(new ArrayList<>());
  }

  @Test
  void testRunInferenceWithConversation_WithEmptyPrompts() {
    mockChat.setTurns(Collections.singletonList(mockLatestTurn));

    when(projectService.getProjectForUser(user, projectId)).thenReturn(mockProject);
    when(modelService.getModelForUser(user, modelId)).thenReturn(mockModel);
    when(chatService.createChat(user, mockProject)).thenReturn(mockChat);
    when(inferenceService.addModelResponseToChatturn(
            user, mockProject, modelId, mockChat.getTurns(), mockLatestTurn))
        .thenReturn(new ChatTurnDTO());

    ChatTurnDTO result =
        playgroundInferenceService.runInferenceWithConversation(
            user, projectId, modelId, null, Collections.emptyList(), null);

    assertNotNull(result);
    verify(inferenceService)
        .addModelResponseToChatturn(
            user, mockProject, modelId, mockChat.getTurns(), mockLatestTurn);
  }

  @Test
  void testRunInferenceWithConversation_WithInvalidPreviousTurnId() {
    when(chatService.getChatTurn("invalid-turn-id", user))
        .thenThrow(new NotFoundException("Previous ChatTurn not found"));

    assertThrows(
        NotFoundException.class,
        () ->
            playgroundInferenceService.runInferenceWithConversation(
                user, projectId, modelId, "invalid-turn-id", null, null));
  }

  @Test
  void testRunInferenceWithConversation_WithValidPrompts() {
    mockLatestTurn.setModelResponse(new ModelResponse());
    mockChat.setTurns(new ArrayList<>(List.of(mockLatestTurn)));

    Prompt assistantPrompt = new Prompt(InputRole.ASSISTANT, "Assistant prompt");
    Prompt userPrompt = new Prompt(InputRole.USER, "User prompt");
    List<Prompt> allPrompts = new ArrayList<>(Arrays.asList(assistantPrompt, userPrompt));

    when(projectService.getProjectForUser(user, projectId)).thenReturn(mockProject);
    when(modelService.getModelForUser(user, modelId)).thenReturn(mockModel);
    when(chatService.createChat(user, mockProject)).thenReturn(mockChat);
    when(modelInputService.saveAll(any(), anyList(), any())).thenReturn(new ArrayList<>());
    when(chatService.saveChatTurn(any(), any())).thenReturn(new ChatTurn());
    when(inferenceService.addModelResponseToChatturn(any(), any(), any(), anyList(), any()))
        .thenReturn(new ChatTurnDTO());

    ChatTurnDTO result =
        playgroundInferenceService.runInferenceWithConversation(
            user, projectId, modelId, null, allPrompts, null);

    assertNotNull(result);
    verify(chatService, times(1)).createChat(user, mockProject);
    verify(modelInputService, times(1)).saveAll(any(), anyList(), any());
  }

  @Test
  void testRunInferenceWithConversation_WithVariables() {
    mockChat.setTurns(Collections.singletonList(mockLatestTurn));
    Map<String, String> variables = new HashMap<>();
    variables.put("key", "value");

    Prompt prompt = new Prompt(InputRole.USER, "User prompt");
    List<Prompt> prompts = Collections.singletonList(prompt);

    when(projectService.getProjectForUser(user, projectId)).thenReturn(mockProject);
    when(modelService.getModelForUser(user, modelId)).thenReturn(mockModel);
    when(chatService.createChat(user, mockProject)).thenReturn(mockChat);
    when(chatService.addOrUpdateVariables("mockChatId", user, variables)).thenReturn(mockChat);
    when(modelInputService.saveAll(any(), anyList(), any())).thenReturn(new ArrayList<>());

    playgroundInferenceService.runInferenceWithConversation(
        user, projectId, modelId, null, prompts, variables);

    verify(chatService).addOrUpdateVariables("mockChatId", user, variables);
  }

  @Test
  void testRunInferenceWithConversation_WithOutVariables() {
    mockChat.setTurns(Collections.singletonList(mockLatestTurn));
    Map<String, String> variables = new HashMap<>();

    Prompt prompt = new Prompt(InputRole.USER, "User prompt");
    List<Prompt> prompts = Collections.singletonList(prompt);

    when(projectService.getProjectForUser(user, projectId)).thenReturn(mockProject);
    when(modelService.getModelForUser(user, modelId)).thenReturn(mockModel);
    when(chatService.createChat(user, mockProject)).thenReturn(mockChat);
    when(modelInputService.saveAll(any(), anyList(), any())).thenReturn(new ArrayList<>());

    playgroundInferenceService.runInferenceWithConversation(
        user, projectId, modelId, null, prompts, variables);

    verify(chatService, never()).addOrUpdateVariables(anyString(), any(User.class), anyMap());
  }

  @Test
  void testRunInferenceWithConversation_WithMultiplePrompts() {
    List<ChatTurn> turnsList = new ArrayList<>();
    turnsList.add(mockLatestTurn);
    mockChat.setTurns(turnsList);

    Map<String, String> variables = new HashMap<>();
    variables.put("key", "value");

    Prompt userPrompt = new Prompt(InputRole.USER, "User prompt");
    Prompt assistantPrompt = new Prompt(InputRole.ASSISTANT, "Assistant prompt");
    Prompt userPrompt1 = new Prompt(InputRole.USER, "User prompt");
    Prompt assistantPrompt1 = new Prompt(InputRole.ASSISTANT, "Assistant prompt");
    List<Prompt> prompts =
        new ArrayList<>(Arrays.asList(assistantPrompt, userPrompt, assistantPrompt1, userPrompt1));

    when(projectService.getProjectForUser(user, projectId)).thenReturn(mockProject);
    when(modelService.getModelForUser(user, modelId)).thenReturn(mockModel);
    when(chatService.createChat(user, mockProject)).thenReturn(mockChat);
    when(chatService.addOrUpdateVariables("mockChatId", user, variables)).thenReturn(mockChat);
    when(modelInputService.saveAll(any(), anyList(), any())).thenReturn(new ArrayList<>());

    playgroundInferenceService.runInferenceWithConversation(
        user, projectId, modelId, null, prompts, variables);

    verify(chatService).addOrUpdateVariables("mockChatId", user, variables);
  }

  @Test
  void testRunInferenceWithConversation_ShouldCreateNewChatForNullPreviousTurnId() {
    mockChat.setTurns(new ArrayList<>());
    when(chatService.createChat(user, mockProject)).thenReturn(mockChat);
    when(projectService.getProjectForUser(any(User.class), eq(projectId))).thenReturn(mockProject);
    when(modelService.getModelForUser(any(User.class), eq(modelId))).thenReturn(mockModel);
    when(inferenceService.addModelResponseToChatturn(any(), any(), any(), anyList(), any()))
        .thenReturn(new ChatTurnDTO());

    ChatTurnDTO result =
        playgroundInferenceService.runInferenceWithConversation(
            user, projectId, modelId, null, null, null);

    verify(chatService).createChat(user, mockProject);
    assertNotNull(result);
  }

  @Test
  void testRunInferenceWithConversation_ShouldThrowForInvalidPreviousTurnId() {
    String invalidPreviousTurnId = "invalid-turn-id";
    when(chatService.getChatTurn(invalidPreviousTurnId, user)).thenReturn(null);

    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () ->
                playgroundInferenceService.runInferenceWithConversation(
                    user, projectId, modelId, invalidPreviousTurnId, null, null));

    assertEquals("Previous ChatTurn not found: invalid-turn-id", exception.getMessage());
  }

  @Test
  void testRunInferenceWithConversation_ShouldProcessAssistantPrompt() {
    List<ChatTurn> turns = new ArrayList<>();
    turns.add(mockLatestTurn);
    mockChat.setTurns(turns);

    Prompt assistantPrompt = new Prompt(InputRole.ASSISTANT, "Assistant message");
    Prompt userPrompt = new Prompt(InputRole.USER, "User input");
    Prompt assistantPrompt1 = new Prompt(InputRole.ASSISTANT, "Assistant message");
    Prompt userPrompt1 = new Prompt(InputRole.USER, "User input");
    List<Prompt> allPrompts =
        new ArrayList<>(List.of(assistantPrompt, userPrompt, assistantPrompt1, userPrompt1));

    ModelInput modelInput = new ModelInput(userPrompt, user);
    mockLatestTurn.setInputs(List.of(modelInput));

    ModelResponse savedResponse =
        new ModelResponse("Saved response text", mockModel, user, mockProject);
    when(projectService.getProjectForUser(user, projectId)).thenReturn(mockProject);
    when(modelService.getModelForUser(user, modelId)).thenReturn(mockModel);
    when(chatService.createChat(user, mockProject)).thenReturn(mockChat);
    when(modelResponseService.saveModelResponse(any(ModelResponse.class)))
        .thenReturn(savedResponse);
    when(inferenceService.addModelResponseToChatturn(any(), any(), any(), anyList(), any()))
        .thenReturn(new ChatTurnDTO());

    ChatTurnDTO result =
        playgroundInferenceService.runInferenceWithConversation(
            user, projectId, modelId, null, allPrompts, null);

    assertNotNull(result);
    verify(chatService).saveChatTurn(mockLatestTurn, user);
    assertNotNull(mockLatestTurn.getModelResponse());
    assertEquals("Saved response text", mockLatestTurn.getModelResponse().getText());
    assertEquals(3, allPrompts.size());
    assertEquals(userPrompt, allPrompts.get(0));
  }

  @Test
  void testRunInferenceWithConversation_ShouldThrowExceptionOnOverwriteModelResponse() {
    ModelResponse existingResponse = new ModelResponse();
    existingResponse.setText("Existing response text");

    mockLatestTurn.setModelResponse(existingResponse);
    mockChat.setTurns(new ArrayList<>(List.of(mockLatestTurn)));

    Prompt assistantPrompt = new Prompt(InputRole.ASSISTANT, "New Assistant message");
    List<Prompt> allPrompts = new ArrayList<>(List.of(assistantPrompt));

    when(projectService.getProjectForUser(user, projectId)).thenReturn(mockProject);
    when(modelService.getModelForUser(user, modelId)).thenReturn(mockModel);
    when(chatService.createChat(user, mockProject)).thenReturn(mockChat);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                playgroundInferenceService.runInferenceWithConversation(
                    user, projectId, modelId, null, allPrompts, null));

    assertEquals(
        "Attempting to override existing model response is not allowed.", exception.getMessage());

    verifyNoInteractions(modelResponseService);
    verify(chatService, never()).saveChatTurn(any(), any());
    assertFalse(allPrompts.isEmpty());
  }

  @Test
  void testRunInferenceWithConversation_ShouldRemoveProcessedAssistantPrompt() {
    mockLatestTurn.setModelResponse(null);
    mockChat.setTurns(new ArrayList<>(List.of(mockLatestTurn)));

    Prompt assistantPrompt = new Prompt(InputRole.ASSISTANT, "New Assistant message");
    Prompt secondPrompt = new Prompt(InputRole.USER, "User prompt");
    List<Prompt> allPrompts = new ArrayList<>(List.of(assistantPrompt, secondPrompt));

    ModelResponse savedResponse =
        new ModelResponse("Saved response text", mockModel, user, mockProject);
    when(projectService.getProjectForUser(user, projectId)).thenReturn(mockProject);
    when(modelService.getModelForUser(user, modelId)).thenReturn(mockModel);
    when(chatService.createChat(user, mockProject)).thenReturn(mockChat);
    when(modelResponseService.saveModelResponse(any())).thenReturn(savedResponse);
    when(inferenceService.addModelResponseToChatturn(any(), any(), any(), anyList(), any()))
        .thenReturn(new ChatTurnDTO());

    ChatTurnDTO result =
        playgroundInferenceService.runInferenceWithConversation(
            user, projectId, modelId, null, allPrompts, null);

    assertNotNull(result);
    assertEquals(1, allPrompts.size());
    assertEquals(secondPrompt, allPrompts.get(0));
  }

  @Test
  void testRunInferenceWithConversation_ShouldThrowExceptionForLastPromptEndingWithAssistant() {
    mockLatestTurn.setModelResponse(new ModelResponse());
    mockChat.setTurns(new ArrayList<>(List.of(mockLatestTurn)));

    Prompt userPrompt = new Prompt(InputRole.USER, "User prompt");
    Prompt assistantPrompt = new Prompt(InputRole.ASSISTANT, "Assistant response");
    List<Prompt> allPrompts = new ArrayList<>(List.of(userPrompt, assistantPrompt));

    when(projectService.getProjectForUser(user, projectId)).thenReturn(mockProject);
    when(modelService.getModelForUser(user, modelId)).thenReturn(mockModel);
    when(chatService.createChat(user, mockProject)).thenReturn(mockChat);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                playgroundInferenceService.runInferenceWithConversation(
                    user, projectId, modelId, null, allPrompts, null));

    assertEquals(
        "Cannot run inference on a turn that ends with an assistant message.",
        exception.getMessage());
  }
}

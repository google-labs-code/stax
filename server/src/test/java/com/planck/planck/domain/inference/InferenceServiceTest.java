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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.planck.planck.domain.apikeys.service.ApiKeysService;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.evaluationmonitoring.EvaluationMonitoringService;
import com.planck.planck.domain.inference.dto.InferenceDTO;
import com.planck.planck.domain.inference.service.InferencePreparationService;
import com.planck.planck.domain.inference.service.InferenceServiceImpl;
import com.planck.planck.domain.inferencemonitoring.InferenceMonitoringService;
import com.planck.planck.domain.inferencestatus.InferenceStatusService;
import com.planck.planck.domain.job.JobStatusService;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.service.ModelInputService;
import com.planck.planck.domain.modelresponse.service.ModelResponseService;
import com.planck.planck.domain.project.ProjectService;
import com.planck.planck.domain.pubsub.InferencePublisherService;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.domain.user.UserService;
import com.planck.planck.entitities.*;
import com.planck.planck.enums.InferenceStatusEnum;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.ChatProviderFactory;
import com.planck.planck.llmproviders.ChatProviderStrategy;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import com.planck.planck.llmproviders.dto.Prompt;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InferenceServiceImplTest {

  // --- Mocks for all dependencies ---
  @Mock private ModelService modelService;
  @Mock private ProjectService projectService;
  @Mock private ApiKeysService apiKeysService;
  @Mock private InferenceMonitoringService inferenceMonitoringService;
  @Mock private InferenceStatusService inferenceStatusService;
  @Mock private ModelResponseService modelResponseService;
  @Mock private ModelInputService modelInputService;
  @Mock private ChatService chatService;
  @Mock private TagLinkService tagLinkService;
  @Mock private ChatProviderFactory chatProviderFactory;
  @Mock private ChatProviderStrategy chatProviderStrategy;
  // New mocks required by the refactored service
  @Mock private JobStatusService jobStatusService;
  @Mock private UserService userService;
  @Mock private EvaluationMonitoringService evaluationMonitoringService;
  @Mock private InferencePublisherService publisherService;
  @Mock private InferencePreparationService inferencePreparationService;

  @InjectMocks private InferenceServiceImpl inferenceService;

  private User testUser;
  private Project testProject;
  private Model testModel;
  private List<Prompt> testPrompts;
  private Map<String, String> variables;
  private String projectId = "proj-123";
  private String modelId = "model-abc";
  private String chatTurnId = "ct-xyz";

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("user-1");

    testProject = new Project();
    testProject.setId(projectId);
    testProject.setUser(testUser);

    testModel = new Model();
    testModel.setId(modelId);
    testModel.setDeprecated(false);

    testPrompts = List.of(new Prompt(InputRole.USER, "Hello there!"));
    variables = Map.of("key1", "value1");
  }

  private ChatResponseWithLatency getChatResponse() {
    ChatResponse response =
        ChatResponse.builder()
            .aiMessage(new AiMessage("Hi back!"))
            .tokenUsage(new TokenUsage())
            .build();
    return new ChatResponseWithLatency(response, 1000L);
  }

  // --- Tests for runInference (new chat) ---

  @Test
  void runInference_Success() {
    // Arrange
    ChatTurn initialChatTurn = new ChatTurn();
    initialChatTurn.setId(chatTurnId);
    initialChatTurn.setChat(new Chat());

    when(projectService.getProjectForUser(testUser, projectId)).thenReturn(testProject);
    when(modelService.getModelForUser(testUser, modelId)).thenReturn(testModel);
    when(chatService.createChat(null, testProject, testUser, variables)).thenReturn(new Chat());
    when(modelInputService.savePrompt(eq(testUser), any(Prompt.class)))
        .thenReturn(new ModelInput());
    when(chatService.saveChatTurn(any(ChatTurn.class), eq(testUser))).thenReturn(initialChatTurn);
    when(chatProviderFactory.getStrategy(testModel, testUser)).thenReturn(chatProviderStrategy);
    when(chatProviderStrategy.chat(anyList())).thenReturn(getChatResponse());
    when(inferenceMonitoringService.saveInferenceMonitoring(any(), any(), any(), any(), any()))
        .thenReturn(new InferenceMonitoring());
    when(modelResponseService.saveModelResponse(any(ModelResponse.class)))
        .thenReturn(new ModelResponse());

    // Act
    ChatTurnDTO resultDTO =
        inferenceService.runInference(testUser, projectId, modelId, testPrompts, null, variables);

    // Assert
    assertNotNull(resultDTO);
    assertEquals(chatTurnId, resultDTO.getId());
    assertNotNull(resultDTO.getResponse());
    assertEquals("Hi back!", resultDTO.getResponse().getText());

    verify(projectService).getProjectForUser(testUser, projectId);
    verify(chatService).createChat(null, testProject, testUser, variables);
    verify(modelInputService).savePrompt(eq(testUser), any(Prompt.class));
    verify(chatProviderStrategy).chat(anyList());
    verify(modelResponseService).saveModelResponse(any(ModelResponse.class));
    verify(tagLinkService).syncInferenceMonitoringTags(any(), eq(testUser));
  }

  @Test
  void runInference_ProjectNotFound() {
    when(projectService.getProjectForUser(testUser, projectId))
        .thenThrow(new NotFoundException("Project not found"));

    assertThrows(
        NotFoundException.class,
        () ->
            inferenceService.runInference(
                testUser, projectId, modelId, testPrompts, null, variables));

    verifyNoInteractions(modelService, chatService, modelInputService);
  }

  @Test
  void runInference_ModelDeprecated() {
    // Arrange
    testModel.setDeprecated(true);
    ChatTurn initialChatTurn = new ChatTurn();
    initialChatTurn.setId(chatTurnId);

    when(projectService.getProjectForUser(testUser, projectId)).thenReturn(testProject);
    when(chatService.createChat(any(), any(), any(), any())).thenReturn(new Chat());
    when(chatService.saveChatTurn(any(ChatTurn.class), eq(testUser))).thenReturn(initialChatTurn);
    when(modelService.getModelForUser(testUser, modelId)).thenReturn(testModel);

    // Act & Assert
    assertThrows(
        IllegalInputException.class,
        () ->
            inferenceService.runInference(
                testUser, projectId, modelId, testPrompts, null, variables));

    verify(chatService)
        .saveChatTurn(any(ChatTurn.class), eq(testUser)); // Verifies the input part was saved
    verifyNoInteractions(chatProviderFactory); // Verifies LLM part was not called
  }

  @Test
  void runInference_PromptsEmpty() {
    when(projectService.getProjectForUser(testUser, projectId)).thenReturn(testProject);

    assertThrows(
        NotFoundException.class,
        () ->
            inferenceService.runInference(
                testUser, projectId, modelId, Collections.emptyList(), null, variables));
  }

  @Test
  void runInference_LlmFails() {
    // Arrange
    ChatTurn initialChatTurn = new ChatTurn();
    initialChatTurn.setId(chatTurnId);
    initialChatTurn.setChat(new Chat());
    ModelResponse response = new ModelResponse();
    response.setContainer(testProject);
    when(projectService.getProjectForUser(testUser, projectId)).thenReturn(testProject);
    when(modelService.getModelForUser(testUser, modelId)).thenReturn(testModel);
    when(chatService.createChat(any(), any(), any(), any())).thenReturn(new Chat());
    when(chatService.saveChatTurn(any(ChatTurn.class), eq(testUser))).thenReturn(initialChatTurn);
    when(chatProviderFactory.getStrategy(testModel, testUser)).thenReturn(chatProviderStrategy);
    when(chatProviderStrategy.chat(anyList())).thenThrow(new RuntimeException("LLM API Error"));
    when(inferenceStatusService.createOrUpdateInferenceStatus(
            any(InferenceDTO.class), eq(InferenceStatusEnum.FAILED)))
        .thenReturn(new InferenceStatus());
    when(modelResponseService.createModelResponse(testUser, testProject, testModel))
        .thenReturn(response);

    // Act
    ChatTurnDTO resultDTO =
        inferenceService.runInference(testUser, projectId, modelId, testPrompts, null, variables);

    // Assert
    assertNotNull(resultDTO);
    assertNotNull(resultDTO.getResponse()); // A shell response should be created
    assertNull(resultDTO.getResponse().getText()); // But it should be empty
    verify(inferenceStatusService)
        .createOrUpdateInferenceStatus(any(InferenceDTO.class), eq(InferenceStatusEnum.FAILED));
    verifyNoInteractions(
        inferenceMonitoringService, tagLinkService); // These are not called on failure
  }

  // --- Tests for runEvaluationInference ---

  @Test
  void runEvaluationInference_Success() {
    ModelInput modelInput = new ModelInput();
    modelInput.setText("text");
    modelInput.setRole(InputRole.USER);

    List<ModelInput> evalInputs = List.of(modelInput);

    when(chatProviderFactory.getStrategy(testModel, testUser)).thenReturn(chatProviderStrategy);
    when(chatProviderStrategy.chat(anyList())).thenReturn(getChatResponse());

    ChatResponseWithLatency result =
        inferenceService.runEvaluationInference(testUser, testModel, evalInputs);

    assertNotNull(result);
    assertEquals("Hi back!", result.getReply());
    verifyNoInteractions(projectService, chatService, inferenceMonitoringService);
  }
}

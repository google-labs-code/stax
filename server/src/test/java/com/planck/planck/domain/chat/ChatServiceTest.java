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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.config.ApplicationLimits;
import com.planck.planck.config.ContainerLimitProvider;
import com.planck.planck.domain.chat.dto.ChatDTO;
import com.planck.planck.domain.chat.service.ChatServiceImpl;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.evaluation.SXSHumanFeedbackRepository;
import com.planck.planck.domain.evaluation.ScoreV2Repository;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusRepository;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorServiceImpl;
import com.planck.planck.domain.inferencemonitoring.InferenceMonitoringRepository;
import com.planck.planck.domain.inferencestatus.InferenceStatusRepository;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.ModelInputRepository;
import com.planck.planck.domain.modelresponse.ModelResponseRepository;
import com.planck.planck.domain.project.EvaluationContainerRepository;
import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationMonitoring;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.InferenceStatus;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.ScoreV2;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InputRole;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.exceptions.UserQuotaExceededException;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @Mock private ChatTurnRepository chatTurnRepository;

  @Mock private ApplicationLimits appLimts;
  @Mock private ContainerLimitProvider containerLimitProvider;
  @Mock private EvaluationContainerRepository evaluationContainerRepository;

  @Mock private ScoreV2Repository scoreV2Repository;
  @Mock private ChatRepository chatRepository;

  @Mock private ModelInputRepository modelInputRepository;
  @Mock private ModelResponseRepository modelResponseRepository;
  @Mock private ModelService modelService;
  @Mock private HumanEvaluatorServiceImpl humanEvaluatorService;
  @Mock private InferenceStatusRepository inferenceStatusRepository;
  @Mock private InferenceMonitoringRepository inferenceMonitoringRepository;

  @Mock private EvaluationStatusRepository evaluationStatusRepository;
  @Mock private SXSHumanFeedbackRepository sxsHumanFeedbackRepository;
  @InjectMocks private ChatServiceImpl chatService;

  private User testUser;
  private Project testProject;
  private Model model;
  private String chatId = "chat-123";
  private String chatTurnId1 = "chat-turn-abc";
  private String chatTurnId2 = "chat-turn-def";
  private Chat testChat;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("user-xyz");

    testProject = new Project();
    testProject.setId("proj-abc");
    testProject.setUser(testUser);

    model = new Model();
    model.setId("model-123");
    model.setName("Test Model");

    testChat = new Chat();
    testChat.setId(chatId);
    testChat.setUser(testUser);
    testChat.setContainer(testProject);

    Map<String, String> initialVariables = new HashMap<>();
    initialVariables.put("key1", "val1");
    initialVariables.put("key2", "val2");
    testChat.setVariables(initialVariables);
  }

  // --- Tests for getChatDTO ---

  @Test
  void getChatDTO_Success_ReturnsChatDTO() {
    // Arrange
    ChatTurn turn1 = createTestChatTurn(chatTurnId1, chatId, 1);
    ChatTurn turn2 = createTestChatTurn(chatTurnId2, chatId, 2);
    List<ChatTurn> chatTurns = Arrays.asList(turn1, turn2);
    Map<String, List<TagDTO>> tagDtoMap = new HashMap<>();
    tagDtoMap.put(chatTurnId1, new ArrayList<>());

    when(chatTurnRepository.findByChatIdAndUserIdOrderBySequenceId(chatId, testUser, null, null))
        .thenReturn(chatTurns);

    // Act
    ChatDTO result = new ChatDTO(chatService.getChat(chatId, testUser, null));

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getChatTurns().size());
    // Add more specific assertions about DTO content if needed
    // e.g., check tokens, latency calculation (though that's in ChatDTO constructor)
    verify(chatTurnRepository).findByChatIdAndUserIdOrderBySequenceId(chatId, testUser, null, null);
  }

  @Test
  void getChatDTO_NotFound_ThrowsIllegalArgumentException() {
    // Arrange
    when(chatTurnRepository.findByChatIdAndUserIdOrderBySequenceId(chatId, testUser, null, null))
        .thenReturn(Collections.emptyList());

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              chatService.getChatDTO(chatId, testUser, null);
            });

    assertEquals("Chat not found for id: " + chatId, exception.getMessage());
    verify(chatTurnRepository).findByChatIdAndUserIdOrderBySequenceId(chatId, testUser, null, null);
  }

  @Test
  void getChatDTO_RepositoryReturnsNull_ThrowsIllegalArgumentException() {
    // Arrange
    when(chatTurnRepository.findByChatIdAndUserIdOrderBySequenceId(chatId, testUser, null, null))
        .thenReturn(null);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              chatService.getChatDTO(chatId, testUser, null);
            });

    assertEquals("Chat not found for id: " + chatId, exception.getMessage());
    verify(chatTurnRepository).findByChatIdAndUserIdOrderBySequenceId(chatId, testUser, null, null);
  }

  // --- Tests for getChat ---

  @Test
  void getChat_Success_ReturnsListOfChatTurns() {
    // Arrange
    ChatTurn turn1 = createTestChatTurn(chatTurnId1, chatId, 1);
    ChatTurn turn2 = createTestChatTurn(chatTurnId2, chatId, 2);
    List<ChatTurn> expectedChatTurns = Arrays.asList(turn1, turn2);

    when(chatTurnRepository.findByChatIdAndUserIdOrderBySequenceId(chatId, testUser, null, null))
        .thenReturn(expectedChatTurns);

    // Act
    List<ChatTurn> result = chatService.getChat(chatId, testUser);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(expectedChatTurns, result);
    verify(chatTurnRepository).findByChatIdAndUserIdOrderBySequenceId(chatId, testUser, null, null);
  }

  @Test
  void getChat_NotFound_ReturnsEmptyList() {
    // Arrange
    when(chatTurnRepository.findByChatIdAndUserIdOrderBySequenceId(chatId, testUser, null, null))
        .thenReturn(Collections.emptyList());

    // Act
    List<ChatTurn> result = chatService.getChat(chatId, testUser);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(chatTurnRepository).findByChatIdAndUserIdOrderBySequenceId(chatId, testUser, null, null);
  }

  // --- Test for saveChatTurn ---

  @Test
  void saveChatTurn_CallsRepositorySave() {
    // Arrange
    ChatTurn turnToSave = createTestChatTurn(chatTurnId1, chatId, 1);
    Project project = (Project) turnToSave.getChat().getContainer();
    when(chatTurnRepository.countDistinctChatIdsByContainer(project, testUser))
        .thenReturn(9999L); // 9999 chat_id
    when(chatTurnRepository.save(any(ChatTurn.class))).thenReturn(turnToSave);

    when(containerLimitProvider.getMaxChatsLimit(testProject)).thenReturn(10000);

    // Act
    ChatTurn savedTurn = chatService.saveChatTurn(turnToSave, testUser);

    // Assert
    assertNotNull(savedTurn);
    assertEquals(turnToSave, savedTurn);
    verify(chatTurnRepository).save(turnToSave);
  }

  // --- Tests for getChatTurn ---
  @Test
  void saveChatTurn_ThrowsException_WhenMaxChatIdsReached() {
    // Arrange
    ChatTurn turnToSave = createTestChatTurn(chatTurnId1, chatId, 1);

    Project project = (Project) turnToSave.getChat().getContainer();

    when(containerLimitProvider.getMaxChatsLimit(project)).thenReturn(10000);
    when(chatTurnRepository.countDistinctChatIdsByContainer(project, testUser))
        .thenReturn(10000L); // 10000 chat_id

    // Act & Assert
    UserQuotaExceededException exception =
        assertThrows(
            UserQuotaExceededException.class,
            () -> {
              chatService.saveChatTurn(turnToSave, testUser);
            });

    assertEquals("Max Quota Reached (10,000) for this container.", exception.getMessage());
    verify(chatTurnRepository, never()).save(any(ChatTurn.class));
  }

  @Test
  void getChatTurn_Success_ReturnsChatTurn() {
    // Arrange
    ChatTurn expectedTurn = createTestChatTurn(chatTurnId1, chatId, 1);

    when(chatTurnRepository.findByIdAndUser(chatTurnId1, testUser)).thenReturn(expectedTurn);

    // Act
    ChatTurn result = chatService.getChatTurn(chatTurnId1, testUser);

    // Assert
    assertNotNull(result);
    assertEquals(expectedTurn, result);
    verify(chatTurnRepository).findByIdAndUser(chatTurnId1, testUser);
  }

  @Test
  void getChatTurn_NotFound_ReturnsNull() {
    // Arrange
    when(chatTurnRepository.findByIdAndUser(chatTurnId1, testUser)).thenReturn(null);

    // Act
    ChatTurn result = chatService.getChatTurn(chatTurnId1, testUser);

    // Assert
    assertNull(result);
    verify(chatTurnRepository).findByIdAndUser(chatTurnId1, testUser);
  }

  // --- Tests for getChatTurnDTO ---

  @Test
  void getChatTurnDTO_Success_ReturnsChatTurnDTO() {
    // Arrange
    ChatTurn turn = createTestChatTurn(chatTurnId1, chatId, 1);
    when(chatTurnRepository.findByIdAndUser(chatTurnId1, testUser)).thenReturn(turn);

    // Act
    ChatTurnDTO result = chatService.getChatTurnDTO(chatTurnId1, testUser);

    // Assert
    assertNotNull(result);
    assertEquals(chatTurnId1, result.getId());
    assertEquals(chatId, result.getChatId());
    assertEquals(1, result.getSequenceId());
    assertNotNull(result.getInputs());
    assertFalse(result.getInputs().isEmpty());
    assertNotNull(result.getResponse());
    verify(chatTurnRepository).findByIdAndUser(chatTurnId1, testUser);
  }

  @Test
  void getChatTurnDTO_NotFound_ThrowsException() {
    // Arrange
    when(chatTurnRepository.findByIdAndUser(chatTurnId1, testUser)).thenReturn(null);

    // Act & Assert
    // Expecting NullPointerException because getChatTurn returns null,
    // and ChatTurnDTO constructor likely accesses fields on the null object.
    assertThrows(
        NullPointerException.class,
        () -> {
          chatService.getChatTurnDTO(chatTurnId1, testUser);
        });
    verify(chatTurnRepository).findByIdAndUser(chatTurnId1, testUser);
  }

  // --- Test for duplicateChat ---

  @Test
  void duplicateChat_Success_CreatesNewChatWithNewId() {
    // Arrange
    String chatId = "chat-123";
    String originalResponseId = "response-abc";
    String originalResponseText = "Original Response";

    ModelInput input1 = new ModelInput();
    input1.setId("input-1");
    ModelResponse response1 = new ModelResponse();
    response1.setId(originalResponseId);
    response1.setText(originalResponseText);

    ChatTurn existingTurn1 = createTestChatTurn(chatTurnId1, chatId, 1, List.of(input1), response1);
    ChatTurn existingTurn2 = createTestChatTurn(chatTurnId2, chatId, 2, List.of(), null);
    List<ChatTurn> existingChatSegment = Arrays.asList(existingTurn1, existingTurn2);

    when(chatTurnRepository.findChatTillSequence(any(), any(), any()))
        .thenReturn(existingChatSegment);

    when(modelInputRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(modelResponseRepository.saveAll(anyList()))
        .thenAnswer(
            invocation -> {
              List<ModelResponse> responses = invocation.getArgument(0);
              responses.forEach(
                  r -> {
                    if (r.getId() == null) r.setId("new-response-" + UUID.randomUUID());
                  });
              return responses;
            });

    when(chatRepository.save(any(Chat.class)))
        .thenAnswer(
            invocation -> {
              Chat chat = invocation.getArgument(0);
              if (chat.getId() == null) chat.setId(Chat.ID_PREFIX + UUID.randomUUID());
              return chat;
            });

    // Act
    List<ChatTurn> savedTurns = chatService.duplicateChat(existingTurn2);

    // Assert
    assertEquals(2, savedTurns.size());
    String newChatId = savedTurns.get(0).getChat().getId();
    assertNotEquals(chatId, newChatId);

    ModelResponse savedResponse = savedTurns.get(0).getModelResponse();

    assertNotNull(savedResponse);
    assertNotNull(savedResponse.getId());
    assertNotEquals(
        originalResponseId,
        savedResponse.getId(),
        "ID of ModelResponse should be different (deep copy)");
    assertEquals(
        originalResponseText,
        savedResponse.getText(),
        "Text content of ModelResponse should be the same");

    ModelInput savedInput = savedTurns.get(0).getInputs().get(0);
    assertNotNull(savedInput);
  }

  // --- Test for fetchLastTurns ---

  @Test
  void fetchLastTurns_CallsRepository() {
    // Arrange
    List<ChatTurn> expectedTurns = Arrays.asList(createTestChatTurn(chatTurnId1, chatId, 5));

    Pageable pageable = Pageable.unpaged();
    Page<ChatTurn> expectedPage = new PageImpl<>(expectedTurns, pageable, expectedTurns.size());

    when(chatTurnRepository.findLatestChatTurnsByUser(testUser, testProject, null, 0, pageable))
        .thenReturn(expectedPage);

    // Act
    Page<ChatTurn> result = chatService.fetchLastTurns(testUser, testProject, null, pageable);

    // Assert
    assertEquals(expectedTurns, result.getContent());
    verify(chatTurnRepository).findLatestChatTurnsByUser(testUser, testProject, null, 0, pageable);
  }

  @Test
  void deleteChats_shouldResolveChatTurnsAndDelegateToDelete() {
    List<String> chatIds = List.of("chat-1", "chat-2");
    String projectId = "project-123";
    User user = new User();
    user.setId("user-1");

    Project testProject = new Project();
    testProject.setId(projectId);

    ChatTurn ct1 = new ChatTurn();
    ct1.setId("ct1");
    ChatTurn ct2 = new ChatTurn();
    ct2.setId("ct2");
    List<ChatTurn> expectedChatTurns = List.of(ct1, ct2);

    when(evaluationContainerRepository.findById(projectId)).thenReturn(Optional.of(testProject));
    when(chatTurnRepository.findAllByUserAndContainerAndChatIdIn(user, testProject, chatIds))
        .thenReturn(expectedChatTurns);

    ChatServiceImpl spyChatService = Mockito.spy(chatService);
    doNothing().when(spyChatService).delete(anyList());

    spyChatService.deleteChats(chatIds, user, projectId);

    verify(spyChatService).delete(expectedChatTurns);
  }

  @Test
  void delete_shouldRemoveScoresInputsAndModelResponsesAndDeleteChatTurns() {
    User user = new User();
    user.setId("user-1");

    ScoreV2 score = new ScoreV2();
    score.setId("score-1");
    EvaluationMonitoring monitoring = new EvaluationMonitoring();
    monitoring.setId("mon-1");
    score.setEvaluationMonitoring(monitoring);

    ModelInput input = new ModelInput();
    input.setId("input-1");

    ModelResponse response = new ModelResponse();
    response.setId("resp-1");
    response.setScores(List.of(score));
    response.setInferenceMonitoring(new InferenceMonitoring());
    ChatTurn chatTurn = new ChatTurn();
    chatTurn.setId("ct-1");
    chatTurn.setUser(user);
    // chatTurn.setProject(testProject);
    chatTurn.setInputs(new ArrayList<>(List.of(input)));
    chatTurn.setModelResponse(response);

    List<ChatTurn> chatTurns = List.of(chatTurn);

    when(chatTurnRepository.existsByModelResponse(response)).thenReturn(false);
    when(chatTurnRepository.countUsagesForInputs(any())).thenReturn(List.of());

    chatService.delete(chatTurns);

    verify(scoreV2Repository).deleteAll(List.of(score));
    verify(modelResponseRepository).delete(response);
    verify(modelInputRepository).delete(input);
    verify(chatTurnRepository).deleteAll(chatTurns);
  }

  private ChatTurn createTestChatTurn(
      String id, String chatId, int seq, List<ModelInput> inputs, ModelResponse response) {
    Chat chat = new Chat();
    chat.setId(chatId);
    chat.setContainer(testProject);
    ChatTurn turn = new ChatTurn();
    turn.setId(id);
    turn.setChat(chat);
    turn.setSequenceId(seq);
    turn.setInputs(inputs);
    turn.setModelResponse(response);
    turn.setUser(testUser);
    return turn;
  }

  private ChatTurn createTestChatTurn(String id, String chatId, int sequenceId) {
    List<ModelInput> defaultInputs = new ArrayList<>(List.of(new ModelInput()));

    ModelResponse defaultResponse = new ModelResponse();
    defaultResponse.setId("response-" + id);
    defaultResponse.setContainer(testProject);
    defaultResponse.setModel(model);

    InferenceMonitoring inference = new InferenceMonitoring();
    inference.setTimetaken(231.0d);
    inference.setTotalTokens(1);
    inference.setCompletionTokens(1);
    inference.setPromptTokens(1);
    defaultResponse.setInferenceMonitoring(inference);

    return createTestChatTurn(id, chatId, sequenceId, defaultInputs, defaultResponse);
  }

  // --- Tests for getVariablesKeys ---

  @Test
  void getVariablesKeys_Success_ReturnsKeysList() {
    // Arrange
    Chat chat = new Chat();
    chat.setId(chatId);
    chat.setUser(testUser);

    Map<String, String> variables = new HashMap<>();
    variables.put("source", "web");
    variables.put("version", "1.2");
    variables.put("category", "test");
    chat.setVariables(variables);

    when(chatRepository.findByIdAndUser(chatId, testUser)).thenReturn(java.util.Optional.of(chat));

    // Act
    List<String> result = chatService.getVariablesKeys(chatId, testUser);

    // Assert
    assertEquals(3, result.size());
    assertTrue(result.contains("source"));
    assertTrue(result.contains("version"));
    assertTrue(result.contains("category"));
  }

  @Test
  void getVariablesKeys_EmptyVariables_ReturnsEmptyList() {
    // Arrange
    Chat chat = new Chat();
    chat.setId(chatId);
    chat.setUser(testUser);
    chat.setVariables(new HashMap<>());

    when(chatRepository.findByIdAndUser(chatId, testUser)).thenReturn(java.util.Optional.of(chat));

    // Act
    List<String> result = chatService.getVariablesKeys(chatId, testUser);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  void getVariablesKeys_NullVariables_ReturnsEmptyList() {
    // Arrange
    Chat chat = new Chat();
    chat.setId(chatId);
    chat.setUser(testUser);
    chat.setVariables(null);

    when(chatRepository.findByIdAndUser(chatId, testUser)).thenReturn(java.util.Optional.of(chat));

    // Act
    List<String> result = chatService.getVariablesKeys(chatId, testUser);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  void getVariables_Success_ReturnsVariablesMap() {
    when(chatRepository.findByIdAndUser(chatId, testUser)).thenReturn(Optional.of(testChat));
    Map<String, String> result = chatService.getVariables(chatId, testUser);
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("val1", result.get("key1"));
  }

  @Test
  void getVariables_NullVariables_ReturnsEmptyMap() {
    testChat.setVariables(null);
    when(chatRepository.findByIdAndUser(chatId, testUser)).thenReturn(Optional.of(testChat));
    Map<String, String> result = chatService.getVariables(chatId, testUser);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void addOrUpdateVariables_AddNewKey_Success() {
    Map<String, String> updates = Map.of("key3", "val3");
    when(chatRepository.findByIdAndUser(chatId, testUser)).thenReturn(Optional.of(testChat));
    when(appLimts.getMaxVariableKeysPerChat()).thenReturn(10);
    when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));
    Chat result = chatService.addOrUpdateVariables(chatId, testUser, updates);
    verify(chatRepository).save(testChat);
    assertEquals(3, result.getVariables().size());
    assertEquals("val3", result.getVariables().get("key3"));
  }

  @Test
  void addOrUpdateVariables_UpdateExistingKey_Success() {
    Map<String, String> updates = Map.of("key1", "newVal1");
    when(chatRepository.findByIdAndUser(chatId, testUser)).thenReturn(Optional.of(testChat));
    when(appLimts.getMaxVariableKeysPerChat()).thenReturn(10);
    when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));
    Chat result = chatService.addOrUpdateVariables(chatId, testUser, updates);
    verify(chatRepository).save(testChat);
    assertEquals(2, result.getVariables().size());
    assertEquals("newVal1", result.getVariables().get("key1"));
  }

  @Test
  void addOrUpdateVariables_QuotaExceeded_ThrowsException() {
    Map<String, String> updates = Map.of("key3", "val3");
    when(chatRepository.findByIdAndUser(chatId, testUser)).thenReturn(Optional.of(testChat));
    when(appLimts.getMaxVariableKeysPerChat()).thenReturn(2);
    UserQuotaExceededException exception =
        assertThrows(
            UserQuotaExceededException.class,
            () -> chatService.addOrUpdateVariables(chatId, testUser, updates));
    assertTrue(exception.getMessage().contains("Exceeded variables key limit"));
    verify(chatRepository, never()).save(any(Chat.class));
  }

  @Test
  void removeVariablesKey_KeyExists_RemovesKey() {
    when(chatRepository.findByIdAndUser(chatId, testUser)).thenReturn(Optional.of(testChat));
    when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));
    Chat result = chatService.removeVariablesKey(chatId, testUser, "key1");
    verify(chatRepository).save(testChat);
    assertEquals(1, result.getVariables().size());
    assertFalse(result.getVariables().containsKey("key1"));
    assertTrue(result.getVariables().containsKey("key2"));
  }

  @Test
  void removeVariablesKey_KeyNotExists_DoesNothing() {
    when(chatRepository.findByIdAndUser(chatId, testUser)).thenReturn(Optional.of(testChat));
    Chat result = chatService.removeVariablesKey(chatId, testUser, "nonExistentKey");
    verify(chatRepository, never()).save(any(Chat.class));
    assertEquals(2, result.getVariables().size());
  }

  @Test
  void replaceVariables_Success_ReplacesAllKeys() {
    Map<String, String> newVariables = Map.of("newKey", "newVal");
    when(chatRepository.findByIdAndUser(chatId, testUser)).thenReturn(Optional.of(testChat));
    when(appLimts.getMaxVariableKeysPerChat()).thenReturn(10);
    when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));
    Chat result = chatService.replaceVariables(chatId, testUser, newVariables);
    verify(chatRepository).save(testChat);
    assertEquals(1, result.getVariables().size());
    assertTrue(result.getVariables().containsKey("newKey"));
    assertFalse(result.getVariables().containsKey("key1"));
  }

  @Test
  void replaceVariables_QuotaExceeded_ThrowsException() {
    Map<String, String> newVariables = Map.of("k1", "v1", "k2", "v2");
    when(appLimts.getMaxVariableKeysPerChat()).thenReturn(1);
    UserQuotaExceededException exception =
        assertThrows(
            UserQuotaExceededException.class,
            () -> chatService.replaceVariables(chatId, testUser, newVariables));
    assertTrue(exception.getMessage().contains("Exceeded variables key limit"));
    verify(chatRepository, never()).save(any(Chat.class));
    verify(chatRepository, never()).findByIdAndUser(any(), any());
  }

  @Test
  void addChatTurn_LinksTurnToChatAndSaves() {
    ChatTurn turn = new ChatTurn();
    assertNull(turn.getChat());
    assertTrue(testChat.getTurns().isEmpty());
    chatService.addChatTurn(testChat, turn);
    verify(chatRepository).save(testChat);
    assertEquals(testChat, turn.getChat());
    assertTrue(testChat.getTurns().contains(turn));
  }

  @Test
  void getFullChatHistory_Success_ReturnsAllTurns() {
    ChatTurn anyTurnInChat = createTestChatTurn(chatTurnId1, chatId, 1);
    List<ChatTurn> expectedHistory =
        List.of(anyTurnInChat, createTestChatTurn(chatTurnId2, chatId, 2));
    when(chatTurnRepository.findByChatIdAndContainerAndUserIdOrderBySequenceId(
            chatId, testProject, testUser))
        .thenReturn(expectedHistory);
    List<ChatTurn> result = chatService.getFullChatHistory(anyTurnInChat);
    assertEquals(expectedHistory, result);
    assertEquals(2, result.size());
  }

  @Test
  void getFullChatHistory_NullTurn_ReturnsEmptyList() {
    List<ChatTurn> result = chatService.getFullChatHistory(null);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getModelFromChatHistory_ModelExists_ReturnsModel() {
    ChatTurn turn1 = new ChatTurn();
    ChatTurn turn2 = createTestChatTurn(chatTurnId1, chatId, 1);
    List<ChatTurn> history = List.of(turn1, turn2);
    Optional<Model> result = chatService.getModelFromChatHistory(history);
    assertTrue(result.isPresent());
    assertEquals(model, result.get());
  }

  @Test
  void getModelFromChatHistory_NoModels_ReturnsEmpty() {
    ChatTurn turn1 = new ChatTurn();
    turn1.setModelResponse(null);
    ChatTurn turn2 = new ChatTurn();
    ModelResponse resp = new ModelResponse();
    resp.setModel(null);
    turn2.setModelResponse(resp);
    List<ChatTurn> history = List.of(turn1, turn2);
    Optional<Model> result = chatService.getModelFromChatHistory(history);
    assertFalse(result.isPresent());
  }

  @Test
  void copyResponseToTurn_Success_CopiesResponse() {
    ModelResponse sourceResponse = new ModelResponse();
    sourceResponse.setText("Test Response");
    sourceResponse.setContainer(testProject);
    ChatTurn sourceTurn = new ChatTurn();
    sourceTurn.setModelResponse(sourceResponse);
    ChatTurn destTurn = new ChatTurn();
    Model destModel = new Model();
    when(modelResponseRepository.save(any(ModelResponse.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    chatService.copyResponseToTurn(sourceTurn, destTurn, destModel);
    verify(modelResponseRepository).save(any(ModelResponse.class));
    verify(chatTurnRepository).save(destTurn);
    assertNotNull(destTurn.getModelResponse());
    assertEquals("Test Response", destTurn.getModelResponse().getText());
    assertNull(destTurn.getModelResponse().getModel());
  }

  @Test
  void copyResponseToTurn_DestinationAlreadyHasResponse_DoesNothing() {
    ModelResponse sourceResponse = new ModelResponse();
    sourceResponse.setText("Source");
    ChatTurn sourceTurn = new ChatTurn();
    sourceTurn.setModelResponse(sourceResponse);
    ModelResponse destResponse = new ModelResponse();
    destResponse.setText("Destination");
    ChatTurn destTurn = new ChatTurn();
    destTurn.setModelResponse(destResponse);
    chatService.copyResponseToTurn(sourceTurn, destTurn, null);
    verify(modelResponseRepository, never()).save(any(ModelResponse.class));
    verify(chatTurnRepository, never()).save(any(ChatTurn.class));
    assertEquals("Destination", destTurn.getModelResponse().getText());
  }

  @Test
  void copyResponseToTurn_SourceHasNoResponse_ThrowsException() {
    ChatTurn sourceTurn = new ChatTurn();
    sourceTurn.setModelResponse(null);
    ChatTurn destTurn = new ChatTurn();
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> chatService.copyResponseToTurn(sourceTurn, destTurn, null));
    assertEquals("Source turn has no response to copy.", exception.getMessage());
  }

  @Test
  void findChatsByUserAndIds_CallsRepository() {
    List<String> ids = List.of("chat-1");
    List<Chat> expectedChats = List.of(testChat);
    when(chatRepository.findByUserAndIdIn(testUser, ids)).thenReturn(expectedChats);
    List<Chat> result = chatService.findChatsByUserAndIds(ids, testUser);
    assertEquals(expectedChats, result);
    verify(chatRepository).findByUserAndIdIn(testUser, ids);
  }

  @Test
  void getChatTurnsByList_CallsRepository() {
    List<String> ids = List.of("turn-1");
    List<ChatTurn> expectedTurns = List.of(createTestChatTurn("turn-1", "chat-1", 1));
    when(chatTurnRepository.findChatTurnsByList(ids, testUser)).thenReturn(expectedTurns);
    List<ChatTurn> result = chatService.getChatTurnsByList(ids, testUser);
    assertEquals(expectedTurns, result);
    verify(chatTurnRepository).findChatTurnsByList(ids, testUser);
  }

  @Test
  void getChatTurnsByChatIdList_CallsRepository() {
    List<String> chatIds = List.of("chat-1");
    List<ChatTurn> expectedTurns = List.of(createTestChatTurn("turn-1", "chat-1", 1));
    when(chatTurnRepository.findChatTurnsByChatList(chatIds, testUser)).thenReturn(expectedTurns);
    List<ChatTurn> result = chatService.getChatTurnsByChatIdList(chatIds, testUser);
    assertEquals(expectedTurns, result);
    verify(chatTurnRepository).findChatTurnsByChatList(chatIds, testUser);
  }

  @Test
  void findLatestChatTurnIdsByProjectId_CallsRepository() {
    List<String> expectedIds = List.of("turn-1", "turn-2");
    when(chatTurnRepository.findLatestChatTurnIdsByContainerId(testProject.getId(), testUser))
        .thenReturn(expectedIds);
    List<String> result =
        chatService.findLatestChatTurnIdsByProjectId(testProject.getId(), testUser);
    assertEquals(expectedIds, result);
    verify(chatTurnRepository).findLatestChatTurnIdsByContainerId(testProject.getId(), testUser);
  }

  @Test
  void existsByModelResponse_CallsRepository() {
    ModelResponse response = new ModelResponse();
    when(chatTurnRepository.existsByModelResponse(response)).thenReturn(true);
    boolean result = chatService.existsByModelResponse(response);
    assertTrue(result);
    verify(chatTurnRepository).existsByModelResponse(response);
  }

  @Test
  void findIdsWithNonEmptyOutput_CallsRepository() {
    List<String> turnIds = List.of("turn-1");
    List<String> expectedIds = List.of("turn-1");
    when(chatTurnRepository.findIdsWithNonEmptyModelResponse(turnIds)).thenReturn(expectedIds);
    List<String> result = chatService.findIdsWithNonEmptyOutput(turnIds);
    assertEquals(expectedIds, result);
    verify(chatTurnRepository).findIdsWithNonEmptyModelResponse(turnIds);
  }

  @Test
  void getAllChatsByUser_CallsRepository() {
    List<Chat> expectedChats = List.of(testChat);
    when(chatRepository.findAllByUser(testUser)).thenReturn(expectedChats);
    List<Chat> result = chatService.getAllChatsByUser(testUser);
    assertEquals(expectedChats, result);
    verify(chatRepository).findAllByUser(testUser);
  }

  @Test
  void saveChat_CallsRepository() {
    when(chatRepository.save(testChat)).thenReturn(testChat);
    Chat result = chatService.saveChat(testChat);
    assertEquals(testChat, result);
    verify(chatRepository).save(testChat);
  }

  @Test
  void createChatTurn_NewChat_Success() {
    String containerId = testProject.getId();
    String modelId = model.getId();
    Prompt prompt = new Prompt(InputRole.USER, "Hello");
    String modelResponseText = "Hi there";
    when(evaluationContainerRepository.findById(containerId)).thenReturn(Optional.of(testProject));
    when(modelService.getModelForUser(testUser, modelId)).thenReturn(model);
    when(modelResponseRepository.save(any(ModelResponse.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(chatRepository.save(any(Chat.class)))
        .thenAnswer(
            inv -> {
              Chat c = inv.getArgument(0);
              c.setId("new-chat-id");
              return c;
            });
    when(containerLimitProvider.getMaxChatsLimit(testProject)).thenReturn(100);
    when(chatTurnRepository.countDistinctChatIdsByContainer(testProject, testUser)).thenReturn(10L);
    when(chatTurnRepository.save(any(ChatTurn.class))).thenAnswer(inv -> inv.getArgument(0));
    ChatTurn result =
        chatService.createChatTurn(testUser, containerId, null, prompt, modelResponseText, modelId);
    assertNotNull(result);
    verify(chatRepository).save(any(Chat.class));
    verify(chatTurnRepository).save(result);
    assertEquals(0, result.getSequenceId());
    assertEquals("new-chat-id", result.getChat().getId());
    assertEquals("Hi there", result.getModelResponse().getText());
    assertEquals(model, result.getModelResponse().getModel());
  }

  @Test
  void createChatTurn_ExistingChat_Success() {
    String containerId = testProject.getId();
    String modelId = model.getId();
    String existingChatId = "chat-123";
    when(evaluationContainerRepository.findById(containerId)).thenReturn(Optional.of(testProject));
    when(chatRepository.findByIdAndUser(existingChatId, testUser))
        .thenReturn(Optional.of(testChat));
    when(chatTurnRepository.findMaxSequenceIdByUserAndContainerAndChatId(
            testUser, testProject, existingChatId))
        .thenReturn(5);
    when(containerLimitProvider.getMaxChatsLimit(testProject)).thenReturn(100);
    when(chatTurnRepository.countDistinctChatIdsByContainer(testProject, testUser)).thenReturn(10L);
    when(chatTurnRepository.save(any(ChatTurn.class))).thenAnswer(inv -> inv.getArgument(0));
    ChatTurn result = chatService.createChatTurn(testUser, containerId, existingChatId, "", "", "");
    assertNotNull(result);
    verify(chatRepository, never()).save(any(Chat.class));
    verify(chatTurnRepository).save(result);
    assertEquals(6, result.getSequenceId());
    assertEquals(existingChatId, result.getChat().getId());
  }

  @Test
  void createChatTurn_QuotaExceeded_ThrowsException() {
    String containerId = testProject.getId();
    when(evaluationContainerRepository.findById(containerId)).thenReturn(Optional.of(testProject));
    when(chatRepository.findByIdAndUser(chatId, testUser)).thenReturn(Optional.of(testChat));
    when(containerLimitProvider.getMaxChatsLimit(testProject)).thenReturn(100);
    when(chatTurnRepository.countDistinctChatIdsByContainer(testProject, testUser))
        .thenReturn(100L);
    assertThrows(
        UserQuotaExceededException.class,
        () -> chatService.createChatTurn(testUser, containerId, chatId, "", "", ""));
  }

  @Test
  void enrichTurnsWithSystemInstructions_AppliesCorrectInstructionTimeline() {
    Chat chat = new Chat();
    chat.setId("chat-for-enrich");
    ModelInput sysInput1 = new ModelInput(new Prompt(InputRole.SYSTEM, "Instruction 1"), testUser);
    ChatTurn sysTurn1 = new ChatTurn();
    sysTurn1.setChat(chat);
    sysTurn1.setInputs(List.of(sysInput1));
    sysTurn1.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-10 10:00:00"));
    ChatTurn userTurn1 = new ChatTurn();
    userTurn1.setChat(chat);
    userTurn1.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-10 10:05:00"));
    ModelInput sysInput2 = new ModelInput(new Prompt(InputRole.SYSTEM, "Instruction 2"), testUser);
    ChatTurn sysTurn2 = new ChatTurn();
    sysTurn2.setChat(chat);
    sysTurn2.setInputs(List.of(sysInput2));
    sysTurn2.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-10 10:10:00"));
    ChatTurn userTurn2 = new ChatTurn();
    userTurn2.setChat(chat);
    userTurn2.setCreatedAt(java.sql.Timestamp.valueOf("2025-10-10 10:15:00"));
    List<ChatTurn> turnsToEnrich = List.of(userTurn1, userTurn2);
    List<ChatTurn> allInstructions = List.of(sysTurn1, sysTurn2);
    when(chatTurnRepository.findSystemInstructionTurnsInChats(List.of("chat-for-enrich")))
        .thenReturn(allInstructions);
    chatService.enrichTurnsWithSystemInstructions(turnsToEnrich);
    assertEquals("Instruction 1", userTurn1.getEffectiveSystemInstruction());
    assertEquals("Instruction 2", userTurn2.getEffectiveSystemInstruction());
  }

  @Test
  void appendMissingTurns_ClonesAndSavesTurns() {
    Chat destinationChat = new Chat();
    destinationChat.setId("dest-chat");
    destinationChat.setUser(testUser);
    ModelInput sourceInput1 = new ModelInput(new Prompt(InputRole.USER, "Q1"), testUser);
    ModelResponse sourceResp1 = new ModelResponse();
    sourceResp1.setText("A1");
    ChatTurn sourceTurn1 = new ChatTurn(List.of(sourceInput1), sourceResp1, testUser, new Chat());
    sourceTurn1.setSequenceId(0);
    ModelInput sourceInput2 = new ModelInput(new Prompt(InputRole.USER, "Q2"), testUser);
    ModelResponse sourceResp2 = new ModelResponse();
    sourceResp2.setText("A2");
    ChatTurn sourceTurn2 = new ChatTurn(List.of(sourceInput2), sourceResp2, testUser, new Chat());
    sourceTurn2.setSequenceId(1);
    List<ChatTurn> sourceTurns = List.of(sourceTurn1, sourceTurn2);
    ChatTurn lastTurn =
        chatService.appendMissingTurns(destinationChat, sourceTurns, testProject, 0);
    assertNotNull(lastTurn);
    assertEquals(1, lastTurn.getSequenceId());
    assertEquals("dest-chat", lastTurn.getChat().getId());
    assertEquals("A2", lastTurn.getModelResponse().getText());
    verify(modelInputRepository).saveAll(anyList());
    verify(modelResponseRepository).saveAll(anyList());
    verify(chatTurnRepository).saveAll(anyList());
  }

  @Test
  void createChat_WithId_BuildsAndSavesChat() {
    ArgumentCaptor<Chat> chatCaptor = ArgumentCaptor.forClass(Chat.class);
    when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> inv.getArgument(0));
    Chat result = chatService.createChat("chat-id-A", testProject, testUser);
    verify(chatRepository).save(chatCaptor.capture());
    Chat savedChat = chatCaptor.getValue();
    assertEquals("chat-id-A", savedChat.getId());
    assertEquals(testUser, savedChat.getUser());
    assertEquals(testProject, savedChat.getContainer());
    assertEquals(result, savedChat);
  }

  @Test
  void getDistinctModelProvidersByProject_CallsRepository() {
    List<ModelProvider> expectedProviders = List.of(ModelProvider.OPENAI);
    when(chatTurnRepository.findDistinctModelProvidersByContainerId(testProject.getId(), testUser))
        .thenReturn(expectedProviders);
    List<ModelProvider> result =
        chatService.getDistinctModelProvidersByProject(testUser, testProject.getId());
    assertEquals(expectedProviders, result);
    verify(chatTurnRepository)
        .findDistinctModelProvidersByContainerId(testProject.getId(), testUser);
  }

  @Test
  void duplicateChatNewLastModelResponse_Success_ClonesWithNewLastResponse() {
    Model newModel = new Model();
    newModel.setId("new-model-456");
    ModelResponse originalResponse = new ModelResponse();
    originalResponse.setId("resp-1");
    originalResponse.setText("Turn 1 Response");
    originalResponse.setModel(model);
    ModelResponse lastResponse = new ModelResponse();
    lastResponse.setId("resp-2");
    lastResponse.setText("Last Response");
    lastResponse.setModel(model);
    ChatTurn existingTurn1 =
        createTestChatTurn(chatTurnId1, chatId, 1, List.of(new ModelInput()), originalResponse);
    ChatTurn existingTurn2 =
        createTestChatTurn(chatTurnId2, chatId, 2, List.of(new ModelInput()), lastResponse);
    existingTurn2.setChat(existingTurn1.getChat());
    List<ChatTurn> existingChatSegment = List.of(existingTurn1, existingTurn2);
    when(chatTurnRepository.findChatTillSequence(chatId, 2, testUser))
        .thenReturn(existingChatSegment);
    when(modelResponseRepository.save(any(ModelResponse.class)))
        .thenAnswer(
            inv -> {
              ModelResponse mr = inv.getArgument(0);
              mr.setId("new-final-resp");
              return mr;
            });
    when(modelInputRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(modelResponseRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(chatRepository.save(any(Chat.class)))
        .thenAnswer(
            inv -> {
              Chat chat = inv.getArgument(0);
              chat.setId(Chat.ID_PREFIX + UUID.randomUUID());
              return chat;
            });
    List<ChatTurn> newTurns =
        chatService.duplicateChatNewLastModelResponse(existingTurn2, newModel);
    assertEquals(2, newTurns.size());
    String newChatId = newTurns.get(0).getChat().getId();
    assertNotEquals(chatId, newChatId);
    ModelResponse copiedResp1 = newTurns.get(0).getModelResponse();
    assertNotNull(copiedResp1);
    assertNotEquals(originalResponse.getId(), copiedResp1.getId());
    assertEquals(originalResponse.getText(), copiedResp1.getText());
    assertEquals(newModel, copiedResp1.getModel());
    ModelResponse newFinalResp = newTurns.get(1).getModelResponse();
    assertNotNull(newFinalResp);
    assertEquals("new-final-resp", newFinalResp.getId());
    assertNull(newFinalResp.getText());
    assertEquals(newModel, newFinalResp.getModel());
  }

  @Test
  void getChatUntilTurn_Success_ReturnsPartialHistory() {
    ChatTurn turn1 = createTestChatTurn(chatTurnId1, chatId, 1);
    ChatTurn turn2 = createTestChatTurn(chatTurnId2, chatId, 2);
    turn2.setChat(turn1.getChat());
    List<ChatTurn> expectedTurns = List.of(turn1, turn2);
    when(chatTurnRepository.findByIdAndUser(chatTurnId2, testUser)).thenReturn(turn2);
    when(chatTurnRepository.findByChatIdAndUserIdUntilSequenceIdOrderBySequenceId(
            chatId, testUser, 2))
        .thenReturn(expectedTurns);
    List<ChatTurn> result = chatService.getChatUntilTurn(chatId, chatTurnId2, testUser);
    assertEquals(2, result.size());
    assertEquals(expectedTurns, result);
  }

  @Test
  void deleteTurnResults_Success_NullifiesResponsesAndDeletesData() {
    ModelResponse response = new ModelResponse();
    response.setScores(new ArrayList<>(List.of(new ScoreV2())));
    response.setInferenceMonitoring(new InferenceMonitoring());
    ChatTurn turn = new ChatTurn();
    turn.setId("turn-1");
    turn.setModelResponse(response);
    turn.setInferenceStatus(new InferenceStatus());
    List<ChatTurn> chatTurnList = List.of(turn);
    when(chatTurnRepository.saveAllAndFlush(anyList())).thenReturn(chatTurnList);
    chatService.deleteTurnResults(testUser, testProject.getId(), chatTurnList);
    assertNull(turn.getModelResponse());
    assertNull(turn.getInferenceStatus());
    verify(chatTurnRepository).saveAllAndFlush(chatTurnList);
    verify(modelResponseRepository).deleteAll(List.of(response));
    verify(scoreV2Repository).deleteAll(response.getScores());
    verify(inferenceStatusRepository).deleteAll(anyList());
    verify(evaluationStatusRepository).deleteAll(anyList());
    verify(sxsHumanFeedbackRepository).deleteByChatTurnIdList(List.of("turn-1"));
  }
}

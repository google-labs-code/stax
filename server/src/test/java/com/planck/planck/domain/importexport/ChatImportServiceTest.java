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

package com.planck.planck.domain.importexport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.config.ApplicationLimits;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorService;
import com.planck.planck.domain.evaluator.llm.service.NewLLMEvaluatorService;
import com.planck.planck.domain.importexport.dto.ChatImportRequest;
import com.planck.planck.domain.importexport.dto.ImportResultDTO;
import com.planck.planck.domain.importexport.dto.TurnCreationResult;
import com.planck.planck.domain.importexport.dto.record.DataRecord;
import com.planck.planck.domain.importexport.dto.record.RecordProvider;
import com.planck.planck.domain.model.service.ModelServiceImpl;
import com.planck.planck.domain.project.EvaluationContainerService;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.domain.tags.TagService;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.HumanEvaluator;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.Tag;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ChatTurnContainerType;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.util.ChatCreationHelper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ChatImportServiceImplTest {

  @Mock private ChatService chatService;
  @Mock private TagService tagService;
  @Mock private TagLinkService tagLinkService;
  @Mock private ModelServiceImpl modelService;
  @Mock private HumanEvaluatorService humanEvaluatorService;
  @Mock private ApplicationLimits applicationLimits;
  @Mock private NewLLMEvaluatorService llmEvaluatorService;
  @Mock private EvaluationContainerService evaluationContainerService;

  @Mock private RecordProvider csvProvider;
  @Mock private RecordProvider jsonProvider;

  private ChatImportServiceImpl chatImportService;

  @Captor private ArgumentCaptor<List<ChatTurn>> chatTurnsCaptor;
  @Captor private ArgumentCaptor<List<TagLink>> tagLinksCaptor;
  @Captor private ArgumentCaptor<List<Tag>> newTagsCaptor;

  private User testUser;
  private Project testProject;

  @BeforeEach
  void setUp() {
    List<RecordProvider> recordProviders = new ArrayList<>();
    recordProviders.add(csvProvider);
    recordProviders.add(jsonProvider);

    chatImportService =
        new ChatImportServiceImpl(
            chatService,
            tagService,
            tagLinkService,
            modelService,
            humanEvaluatorService,
            applicationLimits,
            llmEvaluatorService,
            evaluationContainerService,
            recordProviders);

    testUser = new User();
    testUser.setId("user-test-id");

    testProject = new Project();
    testProject.setId("project-id");
    testProject.setEvaluationType(EvaluationType.POINTWISE);
    testProject.setUser(testUser);
  }

  @Test
  void importChatsFromCsv_withJsonColumn_createsTurnsAndTagsCorrectly() throws IOException {
    String chatJson =
        """
                [
                  {"role": "user", "content": "Hello"},
                  {"role": "assistant", "content": "Hi there!", "model_label": "TestModel", "tags": "greeting, test"}
                ]
                """;
    String csvContent = createCsvString("chat_history", chatJson);
    MultipartFile file =
        new MockMultipartFile(
            "file.csv", "file.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

    ChatImportRequest request =
        new ChatImportRequest(
            file,
            testUser,
            ChatTurnContainerType.PROJECT,
            "project-id",
            "chat_history",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    setupCommonMocks();

    DataRecord mockRecord = mock(DataRecord.class);
    lenient().when(mockRecord.getValue("chat_history")).thenReturn(chatJson);
    when(csvProvider.supports("text/csv")).thenReturn(true);
    when(csvProvider.getRecords(any())).thenReturn(Stream.of(mockRecord));

    executeAndAssertTestLogic(request);

    verify(csvProvider, times(1)).getRecords(any());
    verify(jsonProvider, never()).getRecords(any());
  }

  @Test
  void importChatsFromJson_createsTurnsAndTagsCorrectly() throws IOException {
    String chatJson =
        """
                [
                  {"role": "user", "content": "Hello"},
                  {"role": "assistant", "content": "Hi there!", "model_label": "TestModel", "tags": "greeting, test"}
                ]
                """;
    String jsonContent =
        String.format(
            """
                [
                  {
                    "chat_history": %s
                  }
                ]
                """,
            escapeJson(chatJson));

    MultipartFile file =
        new MockMultipartFile(
            "file.json",
            "file.json",
            "application/json",
            jsonContent.getBytes(StandardCharsets.UTF_8));

    ChatImportRequest request =
        new ChatImportRequest(
            file,
            testUser,
            ChatTurnContainerType.PROJECT,
            "project-id",
            "chat_history",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    setupCommonMocks();

    DataRecord mockRecord = mock(DataRecord.class);
    lenient().when(mockRecord.getValue("chat_history")).thenReturn(chatJson);
    when(jsonProvider.supports("application/json")).thenReturn(true);
    when(jsonProvider.getRecords(any())).thenReturn(Stream.of(mockRecord));

    executeAndAssertTestLogic(request);

    verify(jsonProvider, times(1)).getRecords(any());
    verify(csvProvider, never()).getRecords(any());
  }

  private void executeAndAssertTestLogic(ChatImportRequest request) throws IOException {
    ChatTurn userTurn = new ChatTurn();
    userTurn.setId("user-turn-id");
    ChatTurn assistantTurn = new ChatTurn();
    assistantTurn.setId("assistant-turn-id");

    TurnCreationResult mockTurnResult =
        new TurnCreationResult(List.of(userTurn, assistantTurn), Map.of(1, "greeting, test"));

    try (MockedStatic<ChatCreationHelper> mockedStatic =
        Mockito.mockStatic(ChatCreationHelper.class)) {
      mockedStatic
          .when(
              () ->
                  ChatCreationHelper.createTurnsFromJson(
                      anyString(),
                      any(Chat.class),
                      any(User.class),
                      anyMap(),
                      any(),
                      anyBoolean(),
                      any(Project.class),
                      any()))
          .thenReturn(mockTurnResult);
      when(evaluationContainerService.getContainerForUser(any(User.class), anyString()))
          .thenReturn(testProject);
      when(chatService.saveChat(chatTurnsCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
      when(tagService.saveAll(newTagsCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
      when(tagLinkService.saveAll(tagLinksCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

      ImportResultDTO result = chatImportService.importChatsFromFile(request);

      assertThat(result.getTotalRows()).isEqualTo(1);
      assertThat(result.getSuccessfulRows()).isEqualTo(1);
      assertThat(result.getFailedRows()).isEqualTo(0);

      List<ChatTurn> savedTurns = chatTurnsCaptor.getValue();
      assertThat(savedTurns).hasSize(2);
      assertThat(savedTurns.get(1)).isEqualTo(assistantTurn);

      List<Tag> createdTags = newTagsCaptor.getValue();
      assertThat(createdTags).hasSize(2);
      assertThat(createdTags.stream().map(Tag::getTagName))
          .containsExactlyInAnyOrder("greeting", "test");

      verify(tagLinkService, times(1)).saveAll(any());
      List<TagLink> createdLinks = tagLinksCaptor.getValue();
      assertThat(createdLinks).hasSize(2);
      createdLinks.forEach(link -> assertThat(link.getTargetId()).isEqualTo(assistantTurn.getId()));
    }
  }

  private void setupCommonMocks() {
    when(modelService.buildModelMap(any(), any())).thenReturn(Map.of("TestModel", new Model()));
    when(tagService.findAllUserTags(any())).thenReturn(Collections.emptyList());
    when(humanEvaluatorService.getEvaluators(any(), any(), any()))
        .thenReturn(List.of(new HumanEvaluator()));
    when(applicationLimits.getMaxVariableKeysPerChat()).thenReturn(10);
    when(chatService.createChat(any(User.class), any(Project.class))).thenReturn(new Chat());
  }

  private String createCsvString(String header, String value) {
    String escapedValue = value.replace("\"", "\"\"");
    return header + "\n" + "\"" + escapedValue + "\"";
  }

  private String escapeJson(String jsonString) {
    return "\"" + jsonString.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }
}

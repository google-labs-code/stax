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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.importexport.dto.ChatExportDTO;
import com.planck.planck.domain.importexport.dto.InferenceAnalyticsDTO;
import com.planck.planck.domain.importexport.dto.message.BaseMessageExportDTO;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.HumanEvalScore;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.ScoreV2;
import com.planck.planck.entitities.Tag;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InputRole;
import com.planck.planck.enums.TagLinkTargetType;
import com.planck.planck.exceptions.NotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatExportServiceTest {

  @Mock ChatService chatService;
  @Mock TagLinkService tagLinkService;

  @InjectMocks ChatExportServiceImpl chatExportService;

  User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("user-xyz");
  }

  @Test
  void exportChat_withFullData_returnsCorrectAggregatedDTOs() {

    String chatId = "chat-1";
    String turnId = "turn-1";

    ModelInput systemInput =
        createModelInput("system-1", "You are a helpful assistant.", null, InputRole.SYSTEM);
    ModelInput userInput = createModelInput("user-1", "Hi!", "Hello there!", InputRole.USER);

    Model model = new Model();
    model.setLabel("Gemini 2.0 Flash");
    HumanEvalScore humanEvalScore = new HumanEvalScore();
    humanEvalScore.setScore(1.0);
    ModelResponse response = new ModelResponse();
    response.setText("Hello!");
    response.setModel(model);
    response.setHumanEvalScores(List.of(humanEvalScore));

    InferenceMonitoring monitoring = createInferenceMonitoring(150.5, 10, 20);
    response.setInferenceMonitoring(monitoring);

    ScoreV2 llmScore1 = createLlmScore("evaluator-A", 0.8);
    ScoreV2 llmScore2 = createLlmScore("evaluator-B", 0.6);

    TagLink tagLink = createTagLink("greeting");
    when(tagLinkService.getTagLinksByEntity(TagLinkTargetType.CHAT_TURN, turnId, testUser))
        .thenReturn(List.of(tagLink));

    ChatTurn chatTurn = new ChatTurn();
    chatTurn.setId(turnId);
    chatTurn.setInputs(List.of(systemInput, userInput));
    chatTurn.setModelResponse(response);
    response.setScores(List.of(llmScore1, llmScore2));
    chatTurn.setSequenceId(1);

    Chat chat = new Chat();
    chat.setId(chatId);
    chat.setTurns(List.of(chatTurn));
    when(chatService.findByChatId(chatId, testUser)).thenReturn(Optional.of(chat));

    ChatExportDTO exportDTO = chatExportService.exportChat(chatId, testUser);

    assertThat(exportDTO.getInput()).isEqualTo("Hi!");
    assertThat(exportDTO.getExpectedOutput()).isEqualTo("Hello there!");
    assertThat(exportDTO.getOutput()).isEqualTo("Hello!");
    assertThat(exportDTO.getModelNickname()).isEqualTo("Gemini 2.0 Flash");
    assertThat(exportDTO.getHumanEvaluation()).isEqualTo(1);
    assertThat(exportDTO.getTurns()).isEqualTo(1);
    assertThat(exportDTO.getSystemInstruction()).isEqualTo("You are a helpful assistant.");
    assertThat(exportDTO.getTags()).isEqualTo("greeting");

    List<BaseMessageExportDTO> fullChat = exportDTO.getChat();
    assertThat(fullChat.size()).isEqualTo(3);

    InferenceAnalyticsDTO inferenceAnalytics = exportDTO.getInferenceAnalytics();
    assertThat(inferenceAnalytics).isNotNull();
    assertThat(inferenceAnalytics.getTotalChatLatency()).isEqualTo(150.5);
    assertThat(inferenceAnalytics.getAvgChatLatency()).isEqualTo(150.5);
    assertThat(inferenceAnalytics.getTotalChatPromptTokens()).isEqualTo(10);
    assertThat(inferenceAnalytics.getTotalChatCompletionTokens()).isEqualTo(20);
    assertThat(inferenceAnalytics.getTotalChatTokens()).isEqualTo(30);
  }

  @Test
  void exportChat_withMultipleTurns_aggregatesAnalyticsAndTagsCorrectly() {

    String chatId = "chat-2";

    ModelResponse response1 = new ModelResponse();
    response1.setText("First answer");
    response1.setInferenceMonitoring(createInferenceMonitoring(100.0, 10, 20));
    ChatTurn turn1 = new ChatTurn();
    turn1.setId("turn-1");
    turn1.setSequenceId(1);
    turn1.setInputs(List.of(createModelInput("u1", "First Q", null, InputRole.USER)));
    turn1.setModelResponse(response1);
    response1.setScores(List.of(createLlmScore("Quality", 0.9), createLlmScore("Clarity", 0.8)));
    when(tagLinkService.getTagLinksByEntity(TagLinkTargetType.CHAT_TURN, "turn-1", testUser))
        .thenReturn(List.of(createTagLink("tech"), createTagLink("quality")));

    ModelResponse response2 = new ModelResponse();
    response2.setText("Second answer");
    response2.setInferenceMonitoring(createInferenceMonitoring(200.0, 30, 40));
    ChatTurn turn2 = new ChatTurn();
    turn2.setId("turn-2");
    turn2.setSequenceId(2);
    turn2.setInputs(List.of(createModelInput("u2", "Second Q", null, InputRole.USER)));
    turn2.setModelResponse(response2);
    response2.setScores(List.of(createLlmScore("Quality", 0.7)));
    when(tagLinkService.getTagLinksByEntity(TagLinkTargetType.CHAT_TURN, "turn-2", testUser))
        .thenReturn(List.of(createTagLink("support")));

    Chat chat = new Chat();
    chat.setId(chatId);
    chat.setTurns(List.of(turn2, turn1));
    when(chatService.findByChatId(chatId, testUser)).thenReturn(Optional.of(chat));

    ChatExportDTO exportDTO = chatExportService.exportChat(chatId, testUser);

    String[] tags = exportDTO.getTags().split(",");
    Arrays.sort(tags);
    assertThat(String.join(",", tags)).isEqualTo("quality,support,tech");

    InferenceAnalyticsDTO inferenceAnalytics = exportDTO.getInferenceAnalytics();
    assertThat(inferenceAnalytics).isNotNull();
    assertThat(inferenceAnalytics.getTotalChatLatency()).isEqualTo(300.0);
    assertThat(inferenceAnalytics.getAvgChatLatency()).isEqualTo(150.0);
    assertThat(inferenceAnalytics.getTotalChatPromptTokens()).isEqualTo(40);
    assertThat(inferenceAnalytics.getTotalChatCompletionTokens()).isEqualTo(60);
    assertThat(inferenceAnalytics.getTotalChatTokens()).isEqualTo(100);
  }

  @Test
  void exportChat_withNoLlmEvals_returnsNullForEvalAnalytics() {
    ChatTurn chatTurn = new ChatTurn();
    chatTurn.setId("turn-1");
    chatTurn.setSequenceId(1);
    chatTurn.setInputs(List.of(createModelInput("u1", "Q", null, InputRole.USER)));
    chatTurn.setModelResponse(new ModelResponse());
    chatTurn.getModelResponse().setScores(Collections.emptyList());

    Chat chat = new Chat();
    chat.setId("chat-1");
    chat.setTurns(List.of(chatTurn));
    when(chatService.findByChatId("chat-1", testUser)).thenReturn(Optional.of(chat));

    chatExportService.exportChat("chat-1", testUser);
  }

  @Test
  void exportChat_ChatNotFound_ThrowsNotFoundException() {
    String chatId = "not-found";
    when(chatService.findByChatId(chatId, testUser)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> chatExportService.exportChat(chatId, testUser))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Chat not found: " + chatId);
  }

  private ModelInput createModelInput(
      String id, String text, String expectedOutput, InputRole role) {
    ModelInput input = new ModelInput();
    input.setId(id);
    input.setText(text);
    input.setExpectedOutput(expectedOutput);
    input.setRole(role);
    return input;
  }

  private InferenceMonitoring createInferenceMonitoring(
      Double latency, Integer promptTokens, Integer completionTokens) {
    InferenceMonitoring monitoring = new InferenceMonitoring();
    monitoring.setTurnTimeTaken(latency);
    monitoring.setTurnPromptTokens(promptTokens);
    monitoring.setTurnCompletionTokens(completionTokens);
    monitoring.setTurnTotalTokens(promptTokens + completionTokens);
    return monitoring;
  }

  private ScoreV2 createLlmScore(String evaluatorName, double score) {
    LLMEvaluator evaluator = new LLMEvaluator();
    evaluator.setName(evaluatorName);
    evaluator.setId("eval-" + evaluatorName.toLowerCase());

    ScoreV2 scoreV2 = new ScoreV2();
    scoreV2.setLlmEvaluator(evaluator);
    scoreV2.setScore(score);
    return scoreV2;
  }

  private TagLink createTagLink(String tagName) {
    Tag tag = new Tag();
    tag.setTagName(tagName);
    TagLink tagLink = new TagLink();
    tagLink.setTag(tag);
    return tagLink;
  }
}

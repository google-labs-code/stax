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

import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.evaluation.EvaluationScoreHelper;
import com.planck.planck.domain.importexport.dto.ChatExportDTO;
import com.planck.planck.domain.importexport.dto.InferenceAnalyticsDTO;
import com.planck.planck.domain.importexport.dto.message.AssistantMessageExportDTO;
import com.planck.planck.domain.importexport.dto.message.BaseMessageExportDTO;
import com.planck.planck.domain.importexport.dto.message.SystemMessageExportDTO;
import com.planck.planck.domain.importexport.dto.message.UserMessageExportDTO;
import com.planck.planck.domain.model.dto.ModelTokens;
import com.planck.planck.domain.project.EvaluationContainerService;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.HumanEvalScore;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InputRole;
import com.planck.planck.enums.TagLinkTargetType;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.util.NumberUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class ChatExportServiceImpl implements ChatExportService {

  private final ChatService chatService;

  private final TagLinkService tagLinkService;

  private final EvaluationContainerService evaluationContainerService;

  public ChatExportServiceImpl(
      ChatService chatService,
      TagLinkService tagLinkService,
      EvaluationContainerService evaluationContainerService) {
    this.chatService = chatService;
    this.tagLinkService = tagLinkService;
    this.evaluationContainerService = evaluationContainerService;
  }

  @Override
  public List<ChatExportDTO> exportChats(List<String> chatIds, User user) {
    return chatIds.stream().map(chatId -> exportChat(chatId, user)).collect(Collectors.toList());
  }

  @Override
  public ChatExportDTO exportChat(String chatId, User user) {
    Chat chat =
        chatService
            .findByChatId(chatId, user)
            .orElseThrow(() -> new NotFoundException("Chat not found: " + chatId));

    return toChatExportDTO(chat, user);
  }

  private ChatExportDTO toChatExportDTO(Chat chat, User user) {
    ChatExportDTO dto = new ChatExportDTO();

    List<ChatTurn> sortedTurns =
        chat.getTurns().stream().sorted(Comparator.comparing(ChatTurn::getSequenceId)).toList();

    if (sortedTurns.isEmpty()) {
      return dto;
    }

    Map<String, String> tagsByTurnId = new HashMap<>();
    for (ChatTurn turn : sortedTurns) {
      String tags = mapTags(turn, user);
      if (tags != null && !tags.isEmpty()) {
        tagsByTurnId.put(turn.getId(), tags);
      }
    }

    populateTopLevelFields(dto, sortedTurns.get(sortedTurns.size() - 1), chat, user);
    dto.setTurns(sortedTurns.size());

    List<BaseMessageExportDTO> fullChatHistory = buildFullChatHistory(sortedTurns, user);
    dto.setChat(fullChatHistory);

    dto.setSystemInstruction(extractLastSystemInstruction(sortedTurns));

    dto.setInferenceAnalytics(calculateInferenceAnalytics(sortedTurns));

    // We only aggregate the tags for now. Should be changed once Chat entity becomes Taggable
    dto.setTags(aggregateAllTags(tagsByTurnId));

    return dto;
  }

  private String aggregateAllTags(Map<String, String> tagsByTurnId) {
    return tagsByTurnId.values().stream()
        .flatMap(tagString -> Arrays.stream(tagString.split(",")))
        .map(String::trim)
        .distinct()
        .collect(Collectors.joining(","));
  }

  private InferenceAnalyticsDTO calculateInferenceAnalytics(List<ChatTurn> sortedTurns) {
    if (sortedTurns == null || sortedTurns.isEmpty()) {
      return null;
    }

    double totalLatency = 0.0;
    long totalPromptTokens = 0L;
    long totalCompletionTokens = 0L;
    int turnsWithLatency = 0;

    for (ChatTurn turn : sortedTurns) {
      ModelResponse response = turn.getModelResponse();

      if (response != null && response.getInferenceMonitoring() != null) {
        InferenceMonitoring monitoring = response.getInferenceMonitoring();

        if (monitoring.getTurnTimeTaken() != null) {
          totalLatency += monitoring.getTurnTimeTaken();
          turnsWithLatency++;
        }

        if (monitoring.getTurnPromptTokens() != null) {
          totalPromptTokens += monitoring.getTurnPromptTokens();
        }
        if (monitoring.getTurnCompletionTokens() != null) {
          totalCompletionTokens += monitoring.getTurnCompletionTokens();
        }
      }
    }

    InferenceAnalyticsDTO analytics = new InferenceAnalyticsDTO();
    analytics.setTotalChatLatency(totalLatency);
    analytics.setTotalChatPromptTokens(totalPromptTokens);
    analytics.setTotalChatCompletionTokens(totalCompletionTokens);
    analytics.setTotalChatTokens(totalPromptTokens + totalCompletionTokens);

    if (turnsWithLatency > 0) {
      double average = totalLatency / turnsWithLatency;
      analytics.setAvgChatLatency(NumberUtil.round(average, 2));
    } else {
      analytics.setAvgChatLatency(0.0);
    }

    return analytics;
  }

  private void populateTopLevelFields(ChatExportDTO dto, ChatTurn lastTurn, Chat chat, User user) {
    lastTurn.getInputs().stream()
        .filter(i -> InputRole.USER.equals(i.getRole()))
        .findFirst()
        .ifPresent(
            lastUserInput -> {
              dto.setInput(lastUserInput.getText());
              dto.setExpectedOutput(lastUserInput.getExpectedOutput());
            });

    mapHumanEvalScore(lastTurn).ifPresent(dto::setHumanEvaluation);
    mapHumanEvalNotes(lastTurn).ifPresent(dto::setHumanEvaluationNotes);

    if (lastTurn.getModelResponse() != null) {
      dto.setOutput(lastTurn.getModelResponse().getText());
      dto.setModelNickname(mapModelLabel(lastTurn));
    }

    if (lastTurn.getModelResponse() != null
        && lastTurn.getModelResponse().getScores() != null
        && !lastTurn.getModelResponse().getScores().isEmpty()) {
      EvaluationScoreHelper helper =
          new EvaluationScoreHelper(lastTurn.getModelResponse().getScores());
      dto.setLlmEvaluations(helper.getLLMScores());
    }

    if (chat.getVariables() != null && !chat.getVariables().isEmpty()) {
      dto.setVariables(chat.getVariables());
    }
  }

  private List<BaseMessageExportDTO> buildFullChatHistory(List<ChatTurn> sortedTurns, User user) {
    return sortedTurns.stream().flatMap(turn -> createMessagesFromTurn(turn, user)).toList();
  }

  private Stream<BaseMessageExportDTO> createMessagesFromTurn(ChatTurn turn, User user) {
    Stream<BaseMessageExportDTO> inputMessages =
        turn.getInputs().stream().map(this::mapToMessageFromInput);

    Stream<BaseMessageExportDTO> responseMessage =
        turn.getModelResponse() == null
            ? Stream.empty()
            : Stream.of(mapToAssistantMessage(turn, user));

    return Stream.concat(inputMessages, responseMessage);
  }

  private String extractLastSystemInstruction(List<ChatTurn> sortedTurns) {
    List<String> systemInstructions =
        sortedTurns.stream()
            .flatMap(turn -> turn.getInputs().stream())
            .filter(input -> InputRole.SYSTEM.equals(input.getRole()))
            .map(ModelInput::getText)
            .toList();

    if (systemInstructions.isEmpty()) {
      return null;
    } else {
      return systemInstructions.get(systemInstructions.size() - 1);
    }
  }

  private BaseMessageExportDTO mapToMessageFromInput(ModelInput input) {
    if (InputRole.SYSTEM.equals(input.getRole())) {
      SystemMessageExportDTO sysMsg = new SystemMessageExportDTO();
      sysMsg.setContent(input.getText());
      sysMsg.setVariables(input.getVariables());
      return sysMsg;
    } else {
      UserMessageExportDTO userMsg = new UserMessageExportDTO();
      userMsg.setContent(input.getText());
      userMsg.setExpectedOutput(input.getExpectedOutput());
      userMsg.setVariables(input.getVariables());
      return userMsg;
    }
  }

  private AssistantMessageExportDTO mapToAssistantMessage(ChatTurn turn, User user) {
    AssistantMessageExportDTO assistantMsg = new AssistantMessageExportDTO();
    ModelResponse response = turn.getModelResponse();

    assistantMsg.setContent(response.getText());
    assistantMsg.setModelNickname(mapModelLabel(turn));
    assistantMsg.setTags(mapTags(turn, user));

    if (response.getInferenceMonitoring() != null) {
      InferenceMonitoring inferenceMonitoring = response.getInferenceMonitoring();
      assistantMsg.setInferenceLatency(inferenceMonitoring.getTurnTimeTaken());
      assistantMsg.setInferenceTokens(
          new ModelTokens(
              inferenceMonitoring.getTurnPromptTokens(),
              inferenceMonitoring.getTurnCompletionTokens(),
              inferenceMonitoring.getTurnTotalTokens()));
    }

    if (turn.getModelResponse() != null
        && turn.getModelResponse().getScores() != null
        && !turn.getModelResponse().getScores().isEmpty()) {
      EvaluationScoreHelper helper = new EvaluationScoreHelper(turn.getModelResponse().getScores());
      assistantMsg.setLlmEvaluations(helper.getLLMScores());
    }

    mapHumanEvalScore(turn).ifPresent(assistantMsg::setHumanEvaluation);
    mapHumanEvalNotes(turn).ifPresent(assistantMsg::setHumanEvaluationNotes);

    return assistantMsg;
  }

  @Override
  public List<ChatExportDTO> exportAllChats(String sourceId, User user) {
    EvaluationContainer container = evaluationContainerService.getContainerForUser(user, sourceId);
    List<String> allChatIds = chatService.getAllChatIds(user, container);
    return allChatIds.stream().map(chatId -> exportChat(chatId, user)).collect(Collectors.toList());
  }

  @Override
  public List<BaseMessageExportDTO> getChatHistory(Chat chat, User user) {
    if (chat == null || chat.getTurns() == null || chat.getTurns().isEmpty()) {
      return Collections.emptyList();
    }

    List<ChatTurn> sortedTurns =
        chat.getTurns().stream().sorted(Comparator.comparing(ChatTurn::getSequenceId)).toList();

    return buildFullChatHistory(sortedTurns, user);
  }

  private String mapModelLabel(ChatTurn turn) {
    if (turn.getModelResponse() != null && turn.getModelResponse().getModel() != null) {
      return turn.getModelResponse().getModel().getLabel();
    }
    return null;
  }

  private Optional<HumanEvalScore> getFirstHumanEvalScore(ChatTurn turn) {
    ModelResponse modelResponse = turn.getModelResponse();
    if (modelResponse == null) {
      return Optional.empty();
    }
    List<HumanEvalScore> scores = modelResponse.getHumanEvalScores();
    if (scores == null || scores.isEmpty()) {
      return Optional.empty();
    }
    return scores.stream().findFirst();
  }

  private Optional<Integer> mapHumanEvalScore(ChatTurn turn) {
    return getFirstHumanEvalScore(turn).map(this::mapScoreToInt);
  }

  private Optional<String> mapHumanEvalNotes(ChatTurn turn) {
    return getFirstHumanEvalScore(turn).map(HumanEvalScore::getNotes);
  }

  private String mapTags(ChatTurn turn, User user) {
    List<TagLink> tagLinks =
        tagLinkService.getTagLinksByEntity(TagLinkTargetType.CHAT_TURN, turn.getId(), user);
    return (tagLinks == null || tagLinks.isEmpty())
        ? null
        : tagLinks.stream()
            .map(tagLink -> tagLink.getTag().getTagName())
            .collect(Collectors.joining(","));
  }

  private int mapScoreToInt(HumanEvalScore score) {
    Double value = score.getScore();
    if (value == null) return 0;
    if (value == 1.0) return 1;
    if (value == -1.0) return -1;
    return 0;
  }
}

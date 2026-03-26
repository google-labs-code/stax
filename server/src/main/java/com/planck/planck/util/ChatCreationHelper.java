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

package com.planck.planck.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.domain.evaluation.EvaluationScoreHelper;
import com.planck.planck.domain.evaluation.dto.LLMEvaluationDTO;
import com.planck.planck.domain.importexport.dto.TurnCreationResult;
import com.planck.planck.domain.importexport.dto.message.AssistantMessageExportDTO;
import com.planck.planck.domain.importexport.dto.message.BaseMessageExportDTO;
import com.planck.planck.domain.importexport.dto.message.SystemMessageExportDTO;
import com.planck.planck.domain.importexport.dto.message.UserMessageExportDTO;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.HumanEvalScore;
import com.planck.planck.entitities.HumanEvaluator;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.InferenceStatus;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.ScoreV2;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationStatusEnum;
import com.planck.planck.enums.InferenceStatusEnum;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.ImportValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class ChatCreationHelper {

  private static final Set<Integer> ALLOWED_EVAL_SCORES = Set.of(-1, 0, 1);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static TurnCreationResult createTurnsFromJson(
      String chatJson,
      Chat chat,
      User user,
      Map<String, Model> modelMap,
      HumanEvaluator defaultHumanEvaluator,
      boolean processUntilLast,
      @Nullable EvaluationContainer container,
      List<LLMEvaluator> evaluators)
      throws JsonProcessingException {

    if (chatJson == null || chatJson.isBlank()) {
      return new TurnCreationResult(Collections.emptyList(), Collections.emptyMap());
    }
    List<BaseMessageExportDTO> messages =
        objectMapper.readValue(chatJson, new TypeReference<>() {});
    if (messages == null || messages.isEmpty()) {
      return new TurnCreationResult(Collections.emptyList(), Collections.emptyMap());
    }

    List<ChatTurn> turns = new ArrayList<>();
    Map<Integer, String> turnIndexToTagsMap = new HashMap<>();
    List<ModelInput> currentInputs = new ArrayList<>();

    long assistantMessagesCount =
        messages.stream().filter(m -> m instanceof AssistantMessageExportDTO).count();
    long limit =
        processUntilLast ? Math.max(0, assistantMessagesCount - 1) : assistantMessagesCount;
    long assistantMessagesProcessed = 0;

    InferenceMonitoring lastInferenceMonitoring = null;
    for (BaseMessageExportDTO messageDto : messages) {
      if (messageDto instanceof SystemMessageExportDTO sysDto) {
        currentInputs.add(
            createModelInput(
                sysDto.getContent(), InputRole.SYSTEM, null, sysDto.getVariables(), user));
      } else if (messageDto instanceof UserMessageExportDTO userDto) {
        currentInputs.add(
            createModelInput(
                userDto.getContent(),
                InputRole.USER,
                userDto.getExpectedOutput(),
                userDto.getVariables(),
                user));
      } else if (messageDto instanceof AssistantMessageExportDTO assistantDto) {
        if (assistantMessagesProcessed >= limit) {
          currentInputs.clear();
          continue;
        }
        processAssistantMessage(
            assistantDto,
            chat,
            user,
            modelMap,
            defaultHumanEvaluator,
            container,
            turns,
            turnIndexToTagsMap,
            currentInputs,
            lastInferenceMonitoring,
            evaluators);
        lastInferenceMonitoring =
            turns.get(turns.size() - 1).getModelResponse().getInferenceMonitoring();
        assistantMessagesProcessed++;
      }
    }

    addFinalTurnIfNeeded(processUntilLast, turns, chat, user, currentInputs);

    return new TurnCreationResult(turns, turnIndexToTagsMap);
  }

  private static ChatTurn createChatTurn(Chat chat, User user, List<ModelInput> inputs) {
    ChatTurn turn = new ChatTurn();
    turn.setChat(chat);
    turn.setUser(user);
    turn.setInputs(inputs);
    return turn;
  }

  private static void processAssistantMessage(
      AssistantMessageExportDTO dto,
      Chat chat,
      User user,
      Map<String, Model> modelMap,
      HumanEvaluator defaultHumanEvaluator,
      @Nullable EvaluationContainer container,
      List<ChatTurn> turns,
      Map<Integer, String> turnIndexToTagsMap,
      List<ModelInput> currentInputs,
      InferenceMonitoring previousInferenceMonitoring,
      List<LLMEvaluator> evaluators) {

    ChatTurn turn = createChatTurn(chat, user, new ArrayList<>(currentInputs));

    ModelResponse response =
        createModelResponse(
            dto,
            user,
            modelMap,
            defaultHumanEvaluator,
            container,
            previousInferenceMonitoring,
            evaluators);
    turn.setModelResponse(response);
    if (dto.getInferenceTokens() != null) {
      InferenceStatus status = createInferenceStatus(user, container);
      status.setChatTurn(turn);
      turn.setInferenceStatus(status);
    }

    turns.add(turn);

    if (dto.getTags() != null && !dto.getTags().isBlank()) {
      turnIndexToTagsMap.put(turns.size() - 1, dto.getTags());
    }

    currentInputs.clear();
  }

  private static void addFinalTurnIfNeeded(
      boolean processUntilLast,
      List<ChatTurn> turns,
      Chat chat,
      User user,
      List<ModelInput> currentInputs) {
    if (!processUntilLast && !currentInputs.isEmpty()) {
      ChatTurn turn = createChatTurn(chat, user, currentInputs);
      turns.add(turn);
    }
  }

  public static ModelInput createModelInput(
      String text, InputRole role, String expectedOutput, Map<String, String> metaData, User user) {
    ModelInput input = new ModelInput();
    input.setText(text);
    input.setRole(role);
    input.setUser(user);
    input.setVariables(metaData);
    input.setExpectedOutput(expectedOutput);
    return input;
  }

  private static ModelResponse createModelResponse(
      AssistantMessageExportDTO dto,
      User user,
      Map<String, Model> modelMap,
      HumanEvaluator defaultHumanEvaluator,
      @Nullable EvaluationContainer container,
      InferenceMonitoring previousInferenceMonitoring,
      List<LLMEvaluator> evaluators) {
    ModelResponse response = new ModelResponse();
    response.setText(dto.getContent());
    response.setUser(user);

    Model model = null;
    String modelLabel = dto.getModelNickname();
    if (modelLabel != null && modelMap.containsKey(modelLabel)) {
      model = modelMap.get(modelLabel);
      response.setModel(model);
    }

    Integer evalValue = dto.getHumanEvaluation();
    if (evalValue != null) {
      HumanEvalScore score =
          createHumanEvalScore(
              evalValue, dto.getHumanEvaluationNotes(), response, user, defaultHumanEvaluator);
      response.setHumanEvalScores(Collections.singletonList(score));
    }

    if (dto.getInferenceTokens() != null) {
      response.setInferenceMonitoring(
          createInferenceMonitoring(
              model,
              container,
              user,
              dto.getInferenceLatency(),
              dto.getInferenceTokens().getInputTokens(),
              dto.getInferenceTokens().getOutputTokens(),
              dto.getInferenceTokens().getTotalTokens(),
              previousInferenceMonitoring));
    }

    if (dto.getLlmEvaluations() != null) {
      response.setScores(
          dto.getLlmEvaluations().entrySet().stream()
              .map(evalDto -> createScore(evalDto, user, container, response, evaluators))
              .flatMap(Optional::stream)
              .collect(Collectors.toList()));
    }

    if (container != null) {
      response.setContainer(container);
    }
    return response;
  }

  public static Optional<ScoreV2> createScore(
      Entry<String, LLMEvaluationDTO> evalDtoEntry,
      User user,
      EvaluationContainer container,
      ModelResponse response,
      List<LLMEvaluator> evaluators) {
    ScoreV2 score = new ScoreV2();
    score.setUserId(user.getId());

    LLMEvaluator llmEvaluator =
        evaluators.stream()
            .filter(
                evaluator ->
                    evaluator
                        .getName()
                        .replaceAll(EvaluationScoreHelper.normalizationPattern, "")
                        .equals(evalDtoEntry.getKey()))
            .findFirst()
            .orElse(null);
    if (llmEvaluator == null) {
      return Optional.empty();
    }

    score.setModelResponse(response);
    score.setLlmEvaluator(llmEvaluator);
    score.setScore(Double.valueOf(evalDtoEntry.getValue().getScore()));
    score.setScoreType(llmEvaluator.getOutputFormatType());
    score.setScorer(llmEvaluator.getName());
    score.setLlmResponse(evalDtoEntry.getValue().getLlmResponse());

    EvaluationStatus status = new EvaluationStatus();
    status.setUserId(user.getId());
    if (container != null) {
      status.setContainerId(container.getId());
    }
    status.setEvaluatorId(llmEvaluator.getId());
    status.setStatus(EvaluationStatusEnum.SUCCESSFUL.getKey());
    score.setEvaluationStatus(status);
    return Optional.of(score);
  }

  private static InferenceMonitoring createInferenceMonitoring(
      Model model,
      EvaluationContainer container,
      User user,
      Double latency,
      Integer inputTokens,
      Integer outputTokens,
      Integer totalTokens,
      InferenceMonitoring previousInferenceMonitoring) {
    InferenceMonitoring inferenceMonitoring =
        new InferenceMonitoring(model, user, container, previousInferenceMonitoring);
    inferenceMonitoring.setCompletionTokens(outputTokens);
    inferenceMonitoring.setPromptTokens(inputTokens);
    inferenceMonitoring.setTimetaken(latency);
    inferenceMonitoring.setTotalTokens(totalTokens);
    return inferenceMonitoring;
  }

  public static InferenceStatus createInferenceStatus(User user, EvaluationContainer container) {
    InferenceStatus inferenceStatus = new InferenceStatus();
    inferenceStatus.setUserId(user.getId());
    if (container != null) {
      inferenceStatus.setContainerId(container.getId());
    }
    inferenceStatus.setStatus(InferenceStatusEnum.SUCCESSFUL.getKey());
    return inferenceStatus;
  }

  private static HumanEvalScore createHumanEvalScore(
      Integer evalValue,
      String notes,
      ModelResponse response,
      User user,
      HumanEvaluator defaultHumanEvaluator) {

    if (!ALLOWED_EVAL_SCORES.contains(evalValue)) {
      throw new ImportValidationException(
          "Invalid HumanEvalScore value: '"
              + evalValue
              + "'. Allowed values are "
              + ALLOWED_EVAL_SCORES);
    }

    if (defaultHumanEvaluator == null) {
      throw new ImportValidationException(
          "Cannot assign HumanEvalScore because a default HumanEvaluator is not configured.");
    }

    HumanEvalScore score =
        new HumanEvalScore((double) evalValue, response, defaultHumanEvaluator, user);
    score.setNotes(notes);
    return score;
  }
}

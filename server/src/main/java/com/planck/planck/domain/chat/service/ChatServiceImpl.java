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

package com.planck.planck.domain.chat.service;

import com.planck.planck.config.ApplicationLimits;
import com.planck.planck.config.ContainerLimitProvider;
import com.planck.planck.domain.chat.ChatRepository;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.dataset.dto.ChatTurnGroupCreateRequest;
import com.planck.planck.domain.dataset.dto.RowCreateRequest;
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
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.HumanEvalScore;
import com.planck.planck.entitities.HumanEvaluator;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.InferenceStatus;
import com.planck.planck.entitities.InputUsageCountDTO;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.ScoreV2;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InputRole;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoringMechanismType;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.exceptions.ResourceLimitExceedException;
import com.planck.planck.exceptions.UserQuotaExceededException;
import com.planck.planck.llmproviders.dto.Prompt;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatServiceImpl implements ChatService {

  @Autowired private ChatTurnRepository chatTurnRepository;
  @Autowired private ScoreV2Repository scoreV2Repository;
  @Autowired private ModelInputRepository modelInputRepository;
  @Autowired private ModelResponseRepository modelResponseRepository;
  @Autowired private InferenceStatusRepository inferenceStatusRepository;
  @Autowired private InferenceMonitoringRepository inferenceMonitoringRepository;
  @Autowired private EvaluationStatusRepository evaluationStatusRepository;
  @Autowired private ChatRepository chatRepository;
  @Autowired private ModelService modelService;
  @Autowired private ApplicationLimits applicationLimits;
  @Autowired private SXSHumanFeedbackRepository sxsHumanFeedbackRepository;
  @Autowired private EvaluationContainerRepository evaluationContainerRepository;
  @Autowired private HumanEvaluatorServiceImpl humanEvaluatorService;
  @Autowired private ContainerLimitProvider containerLimitProvider;

  private static final String CHAT_TURN_LIMIT_EXCEEDED_MESSAGE =
      "Limit for DataSet with id: %s is exceeded.";

  @Transactional(readOnly = true)
  @Override
  public List<ChatTurn> getChatDTO(String chatId, User user, List<String> tagIds) {
    Integer tagCount = tagIds != null ? tagIds.size() : null;
    List<ChatTurn> chat =
        chatTurnRepository.findByChatIdAndUserIdOrderBySequenceId(chatId, user, tagIds, tagCount);
    if (chat == null || chat.isEmpty())
      throw new IllegalArgumentException("Chat not found for id: " + chatId);
    return chat;
  }

  @Override
  public List<Chat> findChatsByUserAndIds(Collection<String> ids, User user) {
    return chatRepository.findByUserAndIdIn(user, ids);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChatTurn> getChat(String chatId, User user) {
    return getChat(chatId, user, null);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChatTurn> getChatUntilTurn(String chatId, String turnId, User user) {
    ChatTurn targetTurn = chatTurnRepository.findByIdAndUser(turnId, user);

    if (targetTurn == null) {
      throw new NotFoundException("Turn not found with ID: " + turnId);
    }

    if (!targetTurn.getChat().getId().equals(chatId)) {
      throw new NotFoundException("Turn ID " + turnId + " does not belong to chat ID " + chatId);
    }

    Integer maxSequenceIdForQuery = targetTurn.getSequenceId();

    List<ChatTurn> result =
        chatTurnRepository.findByChatIdAndUserIdUntilSequenceIdOrderBySequenceId(
            chatId, user, maxSequenceIdForQuery);

    enrichTurnsWithSystemInstructions(result);

    return result;
  }

  @Override
  @Transactional
  public ChatTurn createChatTurn(
      User user,
      String containerId,
      @Nullable String chatId,
      String modelPrompt,
      String modelResponseText,
      String modelId) {

    Prompt prompt = null;
    if (modelPrompt != null && !modelPrompt.isEmpty()) {
      prompt = new Prompt(InputRole.USER, modelPrompt);
    }

    return createChatTurn(user, containerId, chatId, prompt, modelResponseText, modelId);
  }

  @Override
  @Transactional
  public ChatTurn createChatTurn(
      User user,
      String containerId,
      @Nullable String chatId,
      @Nullable Prompt prompt,
      @Nullable String modelResponseText,
      String modelId) {

    EvaluationContainer container =
        evaluationContainerRepository
            .findById(containerId)
            .orElseThrow(() -> new NotFoundException("Container not found: " + containerId));
    if (!container.getUser().getId().equals(user.getId())) {
      throw new SecurityException("User does not have access to this container.");
    }

    ModelResponse modelResponse = null;
    if (modelId != null && !modelId.isEmpty()) {
      modelResponse = new ModelResponse();
      Model model = modelService.getModelForUser(user, modelId);

      modelResponse.setModel(model);
      modelResponse.setUser(user);

      if (modelResponseText != null) {
        modelResponse.setText(modelResponseText);
      }

      modelResponse = modelResponseRepository.save(modelResponse);
    }

    Chat chat;
    int sequenceId = 0;

    if (chatId == null || chatId.isEmpty()) {
      chat = createChat(user, container);
    } else {
      chat = getChatById(chatId, user);
      Integer maxSequence =
          findMaxSequenceIdByUserAndContainerAndChatId(user, chat.getId(), container);
      sequenceId = (maxSequence != null) ? maxSequence + 1 : 0;
    }

    ModelInput input = null;
    if (prompt != null) {
      input = new ModelInput(prompt, user);
    }

    ChatTurn chatTurn = new ChatTurn(user, chat, sequenceId, input, modelResponse);

    long count = chatTurnRepository.countDistinctChatIdsByContainer(container, user);
    if (count >= containerLimitProvider.getMaxChatsLimit(container)) {
      throw new UserQuotaExceededException("Max Quota Reached for this container.");
    }

    return chatTurnRepository.save(chatTurn);
  }

  @Override
  public ChatTurn updateTurn(
      User user,
      String containerId,
      String chatTurnId,
      String modelPrompt,
      String modelResponse,
      String modelId,
      String expectedOutput) {

    Prompt prompt = (modelPrompt != null) ? new Prompt(InputRole.USER, modelPrompt) : null;

    return updateTurn(
        user, containerId, chatTurnId, prompt, modelResponse, modelId, expectedOutput);
  }

  @Override
  public ChatTurn updateTurn(
      User user,
      String containerId,
      String chatTurnId,
      @Nullable Prompt prompt,
      @Nullable String modelResponse,
      @Nullable String modelId,
      @Nullable String expectedOutput) {

    ChatTurn chatTurn = chatTurnRepository.findByIdAndUser(chatTurnId, user);
    if (chatTurn == null) {
      throw new NotFoundException("Chat Turn not found: " + chatTurnId);
    }

    if (!chatTurn.getChat().getContainer().getId().equals(containerId)) {
      throw new NotFoundException("ChatTurn not found for the provided container");
    }

    updatePromptIfNeeded(chatTurn, user, prompt, expectedOutput);
    updateModelResponseIfNeeded(chatTurn, user, modelResponse, modelId);

    ChatTurn updated = chatTurnRepository.save(chatTurn);
    return updated;
  }

  private void updatePromptIfNeeded(
      ChatTurn chatTurn, User user, @Nullable Prompt prompt, @Nullable String expectedOutput) {

    if (prompt == null && expectedOutput == null) {
      return;
    }

    ModelInput modelInputToUse = null;

    if (prompt != null && chatTurn.getInputs() != null) {
      modelInputToUse =
          chatTurn.getInputs().stream()
              .filter(mi -> mi.getRole() == prompt.getRole())
              .findFirst()
              .orElse(null);
    }

    if (modelInputToUse == null && prompt != null) {
      modelInputToUse = new ModelInput(prompt, user);
      chatTurn.getInputs().add(modelInputToUse);
    }

    if (modelInputToUse != null) {

      if (prompt != null) {
        modelInputToUse.setText(prompt.getText());
      }

      if (expectedOutput != null) {
        modelInputToUse.setExpectedOutput(expectedOutput);
      }
      modelInputRepository.save(modelInputToUse);
    }
  }

  private void updateModelResponseIfNeeded(
      ChatTurn chatTurn, User user, String modelResponse, String modelId) {

    if (modelResponse == null && modelId == null) {
      return;
    }

    ModelResponse responseToUpdate = chatTurn.getModelResponse();

    if (responseToUpdate == null) {
      responseToUpdate = new ModelResponse();
      responseToUpdate.setUser(user);
      chatTurn.setModelResponse(responseToUpdate);
    }

    if (modelId != null) {
      Model model = modelService.getModelForUser(user, modelId);
      responseToUpdate.setModel(model);
    }

    if (modelResponse != null) {
      responseToUpdate.setText(modelResponse);
    }

    modelResponseRepository.save(responseToUpdate);
  }

  @Override
  public ChatTurn saveChatTurn(ChatTurn chatTurn, User user) {

    EvaluationContainer container = chatTurn.getChat().getContainer();
    if (container == null) {
      throw new IllegalArgumentException(
          "ChatTurn must belong to a Chat that is linked to a Container.");
    }

    int maxChatsInContainer = containerLimitProvider.getMaxChatsLimit(container);
    long count = chatTurnRepository.countDistinctChatIdsByContainer(container, user);

    if (chatTurnRepository.countByChat(chatTurn.getChat()) <= 1 && count >= maxChatsInContainer) {
      throw new UserQuotaExceededException(
          String.format(
              "Max Quota Reached (%s) for this container.",
              String.format("%,d", maxChatsInContainer)));
    }

    return chatTurnRepository.save(chatTurn);
  }

  @Override
  public List<ChatTurn> saveChat(List<ChatTurn> chatTurns) {
    return chatTurnRepository.saveAllAndFlush(chatTurns);
  }

  @Transactional(readOnly = true)
  @Override
  public ChatTurn getChatTurn(String chatTurnId, User user) {
    return chatTurnRepository.findByIdAndUser(chatTurnId, user);
  }

  @Override
  public ChatTurnDTO getChatTurnDTO(String chatTurnId, User user) {
    return new ChatTurnDTO(this.getChatTurn(chatTurnId, user));
  }

  @Transactional(readOnly = false)
  @Override
  public List<ChatTurn> duplicateChat(ChatTurn existingChatTurn) {
    return duplicateChatInternal(
        existingChatTurn,
        originalTurn -> {
          ModelResponse originalResponse = originalTurn.getModelResponse();
          return (originalResponse != null)
              ? originalResponse.deepCopy(originalResponse.getContainer())
              : null;
        });
  }

  @Transactional(readOnly = false)
  @Override
  public List<ChatTurn> duplicateChatNewLastModelResponse(ChatTurn existingChatTurn, Model model) {
    Integer maxSequence = existingChatTurn.getSequenceId();

    ModelResponse newFinalResponse = new ModelResponse();
    newFinalResponse.setModel(model);
    newFinalResponse.setUser(existingChatTurn.getUser());

    modelResponseRepository.save(newFinalResponse);

    return duplicateChatInternal(
        existingChatTurn,
        originalTurn -> {
          if (originalTurn.getSequenceId().equals(maxSequence)) {
            return newFinalResponse;
          } else {
            ModelResponse originalResponse = originalTurn.getModelResponse();
            if (originalResponse == null) {
              return null;
            }
            ModelResponse copiedResponse =
                originalResponse.deepCopy(originalResponse.getContainer());
            copiedResponse.setModel(model);

            return copiedResponse;
          }
        });
  }

  @Transactional(readOnly = false)
  @Override
  public List<ChatTurn> duplicateChatWithNewModel(ChatTurn existingChatTurn, Model newModel) {
    return duplicateChatInternal(
        existingChatTurn,
        originalTurn -> {
          ModelResponse originalResponse = originalTurn.getModelResponse();
          if (originalResponse == null) {
            return null;
          }
          ModelResponse copiedResponse = originalResponse.deepCopy(originalResponse.getContainer());
          copiedResponse.setModel(newModel);
          return copiedResponse;
        });
  }

  private Chat duplicateChat(Chat chat) {
    Integer maxSequence =
        chat.getTurns().stream().map(ChatTurn::getSequenceId).max(Integer::compareTo).orElse(0);
    Function<ChatTurn, ModelResponse> responseResolver =
        chatTurn ->
            chatTurn.getSequenceId().equals(maxSequence) ? null : chatTurn.getModelResponse();
    return duplicateChat(chat, chat.getUser(), chat.getVariables(), responseResolver);
  }

  private List<ChatTurn> duplicateChatInternal(
      ChatTurn existingChatTurn, Function<ChatTurn, ModelResponse> responseCreationStrategy) {

    List<ChatTurn> existingChat = getExistingChatTurns(existingChatTurn);
    return duplicateChatTurns(
            existingChatTurn.getChat().getContainer(),
            existingChat,
            existingChatTurn.getUser(),
            existingChatTurn.getChat().getVariables(),
            responseCreationStrategy)
        .getTurns();
  }

  private List<ChatTurn> getExistingChatTurns(ChatTurn existingChatTurn) {
    return chatTurnRepository.findChatTillSequence(
        existingChatTurn.getChat().getId(),
        existingChatTurn.getSequenceId(),
        existingChatTurn.getUser());
  }

  private Chat duplicateChatTurns(
      EvaluationContainer container,
      List<ChatTurn> existingChatTurns,
      User user,
      Map<String, String> variables,
      Function<ChatTurn, ModelResponse> responseCreationStrategy) {

    Chat newChat = createChat(user, container);
    newChat.setVariables(variables);

    List<ChatTurn> newTurns = new ArrayList<>();

    for (ChatTurn originalTurn : existingChatTurns) {
      List<ModelInput> newInputs =
          originalTurn.getInputs().stream().map(ModelInput::deepCopy).collect(Collectors.toList());

      ModelResponse responseForNewTurn = responseCreationStrategy.apply(originalTurn);

      ChatTurn newChatTurn =
          new ChatTurn(newInputs, responseForNewTurn, originalTurn.getUser(), newChat);
      newChatTurn.setSequenceId(originalTurn.getSequenceId());
      newChatTurn.setCreatedAt(new Timestamp(System.currentTimeMillis()));
      newChatTurn.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
      newTurns.add(newChatTurn);
      newChat.addTurn(newChatTurn);
    }

    List<ModelInput> allNewInputs =
        newTurns.stream().flatMap(t -> t.getInputs().stream()).collect(Collectors.toList());
    modelInputRepository.saveAll(allNewInputs);

    List<ModelResponse> allNewResponses =
        newTurns.stream()
            .map(ChatTurn::getModelResponse)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    modelResponseRepository.saveAll(allNewResponses);

    newChat.setTurns(newTurns);
    return chatRepository.save(newChat);
  }

  private Chat duplicateChat(
      Chat chat,
      User user,
      Map<String, String> variables,
      Function<ChatTurn, ModelResponse> responseCreationStrategy) {

    return duplicateChatTurns(
        chat.getContainer(), chat.getTurns(), user, variables, responseCreationStrategy);
  }

  @Override
  public Page<ChatTurn> fetchLastTurns(
      User user, EvaluationContainer container, List<String> tagIds, Pageable pageable) {
    Integer tagCount = tagIds != null ? tagIds.size() : 0;
    Page<ChatTurn> result =
        chatTurnRepository.findLatestChatTurnsByUser(user, container, tagIds, tagCount, pageable);
    enrichTurnsWithSystemInstructions(result.getContent());
    return result;
  }

  @Override
  public List<ChatTurn> getChatTurnsByList(List<String> chatIds, User user) {
    return chatTurnRepository.findChatTurnsByList(chatIds, user);
  }

  @Override
  public List<ChatTurn> getChatTurnsByChatIdList(List<String> chatIds, User user) {
    return chatTurnRepository.findChatTurnsByChatList(chatIds, user);
  }

  @Transactional
  @Override
  public void deleteChats(User user, String containerId) {
    EvaluationContainer evaluationContainer =
        evaluationContainerRepository
            .findById(containerId)
            .orElseThrow(() -> new NotFoundException("Container not found: " + containerId));
    List<ChatTurn> chatTurns =
        chatTurnRepository.findAllByUserAndChatContainer(user, evaluationContainer);
    delete(chatTurns);
  }

  @Transactional
  @Override
  public void deleteChats(List<String> chatIds, User user, String containerId) {
    EvaluationContainer evaluationContainer =
        evaluationContainerRepository
            .findById(containerId)
            .orElseThrow(() -> new NotFoundException("Container not found: " + containerId));
    List<ChatTurn> chatTurns =
        chatTurnRepository.findAllByUserAndContainerAndChatIdIn(user, evaluationContainer, chatIds);
    delete(chatTurns);
  }

  @Transactional
  @Override
  public void deleteChats(List<Chat> chats) {
    chatRepository.deleteAll(chats);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChatTurn> getChat(String chatId, User user, List<String> tagIds) {
    Integer tagCount = tagIds != null ? tagIds.size() : null;
    List<ChatTurn> result =
        chatTurnRepository.findByChatIdAndUserIdOrderBySequenceId(chatId, user, tagIds, tagCount);
    enrichTurnsWithSystemInstructions(result);
    return result;
  }

  @Override
  public Integer findMaxSequenceIdByUserAndContainerAndChatId(
      User user, String chatId, EvaluationContainer container) {
    return chatTurnRepository.findMaxSequenceIdByUserAndContainerAndChatId(user, container, chatId);
  }

  @Override
  public List<ChatTurn> findAllByUserAndChatContainer(User user, EvaluationContainer container) {
    return chatTurnRepository.findAllByUserAndChatContainer(user, container);
  }

  @Transactional
  @Override
  public void delete(ChatTurn chatTurn) {
    delete(Collections.singletonList(chatTurn));
  }

  @Transactional
  @Override
  public void delete(User user, String chatTurnId) {
    ChatTurn chatTurn =
        chatTurnRepository
            .findById(chatTurnId)
            .orElseThrow(() -> new NotFoundException("ChatTurn not found: " + chatTurnId));
    if (!chatTurn.getUser().getId().equals(user.getId())) {
      throw new NotFoundException("ChatTurn does not belong to the user");
    }
    delete(Collections.singletonList(chatTurn));
  }

  @Transactional
  @Override
  public void delete(User user, List<String> chatTurnIds) {
    if (chatTurnIds == null || chatTurnIds.isEmpty()) return;

    List<ChatTurn> chatTurns =
        chatTurnRepository.findAllById(chatTurnIds).stream()
            .filter(ct -> ct.getUser().getId().equals(user.getId()))
            .toList();

    delete(chatTurns);
  }

  @Transactional
  @Override
  public void delete(List<ChatTurn> chatTurns) {
    if (chatTurns.isEmpty()) return;

    List<ScoreV2> allScores =
        chatTurns.stream()
            .flatMap(
                ct ->
                    ct.getModelResponse() != null
                        ? ct.getModelResponse().getScores().stream()
                        : Stream.empty())
            .toList();

    Set<ModelInput> candidateInputs =
        chatTurns.stream()
            .flatMap(ct -> ct.getInputs() != null ? ct.getInputs().stream() : Stream.empty())
            .collect(Collectors.toSet());

    Set<ModelResponse> orphanedResponses =
        chatTurns.stream()
            .map(ChatTurn::getModelResponse)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    List<InferenceMonitoring> orphanedInferenceMonitorings = new ArrayList<>();
    List<InferenceStatus> orphanedInferenceStatuses = new ArrayList<>();

    Set<Chat> candidateChats = new HashSet<>();

    for (ChatTurn ct : chatTurns) {
      if (ct.getChat() != null) {
        candidateChats.add(ct.getChat());
      }
      if (ct.getInputs() != null && !ct.getInputs().isEmpty()) {
        ct.getInputs().clear();
      }

      if (ct.getInferenceStatus() != null) {
        ct.getInferenceStatus().setChatTurn(null);
        orphanedInferenceStatuses.add(ct.getInferenceStatus());
      }
      ct.setInferenceStatus(null);
      if (ct.getModelResponse() != null && ct.getModelResponse().getInferenceMonitoring() != null) {
        InferenceMonitoring inferenceMonitoring = ct.getModelResponse().getInferenceMonitoring();
        inferenceMonitoring.setContainer(null);
        orphanedInferenceMonitorings.add(inferenceMonitoring);
      }
      ct.setModelResponse(null);
    }

    if (!allScores.isEmpty()) {
      for (ScoreV2 score : allScores) {
        if (score.getEvaluationMonitoring() != null) {
          score.getEvaluationMonitoring().setContainer(null);
        }
      }
      scoreV2Repository.deleteAll(allScores);
    }

    inferenceStatusRepository.deleteAll(orphanedInferenceStatuses);

    for (ModelResponse response : orphanedResponses) {
      if (!chatTurnRepository.existsByModelResponse(response)) {
        modelResponseRepository.delete(response);
      }
    }

    Map<String, Long> usageMap =
        chatTurnRepository.countUsagesForInputs(candidateInputs).stream()
            .collect(Collectors.toMap(InputUsageCountDTO::inputId, InputUsageCountDTO::usageCount));

    for (ModelInput input : candidateInputs) {
      if (!usageMap.containsKey(input.getId())) {
        modelInputRepository.delete(input);
      }
    }

    chatTurnRepository.deleteAll(chatTurns);

    for (Chat chat : candidateChats) {
      if (chatTurnRepository.countByChat(chat) == 0) {
        chatRepository.delete(chat);
      }
    }

    inferenceMonitoringRepository.saveAll(orphanedInferenceMonitorings);
  }

  @Override
  public List<String> findLatestChatTurnIdsByProjectId(String projectId, User user) {
    return chatTurnRepository.findLatestChatTurnIdsByContainerId(projectId, user);
  }

  @Override
  public boolean existsByModelResponse(ModelResponse modelResponse) {
    return chatTurnRepository.existsByModelResponse(modelResponse);
  }

  @Transactional
  @Override
  public void deleteTurnResultsByProject(User user, String projectId) {
    deleteTurnResultsByTurnIds(user, projectId, findLatestChatTurnIdsByProjectId(projectId, user));
  }

  @Transactional
  @Override
  public void deleteTurnResultsByTurnIds(User user, String projectId, List<String> chatTurnIds) {
    List<ChatTurn> chatTurnList = getChatTurnsByList(chatTurnIds, user);
    deleteTurnResults(user, projectId, chatTurnList);
  }

  @Transactional
  @Override
  public void deleteTurnResults(User user, String projectId, List<ChatTurn> chatTurnList) {

    List<ModelResponse> modelResponses = new ArrayList<>();
    List<ScoreV2> scores = new ArrayList<>();
    List<InferenceStatus> inferenceStatuses = new ArrayList<>();
    List<EvaluationStatus> evaluationStatuses = new ArrayList<>();

    for (ChatTurn chatTurn : chatTurnList) {
      if (chatTurn.getModelResponse() != null) {
        ModelResponse modelResponse = chatTurn.getModelResponse();
        modelResponses.add(modelResponse);
        if (modelResponse.getInferenceMonitoring() != null) {
          InferenceMonitoring inferenceMonitoring = modelResponse.getInferenceMonitoring();
          inferenceMonitoring.setContainer(null);
        }

        if (chatTurn.getModelResponse() != null
            && chatTurn.getModelResponse().getScores() != null) {
          scores.addAll(chatTurn.getModelResponse().getScores());
          for (ScoreV2 score : scores) {
            if (score.getEvaluationMonitoring() != null) {
              score.getEvaluationMonitoring().setContainer(null);
            }
          }
        }

        chatTurn.setModelResponse(null);
      }

      if (chatTurn.getInferenceStatus() != null) {
        inferenceStatuses.add(chatTurn.getInferenceStatus());
        chatTurn.setInferenceStatus(null);
      }

      chatTurnRepository.saveAllAndFlush(chatTurnList);
      modelResponseRepository.deleteAll(modelResponses);
      scoreV2Repository.deleteAll(scores);
      inferenceStatusRepository.deleteAll(inferenceStatuses);
      evaluationStatusRepository.deleteAll(evaluationStatuses);

      sxsHumanFeedbackRepository.deleteByChatTurnIdList(
          chatTurnList.stream().map(ChatTurn::getId).collect(Collectors.toList()));
    }
  }

  @Override
  public List<String> findIdsWithNonEmptyOutput(List<String> chatTurnIds) {
    return chatTurnRepository.findIdsWithNonEmptyModelResponse(chatTurnIds);
  }

  @Override
  public Chat createChat(User user, EvaluationContainer container) {
    return chatRepository.save(buildChat(null, user, null, container));
  }

  @Override
  public Chat createChat(String id, EvaluationContainer container, User user) {
    return chatRepository.save(buildChat(id, user, null, container));
  }

  @Override
  public Chat createChat(
      String id, EvaluationContainer container, User user, Map<String, String> variables) {
    return chatRepository.save(buildChat(id, user, variables, container));
  }

  @Override
  public Chat getChatById(String chatId, User user) {
    return chatRepository
        .findByIdAndUser(chatId, user)
        .orElseThrow(() -> new NotFoundException("Chat not found: " + chatId));
  }

  @Override
  public Optional<Chat> findByChatId(String chatId, User user) {
    return chatRepository.findByIdAndUser(chatId, user);
  }

  @Transactional(readOnly = true)
  @Override
  public List<Chat> getAllChatsByUser(User user) {
    return chatRepository.findAllByUser(user);
  }

  @Transactional(readOnly = true)
  @Override
  public List<Chat> getAllChatsByContainer(EvaluationContainer container) {
    return chatRepository.findAllByContainer(container);
  }

  @Override
  public void deleteChat(String chatId, User user) {
    Chat chat = getChatById(chatId, user);
    chatRepository.delete(chat);
  }

  @Override
  public void addChatTurn(Chat chat, ChatTurn chatTurn) {
    chatTurn.setChat(chat);
    chat.getTurns().add(chatTurn);
    chatRepository.save(chat);
  }

  @Override
  public List<com.planck.planck.enums.ModelProvider> getDistinctModelProvidersByProject(
      User user, String projectId) {
    return chatTurnRepository.findDistinctModelProvidersByContainerId(projectId, user);
  }

  @Override
  public Chat replaceVariables(String chatId, User user, Map<String, String> newVariables) {

    if (newVariables.size() > applicationLimits.getMaxVariableKeysPerChat()) {
      throw new UserQuotaExceededException(
          "Exceeded variables key limit. Maximum allowed is "
              + applicationLimits.getMaxVariableKeysPerChat()
              + ".");
    }

    Chat chat = getChatById(chatId, user);

    chat.setVariables(newVariables);

    return chatRepository.save(chat);
  }

  @Override
  public Chat addOrUpdateVariables(String chatId, User user, Map<String, String> variablesUpdates) {
    Chat chat = getChatById(chatId, user);

    if (variablesUpdates == null || variablesUpdates.isEmpty()) {
      return chat;
    }

    Map<String, String> currentVariables = chat.getVariables();

    long newKeysCount =
        variablesUpdates.keySet().stream()
            .filter(key -> !currentVariables.containsKey(key))
            .count();

    if (currentVariables.size() + newKeysCount > applicationLimits.getMaxVariableKeysPerChat()) {
      throw new UserQuotaExceededException(
          "Exceeded variables key limit. Maximum allowed is "
              + applicationLimits.getMaxVariableKeysPerChat()
              + ".");
    }

    currentVariables.putAll(variablesUpdates);
    chat.setVariables(currentVariables);

    return chatRepository.save(chat);
  }

  @Override
  public Chat removeVariablesKey(String chatId, User user, String key) {
    Chat chat = getChatById(chatId, user);
    Map<String, String> currentVariables = chat.getVariables();

    if (currentVariables != null && currentVariables.containsKey(key)) {
      currentVariables.remove(key);
      chat.setVariables(currentVariables);
      return chatRepository.save(chat);
    }

    return chat;
  }

  @Override
  public Map<String, String> getVariables(String chatId, User user) {
    Chat chat = getChatById(chatId, user);

    return chat.getVariables() != null ? chat.getVariables() : Collections.emptyMap();
  }

  @Override
  public List<String> getVariablesKeys(String chatId, User user) {
    Chat chat = getChatById(chatId, user);
    Map<String, String> variables = chat.getVariables();

    return variables != null ? new ArrayList<>(variables.keySet()) : new ArrayList<>();
  }

  @Override
  @Transactional
  public Map<String, Chat> duplicateChatsForSideB(List<Chat> chats, User user) {
    Map<String, Chat> newChats = new HashMap<>();

    for (Chat chat : chats) {
      Chat newChat = duplicateChat(chat);
      newChats.put(chat.getId(), newChat);
    }

    return newChats;
  }

  @Override
  public void enrichTurnsWithSystemInstructions(List<ChatTurn> turns) {
    if (turns == null || turns.isEmpty()) {
      return;
    }

    Map<String, List<ChatTurn>> instructionsByChat = fetchAndGroupInstructions(turns);

    Map<String, List<ChatTurn>> turnsByChat =
        turns.stream().collect(Collectors.groupingBy(turn -> turn.getChat().getId()));

    turnsByChat.forEach(
        (chatId, turnsInThisChat) -> {
          List<ChatTurn> instructionsInThisChat = instructionsByChat.get(chatId);
          enrichTurnsForSingleChat(turnsInThisChat, instructionsInThisChat);
        });
  }

  private Map<String, List<ChatTurn>> fetchAndGroupInstructions(List<ChatTurn> turns) {
    List<String> allChatIds =
        turns.stream().map(turn -> turn.getChat().getId()).distinct().collect(Collectors.toList());

    List<ChatTurn> allInstructions =
        chatTurnRepository.findSystemInstructionTurnsInChats(allChatIds);

    return allInstructions.stream().collect(Collectors.groupingBy(inst -> inst.getChat().getId()));
  }

  private void enrichTurnsForSingleChat(
      List<ChatTurn> turnsInThisChat, List<ChatTurn> instructionsInThisChat) {
    if (instructionsInThisChat == null || instructionsInThisChat.isEmpty()) {
      return;
    }

    TreeMap<Timestamp, String> instructionTimeline =
        buildInstructionTimeline(instructionsInThisChat);

    applyInstructionsToTurns(turnsInThisChat, instructionTimeline);
  }

  private TreeMap<Timestamp, String> buildInstructionTimeline(List<ChatTurn> instructions) {
    return instructions.stream()
        .collect(
            Collectors.toMap(
                ChatTurn::getCreatedAt,
                inst ->
                    inst.getInputs().stream()
                        .filter(inp -> inp.getRole() == InputRole.SYSTEM)
                        .findFirst()
                        .map(ModelInput::getText)
                        .orElse(""),
                (existing, replacement) -> replacement,
                TreeMap::new));
  }

  private void applyInstructionsToTurns(
      List<ChatTurn> turnsToEnrich, TreeMap<Timestamp, String> instructionTimeline) {
    for (ChatTurn currentTurn : turnsToEnrich) {
      Map.Entry<Timestamp, String> instructionEntry =
          instructionTimeline.floorEntry(currentTurn.getCreatedAt());
      if (instructionEntry != null) {
        currentTurn.setEffectiveSystemInstruction(instructionEntry.getValue());
      }
    }
  }

  private Chat buildChat(
      String id, User user, Map<String, String> variables, EvaluationContainer container) {
    Chat chat = new Chat();
    chat.setContainer(container);
    chat.setId((id != null) ? id : Chat.ID_PREFIX + UUID.randomUUID());
    chat.setUser(user);
    if (variables != null) chat.setVariables(variables);
    return chat;
  }

  @Override
  public Chat saveChat(Chat chat) {
    return chatRepository.save(chat);
  }

  @Override
  public List<ChatTurn> getFullChatHistory(ChatTurn anyTurnInChat) {
    if (anyTurnInChat == null || anyTurnInChat.getChat() == null) {
      return Collections.emptyList();
    }
    return chatTurnRepository.findByChatIdAndContainerAndUserIdOrderBySequenceId(
        anyTurnInChat.getChat().getId(),
        anyTurnInChat.getChat().getContainer(),
        anyTurnInChat.getUser());
  }

  @Override
  public Optional<Model> getModelFromChatHistory(List<ChatTurn> chatHistory) {
    return chatHistory.stream()
        .map(ChatTurn::getModelResponse)
        .filter(Objects::nonNull)
        .map(ModelResponse::getModel)
        .filter(Objects::nonNull)
        .findFirst();
  }

  @Transactional
  @Override
  public ChatTurn appendMissingTurns(
      Chat destinationChat,
      List<ChatTurn> sourceTurns,
      EvaluationContainer container,
      int startIndex) {

    List<ChatTurn> turnsToCreate = new ArrayList<>();
    for (int i = startIndex; i < sourceTurns.size(); i++) {
      ChatTurn sourceTurn = sourceTurns.get(i);
      ChatTurn newTurn = createNewTurnFromSource(sourceTurn, destinationChat, container, null);
      turnsToCreate.add(newTurn);
    }

    saveTurnsInBatch(turnsToCreate);

    return turnsToCreate.get(turnsToCreate.size() - 1);
  }

  private ChatTurn createNewTurnFromSource(
      ChatTurn sourceTurn,
      Chat destinationChat,
      EvaluationContainer container,
      @Nullable Model model) {
    List<ModelInput> newInputs =
        sourceTurn.getInputs().stream().map(ModelInput::deepCopy).collect(Collectors.toList());

    ModelResponse newResponse = null;
    if (sourceTurn.getModelResponse() != null) {
      newResponse = sourceTurn.getModelResponse().deepCopy(container);
      newResponse.setModel(model);
    }

    ChatTurn newChatTurn =
        new ChatTurn(newInputs, newResponse, container.getUser(), destinationChat);
    newChatTurn.setSequenceId(sourceTurn.getSequenceId());
    destinationChat.addTurn(newChatTurn);
    return newChatTurn;
  }

  private void saveTurnsInBatch(List<ChatTurn> turns) {
    List<ModelInput> allNewInputs =
        turns.stream().flatMap(t -> t.getInputs().stream()).collect(Collectors.toList());
    modelInputRepository.saveAll(allNewInputs);

    List<ModelResponse> allNewResponses =
        turns.stream()
            .map(ChatTurn::getModelResponse)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    modelResponseRepository.saveAll(allNewResponses);

    chatTurnRepository.saveAll(turns);
  }

  @Transactional
  @Override
  public void copyResponseToTurn(
      ChatTurn sourceTurn, ChatTurn destinationTurn, Model modelForDestination) {
    if (sourceTurn.getModelResponse() == null) {
      throw new IllegalStateException("Source turn has no response to copy.");
    }
    if (destinationTurn.getModelResponse() != null
        && destinationTurn.getModelResponse().getText() != null) {
      return;
    }

    ModelResponse originalResponse = sourceTurn.getModelResponse();
    ModelResponse copiedResponse = originalResponse.deepCopy(originalResponse.getContainer());
    copiedResponse.setModel(null);

    modelResponseRepository.save(copiedResponse);
    destinationTurn.setModelResponse(copiedResponse);
    chatTurnRepository.save(destinationTurn);
  }

  @Override
  public List<String> getAllChatIds(User user, EvaluationContainer container) {
    return chatTurnRepository.findAllChatIdsByUserAndContainer(user, container);
  }

  @Override
  public List<ChatTurnDTO> addTurnsInBulk(
      User user, String containerId, List<ChatTurnGroupCreateRequest> bulkRequestList) {

    EvaluationContainer container =
        evaluationContainerRepository
            .findById(containerId)
            .orElseThrow(() -> new NotFoundException("Container not found: " + containerId));

    List<ChatTurnDTO> allResults = new ArrayList<>();
    for (ChatTurnGroupCreateRequest group : bulkRequestList) {
      List<RowCreateRequest> rows = group.getRows();
      String chatId = group.getChatId();

      if (chatId == null || chatId.isEmpty()) {
        chatId = "chat-" + UUID.randomUUID();
      }
      final String effectiveChatId = chatId;

      for (RowCreateRequest row : rows) {
        row.setChatId(chatId);
      }

      int startingSeq = getStartingSequence(user, effectiveChatId, container);

      List<ChatTurnDTO> groupResult =
          bulkInsertCommon(
              user, rows, container, (i, req, cont) -> Pair.of(effectiveChatId, startingSeq + i));

      allResults.addAll(groupResult);
    }

    return allResults;
  }

  private List<ChatTurnDTO> bulkInsertCommon(
      User user,
      List<RowCreateRequest> requests,
      EvaluationContainer container,
      ChatIdSequenceGenerator chatSeqGen) {

    Map<String, Model> modelMap = modelService.buildModelMap(user, Model::getLabel);

    HumanEvaluator defaultHumanEvaluator =
        humanEvaluatorService
            .getEvaluators(user, ScopeType.USER, ScoringMechanismType.CATEGORY)
            .stream()
            .findFirst()
            .orElse(null);

    validateChatTurnLimit(container, requests.size());

    List<ChatTurn> turnsToSave = new ArrayList<>();

    for (int i = 0; i < requests.size(); i++) {
      RowCreateRequest req = requests.get(i);
      List<ModelInput> turnInputs = buildModelInputsForRow(req, user);
      ModelResponse response = buildModelResponseForRow(req, user, modelMap, defaultHumanEvaluator);

      Pair<String, Integer> chatSeq = chatSeqGen.generate(i, req, container);
      Chat chat = findOrCreateChat(chatSeq.getLeft(), container, user, req.getVariables());

      ChatTurn turn = buildChatTurn(user, chat, chatSeq.getRight(), turnInputs, response);
      turnsToSave.add(turn);
    }

    chatTurnRepository.saveAllAndFlush(turnsToSave);
    return turnsToSave.stream().map(ChatTurnDTO::new).toList();
  }

  private List<ModelInput> buildModelInputsForRow(RowCreateRequest req, User user) {
    List<ModelInput> turnInputs = new ArrayList<>();

    Prompt userPrompt = new Prompt(InputRole.USER, req.getModelPrompt());
    ModelInput userInput = new ModelInput(userPrompt, user);
    if (req.getExpectedOutput() != null && !req.getExpectedOutput().isEmpty()) {
      userInput.setExpectedOutput(req.getExpectedOutput());
    }
    turnInputs.add(userInput);

    if (req.getSystemInstruction() != null && !req.getSystemInstruction().trim().isEmpty()) {
      Prompt systemPrompt = new Prompt(InputRole.SYSTEM, req.getSystemInstruction());
      ModelInput systemInput = new ModelInput(systemPrompt, user);
      turnInputs.add(systemInput);
    }

    return turnInputs;
  }

  private ModelResponse buildModelResponseForRow(
      RowCreateRequest req,
      User user,
      Map<String, Model> modelMap,
      HumanEvaluator defaultHumanEvaluator) {

    if (req.getModelResponse() == null || req.getModelResponse().isEmpty()) {
      return null;
    }

    ModelResponse response = new ModelResponse();
    response.setUser(user);
    response.setText(req.getModelResponse());
    if (req.getModelLabel() != null && !req.getModelLabel().isEmpty()) {
      Model model = modelMap.get(req.getModelLabel());

      response.setModel(model);
    }

    Integer evalValue = req.getHumanScore();

    if (evalValue != null) {
      if (defaultHumanEvaluator == null) {
      } else if (evalValue != -1 && evalValue != 0 && evalValue != 1) {
      } else {
        double scoreValue = (double) evalValue;
        HumanEvalScore humanEvalScore =
            new HumanEvalScore(scoreValue, response, defaultHumanEvaluator, user);
        response.setHumanEvalScores(new ArrayList<>(List.of(humanEvalScore)));
      }
    }

    return response;
  }

  private Chat findOrCreateChat(
      String chatId, EvaluationContainer container, User user, Map<String, String> variables) {
    return findByChatId(chatId, user).orElseGet(() -> createChat(chatId, container, user));
  }

  private ChatTurn buildChatTurn(
      User user, Chat chat, int sequenceId, List<ModelInput> inputs, ModelResponse response) {

    ChatTurn turn = new ChatTurn();
    turn.setUser(user);
    turn.setChat(chat);
    turn.setSequenceId(sequenceId);
    turn.setInputs(inputs);
    turn.setModelResponse(response);
    return turn;
  }

  private int getStartingSequence(User user, String chatId, EvaluationContainer container) {
    Integer startingSequence =
        findMaxSequenceIdByUserAndContainerAndChatId(user, chatId, container);
    return (startingSequence != null) ? startingSequence + 1 : 0;
  }

  @FunctionalInterface
  private interface ChatIdSequenceGenerator {
    Pair<String, Integer> generate(int i, RowCreateRequest req, EvaluationContainer container);
  }

  private void validateChatTurnLimit(EvaluationContainer container, int newChatTurnsToAdd) {
    int currentCount = chatTurnRepository.countByContainer(container);
    int maxAllowed = containerLimitProvider.getMaxChatsLimit(container);
    if ((currentCount + newChatTurnsToAdd) > maxAllowed) {
      throw new ResourceLimitExceedException(
          String.format(CHAT_TURN_LIMIT_EXCEEDED_MESSAGE, container.getId()));
    }
  }
}

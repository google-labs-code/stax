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

package com.planck.planck.domain.inference.service;

import com.planck.planck.annotation.CheckJobActive;
import com.planck.planck.domain.apikeys.service.ApiKeysService;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.evaluationmonitoring.EvaluationMonitoringService;
import com.planck.planck.domain.inference.dto.BulkInferenceResponseDTO;
import com.planck.planck.domain.inference.dto.InferenceDTO;
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
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationMonitoring;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.InferenceStatus;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.ScoreV2;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InferenceStatusEnum;
import com.planck.planck.enums.JobStatusEnum;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.ChatProviderFactory;
import com.planck.planck.llmproviders.ChatProviderStrategy;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.util.PlanckConstants;
import com.planck.planck.util.PromptUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class InferenceServiceImpl implements InferenceService {

  @Autowired ModelService modelService;
  @Autowired ProjectService projectService;
  @Autowired ApiKeysService apiKeysService;
  @Autowired InferenceMonitoringService inferenceMonitoringService;
  @Autowired ModelResponseService modelResponseService;
  @Autowired ModelInputService modelInputService;
  @Autowired ChatService chatService;
  @Autowired JobStatusService jobStatusService;
  @Autowired InferenceStatusService inferenceStatusService;
  @Autowired UserService userService;
  @Autowired TagLinkService tagLinkService;
  @Autowired EvaluationMonitoringService evaluationMonitoringService;
  @Autowired InferencePublisherService publisherService;
  @Autowired ChatProviderFactory chatProviderFactory;
  @Autowired InferencePreparationService inferencePreparationService;

  @Override
  @Transactional
  public ChatTurnDTO addModelResponseToChatturn(
      User user,
      Project project,
      String modelId,
      List<ChatTurn> previousChatTurns,
      ChatTurn chatTurn) {
    Model model = modelService.getModelForUser(user, modelId);
    if (model.isDeprecated()) throw new IllegalInputException("Model is deprecated");

    InferenceMonitoring previousInferenceMonitoring =
        getPreviousInferenceMonitoring(previousChatTurns);

    List<Prompt> chatPrompt =
        inferencePreparationService.buildPromptHistory(previousChatTurns, chatTurn);

    InferenceDTO dto = new InferenceDTO();
    dto.setUserId(user.getId());
    dto.setProjectId(project.getId());
    dto.setChatTurnId(chatTurn.getId());
    dto.setModelId(modelId);
    dto.setComments("Sync inference");

    ChatResponseWithLatency chatResponse =
        executeInferenceWithModel(model, user, chatPrompt, dto, chatTurn, project);
    if (chatResponse == null) {
      // Inference failed and status/chatTurn updated in executeInferenceWithModel
      return new ChatTurnDTO(chatService.saveChatTurn(chatTurn, user));
    }

    saveSuccessfulInferenceResponse(
        user, project, model, chatResponse, previousInferenceMonitoring, dto, chatTurn);
    ChatTurnDTO chatDtoResult = new ChatTurnDTO(chatService.saveChatTurn(chatTurn, user));
    tagLinkService.syncInferenceMonitoringTags(
        chatTurn.getModelResponse().getInferenceMonitoring().getId(), user);
    return chatDtoResult;
  }

  @Transactional(readOnly = false)
  @Override
  public ChatTurnDTO runInference(
      User user,
      String projectId,
      String modelId,
      List<Prompt> prompts,
      List<ChatTurn> previousChatTurns,
      Map<String, String> variables) {
    Project project = projectService.getProjectForUser(user, projectId);
    ChatTurn previousChatTurn =
        previousChatTurns != null && !previousChatTurns.isEmpty()
            ? previousChatTurns.get(previousChatTurns.size() - 1)
            : null;
    ChatTurn chatTurn = saveModelInputRequest(user, project, prompts, previousChatTurn, variables);
    return addModelResponseToChatturn(user, project, modelId, previousChatTurns, chatTurn);
  }

  private ChatTurn saveModelInputRequest(
      User user,
      Project project,
      List<Prompt> prompts,
      ChatTurn previousChatTurn,
      Map<String, String> variables) {
    validatePrompts(prompts);

    List<ModelInput> modelInputs = new ArrayList<>();

    Chat chat;

    if (previousChatTurn != null) {
      chat = previousChatTurn.getChat();
      chat = chatService.addOrUpdateVariables(chat.getId(), user, variables);
    } else {
      chat = chatService.createChat(null, project, user, variables);
    }

    for (Prompt prompt : prompts) {
      if (PromptUtil.containsVariableInput(prompt.getText())) {
        modelInputs.add(modelInputService.savePrompt(user, prompt, chat.getVariables()));
      } else {
        modelInputs.add(modelInputService.savePrompt(user, prompt));
      }
    }

    ChatTurn chatTurn =
        previousChatTurn != null
            ? new ChatTurn(modelInputs, user, previousChatTurn)
            : new ChatTurn(modelInputs, user, chat);
    return chatService.saveChatTurn(chatTurn, user);
  }

  @Override
  public BulkInferenceResponseDTO runReinferenceForProject(
      User user, String projectId, List<String> modelIds) {
    List<String> chatTurnIds = getChatTurnIdsByProjectId(user, projectId);

    log.info(
        "Running bulk quick compare inference for project: {}, number of requests: {}",
        projectId,
        chatTurnIds.size());

    return runInferenceCommon(user, projectId, chatTurnIds, modelIds);
  }

  @Override
  public BulkInferenceResponseDTO runReinference(
      User user, String projectId, List<String> chatTurnIds, List<String> modelIds) {
    log.info(
        "Running bulk quick compare inference for project: {}, number of requests: {}",
        projectId,
        chatTurnIds.size());
    return runInferenceCommon(user, projectId, chatTurnIds, modelIds);
  }

  private BulkInferenceResponseDTO runInferenceCommon(
      User user, String projectId, List<String> chatTurnIds, List<String> modelIds) {
    Project project = projectService.getProjectForUser(user, projectId);

    List<ChatTurn> chatTurns = chatService.getChatTurnsByList(chatTurnIds, user);

    Map<String, List<ChatTurn>> modelToChatTurns = filterChatTurnsByModels(chatTurns, modelIds);
    int totalTasks = modelToChatTurns.values().stream().mapToInt(List::size).sum();
    if (totalTasks == 0) {
      throw new IllegalInputException("No eligible chat turns found for inference");
    }

    JobStatus job =
        jobStatusService.createScorerJobStatus(
            PlanckConstants.INFERENCE, null, user, JobStatusEnum.PENDING, totalTasks, project);

    // Determine if this is a bulk job (more than 10 chat turns)
    boolean isBulk = chatTurnIds.size() > 10;

    // In case the chat turn doesn't have a model response, for the 1st loop interation
    // We have to run with modelId which was provided without duplicate. Any following runs -
    // duplicate it.
    boolean duplicateOnNoResponse = false;
    for (Map.Entry<String, List<ChatTurn>> entry : modelToChatTurns.entrySet()) {
      String modelId = entry.getKey();
      List<ChatTurn> turns = entry.getValue();
      for (ChatTurn ct : turns) {
        InferenceDTO dto = new InferenceDTO();
        dto.setUserId(user.getId());
        dto.setProjectId(projectId);
        dto.setJobId(job.getId());
        dto.setChatTurnId(ct.getId());

        try {
          if (modelId != null) {
            publisherService.sendInferenceWithModel(
                ct, dto, modelId, duplicateOnNoResponse, isBulk);
          } else {
            publisherService.sendInferenceUsingExistingModel(ct, dto);
          }
        } catch (Exception e) {
          dto.setComments("Inference processing failed: " + e.getMessage());
          log.error("Failed to process inference for jobId={}. Error: {}", dto.getJobId(), e);
          inferenceStatusService.createOrUpdateInferenceStatus(dto, InferenceStatusEnum.FAILED);
          trackJobStatus(dto);
        }
      }
      // after 1st loop, duplicate any chat turns without model response
      duplicateOnNoResponse = true;
    }

    return new BulkInferenceResponseDTO(job.getId(), chatTurnIds);
  }

  private Map<String, List<ChatTurn>> filterChatTurnsByModels(
      List<ChatTurn> chatTurns, List<String> modelIds) {
    Map<String, List<ChatTurn>> modelToChatTurns = new HashMap<>();
    if (modelIds == null || modelIds.isEmpty()) {
      List<ChatTurn> eligible =
          chatTurns.stream()
              .filter(
                  ct -> {
                    InferenceStatus s = ct.getInferenceStatus();
                    return s == null || !InferenceStatusEnum.PENDING.getKey().equals(s.getStatus());
                  })
              .collect(Collectors.toList());
      // null - to use original model id from chat turn
      modelToChatTurns.put(null, eligible);
    } else {
      for (String modelId : modelIds) {
        List<ChatTurn> eligible =
            chatTurns.stream()
                .filter(
                    ct -> {
                      // no response/model - eligible, since that is the first inference for that
                      // chat turn
                      ModelResponse mr = ct.getModelResponse();
                      if (mr == null || mr.getModel() == null) {
                        return true;
                      }

                      InferenceStatus s = ct.getInferenceStatus();
                      if (s != null
                          && InferenceStatusEnum.PENDING.getKey().equals(s.getStatus())
                          && modelId.equals(mr.getModel().getId())) {
                        return false;
                      }
                      return true;
                    })
                .collect(Collectors.toList());
        modelToChatTurns.put(modelId, eligible);
      }
    }
    return modelToChatTurns;
  }

  @Transactional
  @Override
  public boolean reRunInferenceAndUpdateResponse(InferenceDTO dto) {
    boolean successful = false;
    User user = userService.findByUserId(dto.getUserId());
    Project project = projectService.getProjectForUser(user, dto.getProjectId());

    ChatTurn chatTurn = getValidatedChatTurn(dto.getChatTurnId(), user);
    List<ChatTurn> chatHistory = validateIsLatestChatTurn(chatTurn, user);

    Model model = resolveModel(user, chatTurn, dto);
    List<Prompt> prompts = inferencePreparationService.buildPromptHistory(chatHistory, chatTurn);

    ChatProviderStrategy providerStrategy = chatProviderFactory.getStrategy(model, user);

    ModelResponse previousModelResponse = chatTurn.getModelResponse();
    chatTurn.setModelResponse(null);

    InferenceMonitoring previousMonitoring =
        previousModelResponse != null ? previousModelResponse.getInferenceMonitoring() : null;
    Set<String> monitoringIds = getMonitoringIds(chatTurn);
    String reply = null;
    String thinking = null;
    InferenceMonitoring monitoring = null;
    try {
      ChatResponseWithLatency chatResponse = providerStrategy.chat(prompts);
      reply = chatResponse.getReply();
      thinking = chatResponse.getThinking();

      monitoring =
          inferenceMonitoringService.saveInferenceMonitoring(
              user, chatResponse, model, project, previousMonitoring);
      successful = true;
    } catch (Exception e) {
      log.error("Exception while communicating with LLM: {}", e);
      dto.setReason(e.getMessage());
    }

    ModelResponse response =
        createOrUpdateModelResponse(
            chatTurn, previousModelResponse, reply, thinking, model, user, project, monitoring);
    chatTurn.setModelResponse(response);
    chatService.saveChatTurn(chatTurn, user);

    cleanupOrphanedEntities(previousModelResponse, monitoringIds, user);

    return successful;
  }

  private List<String> getChatTurnIdsByProjectId(User user, String projectId) {
    List<String> chatTurnIds = chatService.findLatestChatTurnIdsByProjectId(projectId, user);

    if (CollectionUtils.isEmpty(chatTurnIds)) {
      throw new IllegalInputException(
          "Project does not have any chat turns. Provide a valid project.");
    }
    return chatTurnIds;
  }

  @CheckJobActive
  @Override
  public void process(InferenceDTO inferenceDTO) {
    try {
      InferenceStatus currentInferenceStatus =
          inferenceStatusService.findByChatTurnIdAndUser(
              inferenceDTO.getChatTurnId(), inferenceDTO.getUserId());

      if (InferenceStatusEnum.getInferenceStatusEnumByKey(currentInferenceStatus.getStatus())
          == InferenceStatusEnum.STOPPED) {
        log.info(
            "Skipping processing for Inference (id: {}), because its status is STOPPED.",
            currentInferenceStatus.getId());
        return;
      }
      inferenceStatusService.updateInferenceStatus(
          inferenceDTO, InferenceStatusEnum.IN_PROGRESS, "Response In-progress");

      boolean successful = reRunInferenceAndUpdateResponse(inferenceDTO);
      // to be implemented - current functionality does not include tracking overall job status
      trackJobStatus(inferenceDTO);
      InferenceStatusEnum status =
          successful ? InferenceStatusEnum.SUCCESSFUL : InferenceStatusEnum.FAILED;
      String inferenceComment =
          successful ? "Inference Processing Successfully done." : "Inference Status failed";
      inferenceStatusService.updateInferenceStatus(inferenceDTO, status, inferenceComment);
      trackJobStatus(inferenceDTO);
    } catch (Exception e) {
      log.error("Inference run failed due to exception: {}", e);
      inferenceStatusService.updateInferenceStatus(
          inferenceDTO,
          InferenceStatusEnum.FAILED,
          "Inference run failed due to error." + e.getMessage());
      trackJobStatus(inferenceDTO);
    }
  }

  @Transactional(readOnly = false)
  @Override
  public ChatResponseWithLatency runEvaluationInference(
      User user, Model model, List<ModelInput> modelInputs) {
    if (model.isDeprecated()) throw new IllegalInputException("Model is deprecated");

    List<Prompt> prompts = new ArrayList<>();
    for (ModelInput modelInput : modelInputs) {
      prompts.add(new Prompt(modelInput));
    }

    ChatProviderStrategy providerStrategy = chatProviderFactory.getStrategy(model, user);
    return providerStrategy.chat(prompts);
  }

  private ChatTurn getValidatedChatTurn(String chatTurnId, User user) {
    ChatTurn chatTurn = chatService.getChatTurn(chatTurnId, user);
    if (chatTurn == null) {
      throw new NotFoundException("ChatTurn: " + chatTurnId + " not found for this user");
    }

    if (chatTurn.getInputs() == null || chatTurn.getInputs().isEmpty()) {
      throw new IllegalInputException("ChatTurn does not have any inputs");
    }

    // prepare variables on model input
    for (ModelInput input : chatTurn.getInputs()) {
      if (PromptUtil.containsVariableInput(input.getText())) {
        input.setVariables(chatTurn.getChat().getVariables());
      }
    }

    return chatTurn;
  }

  private List<ChatTurn> validateIsLatestChatTurn(ChatTurn chatTurn, User user) {
    List<ChatTurn> chatHistory = chatService.getChat(chatTurn.getChat().getId(), user);
    if (chatHistory.isEmpty()
        || !chatTurn.getId().equals(chatHistory.get(chatHistory.size() - 1).getId())) {
      throw new IllegalArgumentException("ChatTurn must be the latest in chat");
    }

    if (chatHistory.size() == 1) {
      return new ArrayList<>();
    }

    if (chatHistory.size() > 1) {
      return chatHistory.subList(0, chatHistory.size() - 1);
    }

    return chatHistory;
  }

  private Set<String> getMonitoringIds(ChatTurn chatTurn) {
    return chatTurn.getModelResponse() == null || chatTurn.getModelResponse().getScores() == null
        ? Set.of()
        : chatTurn.getModelResponse().getScores().stream()
            .map(ScoreV2::getEvaluationMonitoring)
            .filter(Objects::nonNull)
            .map(EvaluationMonitoring::getId)
            .collect(Collectors.toSet());
  }

  private void cleanupOrphanedEntities(
      ModelResponse previousModelResponse, Set<String> monitoringIds, User user) {
    if (previousModelResponse != null
        && !chatService.existsByModelResponse(previousModelResponse)) {
      modelResponseService.deleteById(user, previousModelResponse.getId());
    }
    if (!monitoringIds.isEmpty()) {
      evaluationMonitoringService.deleteEvaluationMonitoringAndRelatedScores(monitoringIds, user);
    }
  }

  private void validatePrompts(List<Prompt> prompts) {
    if (prompts.isEmpty())
      throw new NotFoundException("At least 1 prompt is mandatory to run inference");
  }

  protected void trackJobStatus(InferenceDTO inferenceDTO) {
    jobStatusService.trackJobStatusCommon(
        inferenceDTO.getJobId(),
        id ->
            inferenceStatusService.findAllByUserAndJobId(
                inferenceDTO.getJobId(), inferenceDTO.getUserId()));
  }

  private Model resolveModel(User user, ChatTurn chatTurn, InferenceDTO dto) {
    if (dto.getModelId() != null && !dto.getModelId().isBlank()) {
      return modelService.getModelForUser(user, dto.getModelId());
    }

    if (chatTurn.getModelResponse() != null) {
      return chatTurn.getModelResponse().getModel();
    }

    throw new IllegalStateException("No model provided and ChatTurn has no response.");
  }

  private ModelResponse createOrUpdateModelResponse(
      ChatTurn chatTurn,
      ModelResponse previousModelResponse,
      String reply,
      String thinking,
      Model model,
      User user,
      Project project,
      InferenceMonitoring monitoring) {

    ModelResponse response =
        previousModelResponse != null ? previousModelResponse : new ModelResponse();
    response.setText(reply);
    response.setThinking(thinking);
    response.setModel(model);
    response.setUser(user);
    response.setContainer(project);
    response.setInferenceMonitoring(monitoring);
    response.setScores(new ArrayList<>());
    response.setHumanEvalScores(new ArrayList<>());

    return modelResponseService.saveModelResponse(response);
  }

  private ChatResponseWithLatency executeInferenceWithModel(
      Model model,
      User user,
      List<Prompt> chatPrompt,
      InferenceDTO dto,
      ChatTurn chatTurn,
      Project project) {
    ChatProviderStrategy providerStrategy = chatProviderFactory.getStrategy(model, user);
    try {
      return providerStrategy.chat(chatPrompt);
    } catch (Exception e) {
      log.error("Exception while communicating with LLM: {}", e);
      dto.setReason(e.getMessage());
      InferenceStatus inferenceStatus =
          inferenceStatusService.createOrUpdateInferenceStatus(dto, InferenceStatusEnum.FAILED);
      // NOTE: This updates the DTO *before* the transaction is saved, ensuring status is right.
      chatTurn.setModelResponse(modelResponseService.createModelResponse(user, project, model));
      chatTurn.setInferenceStatus(inferenceStatus);
      return null;
    }
  }

  private void saveSuccessfulInferenceResponse(
      User user,
      Project project,
      Model model,
      ChatResponseWithLatency chatResponse,
      InferenceMonitoring previousInferenceMonitoring,
      InferenceDTO dto,
      ChatTurn chatTurn) {
    String responseText = chatResponse.getReply();
    String thinkingText = chatResponse.getThinking();

    InferenceMonitoring inferenceMonitoring =
        inferenceMonitoringService.saveInferenceMonitoring(
            user, chatResponse, model, project, previousInferenceMonitoring);

    ModelResponse modelResponse =
        new ModelResponse(responseText, thinkingText, model, user, project, inferenceMonitoring);
    modelResponseService.saveModelResponse(modelResponse);
    inferenceStatusService.createOrUpdateInferenceStatus(dto, InferenceStatusEnum.SUCCESSFUL);
    inferenceMonitoringService.save(inferenceMonitoring);
    chatTurn.setModelResponse(modelResponse);
  }

  private InferenceMonitoring getPreviousInferenceMonitoring(List<ChatTurn> previousChatTurns) {
    if (previousChatTurns != null && !previousChatTurns.isEmpty()) {
      ChatTurn previousChatTurn = previousChatTurns.get(previousChatTurns.size() - 1);
      if (previousChatTurn != null
          && previousChatTurn.getModelResponse() != null
          && previousChatTurn.getModelResponse().getInferenceMonitoring() != null) {
        return previousChatTurn.getModelResponse().getInferenceMonitoring();
      }
    }
    return null;
  }
}

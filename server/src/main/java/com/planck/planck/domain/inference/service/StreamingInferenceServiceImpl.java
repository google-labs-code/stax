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

import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.inference.dto.InferenceDTO;
import com.planck.planck.domain.inferencemonitoring.InferenceMonitoringService;
import com.planck.planck.domain.inferencestatus.InferenceStatusService;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelresponse.service.ModelResponseService;
import com.planck.planck.domain.project.ProjectService;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InferenceStatusEnum;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.llmproviders.ChatProviderFactory;
import com.planck.planck.llmproviders.ChatProviderStrategy;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.llmproviders.dto.StreamingChatResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class StreamingInferenceServiceImpl implements StreamingInferenceService {

  @Autowired private ChatProviderFactory chatProviderFactory;

  @Autowired private ModelService modelService;

  @Autowired private ProjectService projectService;

  @Autowired private ChatService chatService;

  @Autowired private ModelResponseService modelResponseService;

  @Autowired InferenceMonitoringService inferenceMonitoringService;

  @Autowired InferenceStatusService inferenceStatusService;

  @Autowired TagLinkService tagLinkService;

  @Override
  public Flux<StreamingChatResponse> streamChatCompletion(
      User user,
      String projectId,
      String modelId,
      String previousChatTurnId,
      List<Prompt> prompts) {

    Project project = projectService.getProjectForUser(user, projectId);
    List<ModelInput> modelInputs = new ArrayList<>();
    for (Prompt prompt : prompts) {
      ModelInput modelInput = new ModelInput(prompt, user);
      modelInputs.add(modelInput);
    }

    Chat chat;
    ChatTurn chatTurn;
    List<ChatTurn> previousChatTurns = null;
    if (previousChatTurnId != null) {
      ChatTurn previousChatTurn = chatService.getChatTurn(previousChatTurnId, user);
      if (previousChatTurn == null) {
        throw new IllegalInputException("Previous chat turn not found");
      }
      chat = previousChatTurn.getChat();
      previousChatTurns = chat.getTurns();
      chatTurn = new ChatTurn(modelInputs, null, user, previousChatTurn);
    } else {
      chat = chatService.createChat(user, project);
      chatTurn = new ChatTurn(modelInputs, user, chat);
    }

    ChatTurn savedChatTurn = chatService.saveChatTurn(chatTurn, user);
    chat.getTurns().add(savedChatTurn);

    return streamChatCompletion(user, project, modelId, prompts, previousChatTurns, savedChatTurn);
  }

  private Flux<StreamingChatResponse> streamChatCompletion(
      User user,
      Project project,
      String modelId,
      List<Prompt> prompts,
      List<ChatTurn> previousChatTurns,
      ChatTurn chatTurn) {

    Model model = modelService.getModelForUser(user, modelId);
    if (model.isDeprecated()) {
      throw new IllegalInputException("Model is deprecated");
    }

    final InferenceMonitoring inferenceMonitoring =
        getPreviousInferenceMonitoring(previousChatTurns);
    ;

    List<Prompt> finalPrompts = getPrompts(prompts, previousChatTurns);

    InferenceDTO inferenceDTO = new InferenceDTO();
    inferenceDTO.setUserId(user.getId());
    inferenceDTO.setProjectId(project.getId());
    inferenceDTO.setChatTurnId(chatTurn.getId());
    inferenceDTO.setModelId(modelId);
    inferenceDTO.setComments("Streaming inference");

    inferenceStatusService.createOrUpdateInferenceStatus(inferenceDTO, InferenceStatusEnum.PENDING);

    ChatProviderStrategy providerStrategy = chatProviderFactory.getStrategy(model, user);

    StringBuilder contentBuilder = new StringBuilder();
    final StreamingChatResponse[] lastResponse = new StreamingChatResponse[1];
    final boolean[] finalizationDone = {false};

    Flux<StreamingChatResponse> stream =
        providerStrategy
            .streamChat(finalPrompts)
            .doOnNext(response -> accumulateContent(response, contentBuilder, lastResponse))
            .map(
                response ->
                    processStreamingResponse(
                        response,
                        finalizationDone,
                        contentBuilder,
                        user,
                        model,
                        project,
                        modelId,
                        chatTurn,
                        inferenceMonitoring))
            .doOnError(
                error -> {
                  inferenceDTO.setComments(error.getMessage());
                  inferenceStatusService.createOrUpdateInferenceStatus(
                      inferenceDTO, InferenceStatusEnum.FAILED);
                  log.error("Error during streaming chat completion: {}", error.getMessage());
                });

    return stream;
  }

  private List<Prompt> getPrompts(List<Prompt> prompts, List<ChatTurn> previousChatTurns) {
    if (previousChatTurns == null || previousChatTurns.isEmpty()) {
      return prompts;
    }

    List<Prompt> newPrompts = new ArrayList<>();

    for (ChatTurn chatTurn : previousChatTurns) {
      List<ModelInput> modelInputs = chatTurn.getInputs();
      for (ModelInput modelInput : modelInputs) {
        newPrompts.add(new Prompt(modelInput));
      }

      if (chatTurn.getModelResponse() != null)
        newPrompts.add(new Prompt(chatTurn.getModelResponse()));
    }

    if (prompts != null) {
      newPrompts.addAll(prompts);
    }

    return newPrompts;
  }

  private InferenceMonitoring getPreviousInferenceMonitoring(List<ChatTurn> previousChatTurns) {
    if (previousChatTurns == null || previousChatTurns.isEmpty()) {
      return null;
    }

    ChatTurn previousChatTurn = previousChatTurns.get(previousChatTurns.size() - 1);
    if (previousChatTurn != null
        && previousChatTurn.getModelResponse() != null
        && previousChatTurn.getModelResponse().getInferenceMonitoring() != null) {
      return previousChatTurn.getModelResponse().getInferenceMonitoring();
    }

    return null;
  }

  private void accumulateContent(
      StreamingChatResponse response,
      StringBuilder contentBuilder,
      StreamingChatResponse[] lastResponse) {
    if (response.getContent() != null) {
      contentBuilder.append(response.getContent());
    }
    lastResponse[0] = response;
  }

  private StreamingChatResponse processStreamingResponse(
      StreamingChatResponse response,
      boolean[] finalizationDone,
      StringBuilder contentBuilder,
      User user,
      Model model,
      Project project,
      String modelId,
      ChatTurn chatTurn,
      InferenceMonitoring previousInferenceMonitoring) {

    // If this is the completion response and we haven't done finalization yet
    if (response.isComplete() && !finalizationDone[0]) {
      finalizationDone[0] = true;

      String accumulatedContent = contentBuilder.toString();

      // Create and save all entities
      InferenceMonitoring inferenceMonitoring =
          createInferenceMonitoring(user, response, model, project, previousInferenceMonitoring);
      ModelResponse modelResponse =
          createAndSaveModelResponse(
              accumulatedContent,
              response.getThinking(),
              model,
              user,
              project,
              inferenceMonitoring);
      ChatTurn savedChatTurn = updateAndSaveChatTurn(chatTurn, modelResponse, user);
      createInferenceStatus(user, project, savedChatTurn, modelId);
      ChatTurnDTO chatTurnDTO = createChatTurnDTO(savedChatTurn);
      syncInferenceMonitoringTags(inferenceMonitoring, user);

      // Log completion
      logCompletion(user, modelId, chatTurnDTO, response);

      // Return the completion response with finalChatDTO
      return buildFinalResponse(response, chatTurnDTO);
    }

    // Return the original response for non-completion chunks
    return response;
  }

  private InferenceMonitoring createInferenceMonitoring(
      User user,
      StreamingChatResponse response,
      Model model,
      Project project,
      InferenceMonitoring previousInferenceMonitoring) {

    InferenceMonitoring inferenceMonitoring =
        new InferenceMonitoring(model, user, project, previousInferenceMonitoring);

    if (response.getLatency() != null) {
      inferenceMonitoring.setTimetaken(response.getLatency().doubleValue());
    }

    if (response.getUsage() != null) {
      if (response.getUsage().getPromptTokens() != null) {
        inferenceMonitoring.setPromptTokens(response.getUsage().getPromptTokens());
      }
      if (response.getUsage().getCompletionTokens() != null) {
        inferenceMonitoring.setCompletionTokens(response.getUsage().getCompletionTokens());
      }
      if (response.getUsage().getTotalTokens() != null) {
        inferenceMonitoring.setTotalTokens(response.getUsage().getTotalTokens());
      }
    }

    return inferenceMonitoring;
  }

  private ModelResponse createAndSaveModelResponse(
      String content,
      String thinking,
      Model model,
      User user,
      Project project,
      InferenceMonitoring inferenceMonitoring) {

    ModelResponse modelResponse =
        new ModelResponse(content, thinking, model, user, project, inferenceMonitoring);
    return modelResponseService.saveModelResponse(modelResponse);
  }

  private ChatTurn updateAndSaveChatTurn(
      ChatTurn chatTurn, ModelResponse modelResponse, User user) {
    chatTurn.setModelResponse(modelResponse);
    ChatTurn savedChatTurn = chatService.saveChatTurn(chatTurn, user);

    log.info(
        "Saved ChatTurn - id: {}, modelResponse: {}, chat: {}",
        savedChatTurn.getId(),
        savedChatTurn.getModelResponse() != null
            ? savedChatTurn.getModelResponse().getId()
            : "null",
        savedChatTurn.getChat() != null ? savedChatTurn.getChat().getId() : "null");

    return savedChatTurn;
  }

  private void createInferenceStatus(
      User user, Project project, ChatTurn savedChatTurn, String modelId) {
    InferenceDTO dto = new InferenceDTO();
    dto.setUserId(user.getId());
    dto.setProjectId(project.getId());
    dto.setChatTurnId(savedChatTurn.getId());
    dto.setModelId(modelId);
    dto.setComments("Streaming inference completed");
    inferenceStatusService.createOrUpdateInferenceStatus(dto, InferenceStatusEnum.SUCCESSFUL);
  }

  private ChatTurnDTO createChatTurnDTO(ChatTurn savedChatTurn) {
    ChatTurnDTO chatTurnDTO = new ChatTurnDTO(savedChatTurn);

    log.info(
        "Created ChatTurnDTO: {}, response: {}, model: {}",
        chatTurnDTO.getId(),
        chatTurnDTO.getResponse() != null ? chatTurnDTO.getResponse().getText() : "null",
        chatTurnDTO.getModel() != null ? chatTurnDTO.getModel().getId() : "null");

    return chatTurnDTO;
  }

  private void syncInferenceMonitoringTags(InferenceMonitoring inferenceMonitoring, User user) {
    tagLinkService.syncInferenceMonitoringTags(inferenceMonitoring.getId(), user);
  }

  private void logCompletion(
      User user, String modelId, ChatTurnDTO chatTurnDTO, StreamingChatResponse response) {
    log.info(
        "Streaming chat completion completed for user: {}, model: {}, finalChatDTO: {}",
        user.getId(),
        modelId,
        chatTurnDTO.getId());
  }

  private StreamingChatResponse buildFinalResponse(
      StreamingChatResponse response, ChatTurnDTO chatTurnDTO) {
    StreamingChatResponse finalResponse =
        StreamingChatResponse.builder()
            .content(response.getContent())
            .isComplete(true)
            .usage(response.getUsage())
            .model(response.getModel())
            .finishReason(response.getFinishReason())
            .latency(response.getLatency())
            .finalChatDTO(chatTurnDTO)
            .build();

    log.info(
        "Final response - content: '{}', isComplete: {}, finalChatDTO: {}, finalChatDTO.id: {}",
        finalResponse.getContent() != null
            ? finalResponse
                    .getContent()
                    .substring(0, Math.min(50, finalResponse.getContent().length()))
                + "..."
            : "null",
        finalResponse.isComplete(),
        finalResponse.getFinalChatDTO() != null ? "present" : "null",
        finalResponse.getFinalChatDTO() != null ? finalResponse.getFinalChatDTO().getId() : "N/A");

    return finalResponse;
  }
}

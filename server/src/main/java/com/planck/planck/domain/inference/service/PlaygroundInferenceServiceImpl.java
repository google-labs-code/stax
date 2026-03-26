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
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.service.ModelInputService;
import com.planck.planck.domain.modelresponse.service.ModelResponseService;
import com.planck.planck.domain.project.ProjectService;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaygroundInferenceServiceImpl implements PlaygroundInferenceService {

  private final ChatService chatService;
  private final ModelInputService modelInputService;
  private final ModelResponseService modelResponseService;
  private final ModelService modelService;
  private final ProjectService projectService;
  private final InferenceService inferenceService;

  @Override
  @Transactional
  public ChatTurnDTO runInferenceWithConversation(
      User user,
      String projectId,
      String modelId,
      String initialPreviousTurnId,
      List<Prompt> allPrompts,
      Map<String, String> variables) {

    Project project = projectService.getProjectForUser(user, projectId);
    Model model = modelService.getModelForUser(user, modelId);
    Chat chat = initializeChat(initialPreviousTurnId, user, project);

    // When chatTurn is created from workbook page, user can just set modelId and run inference.
    if (allPrompts == null || allPrompts.isEmpty()) {
      return inferenceService.addModelResponseToChatturn(
          user, project, modelId, chat.getTurns(), chat.getLatestTurn());
    }

    handleContinueFromWorkbookPage(user, allPrompts, project, model, chat);

    int sequenceId =
        chat.getTurns().stream()
            .map(ChatTurn::getSequenceId)
            .max(Comparator.naturalOrder())
            .map(id -> id + 1)
            .orElse(0);

    if (variables != null && !variables.isEmpty()) {
      chatService.addOrUpdateVariables(chat.getId(), user, variables);
    }

    List<List<Prompt>> promptsByTurn = splitPromptsByTurn(allPrompts);

    validatePromptsByTurn(promptsByTurn);

    ChatTurn latestTurn = chat.getLatestTurn();

    if (latestTurn != null && latestTurn.getInputs().isEmpty() && !promptsByTurn.isEmpty()) {

      List<Prompt> completionPrompts = promptsByTurn.get(0);
      completeTurnFromPrompts(latestTurn, user, project, completionPrompts, model);

      promptsByTurn.remove(0);
    }

    for (List<Prompt> turnPrompts : promptsByTurn) {
      createTurnFromPrompts(chat, user, project, turnPrompts, sequenceId, model);
      sequenceId++;
    }

    List<ChatTurn> conversation =
        chat.getTurns().size() > 1
            ? chat.getTurns().subList(0, chat.getTurns().size() - 1)
            : new ArrayList<>();
    ChatTurn finalTurn = chat.getTurns().get(chat.getTurns().size() - 1);

    return inferenceService.addModelResponseToChatturn(
        user, project, modelId, conversation, finalTurn);
  }

  /**
   * This is an edge case, where a user creates a turn from the workbook page and then moves to the
   * prompt playground. The 1st turn is already created, but doesn't have a model response. Then the
   * user adds an Assistant message and continues from there, by also setting the ModelId. So this
   * method handles that scenario.
   */
  private void handleContinueFromWorkbookPage(
      User user, List<Prompt> allPrompts, Project project, Model model, Chat chat) {
    if (allPrompts.get(0).getRole() == InputRole.ASSISTANT) {
      ModelResponse response = chat.getLatestTurn().getModelResponse();
      if (response != null && response.getText() != null) {
        throw new IllegalArgumentException(
            "Attempting to override existing model response is not allowed.");
      }

      if (response == null) {
        response = new ModelResponse(allPrompts.get(0).getText(), model, user, project);
      } else {
        response.setText(allPrompts.get(0).getText());
      }

      ModelResponse savedResponse = modelResponseService.saveModelResponse(response);
      chat.getLatestTurn().setModelResponse(savedResponse);
      chatService.saveChatTurn(chat.getLatestTurn(), user);
      allPrompts.remove(0);
    }
  }

  private void validatePromptsByTurn(List<List<Prompt>> promptsByTurn) {
    if (promptsByTurn.isEmpty()) {
      throw new IllegalArgumentException("Cannot run inference with an empty prompt list.");
    }

    List<Prompt> lastTurn = promptsByTurn.get(promptsByTurn.size() - 1);

    if (lastTurn.isEmpty()) {
      throw new IllegalArgumentException("The final turn of prompts cannot be empty.");
    }

    Prompt lastPrompt = lastTurn.get(lastTurn.size() - 1);
    if (lastPrompt.getRole() == InputRole.ASSISTANT) {
      throw new IllegalArgumentException(
          "Cannot run inference on a turn that ends with an assistant message.");
    }
  }

  private Chat initializeChat(String previousTurnId, User user, EvaluationContainer container) {
    if (previousTurnId == null) {
      return chatService.createChat(user, container);
    }

    ChatTurn previousTurn = chatService.getChatTurn(previousTurnId, user);
    if (previousTurn == null) {
      throw new NotFoundException("Previous ChatTurn not found: " + previousTurnId);
    }
    return previousTurn.getChat();
  }

  private List<List<Prompt>> splitPromptsByTurn(List<Prompt> allPrompts) {
    List<List<Prompt>> promptsByTurn = new ArrayList<>();
    List<Prompt> currentTurn = new ArrayList<>();

    for (Prompt prompt : allPrompts) {
      currentTurn.add(prompt);

      if (prompt.getRole() == InputRole.ASSISTANT) {
        promptsByTurn.add(new ArrayList<>(currentTurn));
        currentTurn.clear();
      }
    }

    if (!currentTurn.isEmpty()) {
      promptsByTurn.add(currentTurn);
    }

    return promptsByTurn;
  }

  private void createTurnFromPrompts(
      Chat chat,
      User user,
      Project project,
      List<Prompt> prompts,
      Integer sequenceId,
      Model model) {
    List<Prompt> inputPrompts = new ArrayList<>();
    Prompt assistantPrompt = null;

    for (Prompt prompt : prompts) {
      if (prompt.getRole() == InputRole.ASSISTANT) {
        assistantPrompt = prompt;
      } else {
        inputPrompts.add(prompt);
      }
    }

    List<ModelInput> savedInputs =
        modelInputService.saveAll(user, inputPrompts, chat.getVariables());

    ChatTurn turn = new ChatTurn();
    turn.setChat(chat);
    turn.setInputs(savedInputs);
    turn.setUser(user);
    turn.setSequenceId(sequenceId);

    if (assistantPrompt != null) {
      ModelResponse response = new ModelResponse(assistantPrompt.getText(), null, user, project);
      ModelResponse savedResponse = modelResponseService.saveModelResponse(response);
      turn.setModelResponse(savedResponse);
    }
    chat.addTurn(turn);

    chatService.saveChatTurn(turn, user);
  }

  private void completeTurnFromPrompts(
      ChatTurn turnToComplete, User user, Project project, List<Prompt> prompts, Model model) {
    List<Prompt> inputPrompts = new ArrayList<>();
    Prompt assistantPrompt = null;

    for (Prompt prompt : prompts) {
      if (prompt.getRole() == InputRole.ASSISTANT) {
        assistantPrompt = prompt;
      } else {
        inputPrompts.add(prompt);
      }
    }

    if (!inputPrompts.isEmpty()) {
      List<ModelInput> savedInputs =
          modelInputService.saveAll(user, inputPrompts, turnToComplete.getChat().getVariables());
      turnToComplete.getInputs().addAll(savedInputs);
    }

    if (assistantPrompt != null) {
      ModelResponse response = new ModelResponse(assistantPrompt.getText(), model, user, project);
      turnToComplete.setModelResponse(response);
    }

    chatService.saveChatTurn(turnToComplete, user);
  }
}

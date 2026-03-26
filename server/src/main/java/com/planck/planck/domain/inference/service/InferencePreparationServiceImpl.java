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
import com.planck.planck.domain.inference.dto.InferenceContext;
import com.planck.planck.domain.inference.dto.InferenceDTO;
import com.planck.planck.domain.inferencestatus.InferenceStatusService;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.service.ModelInputService;
import com.planck.planck.domain.modelresponse.ModelResponseRepository;
import com.planck.planck.domain.user.UserService;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.InferenceStatus;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InferenceStatusEnum;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.PromptUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InferencePreparationServiceImpl implements InferencePreparationService {

  final UserService userService;
  final ChatService chatService;
  final ModelService modelService;
  final InferenceStatusService inferenceStatusService;
  final ModelResponseRepository modelResponseRepository;
  final ModelInputService modelInputService;

  @Override
  public InferenceContext prepareUsingExistingModel(ChatTurn chatTurn, InferenceDTO dto) {
    User user = userService.findByUserId(dto.getUserId());

    if (chatTurn == null) throw new NotFoundException("Chat turn not found for user");

    Model model;
    if (chatTurn.getModelResponse() == null) {
      // If we're trying to run inference without model on the 1st chat turn - no way to determine a
      // model to use
      if (chatTurn.getSequenceId() == 1)
        throw new IllegalArgumentException(
            "Can't run inference for chat turn: " + dto.getChatTurnId());

      // If it's not the fist turn, try to fall back to previous model response.
      List<ChatTurn> chatTurns = chatService.getChat(chatTurn.getChat().getId(), user);
      ChatTurn previousChatTurn = chatTurns.get(chatTurns.size() - 2);
      if (previousChatTurn.getModelResponse() == null)
        throw new IllegalArgumentException(
            "Can't run inference for chat turn: "
                + dto.getChatTurnId()
                + " Unable to determine model to use");
      model = previousChatTurn.getModelResponse().getModel();
    } else {
      model = chatTurn.getModelResponse().getModel();
    }

    dto.setModelId(model.getId());
    return new InferenceContext(model, ObjectMapperUtil.toJsonString(dto));
  }

  @Override
  public InferenceContext prepareWithExplicitModel(
      ChatTurn chatTurn, InferenceDTO dto, String modelId, Boolean duplicateOnNoResponse) {
    User user = userService.findByUserId(dto.getUserId());
    dto.setModelId(modelId);
    Model model = modelService.getModelForUser(user, dto.getModelId());

    if (chatTurn == null) throw new NotFoundException("Chat turn not found");

    // if chat turn has response - we should create the duplicate of all the chat with new chat id
    // and chat turns id with no response for the latest chat turn in the array
    if (shouldDuplicateChat(chatTurn, modelId, duplicateOnNoResponse)) {
      List<ChatTurn> newChatTurns = chatService.duplicateChatNewLastModelResponse(chatTurn, model);
      int size = newChatTurns.size();
      // get the latest chat turn from the created array of turns with new chat id
      ChatTurn newChatTurn = newChatTurns.get(size > 1 ? size - 1 : 0);
      dto.setChatTurnId(newChatTurn.getId());
    } else if (chatTurn.getModelResponse() == null
        || chatTurn.getModelResponse().getModel() == null) {
      updateModelOnChatTurn(chatTurn, user, model);
    }

    return new InferenceContext(model, ObjectMapperUtil.toJsonString(dto));
  }

  private void updateModelOnChatTurn(ChatTurn chatTurn, User user, Model model) {
    ModelResponse response =
        Optional.ofNullable(chatTurn.getModelResponse()).orElseGet(ModelResponse::new);

    response.setUser(user);
    response.setModel(model);
    response.setContainer(chatTurn.getChat().getContainer());
    ModelResponse saved = modelResponseRepository.save(response);

    if (chatTurn.getModelResponse() == null) {
      chatTurn.setModelResponse(saved);
      chatService.saveChatTurn(chatTurn, user);
    }
  }

  private boolean shouldDuplicateChat(
      ChatTurn chatTurn, String targetModelId, boolean duplicateIfNoResponse) {
    ModelResponse response = chatTurn.getModelResponse();

    if (response == null || response.getText() == null) {
      return duplicateIfNoResponse;
    }

    return response.getModel() != null && !response.getModel().getId().equals(targetModelId);
  }

  @Override
  public InferenceDTO createInferenceStatus(InferenceDTO dto) {
    InferenceStatus status =
        inferenceStatusService.createOrUpdateInferenceStatus(dto, InferenceStatusEnum.PENDING);
    dto.setInferenceStatusId(status.getId());
    return dto;
  }

  @Override
  @Transactional
  public List<Prompt> buildPromptHistory(List<ChatTurn> previousTurns, ChatTurn currentTurn) {
    Map<String, String> variables = currentTurn.getChat().getVariables();
    List<Prompt> newPrompts = new ArrayList<>();
    List<ModelInput> inputsToSave = new ArrayList<>();

    List<ChatTurn> allTurns = new ArrayList<>();
    if (previousTurns != null) {
      allTurns.addAll(previousTurns);
    }
    allTurns.add(currentTurn);

    for (ChatTurn chatTurn : allTurns) {
      if (chatTurn.getInputs() != null) {
        for (ModelInput modelInput : chatTurn.getInputs()) {
          if (!MapUtils.isEmpty(variables)
              && PromptUtil.containsVariableInput(modelInput.getText())) {
            modelInput.setVariables(variables);
            inputsToSave.add(modelInput);
          }
          newPrompts.add(new Prompt(modelInput.getRole(), modelInput.getText()));
        }
      }
      if (chatTurn.getModelResponse() != null
          && StringUtils.isNotBlank(chatTurn.getModelResponse().getText())) {
        newPrompts.add(new Prompt(InputRole.ASSISTANT, chatTurn.getModelResponse().getText()));
      }
    }

    if (!inputsToSave.isEmpty()) {
      modelInputService.saveAll(inputsToSave);
    }

    if (!newPrompts.isEmpty()
        && newPrompts.get(newPrompts.size() - 1).getRole() == InputRole.ASSISTANT) {
      newPrompts.remove(newPrompts.size() - 1);
    }

    if (MapUtils.isEmpty(variables)) {
      return newPrompts;
    }

    Set<InputRole> rolesToBeEnriched = Set.of(InputRole.USER, InputRole.SYSTEM);
    for (Prompt prompt : newPrompts) {
      if (prompt.getText() != null && rolesToBeEnriched.contains(prompt.getRole())) {
        String enrichedText = PromptUtil.enrichText(prompt.getText(), variables);
        prompt.setText(enrichedText);
      }
    }
    return newPrompts;
  }
}

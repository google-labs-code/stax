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
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.evaluation.SxsEvaluationPairRepository;
import com.planck.planck.domain.inference.dto.ContinueSxsRequestDTO;
import com.planck.planck.domain.project.ProjectRepository;
import com.planck.planck.domain.project.SxsEvaluationPairService;
import com.planck.planck.domain.project.dto.SxsInferenceRequestDTO;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SxsPlaygroundInferenceServiceImpl implements SxsPlaygroundInferenceService {

  private final PlaygroundInferenceService playgroundInferenceService;
  private final SxsEvaluationPairRepository sxsEvaluationPairRepository;
  private final ProjectRepository projectRepository;
  private final ChatTurnRepository chatTurnRepository;
  private final ChatService chatService;
  private final SxsEvaluationPairService sxsEvaluationPairService;

  @Override
  @Transactional
  public SxsEvaluationPair runInitialSxsInference(
      User user, String projectId, SxsInferenceRequestDTO request) {
    Project project =
        projectRepository
            .findByUserAndId(user, projectId)
            .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));
    if (project.getEvaluationType() != EvaluationType.SXS) {
      throw new IllegalInputException("This inference is only supported for SxS projects.");
    }

    List<Prompt> promptsA = getPromptsForSide(request.getPrompts(), request.getModelAInstruction());

    ChatTurnDTO finalTurnADto =
        playgroundInferenceService.runInferenceWithConversation(
            user, projectId, request.getModelIdA(), null, promptsA, request.getVariables());

    ChatTurn finalTurnA = chatTurnRepository.findById(finalTurnADto.getId()).orElseThrow();

    SxsEvaluationPair pair =
        SxsEvaluationPair.builder()
            .container(project)
            .user(user)
            .chatA(finalTurnA.getChat())
            .chatTurnA(finalTurnA)
            .expectedOutput(request.getExpectedOutput())
            .variables(
                request.getVariables() != null ? new HashMap<>(request.getVariables()) : null)
            .build();

    if (request.getModelIdB() != null && !request.getModelIdB().isBlank()) {
      List<Prompt> promptsB =
          getPromptsForSide(request.getPrompts(), request.getModelBInstruction());
      ChatTurnDTO finalTurnBDto =
          playgroundInferenceService.runInferenceWithConversation(
              user, projectId, request.getModelIdB(), null, promptsB, request.getVariables());

      ChatTurn finalTurnB = chatTurnRepository.findById(finalTurnBDto.getId()).orElseThrow();

      pair.setChatB(finalTurnB.getChat());
      pair.setChatTurnB(finalTurnB);
    }

    return sxsEvaluationPairRepository.save(pair);
  }

  @Override
  @Transactional
  public SxsEvaluationPair continueSxsEvaluation(
      User user, String projectId, String pairId, ContinueSxsRequestDTO request) {

    SxsEvaluationPair pair = findSxsPairOrThrow(pairId, user);

    handlePairSynchronization(pair, user, request);

    ensureSideBIsReady(pair, user, projectId, request);

    runNewInferenceTurns(pair, user, projectId, request);

    return sxsEvaluationPairRepository.save(pair);
  }

  private SxsEvaluationPair findSxsPairOrThrow(String pairId, User user) {
    return sxsEvaluationPairRepository
        .findByIdAndUser(pairId, user)
        .orElseThrow(() -> new NotFoundException("SxS Pair not found: " + pairId));
  }

  private void handlePairSynchronization(
      SxsEvaluationPair pair, User user, ContinueSxsRequestDTO request) {
    boolean bothSidesExist = pair.getChatTurnA() != null && pair.getChatTurnB() != null;
    if (bothSidesExist && !shouldSkipSynchronization(pair, request)) {
      sxsEvaluationPairService.synchronizeSxsPairChats(
          pair, user, request.getModelIdA(), request.getModelIdB());
    }
  }

  private void ensureSideBIsReady(
      SxsEvaluationPair pair, User user, String projectId, ContinueSxsRequestDTO request) {
    boolean sideBNeedsGeneration =
        pair.getChatTurnB() == null && StringUtils.isNotBlank(request.getModelIdB());

    if (sideBNeedsGeneration) {
      sxsEvaluationPairService.generateAndSetTurnB(
          user, projectId, pair.getId(), request.getModelIdB(), false);
    }
  }

  private void runNewInferenceTurns(
      SxsEvaluationPair pair, User user, String projectId, ContinueSxsRequestDTO request) {

    List<Prompt> prompts = request.getPrompts();

    ChatTurnDTO newFinalTurnADto =
        runInferenceForSide(pair.getChatTurnA(), user, projectId, prompts, request.getModelIdA())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Cannot continue conversation: Model is required in the current response or should be provided in the request."));

    Optional<ChatTurnDTO> newFinalTurnBDtoOpt =
        runInferenceForSide(pair.getChatTurnB(), user, projectId, prompts, request.getModelIdB());

    updatePairWithNewTurns(pair, newFinalTurnADto, newFinalTurnBDtoOpt);
  }

  private Optional<ChatTurnDTO> runInferenceForSide(
      @Nullable ChatTurn turn,
      User user,
      String projectId,
      List<Prompt> prompts,
      @Nullable String requestModelId) {

    if (turn == null) {
      return Optional.empty();
    }

    final String modelIdToUse;

    if (turn.getModelResponse() != null && turn.getModelResponse().getModel() != null) {
      String existingModelId = turn.getModelResponse().getModel().getId();

      if (requestModelId != null && !requestModelId.equals(existingModelId)) {
        throw new IllegalArgumentException(
            String.format(
                "Model ID conflict. Turn already has model '%s', but a different model '%s' was requested.",
                existingModelId, requestModelId));
      }
      modelIdToUse = existingModelId;

    } else if (requestModelId != null) {
      modelIdToUse = requestModelId;
    } else {
      return Optional.empty();
    }

    return Optional.of(
        playgroundInferenceService.runInferenceWithConversation(
            user, projectId, modelIdToUse, turn.getId(), prompts, null));
  }

  private void updatePairWithNewTurns(
      SxsEvaluationPair pair, ChatTurnDTO newTurnADto, Optional<ChatTurnDTO> newTurnBDtoOpt) {
    ChatTurn newFinalTurnA = chatTurnRepository.findById(newTurnADto.getId()).orElseThrow();
    pair.setChatTurnA(newFinalTurnA);

    newTurnBDtoOpt.ifPresent(
        dto -> {
          ChatTurn newFinalTurnB = chatTurnRepository.findById(dto.getId()).orElseThrow();
          pair.setChatTurnB(newFinalTurnB);
        });
  }

  private boolean shouldSkipSynchronization(SxsEvaluationPair pair, ContinueSxsRequestDTO request) {
    boolean noModelsInRequest =
        (StringUtils.isBlank(request.getModelIdA()))
            && (StringUtils.isBlank(request.getModelIdB()));

    if (!noModelsInRequest) {
      return false;
    }

    List<ChatTurn> turnsA = chatService.getFullChatHistory(pair.getChatTurnA());
    List<ChatTurn> turnsB = chatService.getFullChatHistory(pair.getChatTurnB());
    return isInitialEmptyState(turnsA) || isInitialEmptyState(turnsB);
  }

  private boolean isInitialEmptyState(List<ChatTurn> turns) {
    if (turns.size() != 1) return false;
    ChatTurn turn = turns.get(0);
    return turn.getModelResponse() == null
        || StringUtils.isBlank(turn.getModelResponse().getText());
  }

  private List<Prompt> getPromptsForSide(List<Prompt> prompts, String systemInstruction) {
    if (StringUtils.isBlank(systemInstruction)) {
      return prompts;
    }

    // This is only used for a new conversation, so we can always append the system instruction
    // as a first prompt. If the requirements change at some point, we can adapt the logic.
    List<Prompt> promptsWithSystem = new ArrayList<>(prompts.size() + 1);
    promptsWithSystem.add(new Prompt(InputRole.SYSTEM, systemInstruction));
    promptsWithSystem.addAll(prompts);

    return promptsWithSystem;
  }
}

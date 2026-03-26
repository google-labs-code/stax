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

package com.planck.planck.domain.project;

import com.planck.planck.domain.chat.ChatRepository;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.chatturn.service.ChatTurnService;
import com.planck.planck.domain.evaluation.SXSHumanFeedbackRepository;
import com.planck.planck.domain.evaluation.SxsEvaluationPairRepository;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.ModelInputRepository;
import com.planck.planck.domain.project.dto.SXSChatRowDTO;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.domain.workbook.dto.SXSWorkbookDTO;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.InputRole;
import com.planck.planck.enums.TagLinkTargetType;
import com.planck.planck.exceptions.IllegalArgumentException;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SxsEvaluationPairServiceImpl implements SxsEvaluationPairService {

  private final SxsEvaluationPairRepository sxsEvaluationPairRepository;
  private final ChatTurnRepository chatTurnRepository;
  private final ChatService chatService;
  private final ModelService modelService;
  private final ChatRepository chatRepository;
  private final ModelInputRepository modelInputRepository;
  private final TagLinkService tagLinkService;
  private final ChatTurnService chatTurnService;
  private final SXSHumanFeedbackRepository sxsHumanFeedbackRepository;
  private final EvaluationContainerRepository evaluationContainerRepository;

  public SxsEvaluationPairServiceImpl(
      SxsEvaluationPairRepository sxsEvaluationPairRepository,
      ChatTurnRepository chatTurnRepository,
      ChatService chatService,
      ModelService modelService,
      ChatRepository chatRepository,
      ModelInputRepository modelInputRepository,
      TagLinkService tagLinkService,
      ChatTurnService chatTurnService,
      SXSHumanFeedbackRepository sxsHumanFeedbackRepository,
      EvaluationContainerRepository evaluationContainerRepository) {
    this.sxsEvaluationPairRepository = sxsEvaluationPairRepository;
    this.chatTurnRepository = chatTurnRepository;
    this.chatService = chatService;
    this.modelService = modelService;
    this.chatRepository = chatRepository;
    this.modelInputRepository = modelInputRepository;
    this.tagLinkService = tagLinkService;
    this.chatTurnService = chatTurnService;
    this.sxsHumanFeedbackRepository = sxsHumanFeedbackRepository;
    this.evaluationContainerRepository = evaluationContainerRepository;
  }

  @Transactional
  @Override
  public SxsEvaluationPair createSxsPair(
      User user, String containerId, Prompt promptInput, String expectedOutput) {
    EvaluationContainer container =
        evaluationContainerRepository
            .findByIdAndUser(containerId, user)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Evaluation Container not found with id: " + containerId));

    if (container.getEvaluationType() != EvaluationType.SXS) {
      throw new NotFoundException("Evaluation container is not an SxS");
    }

    ModelInput modelInput = null;

    if (promptInput != null) {
      modelInput = modelInputRepository.save(new ModelInput(promptInput, user));
    }

    ChatTurn turnA = createInitialTurnForSXS(user, container, modelInput);
    ChatTurn turnB = createInitialTurnForSXS(user, container, modelInput);

    return createAndSaveSxsPair(container, turnA, turnB, expectedOutput, null);
  }

  @Transactional
  public SxsEvaluationPair createSxsPair(
      User user,
      String containerId,
      String chatTurnIdA,
      String chatTurnIdB,
      String expectedOutput,
      Map<String, String> variables) {
    EvaluationContainer container =
        evaluationContainerRepository
            .findByIdAndUser(containerId, user)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Evaluation Container not found with id: " + containerId));

    if (container.getEvaluationType() != EvaluationType.SXS) {
      throw new IllegalInputException("Evaluation pairs can only be created in SxS containers.");
    }

    ChatTurn turnA = chatTurnRepository.findByIdAndUser(chatTurnIdA, user);
    if (turnA == null) {
      throw new NotFoundException("ChatTurn A not found with id: " + chatTurnIdA);
    }

    ChatTurn turnB = null;
    if (chatTurnIdB != null && !chatTurnIdB.isBlank()) {
      turnB = chatTurnRepository.findByIdAndUser(chatTurnIdB, user);
      if (turnB == null) {
        throw new NotFoundException("ChatTurn B not found with id: " + chatTurnIdB);
      }
    }

    return this.createAndSaveSxsPair(container, turnA, turnB, expectedOutput, variables);
  }

  private SxsEvaluationPair createAndSaveSxsPair(
      EvaluationContainer container,
      ChatTurn turnA,
      ChatTurn turnB,
      String expectedOutput,
      Map<String, String> variables) {
    SxsEvaluationPair newPair =
        SxsEvaluationPair.builder()
            .container(container)
            .user(container.getUser())
            .chatA(turnA.getChat())
            .chatTurnA(turnA)
            .chatB(turnB != null ? turnB.getChat() : null)
            .chatTurnB(turnB)
            .expectedOutput(expectedOutput)
            .variables(variables)
            .build();

    return sxsEvaluationPairRepository.save(newPair);
  }

  @Override
  @Transactional(readOnly = true)
  public SxsEvaluationPair getSxsPairById(String pairId, User user) {
    return sxsEvaluationPairRepository
        .findByIdAndUser(pairId, user)
        .orElseThrow(() -> new NotFoundException("SxsEvaluationPair not found with id: " + pairId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<SXSChatRowDTO> getSXSRowsForPair(User user, String containerId, String pairId) {
    SxsEvaluationPair pair = getSxsPairById(pairId, user);
    List<String> chatIds = new ArrayList<>();
    chatIds.add(pair.getChatA().getId());
    if (pair.getChatB() != null) {
      chatIds.add(pair.getChatB().getId());
    }
    List<ChatTurn> chatTurns = chatService.getChatTurnsByChatIdList(chatIds, user);
    List<ChatTurn> sideATurns =
        chatTurns.stream()
            .filter(turn -> turn.getChat().getId().equals(pair.getChatA().getId()))
            .sorted(Comparator.comparing(ChatTurn::getSequenceId))
            .toList();
    List<ChatTurn> sideBTurns =
        chatTurns.stream()
            .filter(
                turn ->
                    pair.getChatB() != null
                        && turn.getChat().getId().equals(pair.getChatB().getId()))
            .sorted(Comparator.comparing(ChatTurn::getSequenceId))
            .toList();
    enrichTurnsWithSystemInstructions(Collections.singletonList(pair));

    List<SXSChatRowDTO> sxsRows = new ArrayList<>();

    int maxTurns = Math.max(sideATurns.size(), sideBTurns.size());

    for (int i = 0; i < maxTurns; i++) {
      ChatTurn sideATurn = i < sideATurns.size() ? sideATurns.get(i) : null;
      ChatTurn sideBTurn = i < sideBTurns.size() ? sideBTurns.get(i) : null;

      SxsEvaluationPair artificialPair =
          SxsEvaluationPair.builder()
              .id(pair.getId())
              .container(pair.getContainer())
              .user(pair.getUser())
              .chatA(pair.getChatA())
              .chatB(pair.getChatB())
              .chatTurnA(sideATurn)
              .chatTurnB(sideBTurn)
              .expectedOutput(pair.getExpectedOutput())
              .variables(pair.getVariables())
              .pairwiseScores(pair.getPairwiseScores())
              .build();

      sxsRows.add(new SXSChatRowDTO(artificialPair, null));
    }

    return sxsRows;
  }

  @Override
  @Transactional
  public void deleteSxsPair(String pairId, User user) {
    SxsEvaluationPair pair = getSxsPairById(pairId, user);
    deletePairsAndAssociatedData(List.of(pair));
  }

  @Override
  @Transactional
  public SxsEvaluationPair generateAndSetTurnB(
      User user,
      String containerId,
      String pairId,
      String modelId,
      boolean createEmptyLastResponse) {

    SxsEvaluationPair pair = getSxsPairById(pairId, user);

    validateGenerateTurnB(containerId, pairId, pair);

    ChatTurn sourceTurnA = pair.getChatTurnA();
    if (sourceTurnA == null) {
      throw new IllegalStateException("Cannot generate Side B because Side A is missing.");
    }
    Model modelForB = modelService.getModelForUser(user, modelId);

    if (pair.getChatTurnB() != null) {
      assignModelToTurnB(pair, modelForB);
    } else {
      createTurnBfromTurnA(createEmptyLastResponse, pair, sourceTurnA, modelForB);
    }

    return sxsEvaluationPairRepository.save(pair);
  }

  @Transactional(readOnly = true)
  public SXSWorkbookDTO getSXSRows(
      User user,
      String containerId,
      int pageSize,
      int pageToken,
      String orderBy,
      String filter,
      List<String> tagIds) {
    EvaluationContainer container =
        evaluationContainerRepository
            .findByIdAndUser(containerId, user)
            .orElseThrow(
                () -> new NotFoundException("Evaluation container not found for this user"));

    int effectivePageSize = Math.min(pageSize < 1 ? 10000 : pageSize, 10000);

    Pageable pageable =
        PageRequest.of(pageToken, effectivePageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<SxsEvaluationPair> sxsPage =
        sxsEvaluationPairRepository.findByContainerWithLatestTurns(container, user, pageable);

    List<SXSChatRowDTO> sxsRows = sxsPage.getContent().stream().map(SXSChatRowDTO::new).toList();

    long totalElements = sxsPage.getTotalElements();
    String nextPageToken = sxsPage.hasNext() ? String.valueOf(sxsPage.getNumber() + 1) : null;

    return new SXSWorkbookDTO(sxsRows, nextPageToken, totalElements);
  }

  private void assignModelToTurnB(SxsEvaluationPair pair, Model modelForB) {
    ChatTurn turnB = pair.getChatTurnB();
    ModelResponse modelResponse =
        turnB.getModelResponse() != null ? turnB.getModelResponse() : new ModelResponse();
    modelResponse.setModel(modelForB);
    modelResponse.setUser(turnB.getUser());
    modelResponse.setContainer(pair.getContainer());
    turnB.setModelResponse(modelResponse);
    chatTurnRepository.save(turnB);
  }

  private void createTurnBfromTurnA(
      boolean createEmptyLastResponse,
      SxsEvaluationPair pair,
      ChatTurn sourceTurnA,
      Model modelForB) {
    List<ChatTurn> newChatBTurns;
    if (createEmptyLastResponse) {
      newChatBTurns = chatService.duplicateChatNewLastModelResponse(sourceTurnA, modelForB);
    } else {
      newChatBTurns = chatService.duplicateChatWithNewModel(sourceTurnA, modelForB);
    }

    if (newChatBTurns.isEmpty()) {
      throw new IllegalStateException("Failed to duplicate chat for Side B.");
    }

    ChatTurn finalTurnB =
        newChatBTurns.stream()
            .max(Comparator.comparing(ChatTurn::getSequenceId))
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Could not find the final turn in the duplicated chat."));

    pair.setChatB(finalTurnB.getChat());
    pair.setChatTurnB(finalTurnB);
  }

  private void validateGenerateTurnB(String containerId, String pairId, SxsEvaluationPair pair) {
    if (!pair.getContainer().getId().equals(containerId)) {
      throw new NotFoundException(
          String.format(
              "SxS Pair with id %s does not belong to container %s", pairId, containerId));
    }

    if (pair.getContainer().getEvaluationType() != EvaluationType.SXS) {
      throw new IllegalInputException("Evaluation pairs can only be created in SxS containers.");
    }

    if (pair.getChatTurnB() != null
        && pair.getChatTurnB().getModelResponse() != null
        && pair.getChatTurnB().getModelResponse().getText() != null) {
      throw new IllegalStateException("Side B already has a model with response");
    }
  }

  @Transactional
  @Override
  public SxsEvaluationPair updateSxsPair(
      String pairId,
      String containerId,
      User user,
      String input,
      String expectedOutput,
      Map<String, String> variables,
      List<String> tagIds) {

    SxsEvaluationPair pair = getSxsPairById(pairId, user);
    if (!Objects.equals(pair.getContainer().getId(), containerId)) {
      throw new NotFoundException("SxsEvaluationPair not found with id: " + pairId);
    }
    ChatTurn turnA = pair.getChatTurnA();
    ChatTurn turnB = pair.getChatTurnB();

    if (input != null) {
      validateNoResponseExists(turnA, turnB);
      updateInputForTurn(turnA, input);
      if (turnB != null) {
        updateInputForTurn(turnB, input);
      }
    }

    if (expectedOutput != null) {
      pair.setExpectedOutput(expectedOutput);
      updateExpectedOutputForTurn(turnA, expectedOutput);
      if (turnB != null) {
        updateExpectedOutputForTurn(turnB, expectedOutput);
      }
    }

    if (variables != null) {
      pair.setVariables(variables);
      updateMetadataForChat(turnA.getChat(), variables);
      if (turnB != null) {
        updateMetadataForChat(turnB.getChat(), variables);
      }
    }

    if (tagIds != null) {
      tagLinkService.updateTagsForEntity(
          user, tagIds, TagLinkTargetType.CHAT, pair.getChatA().getId());
      if (turnB != null) {
        tagLinkService.updateTagsForEntity(
            user, tagIds, TagLinkTargetType.CHAT, pair.getChatB().getId());
      }
    }

    return sxsEvaluationPairRepository.save(pair);
  }

  private void validateNoResponseExists(ChatTurn turnA, ChatTurn turnB) {
    if (responseExists(turnA) || responseExists(turnB)) {
      var errorMessage =
          (turnB == null)
              ? "Cannot update input: a model response already exists for Side A."
              : "Cannot update input: a model response already exists for Side A or Side B.";
      throw new IllegalArgumentException(errorMessage);
    }
  }

  private boolean responseExists(ChatTurn turn) {
    return Optional.ofNullable(turn)
        .map(ChatTurn::getModelResponse)
        .map(ModelResponse::getText)
        .filter(text -> !text.isBlank())
        .isPresent();
  }

  private void updateInputForTurn(ChatTurn turn, String newInput) {
    ModelInput userInput;
    if (turn.getInputs() != null && !turn.getInputs().isEmpty()) {
      userInput = turn.getLastUserInput();
      userInput.setText(newInput);
    } else {
      Prompt prompt = new Prompt(InputRole.USER, newInput);
      userInput = new ModelInput(prompt, turn.getUser());
      turn.getInputs().add(userInput);
    }
    modelInputRepository.save(userInput);
  }

  private void updateExpectedOutputForTurn(ChatTurn turn, String newExpectedOutput) {
    ModelInput userInput = turn.getLastUserInput();
    userInput.setExpectedOutput(newExpectedOutput);
    modelInputRepository.save(userInput);
  }

  private void updateMetadataForChat(Chat chat, Map<String, String> newVariables) {
    chat.setVariables(newVariables);
    chatRepository.save(chat);
  }

  private ChatTurn createInitialTurnForSXS(
      User user, EvaluationContainer container, ModelInput modelInput) {
    Chat chat = new Chat();
    chat.setContainer(container);
    chat.setUser(user);
    ChatTurn chatTurn = new ChatTurn(user, chat, 0, modelInput, null);
    chat.addTurn(chatTurn);
    chatRepository.save(chat);
    return chatTurn;
  }

  @Transactional
  @Override
  public void deleteSxsPairsByIds(List<String> pairIds, User user) {
    if (pairIds == null || pairIds.isEmpty()) {
      return;
    }
    List<SxsEvaluationPair> pairsToDelete =
        sxsEvaluationPairRepository.findByIdsAndUser(pairIds, user);
    deletePairsAndAssociatedData(pairsToDelete);
  }

  @Transactional
  @Override
  public void deleteSxsPairsByContainer(String containerId, User user) {
    EvaluationContainer container =
        evaluationContainerRepository
            .findByIdAndUser(containerId, user)
            .orElseThrow(
                () -> new NotFoundException("Evaluation container not found for this user"));
    List<SxsEvaluationPair> pairsToDelete = sxsEvaluationPairRepository.findByContainer(container);
    deletePairsAndAssociatedData(pairsToDelete);
  }

  private void deletePairsAndAssociatedData(List<SxsEvaluationPair> pairs) {
    if (pairs == null || pairs.isEmpty()) {
      return;
    }

    List<Chat> chatsToDeleteTurnsFrom =
        pairs.stream()
            .flatMap(pair -> Stream.of(pair.getChatA(), pair.getChatB()))
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

    if (!chatsToDeleteTurnsFrom.isEmpty()) {
      List<ChatTurn> turnsToDelete = chatTurnService.findByChatIn(chatsToDeleteTurnsFrom);

      chatService.delete(turnsToDelete);
      sxsHumanFeedbackRepository.deleteByPairIdList(
          pairs.stream().map(SxsEvaluationPair::getId).collect(Collectors.toList()));
    }
  }

  public SxsEvaluationPair synchronizeSxsPairChats(
      SxsEvaluationPair pair, User user, @Nullable String modelIdA, @Nullable String modelIdB) {
    List<ChatTurn> turnsA = chatService.getFullChatHistory(pair.getChatTurnA());
    List<ChatTurn> turnsB = chatService.getFullChatHistory(pair.getChatTurnB());

    synchronizeCommonTurns(turnsA, turnsB, user, modelIdA, modelIdB);

    turnsA = chatService.getFullChatHistory(pair.getChatTurnA());
    turnsB = chatService.getFullChatHistory(pair.getChatTurnB());

    if (turnsA.size() > turnsB.size()) {
      ChatTurn newLastTurnB =
          chatService.appendMissingTurns(
              pair.getChatB(), turnsA, pair.getContainer(), turnsB.size());
      pair.setChatTurnB(newLastTurnB);
    } else if (turnsB.size() > turnsA.size()) {
      ChatTurn newLastTurnA =
          chatService.appendMissingTurns(
              pair.getChatA(), turnsB, pair.getContainer(), turnsA.size());
      pair.setChatTurnA(newLastTurnA);
    }

    return pair;
  }

  private void synchronizeCommonTurns(
      List<ChatTurn> turnsA,
      List<ChatTurn> turnsB,
      User user,
      @Nullable String modelIdA,
      @Nullable String modelIdB) {
    int commonSize = Math.min(turnsA.size(), turnsB.size());

    for (int i = 0; i < commonSize; i++) {
      ChatTurn turnA = turnsA.get(i);
      ChatTurn turnB = turnsB.get(i);

      boolean isLastCommonTurn = (i == commonSize - 1);
      boolean responseMissingA = isResponseMissing(turnA);
      boolean responseMissingB = isResponseMissing(turnB);

      if (responseMissingB && !responseMissingA) {
        Model model = resolveModelForSide(user, turnsB, isLastCommonTurn ? modelIdB : null);
        chatService.copyResponseToTurn(turnA, turnB, model);
      } else if (responseMissingA && !responseMissingB) {
        Model model = resolveModelForSide(user, turnsA, isLastCommonTurn ? modelIdA : null);
        chatService.copyResponseToTurn(turnB, turnA, model);
      }
    }
  }

  private Model resolveModelForSide(
      User user, List<ChatTurn> turns, @Nullable String modelIdFromRequest) {
    if (modelIdFromRequest != null) {
      return modelService.getModelForUser(user, modelIdFromRequest);
    }
    return chatService
        .getModelFromChatHistory(turns)
        .orElseThrow(() -> new IllegalStateException("Cannot determine model for the side."));
  }

  private boolean isResponseMissing(ChatTurn turn) {
    return turn.getModelResponse() == null || turn.getModelResponse().getText() == null;
  }

  @Override
  public void createSxsEvaluationPairs(
      User user, List<Chat> targetChats, EvaluationContainer container) {
    if (container.getEvaluationType() != EvaluationType.SXS) {
      return;
    }

    Map<String, Chat> sideB = chatService.duplicateChatsForSideB(targetChats, user);

    List<SxsEvaluationPair> sxsEvaluationPairs = new ArrayList<>();
    for (Chat chat : targetChats) {
      Chat sideBChat = sideB.get(chat.getId());
      for (ChatTurn chatTurn : chat.getTurns()) {
        SxsEvaluationPair sxsEvaluationPair = new SxsEvaluationPair();
        sxsEvaluationPair.setChatA(chat);
        sxsEvaluationPair.setChatTurnA(chatTurn);
        sxsEvaluationPair.setChatB(sideBChat);
        ChatTurn sideBChatTurn =
            sideBChat.getTurns().stream()
                .filter(t -> t.getSequenceId() == chatTurn.getSequenceId())
                .findFirst()
                .orElse(null);
        sxsEvaluationPair.setChatTurnB(sideBChatTurn);
        sxsEvaluationPair.setContainer(container);
        sxsEvaluationPair.setUser(user);
        sxsEvaluationPairs.add(sxsEvaluationPair);
      }
    }
    sxsEvaluationPairRepository.saveAll(sxsEvaluationPairs);
  }

  private void enrichTurnsWithSystemInstructions(List<SxsEvaluationPair> sxsPairs) {
    if (sxsPairs == null || sxsPairs.isEmpty()) {
      return;
    }

    List<ChatTurn> allTurns =
        sxsPairs.stream()
            .flatMap(pair -> Stream.of(pair.getChatTurnA(), pair.getChatTurnB()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    chatService.enrichTurnsWithSystemInstructions(allTurns);
  }
}

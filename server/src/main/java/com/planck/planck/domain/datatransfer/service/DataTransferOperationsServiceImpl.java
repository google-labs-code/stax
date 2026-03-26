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

package com.planck.planck.domain.datatransfer.service;

import com.planck.planck.domain.chat.ChatRepository;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.chatturn.service.ChatTurnCopyService;
import com.planck.planck.domain.datatransfer.dto.CopyChatErrorDetailDTO;
import com.planck.planck.domain.datatransfer.dto.CopyChatsResponseDTO;
import com.planck.planck.domain.datatransfer.dto.MoveChatsResponseDTO;
import com.planck.planck.domain.project.EvaluationContainerRepository;
import com.planck.planck.domain.project.SxsEvaluationPairService;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.TagLinkTargetType;
import com.planck.planck.exceptions.NotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DataTransferOperationsServiceImpl implements DataTransferOperationsService {

  private final ChatTurnRepository chatTurnRepository;

  private final ChatTurnCopyService chatTurnCopyService;

  private final TagLinkService tagLinkService;
  private final ChatRepository chatRepository;
  private final EvaluationContainerRepository evaluationContainerRepository;
  private final SxsEvaluationPairService sxsEvaluationPairService;

  public DataTransferOperationsServiceImpl(
      ChatTurnRepository chatTurnRepository,
      ChatTurnCopyService chatTurnCopyService,
      TagLinkService tagLinkService,
      ChatRepository chatRepository,
      EvaluationContainerRepository evaluationContainerRepository,
      SxsEvaluationPairService sxsEvaluationPairService) {
    this.chatTurnRepository = chatTurnRepository;
    this.chatTurnCopyService = chatTurnCopyService;
    this.tagLinkService = tagLinkService;
    this.chatRepository = chatRepository;
    this.evaluationContainerRepository = evaluationContainerRepository;
    this.sxsEvaluationPairService = sxsEvaluationPairService;
  }

  @Transactional
  @Override
  public CopyChatsResponseDTO copyAllChatsFromProjectToProject(
      String sourceId, String targetId, User user) {
    log.info("User requested full chat copy: sourceId={}, targetId={}", sourceId, targetId);
    return copyChatsInternal(sourceId, targetId, null, user);
  }

  @Transactional
  @Override
  public CopyChatsResponseDTO copySelectedChatsFromProjectToProject(
      String sourceId, String targetId, List<String> chatIds, User user) {
    log.info(
        "User requested selective chat copy: sourceId={}, targetId={}, numChatIds={}",
        sourceId,
        targetId,
        chatIds != null ? chatIds.size() : 0);
    return copyChatsInternal(sourceId, targetId, chatIds, user);
  }

  @Override
  @Transactional
  public MoveChatsResponseDTO moveChatsFromSourceToTarget(
      String sourceId, String targetId, List<String> chatIds, User user) {

    if (chatIds == null || chatIds.isEmpty()) {
      return new MoveChatsResponseDTO(
          "No chat IDs provided to move.", 0, 0, Collections.emptyList());
    }

    EvaluationContainer targetContainer =
        evaluationContainerRepository
            .findById(targetId)
            .orElseThrow(() -> new NotFoundException("Target container not found: " + targetId));

    if (!targetContainer.getUser().getId().equals(user.getId())) {
      throw new SecurityException("User does not have access to the target container.");
    }

    List<Chat> chatsToBeMoved = chatRepository.findByUserAndIdIn(user, chatIds);
    List<CopyChatErrorDetailDTO> errors = new ArrayList<>();
    List<Chat> validChatsToMove = new ArrayList<>();
    int successfullyMovedTurns = 0;

    for (String requestedChatId : chatIds) {
      Optional<Chat> foundChat =
          chatsToBeMoved.stream().filter(c -> c.getId().equals(requestedChatId)).findFirst();

      if (foundChat.isPresent()) {
        Chat chat = foundChat.get();
        if (chat.getContainer().getId().equals(sourceId)) {
          validChatsToMove.add(chat);
          successfullyMovedTurns += chat.getTurns().size();
        } else {
          errors.add(
              new CopyChatErrorDetailDTO(
                  requestedChatId, "MOVE_FAILED: Chat does not belong to the source container."));
        }
      } else {
        errors.add(
            new CopyChatErrorDetailDTO(
                requestedChatId, "MOVE_FAILED: Chat not found or does not belong to the user."));
      }
    }

    for (Chat chat : validChatsToMove) {
      chat.setContainer(targetContainer);
    }

    if (!validChatsToMove.isEmpty()) {
      chatRepository.saveAll(validChatsToMove);
    }

    int successfullyMovedChats = validChatsToMove.size();
    int failedChats = errors.size();
    String message =
        String.format(
            "Move operation processed. %d of %d chats moved (total %d turns).",
            successfullyMovedChats, chatIds.size(), successfullyMovedTurns);

    return new MoveChatsResponseDTO(message, successfullyMovedChats, failedChats, errors);
  }

  private CopyChatsResponseDTO copyChatsInternal(
      String sourceId, String targetId, List<String> chatIds, User user) {

    EvaluationContainer sourceContainer =
        evaluationContainerRepository
            .findById(sourceId)
            .orElseThrow(() -> new NotFoundException("Source container not found: " + sourceId));

    EvaluationContainer targetContainer =
        evaluationContainerRepository
            .findById(targetId)
            .orElseThrow(() -> new NotFoundException("Target container not found: " + targetId));

    if (!sourceContainer.getUser().getId().equals(user.getId())
        || !targetContainer.getUser().getId().equals(user.getId())) {
      throw new SecurityException("User does not have access to one of the containers.");
    }

    List<String> chatIdsToCopy;
    if (chatIds != null && !chatIds.isEmpty()) {
      chatIdsToCopy = new ArrayList<>(new HashSet<>(chatIds));
    } else {
      chatIdsToCopy = chatTurnRepository.findAllChatIdsByUserAndContainer(user, sourceContainer);
    }

    if (chatIdsToCopy.isEmpty()) {
      return new CopyChatsResponseDTO(
          "Source container has no chats to copy.", 0, 0, new HashMap<>(), new ArrayList<>());
    }

    log.info(
        "Copying {} chat(s) from container {} to {}", chatIdsToCopy.size(), sourceId, targetId);

    return doCopyChatsInBatch(chatIdsToCopy, targetContainer, user);
  }

  private CopyChatsResponseDTO doCopyChatsInBatch(
      List<String> sourceChatIdsToCopy, EvaluationContainer targetContainer, User user) {

    if (sourceChatIdsToCopy == null || sourceChatIdsToCopy.isEmpty()) {
      return new CopyChatsResponseDTO(
          "No chats selected to copy.", 0, 0, new HashMap<>(), new ArrayList<>());
    }

    List<ChatTurn> allSourceTurns = chatTurnRepository.findForCopy(sourceChatIdsToCopy);
    if (allSourceTurns.isEmpty()) {
      return new CopyChatsResponseDTO(
          "No source content found to copy.", 0, 0, new HashMap<>(), new ArrayList<>());
    }

    List<String> sourceTurnIds =
        allSourceTurns.stream().map(ChatTurn::getId).collect(Collectors.toList());
    Map<String, List<TagLink>> tagsByTurnId =
        tagLinkService
            .getTagLinksByEntities(TagLinkTargetType.CHAT_TURN, sourceTurnIds, user)
            .stream()
            .collect(Collectors.groupingBy(TagLink::getTargetId));
    Map<String, List<ChatTurn>> sourceTurnsByChatId =
        allSourceTurns.stream().collect(Collectors.groupingBy(turn -> turn.getChat().getId()));

    List<CopyChatErrorDetailDTO> errors = new ArrayList<>();
    Map<String, String> copiedChatIdMappings = new HashMap<>();
    List<Chat> newChatsToSave = new ArrayList<>();
    Map<ChatTurn, ChatTurn> sourceToTargetTurnMap = new HashMap<>();

    for (String sourceChatId : sourceChatIdsToCopy) {
      List<ChatTurn> sourceTurnsForThisChat = sourceTurnsByChatId.get(sourceChatId);

      if (sourceTurnsForThisChat == null || sourceTurnsForThisChat.isEmpty()) {
        errors.add(new CopyChatErrorDetailDTO(sourceChatId, "SOURCE_CHAT_ID_NOT_FOUND_OR_EMPTY"));
        continue;
      }

      Chat sourceChat = sourceTurnsForThisChat.get(0).getChat();
      Chat newChat = new Chat();
      newChat.setUser(user);
      newChat.setContainer(targetContainer);
      newChat.setVariables(sourceChat.getVariables());

      List<ChatTurn> newTurnsForThisChat = new ArrayList<>();
      for (ChatTurn sourceTurn : sourceTurnsForThisChat) {
        ChatTurn newTurn = new ChatTurn();
        newTurn.setChat(newChat);
        newTurn.copyFromExisting(sourceTurn);
        newTurnsForThisChat.add(newTurn);
        sourceToTargetTurnMap.put(sourceTurn, newTurn);
      }
      newChat.setTurns(newTurnsForThisChat);
      newChatsToSave.add(newChat);
    }

    try {
      chatRepository.saveAll(newChatsToSave);

      for (Chat newSavedChat : newChatsToSave) {
        if (newSavedChat.getTurns() == null || newSavedChat.getTurns().isEmpty()) continue;

        ChatTurn firstNewTurn = newSavedChat.getTurns().get(0);
        ChatTurn originalSourceTurn =
            sourceToTargetTurnMap.entrySet().stream()
                .filter(entry -> entry.getValue() == firstNewTurn)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (originalSourceTurn != null) {
          String originalChatId = originalSourceTurn.getChat().getId();
          copiedChatIdMappings.put(originalChatId, newSavedChat.getId());
        }
      }

      chatTurnCopyService.copyAllChatTurnRelatedEntities(sourceToTargetTurnMap, user, tagsByTurnId);

      if (targetContainer.getEvaluationType() == EvaluationType.SXS) {
        sxsEvaluationPairService.createSxsEvaluationPairs(user, newChatsToSave, targetContainer);
      }

    } catch (Exception e) {
      log.error(
          "Failed during batch copy to container '{}'. Error: {}",
          targetContainer.getId(),
          e.getMessage(),
          e);
      return new CopyChatsResponseDTO(
          "Batch copy failed with internal error: " + e.getMessage(),
          0,
          sourceChatIdsToCopy.size(),
          new HashMap<>(),
          errors);
    }

    int successfullyCopiedChats = newChatsToSave.size();
    String message =
        String.format(
            "Copy operation processed. %d of %d chats copied (total %d turns).",
            successfullyCopiedChats, sourceChatIdsToCopy.size(), allSourceTurns.size());

    if (!errors.isEmpty()) {
      message += " Some errors occurred. Check error details.";
    }

    return new CopyChatsResponseDTO(
        message, successfullyCopiedChats, errors.size(), copiedChatIdMappings, errors);
  }
}

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

import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.dataset.dto.ChatTurnGroupCreateRequest;
import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.User;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface ChatService {

  List<ChatTurn> getChatDTO(String chatId, User user, List<String> tagIds);

  List<ChatTurn> getChat(String chatId, User user);

  List<ChatTurn> getChat(String chatId, User user, List<String> tagIds);

  List<ChatTurn> getChatUntilTurn(String chatId, String turnId, User user);

  ChatTurn createChatTurn(
      User user,
      String containerId,
      @Nullable String chatId,
      String modelPrompt,
      String modelResponseText,
      String modelId);

  ChatTurn createChatTurn(
      User user,
      String containerId,
      @Nullable String chatId,
      @Nullable Prompt prompt,
      @Nullable String modelResponseText,
      String modelId);

  ChatTurn updateTurn(
      User user,
      String containerId,
      String chatTurnId,
      String modelPrompt,
      String modelResponse,
      String modelId,
      String expectedOutput);

  ChatTurn updateTurn(
      User user,
      String containerId,
      String chatTurnId,
      Prompt modelPrompt,
      @Nullable String modelResponse,
      @Nullable String modelId,
      @Nullable String expectedOutput);

  ChatTurn saveChatTurn(ChatTurn chatTurn, User user);

  List<ChatTurn> saveChat(List<ChatTurn> chatTurns);

  ChatTurn getChatTurn(String chatTurnId, User user);

  ChatTurnDTO getChatTurnDTO(String chatTurnId, User user);

  List<ChatTurn> duplicateChat(ChatTurn existingChatTurn);

  List<ChatTurn> duplicateChatNewLastModelResponse(ChatTurn existingChatTurn, Model model);

  List<ChatTurn> duplicateChatWithNewModel(ChatTurn existingChatTurn, Model newModel);

  Page<ChatTurn> fetchLastTurns(
      User user, EvaluationContainer container, List<String> tagIds, Pageable pageable);

  List<ChatTurn> getChatTurnsByList(List<String> chatTurnIds, User user);

  List<ChatTurn> getChatTurnsByChatIdList(List<String> chatIds, User user);

  @Transactional
  void deleteChats(User user, String containerId);

  void deleteChats(List<String> chatIds, User user, String projectId);

  Integer findMaxSequenceIdByUserAndContainerAndChatId(
      User user, String chatId, EvaluationContainer evaluationContainer);

  List<ChatTurn> findAllByUserAndChatContainer(User user, EvaluationContainer container);

  void delete(ChatTurn chatTurn);

  void delete(User user, String chatTurnId);

  void delete(User user, List<String> chatTurnIds);

  void delete(List<ChatTurn> chatTurns);

  List<String> findLatestChatTurnIdsByProjectId(String projectId, User user);

  boolean existsByModelResponse(ModelResponse modelResponse);

  void deleteTurnResultsByProject(User user, String projectId);

  void deleteTurnResultsByTurnIds(User user, String projectId, List<String> chatTurnIds);

  void deleteTurnResults(User user, String projectId, List<ChatTurn> chatTurnList);

  List<String> findIdsWithNonEmptyOutput(List<String> chatTurnIds);

  Chat createChat(User user, EvaluationContainer container);

  Chat createChat(String id, EvaluationContainer container, User user);

  Chat createChat(
      String id, EvaluationContainer container, User user, Map<String, String> variables);

  Chat getChatById(String chatId, User user);

  Optional<Chat> findByChatId(String chatId, User user);

  List<Chat> getAllChatsByUser(User user);

  void deleteChat(String chatId, User user);

  void addChatTurn(Chat chat, ChatTurn chatTurn);

  List<com.planck.planck.enums.ModelProvider> getDistinctModelProvidersByProject(
      User user, String projectId);

  Chat replaceVariables(String chatId, User user, Map<String, String> newVariables);

  Chat addOrUpdateVariables(String chatId, User user, Map<String, String> variablesUpdates);

  Chat removeVariablesKey(String chatId, User user, String key);

  Map<String, String> getVariables(String chatId, User user);

  List<String> getVariablesKeys(String chatId, User user);

  Map<String, Chat> duplicateChatsForSideB(List<Chat> chats, User user);

  void enrichTurnsWithSystemInstructions(List<ChatTurn> turns);

  Chat saveChat(Chat chat);

  List<ChatTurn> getFullChatHistory(ChatTurn anyTurnInChat);

  Optional<Model> getModelFromChatHistory(List<ChatTurn> chatHistory);

  ChatTurn appendMissingTurns(
      Chat destinationChat,
      List<ChatTurn> sourceTurns,
      EvaluationContainer container,
      int startIndex);

  void copyResponseToTurn(ChatTurn sourceTurn, ChatTurn destinationTurn, Model modelForDestination);

  List<Chat> findChatsByUserAndIds(Collection<String> ids, User user);

  List<String> getAllChatIds(User user, EvaluationContainer container);

  List<ChatTurnDTO> addTurnsInBulk(
      User user, String containerId, List<ChatTurnGroupCreateRequest> bulkRequestList);

  List<Chat> getAllChatsByContainer(EvaluationContainer container);

  void deleteChats(List<Chat> chats);
}

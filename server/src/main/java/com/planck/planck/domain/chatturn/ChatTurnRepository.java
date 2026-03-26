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

package com.planck.planck.domain.chatturn;

import com.planck.planck.entitities.Chat;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.InputUsageCountDTO;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatTurnRepository extends JpaRepository<ChatTurn, String> {

  @Query(
      """
              SELECT ct FROM ChatTurn ct
              WHERE ct.chat.id = :chatId
                  AND ct.user = :user
                  AND (
                  :tagIds IS NULL  OR :tagCount = 0 OR
                  (SELECT COUNT(DISTINCT tl.tag.tagId) FROM TagLink tl
                      WHERE tl.targetId = ct.id
                      AND tl.targetType = 'CHAT_TURN'
                      AND tl.tag.tagId IN :tagIds
                  ) >= :tagCount
                  )
              ORDER BY ct.sequenceId ASC
          """)
  List<ChatTurn> findByChatIdAndUserIdOrderBySequenceId(
      @Param("chatId") String chatId,
      @Param("user") User user,
      @Param("tagIds") List<String> tagIds,
      @Param("tagCount") Integer tagCount);

  List<ChatTurn> findByChatIn(List<Chat> chats);

  @Query("SELECT ct FROM ChatTurn ct WHERE ct.id = :chatTurnId AND ct.user = :user")
  ChatTurn findByIdAndUser(@Param("chatTurnId") String chatTurnid, User user);

  @Query(
      "SELECT ct FROM ChatTurn ct WHERE ct.chat.id = :chatId AND ct.chat.container = :container AND ct.user ="
          + " :user ORDER BY ct.sequenceId ASC")
  List<ChatTurn> findByChatIdAndContainerAndUserIdOrderBySequenceId(
      @Param("chatId") String chatId,
      @Param("container") EvaluationContainer container,
      @Param("user") User user);

  @EntityGraph(
      attributePaths = {"modelResponse.humanEvalScores.humanEvaluator"},
      type = EntityGraph.EntityGraphType.LOAD)
  @Query("SELECT ct FROM ChatTurn ct WHERE ct.id = :id AND ct.user = :user")
  ChatTurn findByChatTurnIdAndUserId(@Param("id") String id, @Param("user") User user);

  @Query(
      "SELECT ct FROM ChatTurn ct WHERE ct.chat.id = :chatId AND ct.user = :user and ct.sequenceId"
          + " <= :sequenceId ORDER BY ct.sequenceId ASC")
  List<ChatTurn> findChatTillSequence(String chatId, Integer sequenceId, User user);

  @Query(
      """
        SELECT ct FROM ChatTurn ct
        WHERE ct.user = :user
          AND ct.chat.container = :container
          AND (ct.chat.id, ct.sequenceId) IN (
            SELECT cta.chat.id, MAX(cta.sequenceId)
            FROM ChatTurn cta
            WHERE cta.user = :user
              AND cta.chat.container = :container
            GROUP BY cta.chat.id
          )
          AND (
            :tagIds IS NULL OR :tagCount = 0 OR
            (SELECT COUNT(DISTINCT tl.tag.tagId) FROM TagLink tl
             WHERE tl.targetId = ct.id
               AND tl.targetType = 'CHAT_TURN'
               AND tl.tag.tagId IN :tagIds
            ) >= :tagCount
          )
    """)
  Page<ChatTurn> findLatestChatTurnsByUser(
      @Param("user") User user,
      @Param("container") EvaluationContainer container,
      @Param("tagIds") List<String> tagIds,
      @Param("tagCount") Integer tagCount,
      Pageable pageable);

  @Query("SELECT ct FROM ChatTurn ct WHERE ct.id IN :chatTurnIds AND ct.user = :user")
  List<ChatTurn> findChatTurnsByList(List<String> chatTurnIds, User user);

  @Query("SELECT ct FROM ChatTurn ct WHERE ct.chat.id IN :chatIds AND ct.user = :user")
  List<ChatTurn> findChatTurnsByChatList(List<String> chatIds, User user);

  @Query(
      "SELECT COUNT(DISTINCT ct.chat.id) FROM ChatTurn ct WHERE ct.chat.container = :container AND ct.user = :user")
  long countDistinctChatIdsByContainer(
      @Param("container") EvaluationContainer container, @Param("user") User user);

  @Query("SELECT ct FROM ChatTurn ct WHERE ct.user = :user AND ct.chat.container = :container")
  List<ChatTurn> findAllByUserAndChatContainer(
      @Param("user") User user, @Param("container") EvaluationContainer container);

  @Query(
      "SELECT ct FROM ChatTurn ct WHERE ct.user = :user AND ct.chat.container = :container AND ct.id IN :ids")
  List<ChatTurn> findAllByUserAndContainerAndIdIn(
      @Param("user") User user,
      @Param("container") EvaluationContainer container,
      @Param("ids") List<String> ids);

  @Query(
      "SELECT ct FROM ChatTurn ct WHERE ct.user = :user AND ct.chat.container = :container AND ct.id = :id")
  Optional<ChatTurn> findChatTurnByIdAndContainerAndUser(
      @Param("id") String id,
      @Param("container") EvaluationContainer container,
      @Param("user") User user);

  @Query(
      "SELECT MAX(ct.sequenceId) FROM ChatTurn ct WHERE ct.user = :user AND ct.chat.id = :chatId AND"
          + " ct.chat.container = :container")
  Integer findMaxSequenceIdByUserAndContainerAndChatId(
      @Param("user") User user,
      @Param("container") EvaluationContainer container,
      @Param("chatId") String chatId);

  @Query(
      "SELECT DISTINCT ct.chat.id FROM ChatTurn ct WHERE ct.user = :user AND ct.chat.container = :container")
  List<String> findAllChatIdsByUserAndContainer(
      @Param("user") User user, @Param("container") EvaluationContainer container);

  @Query(
      "SELECT ct FROM ChatTurn ct WHERE ct.user = :user AND ct.chat.container = :container AND ct.chat.id ="
          + " :chatId")
  List<ChatTurn> findAllByUserAndContainerAndChatId(
      @Param("user") User user,
      @Param("container") EvaluationContainer container,
      @Param("chatId") String chatId);

  @Query(
      "SELECT ct FROM ChatTurn ct WHERE ct.user = :user AND ct.chat.container = :container AND ct.chat.id IN"
          + " :chatIds")
  List<ChatTurn> findAllByUserAndContainerAndChatIdIn(
      @Param("user") User user,
      @Param("container") EvaluationContainer container,
      @Param("chatIds") List<String> chatIds);

  boolean existsByModelResponse(ModelResponse modelResponse);

  @Query("SELECT COUNT(ct) FROM ChatTurn ct JOIN ct.inputs input WHERE input = :input")
  long countChatTurnsUsingInput(@Param("input") ModelInput input);

  @Query(
      """
      SELECT new com.planck.planck.entitities.InputUsageCountDTO(input.id, COUNT(ct))
      FROM ChatTurn ct
      JOIN ct.inputs input
      WHERE input IN :inputs
      GROUP BY input.id
    """)
  List<InputUsageCountDTO> countUsagesForInputs(@Param("inputs") Set<ModelInput> inputs);

  @Query(
      """
              select ct.id from ChatTurn ct where (ct.chat.id, ct.sequenceId) IN
                  (select cta.chat.id, MAX(cta.sequenceId) from ChatTurn cta
                      where cta.chat.container.id = :containerId and cta.user = :user group by cta.chat.id)
              """)
  List<String> findLatestChatTurnIdsByContainerId(
      @Param("containerId") String containerId, @Param("user") User user);

  @Query(
      "select ct.id from ChatTurn ct where ct.chat.container.id = :containerId and ct.user = :user")
  List<String> findChatTurnIdsByContainerId(
      @Param("containerId") String containerId, @Param("user") User user);

  @Query("select COUNT(ct.id) from ChatTurn ct where ct.chat.container = :container")
  int countByContainer(@Param("container") EvaluationContainer container);

  @Query(
      """
        SELECT ct.id FROM ChatTurn ct
        JOIN ct.modelResponse mr
        WHERE ct.id IN :ids AND mr.text IS NOT NULL AND TRIM(mr.text) <> ''
        """)
  List<String> findIdsWithNonEmptyModelResponse(@Param("ids") List<String> chatTurnIds);

  int countByChat(Chat chat);

  @Query(
      """
            SELECT ct
            FROM ChatTurn ct
            JOIN ct.inputs inp
            WHERE ct.chat.id IN :chatIds AND inp.role = 2
            ORDER BY ct.createdAt ASC
        """)
  List<ChatTurn> findSystemInstructionTurnsInChats(@Param("chatIds") List<String> chatIds);

  @Query(
      """
          SELECT DISTINCT m.provider
          FROM ChatTurn ct
          JOIN ct.modelResponse mr
          JOIN mr.model m
          WHERE ct.chat.container.id = :containerId AND ct.user = :user
          """)
  List<com.planck.planck.enums.ModelProvider> findDistinctModelProvidersByContainerId(
      @Param("containerId") String containerId, @Param("user") User user);

  @Query(
      "SELECT DISTINCT ct FROM ChatTurn ct "
          + "LEFT JOIN FETCH ct.chat c "
          + "LEFT JOIN FETCH ct.modelResponse mr "
          + "LEFT JOIN FETCH ct.inputs "
          + "WHERE c.id IN :chatIds")
  List<ChatTurn> findForCopy(@Param("chatIds") List<String> chatIds);

  @Query(
      """
                  SELECT ct FROM ChatTurn ct
                  WHERE ct.chat.id = :chatId
                      AND ct.user = :user
                      AND ct.sequenceId <= :maxSequenceId
                  ORDER BY ct.sequenceId ASC
              """)
  List<ChatTurn> findByChatIdAndUserIdUntilSequenceIdOrderBySequenceId(
      @Param("chatId") String chatId,
      @Param("user") User user,
      @Param("maxSequenceId") Integer maxSequenceId);
}

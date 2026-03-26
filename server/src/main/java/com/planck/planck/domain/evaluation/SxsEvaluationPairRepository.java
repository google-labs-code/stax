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

package com.planck.planck.domain.evaluation;

import com.planck.planck.domain.evaluation.dto.SXSPairEvaluationRequest;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SxsEvaluationPairRepository extends JpaRepository<SxsEvaluationPair, String> {

  @Query("SELECT p FROM SxsEvaluationPair p WHERE p.id = :id AND p.user = :user")
  Optional<SxsEvaluationPair> findByIdAndUser(String id, User user);

  @Query("SELECT p FROM SxsEvaluationPair p WHERE p.id IN :id AND p.user = :user")
  List<SxsEvaluationPair> findByIdsAndUser(List<String> id, User user);

  Page<SxsEvaluationPair> findByContainer(EvaluationContainer container, Pageable pageable);

  List<SxsEvaluationPair> findByContainer(EvaluationContainer container);

  @Query("SELECT p.id FROM SxsEvaluationPair p WHERE p.container = :container")
  List<String> findIdsByContainer(EvaluationContainer container);

  @Query("SELECT p FROM SxsEvaluationPair p WHERE p.id IN :ids")
  @EntityGraph(attributePaths = {"chatA", "chatA.turns", "chatTurnA.modelResponse"})
  List<SxsEvaluationPair> findByIdInWithChatAEagerFetching(List<String> ids);

  List<SxsEvaluationPair> findAllByIdInAndContainer(
      List<String> ids, EvaluationContainer container);

  /**
   * Find SxS evaluation pairs for a project with their latest chat turns. This query is optimized
   * for better performance with large datasets.
   *
   * <p>Note: This requires a composite index on chat_turns(chat_id, sequence_id) for optimal
   * performance. The index is created via migration: IDX_chat_turns_chat_sequence
   */
  @Query(
      """
          SELECT DISTINCT p FROM SxsEvaluationPair p
          LEFT JOIN FETCH p.chatTurnA cta
          LEFT JOIN FETCH p.chatTurnB ctb
          LEFT JOIN FETCH cta.chat ca
          LEFT JOIN FETCH ctb.chat cb
          WHERE p.container = :container
          AND p.user = :user
          AND cta.sequenceId = (
              SELECT MAX(ct.sequenceId)
              FROM ChatTurn ct
              WHERE ct.chat.id = cta.chat.id
          )
          AND (ctb.id IS NULL OR ctb.sequenceId = (
              SELECT MAX(ct.sequenceId)
              FROM ChatTurn ct
              WHERE ct.chat.id = ctb.chat.id
          ))
          """)
  Page<SxsEvaluationPair> findByContainerWithLatestTurns(
      @Param("container") EvaluationContainer container,
      @Param("user") User user,
      Pageable pageable);

  @Query(
      """
      SELECT NEW com.planck.planck.domain.evaluation.dto.SXSPairEvaluationRequest(
          p.id,
          p.chatTurnA.id,
          p.chatTurnB.id
      )
      FROM SxsEvaluationPair p
      LEFT JOIN p.chatTurnA cta
      LEFT JOIN cta.modelResponse mra
      LEFT JOIN p.chatTurnB ctb
      LEFT JOIN ctb.modelResponse mrb
      WHERE p.id IN :ids
      AND p.user = :user
      AND mra.text IS NOT NULL
      AND TRIM(mra.text) <> ''
      AND (p.chatTurnB IS NULL OR (mrb.text IS NOT NULL AND TRIM(mrb.text) <> ''))
      """)
  List<SXSPairEvaluationRequest> findPairsWithOutputOnBothSides(
      @Param("ids") List<String> ids, @Param("user") User user);
}

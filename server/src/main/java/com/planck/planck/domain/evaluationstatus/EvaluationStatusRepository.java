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

package com.planck.planck.domain.evaluationstatus;

import com.planck.planck.entitities.EvaluationStatus;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EvaluationStatusRepository extends JpaRepository<EvaluationStatus, String> {

  List<EvaluationStatus> findAllByJobIdAndUserId(
      @Param("jobId") String jobId, @Param("userId") String userId);

  EvaluationStatus findByEvaluatorIdAndChatTurnIdAndUserId(
      @Param("evaluatorId") String evaluatorId,
      @Param("chatTurnId") String chatTurnId,
      @Param("userId") String userId);

  @Query(
      """
    SELECT es FROM EvaluationStatus es WHERE es.chatTurnId IN :chatTurnIds
    AND es.evaluatorId IN :evaluatorIds AND es.userId = :userId""")
  List<EvaluationStatus> findByTurnAndEvaluatorLists(
      @Param("chatTurnIds") List<String> chatTurnIds,
      @Param("evaluatorIds") Set<String> evaluatorIds,
      @Param("userId") String userId);

  @Transactional
  @Modifying
  @Query(
      "UPDATE EvaluationStatus es SET es.status = :status, es.comments = :comments, es.reason = :reason "
          + "WHERE es.evaluatorId = :evaluatorId AND es.chatTurnId = :chatTurnId "
          + "AND es.jobId = :jobId AND es.userId = :userId")
  void updateStatusByEvaluatorIdAndChatTurnIdAndJobIdAndUserId(
      @Param("status") Integer status,
      @Param("evaluatorId") String evaluatorId,
      @Param("chatTurnId") String chatTurnId,
      @Param("jobId") String jobId,
      @Param("userId") String userId,
      @Param("comments") String comments,
      @Param("reason") String reason);

  @Transactional
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE EvaluationStatus es SET es.status = :newStatus, es.comments = :comments "
          + "WHERE es.jobId = :jobId AND es.status = :oldStatus")
  int updateStatusByJobIdAndUserId(
      @Param("newStatus") Integer newStatus,
      @Param("jobId") String jobId,
      @Param("comments") String comments,
      @Param("oldStatus") Integer oldStatus);

  boolean existsByEvaluatorId(String evaluatorId);
}

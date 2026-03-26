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

package com.planck.planck.domain.inferencestatus;

import com.planck.planck.entitities.InferenceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface InferenceStatusRepository extends JpaRepository<InferenceStatus, String> {

  List<InferenceStatus> findAllByJobIdAndUserId(
      @Param("jobId") String jobId, @Param("userId") String userId);

  @Transactional
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE InferenceStatus is SET is.status = :status, is.comments = :comments, is.reason = :reason "
          + "WHERE is.chatTurn.id = :chatTurnId "
          + "AND is.jobId = :jobId AND is.userId = :userId")
  void updateStatusByChatTurnIdAndJobIdAndUserId(
      @Param("status") Integer status,
      @Param("chatTurnId") String chatTurnId,
      @Param("jobId") String jobId,
      @Param("userId") String userId,
      @Param("comments") String comments,
      @Param("reason") String reason);

  @Transactional
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE InferenceStatus is SET is.status = :status, is.comments = :comments "
          + "WHERE is.jobId = :jobId AND is.status = :inferenceStatus")
  int updateStatusByJobIdAndUserId(
      @Param("status") Integer status,
      @Param("jobId") String jobId,
      @Param("comments") String comments,
      @Param("inferenceStatus") Integer inferenceStatus);

  @Query(
      "SELECT is FROM InferenceStatus is WHERE is.chatTurn.id = :chatTurnId AND is.userId = :userId")
  Optional<InferenceStatus> getByChatTurnId(
      @Param("chatTurnId") String chatTurnId, @Param("userId") String userId);
}

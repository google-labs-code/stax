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

import com.planck.planck.domain.importexport.dto.SXSHumanEvalRatingExportDTO;
import com.planck.planck.domain.project.dto.SXSRatingCount;
import com.planck.planck.entitities.SXSHumanFeedback;
import com.planck.planck.entitities.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SXSHumanFeedbackRepository extends JpaRepository<SXSHumanFeedback, String> {

  @Query("SELECT f FROM SXSHumanFeedback f WHERE f.pairId = :pairId")
  List<SXSHumanFeedback> findByPairId(@Param("pairId") String pairId);

  @Query("SELECT f FROM SXSHumanFeedback f WHERE f.pairId IN :pairIds")
  List<SXSHumanFeedback> findByPairIds(@Param("pairIds") List<String> pairIds);

  @Query(
      "SELECT f FROM SXSHumanFeedback f WHERE f.pairId = :pairId AND f.chatTurnA = :chatTurnA AND f.chatTurnB = :chatTurnB")
  Optional<SXSHumanFeedback> findByPairIdAndChatTurns(
      @Param("pairId") String pairId,
      @Param("chatTurnA") String chatTurnA,
      @Param("chatTurnB") String chatTurnB);

  @Modifying
  @Query("DELETE FROM SXSHumanFeedback f WHERE f.pairId = :pairId")
  void deleteByPairId(String pairId);

  @Modifying
  @Query("DELETE FROM SXSHumanFeedback f WHERE f.pairId IN :pairIds")
  void deleteByPairIdList(List<String> pairIds);

  @Modifying
  @Query(
      "DELETE FROM SXSHumanFeedback f WHERE f.chatTurnA IN :chatTurnIds OR f.chatTurnB IN :chatTurnIds")
  void deleteByChatTurnIdList(List<String> chatTurnIds);

  @Modifying
  @Query(
      "DELETE FROM SXSHumanFeedback f WHERE f.chatTurnA = :chatTurnA AND f.chatTurnB = :chatTurnB")
  void deleteByChatTurnAAndChatTurnB(String chatTurnA, String chatTurnB);

  @Query(
      """
      SELECT new com.planck.planck.domain.project.dto.SXSRatingCount(shf.humanSxsRating, COUNT(shf))
      FROM SXSHumanFeedback shf
      WHERE shf.projectId = :projectId
      AND shf.user = :user
      AND shf.humanSxsRating IS NOT NULL
      GROUP BY shf.humanSxsRating
      """)
  List<SXSRatingCount> getHumanEvalMetricsByProject(
      @Param("projectId") String projectId, @Param("user") User user);

  @Query(
      """
      SELECT new com.planck.planck.domain.importexport.dto.SXSHumanEvalRatingExportDTO(cta.sequenceId, shf.humanSxsRating, shf.humanSxsNotes)
      FROM SXSHumanFeedback shf
      JOIN ChatTurn cta ON cta.id = shf.chatTurnA
      WHERE shf.pairId = :pairId
      """)
  List<SXSHumanEvalRatingExportDTO> getHumanFeedbackExportDTOsByPairId(
      @Param("pairId") String pairId);

  @Modifying
  @Query(
      "DELETE FROM SXSHumanFeedback f WHERE f.pairId = :pairId AND f.chatTurnA = :chatTurnA AND f.chatTurnB = :chatTurnB")
  void deleteByPairIdAndChatTurns(String pairId, String chatTurnA, String chatTurnB);
}

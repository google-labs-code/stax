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

package com.planck.planck.domain.tags.repository;

import com.planck.planck.entitities.Tag;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.entitities.User;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("tagLinkRepository")
public interface TagLinkRepository extends JpaRepository<TagLink, String> {
  @Query("SELECT t FROM TagLink t WHERE t.tag = :tag and t.user = :user")
  List<TagLink> findByTag(@Param("tag") Tag tag, @Param("user") User user);

  @Query(
      "SELECT t FROM TagLink t WHERE t.tag = :tag AND t.targetType = :targetType and t.user = :user")
  List<TagLink> findByTagAndTargetType(
      @Param("tag") Tag tag, @Param("targetType") String targetType, @Param("user") User user);

  @Query(
      "SELECT t FROM TagLink t WHERE t.targetId = :targetId AND t.targetType = :targetType and t.user = :user")
  List<TagLink> findByTargetIdAndTargetType(
      @Param("targetId") String targetId,
      @Param("targetType") String targetType,
      @Param("user") User user);

  @Query(
      "SELECT t FROM TagLink t WHERE t.targetId IN :targetIds AND t.targetType = :targetType and t.user = :user")
  List<TagLink> findByTargetIdsAndTargetType(
      @Param("targetIds") List<String> targetIds,
      @Param("targetType") String targetType,
      @Param("user") User user);

  @Query(
      """
    SELECT t FROM TagLink t JOIN FETCH t.tag
    WHERE ((:targetIds IS NULL AND :targetType IS NULL) OR (t.targetId IN :targetIds AND t.targetType = :targetType))
    AND (:tagIds IS NULL OR t.tag.id IN :tagIds) AND t.user = :user
      """)
  Set<TagLink> findByTargetOrTag(
      @Param("targetIds") List<String> targetIds,
      @Param("targetType") String targetType,
      @Param("tagIds") List<String> tagIds,
      @Param("user") User user);

  @Query(
      "SELECT t.targetId, t.tag.id FROM TagLink t WHERE t.targetId IN :targetIds AND t.targetType = :targetType AND t.user = :user")
  List<Object[]> findTargetIdAndTagIdByTargetIdsAndTypeAndUser(
      @Param("targetIds") List<String> targetIds,
      @Param("targetType") String targetType,
      @Param("user") User user);

  @Query("SELECT t FROM TagLink t WHERE t.targetType = :targetType and t.user = :user")
  List<TagLink> findByTargetType(@Param("targetType") String targetType, @Param("user") User user);

  @Modifying
  @Transactional
  @Query("DELETE FROM TagLink t WHERE t.tag = :tag and t.user = :user")
  int deleteByTag(@Param("tag") Tag tag, @Param("user") User user);

  @Modifying
  @Transactional
  @Query("DELETE FROM TagLink t WHERE t.tag.id IN :tagIds and t.user = :user")
  int deleteByTagIds(@Param("tagIds") List<String> tagIds, @Param("user") User user);

  @Modifying
  @Transactional
  @Query(
      "DELETE FROM TagLink t WHERE t.targetId IN :targetIdList AND t.targetType = :targetType and t.user = :user")
  int deleteByTargetIdListAndTargetType(
      @Param("targetIdList") List<String> targetIdList,
      @Param("targetType") String targetType,
      @Param("user") User user);

  @Query(
      """
        SELECT em.id
        FROM EvaluationMonitoring em
        JOIN ScoreV2 s ON s.evaluationMonitoring = em
        WHERE s.modelResponse.id IN (
            SELECT ct.modelResponse.id
            FROM ChatTurn ct
            WHERE ct.id IN :chatTurnIds
        )
          AND em.user = :user
    """)
  List<String> findEvaluationMonitoringIdsByChatTurnIds(
      @Param("chatTurnIds") List<String> chatTurnIds, @Param("user") User user);

  @Query(
      """
        SELECT im.id
        FROM InferenceMonitoring im
        JOIN ModelResponse mr ON mr.inferenceMonitoring = im
        JOIN ChatTurn ct ON ct.modelResponse.id = mr.id
        WHERE ct.id IN :chatTurnIds
          AND im.user = :user
    """)
  List<String> findInferenceMonitoringIdsByChatTurnIds(
      @Param("chatTurnIds") List<String> chatTurnIds, @Param("user") User user);

  @Query(
      """
        SELECT tl
        FROM TagLink tl
        WHERE tl.targetType = 'EVALUATION_MONITORING'
          AND tl.user = :user
          AND (:tagIds IS NULL OR tl.tag.tagId IN :tagIds)
          AND tl.targetId IN (
            SELECT DISTINCT s.evaluationMonitoring.id
            FROM ScoreV2 s
            WHERE s.modelResponse.id IN (
                SELECT ct.modelResponse.id
                FROM ChatTurn ct
                WHERE ct.id IN :chatTurnIds
            )
          )
    """)
  List<TagLink> findEvaluationTagLinksByChatTurnIdsAndTagIds(
      @Param("chatTurnIds") List<String> chatTurnIds,
      @Param("tagIds") List<String> tagIds,
      @Param("user") User user);

  @Query(
      """
        SELECT tl
        FROM TagLink tl
        WHERE tl.targetType = 'INFERENCE_MONITORING'
          AND tl.user = :user
          AND (:tagIds IS NULL OR tl.tag.tagId IN :tagIds)
          AND tl.targetId IN (
            SELECT DISTINCT im.id
            FROM InferenceMonitoring im
            JOIN ModelResponse mr ON mr.inferenceMonitoring = im
            JOIN ChatTurn ct ON ct.modelResponse.id = mr.id
            WHERE ct.id IN :chatTurnIds
          )
    """)
  List<TagLink> findInferenceTagLinksByChatTurnIds(
      @Param("chatTurnIds") List<String> chatTurnIds,
      @Param("tagIds") List<String> tagIds,
      @Param("user") User user);

  @Query(
      """
            SELECT DISTINCT ctl.tag
            FROM ScoreV2 s
            JOIN ChatTurn ct ON s.modelResponse.id = ct.modelResponse.id
            JOIN TagLink ctl ON ctl.targetId = ct.id
            WHERE s.evaluationMonitoring.id = :evaluationMonitoringId
              AND ctl.targetType = 'CHAT_TURN'
              AND ctl.user = :user
        """)
  List<Tag> findTagsForEvaluationMonitoring(
      @Param("evaluationMonitoringId") String evaluationMonitoringId, @Param("user") User user);

  @Query("SELECT t FROM TagLink t WHERE t.user = :user")
  List<TagLink> findAllByUser(User user);

  @Modifying
  @Query("DELETE FROM TagLink t WHERE t.id IN :ids and t.user = :user")
  void deleteByIdsAndUser(@Param("ids") List<String> ids, @Param("user") User user);
}

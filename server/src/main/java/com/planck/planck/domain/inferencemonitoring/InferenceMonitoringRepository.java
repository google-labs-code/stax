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

package com.planck.planck.domain.inferencemonitoring;

import com.planck.planck.domain.analytics.inference.dto.ProjectInferenceMonitoringSummaryDTO;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InferenceMonitoringRepository
    extends JpaRepository<InferenceMonitoring, String>, InferenceMonitoringRepositoryCustom {
  @Query("SELECT im FROM InferenceMonitoring im WHERE im.id IN :idList AND im.user = :user")
  List<InferenceMonitoring> findInferenceMonitroginsByList(List<String> idList, User user);

  @Modifying
  @Query("UPDATE InferenceMonitoring im SET im.container = NULL WHERE im.container = :container")
  void clearEvaluationContainerReference(
      @Param("container") EvaluationContainer evaluationContainer);

  @Query(
      """
          SELECT new com.planck.planck.domain.analytics.inference.dto.ProjectInferenceMonitoringSummaryDTO(
              COUNT(im),
              CAST(COALESCE(AVG(im.turnTimeTaken), 0.0) AS double),
              CAST(COALESCE(SUM(im.turnPromptTokens), 0L) AS long),
              CAST(COALESCE(SUM(im.turnCompletionTokens), 0L) AS long),
              CAST(COALESCE(SUM(im.turnTotalTokens), 0L) AS long)
          )
          FROM InferenceMonitoring im
          WHERE im.container.id = :containerId AND im.user = :user
          """)
  ProjectInferenceMonitoringSummaryDTO getProjectInferenceMonitoringSummary(
      @Param("containerId") String containerId, @Param("user") User user);

  /** Get inference monitoring summary for Side A of SxS pairs in a project */
  @Query(
      """
      SELECT new com.planck.planck.domain.analytics.inference.dto.ProjectInferenceMonitoringSummaryDTO(
          COUNT(im),
          CAST(COALESCE(AVG(im.turnTimeTaken), 0.0) AS double),
          CAST(COALESCE(SUM(im.turnPromptTokens), 0L) AS long),
          CAST(COALESCE(SUM(im.turnCompletionTokens), 0L) AS long),
          CAST(COALESCE(SUM(im.turnTotalTokens), 0L) AS long)
      )
      FROM com.planck.planck.entitities.SxsEvaluationPair p
      JOIN p.chatTurnA cta
      JOIN cta.modelResponse mr
      JOIN mr.inferenceMonitoring im
      WHERE p.container = :container
      AND p.container.user = :user
      AND mr IS NOT NULL
      AND im IS NOT NULL
      """)
  ProjectInferenceMonitoringSummaryDTO getSxSSideAInferenceMonitoringSummary(
      @Param("container") EvaluationContainer container, @Param("user") User user);

  /** Get inference monitoring summary for Side B of SxS pairs in a project */
  @Query(
      """
      SELECT new com.planck.planck.domain.analytics.inference.dto.ProjectInferenceMonitoringSummaryDTO(
          COUNT(im),
          CAST(COALESCE(AVG(im.turnTimeTaken), 0.0) AS double),
          CAST(COALESCE(SUM(im.turnPromptTokens), 0L) AS long),
          CAST(COALESCE(SUM(im.turnCompletionTokens), 0L) AS long),
          CAST(COALESCE(SUM(im.turnTotalTokens), 0L) AS long)
      )
      FROM com.planck.planck.entitities.SxsEvaluationPair p
      JOIN p.chatTurnB ctb
      JOIN ctb.modelResponse mr
      JOIN mr.inferenceMonitoring im
      WHERE p.container = :container
      AND p.container.user = :user
      AND mr IS NOT NULL
      AND im IS NOT NULL
      """)
  ProjectInferenceMonitoringSummaryDTO getSxSSideBInferenceMonitoringSummary(
      @Param("container") EvaluationContainer container, @Param("user") User user);
}

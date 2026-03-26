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

import com.planck.planck.entitities.EvaluationMonitoring;
import com.planck.planck.entitities.ScoreV2;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreV2Repository extends JpaRepository<ScoreV2, String> {

  @Query(
      "SELECT AVG(s.score) FROM ScoreV2 s JOIN s.modelResponse mr"
          + " WHERE s.llmEvaluator.id =:evaluatorId AND mr.container.id = :containerId ")
  Double findAvgScoreByEvaluatorIdAndProjectId(
      @Param("evaluatorId") String evaluatorId, @Param("containerId") String containerId);

  void deleteByEvaluationMonitoringIn(List<EvaluationMonitoring> monitorings);

  List<ScoreV2> findByModelResponse_Id(String modelResponseId);
}

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

import com.planck.planck.domain.analytics.evaluation.dto.ScorerEvaluationRecord;
import com.planck.planck.entitities.PairwiseScore;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PairwiseScoreRepository extends JpaRepository<PairwiseScore, String> {

  @Query(
      """
        SELECT ps FROM PairwiseScore ps WHERE ps.pair.id = :pairId
        AND ps.chatTurnA.id = :chatTurnAId AND ps.chatTurnB.id = :chatTurnBId
        AND ps.llmEvaluator.id = :evaluatorId AND ps.userId = :userId""")
  public Optional<PairwiseScore> findByPairAndChatTurnsAndEvaluatorAndUser(
      String pairId, String chatTurnAId, String chatTurnBId, String evaluatorId, String userId);

  @Query("SELECT ps FROM PairwiseScore ps WHERE ps.pair.id = :pairId AND ps.userId = :userId")
  public List<PairwiseScore> findByPairAndUser(String pairId, String userId);

  @Query(
      """
        SELECT ps FROM PairwiseScore ps
        WHERE ps.pair.id IN :pairIds
        AND ps.chatTurnA.id IN :chatTurnAIds
        AND ps.chatTurnB.id IN :chatTurnBIds
        AND ps.llmEvaluator.id IN :evaluatorIds
        AND ps.userId = :userId""")
  public List<PairwiseScore> findEvaluationStatusByPairDetailsAndEvaluators(
      List<String> pairIds,
      List<String> chatTurnAIds,
      List<String> chatTurnBIds,
      Set<String> evaluatorIds,
      String userId);

  @Query(
      """
      SELECT new com.planck.planck.domain.analytics.evaluation.dto.ScorerEvaluationRecord(
          ps.llmEvaluator.id,
          ps.llmEvaluator.name,
          ps.score,
          COUNT(ps)
      )
      FROM PairwiseScore ps
      JOIN ps.pair p
      WHERE p.container.id = :projectId
      AND ps.userId = :userId
      AND ps.score IS NOT NULL
      GROUP BY ps.llmEvaluator.id, ps.llmEvaluator.name, ps.score
      ORDER BY ps.llmEvaluator.id, ps.score
      """)
  public List<ScorerEvaluationRecord> getPairwiseEvaluationRecords(String projectId, String userId);

  @Query(
      "SELECT CASE WHEN COUNT(ps) > 0 THEN true ELSE false END FROM PairwiseScore ps WHERE ps.llmEvaluator.id = :evaluatorId AND ps.userId = :userId")
  public boolean existsByEvaluatorId(String evaluatorId, String userId);
}

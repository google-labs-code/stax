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

import com.planck.planck.entitities.HumanEvalScore;
import com.planck.planck.entitities.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HumanEvalScoreRepository extends JpaRepository<HumanEvalScore, String> {

  @Query(
      "SELECT hs FROM HumanEvalScore hs WHERE hs.modelResponse.id = :modelResponseId AND hs.humanEvaluator.id = :evaluatorId AND hs.user = :user")
  Optional<HumanEvalScore> findByModelResponseIdAndEvaluatorId(
      @Param("modelResponseId") String modelResponseId,
      @Param("evaluatorId") String evaluatorId,
      @Param("user") User user);

  @Query(
      """
        SELECT hs FROM HumanEvalScore hs
        JOIN hs.modelResponse mr
        JOIN ChatTurn ct ON mr.id = ct.modelResponse.id
        WHERE ct.chat.container.id = :containerId
    """)
  List<HumanEvalScore> findAllByContainerId(@Param("containerId") String containerId);
}

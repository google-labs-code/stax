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

package com.planck.planck.domain.evaluator.heuristic;

import com.planck.planck.entitities.PointwiseHeuristicEvaluator;
import com.planck.planck.entitities.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PointwiseHeuristicEvaluatorRepository
    extends JpaRepository<PointwiseHeuristicEvaluator, String> {

  List<PointwiseHeuristicEvaluator> findAllByUser(User user);

  @Query(
      "SELECT COUNT(phe) > 0 FROM PointwiseHeuristicEvaluator phe WHERE phe.name = :name AND phe.user = :user")
  boolean checkIfPointwiseHeuristicEvaluatorNameExists(
      @Param("name") String name, @Param("user") User user);

  @Query(
      "SELECT phe FROM PointwiseHeuristicEvaluator phe WHERE phe.user = :user AND phe.deprecated = false")
  List<PointwiseHeuristicEvaluator> findAllNonDeprecatedByUser(User user);

  @Query("SELECT phe FROM PointwiseHeuristicEvaluator phe WHERE phe.id = :id AND phe.user = :user")
  Optional<PointwiseHeuristicEvaluator> findByIdAndUser(
      @Param("id") String id, @Param("user") User user);
}

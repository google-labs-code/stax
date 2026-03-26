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

package com.planck.planck.domain.evaluator.pairwise;

import com.planck.planck.entitities.PairwiseLLMEvaluator;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ScopeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PairwiseLLMEvaluatorRepository
    extends JpaRepository<PairwiseLLMEvaluator, String> {

  List<PairwiseLLMEvaluator> findAllByUserOrType(User user, ScopeType type);

  @Query(
      "SELECT COUNT(ple) > 0 FROM PairwiseLLMEvaluator ple WHERE ple.name = :name AND (ple.user = :user OR ple.type = :type)")
  boolean checkIfPairwiseLLMEvaluatorNameExists(
      @Param("name") String name, @Param("user") User user, @Param("type") ScopeType type);
}

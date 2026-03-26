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

package com.planck.planck.domain.evaluator.llm;

import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.PairwiseLLMEvaluator;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ScopeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LLMEvaluatorRepository extends JpaRepository<LLMEvaluator, String> {

  List<LLMEvaluator> findAllByUserOrType(User user, ScopeType type);

  @Query("SELECT le FROM LLMEvaluator le WHERE le.user = :user AND le.deprecated = false")
  List<LLMEvaluator> findAllNonDeprecatedByUser(User user);

  @Query("SELECT le FROM LLMEvaluator le WHERE le.type = :type AND le.deprecated = false")
  List<LLMEvaluator> findAllNonDeprecatedByType(ScopeType type);

  @Query("SELECT le FROM LLMEvaluator le WHERE le.id = :id AND le.user = :user")
  Optional<LLMEvaluator> findByIdAndUser(@Param("id") String id, @Param("user") User user);

  @Query(
      "SELECT COUNT(le) > 0 FROM LLMEvaluator le WHERE le.name = :name AND (le.user = :user OR le.type = :type)")
  boolean checkIfLLMEvaluatorNameExists(
      @Param("name") String name, @Param("user") User user, @Param("type") ScopeType type);

  @Query(
      "SELECT le FROM LLMEvaluator le WHERE le.name = :name AND (le.user = :user OR le.type = :type)")
  Optional<LLMEvaluator> findByNameAndUserOrType(
      @Param("name") String name, @Param("user") User user, @Param("type") ScopeType type);

  @Query(
      "SELECT le FROM LLMEvaluator le "
          + "WHERE (le.id = :id AND le.type = 'SYSTEM') "
          + "OR (le.id = :id AND le.user = :user AND le.type = 'USER')")
  Optional<LLMEvaluator> findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
      @Param("id") String id, @Param("user") User user);

  @Query(
      "SELECT le FROM ScoreV2 s JOIN s.llmEvaluator le JOIN s.modelResponse mr "
          + "WHERE mr.container.id = :containerId "
          + "ORDER BY s.updatedAt DESC LIMIT 1")
  Optional<LLMEvaluator> findLatestRunEvaluatorByContainerId(
      @Param("containerId") String containerId);

  @Query(
      "SELECT ple FROM PairwiseLLMEvaluator ple WHERE (ple.user = :user OR ple.type = 'SYSTEM') AND ple.id = :evaluatorId")
  Optional<PairwiseLLMEvaluator> findPairwiseEvaluatorByIdAndUser(String evaluatorId, User user);
}

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

package com.planck.planck.domain.evaluationmonitoring;

import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.EvaluationMonitoring;
import com.planck.planck.entitities.User;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationMonitoringRepository
    extends JpaRepository<EvaluationMonitoring, String> {

  @Query("SELECT em FROM EvaluationMonitoring em WHERE em.id IN :ids AND em.user = :user")
  List<EvaluationMonitoring> findEvaluationMonitroginsByList(Collection<String> ids, User user);

  List<EvaluationMonitoring> findEvaluationMonitoringByContainerAndUser(
      EvaluationContainer container, User user);
}

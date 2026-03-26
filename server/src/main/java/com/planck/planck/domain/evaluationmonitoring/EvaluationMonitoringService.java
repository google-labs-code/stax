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
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.PairwiseLLMEvaluator;
import com.planck.planck.entitities.User;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import java.util.List;
import java.util.Set;

public interface EvaluationMonitoringService {
  EvaluationMonitoring saveEvaluationMonitoring(
      User user,
      ChatResponseWithLatency chatResponse,
      Model model,
      LLMEvaluator evaluator,
      EvaluationContainer container);

  void deleteEvaluationMonitoringAndRelatedScores(EvaluationContainer container, User user);

  void deleteEvaluationMonitoringAndRelatedScores(Set<String> monitoringIds, User user);

  void deleteEvaluationMonitoringAndRelatedScores(List<EvaluationMonitoring> evaluationMonitorings);

  EvaluationMonitoring saveEvaluationMonitoring(
      User user,
      ChatResponseWithLatency response,
      Model evalModel,
      PairwiseLLMEvaluator llmEvaluator,
      EvaluationContainer container);
}

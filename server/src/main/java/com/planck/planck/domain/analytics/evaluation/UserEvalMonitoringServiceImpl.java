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

package com.planck.planck.domain.analytics.evaluation;

import com.planck.planck.domain.analytics.evaluation.dto.EvaluationAnalyticsRowDTO;
import com.planck.planck.domain.analytics.evaluation.dto.UserEvalChartBuilder;
import com.planck.planck.domain.analytics.evaluation.dto.UserEvalChartEntryDTO;
import com.planck.planck.domain.analytics.evaluation.dto.UserEvalMonitoringParams;
import com.planck.planck.domain.evaluator.llm.LLMEvaluatorRepository;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.User;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserEvalMonitoringServiceImpl implements UserEvalMonitoringService {

  @Autowired private EvaluationAnalyticsRepository evaluationAnalyticsRepository;

  @Autowired private LLMEvaluatorRepository llmEvaluatorRepository;

  @Override
  public Map<String, UserEvalChartEntryDTO> getAnalyticsMonitoringData(
      User user, UserEvalMonitoringParams params) {
    log.info("Get analytics eval monitoring data");

    List<EvaluationAnalyticsRowDTO> results =
        evaluationAnalyticsRepository.getAnalyticsData(params, user);

    Map<String, List<EvaluationAnalyticsRowDTO>> groupedByEvaluator =
        results.stream()
            .filter(row -> row.evaluatorId() != null)
            .collect(Collectors.groupingBy(EvaluationAnalyticsRowDTO::evaluatorId));

    List<String> evaluatorIds = new ArrayList<>(groupedByEvaluator.keySet());
    List<LLMEvaluator> evaluators = llmEvaluatorRepository.findAllById(evaluatorIds);
    Map<String, LLMEvaluator> evaluatorMap =
        evaluators.stream().collect(Collectors.toMap(LLMEvaluator::getId, Function.identity()));

    // 4. Build response entries
    Map<String, UserEvalChartEntryDTO> chartMap = new LinkedHashMap<>();
    for (Map.Entry<String, List<EvaluationAnalyticsRowDTO>> entry : groupedByEvaluator.entrySet()) {
      String evaluatorId = entry.getKey();
      LLMEvaluator evaluator = evaluatorMap.get(evaluatorId);
      if (evaluator != null) {
        UserEvalChartEntryDTO chartEntry = UserEvalChartBuilder.build(entry.getValue(), evaluator);
        chartEntry.setScorerId(evaluator.getId());
        chartEntry.setScorerName(evaluator.getName());
        chartMap.put(evaluator.getName(), chartEntry);
      }
    }

    return chartMap;
  }
}

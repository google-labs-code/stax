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

package com.planck.planck.domain.analytics.evaluation.dto;

import com.planck.planck.domain.evaluator.dto.OutputCategoryDTO;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.util.ObjectMapperUtil;
import java.util.*;
import java.util.stream.Collectors;

public class UserEvalChartBuilder {

  public static UserEvalChartEntryDTO build(
      List<EvaluationAnalyticsRowDTO> rows, LLMEvaluator evaluator) {
    UserEvalChartEntryDTO chart = new UserEvalChartEntryDTO();
    chart.setDatapoints(new ArrayList<>());
    chart.setVariables(new EvalChartEntryVariablesDTO());
    chart.setMonitoring(new EvalChartMonitoringStatsDTO());

    // Parse and initialize datapoints from evaluator config
    List<OutputCategoryDTO> outputCategories =
        ObjectMapperUtil.convertJsonStringToList(
            evaluator.getOutputCategories(), OutputCategoryDTO.class);

    if (outputCategories == null) {
      outputCategories = new ArrayList<>();
    }

    List<EvalChartDatapointDTO> initialPoints =
        outputCategories.stream()
            .map(cfg -> new EvalChartDatapointDTO(cfg.getName(), cfg.getValue(), cfg.getColor()))
            .collect(Collectors.toList());
    chart.getDatapoints().addAll(initialPoints);

    for (EvaluationAnalyticsRowDTO row : rows) {
      String rowScore = String.valueOf(row.score());

      Optional<EvalChartDatapointDTO> match =
          chart.getDatapoints().stream().filter(dp -> dp.getScore().equals(rowScore)).findFirst();

      int rowPromptTokens = row.promptTokens() != null ? row.promptTokens() : 0;
      int rowCompletionTokens = row.completionTokens() != null ? row.completionTokens() : 0;
      double rowTimeTaken = row.timeTaken() != null ? row.timeTaken() : 0;

      if (match.isPresent()) {
        EvalChartDatapointDTO dp = match.get();
        dp.incrementCount();
        dp.addPromptTokens(rowPromptTokens);
        dp.addCompletionTokens(rowCompletionTokens);
        dp.addTotalLatency(rowTimeTaken);
        dp.updateAverages();
      } else {
        EvalChartEntryVariablesDTO meta = chart.getVariables();
        switch (row.status()) {
          case 0 -> meta.incrementPending();
          case 2 -> meta.incrementInProgress();
          case -1 -> meta.incrementFailed();
          default -> meta.incrementUnknown();
        }
      }

      // Monitoring aggregation
      EvalChartMonitoringStatsDTO monitoring = chart.getMonitoring();
      monitoring.setTotalPromptTokens(monitoring.getTotalPromptTokens() + rowPromptTokens);
      monitoring.setTotalCompletionTokens(
          monitoring.getTotalCompletionTokens() + rowCompletionTokens);
      monitoring.setTotalLatency(monitoring.getTotalLatency() + rowTimeTaken);
    }

    // Finalize global averages
    long totalCount =
        chart.getDatapoints().stream().mapToLong(EvalChartDatapointDTO::getCount).sum();
    if (totalCount > 0) {
      EvalChartMonitoringStatsDTO stats = chart.getMonitoring();
      stats.setAveragePromptTokens(stats.getTotalPromptTokens() / totalCount);
      stats.setAverageCompletionTokens(stats.getTotalCompletionTokens() / totalCount);
      stats.setAverageLatency(stats.getTotalLatency() / (double) totalCount);
    }

    return chart;
  }
}

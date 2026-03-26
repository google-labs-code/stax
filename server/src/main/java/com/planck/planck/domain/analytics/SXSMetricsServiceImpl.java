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

package com.planck.planck.domain.analytics;

import com.planck.planck.domain.analytics.evaluation.UserEvalMonitoringService;
import com.planck.planck.domain.analytics.evaluation.dto.BaseEvalChartDatapointDTO;
import com.planck.planck.domain.analytics.evaluation.dto.ProjectEvaluationAnalyticsByScorer;
import com.planck.planck.domain.analytics.evaluation.dto.ScorerEvaluationRecord;
import com.planck.planck.domain.analytics.evaluation.dto.UserEvalChartEntryDTO;
import com.planck.planck.domain.analytics.evaluation.dto.UserEvalMonitoringParams;
import com.planck.planck.domain.analytics.inference.dto.ProjectInferenceMonitoringSummaryDTO;
import com.planck.planck.domain.evaluation.PairwiseScoreRepository;
import com.planck.planck.domain.evaluation.SXSHumanFeedbackService;
import com.planck.planck.domain.evaluator.dto.OutputCategoryDTO;
import com.planck.planck.domain.evaluator.pairwise.service.PairwiseLLMEvaluatorService;
import com.planck.planck.domain.inferencemonitoring.InferenceMonitoringRepository;
import com.planck.planck.domain.project.ProjectRepository;
import com.planck.planck.domain.project.dto.SXSEvaluationAnalyticsDTO;
import com.planck.planck.domain.project.dto.SXSHumanEvalMetricsDTO;
import com.planck.planck.domain.project.dto.SXSPairwiseEvaluationMetricsDTO;
import com.planck.planck.domain.project.dto.SXSProjectInferenceMonitoringSummaryDTO;
import com.planck.planck.domain.project.dto.SXSRatingCount;
import com.planck.planck.entitities.PairwiseLLMEvaluator;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.HumanSxsRating;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.util.ObjectMapperUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SXSMetricsServiceImpl implements SXSMetricsService {
  private final ProjectRepository projectRepository;
  private final InferenceMonitoringRepository inferenceMonitoringRepository;
  private final UserEvalMonitoringService userEvalMonitoringService;
  private final SXSHumanFeedbackService sxsHumanFeedbackService;
  private final PairwiseScoreRepository pairwiseScoreRepository;
  private final PairwiseLLMEvaluatorService pairwiseLLMEvaluatorService;

  public SXSMetricsServiceImpl(
      ProjectRepository projectRepository,
      InferenceMonitoringRepository inferenceMonitoringRepository,
      UserEvalMonitoringService userEvalMonitoringService,
      SXSHumanFeedbackService sxsHumanFeedbackService,
      PairwiseScoreRepository sxsPairwiseScoreRepository,
      PairwiseLLMEvaluatorService pairwiseLLMEvaluatorService) {
    this.projectRepository = projectRepository;
    this.inferenceMonitoringRepository = inferenceMonitoringRepository;
    this.userEvalMonitoringService = userEvalMonitoringService;
    this.sxsHumanFeedbackService = sxsHumanFeedbackService;
    this.pairwiseScoreRepository = sxsPairwiseScoreRepository;
    this.pairwiseLLMEvaluatorService = pairwiseLLMEvaluatorService;
  }

  @Override
  @Transactional(readOnly = true)
  public SXSHumanEvalMetricsDTO getHumanEvalMetrics(String projectId, User user) {
    Project project = getProject(projectId, user);

    List<SXSRatingCount> ratingCounts =
        sxsHumanFeedbackService.getHumanEvalMetricsByProject(project.getId(), user);

    Map<HumanSxsRating, Long> ratingCountsMap =
        ratingCounts.stream()
            .collect(Collectors.toMap(SXSRatingCount::rating, SXSRatingCount::count));

    Long total = ratingCountsMap.values().stream().mapToLong(Long::longValue).sum();

    return SXSHumanEvalMetricsDTO.builder().ratingCounts(ratingCountsMap).total(total).build();
  }

  @Override
  @Transactional(readOnly = true)
  public SXSProjectInferenceMonitoringSummaryDTO getInferenceMetrics(String projectId, User user) {
    Project project = getProject(projectId, user);

    CompletableFuture<ProjectInferenceMonitoringSummaryDTO> sideAFuture =
        CompletableFuture.supplyAsync(
            () ->
                inferenceMonitoringRepository.getSxSSideAInferenceMonitoringSummary(project, user));

    CompletableFuture<ProjectInferenceMonitoringSummaryDTO> sideBFuture =
        CompletableFuture.supplyAsync(
            () ->
                inferenceMonitoringRepository.getSxSSideBInferenceMonitoringSummary(project, user));

    ProjectInferenceMonitoringSummaryDTO sideA = sideAFuture.join();
    ProjectInferenceMonitoringSummaryDTO sideB = sideBFuture.join();

    ProjectInferenceMonitoringSummaryDTO delta = calculateInferenceDelta(sideA, sideB);

    return SXSProjectInferenceMonitoringSummaryDTO.builder()
        .sideA(sideA)
        .sideB(sideB)
        .delta(delta)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public SXSEvaluationAnalyticsDTO getEvaluationAnalyticsByScorer(
      String projectId, String scorerId, User user) {
    Project project = getProject(projectId, user);

    ProjectEvaluationAnalyticsByScorer sideA =
        getAnalyticsForScorer(project, scorerId, user, "chatTurnA");

    ProjectEvaluationAnalyticsByScorer sideB =
        getAnalyticsForScorer(project, scorerId, user, "chatTurnB");

    String scorerName =
        sideA.getScorerName() != null ? sideA.getScorerName() : sideB.getScorerName();

    return SXSEvaluationAnalyticsDTO.builder()
        .sideA(sideA)
        .sideB(sideB)
        .delta(calculateAverageScoreDelta(sideA, sideB))
        .scorerId(scorerId)
        .scorerName(scorerName)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<SXSEvaluationAnalyticsDTO> getEvaluationAnalyticsForAllScorers(
      String projectId, User user) {
    Project project = getProject(projectId, user);

    UserEvalMonitoringParams params = new UserEvalMonitoringParams();
    params.setProjectId(project.getId());
    params.setSxsJoinColumnName("chatTurnA");
    Map<String, UserEvalChartEntryDTO> allSideAAnalytics =
        userEvalMonitoringService.getAnalyticsMonitoringData(user, params);

    params.setSxsJoinColumnName("chatTurnB");
    Map<String, UserEvalChartEntryDTO> allSideBAnalytics =
        userEvalMonitoringService.getAnalyticsMonitoringData(user, params);

    if (allSideAAnalytics.isEmpty() && allSideBAnalytics.isEmpty()) {
      return List.of();
    }

    List<SXSEvaluationAnalyticsDTO> result =
        getEvaluationMetricsResult(allSideAAnalytics, allSideBAnalytics);

    return result;
  }

  private List<SXSEvaluationAnalyticsDTO> getEvaluationMetricsResult(
      Map<String, UserEvalChartEntryDTO> allSideAAnalytics,
      Map<String, UserEvalChartEntryDTO> allSideBAnalytics) {
    List<SXSEvaluationAnalyticsDTO> result = new ArrayList<>();

    for (Map.Entry<String, UserEvalChartEntryDTO> sideAEntry : allSideAAnalytics.entrySet()) {
      String scorerName = sideAEntry.getKey();
      UserEvalChartEntryDTO sideAChartEntry = sideAEntry.getValue();

      UserEvalChartEntryDTO sideBChartEntry = allSideBAnalytics.get(scorerName);

      ProjectEvaluationAnalyticsByScorer sideA =
          new ProjectEvaluationAnalyticsByScorer(
              sideAChartEntry.getDatapoints(),
              sideAChartEntry.getScorerId(),
              sideAChartEntry.getScorerName());
      ProjectEvaluationAnalyticsByScorer sideB =
          sideBChartEntry != null
              ? new ProjectEvaluationAnalyticsByScorer(
                  sideBChartEntry.getDatapoints(),
                  sideBChartEntry.getScorerId(),
                  sideBChartEntry.getScorerName())
              : null;

      String scorerId = sideAChartEntry.getScorerId();
      SXSEvaluationAnalyticsDTO analyticsDTO =
          SXSEvaluationAnalyticsDTO.builder()
              .sideA(sideA)
              .sideB(sideB)
              .delta(calculateAverageScoreDelta(sideA, sideB))
              .scorerId(scorerId)
              .scorerName(scorerName)
              .build();

      result.add(analyticsDTO);

      allSideBAnalytics.remove(scorerName);
    }

    for (Map.Entry<String, UserEvalChartEntryDTO> sideBEntry : allSideBAnalytics.entrySet()) {
      UserEvalChartEntryDTO sideBChartEntry = sideBEntry.getValue();

      ProjectEvaluationAnalyticsByScorer sideA = null;
      ProjectEvaluationAnalyticsByScorer sideB =
          new ProjectEvaluationAnalyticsByScorer(
              sideBChartEntry.getDatapoints(),
              sideBChartEntry.getScorerId(),
              sideBChartEntry.getScorerName());

      String scorerId = sideBChartEntry.getScorerId();
      String scorerName = sideBChartEntry.getScorerName();

      SXSEvaluationAnalyticsDTO analyticsDTO =
          SXSEvaluationAnalyticsDTO.builder()
              .sideA(sideA)
              .sideB(sideB)
              .delta(calculateAverageScoreDelta(sideA, sideB))
              .scorerId(scorerId)
              .scorerName(scorerName)
              .build();

      result.add(analyticsDTO);
    }
    return result;
  }

  private Project getProject(String projectId, User user) {
    Project project =
        projectRepository
            .findByUserAndId(user, projectId)
            .orElseThrow(() -> new NotFoundException("Project not found for this user"));

    if (project.getEvaluationType() != EvaluationType.SXS) {
      throw new NotFoundException("Project is not an SxS project");
    }

    return project;
  }

  private ProjectEvaluationAnalyticsByScorer getAnalyticsForScorer(
      Project project, String scorerId, User user, String sxsJoinColumn) {
    UserEvalMonitoringParams params = new UserEvalMonitoringParams();
    params.setSxsJoinColumnName(sxsJoinColumn);
    params.setProjectId(project.getId());
    params.setScorerIds(List.of(scorerId));

    Map<String, UserEvalChartEntryDTO> analytics =
        userEvalMonitoringService.getAnalyticsMonitoringData(user, params);

    if (analytics.isEmpty()) {
      return new ProjectEvaluationAnalyticsByScorer();
    }

    UserEvalChartEntryDTO entry = analytics.values().stream().findFirst().orElse(null);

    if (entry == null) {
      return new ProjectEvaluationAnalyticsByScorer();
    }

    return new ProjectEvaluationAnalyticsByScorer(
        entry.getDatapoints(), entry.getScorerId(), entry.getScorerName());
  }

  private ProjectInferenceMonitoringSummaryDTO calculateInferenceDelta(
      ProjectInferenceMonitoringSummaryDTO sideA, ProjectInferenceMonitoringSummaryDTO sideB) {

    return ProjectInferenceMonitoringSummaryDTO.builder()
        .totalInferences(sideA.getTotalInferences() - sideB.getTotalInferences())
        .averageTurnTimeTaken(
            calculateDelta(sideA.getAverageTurnTimeTaken(), sideB.getAverageTurnTimeTaken()))
        .totalPromptTokens(sideA.getTotalPromptTokens() - sideB.getTotalPromptTokens())
        .totalCompletionTokens(sideA.getTotalCompletionTokens() - sideB.getTotalCompletionTokens())
        .totalTokens(sideA.getTotalTokens() - sideB.getTotalTokens())
        .build();
  }

  private Double calculateAverageScoreDelta(
      ProjectEvaluationAnalyticsByScorer sideA, ProjectEvaluationAnalyticsByScorer sideB) {

    if (sideA == null || sideB == null) {
      return 0.0;
    }

    return calculateDelta(sideA.getAverageScore(), sideB.getAverageScore());
  }

  private Double calculateDelta(Double sideA, Double sideB) {
    if (sideA == null && sideB == null) return 0.0;
    if (sideA == null) return -sideB;
    if (sideB == null) return sideA;
    return sideA - sideB;
  }

  @Override
  public List<SXSPairwiseEvaluationMetricsDTO> getSxsEvalMetrics(String containerId, User user) {
    List<ScorerEvaluationRecord> records =
        pairwiseScoreRepository.getPairwiseEvaluationRecords(containerId, user.getId());
    List<PairwiseLLMEvaluator> evaluators =
        pairwiseLLMEvaluatorService.getAllPairwiseLLMEvaluators(user);

    // Create maps for quick lookups
    Map<String, PairwiseLLMEvaluator> evaluatorMap =
        evaluators.stream().collect(Collectors.toMap(PairwiseLLMEvaluator::getId, e -> e));

    Map<String, List<OutputCategoryDTO>> categoriesMap =
        evaluators.stream()
            .collect(
                Collectors.toMap(
                    PairwiseLLMEvaluator::getId,
                    e ->
                        ObjectMapperUtil.convertJsonStringToList(
                            e.getOutputCategories(), OutputCategoryDTO.class)));

    Map<String, SXSPairwiseEvaluationMetricsDTO> metricsMap = new HashMap<>();

    for (ScorerEvaluationRecord record : records) {
      if (!metricsMap.containsKey(record.scorerId())) {
        PairwiseLLMEvaluator evaluator = evaluatorMap.get(record.scorerId());
        if (evaluator == null) continue; // Skip if evaluator not found

        List<OutputCategoryDTO> categories = categoriesMap.get(record.scorerId());
        List<BaseEvalChartDatapointDTO> datapoints =
            categories.stream()
                .map(
                    category ->
                        new BaseEvalChartDatapointDTO(
                            category.getName(),
                            category.getName(),
                            category.getColor(),
                            0L // Initialize count to 0
                            ))
                .collect(Collectors.toList());

        metricsMap.put(
            record.scorerId(),
            new SXSPairwiseEvaluationMetricsDTO(
                evaluator.getId(), evaluator.getName(), datapoints));
      }

      // Update the count for this category
      metricsMap.get(record.scorerId()).updateCount(record.score(), record.count());
    }

    return new ArrayList<>(metricsMap.values());
  }
}

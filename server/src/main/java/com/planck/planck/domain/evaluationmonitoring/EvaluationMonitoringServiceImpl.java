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

import com.planck.planck.domain.evaluation.ScoreV2Repository;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.EvaluationMonitoring;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.PairwiseLLMEvaluator;
import com.planck.planck.entitities.User;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EvaluationMonitoringServiceImpl implements EvaluationMonitoringService {

  @Autowired private EvaluationMonitoringRepository evaluationMonitoringRepository;
  @Autowired private ScoreV2Repository scoreRepository;

  @Override
  public EvaluationMonitoring saveEvaluationMonitoring(
      User user,
      ChatResponseWithLatency chatResponse,
      Model model,
      LLMEvaluator evaluator,
      EvaluationContainer container) {
    log.info("Start EvaluationMonitoring ");

    EvaluationMonitoring evaluationMonitoring =
        new EvaluationMonitoring(model, user, container, evaluator);

    evaluationMonitoring.setTimeTaken(chatResponse.getLatencyMillis());

    evaluationMonitoring.setPromptTokens(chatResponse.getPromptTokens());
    evaluationMonitoring.setCompletionTokens(chatResponse.getTurnCompletionTokens());
    evaluationMonitoring.setTotalTokens(chatResponse.getTotalTokens());

    log.info("Saved EvaluationMonitoring");
    return evaluationMonitoringRepository.save(evaluationMonitoring);
  }

  @Transactional
  @Override
  public void deleteEvaluationMonitoringAndRelatedScores(EvaluationContainer container, User user) {
    List<EvaluationMonitoring> evaluationMonitorings =
        evaluationMonitoringRepository.findEvaluationMonitoringByContainerAndUser(container, user);
    deleteEvaluationMonitoringAndRelatedScores(evaluationMonitorings);
  }

  @Transactional
  @Override
  public void deleteEvaluationMonitoringAndRelatedScores(Set<String> monitoringIds, User user) {
    List<EvaluationMonitoring> evaluationMonitorings =
        evaluationMonitoringRepository.findEvaluationMonitroginsByList(monitoringIds, user);
    scoreRepository.deleteByEvaluationMonitoringIn(evaluationMonitorings);
    evaluationMonitoringRepository.deleteAll(evaluationMonitorings);
  }

  @Transactional
  @Override
  public void deleteEvaluationMonitoringAndRelatedScores(
      List<EvaluationMonitoring> evaluationMonitorings) {
    scoreRepository.deleteByEvaluationMonitoringIn(evaluationMonitorings);
    evaluationMonitoringRepository.deleteAll(evaluationMonitorings);
  }

  @Override
  public EvaluationMonitoring saveEvaluationMonitoring(
      User user,
      ChatResponseWithLatency response,
      Model evalModel,
      PairwiseLLMEvaluator llmEvaluator,
      EvaluationContainer container) {
    log.info("Start EvaluationMonitoring for pairwise evaluator");
    EvaluationMonitoring evaluationMonitoring =
        new EvaluationMonitoring(evalModel, user, container, llmEvaluator);
    evaluationMonitoring.setTimeTaken(response.getLatencyMillis());
    evaluationMonitoring.setPromptTokens(response.getPromptTokens());
    evaluationMonitoring.setCompletionTokens(response.getTurnCompletionTokens());
    evaluationMonitoring.setTotalTokens(response.getTotalTokens());
    log.info("Saved EvaluationMonitoring for pairwise evaluator");
    return evaluationMonitoringRepository.save(evaluationMonitoring);
  }
}

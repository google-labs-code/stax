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

package com.planck.planck.domain.inferencemonitoring;

import com.planck.planck.domain.analytics.inference.dto.ProjectInferenceMonitoringSummaryDTO;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InferenceMonitoringServiceImpl implements InferenceMonitoringService {

  @Autowired private InferenceMonitoringRepository inferenceMonitoringRepository;

  public InferenceMonitoring saveInferenceMonitoring(
      User user,
      ChatResponseWithLatency chatResponse,
      Model model,
      Project project,
      InferenceMonitoring previousInferenceMonitoring) {
    InferenceMonitoring inferenceMonitoring =
        new InferenceMonitoring(model, user, project, previousInferenceMonitoring);
    inferenceMonitoring.setTimetaken(chatResponse.getLatencyMillis());

    inferenceMonitoring.setPromptTokens(chatResponse.getPromptTokens());
    inferenceMonitoring.setCompletionTokens(chatResponse.getTurnCompletionTokens());
    inferenceMonitoring.setTotalTokens(chatResponse.getTotalTokens());

    return inferenceMonitoringRepository.save(inferenceMonitoring);
  }

  @Override
  public InferenceMonitoring save(InferenceMonitoring inferenceMonitoring) {
    return inferenceMonitoringRepository.save(inferenceMonitoring);
  }

  @Override
  public void clearEvaluatiionContainerReference(EvaluationContainer container) {
    inferenceMonitoringRepository.clearEvaluationContainerReference(container);
  }

  @Override
  public ProjectInferenceMonitoringSummaryDTO getProjectInferenceMonitoringSummary(
      User user, String projectId) {
    return inferenceMonitoringRepository.getProjectInferenceMonitoringSummary(projectId, user);
  }
}

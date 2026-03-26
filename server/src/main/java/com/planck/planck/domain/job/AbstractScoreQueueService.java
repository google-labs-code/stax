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

package com.planck.planck.domain.job;

import com.planck.planck.domain.evaluation.dto.EvaluationDTO;
import com.planck.planck.domain.evaluation.serivce.EvaluationService;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusService;
import com.planck.planck.domain.evaluator.heuristic.service.PointwiseHeuristicEvaluatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public abstract class AbstractScoreQueueService {

  @Autowired protected ScoreStatusService scoreStatusService;

  @Autowired protected JobStatusService jobStatusService;

  @Autowired protected EvaluationStatusService evaluationStatusService;

  @Lazy @Autowired protected EvaluationService evaluationService;

  @Lazy @Autowired protected PointwiseHeuristicEvaluatorService heuristicEvaluatorService;

  protected void trackJobStatus(EvaluationDTO evaluationDTO) {
    jobStatusService.trackJobStatusCommon(
        evaluationDTO.getJobId(),
        id ->
            evaluationStatusService.findAllByUserAndJobId(
                evaluationDTO.getJobId(), evaluationDTO.getUserId()));
  }
}

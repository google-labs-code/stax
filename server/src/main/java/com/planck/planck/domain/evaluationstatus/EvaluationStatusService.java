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

package com.planck.planck.domain.evaluationstatus;

import com.planck.planck.domain.evaluation.dto.EvaluationDTO;
import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationStatusEnum;
import java.util.Map;

public interface EvaluationStatusService {

  EvaluationStatus createEvaluationStatus(
      String evaluatorId, String chatTurnId, String userId, String jobId);

  void updateEvaluationStatus(
      EvaluationDTO EvaluationDTO, EvaluationStatusEnum evaluatorStatusEnum, String comment);

  int updateEvaluationStatus(
      EvaluationStatusEnum newEvaluationStatus,
      String jobId,
      String comment,
      EvaluationStatusEnum oldEvaluationStatus);

  Map<String, Integer> findAllByUserAndJobId(String jobId, String userId);

  EvaluationStatus findById(String evaluationStatusId);

  void stopEvaluation(String evaluationStatusId, String projectId, User user);

  void updateEvaluationStatus(
      EvaluationStatus evaluationStatus, EvaluationStatusEnum inProgress, String comment);
}

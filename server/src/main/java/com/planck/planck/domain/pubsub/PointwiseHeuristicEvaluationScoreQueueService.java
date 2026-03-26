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

package com.planck.planck.domain.pubsub;

import com.planck.planck.annotation.CheckJobActive;
import com.planck.planck.domain.evaluation.dto.EvaluationDTO;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusService;
import com.planck.planck.domain.job.AbstractScoreQueueService;
import com.planck.planck.domain.user.UserService;
import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationStatusEnum;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.QueueConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Service;

@Slf4j
@Service("pointwiseHeuristicEvaluationScoreQueueService")
public class PointwiseHeuristicEvaluationScoreQueueService extends AbstractScoreQueueService
    implements IScoreService {

  @Autowired private EvaluationStatusService evaluationStatusService;

  @Autowired private PubsubOutboundEvalGateway messagingEvalGateway;

  @Autowired private UserService userService;

  @MessagingGateway
  public interface PubsubOutboundEvalGateway {
    @Gateway(requestChannel = QueueConstants.HEURISTIC_EVAL_PUB_CHANNEL)
    void sendToHeuristicEval(String evaluationDTOJson);

    @Gateway(requestChannel = QueueConstants.HEURISTIC_EVAL_PUB_CHANNEL_BULK)
    void sendToHeuristicEvalBulk(String evaluationDTOJson);
  }

  @Override
  @CheckJobActive
  public void process(EvaluationDTO evaluationDTO) {
    try {
      EvaluationStatus currentEvaluationStatus =
          evaluationStatusService.findById(evaluationDTO.getEvaluationStatusId());

      if (EvaluationStatusEnum.getEvaluationStatusEnumByKey(currentEvaluationStatus.getStatus())
          == EvaluationStatusEnum.STOPPED) {
        log.info(
            "Skipping processing for Heuristic Evaluation (id: {}), because its status is STOPPED.",
            currentEvaluationStatus.getId());
        return;
      }

      evaluationStatusService.updateEvaluationStatus(
          evaluationDTO, EvaluationStatusEnum.IN_PROGRESS, "Heuristic evaluation in-progress.");

      User user = userService.findByUserId(evaluationDTO.getUserId());
      evaluationService.runPointwiseHeuristicEval(
          evaluationDTO.getEvaluatorId(),
          user,
          evaluationDTO.getChatTurnId(),
          currentEvaluationStatus);

      evaluationStatusService.updateEvaluationStatus(
          evaluationDTO,
          EvaluationStatusEnum.SUCCESSFUL,
          "Heuristic evaluation successfully done.");
      log.info(
          "Heuristic evaluation successfully completed. Chat turn Id: {} and Evaluation Status Id:{}",
          evaluationDTO.getChatTurnId(),
          evaluationDTO.getEvaluationStatusId());

      trackJobStatus(evaluationDTO);
    } catch (Exception e) {
      log.error("Heuristic evaluation failed due to Exception: {}", e.getMessage());
      evaluationStatusService.updateEvaluationStatus(
          evaluationDTO,
          EvaluationStatusEnum.FAILED,
          "Heuristic evaluation error: " + e.getMessage());
      trackJobStatus(evaluationDTO);
    }
  }

  public void sendToQueue(EvaluationDTO evaluationDTO, boolean isBulk) {
    String evaluationDTOJson = ObjectMapperUtil.toJsonString(evaluationDTO);

    if (isBulk) {
      this.messagingEvalGateway.sendToHeuristicEvalBulk(evaluationDTOJson);
    } else {
      this.messagingEvalGateway.sendToHeuristicEval(evaluationDTOJson);
    }
  }
}

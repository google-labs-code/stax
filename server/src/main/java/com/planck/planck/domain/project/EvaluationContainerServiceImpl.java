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

package com.planck.planck.domain.project;

import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.evaluationmonitoring.EvaluationMonitoringService;
import com.planck.planck.domain.inferencemonitoring.InferenceMonitoringService;
import com.planck.planck.domain.job.JobStatusService;
import com.planck.planck.domain.modelresponse.service.ModelResponseService;
import com.planck.planck.entitities.*;
import com.planck.planck.exceptions.NotFoundException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class EvaluationContainerServiceImpl implements EvaluationContainerService {

  @Autowired private EvaluationContainerRepository containerRepository;
  @Autowired private ChatService chatService;
  @Autowired private JobStatusService jobStatusService;
  @Autowired private EvaluationMonitoringService evaluationMonitoringService;
  @Autowired private InferenceMonitoringService inferenceMonitoringService;
  @Autowired private ModelResponseService modelResponseService;

  @Override
  public <T extends EvaluationContainer> T getContainerForUser(
      User user, String containerId, Class<T> type) {
    EvaluationContainer container =
        containerRepository
            .findById(containerId)
            .orElseThrow(() -> new NotFoundException("Container not found for id: " + containerId));

    if (!container.getUser().getId().equals(user.getId())) {
      throw new SecurityException("User does not have access to this container.");
    }
    if (!type.isInstance(container)) {
      throw new IllegalArgumentException(
          "Container is not of expected type " + type.getSimpleName());
    }
    return type.cast(container);
  }

  @Override
  public EvaluationContainer getContainerForUser(User user, String containerId) {
    return containerRepository
        .findByIdAndUser(containerId, user)
        .orElseThrow(() -> new NotFoundException("Container not found for id: " + containerId));
  }

  @Override
  @Transactional
  public void deleteContainer(User user, String containerId) {
    EvaluationContainer container =
        getContainerForUser(user, containerId, EvaluationContainer.class);
    deleteContainer(container);
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  @Override
  public void deleteContainer(EvaluationContainer container) {

    List<ChatTurn> chatTurnsToBeDeleted =
        chatService.findAllByUserAndChatContainer(container.getUser(), container);

    chatService.delete(chatTurnsToBeDeleted);

    // There might be orphan chats, without any turns, which were not deleted from above
    List<Chat> chatsToBeDeleted = chatService.getAllChatsByContainer(container);
    if (!chatsToBeDeleted.isEmpty()) {
      chatService.deleteChats(chatsToBeDeleted);
    }

    jobStatusService.deleteByProjectIdAndUserId(container.getId(), container.getUser());
    modelResponseService.deleteByContainer(container);
    inferenceMonitoringService.clearEvaluatiionContainerReference(container);

    // Temporary, should be removed once schema can be changed to keep evaluation monitoring with
    // nullable projectId.
    evaluationMonitoringService.deleteEvaluationMonitoringAndRelatedScores(
        container, container.getUser());

    Long numDeleted = containerRepository.deleteByUserAndId(container.getUser(), container.getId());
    if (numDeleted != 1) {
      throw new NotFoundException(container.getId() + " can't be found");
    }
  }
}

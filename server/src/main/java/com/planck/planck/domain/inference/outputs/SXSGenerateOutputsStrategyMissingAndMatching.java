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

package com.planck.planck.domain.inference.outputs;

import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.exceptions.IllegalInputException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SXSGenerateOutputsStrategyMissingAndMatching extends SXSGenerateOutputsStrategy {

  public SXSGenerateOutputsStrategyMissingAndMatching(Model modelA, Model modelB, Project project) {
    super(modelA, modelB, project);
  }

  @Override
  protected void validateInputs() {
    super.validateInputs();
    if (modelA == null && modelB == null) {
      throw new IllegalInputException("Model A or Model B is required");
    }
  }

  @Override
  protected void duplicateChats() {
    return;
  }

  @Override
  protected void sendToQueue() {
    QueueData queueData = collectChatTurnsForQueue();

    if (queueData.hasChatTurns()) {
      JobStatus job = createJobStatus(queueData.getTotalCount());
      boolean isBulk = queueData.getTotalCount() > 10;

      sendNewModelInferences(
          queueData.getChatTurnsForNewModel(), queueData.getTurnToModelId(), job, isBulk);
      sendExistingModelInferences(queueData.getChatTurnsWithExistingModel(), job);
    }
  }

  private QueueData collectChatTurnsForQueue() {
    List<ChatTurn> chatTurnsForNewModel = new ArrayList<>();
    List<ChatTurn> chatTurnsWithExistingModel = new ArrayList<>();
    Map<String, String> turnToModelId = new HashMap<>();

    for (SxsEvaluationPair sxsPair : sxsPairs) {
      if (!isPairFailed(sxsPair)) {
        processSideA(sxsPair, chatTurnsForNewModel, chatTurnsWithExistingModel, turnToModelId);
        processSideB(sxsPair, chatTurnsForNewModel, chatTurnsWithExistingModel, turnToModelId);
      }
    }

    return new QueueData(chatTurnsForNewModel, chatTurnsWithExistingModel, turnToModelId);
  }

  // sendNewModelInferences and sendExistingModelInferences are now inherited from base class

  private void processSideA(
      SxsEvaluationPair sxsPair,
      List<ChatTurn> chatTurnsForNewModel,
      List<ChatTurn> chatTurnsWithExistingModel,
      Map<String, String> turnToModelId) {
    if (modelA == null) {
      return;
    }

    ChatTurn latestTurn = sxsPair.getChatA().getLatestTurn();
    ModelResponse modelResponse = latestTurn.getModelResponse();

    if (shouldSendToQueue(modelResponse, modelA)) {
      if (modelResponse == null || modelResponse.getText() == null) {
        chatTurnsForNewModel.add(latestTurn);
        turnToModelId.put(latestTurn.getId(), modelA.getId());
      } else {
        chatTurnsWithExistingModel.add(latestTurn);
      }
    } else {
      skippedSxsPairIds.add(sxsPair.getId());
    }
  }

  private void processSideB(
      SxsEvaluationPair sxsPair,
      List<ChatTurn> chatTurnsForNewModel,
      List<ChatTurn> chatTurnsWithExistingModel,
      Map<String, String> turnToModelId) {
    if (modelB == null) {
      return;
    }

    ChatTurn latestTurn = sxsPair.getChatB().getLatestTurn();
    ModelResponse modelResponse = latestTurn.getModelResponse();

    if (shouldSendToQueue(modelResponse, modelB)) {
      if (modelResponse == null || modelResponse.getText() == null) {
        chatTurnsForNewModel.add(latestTurn);
        turnToModelId.put(latestTurn.getId(), modelB.getId());
      } else {
        chatTurnsWithExistingModel.add(latestTurn);
      }
    } else {
      skippedSxsPairIds.add(sxsPair.getId());
    }
  }

  private boolean shouldSendToQueue(ModelResponse modelResponse, Model expectedModel) {
    if (modelResponse == null || modelResponse.getText() == null) {
      return true;
    }

    if (modelResponse.getModel() != null
        && modelResponse.getModel().getId().equals(expectedModel.getId())) {
      return true;
    }

    return false;
  }
}

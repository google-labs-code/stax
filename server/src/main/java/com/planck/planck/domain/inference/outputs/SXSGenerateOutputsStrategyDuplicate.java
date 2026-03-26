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

import com.planck.planck.entitities.Chat;
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
import java.util.stream.Collectors;

public class SXSGenerateOutputsStrategyDuplicate extends SXSGenerateOutputsStrategy {

  public SXSGenerateOutputsStrategyDuplicate(Model modelA, Model modelB, Project project) {
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
    List<SxsEvaluationPair> pairsToDuplicate =
        SXSChatDuplicationUtils.findPairsWithModelMismatch(
            sxsPairs, modelA, modelB, failedSxsPairIds);
    if (!pairsToDuplicate.isEmpty()) {
      List<SxsEvaluationPair> newPairs =
          SXSChatDuplicationUtils.createNewPairsForMismatches(
              pairsToDuplicate, getChatService(), getSxsEvaluationPairRepository(), user);
      newSxsPairIds.addAll(
          newPairs.stream().map(SxsEvaluationPair::getId).collect(Collectors.toList()));
      sxsPairs.addAll(newPairs);
    }
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
    List<ChatTurn> chatTurnsToClear = new ArrayList<>();

    for (SxsEvaluationPair sxsPair : sxsPairs) {
      if (!isPairFailed(sxsPair)) {
        processSide(
            sxsPair.getChatA(), modelA, chatTurnsForNewModel, turnToModelId, chatTurnsToClear);
        processSide(
            sxsPair.getChatB(), modelB, chatTurnsForNewModel, turnToModelId, chatTurnsToClear);
      }
    }

    if (!chatTurnsToClear.isEmpty()) {
      clearModelOutputs(chatTurnsToClear);
    }

    return new QueueData(chatTurnsForNewModel, chatTurnsWithExistingModel, turnToModelId);
  }

  private void processSide(
      Chat chat,
      Model model,
      List<ChatTurn> chatTurnsForNewModel,
      Map<String, String> turnToModelId,
      List<ChatTurn> chatTurnsToClear) {
    if (model == null || chat == null) {
      return;
    }

    ChatTurn latestTurn = chat.getLatestTurn();
    ModelResponse modelResponse = latestTurn.getModelResponse();

    if (shouldRunWithNewModel(modelResponse, model)) {
      chatTurnsForNewModel.add(latestTurn);
      turnToModelId.put(latestTurn.getId(), model.getId());
    } else if (shouldClearAndRerun(modelResponse, model)) {
      chatTurnsToClear.add(latestTurn);
      chatTurnsForNewModel.add(latestTurn);
      turnToModelId.put(latestTurn.getId(), model.getId());
    }
  }

  private boolean shouldRunWithNewModel(ModelResponse modelResponse, Model expectedModel) {
    return modelResponse == null || modelResponse.getModel() == null;
  }

  private boolean shouldClearAndRerun(ModelResponse modelResponse, Model expectedModel) {
    return modelResponse != null
        && modelResponse.getModel() != null
        && modelResponse.getModel().getId().equals(expectedModel.getId());
  }
}

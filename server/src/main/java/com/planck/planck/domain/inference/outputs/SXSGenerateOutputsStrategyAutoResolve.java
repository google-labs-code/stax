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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

public class SXSGenerateOutputsStrategyAutoResolve extends SXSGenerateOutputsStrategy {

  public SXSGenerateOutputsStrategyAutoResolve(Model modelA, Model modelB, Project project) {
    super(modelA, modelB, project);
  }

  @Override
  protected void validateInputs() {
    super.validateInputs();
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
            sxsPair.getChatA(),
            modelA,
            chatTurnsForNewModel,
            chatTurnsWithExistingModel,
            turnToModelId,
            chatTurnsToClear);
        processSide(
            sxsPair.getChatB(),
            modelB,
            chatTurnsForNewModel,
            chatTurnsWithExistingModel,
            turnToModelId,
            chatTurnsToClear);
      }
    }

    if (!chatTurnsToClear.isEmpty()) {
      clearModelOutputs(chatTurnsToClear);
    }

    return new QueueData(chatTurnsForNewModel, chatTurnsWithExistingModel, turnToModelId);
  }

  private void processSide(
      Chat chat,
      Model inputModel,
      List<ChatTurn> chatTurnsForNewModel,
      List<ChatTurn> chatTurnsWithExistingModel,
      Map<String, String> turnToModelId,
      List<ChatTurn> chatTurnsToClear) {

    if (chat == null) {
      return;
    }

    ChatTurn latestTurn = chat.getLatestTurn();
    ModelResponse modelResponse = latestTurn.getModelResponse();

    if (inputModel == null) {
      if (modelResponse != null
          && modelResponse.getModel() != null
          && !StringUtils.hasText(modelResponse.getText())) {
        chatTurnsWithExistingModel.add(latestTurn);
      }
      return;
    }

    if (modelResponse == null) {
      addToNewModelQueue(latestTurn, inputModel, chatTurnsForNewModel, turnToModelId);
      return;
    }

    if (modelResponse.getModel() == null) {
      addToNewModelQueue(latestTurn, inputModel, chatTurnsForNewModel, turnToModelId);
      return;
    }

    Model existingModel = modelResponse.getModel();

    if (isModelMatch(existingModel, inputModel)) {
      addToClearAndRerunQueue(
          latestTurn, inputModel, chatTurnsForNewModel, turnToModelId, chatTurnsToClear);
      return;
    }

    addToNewModelQueue(latestTurn, inputModel, chatTurnsForNewModel, turnToModelId);
  }

  private boolean isModelMatch(Model existingModel, Model inputModel) {
    return inputModel != null && existingModel.getId().equals(inputModel.getId());
  }

  private void addToNewModelQueue(
      ChatTurn chatTurn,
      Model model,
      List<ChatTurn> chatTurnsForNewModel,
      Map<String, String> turnToModelId) {
    chatTurnsForNewModel.add(chatTurn);
    turnToModelId.put(chatTurn.getId(), model.getId());
  }

  private void addToClearAndRerunQueue(
      ChatTurn chatTurn,
      Model model,
      List<ChatTurn> chatTurnsForNewModel,
      Map<String, String> turnToModelId,
      List<ChatTurn> chatTurnsToClear) {
    chatTurnsToClear.add(chatTurn);
    chatTurnsForNewModel.add(chatTurn);
    turnToModelId.put(chatTurn.getId(), model.getId());
  }
}

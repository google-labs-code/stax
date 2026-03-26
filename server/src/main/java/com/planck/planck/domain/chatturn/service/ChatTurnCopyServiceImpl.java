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

package com.planck.planck.domain.chatturn.service;

import com.planck.planck.domain.evaluation.HumanEvalScoreRepository;
import com.planck.planck.domain.evaluation.ScoreV2Repository;
import com.planck.planck.domain.evaluationmonitoring.EvaluationMonitoringRepository;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusRepository;
import com.planck.planck.domain.inferencemonitoring.InferenceMonitoringRepository;
import com.planck.planck.domain.modelinput.ModelInputRepository;
import com.planck.planck.domain.modelresponse.ModelResponseRepository;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationMonitoring;
import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.HumanEvalScore;
import com.planck.planck.entitities.InferenceMonitoring;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.ScoreV2;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.TagLinkTargetType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatTurnCopyServiceImpl implements ChatTurnCopyService {
  @Autowired private InferenceMonitoringRepository inferenceMonitoringRepository;

  @Autowired private EvaluationMonitoringRepository evaluationMonitoringRepository;

  @Autowired private EvaluationStatusRepository evaluationStatusRepository;

  @Autowired private ScoreV2Repository scoreV2Repository;

  @Autowired private ModelResponseRepository modelResponseRepository;

  @Autowired private ModelInputRepository modelInputRepository;

  @Autowired private HumanEvalScoreRepository humanEvalScoreRepository;

  @Autowired private TagLinkService tagLinkService;

  @Override
  public void copyAllChatTurnRelatedEntities(
      Map<ChatTurn, ChatTurn> sourceToTargetTurnMap,
      User user,
      Map<String, List<TagLink>> tagsByTurnId) {

    List<ModelInput> allModelInputsToSave = new ArrayList<>();
    List<ModelResponse> allModelResponsesToSave = new ArrayList<>();
    List<ScoreV2> allScoresToSave = new ArrayList<>();
    List<EvaluationMonitoring> allMonitoringsToSave = new ArrayList<>();
    List<EvaluationStatus> allStatusesToSave = new ArrayList<>();
    List<InferenceMonitoring> allInferencesToSave = new ArrayList<>();
    List<HumanEvalScore> allHumanEvalScoresToSave = new ArrayList<>();
    List<TagLink> allTagLinksToSave = new ArrayList<>();

    for (Map.Entry<ChatTurn, ChatTurn> entry : sourceToTargetTurnMap.entrySet()) {
      ChatTurn originalTurn = entry.getKey();
      ChatTurn targetTurn = entry.getValue();

      copyModelInputs(originalTurn, targetTurn, allModelInputsToSave);
      copyModelResponses(
          originalTurn,
          targetTurn,
          user,
          allModelResponsesToSave,
          allHumanEvalScoresToSave,
          allInferencesToSave,
          allScoresToSave,
          allMonitoringsToSave,
          allStatusesToSave);
      List<TagLink> tagsForThisTurn = tagsByTurnId.get(originalTurn.getId());
      copyTags(targetTurn, tagsForThisTurn, allTagLinksToSave);
    }

    modelInputRepository.saveAll(allModelInputsToSave);
    modelResponseRepository.saveAll(allModelResponsesToSave);
    inferenceMonitoringRepository.saveAll(allInferencesToSave);
    humanEvalScoreRepository.saveAll(allHumanEvalScoresToSave);
    scoreV2Repository.saveAll(allScoresToSave);
    evaluationMonitoringRepository.saveAll(allMonitoringsToSave);
    evaluationStatusRepository.saveAll(allStatusesToSave);
    tagLinkService.saveAll(allTagLinksToSave);
  }

  private void copyTags(
      ChatTurn targetTurn, List<TagLink> originalTagLinks, List<TagLink> allTagLinksToSave) {

    if (originalTagLinks == null || originalTagLinks.isEmpty()) {
      return;
    }
    for (TagLink originalLink : originalTagLinks) {
      TagLink newLink = new TagLink();
      newLink.setTag(originalLink.getTag());
      newLink.setUser(originalLink.getUser());
      newLink.setTargetType(TagLinkTargetType.CHAT_TURN);
      newLink.setTargetId(targetTurn.getId());

      allTagLinksToSave.add(newLink);
    }
  }

  private void copyModelInputs(
      ChatTurn originalTurn, ChatTurn targetTurn, List<ModelInput> allModelInputsToSave) {
    if (originalTurn.getInputs() == null) return;

    List<ModelInput> newInputsForTurn = new ArrayList<>();
    for (ModelInput originalInput : originalTurn.getInputs()) {
      ModelInput copiedInput = originalInput.deepCopy();
      newInputsForTurn.add(copiedInput);
    }
    targetTurn.setInputs(newInputsForTurn);
    allModelInputsToSave.addAll(newInputsForTurn);
  }

  private void copyModelResponses(
      ChatTurn originalTurn,
      ChatTurn targetTurn,
      User user,
      List<ModelResponse> allModelResponsesToSave,
      List<HumanEvalScore> allHumanEvalScoresToSave,
      List<InferenceMonitoring> allInferencesToSave,
      List<ScoreV2> allScoresToSave,
      List<EvaluationMonitoring> allMonitoringsToSave,
      List<EvaluationStatus> allStatusesToSave) {

    ModelResponse originalResponse = originalTurn.getModelResponse();
    if (originalResponse == null) return;

    ModelResponse newResponse = originalResponse.deepCopy(targetTurn.getChat().getContainer());
    targetTurn.setModelResponse(newResponse);
    allModelResponsesToSave.add(newResponse);

    copyHumanEvalScores(originalResponse, newResponse, allHumanEvalScoresToSave);
    copyInferenceMonitoring(originalResponse, newResponse, user, targetTurn, allInferencesToSave);
    copyScoresAndEvaluations(
        originalResponse,
        newResponse,
        user,
        targetTurn,
        allScoresToSave,
        allMonitoringsToSave,
        allStatusesToSave);
  }

  private void copyHumanEvalScores(
      ModelResponse originalResponse,
      ModelResponse newResponse,
      List<HumanEvalScore> allHumanEvalScoresToSave) {
    if (originalResponse.getHumanEvalScores() == null
        || originalResponse.getHumanEvalScores().isEmpty()) {
      return;
    }

    List<HumanEvalScore> newScores = new ArrayList<>();
    for (HumanEvalScore originalScore : originalResponse.getHumanEvalScores()) {
      HumanEvalScore newScore = originalScore.deepCopy(newResponse);
      newScores.add(newScore);
    }
    newResponse.setHumanEvalScores(newScores);
    allHumanEvalScoresToSave.addAll(newScores);
  }

  private void copyInferenceMonitoring(
      ModelResponse originalResponse,
      ModelResponse newResponse,
      User user,
      ChatTurn targetTurn,
      List<InferenceMonitoring> allInferencesToSave) {
    if (originalResponse.getInferenceMonitoring() == null) {
      return;
    }

    InferenceMonitoring newInference =
        originalResponse.getInferenceMonitoring().deepCopy(targetTurn.getChat().getContainer());
    newInference.setUser(user);
    newInference.setContainer(originalResponse.getContainer());
    newResponse.setInferenceMonitoring(newInference);
    allInferencesToSave.add(newInference);
  }

  private void copyScoresAndEvaluations(
      ModelResponse originalResponse,
      ModelResponse newResponse,
      User user,
      ChatTurn targetTurn,
      List<ScoreV2> allScoresToSave,
      List<EvaluationMonitoring> allMonitoringsToSave,
      List<EvaluationStatus> allStatusesToSave) {
    if (originalResponse.getScores() == null || originalResponse.getScores().isEmpty()) {
      return;
    }

    List<ScoreV2> newScoresV2 = new ArrayList<>();
    for (ScoreV2 originalScore : originalResponse.getScores()) {
      ScoreV2 newScore = originalScore.deepCopy(newResponse);
      newScoresV2.add(newScore);

      copyEvaluationMonitoring(originalScore, newScore, targetTurn, allMonitoringsToSave);
      copyEvaluationStatus(originalScore, newScore, user, targetTurn, allStatusesToSave);
    }
    newResponse.setScores(newScoresV2);
    allScoresToSave.addAll(newScoresV2);
  }

  private void copyEvaluationStatus(
      ScoreV2 originalScore,
      ScoreV2 newScore,
      User user,
      ChatTurn targetTurn,
      List<EvaluationStatus> allStatusesToSave) {
    if (originalScore.getEvaluationStatus() != null) {
      EvaluationStatus newStatus = originalScore.getEvaluationStatus().deepCopy();
      if (targetTurn.getChat().getContainer() != null) {
        newStatus.setContainerId(targetTurn.getChat().getContainer().getId());
      }
      newStatus.setUserId(user.getId());
      newStatus.setChatTurnId(targetTurn.getId());
      newScore.setEvaluationStatus(newStatus);
      allStatusesToSave.add(newStatus);
    }
  }

  private void copyEvaluationMonitoring(
      ScoreV2 originalScore,
      ScoreV2 newScore,
      ChatTurn targetTurn,
      List<EvaluationMonitoring> allMonitoringsToSave) {
    if (originalScore.getEvaluationMonitoring() != null
        && originalScore.getLlmEvaluator() != null) {
      EvaluationMonitoring newMonitoring = originalScore.getEvaluationMonitoring().deepCopy();
      newMonitoring.setContainer(targetTurn.getChat().getContainer());
      newMonitoring.setEvaluator(originalScore.getLlmEvaluator());
      newScore.setEvaluationMonitoring(newMonitoring);
      allMonitoringsToSave.add(newMonitoring);
    }
  }
}

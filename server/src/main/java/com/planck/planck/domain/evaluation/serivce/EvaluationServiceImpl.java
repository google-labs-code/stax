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

package com.planck.planck.domain.evaluation.serivce;

import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.evaluation.LLMChoiceOutputResponseDTO;
import com.planck.planck.domain.evaluation.PairwiseScoreRepository;
import com.planck.planck.domain.evaluation.PointwiseHeuristicEvaluationScoreRepository;
import com.planck.planck.domain.evaluation.ScoreV2Repository;
import com.planck.planck.domain.evaluation.SxsEvaluationPairRepository;
import com.planck.planck.domain.evaluation.dto.ChatTurnEvaluationRequest;
import com.planck.planck.domain.evaluation.dto.SXSPairEvaluationRequest;
import com.planck.planck.domain.evaluation.dto.ScorerResponseDTO;
import com.planck.planck.domain.evaluationmonitoring.EvaluationMonitoringService;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusService;
import com.planck.planck.domain.evaluator.dto.EvaluatorVariableDTO;
import com.planck.planck.domain.evaluator.heuristic.PointwiseHeuristicEvaluatorRepository;
import com.planck.planck.domain.evaluator.llm.LLMEvaluatorRepository;
import com.planck.planck.domain.inference.service.InferenceService;
import com.planck.planck.domain.project.EvaluationContainerService;
import com.planck.planck.domain.project.dto.SXSChatRowDTO;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.EvaluationMonitoring;
import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.PairwiseLLMEvaluator;
import com.planck.planck.entitities.PairwiseScore;
import com.planck.planck.entitities.PointwiseHeuristicEvaluationScore;
import com.planck.planck.entitities.PointwiseHeuristicEvaluator;
import com.planck.planck.entitities.ScoreV2;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.CriteriaType;
import com.planck.planck.enums.EvaluationStatusEnum;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.InputRole;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import com.planck.planck.util.ObjectMapperUtil;
import com.planck.planck.util.PromptUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class EvaluationServiceImpl implements EvaluationService {

  @Autowired private ScoreV2Repository scoreRepository;

  @Autowired private ChatService chatService;

  @Autowired private LLMEvaluatorRepository llmEvaluatorRepository;

  @Autowired private ChatTurnRepository chatTurnRepository;

  @Autowired private InferenceService inferenceService;

  @Autowired private EvaluationStatusService evaluationStatusService;

  @Autowired EvaluationMonitoringService evaluationMonitoringService;
  @Autowired private TagLinkService tagLinkService;

  @Autowired private EvaluationBatchService evaluationBatchService;

  @Autowired private PointwiseHeuristicEvaluatorRepository heuristicEvaluatorRepository;

  @Autowired private PointwiseHeuristicEvaluationScoreRepository heuristicEvaluationScore;
  @Autowired private SxsEvaluationPairRepository sxsPairRepository;

  @Autowired private PairwiseScoreRepository pairwiseScoreRepository;
  @Autowired private EvaluationContainerService evaluationContainerService;

  @Override
  @Transactional
  public ScorerResponseDTO generateScoresFromProject(
      String projectId, List<String> evaluatorIds, User user) {

    EvaluationContainer container = evaluationContainerService.getContainerForUser(user, projectId);

    if (container.getEvaluationType() == EvaluationType.SXS) {
      List<SxsEvaluationPair> pairs = sxsPairRepository.findByContainer(container);
      List<SXSPairEvaluationRequest> pairRequests = new ArrayList<>();
      for (SxsEvaluationPair pair : pairs) {
        SXSPairEvaluationRequest pairRequest =
            new SXSPairEvaluationRequest(
                pair.getId(), pair.getChatTurnA().getId(), pair.getChatTurnB().getId());
        pairRequests.add(pairRequest);
      }

      String jobId =
          evaluationBatchService.processLists(pairRequests, null, evaluatorIds, projectId, user);
      ScorerResponseDTO response = new ScorerResponseDTO();
      response.setJobId(jobId);
      return response;
    }

    List<String> chatTurnIds =
        chatTurnRepository.findLatestChatTurnIdsByContainerId(projectId, user);

    if (CollectionUtils.isEmpty(chatTurnIds)) {
      throw new IllegalInputException(
          "Project does not have any chat turns. Provide a valid project.");
    }
    String jobId =
        evaluationBatchService.processProject(chatTurnIds, evaluatorIds, projectId, user);

    ScorerResponseDTO response = new ScorerResponseDTO();
    response.setJobId(jobId);
    response.setChatTurnIds(chatTurnIds);
    return response;
  }

  @Override
  public ScorerResponseDTO generateScoresFromDataSet(
      String dataSetId, List<String> evaluatorIds, User user) {
    List<String> chatTurnIds =
        chatTurnRepository.findLatestChatTurnIdsByContainerId(dataSetId, user);

    if (CollectionUtils.isEmpty(chatTurnIds)) {
      throw new IllegalInputException(
          "DataSet does not have any chat turns. Provide a valid DataSet.");
    }

    String jobId =
        evaluationBatchService.processDataSet(chatTurnIds, evaluatorIds, dataSetId, user);
    ScorerResponseDTO response = new ScorerResponseDTO();
    response.setJobId(jobId);
    response.setChatTurnIds(chatTurnIds);
    return response;
  }

  @Override
  @Transactional
  public ScorerResponseDTO generateScoresFromChatTurns(
      List<String> chatTurnIds,
      List<SXSPairEvaluationRequest> pairs,
      List<String> evaluatorIds,
      String projectId,
      User user) {

    String jobId =
        evaluationBatchService.processLists(pairs, chatTurnIds, evaluatorIds, projectId, user);

    ScorerResponseDTO scorerResponseDTO = new ScorerResponseDTO();
    scorerResponseDTO.setJobId(jobId);
    scorerResponseDTO.setChatTurnIds(chatTurnIds);
    log.info(
        "Generate evaluators from chat turns. Number of evaluator IDs: {}, Job ID: {}",
        evaluatorIds != null ? evaluatorIds.size() : 0,
        jobId);
    return scorerResponseDTO;
  }

  @Override
  @Transactional
  public Object generateScore(ChatTurnEvaluationRequest request, String projectId, User user) {
    return generateScore(
        request.getChatTurnId(),
        request.getEvaluatorId(),
        request.getSxsPair() != null ? request.getSxsPair().getId() : null,
        request.getSxsPair() != null ? request.getSxsPair().getChatTurnIdA() : null,
        request.getSxsPair() != null ? request.getSxsPair().getChatTurnIdB() : null,
        projectId,
        user);
  }

  @Override
  @Transactional
  public Object generateScore(
      String chatTurnId,
      String evaluatorId,
      String pairId,
      String chatTurnIdA,
      String chatTurnIdB,
      String projectId,
      User user) {

    String effectiveChatTurnId = chatTurnIdA != null ? chatTurnIdA : chatTurnId;

    EvaluationStatus evaluationStatus =
        evaluationStatusService.createEvaluationStatus(
            evaluatorId, effectiveChatTurnId, user.getId(), projectId);

    return generateScore(
        pairId, effectiveChatTurnId, chatTurnIdB, evaluatorId, projectId, evaluationStatus, user);
  }

  @Override
  @Transactional
  public Object generateScore(
      String pairId,
      String chatTurnIdA,
      String chatTurnIdB,
      String evaluatorId,
      String containerId,
      EvaluationStatus evaluationStatus,
      User user) {
    try {
      evaluationStatusService.updateEvaluationStatus(
          evaluationStatus, EvaluationStatusEnum.IN_PROGRESS, "Evaluation in-progress.");

      Object result;
      if (!StringUtils.isEmpty(pairId)) {
        result =
            generateScoreFromSXSChatRow(
                pairId, chatTurnIdA, chatTurnIdB, evaluatorId, containerId, evaluationStatus, user);
      } else {
        result =
            generateScoresFromChatTurn(
                chatTurnIdA, evaluatorId, containerId, evaluationStatus, user);
      }

      evaluationStatusService.updateEvaluationStatus(
          evaluationStatus, EvaluationStatusEnum.SUCCESSFUL, "Evaluation successful.");

      return result;

    } catch (Exception e) {
      log.error("Evaluation failed due to Exception: {}", e.getMessage());
      evaluationStatusService.updateEvaluationStatus(
          evaluationStatus, EvaluationStatusEnum.FAILED, "Evaluation error: " + e.getMessage());
      throw e;
    }
  }

  private SXSChatRowDTO generateScoreFromSXSChatRow(
      String pairId,
      String chatTurnAId,
      String chatTurnBId,
      String evaluatorId,
      String projectId,
      EvaluationStatus evaluationStatus,
      User user) {

    SxsEvaluationPair pair =
        sxsPairRepository
            .findByIdAndUser(pairId, user)
            .orElseThrow(
                () -> new NotFoundException("SXS Evaluation Pair not found with id: " + pairId));

    ChatTurn chatTurnA;
    ChatTurn chatTurnB;
    if (StringUtils.isEmpty(chatTurnAId) || StringUtils.isEmpty(chatTurnBId)) {
      chatTurnA = pair.getChatTurnA();
      chatTurnB = pair.getChatTurnB();
    } else {
      chatTurnA = chatTurnRepository.findByChatTurnIdAndUserId(chatTurnAId, user);
      if (chatTurnA == null) {
        throw new NotFoundException("Chat Turn A not found with id: " + chatTurnAId);
      }
      chatTurnB = chatTurnRepository.findByChatTurnIdAndUserId(chatTurnBId, user);
      if (chatTurnB == null) {
        throw new NotFoundException("Chat Turn B not found with id: " + chatTurnBId);
      }
    }
    if (chatTurnA.getModelResponse() == null || chatTurnB.getModelResponse() == null) {
      throw new IllegalInputException(
          "Both Chat Turns must have Model Responses to run evaluation.");
    }

    runSXSEval(evaluatorId, user, pair, chatTurnA, chatTurnB, evaluationStatus);

    return new SXSChatRowDTO(pair);
  }

  private ChatTurnDTO generateScoresFromChatTurn(
      String chatTurnId,
      String evaluatorId,
      String projectId,
      EvaluationStatus evaluationStatus,
      User user) {
    ChatTurn chatTurn = chatTurnRepository.findByChatTurnIdAndUserId(chatTurnId, user);
    if (chatTurn == null) {
      throw new NotFoundException(
          "Cannot generate scores because ChatTurn not found with chatTurnId: " + chatTurnId);
    }

    ModelResponse modelResponse = chatTurn.getModelResponse();
    if (modelResponse == null) {
      throw new IllegalInputException(
          "Cannot generate scores because ModelResponse is missing for ChatTurn with id: "
              + chatTurnId);
    }

    if (llmEvaluatorRepository
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, user)
        .isPresent()) {
      runPointwiseLLMEval(evaluatorId, chatTurnId, evaluationStatus, user);
    } else {
      runPointwiseHeuristicEval(evaluatorId, user, chatTurnId, evaluationStatus);
    }

    return new ChatTurnDTO(chatTurnRepository.findByChatTurnIdAndUserId(chatTurnId, user));
  }

  @Override
  @Transactional
  public void runSxsEvalFromQueue(
      String evaluatorId,
      String pairId,
      String chatTurnIdA,
      String chatTurnIdB,
      User user,
      EvaluationStatus evaluationStatus) {
    SxsEvaluationPair pair =
        sxsPairRepository
            .findByIdAndUser(pairId, user)
            .orElseThrow(
                () -> new NotFoundException("SXS Evaluation Pair not found with id: " + pairId));

    ChatTurn chatTurnA = retrieveChatTurn(chatTurnIdA, user);
    ChatTurn chatTurnB = retrieveChatTurn(chatTurnIdB, user);

    runSXSEval(evaluatorId, user, pair, chatTurnA, chatTurnB, evaluationStatus);
  }

  private ScoreV2 runPointwiseLLMEval(
      String evaluatorId, String chatTurnId, EvaluationStatus evaluationStatus, User user) {
    ChatTurn chatTurn = retrieveChatTurn(chatTurnId, user);
    ModelResponse modelResponse = retrieveModelResponse(chatTurn, chatTurnId);

    LLMEvaluator llmEvaluator = getLLMEvaluator(evaluatorId, user);

    List<ScoreV2> scoresFromModelResponse = modelResponse.getScores();
    List<ChatTurn> chat = chatService.getChat(chatTurn.getChat().getId(), user);
    ScoreV2 existingScore = findScoreInChatTurnList(scoresFromModelResponse, evaluatorId);

    ScoreV2 currentScore;
    if (existingScore != null) {
      currentScore = existingScore;
    } else {
      currentScore = new ScoreV2();
      currentScore.setUserId(user.getId());
      currentScore.setModelResponse(modelResponse);
      currentScore.setLlmEvaluator(llmEvaluator);
      currentScore.setEvaluationStatus(evaluationStatus);
      currentScore.setScoreType(llmEvaluator.getOutputFormatType());
      currentScore.setScorer(llmEvaluator.getName());
    }

    Model evalModel = llmEvaluator.getModel();
    List<ModelInput> inputs =
        createPointwisePromptInputs(llmEvaluator.getVariables(), llmEvaluator.getInputs(), chat);
    ChatResponseWithLatency response =
        inferenceService.runEvaluationInference(user, evalModel, inputs);
    EvaluationMonitoring evaluationMonitoring =
        evaluationMonitoringService.saveEvaluationMonitoring(
            user, response, evalModel, llmEvaluator, chatTurn.getChat().getContainer());

    LLMChoiceOutputResponseDTO choiceOutputResponseDTO =
        new LLMChoiceOutputResponseDTO(response.getReply(), llmEvaluator.getOutputCategories());

    currentScore.setScore(Double.valueOf(choiceOutputResponseDTO.getScore()));
    currentScore.setLlmResponse(response.getReply());
    currentScore.setEvaluationMonitoring(evaluationMonitoring);
    modelResponse.getScores().add(currentScore);
    ScoreV2 savedScore = scoreRepository.save(currentScore);
    tagLinkService.syncEvaluationMonitoringTags(savedScore.getEvaluationMonitoring().getId(), user);
    return savedScore;
  }

  @Override
  public PointwiseHeuristicEvaluationScore runPointwiseHeuristicEval(
      String evaluatorId, User user, String chatTurnId, EvaluationStatus evaluationStatus) {

    ChatTurn chatTurn = retrieveChatTurn(chatTurnId, user);
    ModelResponse modelResponse = retrieveModelResponse(chatTurn, chatTurnId);
    PointwiseHeuristicEvaluator evaluator = retrieveEvaluator(evaluatorId, user);

    PointwiseHeuristicEvaluationScore currentScore =
        findOrCreateScore(modelResponse, evaluatorId, evaluator, evaluationStatus, user);

    applyEvaluationLogic(currentScore, evaluator, modelResponse.getText());

    modelResponse.getPointwiseHeuristicEvaluationScores().add(currentScore);
    return heuristicEvaluationScore.save(currentScore);
  }

  public PairwiseScore runSXSEval(
      String evaluatorId,
      User user,
      SxsEvaluationPair pair,
      ChatTurn chatTurnA,
      ChatTurn chatTurnB,
      EvaluationStatus evaluationStatus) {

    if (chatTurnA == null || chatTurnB == null) {
      throw new NotFoundException(
          "No chat turn found to run pairwise evaluation with evaluatorId: " + evaluatorId);
    }

    PairwiseLLMEvaluator llmEvaluator = getPairwiseLLMEvaluator(evaluatorId, user);

    List<ChatTurn> chatA = chatService.getChat(chatTurnA.getChat().getId(), user);
    List<ChatTurn> filteredChatA = getChatUpToTurn(chatA, chatTurnA);
    List<ChatTurn> chatB = chatService.getChat(chatTurnB.getChat().getId(), user);
    List<ChatTurn> filteredChatB = getChatUpToTurn(chatB, chatTurnB);

    Model evalModel = llmEvaluator.getModel();
    List<ModelInput> inputs =
        createPairwisePromptInputs(
            llmEvaluator.getVariables(),
            llmEvaluator.getInputs(),
            pair,
            filteredChatA,
            filteredChatB);

    ChatResponseWithLatency response =
        inferenceService.runEvaluationInference(user, evalModel, inputs);
    EvaluationMonitoring evaluationMonitoring =
        evaluationMonitoringService.saveEvaluationMonitoring(
            user, response, evalModel, llmEvaluator, chatTurnA.getChat().getContainer());

    return processPairwiseEvaluatorResponse(
        user,
        pair,
        chatTurnA,
        chatTurnB,
        evaluationStatus,
        llmEvaluator,
        response,
        evaluationMonitoring);
  }

  private List<ChatTurn> getChatUpToTurn(List<ChatTurn> chat, ChatTurn chatTurn) {
    for (int i = 0; i < chat.size(); i++) {
      if (Objects.equals(chat.get(i).getId(), chatTurn.getId())) {
        return chat.subList(0, i + 1);
      }
    }

    throw new IllegalInputException("Chat turn not found in chat history");
  }

  private PairwiseLLMEvaluator getPairwiseLLMEvaluator(String evaluatorId, User user) {
    Optional<PairwiseLLMEvaluator> optionalLlmEvaluator =
        llmEvaluatorRepository.findPairwiseEvaluatorByIdAndUser(evaluatorId, user);

    if (!optionalLlmEvaluator.isPresent()) {
      throw new NotFoundException(
          "Pairwise LLMEvaluator with id '" + evaluatorId + "' not found for user '");
    }

    PairwiseLLMEvaluator llmEvaluator = optionalLlmEvaluator.get();

    if (llmEvaluator.getOutputFormatType() != com.planck.planck.enums.ScoreType.Choices
        && llmEvaluator.getOutputFormatType() != com.planck.planck.enums.ScoreType.Json) {
      throw new IllegalInputException(
          "Unsupported LLM Evaluator output format type: " + llmEvaluator.getOutputFormatType());
    }
    return llmEvaluator;
  }

  private LLMEvaluator getLLMEvaluator(String evaluatorId, User user) {
    LLMEvaluator llmEvaluator =
        llmEvaluatorRepository
            .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, user)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "LLMEvaluator with id '"
                            + evaluatorId
                            + "' not found for user '"
                            + user.getId()
                            + "'"));

    if (llmEvaluator.getOutputFormatType() != com.planck.planck.enums.ScoreType.Choices
        && llmEvaluator.getOutputFormatType() != com.planck.planck.enums.ScoreType.Json) {
      throw new IllegalInputException(
          "Unsupported LLM Evaluator output format type: " + llmEvaluator.getOutputFormatType());
    }

    return llmEvaluator;
  }

  private PairwiseScore processPairwiseEvaluatorResponse(
      User user,
      SxsEvaluationPair pair,
      ChatTurn chatTurnA,
      ChatTurn chatTurnB,
      EvaluationStatus evaluationStatus,
      PairwiseLLMEvaluator llmEvaluator,
      ChatResponseWithLatency response,
      EvaluationMonitoring evaluationMonitoring) {

    String llmResponse = response.getReply();
    LLMChoiceOutputResponseDTO choiceOutputResponseDTO =
        new LLMChoiceOutputResponseDTO(llmResponse, llmEvaluator.getOutputCategories());

    Optional<PairwiseScore> existingScore =
        pairwiseScoreRepository.findByPairAndChatTurnsAndEvaluatorAndUser(
            pair.getId(), chatTurnA.getId(), chatTurnB.getId(), llmEvaluator.getId(), user.getId());

    PairwiseScore currentScore;
    if (existingScore.isPresent()) {
      currentScore = existingScore.get();
    } else {
      currentScore = new PairwiseScore();
      currentScore.setUserId(user.getId());
      currentScore.setChatTurnA(chatTurnA);
      currentScore.setChatTurnB(chatTurnB);
      currentScore.setPair(pair);
      currentScore.setLlmEvaluator(llmEvaluator);

      currentScore.setScoreType(llmEvaluator.getOutputFormatType());
      currentScore.setScorer(llmEvaluator.getName());
      currentScore.setEvaluationMonitoring(evaluationMonitoring);
      currentScore.setEvaluationStatus(evaluationStatus);
    }

    currentScore.setScore(choiceOutputResponseDTO.getScore());
    currentScore.setLlmResponse(llmResponse);
    return pairwiseScoreRepository.saveAndFlush(currentScore);
  }

  private ChatTurn retrieveChatTurn(String chatTurnId, User user) {
    ChatTurn chatTurn = chatTurnRepository.findByChatTurnIdAndUserId(chatTurnId, user);
    if (chatTurn == null) {
      throw new NotFoundException(
          "No chat turn found to run evaluation with chatTurnId: " + chatTurnId);
    }
    return chatTurn;
  }

  private ModelResponse retrieveModelResponse(ChatTurn chatTurn, String chatTurnId) {
    ModelResponse modelResponse = chatTurn.getModelResponse();
    if (modelResponse == null) {
      throw new IllegalInputException(
          "ChatTurn with id '" + chatTurnId + "' does not have a ModelResponse.");
    }
    return modelResponse;
  }

  private PointwiseHeuristicEvaluator retrieveEvaluator(String evaluatorId, User user) {
    return heuristicEvaluatorRepository
        .findById(evaluatorId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "PointwiseHeuristicEvaluator with id '"
                        + evaluatorId
                        + "' not found for user '"
                        + user.getId()
                        + "'"));
  }

  private PointwiseHeuristicEvaluationScore findOrCreateScore(
      ModelResponse modelResponse,
      String evaluatorId,
      PointwiseHeuristicEvaluator evaluator,
      EvaluationStatus evaluationStatus,
      User user) {

    List<PointwiseHeuristicEvaluationScore> heuristicScores =
        modelResponse.getPointwiseHeuristicEvaluationScores();

    PointwiseHeuristicEvaluationScore existingScore =
        findPointwiseHeuristicScoreInChatTurnList(heuristicScores, evaluatorId);

    if (existingScore != null) {
      return existingScore;
    } else {
      PointwiseHeuristicEvaluationScore newScore = new PointwiseHeuristicEvaluationScore();
      newScore.setPointwiseEvaluator(evaluator);
      newScore.setModelResponse(modelResponse);
      newScore.setEvaluationStatus(evaluationStatus);
      newScore.setUser(user);
      return newScore;
    }
  }

  private void applyEvaluationLogic(
      PointwiseHeuristicEvaluationScore score,
      PointwiseHeuristicEvaluator evaluator,
      String textForEvaluation) {

    if (evaluator.getCriteriaType().equals(CriteriaType.CONTAINS)) {
      score.setMatch(textForEvaluation.contains(evaluator.getCriteria()));
    } else if (evaluator.getCriteriaType().equals(CriteriaType.REGEX)) {
      try {
        Pattern pattern =
            Pattern.compile(evaluator.getCriteria(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(textForEvaluation);
        score.setMatch(matcher.find());
      } catch (PatternSyntaxException e) {
        throw new IllegalInputException(
            "Invalid regex criteria for evaluator ID "
                + evaluator.getId()
                + ": "
                + evaluator.getCriteria());
      }
    }
  }

  private List<ModelInput> createPointwisePromptInputs(
      String variables, List<ModelInput> inputs, List<ChatTurn> chat) {
    return createEvaluatorPromptInputs(variables, inputs, null, chat, null);
  }

  private List<ModelInput> createPairwisePromptInputs(
      String variables,
      List<ModelInput> inputs,
      SxsEvaluationPair pair,
      List<ChatTurn> chatA,
      List<ChatTurn> chatB) {
    return createEvaluatorPromptInputs(variables, inputs, pair, chatA, chatB);
  }

  private List<ModelInput> createEvaluatorPromptInputs(
      String variableStr,
      List<ModelInput> inputs,
      SxsEvaluationPair pair,
      List<ChatTurn> chatA,
      List<ChatTurn> chatB) {
    List<EvaluatorVariableDTO> variableList =
        ObjectMapperUtil.convertJsonStringToList(variableStr, EvaluatorVariableDTO.class);

    if (variableList == null || variableList.isEmpty()) {
      throw new IllegalArgumentException(
          "Evaluator variables must be provided to define the prompt structure.");
    }

    if (pair != null && (chatA == null || chatA.isEmpty() || chatB == null || chatB.isEmpty())) {
      throw new IllegalArgumentException(
          "Both chats must be provided and non-empty for pairwise evaluation.");
    }

    if (chatA == null || chatA.isEmpty()) {
      throw new IllegalArgumentException("Chat A cannot be null or empty for evaluation.");
    }

    Map<String, EvaluatorVariableDTO> variableMap =
        variableList.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(EvaluatorVariableDTO::getName, Function.identity()));

    String expectedOutput =
        pair != null
            ? pair.getExpectedOutput()
            : chatA
                .get(chatA.size() - 1)
                .getInputs()
                .get(chatA.get(chatA.size() - 1).getInputs().size() - 1)
                .getExpectedOutput();

    List<ModelInput> finalInputs = new ArrayList<>();
    for (ModelInput templateInput : inputs) {
      String templateText = templateInput.getText();

      for (EvaluatorVariableDTO variable : variableList) {
        switch (variable.getName().toLowerCase()) {
          case "history":
          case "history.a":
            templateText =
                handleHistoryVariable(templateText, chatA, variable.getName(), variableMap);
            break;
          case "history.b":
            templateText =
                handleHistoryVariable(templateText, chatB, variable.getName(), variableMap);
            break;
          case "prompt":
          case "input":
          case "input.a":
          case "input.b":
            templateText =
                handleInputVariable(templateText, chatA, variable.getName(), variableMap);
            break;
          case "output":
          case "output.a":
            templateText =
                handleOutputVariable(templateText, chatA, variable.getName(), variableMap);
            break;
          case "output.b":
            templateText =
                handleOutputVariable(templateText, chatB, variable.getName(), variableMap);
            break;
          case "system.instruction":
          case "system.instruction.A":
            templateText =
                handleSystemInstructionVariable(
                    templateText, chatA, variable.getName(), variableMap);
            break;
          case "system.instruction.B":
            templateText =
                handleSystemInstructionVariable(
                    templateText, chatB, variable.getName(), variableMap);
            break;
          case "expected_output":
            templateText =
                handleExpectedOutputVariable(
                    templateText, expectedOutput, variable.getName(), variableMap);
            break;
          default:
            log.warn("Unsupported variable in evaluator: {}", variable.getName());
        }
      }

      ModelInput newModelInput = new ModelInput();
      newModelInput.setText(templateText);
      newModelInput.setRole(templateInput.getRole());
      finalInputs.add(newModelInput);
    }
    return finalInputs;
  }

  private String handleExpectedOutputVariable(
      String templateText,
      String expectedOutput,
      String name,
      Map<String, EvaluatorVariableDTO> variableMap) {
    handleMissingVariable(name, variableMap);

    return templateText.replace("{{" + name + "}}", expectedOutput);
  }

  private void handleMissingVariable(String name, Map<String, EvaluatorVariableDTO> variableMap) {
    EvaluatorVariableDTO varDTO = variableMap.get(name);
    if (varDTO != null && varDTO.isRequired()) {
      throw new IllegalArgumentException(
          "Variable '" + name + "' is required could not be extracted.");
    }
  }

  private String handleSystemInstructionVariable(
      String templateText,
      List<ChatTurn> chatA,
      String name,
      Map<String, EvaluatorVariableDTO> variableMap) {
    for (int i = chatA.size() - 1; i >= 0; i--) {
      ChatTurn turn = chatA.get(i);
      if (turn.getInputs() != null && !turn.getInputs().isEmpty()) {
        Optional<String> systemInstructionOpt =
            turn.getInputs().stream()
                .filter(
                    inp ->
                        inp != null && inp.getText() != null && inp.getRole() == InputRole.SYSTEM)
                .map(
                    inp ->
                        PromptUtil.containsVariableInput(inp.getText())
                            ? PromptUtil.enrichText(inp.getText(), inp.getVariables())
                            : inp.getText())
                .filter(text -> !text.isEmpty())
                .findFirst();

        if (systemInstructionOpt.isPresent()) {
          return templateText.replace("{{" + name + "}}", systemInstructionOpt.get());
        }
      }
    }

    handleMissingVariable(name, variableMap);
    return templateText;
  }

  private String handleOutputVariable(
      String templateText,
      List<ChatTurn> chatA,
      String name,
      Map<String, EvaluatorVariableDTO> variableMap) {
    ChatTurn lastTurn = chatA.get(chatA.size() - 1);
    if (lastTurn.getModelResponse() == null || lastTurn.getModelResponse().getText() == null) {
      handleMissingVariable(name, variableMap);
    }

    return templateText.replace("{{" + name + "}}", lastTurn.getModelResponse().getText());
  }

  private String handleInputVariable(
      String templateText,
      List<ChatTurn> chatA,
      String name,
      Map<String, EvaluatorVariableDTO> variableMap) {
    ChatTurn lastTurn = chatA.get(chatA.size() - 1);

    if (lastTurn.getInputs() == null
        || lastTurn.getInputs().isEmpty()
        || lastTurn.getInputs().stream()
            .noneMatch(
                inp -> inp != null && inp.getText() != null && inp.getRole() == InputRole.USER)) {
      handleMissingVariable(name, variableMap);
    }

    String inputString =
        lastTurn.getInputs().stream()
            .filter(inp -> inp != null && inp.getText() != null && inp.getRole() == InputRole.USER)
            .map(
                inp -> {
                  if (PromptUtil.containsVariableInput(inp.getText())) {
                    return PromptUtil.enrichText(inp.getText(), inp.getVariables());
                  }
                  return inp.getText();
                })
            .collect(Collectors.joining("\n"));

    return templateText.replace("{{" + name + "}}", inputString);
  }

  private String handleHistoryVariable(
      String templateText,
      List<ChatTurn> chatA,
      String name,
      Map<String, EvaluatorVariableDTO> variableMap) {
    if (chatA.size() == 1) {
      return templateText.replace("{{" + name + "}}", "N/A");
    }

    StringBuilder historyBuilder = new StringBuilder();
    List<ChatTurn> historyTurns = chatA.subList(0, chatA.size() - 1);
    for (ChatTurn turn : historyTurns) {
      if (turn.getInputs() != null) {
        for (ModelInput modelInput : turn.getInputs()) {
          if (modelInput != null && modelInput.getText() != null) {
            String role =
                (modelInput.getRole() != null) ? modelInput.getRole().name() : "UNKNOWN_ROLE";
            String inputText = modelInput.getText();
            if (PromptUtil.containsVariableInput(inputText)) {
              inputText = PromptUtil.enrichText(inputText, modelInput.getVariables());
            }
            historyBuilder.append(role).append(": ").append(inputText).append("\n");
          }
        }
      }

      ModelResponse modelResponse = turn.getModelResponse();
      if (modelResponse != null && modelResponse.getText() != null) {
        historyBuilder.append("ASSISTANT: ").append(modelResponse.getText()).append("\n");
      }
      historyBuilder.append("\n");
    }

    return templateText.replace("{{" + name + "}}", historyBuilder.toString().trim());
  }

  public PointwiseHeuristicEvaluationScore findPointwiseHeuristicScoreInChatTurnList(
      List<PointwiseHeuristicEvaluationScore> scores, String heuristicEvaluatorId) {
    return findScoreInChatTurnList(
        scores,
        heuristicEvaluatorId,
        s -> s.getPointwiseEvaluator() != null ? s.getPointwiseEvaluator().getId() : null);
  }

  public ScoreV2 findScoreInChatTurnList(List<ScoreV2> scores, String llmEvaluatorId) {
    return findScoreInChatTurnList(
        scores,
        llmEvaluatorId,
        s -> s.getLlmEvaluator() != null ? s.getLlmEvaluator().getId() : null);
  }

  private <T> T findScoreInChatTurnList(
      List<T> scores, String evaluatorId, Function<T, String> evaluatorIdExtractor) {
    if (scores == null) {
      return null;
    }
    return scores.stream()
        .filter(
            score ->
                Objects.nonNull(score) && evaluatorId.equals(evaluatorIdExtractor.apply(score)))
        .findFirst()
        .orElse(null);
  }
}

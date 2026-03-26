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
import com.planck.planck.domain.evaluation.PairwiseScoreRepository;
import com.planck.planck.domain.evaluation.PointwiseHeuristicEvaluationScoreRepository;
import com.planck.planck.domain.evaluation.ScoreV2Repository;
import com.planck.planck.domain.evaluation.SxsEvaluationPairRepository;
import com.planck.planck.domain.evaluation.dto.EvaluationDTO;
import com.planck.planck.domain.evaluation.dto.SXSPairEvaluationRequest;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusRepository;
import com.planck.planck.domain.evaluator.heuristic.PointwiseHeuristicEvaluatorRepository;
import com.planck.planck.domain.evaluator.llm.LLMEvaluatorRepository;
import com.planck.planck.domain.evaluator.pairwise.service.PairwiseLLMEvaluatorService;
import com.planck.planck.domain.job.JobStatusService;
import com.planck.planck.domain.pubsub.LLMScoreQueueService;
import com.planck.planck.domain.pubsub.PointwiseHeuristicEvaluationScoreQueueService;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.EvaluationStatus;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.PairwiseLLMEvaluator;
import com.planck.planck.entitities.PairwiseScore;
import com.planck.planck.entitities.PointwiseHeuristicEvaluationScore;
import com.planck.planck.entitities.PointwiseHeuristicEvaluator;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.ScoreV2;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationStatusEnum;
import com.planck.planck.enums.JobStatusEnum;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.util.PlanckConstants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Slf4j
public class EvaluationBatchServiceImpl implements EvaluationBatchService {
  @Autowired private JobStatusService jobStatusService;
  @Autowired private LLMScoreQueueService llmScoreQueueService;
  @Autowired private ChatService chatService;

  @Autowired private LLMEvaluatorRepository llmEvaluatorRepository;
  @Autowired private PairwiseLLMEvaluatorService pairwiseLLMEvaluatorService;
  @Autowired private EvaluationStatusRepository evaluationStatusRepository;
  @Autowired private ScoreV2Repository scoreV2Repository;
  @Autowired private SxsEvaluationPairRepository sxsPairRepository;
  @Autowired private PairwiseScoreRepository pairwiseScoreRepository;
  @PersistenceContext private EntityManager entityManager;

  @Autowired private PointwiseHeuristicEvaluatorRepository pointwiseHeuristicEvaluatorRepository;

  @Autowired
  private PointwiseHeuristicEvaluationScoreRepository pointwiseHeuristicEvaluationScoreRepository;

  @Autowired
  private PointwiseHeuristicEvaluationScoreQueueService heuristicEvaluationScoreQueueService;

  @Override
  public String processProject(
      List<String> chatTurnIds, List<String> evaluatorIds, String containerId, User user) {

    Map<String, LLMEvaluator> llmEvaluators = findLLMEvaluators(evaluatorIds, user);
    Map<String, PointwiseHeuristicEvaluator> heuristicEvaluators =
        findHeuristicEvaluators(evaluatorIds, user);

    validateEvaluators(evaluatorIds, llmEvaluators.keySet(), heuristicEvaluators.keySet());

    List<String> filteredIds = filterChatTurnIdsWithOutput(chatTurnIds);
    Integer total = filteredIds.size() * (llmEvaluators.size() + heuristicEvaluators.size());
    Project project = entityManager.getReference(Project.class, containerId);

    JobStatus jobStatus = createJobStatus(project, total, user);
    processBatch(filteredIds, llmEvaluators, heuristicEvaluators, jobStatus, user, project.getId());
    return jobStatus.getId();
  }

  @Override
  public String processDataSet(
      List<String> chatTurnIds, List<String> evaluatorIds, String dataSetId, User user) {
    throw new NotImplementedException("Evaluation on data sets is not supported yet");
  }

  @Override
  public String processChatTurns(List<String> chatTurnIds, List<String> evaluatorIds, User user) {

    Map<String, LLMEvaluator> llmEvaluators = findLLMEvaluators(evaluatorIds, user);
    Map<String, PointwiseHeuristicEvaluator> heuristicEvaluators =
        findHeuristicEvaluators(evaluatorIds, user);

    validateEvaluators(evaluatorIds, llmEvaluators.keySet(), heuristicEvaluators.keySet());

    List<String> filteredIds = filterChatTurnIdsWithOutput(chatTurnIds);
    Integer total = filteredIds.size() * (llmEvaluators.size() + heuristicEvaluators.size());
    ChatTurn chatTurn = chatService.getChatTurn(filteredIds.get(0), user);
    Project project = (Project) chatTurn.getChat().getContainer();

    JobStatus jobStatus = createJobStatus(project, total, user);
    processBatch(filteredIds, llmEvaluators, heuristicEvaluators, jobStatus, user, project.getId());
    return jobStatus.getId();
  }

  @Override
  @Transactional
  public String processLists(
      List<SXSPairEvaluationRequest> sxsPairIds,
      List<String> chatTurnIds,
      List<String> evaluatorIds,
      String containerId,
      User user) {
    Map<String, LLMEvaluator> llmEvals = findLLMEvaluators(evaluatorIds, user);
    Map<String, PointwiseHeuristicEvaluator> heuristicEvals =
        findHeuristicEvaluators(evaluatorIds, user);
    Map<String, PairwiseLLMEvaluator> pairwiseEvals = findPairwiseEvaluators(evaluatorIds, user);

    validateEvaluators(
        evaluatorIds, llmEvals.keySet(), heuristicEvals.keySet(), pairwiseEvals.keySet());
    List<SXSPairEvaluationRequest> filteredSxsPairList = null;
    if (chatTurnIds == null) {
      chatTurnIds = new ArrayList<>();
    }

    Set<String> chatTurnIdsSet = new HashSet<>(chatTurnIds);
    Integer totalSxs = 0;
    if (sxsPairIds != null && !sxsPairIds.isEmpty()) {
      filteredSxsPairList = filterSXSPairIdsWithOutput(sxsPairIds, user);
      totalSxs += filteredSxsPairList.size() * pairwiseEvals.size();
      chatTurnIdsSet.addAll(getTurnIdsFromSxsPairs(sxsPairIds));
    }

    List<String> filteredIds = null;
    Integer totalPointwise = 0;
    if (chatTurnIdsSet != null && !chatTurnIdsSet.isEmpty()) {
      filteredIds = filterChatTurnIdsWithOutput(List.copyOf(chatTurnIdsSet));
      totalPointwise +=
          filteredIds.size() * heuristicEvals.size() + filteredIds.size() * llmEvals.size();
    }

    Project project = entityManager.getReference(Project.class, containerId);
    if (totalPointwise + totalSxs == 0) {
      throw new IllegalInputException("No chat turns or SxS pairs available for evaluation");
    }

    JobStatus jobStatus = createJobStatus(project, totalSxs + totalPointwise, user);
    if (totalPointwise > 0) {
      processBatch(filteredIds, llmEvals, heuristicEvals, jobStatus, user, project.getId());
    }

    if (totalSxs > 0 && !pairwiseEvals.isEmpty()) {
      processSxsBatch(filteredSxsPairList, pairwiseEvals, jobStatus, user, project.getId());
    }

    return jobStatus.getId();
  }

  private List<SXSPairEvaluationRequest> filterSXSPairIdsWithOutput(
      List<SXSPairEvaluationRequest> sxsPairIds, User user) {
    List<SXSPairEvaluationRequest> filtered = new ArrayList<>();

    List<String> pairIdsWithoutExplicitChatTurn =
        sxsPairIds.stream()
            .filter(pair -> pair.getChatTurnIdA() == null || pair.getChatTurnIdB() == null)
            .map(SXSPairEvaluationRequest::getId)
            .toList();

    List<SXSPairEvaluationRequest> pairsWithoutExplicitChatTurn =
        sxsPairRepository.findPairsWithOutputOnBothSides(pairIdsWithoutExplicitChatTurn, user);

    filtered.addAll(pairsWithoutExplicitChatTurn);

    List<String> explicitChatTurnIds = getTurnIdsFromSxsPairs(sxsPairIds);

    List<String> chatTurnIdsWithOutput = new ArrayList<>();
    if (!explicitChatTurnIds.isEmpty()) {
      chatTurnIdsWithOutput.addAll(filterChatTurnIdsWithOutput(explicitChatTurnIds));
    }

    for (SXSPairEvaluationRequest pair : sxsPairIds) {
      if (pair.getChatTurnIdA() != null && pair.getChatTurnIdB() != null) {
        if (chatTurnIdsWithOutput.contains(pair.getChatTurnIdA())
            && chatTurnIdsWithOutput.contains(pair.getChatTurnIdB())) {
          filtered.add(pair);
        }
      }
    }

    return filtered;
  }

  private List<String> getTurnIdsFromSxsPairs(List<SXSPairEvaluationRequest> sxsPairIds) {
    return sxsPairIds.stream()
        .filter(pair -> pair.getChatTurnIdA() != null && pair.getChatTurnIdB() != null)
        .flatMap(pair -> List.of(pair.getChatTurnIdA(), pair.getChatTurnIdB()).stream())
        .toList();
  }

  private Map<String, LLMEvaluator> findLLMEvaluators(List<String> evaluatorIds, User user) {
    List<LLMEvaluator> allUserEvaluatorIds =
        llmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM);
    return allUserEvaluatorIds.stream()
        .filter(evaluator -> evaluatorIds.contains(evaluator.getId()))
        .collect(Collectors.toMap(LLMEvaluator::getId, Function.identity()));
  }

  private Map<String, PointwiseHeuristicEvaluator> findHeuristicEvaluators(
      List<String> evaluatorIds, User user) {
    List<PointwiseHeuristicEvaluator> allUserEvaluatorIds =
        pointwiseHeuristicEvaluatorRepository.findAllByUser(user);
    return allUserEvaluatorIds.stream()
        .filter(evaluator -> evaluatorIds.contains(evaluator.getId()))
        .collect(Collectors.toMap(PointwiseHeuristicEvaluator::getId, Function.identity()));
  }

  private Map<String, PairwiseLLMEvaluator> findPairwiseEvaluators(
      List<String> evaluatorIds, User user) {
    List<PairwiseLLMEvaluator> allUserEvaluatorIds =
        pairwiseLLMEvaluatorService.getAllPairwiseLLMEvaluators(user);

    return allUserEvaluatorIds.stream()
        .filter(evaluator -> evaluatorIds.contains(evaluator.getId()))
        .collect(Collectors.toMap(PairwiseLLMEvaluator::getId, Function.identity()));
  }

  private void validateEvaluators(
      List<String> evaluatorIds, Set<String> llmEvalIds, Set<String> heuristicEvalIds) {
    validateEvaluators(evaluatorIds, llmEvalIds, heuristicEvalIds, null);
  }

  private void validateEvaluators(
      List<String> evaluatorIds,
      Set<String> existingLLMEvaluatorIds,
      Set<String> existingHeuristicEvaluatorIds,
      Set<String> existingPairwiseEvaluatorIds) {

    Integer llmEvalCount = existingLLMEvaluatorIds != null ? existingLLMEvaluatorIds.size() : 0;
    Integer heuristicEvalCount =
        existingHeuristicEvaluatorIds != null ? existingHeuristicEvaluatorIds.size() : 0;
    Integer pairwiseEvalCount =
        existingPairwiseEvaluatorIds != null ? existingPairwiseEvaluatorIds.size() : 0;

    if (llmEvalCount + heuristicEvalCount + pairwiseEvalCount < evaluatorIds.size()) {
      List<String> invalidEvaluators =
          evaluatorIds.stream()
              .filter(
                  id -> existingLLMEvaluatorIds != null && !existingLLMEvaluatorIds.contains(id))
              .filter(
                  id ->
                      existingHeuristicEvaluatorIds != null
                          && !existingHeuristicEvaluatorIds.contains(id))
              .filter(
                  id ->
                      existingPairwiseEvaluatorIds != null
                          && !existingPairwiseEvaluatorIds.contains(id))
              .toList();

      throw new IllegalInputException("Invalid evaluator(s) provided: " + invalidEvaluators);
    }
  }

  private List<String> filterChatTurnIdsWithOutput(List<String> chatTurnIds) {
    List<String> filteredIds = chatService.findIdsWithNonEmptyOutput(chatTurnIds);
    if (filteredIds.isEmpty()) {
      throw new IllegalInputException("No chat turns available for evaluation");
    }
    return filteredIds;
  }

  private JobStatus createJobStatus(Project project, Integer total, User user) {
    JobStatus jobStatus =
        jobStatusService.createScorerJobStatus(
            PlanckConstants.EVAL, null, user, JobStatusEnum.PENDING, total, project);

    return jobStatus;
  }

  private void processBatch(
      List<String> chatTurnIds,
      Map<String, LLMEvaluator> llmEvaluators,
      Map<String, PointwiseHeuristicEvaluator> heuristicEvaluators,
      JobStatus jobStatus,
      User user,
      String containerId) {
    log.info(
        "Processing combined batch evaluation for user: {}, chatTurnIds: {}, evaluatorIds: {}",
        user.getId(),
        chatTurnIds.size(),
        llmEvaluators.size() + heuristicEvaluators.size());

    Set<String> evaluatorsId = new HashSet<>();
    evaluatorsId.addAll(llmEvaluators.keySet());
    evaluatorsId.addAll(heuristicEvaluators.keySet());

    List<EvaluationStatus> evaluationStatuses =
        processExistingStatuses(chatTurnIds, evaluatorsId, jobStatus, user);

    Set<String> existingKeys =
        evaluationStatuses.stream()
            .map(es -> es.getChatTurnId() + "-" + es.getEvaluatorId())
            .collect(Collectors.toSet());

    List<EvaluationStatus> newStatuses =
        processNewEvaluationStatuses(
            chatTurnIds,
            llmEvaluators,
            heuristicEvaluators,
            jobStatus,
            user,
            existingKeys,
            containerId);

    evaluationStatuses.addAll(newStatuses);

    List<EvaluationDTO> evaluationDTOs =
        prepareEvaluationDTOs(user, evaluationStatuses, containerId);

    List<EvaluationDTO> llmEvaluationDTOs =
        evaluationDTOs.stream()
            .filter(ev -> llmEvaluators.containsKey(ev.getEvaluatorId()))
            .toList();

    List<EvaluationDTO> heuristicEvaluationsDTO =
        evaluationDTOs.stream()
            .filter(ev -> heuristicEvaluators.containsKey(ev.getEvaluatorId()))
            .toList();

    boolean isLLMEvaluatorsBulk = evaluationDTOs.size() > 10;
    boolean isHeuristicEvaluatorsBulk = evaluationDTOs.size() > 10;

    // Send messages to queue AFTER transaction commits to ensure database records exist
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            log.info(
                "Transaction committed. Sending {} LLM and {} heuristic evaluations to queues.",
                llmEvaluationDTOs.size(),
                heuristicEvaluationsDTO.size());

            llmEvaluationDTOs.parallelStream()
                .forEach(
                    dto ->
                        llmScoreQueueService.sendToQueue(
                            dto,
                            llmEvaluators.get(dto.getEvaluatorId()).getModel().getProvider(),
                            isLLMEvaluatorsBulk));

            heuristicEvaluationsDTO.parallelStream()
                .forEach(
                    dto ->
                        heuristicEvaluationScoreQueueService.sendToQueue(
                            dto, isHeuristicEvaluatorsBulk));
          }
        });
  }

  private void processSxsBatch(
      List<SXSPairEvaluationRequest> sxsPairIds,
      Map<String, PairwiseLLMEvaluator> pairwiseEvals,
      JobStatus jobStatus,
      User user,
      String containerId) {

    List<PairwiseScore> scores =
        processSxsScores(sxsPairIds, pairwiseEvals.keySet(), jobStatus, user);

    List<EvaluationDTO> evaluationDTOs =
        prepareSxsEvaluationDTOs(user, scores, jobStatus, containerId);

    boolean isBulk = evaluationDTOs.size() > 10;

    // Send messages to queue AFTER transaction commits to ensure database records exist
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            log.info(
                "Transaction committed. Sending {} pairwise evaluations to queues.",
                evaluationDTOs.size());

            evaluationDTOs.parallelStream()
                .forEach(
                    dto ->
                        llmScoreQueueService.sendToQueue(
                            dto,
                            pairwiseEvals.get(dto.getEvaluatorId()).getModel().getProvider(),
                            isBulk));
          }
        });
  }

  private List<EvaluationDTO> prepareSxsEvaluationDTOs(
      User user, List<PairwiseScore> scores, JobStatus status, String containerId) {
    List<EvaluationDTO> dtos = new ArrayList<>();
    for (PairwiseScore score : scores) {
      EvaluationDTO evaluationDTO = new EvaluationDTO();
      evaluationDTO.setChatTurnId(score.getChatTurnA().getId());
      evaluationDTO.setChatturnIdB(score.getChatTurnB().getId());
      evaluationDTO.setPairId(score.getPair().getId());
      evaluationDTO.setEvaluatorId(score.getLlmEvaluator().getId());
      evaluationDTO.setJobId(status.getId());
      evaluationDTO.setEvaluationStatusId(score.getEvaluationStatus().getId());
      evaluationDTO.setUserId(user.getId());
      evaluationDTO.setContainerId(containerId);

      dtos.add(evaluationDTO);
    }
    return dtos;
  }

  private List<EvaluationStatus> processExistingStatuses(
      List<String> chatTurnIds, Set<String> evaluators, JobStatus jobStatus, User user) {
    List<EvaluationStatus> existingStatuses =
        evaluationStatusRepository.findByTurnAndEvaluatorLists(
            chatTurnIds, evaluators, user.getId());

    for (EvaluationStatus existing : existingStatuses) {
      existing.setJobId(jobStatus.getId());
      existing.setStatus(EvaluationStatusEnum.PENDING.getKey());
    }
    evaluationStatusRepository.saveAll(existingStatuses);
    return existingStatuses;
  }

  private List<PairwiseScore> processSxsScores(
      List<SXSPairEvaluationRequest> pairs,
      Set<String> evaluators,
      JobStatus jobStatus,
      User user) {
    List<String> pairIds = pairs.stream().map(SXSPairEvaluationRequest::getId).toList();
    List<String> chatTurnAIds =
        pairs.stream().map(SXSPairEvaluationRequest::getChatTurnIdA).toList();
    List<String> chatTurnBIds =
        pairs.stream().map(SXSPairEvaluationRequest::getChatTurnIdB).toList();
    List<PairwiseScore> existingScores =
        pairwiseScoreRepository.findEvaluationStatusByPairDetailsAndEvaluators(
            pairIds, chatTurnAIds, chatTurnBIds, evaluators, user.getId());

    List<EvaluationStatus> allStatuses =
        existingScores.stream()
            .map(PairwiseScore::getEvaluationStatus)
            .filter(es -> es != null)
            .collect(Collectors.toList());

    List<EvaluationStatus> evaluationStatuses = new ArrayList<>();
    List<PairwiseScore> newScores = new ArrayList<>();
    for (SXSPairEvaluationRequest pair : pairs) {
      for (String evaluator : evaluators) {
        PairwiseScore existingScore =
            existingScores.stream()
                .filter(
                    score ->
                        pair.getId().equals(score.getPair().getId())
                            && pair.getChatTurnIdA().equals(score.getChatTurnA().getId())
                            && pair.getChatTurnIdB().equals(score.getChatTurnB().getId())
                            && evaluator.equals(score.getLlmEvaluator().getId()))
                .findFirst()
                .orElse(null);

        if (existingScore == null || existingScore.getEvaluationStatus() == null) {
          EvaluationStatus newStatus = new EvaluationStatus();
          newStatus.setUserId(user.getId());
          newStatus.setJobId(jobStatus.getId());
          newStatus.setEvaluatorId(evaluator);
          newStatus.setStatus(EvaluationStatusEnum.PENDING.getKey());
          newStatus.setContainerId(jobStatus.getProject().getId());
          newStatus.setChatTurnId(pair.getChatTurnIdA());
          evaluationStatuses.add(newStatus);

          PairwiseScore newScore = new PairwiseScore();
          newScore.setPair(entityManager.getReference(SxsEvaluationPair.class, pair.getId()));
          newScore.setChatTurnA(entityManager.getReference(ChatTurn.class, pair.getChatTurnIdA()));
          newScore.setChatTurnB(entityManager.getReference(ChatTurn.class, pair.getChatTurnIdB()));
          newScore.setLlmEvaluator(
              entityManager.getReference(PairwiseLLMEvaluator.class, evaluator));
          newScore.setUserId(user.getId());
          newScore.setEvaluationStatus(newStatus);
          newScores.add(newScore);
        } else {
          EvaluationStatus existingStatus = existingScore.getEvaluationStatus();
          existingStatus.setJobId(jobStatus.getId());
          existingStatus.setStatus(EvaluationStatusEnum.PENDING.getKey());
          evaluationStatuses.add(existingStatus);
        }
      }
    }
    allStatuses.addAll(evaluationStatuses);

    evaluationStatusRepository.saveAll(allStatuses);

    if (!evaluationStatuses.isEmpty()) {
      List<PairwiseScore> savedScores = pairwiseScoreRepository.saveAllAndFlush(newScores);
      existingScores.addAll(savedScores);
    }

    return existingScores;
  }

  private List<EvaluationStatus> processNewEvaluationStatuses(
      List<String> chatTurnIds,
      Map<String, LLMEvaluator> llmEvaluators,
      Map<String, PointwiseHeuristicEvaluator> heuristicEvaluators,
      JobStatus jobStatus,
      User user,
      Set<String> existingKeys,
      String containerId) {

    List<String> allEvaluatorIds = new ArrayList<>();
    allEvaluatorIds.addAll(llmEvaluators.keySet());
    allEvaluatorIds.addAll(heuristicEvaluators.keySet());

    List<EvaluationStatus> newStatuses = new ArrayList<>();

    for (String chatTurnId : chatTurnIds) {
      for (String evaluatorId : allEvaluatorIds) {
        String key = chatTurnId + "-" + evaluatorId;
        if (!existingKeys.contains(key)) {
          EvaluationStatus newStatus = new EvaluationStatus();
          newStatus.setChatTurnId(chatTurnId);
          newStatus.setEvaluatorId(evaluatorId);
          newStatus.setUserId(user.getId());
          newStatus.setJobId(jobStatus.getId());
          newStatus.setStatus(EvaluationStatusEnum.PENDING.getKey());
          newStatus.setContainerId(containerId);
          newStatuses.add(newStatus);
        }
      }
    }
    if (!newStatuses.isEmpty()) {
      evaluationStatusRepository.saveAll(newStatuses);
      createNewScores(newStatuses, llmEvaluators, heuristicEvaluators, user);
    }
    return newStatuses;
  }

  @Transactional
  private void createNewScores(
      List<EvaluationStatus> newStatuses,
      Map<String, LLMEvaluator> llmEvaluators,
      Map<String, PointwiseHeuristicEvaluator> heuristicEvaluators,
      User user) {

    List<ScoreV2> newLLMScores =
        newStatuses.stream()
            .filter(status -> llmEvaluators.containsKey(status.getEvaluatorId()))
            .map(
                status -> {
                  LLMEvaluator llmEvaluator = llmEvaluators.get(status.getEvaluatorId());
                  ChatTurn chatTurn =
                      entityManager.getReference(ChatTurn.class, status.getChatTurnId());
                  return createNewScoreV2(user, status, llmEvaluator, chatTurn);
                })
            .toList();

    List<PointwiseHeuristicEvaluationScore> newHeuristicScores =
        newStatuses.stream()
            .filter(status -> heuristicEvaluators.containsKey(status.getEvaluatorId()))
            .map(
                status -> {
                  PointwiseHeuristicEvaluator heuristicEvaluator =
                      heuristicEvaluators.get(status.getEvaluatorId());
                  ChatTurn chatTurn =
                      entityManager.getReference(ChatTurn.class, status.getChatTurnId());
                  return createNewHeuristicScore(user, status, heuristicEvaluator, chatTurn);
                })
            .toList();

    if (!newLLMScores.isEmpty()) {
      scoreV2Repository.saveAllAndFlush(newLLMScores);
    }
    if (!newHeuristicScores.isEmpty()) {
      pointwiseHeuristicEvaluationScoreRepository.saveAllAndFlush(newHeuristicScores);
    }
  }

  private PointwiseHeuristicEvaluationScore createNewHeuristicScore(
      User user,
      EvaluationStatus newStatus,
      PointwiseHeuristicEvaluator heuristicEvaluator,
      ChatTurn chatTurn) {
    PointwiseHeuristicEvaluationScore newScore = new PointwiseHeuristicEvaluationScore();
    newScore.setModelResponse(chatTurn.getModelResponse());
    newScore.setEvaluationStatus(newStatus);
    newScore.setPointwiseEvaluator(heuristicEvaluator);
    newScore.setUser(user);

    newScore.setPointwiseEvaluator(heuristicEvaluator);
    return newScore;
  }

  private ScoreV2 createNewScoreV2(
      User user, EvaluationStatus newStatus, LLMEvaluator llmEvaluator, ChatTurn chatTurn) {
    ScoreV2 newScore = new ScoreV2();
    newScore.setModelResponse(chatTurn.getModelResponse());
    newScore.setEvaluationStatus(newStatus);
    newScore.setUserId(user.getId());

    newScore.setLlmEvaluator(llmEvaluator);
    newScore.setScorer(llmEvaluator.getName());
    newScore.setScoreType(llmEvaluator.getOutputFormatType());
    return newScore;
  }

  private List<EvaluationDTO> prepareEvaluationDTOs(
      User user, List<EvaluationStatus> existingStatuses, String containerId) {
    List<EvaluationDTO> evaluationDTOs = new ArrayList<>();
    for (EvaluationStatus status : existingStatuses) {
      EvaluationDTO evaluationDTO = new EvaluationDTO();
      evaluationDTO.setChatTurnId(status.getChatTurnId());
      evaluationDTO.setEvaluatorId(status.getEvaluatorId());
      evaluationDTO.setJobId(status.getJobId());
      evaluationDTO.setEvaluationStatusId(status.getId());
      evaluationDTO.setUserId(user.getId());
      evaluationDTO.setContainerId(containerId);

      evaluationDTOs.add(evaluationDTO);
    }
    return evaluationDTOs;
  }
}

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

package com.planck.planck.domain.evaluation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.evaluation.dto.EvaluationDTO;
import com.planck.planck.domain.evaluation.dto.SXSPairEvaluationRequest;
import com.planck.planck.domain.evaluation.serivce.EvaluationBatchServiceImpl;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusRepository;
import com.planck.planck.domain.evaluator.heuristic.PointwiseHeuristicEvaluatorRepository;
import com.planck.planck.domain.evaluator.llm.LLMEvaluatorRepository;
import com.planck.planck.domain.evaluator.pairwise.service.PairwiseLLMEvaluatorService;
import com.planck.planck.domain.job.JobStatusService;
import com.planck.planck.domain.pubsub.LLMScoreQueueService;
import com.planck.planck.domain.pubsub.PointwiseHeuristicEvaluationScoreQueueService;
import com.planck.planck.entitities.*;
import com.planck.planck.enums.JobStatusEnum;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.exceptions.IllegalInputException;
import jakarta.persistence.EntityManager;
import java.util.*;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class EvaluationBatchServiceImplTest {
  @Mock private JobStatusService jobStatusService;
  @Mock private LLMScoreQueueService llmScoreQueueService;
  @Mock private ChatService chatService;
  @Mock private LLMEvaluatorRepository llmEvaluatorRepository;
  @Mock private PairwiseLLMEvaluatorService pairwiseLLMEvaluatorService;
  @Mock private EvaluationStatusRepository evaluationStatusRepository;
  @Mock private ScoreV2Repository scoreV2Repository;
  @Mock private SxsEvaluationPairRepository sxsPairRepository;
  @Mock private PairwiseScoreRepository pairwiseScoreRepository;
  @Mock private EntityManager entityManager;
  @Mock private PointwiseHeuristicEvaluatorRepository pointwiseHeuristicEvaluatorRepository;

  @Mock
  private PointwiseHeuristicEvaluationScoreRepository pointwiseHeuristicEvaluationScoreRepository;

  @Mock private PointwiseHeuristicEvaluationScoreQueueService heuristicEvaluationScoreQueueService;
  @InjectMocks private EvaluationBatchServiceImpl evaluationBatchService;
  private User user;
  private Project project;
  private LLMEvaluator llmEvaluator;
  private PointwiseHeuristicEvaluator heuristicEvaluator;
  private PairwiseLLMEvaluator pairwiseEvaluator;
  private ChatTurn chatTurn1, chatTurn2;
  private JobStatus jobStatus;
  private Model model;
  private SxsEvaluationPair sxsPair;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId("user-1");
    project = new Project();
    project.setId("proj-1");
    model = new Model();
    model.setId("model-1");
    model.setProvider(ModelProvider.OPENAI);
    llmEvaluator = new LLMEvaluator();
    llmEvaluator.setId("llm-1");
    llmEvaluator.setModel(model);
    heuristicEvaluator = new PointwiseHeuristicEvaluator();
    heuristicEvaluator.setId("heur-1");
    pairwiseEvaluator = new PairwiseLLMEvaluator();
    pairwiseEvaluator.setId("pair-1");
    pairwiseEvaluator.setModel(model);
    chatTurn1 = new ChatTurn();
    chatTurn1.setId("ct-1");
    ModelResponse mr1 = new ModelResponse();
    mr1.setContainer(project);
    chatTurn1.setModelResponse(mr1);
    Chat chat1 = new Chat();
    chat1.setContainer(project);
    chatTurn1.setChat(chat1);
    chatTurn2 = new ChatTurn();
    chatTurn2.setId("ct-2");
    ModelResponse mr2 = new ModelResponse();
    mr2.setContainer(project);
    chatTurn2.setModelResponse(mr2);
    Chat chat2 = new Chat();
    chat2.setContainer(project);
    chatTurn2.setChat(chat2);
    sxsPair = new SxsEvaluationPair();
    sxsPair.setId("pair-1");
    jobStatus = new JobStatus();
    jobStatus.setId("job-1");
    jobStatus.setProject(project);
  }

  @Test
  void processProject_Success() {
    List<String> turnIds = List.of("ct-1", "ct-2");
    List<String> evalIds = List.of("llm-1", "heur-1");
    List<String> filteredTurnIds = List.of("ct-1", "ct-2");
    when(llmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM))
        .thenReturn(List.of(llmEvaluator));
    when(pointwiseHeuristicEvaluatorRepository.findAllByUser(user))
        .thenReturn(List.of(heuristicEvaluator));
    when(chatService.findIdsWithNonEmptyOutput(turnIds)).thenReturn(filteredTurnIds);
    when(entityManager.getReference(Project.class, "proj-1")).thenReturn(project);
    when(jobStatusService.createScorerJobStatus(
            any(), any(), eq(user), eq(JobStatusEnum.PENDING), eq(4), eq(project)))
        .thenReturn(jobStatus);
    when(evaluationStatusRepository.findByTurnAndEvaluatorLists(
            anyList(), anySet(), eq(user.getId())))
        .thenReturn(new ArrayList<>());
    when(evaluationStatusRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(entityManager.getReference(eq(ChatTurn.class), eq("ct-1"))).thenReturn(chatTurn1);
    when(entityManager.getReference(eq(ChatTurn.class), eq("ct-2"))).thenReturn(chatTurn2);
    try (MockedStatic<TransactionSynchronizationManager> mockedTxSync =
        Mockito.mockStatic(TransactionSynchronizationManager.class)) {
      ArgumentCaptor<TransactionSynchronization> syncCaptor =
          ArgumentCaptor.forClass(TransactionSynchronization.class);
      mockedTxSync
          .when(
              () -> TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()))
          .then(invocation -> null);
      // Act
      String resultJobId = evaluationBatchService.processProject(turnIds, evalIds, "proj-1", user);
      // Assert
      assertEquals("job-1", resultJobId);
      verify(jobStatusService).createScorerJobStatus(any(), any(), any(), any(), eq(4), any());
      verify(llmScoreQueueService, never()).sendToQueue(any(), any(), anyBoolean());
      verify(heuristicEvaluationScoreQueueService, never()).sendToQueue(any(), anyBoolean());
      syncCaptor.getValue().afterCommit();
      verify(evaluationStatusRepository, atLeast(1)).saveAll(anyList());
      verify(llmScoreQueueService, times(2))
          .sendToQueue(any(EvaluationDTO.class), eq(model.getProvider()), eq(false));
      verify(heuristicEvaluationScoreQueueService, times(2))
          .sendToQueue(any(EvaluationDTO.class), eq(false));
      verify(entityManager, times(4)).getReference(eq(ChatTurn.class), anyString());
      verify(entityManager, never()).getReference(eq(LLMEvaluator.class), anyString());
      verify(entityManager, never())
          .getReference(eq(PointwiseHeuristicEvaluator.class), anyString());
    }
  }

  @Test
  void processProject_NoTurnsWithOutput_ThrowsException() {
    List<String> turnIds = List.of("ct-1");
    List<String> evalIds = List.of("llm-1");
    when(llmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM))
        .thenReturn(List.of(llmEvaluator));
    when(pointwiseHeuristicEvaluatorRepository.findAllByUser(user)).thenReturn(new ArrayList<>());
    when(chatService.findIdsWithNonEmptyOutput(turnIds)).thenReturn(Collections.emptyList());
    // Act & Assert
    assertThrows(
        IllegalInputException.class,
        () -> {
          evaluationBatchService.processProject(turnIds, evalIds, "proj-1", user);
        });
  }

  @Test
  void processProject_InvalidEvaluator_ThrowsException() {
    List<String> turnIds = List.of("ct-1");
    List<String> evalIds = List.of("invalid-eval");
    when(llmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM))
        .thenReturn(new ArrayList<>());
    when(pointwiseHeuristicEvaluatorRepository.findAllByUser(user)).thenReturn(new ArrayList<>());
    // Act & Assert
    assertThrows(
        IllegalInputException.class,
        () -> {
          evaluationBatchService.processProject(turnIds, evalIds, "proj-1", user);
        });
  }

  @Test
  void processDataSet_ThrowsNotImplemented() {
    // Act & Assert
    assertThrows(
        NotImplementedException.class,
        () -> {
          evaluationBatchService.processDataSet(List.of("ct-1"), List.of("llm-1"), "ds-1", user);
        });
  }

  @Test
  void processChatTurns_Success() {
    List<String> turnIds = List.of("ct-1");
    List<String> evalIds = List.of("llm-1");
    List<String> filteredTurnIds = List.of("ct-1");
    Chat chat = new Chat();
    chat.setContainer(project);
    chatTurn1.setChat(chat);
    when(llmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM))
        .thenReturn(List.of(llmEvaluator));
    when(pointwiseHeuristicEvaluatorRepository.findAllByUser(user)).thenReturn(new ArrayList<>());
    when(chatService.findIdsWithNonEmptyOutput(turnIds)).thenReturn(filteredTurnIds);
    when(chatService.getChatTurn("ct-1", user)).thenReturn(chatTurn1);
    when(jobStatusService.createScorerJobStatus(
            any(), any(), eq(user), eq(JobStatusEnum.PENDING), eq(1), eq(project)))
        .thenReturn(jobStatus);
    when(evaluationStatusRepository.findByTurnAndEvaluatorLists(
            anyList(), anySet(), eq(user.getId())))
        .thenReturn(new ArrayList<>());
    when(evaluationStatusRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(entityManager.getReference(eq(ChatTurn.class), eq("ct-1"))).thenReturn(chatTurn1);
    when(scoreV2Repository.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));
    try (MockedStatic<TransactionSynchronizationManager> mockedTxSync =
        Mockito.mockStatic(TransactionSynchronizationManager.class)) {
      ArgumentCaptor<TransactionSynchronization> syncCaptor =
          ArgumentCaptor.forClass(TransactionSynchronization.class);
      mockedTxSync
          .when(
              () -> TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()))
          .then(invocation -> null);
      // Act
      String resultJobId = evaluationBatchService.processChatTurns(turnIds, evalIds, user);
      // Assert
      assertEquals("job-1", resultJobId);
      verify(chatService).getChatTurn("ct-1", user);
      verify(jobStatusService).createScorerJobStatus(any(), any(), any(), any(), eq(1), any());
      verify(llmScoreQueueService, never()).sendToQueue(any(), any(), anyBoolean());
      syncCaptor.getValue().afterCommit();
      verify(llmScoreQueueService)
          .sendToQueue(any(EvaluationDTO.class), eq(model.getProvider()), eq(false));
      verify(entityManager).getReference(eq(ChatTurn.class), eq("ct-1"));
    }
  }

  @Test
  void processLists_PointwiseOnly_Success() {
    List<String> turnIds = List.of("ct-1");
    List<String> evalIds = List.of("llm-1", "heur-1");
    List<String> filteredTurnIds = List.of("ct-1");
    when(llmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM))
        .thenReturn(List.of(llmEvaluator));
    when(pointwiseHeuristicEvaluatorRepository.findAllByUser(user))
        .thenReturn(List.of(heuristicEvaluator));
    when(pairwiseLLMEvaluatorService.getAllPairwiseLLMEvaluators(user))
        .thenReturn(new ArrayList<>());
    when(chatService.findIdsWithNonEmptyOutput(turnIds)).thenReturn(filteredTurnIds);
    when(entityManager.getReference(Project.class, "proj-1")).thenReturn(project);
    when(jobStatusService.createScorerJobStatus(
            any(), any(), eq(user), eq(JobStatusEnum.PENDING), eq(2), eq(project)))
        .thenReturn(jobStatus);
    when(evaluationStatusRepository.findByTurnAndEvaluatorLists(
            anyList(), anySet(), eq(user.getId())))
        .thenReturn(new ArrayList<>());
    when(evaluationStatusRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(entityManager.getReference(eq(ChatTurn.class), eq("ct-1"))).thenReturn(chatTurn1);
    when(scoreV2Repository.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(pointwiseHeuristicEvaluationScoreRepository.saveAllAndFlush(anyList()))
        .thenAnswer(inv -> inv.getArgument(0));
    try (MockedStatic<TransactionSynchronizationManager> mockedTxSync =
        Mockito.mockStatic(TransactionSynchronizationManager.class)) {
      ArgumentCaptor<TransactionSynchronization> syncCaptor =
          ArgumentCaptor.forClass(TransactionSynchronization.class);
      mockedTxSync
          .when(
              () -> TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()))
          .then(invocation -> null);
      // Act
      String resultJobId =
          evaluationBatchService.processLists(null, turnIds, evalIds, "proj-1", user);
      // Assert
      assertEquals("job-1", resultJobId);
      verify(llmScoreQueueService, never()).sendToQueue(any(), any(), anyBoolean());
      verify(heuristicEvaluationScoreQueueService, never()).sendToQueue(any(), anyBoolean());
      syncCaptor.getValue().afterCommit();
      verify(llmScoreQueueService)
          .sendToQueue(any(EvaluationDTO.class), eq(model.getProvider()), eq(false));
      verify(heuristicEvaluationScoreQueueService).sendToQueue(any(EvaluationDTO.class), eq(false));
      verify(pairwiseScoreRepository, never())
          .findEvaluationStatusByPairDetailsAndEvaluators(any(), any(), any(), any(), any());
      verify(entityManager, times(2)).getReference(eq(ChatTurn.class), eq("ct-1"));
    }
  }

  @Test
  void processLists_SXSOnly_Success() {
    SXSPairEvaluationRequest pairRequest = new SXSPairEvaluationRequest("pair-1", "ct-1", "ct-2");
    List<SXSPairEvaluationRequest> sxsPairs = List.of(pairRequest);
    List<String> evalIds = List.of("pair-1");
    when(llmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM))
        .thenReturn(new ArrayList<>());
    when(pointwiseHeuristicEvaluatorRepository.findAllByUser(user)).thenReturn(new ArrayList<>());
    when(pairwiseLLMEvaluatorService.getAllPairwiseLLMEvaluators(user))
        .thenReturn(List.of(pairwiseEvaluator));
    when(sxsPairRepository.findPairsWithOutputOnBothSides(anyList(), eq(user)))
        .thenReturn(new ArrayList<>());
    when(chatService.findIdsWithNonEmptyOutput(anyList())).thenReturn(List.of("ct-1", "ct-2"));
    when(entityManager.getReference(Project.class, "proj-1")).thenReturn(project);
    when(jobStatusService.createScorerJobStatus(
            any(), any(), eq(user), eq(JobStatusEnum.PENDING), eq(1), eq(project)))
        .thenReturn(jobStatus);
    when(pairwiseScoreRepository.findEvaluationStatusByPairDetailsAndEvaluators(
            anyList(), anyList(), anyList(), anySet(), eq(user.getId())))
        .thenReturn(new ArrayList<>());
    when(evaluationStatusRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(pairwiseScoreRepository.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(entityManager.getReference(eq(ChatTurn.class), eq("ct-1"))).thenReturn(chatTurn1);
    when(entityManager.getReference(eq(ChatTurn.class), eq("ct-2"))).thenReturn(chatTurn2);
    when(entityManager.getReference(eq(SxsEvaluationPair.class), eq("pair-1"))).thenReturn(sxsPair);
    when(entityManager.getReference(eq(PairwiseLLMEvaluator.class), eq("pair-1")))
        .thenReturn(pairwiseEvaluator);
    try (MockedStatic<TransactionSynchronizationManager> mockedTxSync =
        Mockito.mockStatic(TransactionSynchronizationManager.class)) {
      ArgumentCaptor<TransactionSynchronization> syncCaptor =
          ArgumentCaptor.forClass(TransactionSynchronization.class);
      mockedTxSync
          .when(
              () -> TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()))
          .then(invocation -> null);
      // Act
      String resultJobId =
          evaluationBatchService.processLists(sxsPairs, null, evalIds, "proj-1", user);
      // Assert
      assertEquals("job-1", resultJobId);
      verify(llmScoreQueueService, never()).sendToQueue(any(), any(), anyBoolean());
      verify(heuristicEvaluationScoreQueueService, never()).sendToQueue(any(), anyBoolean());
      syncCaptor.getValue().afterCommit();
      verify(llmScoreQueueService)
          .sendToQueue(any(EvaluationDTO.class), eq(model.getProvider()), eq(false));
      verify(heuristicEvaluationScoreQueueService, never()).sendToQueue(any(), anyBoolean());
      verify(evaluationStatusRepository, times(1)).saveAll(anyList());
      verify(pairwiseScoreRepository).saveAllAndFlush(anyList());
      verify(entityManager).getReference(eq(ChatTurn.class), eq("ct-1"));
      verify(entityManager).getReference(eq(ChatTurn.class), eq("ct-2"));
      verify(entityManager).getReference(eq(SxsEvaluationPair.class), eq("pair-1"));
      verify(entityManager).getReference(eq(PairwiseLLMEvaluator.class), eq("pair-1"));
    }
  }

  @Test
  void processLists_Mixed_Success() {
    List<String> turnIds = List.of("ct-1");
    SXSPairEvaluationRequest pairRequest = new SXSPairEvaluationRequest("pair-1", "ct-1", "ct-2");
    List<SXSPairEvaluationRequest> sxsPairs = List.of(pairRequest);
    List<String> evalIds = List.of("llm-1", "heur-1", "pair-1");
    List<String> filteredTurnIds = List.of("ct-1", "ct-2");
    when(llmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM))
        .thenReturn(List.of(llmEvaluator));
    when(pointwiseHeuristicEvaluatorRepository.findAllByUser(user))
        .thenReturn(List.of(heuristicEvaluator));
    when(pairwiseLLMEvaluatorService.getAllPairwiseLLMEvaluators(user))
        .thenReturn(List.of(pairwiseEvaluator));
    when(sxsPairRepository.findPairsWithOutputOnBothSides(anyList(), eq(user)))
        .thenReturn(new ArrayList<>());
    when(chatService.findIdsWithNonEmptyOutput(anyList())).thenReturn(filteredTurnIds);
    when(entityManager.getReference(Project.class, "proj-1")).thenReturn(project);
    when(jobStatusService.createScorerJobStatus(
            any(), any(), eq(user), eq(JobStatusEnum.PENDING), eq(5), eq(project)))
        .thenReturn(jobStatus);
    when(evaluationStatusRepository.findByTurnAndEvaluatorLists(
            anyList(), anySet(), eq(user.getId())))
        .thenReturn(new ArrayList<>());
    when(pairwiseScoreRepository.findEvaluationStatusByPairDetailsAndEvaluators(
            anyList(), anyList(), anyList(), anySet(), eq(user.getId())))
        .thenReturn(new ArrayList<>());
    when(evaluationStatusRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(pairwiseScoreRepository.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(entityManager.getReference(eq(ChatTurn.class), eq("ct-1"))).thenReturn(chatTurn1);
    when(entityManager.getReference(eq(ChatTurn.class), eq("ct-2"))).thenReturn(chatTurn2);
    when(entityManager.getReference(eq(SxsEvaluationPair.class), eq("pair-1"))).thenReturn(sxsPair);
    when(entityManager.getReference(eq(PairwiseLLMEvaluator.class), eq("pair-1")))
        .thenReturn(pairwiseEvaluator);
    when(scoreV2Repository.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));
    when(pointwiseHeuristicEvaluationScoreRepository.saveAllAndFlush(anyList()))
        .thenAnswer(inv -> inv.getArgument(0));
    try (MockedStatic<TransactionSynchronizationManager> mockedTxSync =
        Mockito.mockStatic(TransactionSynchronizationManager.class)) {
      ArgumentCaptor<TransactionSynchronization> syncCaptor =
          ArgumentCaptor.forClass(TransactionSynchronization.class);
      mockedTxSync
          .when(
              () -> TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()))
          .then(invocation -> null);
      // Act
      String resultJobId =
          evaluationBatchService.processLists(sxsPairs, turnIds, evalIds, "proj-1", user);
      // Assert
      assertEquals("job-1", resultJobId);
      verify(llmScoreQueueService, never()).sendToQueue(any(), any(), anyBoolean());
      verify(heuristicEvaluationScoreQueueService, never()).sendToQueue(any(), anyBoolean());
      assertEquals(2, syncCaptor.getAllValues().size());
      syncCaptor.getAllValues().forEach(TransactionSynchronization::afterCommit);
      verify(llmScoreQueueService, times(3))
          .sendToQueue(any(EvaluationDTO.class), eq(model.getProvider()), eq(false));
      verify(heuristicEvaluationScoreQueueService, times(2))
          .sendToQueue(any(EvaluationDTO.class), eq(false));
      verify(evaluationStatusRepository, times(3)).saveAll(anyList());
      verify(pairwiseScoreRepository).saveAllAndFlush(anyList());
      verify(entityManager, times(6)).getReference(eq(ChatTurn.class), anyString());
      verify(entityManager, never()).getReference(eq(LLMEvaluator.class), anyString());
      verify(entityManager, never())
          .getReference(eq(PointwiseHeuristicEvaluator.class), anyString());
      verify(entityManager, times(1)).getReference(eq(SxsEvaluationPair.class), eq("pair-1"));
      verify(entityManager, times(1)).getReference(eq(PairwiseLLMEvaluator.class), eq("pair-1"));
    }
  }

  @Test
  void processLists_NoTurnsOrPairs_ThrowsException() {
    List<String> evalIds = List.of("llm-1");
    when(llmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM))
        .thenReturn(List.of(llmEvaluator));
    when(pointwiseHeuristicEvaluatorRepository.findAllByUser(user)).thenReturn(new ArrayList<>());
    when(pairwiseLLMEvaluatorService.getAllPairwiseLLMEvaluators(user))
        .thenReturn(new ArrayList<>());
    when(entityManager.getReference(Project.class, "proj-1")).thenReturn(project);
    // Act & Assert
    assertThrows(
        IllegalInputException.class,
        () -> {
          evaluationBatchService.processLists(null, null, evalIds, "proj-1", user);
        });
    assertThrows(
        IllegalInputException.class,
        () -> {
          evaluationBatchService.processLists(
              Collections.emptyList(), Collections.emptyList(), evalIds, "proj-1", user);
        });
  }

  @Test
  void processLists_InvalidEvaluator_ThrowsException() {
    List<String> turnIds = List.of("ct-1");
    List<String> evalIds = List.of("invalid-eval");
    when(llmEvaluatorRepository.findAllByUserOrType(user, ScopeType.SYSTEM))
        .thenReturn(new ArrayList<>());
    when(pointwiseHeuristicEvaluatorRepository.findAllByUser(user)).thenReturn(new ArrayList<>());
    when(pairwiseLLMEvaluatorService.getAllPairwiseLLMEvaluators(user))
        .thenReturn(new ArrayList<>());
    // Act & Assert
    assertThrows(
        IllegalInputException.class,
        () -> {
          evaluationBatchService.processLists(null, turnIds, evalIds, "proj-1", user);
        });
  }
}

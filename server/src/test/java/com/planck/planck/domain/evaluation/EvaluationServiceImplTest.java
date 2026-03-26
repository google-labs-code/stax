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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.evaluation.dto.ChatTurnEvaluationRequest;
import com.planck.planck.domain.evaluation.dto.ScorerResponseDTO;
import com.planck.planck.domain.evaluation.serivce.EvaluationBatchService;
import com.planck.planck.domain.evaluation.serivce.EvaluationServiceImpl;
import com.planck.planck.domain.evaluationmonitoring.EvaluationMonitoringService;
import com.planck.planck.domain.evaluationstatus.EvaluationStatusService;
import com.planck.planck.domain.evaluator.dto.EvaluatorVariableDTO;
import com.planck.planck.domain.evaluator.dto.OutputCategoryDTO;
import com.planck.planck.domain.evaluator.heuristic.PointwiseHeuristicEvaluatorRepository;
import com.planck.planck.domain.evaluator.llm.LLMEvaluatorRepository;
import com.planck.planck.domain.inference.service.InferenceService;
import com.planck.planck.domain.project.EvaluationContainerService;
import com.planck.planck.domain.tags.TagLinkService;
import com.planck.planck.entitities.Chat;
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
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.ScoreV2;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.CriteriaType;
import com.planck.planck.enums.EvaluationStatusEnum;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.InputRole;
import com.planck.planck.enums.ScoreType;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.util.ObjectMapperUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceImplTest {

  @Mock private ScoreV2Repository scoreRepository;
  @Mock private ChatService chatService;
  @Mock private LLMEvaluatorRepository llmEvaluatorRepository;
  @Mock private ChatTurnRepository chatTurnRepository;
  @Mock private InferenceService inferenceService;
  @Mock private EvaluationStatusService evaluationStatusService;
  @Mock private EvaluationMonitoringService evaluationMonitoringService;
  @Mock private TagLinkService tagLinkService;
  @Mock private EvaluationBatchService evaluationBatchService;
  @Mock private PointwiseHeuristicEvaluatorRepository heuristicEvaluatorRepository;
  @Mock private PointwiseHeuristicEvaluationScoreRepository heuristicEvaluationScore;
  @Mock private SxsEvaluationPairRepository sxsPairRepository;
  @Mock private PairwiseScoreRepository pairwiseScoreRepository;
  @Mock private EvaluationContainerService evaluationContainerService;

  @InjectMocks @Spy private EvaluationServiceImpl evaluationService;

  private User user;
  private EvaluationContainer container;
  private Model model;
  private ChatTurn turnA, turnB, turnHistory;
  private ModelResponse responseA, responseB;
  private LLMEvaluator llmEvaluator;
  private PairwiseLLMEvaluator pairwiseEvaluator;
  private PointwiseHeuristicEvaluator heuristicEvaluator;
  private SxsEvaluationPair sxsPair;
  private EvaluationStatus evalStatus;
  private List<EvaluatorVariableDTO> mockVariables;

  private static MockedStatic<ObjectMapperUtil> mockedObjectMapperUtil;

  @BeforeAll
  static void beforeAll() {
    mockedObjectMapperUtil = Mockito.mockStatic(ObjectMapperUtil.class);
  }

  @AfterAll
  static void afterAll() {
    mockedObjectMapperUtil.close();
  }

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId("user-123");

    container = new Project();
    container.setId("project-1");

    model = new Model();
    model.setId("model-1");

    turnA = new ChatTurn();
    turnA.setId("turn-A");
    turnA.setSequenceId(0);
    Chat chatA = new Chat();
    chatA.setId("chat-A");
    chatA.setContainer(container);
    turnA.setChat(chatA);
    responseA = new ModelResponse();
    responseA.setText("This is output A");
    responseA.setContainer(container);
    responseA.setScores(new ArrayList<>());
    responseA.setPointwiseHeuristicEvaluationScores(new ArrayList<>());
    turnA.setModelResponse(responseA);
    turnA.setInputs(
        new ArrayList<>(List.of(new ModelInput(new Prompt(InputRole.USER, "Prompt A"), user))));

    turnB = new ChatTurn();
    turnB.setId("turn-B");
    turnB.setSequenceId(1);
    Chat chatB = new Chat();
    chatB.setId("chat-B");
    chatB.setContainer(container);
    turnB.setChat(chatB);
    responseB = new ModelResponse();
    responseB.setText("This is output B");
    responseB.setContainer(container);
    responseB.setScores(new ArrayList<>());
    responseB.setPointwiseHeuristicEvaluationScores(new ArrayList<>());
    turnB.setModelResponse(responseB);
    turnB.setInputs(List.of(new ModelInput(new Prompt(InputRole.USER, "Prompt B"), user)));

    turnHistory = new ChatTurn();
    turnHistory.setId("turn-H");
    turnHistory.setChat(chatA);
    turnHistory.setInputs(
        List.of(new ModelInput(new Prompt(InputRole.USER, "History Prompt"), user)));
    ModelResponse historyResponse = new ModelResponse();
    historyResponse.setText("History Response");
    turnHistory.setModelResponse(historyResponse);

    llmEvaluator = new LLMEvaluator();
    llmEvaluator.setId("llm-eval-1");
    llmEvaluator.setModel(model);
    llmEvaluator.setOutputFormatType(ScoreType.Choices);
    llmEvaluator.setOutputCategories(
        "[{\"name\":\"Good\",\"value\":\"1\"}, {\"name\":\"Bad\",\"value\":\"0\"}]");

    pairwiseEvaluator = new PairwiseLLMEvaluator();
    pairwiseEvaluator.setId("pairwise-eval-1");
    pairwiseEvaluator.setModel(model);
    pairwiseEvaluator.setOutputFormatType(ScoreType.Choices);
    pairwiseEvaluator.setOutputCategories(
        "[{\"name\":\"A\",\"value\":\"1\"}, {\"name\":\"B\",\"value\":\"-1\"}]");

    heuristicEvaluator = new PointwiseHeuristicEvaluator();
    heuristicEvaluator.setId("heuristic-eval-1");

    sxsPair = new SxsEvaluationPair();
    sxsPair.setId("pair-1");
    sxsPair.setChatTurnA(turnA);
    sxsPair.setChatTurnB(turnB);
    sxsPair.setContainer(container);
    sxsPair.setUser(user);

    evalStatus = new EvaluationStatus();
    evalStatus.setId("status-1");

    mockVariables = new ArrayList<>();
    EvaluatorVariableDTO inputVar = new EvaluatorVariableDTO();
    inputVar.setName("input");
    inputVar.setRequired(true);
    mockVariables.add(inputVar);

    EvaluatorVariableDTO outputVar = new EvaluatorVariableDTO();
    outputVar.setName("output");
    outputVar.setRequired(true);
    mockVariables.add(outputVar);
  }

  @Test
  void generateScoresFromProject_Pointwise_Success() {
    container.setEvaluationType(EvaluationType.POINTWISE);
    List<String> turnIds = List.of("turn-A", "turn-B");
    List<String> evalIds = List.of("llm-eval-1");

    when(evaluationContainerService.getContainerForUser(user, "project-1")).thenReturn(container);
    when(chatTurnRepository.findLatestChatTurnIdsByContainerId("project-1", user))
        .thenReturn(turnIds);
    when(evaluationBatchService.processProject(turnIds, evalIds, "project-1", user))
        .thenReturn("job-123");

    // Act
    ScorerResponseDTO response =
        evaluationService.generateScoresFromProject("project-1", evalIds, user);

    // Assert
    assertEquals("job-123", response.getJobId());
    assertEquals(turnIds, response.getChatTurnIds());
    verify(evaluationBatchService).processProject(anyList(), anyList(), anyString(), any());
    verify(evaluationBatchService, never())
        .processLists(any(), any(), anyList(), anyString(), any());
  }

  @Test
  void generateScoresFromProject_SXS_Success() {
    container.setEvaluationType(EvaluationType.SXS);
    List<String> evalIds = List.of("pairwise-eval-1");

    when(evaluationContainerService.getContainerForUser(user, "project-1")).thenReturn(container);
    when(sxsPairRepository.findByContainer(container)).thenReturn(List.of(sxsPair));
    when(evaluationBatchService.processLists(
            anyList(), isNull(), eq(evalIds), eq("project-1"), eq(user)))
        .thenReturn("job-456");

    // Act
    ScorerResponseDTO response =
        evaluationService.generateScoresFromProject("project-1", evalIds, user);

    // Assert
    assertEquals("job-456", response.getJobId());
    verify(evaluationBatchService).processLists(anyList(), isNull(), anyList(), anyString(), any());
    verify(evaluationBatchService, never())
        .processProject(anyList(), anyList(), anyString(), any());
  }

  @Test
  void generateScoresFromProject_Empty_ThrowsException() {
    container.setEvaluationType(EvaluationType.POINTWISE);
    when(evaluationContainerService.getContainerForUser(user, "project-1")).thenReturn(container);
    when(chatTurnRepository.findLatestChatTurnIdsByContainerId("project-1", user))
        .thenReturn(Collections.emptyList());

    // Act & Assert
    assertThrows(
        IllegalInputException.class,
        () -> {
          evaluationService.generateScoresFromProject("project-1", List.of("e1"), user);
        });
  }

  @Test
  void generateScoresFromChatTurns_Success() {
    List<String> turnIds = List.of("turn-A");
    List<String> evalIds = List.of("llm-eval-1");
    when(evaluationBatchService.processLists(
            isNull(), eq(turnIds), eq(evalIds), eq("project-1"), eq(user)))
        .thenReturn("job-789");

    // Act
    ScorerResponseDTO response =
        evaluationService.generateScoresFromChatTurns(turnIds, null, evalIds, "project-1", user);

    // Assert
    assertEquals("job-789", response.getJobId());
    assertEquals(turnIds, response.getChatTurnIds());
  }

  @Test
  void generateScore_PointwiseHeuristic_Contains_Success() {
    heuristicEvaluator.setCriteriaType(CriteriaType.CONTAINS);
    heuristicEvaluator.setCriteria("output A");
    responseA.setText("This is output A");

    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-A", user)).thenReturn(turnA);
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            "heuristic-eval-1", user))
        .thenReturn(Optional.empty());
    when(heuristicEvaluatorRepository.findById("heuristic-eval-1"))
        .thenReturn(Optional.of(heuristicEvaluator));
    when(heuristicEvaluationScore.save(any(PointwiseHeuristicEvaluationScore.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // Act
    evaluationService.generateScore(
        null, "turn-A", null, "heuristic-eval-1", "project-1", evalStatus, user);

    // Assert
    verify(evaluationStatusService, times(1))
        .updateEvaluationStatus(eq(evalStatus), eq(EvaluationStatusEnum.IN_PROGRESS), anyString());
    verify(evaluationStatusService, times(1))
        .updateEvaluationStatus(eq(evalStatus), eq(EvaluationStatusEnum.SUCCESSFUL), anyString());

    ArgumentCaptor<PointwiseHeuristicEvaluationScore> captor =
        ArgumentCaptor.forClass(PointwiseHeuristicEvaluationScore.class);
    verify(heuristicEvaluationScore).save(captor.capture());
    assertTrue(captor.getValue().isMatch());
  }

  @Test
  void generateScore_PointwiseHeuristic_Regex_Success() {
    heuristicEvaluator.setCriteriaType(CriteriaType.REGEX);
    heuristicEvaluator.setCriteria("^This.*A$");
    responseA.setText("This is output A");

    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-A", user)).thenReturn(turnA);
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            "heuristic-eval-1", user))
        .thenReturn(Optional.empty());
    when(heuristicEvaluatorRepository.findById("heuristic-eval-1"))
        .thenReturn(Optional.of(heuristicEvaluator));
    when(heuristicEvaluationScore.save(any(PointwiseHeuristicEvaluationScore.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // Act
    evaluationService.generateScore(
        null, "turn-A", null, "heuristic-eval-1", "project-1", evalStatus, user);

    // Assert
    ArgumentCaptor<PointwiseHeuristicEvaluationScore> captor =
        ArgumentCaptor.forClass(PointwiseHeuristicEvaluationScore.class);
    verify(heuristicEvaluationScore).save(captor.capture());
    assertTrue(captor.getValue().isMatch());
  }

  @Test
  void generateScore_PointwiseHeuristic_InvalidRegex_ThrowsException() {
    heuristicEvaluator.setCriteriaType(CriteriaType.REGEX);
    heuristicEvaluator.setCriteria("*[invalid");
    responseA.setText("This is output A");

    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-A", user)).thenReturn(turnA);
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            "heuristic-eval-1", user))
        .thenReturn(Optional.empty());
    when(heuristicEvaluatorRepository.findById("heuristic-eval-1"))
        .thenReturn(Optional.of(heuristicEvaluator));

    // Act & Assert
    assertThrows(
        IllegalInputException.class,
        () -> {
          evaluationService.generateScore(
              null, "turn-A", null, "heuristic-eval-1", "project-1", evalStatus, user);
        });
    verify(evaluationStatusService, times(1))
        .updateEvaluationStatus(eq(evalStatus), eq(EvaluationStatusEnum.FAILED), anyString());
  }

  @Test
  void generateScore_PointwiseLLM_Success() {
    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-A", user)).thenReturn(turnA);
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            "llm-eval-1", user))
        .thenReturn(Optional.of(llmEvaluator));
    when(chatService.getChat("chat-A", user)).thenReturn(List.of(turnA));

    ChatResponseWithLatency llmResponse =
        new ChatResponseWithLatency("Good\nThis is the reasoning.", null, null, null, null, 100.0);
    when(inferenceService.runEvaluationInference(eq(user), eq(model), anyList()))
        .thenReturn(llmResponse);

    EvaluationMonitoring monitoring = new EvaluationMonitoring();
    monitoring.setId("mon-1");
    when(evaluationMonitoringService.saveEvaluationMonitoring(
            any(User.class),
            any(ChatResponseWithLatency.class),
            any(Model.class),
            any(LLMEvaluator.class),
            any(EvaluationContainer.class)))
        .thenReturn(monitoring);

    when(scoreRepository.save(any(ScoreV2.class))).thenAnswer(inv -> inv.getArgument(0));

    Prompt promptTemplate = new Prompt(InputRole.USER, "Evaluate: {{output}}");
    ModelInput modelInputTemplate = new ModelInput(promptTemplate, user);
    llmEvaluator.setInputs(List.of(modelInputTemplate));

    mockedObjectMapperUtil
        .when(() -> ObjectMapperUtil.convertJsonStringToList(any(), eq(EvaluatorVariableDTO.class)))
        .thenReturn(mockVariables);

    List<OutputCategoryDTO> parsedCategories = new ArrayList<>();
    OutputCategoryDTO goodCat = new OutputCategoryDTO();
    goodCat.setName("Good");
    goodCat.setValue("1");
    parsedCategories.add(goodCat);
    OutputCategoryDTO badCat = new OutputCategoryDTO();
    badCat.setName("Bad");
    badCat.setValue("0");
    parsedCategories.add(badCat);

    mockedObjectMapperUtil
        .when(
            () ->
                ObjectMapperUtil.convertJsonStringToList(
                    anyString(),
                    eq(com.planck.planck.domain.evaluator.dto.OutputCategoryDTO.class)))
        .thenReturn(parsedCategories);

    // Act
    evaluationService.generateScore(
        null, "turn-A", null, "llm-eval-1", "project-1", evalStatus, user);

    // Assert
    verify(inferenceService).runEvaluationInference(any(), any(), anyList());
    verify(tagLinkService).syncEvaluationMonitoringTags("mon-1", user);

    ArgumentCaptor<ScoreV2> captor = ArgumentCaptor.forClass(ScoreV2.class);
    verify(scoreRepository).save(captor.capture());
    Map<String, Object> scoreMap = captor.getValue().getScore();
    Object scoreValue = scoreMap.get("score");
    assertNotNull(scoreValue, "Score value in the map should not be null");
    assertEquals(1.0, Double.parseDouble(scoreValue.toString()), 0.0);
    assertEquals("Good\nThis is the reasoning.", captor.getValue().getLlmResponse());
  }

  @Test
  void generateScore_SXS_Success() {
    when(sxsPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.of(sxsPair));
    when(llmEvaluatorRepository.findPairwiseEvaluatorByIdAndUser("pairwise-eval-1", user))
        .thenReturn(Optional.of(pairwiseEvaluator));

    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-A", user)).thenReturn(turnA);
    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-B", user)).thenReturn(turnB);

    when(chatService.getChat("chat-A", user)).thenReturn(List.of(turnA));
    when(chatService.getChat("chat-B", user)).thenReturn(List.of(turnB));
    ChatResponseWithLatency llmResponse =
        new ChatResponseWithLatency("B\nReasoning.", null, null, null, null, 100.0);
    when(inferenceService.runEvaluationInference(eq(user), eq(model), anyList()))
        .thenReturn(llmResponse);

    when(evaluationMonitoringService.saveEvaluationMonitoring(
            any(User.class),
            any(ChatResponseWithLatency.class),
            any(Model.class),
            any(PairwiseLLMEvaluator.class),
            any(EvaluationContainer.class)))
        .thenReturn(new EvaluationMonitoring());
    when(pairwiseScoreRepository.findByPairAndChatTurnsAndEvaluatorAndUser(
            anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(Optional.empty());
    when(pairwiseScoreRepository.saveAndFlush(any(PairwiseScore.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    Prompt promptTemplate =
        new Prompt(InputRole.USER, "Compare A: {{output.a}} vs B: {{output.b}}");
    ModelInput modelInputTemplate = new ModelInput(promptTemplate, user);
    pairwiseEvaluator.setInputs(List.of(modelInputTemplate));

    mockedObjectMapperUtil
        .when(() -> ObjectMapperUtil.convertJsonStringToList(any(), eq(EvaluatorVariableDTO.class)))
        .thenReturn(mockVariables);

    List<OutputCategoryDTO> parsedCategories = new ArrayList<>();
    OutputCategoryDTO catA = new OutputCategoryDTO();
    catA.setName("A");
    catA.setValue("1");
    parsedCategories.add(catA);
    OutputCategoryDTO catB = new OutputCategoryDTO();
    catB.setName("B");
    catB.setValue("-1");
    parsedCategories.add(catB);
    mockedObjectMapperUtil
        .when(
            () ->
                ObjectMapperUtil.convertJsonStringToList(anyString(), eq(OutputCategoryDTO.class)))
        .thenReturn(parsedCategories);

    // Act
    evaluationService.generateScore(
        "pair-1", "turn-A", "turn-B", "pairwise-eval-1", "project-1", evalStatus, user);

    // Assert
    verify(sxsPairRepository).findByIdAndUser("pair-1", user);
    verify(chatTurnRepository).findByChatTurnIdAndUserId("turn-A", user);
    verify(chatTurnRepository).findByChatTurnIdAndUserId("turn-B", user);
    verify(chatService, times(2)).getChat(anyString(), any(User.class));

    ArgumentCaptor<PairwiseScore> captor = ArgumentCaptor.forClass(PairwiseScore.class);
    verify(pairwiseScoreRepository).saveAndFlush(captor.capture());
    assertEquals(-1.0, Double.parseDouble(captor.getValue().getScore()), 0.0);
    assertEquals("B\nReasoning.", captor.getValue().getLlmResponse());
    assertEquals(turnA, captor.getValue().getChatTurnA());
    assertEquals(turnB, captor.getValue().getChatTurnB());
  }

  @Test
  void generateScore_Failure_UpdatesStatus() {
    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-A", user)).thenReturn(turnA);
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            "llm-eval-1", user))
        .thenReturn(Optional.of(llmEvaluator));
    when(chatService.getChat("chat-A", user)).thenThrow(new RuntimeException("DB Failed"));

    // Act & Assert
    assertThrows(
        RuntimeException.class,
        () -> {
          evaluationService.generateScore(
              null, "turn-A", null, "llm-eval-1", "project-1", evalStatus, user);
        });

    verify(evaluationStatusService, times(1))
        .updateEvaluationStatus(eq(evalStatus), eq(EvaluationStatusEnum.IN_PROGRESS), anyString());
    verify(evaluationStatusService, times(1))
        .updateEvaluationStatus(
            eq(evalStatus), eq(EvaluationStatusEnum.FAILED), contains("DB Failed"));
    verify(evaluationStatusService, never())
        .updateEvaluationStatus(eq(evalStatus), eq(EvaluationStatusEnum.SUCCESSFUL), anyString());
  }

  @Test
  void getChatUpToTurn_ThrowsException_WhenTurnNotFound() {
    when(sxsPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.of(sxsPair));

    when(llmEvaluatorRepository.findPairwiseEvaluatorByIdAndUser("pairwise-eval-1", user))
        .thenReturn(Optional.of(pairwiseEvaluator));

    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-A", user)).thenReturn(turnA);
    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-B", user)).thenReturn(turnB);

    when(chatService.getChat("chat-A", user)).thenReturn(List.of(turnHistory));

    // Act & Assert
    assertThrows(
        IllegalInputException.class,
        () -> {
          evaluationService.generateScore(
              "pair-1", "turn-A", "turn-B", "pairwise-eval-1", "project-1", evalStatus, user);
        },
        "Chat turn not found in chat history");
  }

  @Test
  void generateScoresFromDataSet_Success() {
    String dataSetId = "dataset-1";
    List<String> turnIds = List.of("turn-A", "turn-B");
    List<String> evalIds = List.of("llm-eval-1");

    when(chatTurnRepository.findLatestChatTurnIdsByContainerId(dataSetId, user))
        .thenReturn(turnIds);
    when(evaluationBatchService.processDataSet(turnIds, evalIds, dataSetId, user))
        .thenReturn("job-ds-1");

    // Act
    ScorerResponseDTO response =
        evaluationService.generateScoresFromDataSet(dataSetId, evalIds, user);

    // Assert
    assertEquals("job-ds-1", response.getJobId());
    assertEquals(turnIds, response.getChatTurnIds());
    verify(evaluationBatchService).processDataSet(turnIds, evalIds, dataSetId, user);
  }

  @Test
  void generateScoresFromDataSet_Empty_ThrowsException() {
    String dataSetId = "dataset-1";
    List<String> evalIds = List.of("llm-eval-1");
    when(chatTurnRepository.findLatestChatTurnIdsByContainerId(dataSetId, user))
        .thenReturn(Collections.emptyList());

    // Act & Assert
    assertThrows(
        IllegalInputException.class,
        () -> {
          evaluationService.generateScoresFromDataSet(dataSetId, evalIds, user);
        });
  }

  @Test
  void generateScore_ChatTurnEvaluationRequest_CallsMainGenerateScore() {
    ChatTurnEvaluationRequest request = new ChatTurnEvaluationRequest();
    request.setChatTurnId("turn-A");
    request.setEvaluatorId("llm-eval-1");
    String projectId = "project-1";

    doReturn(new ChatTurnDTO())
        .when(evaluationService)
        .generateScore(
            eq("turn-A"), eq("llm-eval-1"), isNull(), isNull(), isNull(), eq(projectId), eq(user));

    // Act
    evaluationService.generateScore(request, projectId, user);

    // Assert
    verify(evaluationService)
        .generateScore(
            eq("turn-A"), eq("llm-eval-1"), isNull(), isNull(), isNull(), eq(projectId), eq(user));
  }

  @Test
  void generateScore_StringArguments_CallsMainGenerateScoreWithStatus() {
    String chatTurnId = "turn-A";
    String evaluatorId = "llm-eval-1";
    String projectId = "project-1";

    when(evaluationStatusService.createEvaluationStatus(
            evaluatorId, chatTurnId, user.getId(), projectId))
        .thenReturn(evalStatus);

    doReturn(new ChatTurnDTO())
        .when(evaluationService)
        .generateScore(
            isNull(),
            eq(chatTurnId),
            isNull(),
            eq(evaluatorId),
            eq(projectId),
            eq(evalStatus),
            eq(user));

    // Act
    evaluationService.generateScore(chatTurnId, evaluatorId, null, null, null, projectId, user);

    // Assert
    verify(evaluationStatusService)
        .createEvaluationStatus(evaluatorId, chatTurnId, user.getId(), projectId);
    verify(evaluationService)
        .generateScore(
            isNull(),
            eq(chatTurnId),
            isNull(),
            eq(evaluatorId),
            eq(projectId),
            eq(evalStatus),
            eq(user));
  }

  @Test
  void runSxsEvalFromQueue_Success() {
    String evaluatorId = "pairwise-eval-1";
    String pairId = "pair-1";
    String chatTurnIdA = "turn-A";
    String chatTurnIdB = "turn-B";

    when(sxsPairRepository.findByIdAndUser(pairId, user)).thenReturn(Optional.of(sxsPair));
    when(chatTurnRepository.findByChatTurnIdAndUserId(chatTurnIdA, user)).thenReturn(turnA);
    when(chatTurnRepository.findByChatTurnIdAndUserId(chatTurnIdB, user)).thenReturn(turnB);

    doReturn(new PairwiseScore())
        .when(evaluationService)
        .runSXSEval(evaluatorId, user, sxsPair, turnA, turnB, evalStatus);

    // Act
    evaluationService.runSxsEvalFromQueue(
        evaluatorId, pairId, chatTurnIdA, chatTurnIdB, user, evalStatus);

    // Assert
    verify(sxsPairRepository).findByIdAndUser(pairId, user);
    verify(chatTurnRepository).findByChatTurnIdAndUserId(chatTurnIdA, user);
    verify(chatTurnRepository).findByChatTurnIdAndUserId(chatTurnIdB, user);
    verify(evaluationService).runSXSEval(evaluatorId, user, sxsPair, turnA, turnB, evalStatus);
  }

  @Test
  void retrieveChatTurn_NotFound_ThrowsException() {
    String evaluatorId = "pairwise-eval-1";
    String pairId = "pair-1";
    String chatTurnIdA = "non-existent-turn";
    String chatTurnIdB = "turn-B";

    when(sxsPairRepository.findByIdAndUser(pairId, user)).thenReturn(Optional.of(sxsPair));
    when(chatTurnRepository.findByChatTurnIdAndUserId(chatTurnIdA, user)).thenReturn(null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> {
              evaluationService.runSxsEvalFromQueue(
                  evaluatorId, pairId, chatTurnIdA, chatTurnIdB, user, evalStatus);
            });
    assertTrue(ex.getMessage().contains("No chat turn found"));
  }

  @Test
  void getPairwiseLLMEvaluator_NotFound_ThrowsException() {
    when(sxsPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.of(sxsPair));
    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-A", user)).thenReturn(turnA);
    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-B", user)).thenReturn(turnB);
    when(llmEvaluatorRepository.findPairwiseEvaluatorByIdAndUser("non-existent-pairwise", user))
        .thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> {
              evaluationService.generateScore(
                  "pair-1",
                  "turn-A",
                  "turn-B",
                  "non-existent-pairwise",
                  "project-1",
                  evalStatus,
                  user);
            });
    assertTrue(
        ex.getMessage()
            .contains("Pairwise LLMEvaluator with id 'non-existent-pairwise' not found"));
  }

  @Test
  void getPairwiseLLMEvaluator_WrongType_ThrowsException() {
    pairwiseEvaluator.setOutputFormatType(ScoreType.Integer);

    when(sxsPairRepository.findByIdAndUser("pair-1", user)).thenReturn(Optional.of(sxsPair));
    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-A", user)).thenReturn(turnA);
    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-B", user)).thenReturn(turnB);
    when(llmEvaluatorRepository.findPairwiseEvaluatorByIdAndUser("pairwise-eval-1", user))
        .thenReturn(Optional.of(pairwiseEvaluator));

    // Act & Assert
    IllegalInputException ex =
        assertThrows(
            IllegalInputException.class,
            () -> {
              evaluationService.generateScore(
                  "pair-1", "turn-A", "turn-B", "pairwise-eval-1", "project-1", evalStatus, user);
            });
    assertTrue(ex.getMessage().contains("Unsupported LLM Evaluator output format type"));
  }

  @Test
  void handleHistoryVariable_SizeOne_ReturnsNA() {
    List<EvaluatorVariableDTO> historyVar = List.of(new EvaluatorVariableDTO("history", true));
    llmEvaluator.setVariables(ObjectMapperUtil.toJsonString(historyVar));
    ModelInput promptTemplate =
        new ModelInput(new Prompt(InputRole.USER, "History: {{history}}"), user);
    llmEvaluator.setInputs(List.of(promptTemplate));

    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-A", user)).thenReturn(turnA);
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            "llm-eval-1", user))
        .thenReturn(Optional.of(llmEvaluator));
    when(chatService.getChat("chat-A", user)).thenReturn(List.of(turnA));
    when(inferenceService.runEvaluationInference(any(), any(), anyList()))
        .thenReturn(new ChatResponseWithLatency("Good", null, null, null, null, 100.0));
    when(evaluationMonitoringService.saveEvaluationMonitoring(
            any(User.class),
            any(ChatResponseWithLatency.class),
            any(Model.class),
            any(LLMEvaluator.class),
            any(EvaluationContainer.class)))
        .thenReturn(new EvaluationMonitoring());
    when(scoreRepository.save(any(ScoreV2.class))).thenAnswer(inv -> inv.getArgument(0));

    mockedObjectMapperUtil
        .when(() -> ObjectMapperUtil.convertJsonStringToList(any(), eq(EvaluatorVariableDTO.class)))
        .thenReturn(historyVar);
    mockedObjectMapperUtil
        .when(
            () ->
                ObjectMapperUtil.convertJsonStringToList(anyString(), eq(OutputCategoryDTO.class)))
        .thenReturn(List.of());

    // Act
    evaluationService.generateScore(
        null, "turn-A", null, "llm-eval-1", "project-1", evalStatus, user);

    // Assert
    ArgumentCaptor<List<ModelInput>> captor = ArgumentCaptor.forClass(List.class);
    verify(inferenceService).runEvaluationInference(any(), any(), captor.capture());
    assertTrue(captor.getValue().get(0).getText().contains("History: N/A"));
  }

  @Test
  void handleMissingVariable_WhenRequiredOutputIsNull_ThrowsIllegalArgumentException() {
    List<EvaluatorVariableDTO> requiredOutputVar =
        List.of(new EvaluatorVariableDTO("output", true));
    llmEvaluator.setVariables(ObjectMapperUtil.toJsonString(requiredOutputVar));

    ModelInput promptTemplate =
        new ModelInput(new Prompt(InputRole.USER, "Output was: {{output}}"), user);
    llmEvaluator.setInputs(List.of(promptTemplate));

    assertNotNull(
        turnA.getModelResponse(), "ModelResponse должен существовать в setUp для этого теста");
    turnA.getModelResponse().setText(null);

    when(chatTurnRepository.findByChatTurnIdAndUserId("turn-A", user)).thenReturn(turnA);
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            "llm-eval-1", user))
        .thenReturn(Optional.of(llmEvaluator));
    when(chatService.getChat("chat-A", user)).thenReturn(List.of(turnA));

    mockedObjectMapperUtil
        .when(() -> ObjectMapperUtil.convertJsonStringToList(any(), eq(EvaluatorVariableDTO.class)))
        .thenReturn(requiredOutputVar);

    // Act & Assert
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              evaluationService.generateScore(
                  null, "turn-A", null, "llm-eval-1", "project-1", evalStatus, user);
            });

    // Assert
    assertTrue(ex.getMessage().contains("Variable 'output' is required could not be extracted"));

    verify(inferenceService, never()).runEvaluationInference(any(), any(), any());
  }
}

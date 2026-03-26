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

package com.planck.planck.domain.evaluator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.planck.planck.domain.evaluation.PairwiseScoreRepository;
import com.planck.planck.domain.evaluator.dto.EvaluatorVariableDTO;
import com.planck.planck.domain.evaluator.dto.OutputCategoryDTO;
import com.planck.planck.domain.evaluator.pairwise.PairwiseLLMEvaluatorRepository;
import com.planck.planck.domain.evaluator.pairwise.dto.PairwiseLLMEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.pairwise.dto.PairwiseLLMEvaluatorResponseDTO;
import com.planck.planck.domain.evaluator.pairwise.service.PairwiseLLMEvaluatorServiceImpl;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.service.ModelInputService;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.PairwiseLLMEvaluator;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InputRole;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoreType;
import com.planck.planck.exceptions.EvaluatorNameExistException;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PairwiseLLMEvaluatorServiceImplTest {

  @Mock private PairwiseLLMEvaluatorRepository pairwiseLlmEvaluatorRepository;
  @Mock private PairwiseScoreRepository pairwiseScoreRepository;
  @Mock private ModelService modelService;
  @Mock private ModelInputService modelInputService;

  @InjectMocks private PairwiseLLMEvaluatorServiceImpl evaluatorService;

  private User testUser;
  private Model testModel;
  private PairwiseLLMEvaluator testEvaluator;
  private PairwiseLLMEvaluatorRequestDTO testRequest;
  private List<Prompt> testPrompts;
  private List<EvaluatorVariableDTO> testVariables;
  private List<OutputCategoryDTO> testOutputCategories;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("user-123");

    testModel = new Model();
    testModel.setId("model-123");
    testModel.setDeprecated(false);

    testPrompts = List.of(new Prompt(InputRole.SYSTEM, "You are a judge."));
    testVariables = new ArrayList<>();
    EvaluatorVariableDTO var1 = new EvaluatorVariableDTO();
    var1.name = "response";
    var1.required = true;
    testVariables.add(var1);

    testOutputCategories = new ArrayList<>();
    OutputCategoryDTO catA = new OutputCategoryDTO();
    catA.name = "Choice A";
    catA.value = "A";
    testOutputCategories.add(catA);

    testRequest = new PairwiseLLMEvaluatorRequestDTO();
    testRequest.setName("Test Evaluator");
    testRequest.setDescription("Test Description");
    testRequest.setModelId("model-123");
    testRequest.setPrompts(testPrompts);
    testRequest.setVariables(testVariables);
    testRequest.setOutputCategories(testOutputCategories);
    testRequest.setOutputFormateType(ScoreType.Json);

    testEvaluator = new PairwiseLLMEvaluator();
    testEvaluator.setId("eval-123");
    testEvaluator.setName("Test Evaluator");
    testEvaluator.setUser(testUser);
    testEvaluator.setType(ScopeType.USER);
    testEvaluator.setModel(testModel);
  }

  // --- createPairwiseLLMEvaluator ---

  @Test
  void createPairwiseLLMEvaluator_Success() {
    when(modelService.getModelForUser(testUser, "model-123")).thenReturn(testModel);
    when(pairwiseLlmEvaluatorRepository.checkIfPairwiseLLMEvaluatorNameExists(
            "Test Evaluator", testUser, ScopeType.SYSTEM))
        .thenReturn(false);
    when(modelInputService.savePrompt(eq(testUser), any(Prompt.class)))
        .thenReturn(new ModelInput());
    when(pairwiseLlmEvaluatorRepository.save(any(PairwiseLLMEvaluator.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    ArgumentCaptor<PairwiseLLMEvaluator> evaluatorCaptor =
        ArgumentCaptor.forClass(PairwiseLLMEvaluator.class);

    PairwiseLLMEvaluatorResponseDTO response =
        evaluatorService.createPairwiseLLMEvaluator(testRequest, testUser);

    assertNotNull(response);
    assertEquals("Test Evaluator", response.getName());
    verify(pairwiseLlmEvaluatorRepository).save(evaluatorCaptor.capture());

    PairwiseLLMEvaluator capturedEvaluator = evaluatorCaptor.getValue();
    assertEquals(testUser, capturedEvaluator.getUser());
    assertEquals(testModel, capturedEvaluator.getModel());
    assertEquals(ScopeType.USER, capturedEvaluator.getType());
    assertEquals(ScoreType.Json, capturedEvaluator.getOutputFormatType());
    assertNotNull(capturedEvaluator.getVariables());
    assertNotNull(capturedEvaluator.getOutputCategories());
    assertEquals(1, capturedEvaluator.getInputs().size());
  }

  @Test
  void createPairwiseLLMEvaluator_ModelNotFound() {
    when(modelService.getModelForUser(testUser, "model-123"))
        .thenThrow(new NotFoundException("Model not found"));

    assertThrows(
        NotFoundException.class,
        () -> evaluatorService.createPairwiseLLMEvaluator(testRequest, testUser));
  }

  @Test
  void createPairwiseLLMEvaluator_ModelDeprecated() {
    testModel.setDeprecated(true);
    when(modelService.getModelForUser(testUser, "model-123")).thenReturn(testModel);

    assertThrows(
        NotFoundException.class,
        () -> evaluatorService.createPairwiseLLMEvaluator(testRequest, testUser));
  }

  @Test
  void createPairwiseLLMEvaluator_NameExists() {
    when(modelService.getModelForUser(testUser, "model-123")).thenReturn(testModel);
    when(pairwiseLlmEvaluatorRepository.checkIfPairwiseLLMEvaluatorNameExists(
            "Test Evaluator", testUser, ScopeType.SYSTEM))
        .thenReturn(true);

    assertThrows(
        EvaluatorNameExistException.class,
        () -> evaluatorService.createPairwiseLLMEvaluator(testRequest, testUser));
  }

  // --- getAllPairwiseLLMEvaluators ---

  @Test
  void getAllPairwiseLLMEvaluators_Success() {
    List<PairwiseLLMEvaluator> evaluators = List.of(testEvaluator);
    when(pairwiseLlmEvaluatorRepository.findAllByUserOrType(testUser, ScopeType.SYSTEM))
        .thenReturn(evaluators);

    List<PairwiseLLMEvaluator> result = evaluatorService.getAllPairwiseLLMEvaluators(testUser);

    assertEquals(1, result.size());
    assertEquals(testEvaluator, result.get(0));
    verify(pairwiseLlmEvaluatorRepository).findAllByUserOrType(testUser, ScopeType.SYSTEM);
  }

  // --- getEvaluatorById ---

  @Test
  void getEvaluatorById_Success() {
    when(pairwiseLlmEvaluatorRepository.findById("eval-123"))
        .thenReturn(Optional.of(testEvaluator));

    PairwiseLLMEvaluatorResponseDTO response =
        evaluatorService.getEvaluatorById(testUser, "eval-123");

    assertNotNull(response);
    assertEquals("eval-123", response.getId());
    assertEquals("Test Evaluator", response.getName());
  }

  @Test
  void getEvaluatorById_NotFound() {
    when(pairwiseLlmEvaluatorRepository.findById("eval-123")).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> evaluatorService.getEvaluatorById(testUser, "eval-123"));
  }

  // --- getEvaluatorByUserOrSystemType ---

  @Test
  void getEvaluatorByUserOrSystemType_Success() {
    List<PairwiseLLMEvaluator> evaluators = List.of(testEvaluator);
    when(pairwiseLlmEvaluatorRepository.findAllByUserOrType(testUser, ScopeType.SYSTEM))
        .thenReturn(evaluators);

    List<PairwiseLLMEvaluatorResponseDTO> result =
        evaluatorService.getEvaluatorByUserOrSystemType(testUser);

    assertEquals(1, result.size());
    assertEquals("eval-123", result.get(0).getId());
  }

  // --- updatePairwiseLLMEvaluator ---

  @Test
  void updatePairwiseLLMEvaluator_Success() {
    PairwiseLLMEvaluatorRequestDTO updateRequest = new PairwiseLLMEvaluatorRequestDTO();
    updateRequest.setName("New Name");
    updateRequest.setDescription("New Description");
    updateRequest.setModelId("model-456");

    Model newModel = new Model();
    newModel.setId("model-456");
    newModel.setDeprecated(false);

    when(pairwiseLlmEvaluatorRepository.findById("eval-123"))
        .thenReturn(Optional.of(testEvaluator));
    when(pairwiseLlmEvaluatorRepository.checkIfPairwiseLLMEvaluatorNameExists(
            "New Name", testUser, ScopeType.SYSTEM))
        .thenReturn(false);
    when(modelService.getModelForUser(testUser, "model-456")).thenReturn(newModel);
    when(pairwiseLlmEvaluatorRepository.save(any(PairwiseLLMEvaluator.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    ArgumentCaptor<PairwiseLLMEvaluator> evaluatorCaptor =
        ArgumentCaptor.forClass(PairwiseLLMEvaluator.class);

    PairwiseLLMEvaluatorResponseDTO response =
        evaluatorService.updatePairwiseLLMEvaluator(testUser, "eval-123", updateRequest);

    assertNotNull(response);
    assertEquals("New Name", response.getName());
    assertEquals("New Description", response.getDescription());

    verify(pairwiseLlmEvaluatorRepository).save(evaluatorCaptor.capture());
    PairwiseLLMEvaluator captured = evaluatorCaptor.getValue();

    assertEquals("New Name", captured.getName());
    assertEquals("New Description", captured.getDescription());
    assertEquals(newModel, captured.getModel());
  }

  @Test
  void updatePairwiseLLMEvaluator_SystemEvaluator_ThrowsException() {
    testEvaluator.setType(ScopeType.SYSTEM);
    when(pairwiseLlmEvaluatorRepository.findById("eval-123"))
        .thenReturn(Optional.of(testEvaluator));

    assertThrows(
        IllegalInputException.class,
        () -> evaluatorService.updatePairwiseLLMEvaluator(testUser, "eval-123", testRequest));
  }

  @Test
  void updatePairwiseLLMEvaluator_NameExists_ThrowsException() {
    testRequest.setName("Existing Name");
    when(pairwiseLlmEvaluatorRepository.findById("eval-123"))
        .thenReturn(Optional.of(testEvaluator));
    when(pairwiseLlmEvaluatorRepository.checkIfPairwiseLLMEvaluatorNameExists(
            "Existing Name", testUser, ScopeType.SYSTEM))
        .thenReturn(true);

    assertThrows(
        EvaluatorNameExistException.class,
        () -> evaluatorService.updatePairwiseLLMEvaluator(testUser, "eval-123", testRequest));
  }

  @Test
  void updatePairwiseLLMEvaluator_NotFound_ThrowsException() {
    when(pairwiseLlmEvaluatorRepository.findById("eval-123")).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> evaluatorService.updatePairwiseLLMEvaluator(testUser, "eval-123", testRequest));
  }

  // --- removePairwiseLLMEvaluatorById ---

  @Test
  void removePairwiseLLMEvaluatorById_Success_Delete() {
    when(pairwiseLlmEvaluatorRepository.findById("eval-123"))
        .thenReturn(Optional.of(testEvaluator));
    when(pairwiseScoreRepository.existsByEvaluatorId("eval-123", testUser.getId()))
        .thenReturn(false);

    evaluatorService.removePairwiseLLMEvaluatorById(testUser, "eval-123");

    verify(pairwiseLlmEvaluatorRepository).delete(testEvaluator);
    verify(pairwiseLlmEvaluatorRepository, never()).save(any());
  }

  @Test
  void removePairwiseLLMEvaluatorById_Success_Deprecate() {
    when(pairwiseLlmEvaluatorRepository.findById("eval-123"))
        .thenReturn(Optional.of(testEvaluator));
    when(pairwiseScoreRepository.existsByEvaluatorId("eval-123", testUser.getId()))
        .thenReturn(true);

    evaluatorService.removePairwiseLLMEvaluatorById(testUser, "eval-123");

    verify(pairwiseLlmEvaluatorRepository).save(testEvaluator);
    verify(pairwiseLlmEvaluatorRepository, never()).delete(any());
    assertTrue(testEvaluator.isDeprecated());
  }

  @Test
  void removePairwiseLLMEvaluatorById_SystemEvaluator_ThrowsException() {
    testEvaluator.setType(ScopeType.SYSTEM);
    when(pairwiseLlmEvaluatorRepository.findById("eval-123"))
        .thenReturn(Optional.of(testEvaluator));

    assertThrows(
        IllegalInputException.class,
        () -> evaluatorService.removePairwiseLLMEvaluatorById(testUser, "eval-123"));
  }

  @Test
  void removePairwiseLLMEvaluatorById_NotFound_ThrowsException() {
    when(pairwiseLlmEvaluatorRepository.findById("eval-123")).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> evaluatorService.removePairwiseLLMEvaluatorById(testUser, "eval-123"));
  }
}

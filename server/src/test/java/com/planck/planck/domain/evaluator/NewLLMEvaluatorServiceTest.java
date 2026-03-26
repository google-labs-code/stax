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

import com.planck.planck.domain.evaluationstatus.EvaluationStatusRepository;
import com.planck.planck.domain.evaluator.dto.EvaluatorVariableDTO;
import com.planck.planck.domain.evaluator.dto.OutputCategoryDTO;
import com.planck.planck.domain.evaluator.llm.LLMEvaluatorRepository;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorResponseDTO;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorUpdateDTO;
import com.planck.planck.domain.evaluator.llm.service.NewLLMEvaluatorServiceImpl;
import com.planck.planck.domain.model.service.ModelService;
import com.planck.planck.domain.modelinput.service.ModelInputService;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InputRole;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoreType;
import com.planck.planck.exceptions.EvaluatorNameExistException;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.llmproviders.dto.Prompt;
import com.planck.planck.util.CustomScorerStringConstant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewLLMEvaluatorServiceTest {

  @Mock private LLMEvaluatorRepository llmEvaluatorRepository;
  @Mock private ModelService modelService;
  @Mock private ModelInputService modelInputService;
  @Mock private EvaluationStatusRepository evaluationStatusRepository;

  @InjectMocks private NewLLMEvaluatorServiceImpl newLLMEvaluatorService;

  private User testUser;
  private Model testModel;
  private LLMEvaluatorRequestDTO testRequestDTO;
  private LLMEvaluator testEvaluator;
  private String modelId = "model-123";
  private String evaluatorId = "eval-abc";
  private String evaluatorName = "Test Evaluator";
  private String evaluatorDescrption = "Test Description";

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("user-xyz");
    testUser.setEmail("test@example.com");

    testModel = new Model();
    testModel.setId(modelId);
    testModel.setName("Test Model");
    testModel.setDeprecated(false);

    Prompt prompt = new Prompt();
    prompt.setRole(InputRole.USER);
    prompt.setText("Evaluate this: {{input}}");

    testRequestDTO = new LLMEvaluatorRequestDTO();
    testRequestDTO.setName(evaluatorName);
    testRequestDTO.setDescription("Test Description");
    testRequestDTO.setModelId(modelId);
    testRequestDTO.setPrompts(List.of(prompt));
    testRequestDTO.setOutputFormateType(ScoreType.Double); // Example ScoreType
    testRequestDTO.setVariables(new ArrayList<>()); // Assuming empty for simplicity
    testRequestDTO.setOutputCategories(new ArrayList<>()); // Assuming empty for simplicity

    testEvaluator = new LLMEvaluator();
    testEvaluator.setId(evaluatorId);
    testEvaluator.setName(evaluatorName);
    testEvaluator.setDescription(evaluatorDescrption);
    testEvaluator.setUser(testUser);
    testEvaluator.setModel(testModel);
    testEvaluator.setType(ScopeType.USER);
    testEvaluator.setInputs(List.of(new ModelInput(prompt, testUser)));
    testEvaluator.setOutputFormatType(ScoreType.Double);
    testEvaluator.setVariables("[]");
    testEvaluator.setOutputCategories("[]");
  }

  // --- createLLMEvaluator Tests ---

  @Test
  void createLLMEvaluator_Success() {
    // Arrange
    ModelInput savedModelInput = new ModelInput(testRequestDTO.getPrompts().get(0), testUser);
    when(modelService.getModelForUser(testUser, modelId)).thenReturn(testModel);
    when(llmEvaluatorRepository.checkIfLLMEvaluatorNameExists(
            evaluatorName, testUser, ScopeType.SYSTEM))
        .thenReturn(false);
    when(modelInputService.savePrompt(eq(testUser), any(Prompt.class))).thenReturn(savedModelInput);
    when(llmEvaluatorRepository.save(any(LLMEvaluator.class)))
        .thenAnswer(
            invocation -> {
              LLMEvaluator saved = invocation.getArgument(0);
              saved.setId(evaluatorId); // Simulate saving and getting an ID
              return saved;
            });

    // Act
    LLMEvaluatorResponseDTO responseDTO =
        newLLMEvaluatorService.createLLMEvaluator(testRequestDTO, testUser);

    // Assert
    assertNotNull(responseDTO);
    assertEquals(evaluatorId, responseDTO.getId());
    assertEquals(evaluatorName, responseDTO.getName());
    assertEquals(modelId, responseDTO.getModelDTO().getId());
    assertEquals(ScopeType.USER, responseDTO.getType());
    assertFalse(responseDTO.getPrompts().isEmpty());

    verify(modelService).getModelForUser(testUser, modelId);
    verify(llmEvaluatorRepository)
        .checkIfLLMEvaluatorNameExists(evaluatorName, testUser, ScopeType.SYSTEM);
    verify(modelInputService, times(testRequestDTO.getPrompts().size()))
        .savePrompt(eq(testUser), any(Prompt.class));
    verify(llmEvaluatorRepository).save(any(LLMEvaluator.class));
  }

  @Test
  void createLLMEvaluator_ModelNotFound() {
    // Arrange
    when(modelService.getModelForUser(testUser, modelId))
        .thenThrow(new NotFoundException("Model not found"));

    // Act & Assert
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> {
              newLLMEvaluatorService.createLLMEvaluator(testRequestDTO, testUser);
            });
    assertEquals("Model not found", exception.getMessage());
    verify(modelService).getModelForUser(testUser, modelId);
    verifyNoInteractions(llmEvaluatorRepository, modelInputService);
  }

  @Test
  void createLLMEvaluator_ModelDeprecated() {
    // Arrange
    testModel.setDeprecated(true);
    when(modelService.getModelForUser(testUser, modelId)).thenReturn(testModel);

    // Act & Assert
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> {
              newLLMEvaluatorService.createLLMEvaluator(testRequestDTO, testUser);
            });
    assertEquals("Model is deprecated", exception.getMessage());
    verify(modelService).getModelForUser(testUser, modelId);
    verifyNoInteractions(llmEvaluatorRepository, modelInputService);
  }

  @Test
  void createLLMEvaluator_NameExists() {
    // Arrange
    when(modelService.getModelForUser(testUser, modelId)).thenReturn(testModel);
    when(llmEvaluatorRepository.checkIfLLMEvaluatorNameExists(
            evaluatorName, testUser, ScopeType.SYSTEM))
        .thenReturn(true);

    // Act & Assert
    EvaluatorNameExistException exception =
        assertThrows(
            EvaluatorNameExistException.class,
            () -> {
              newLLMEvaluatorService.createLLMEvaluator(testRequestDTO, testUser);
            });
    assertEquals(CustomScorerStringConstant.SCRORE_NAME_ALREADY_EXIST, exception.getMessage());
    verify(modelService).getModelForUser(testUser, modelId);
    verify(llmEvaluatorRepository)
        .checkIfLLMEvaluatorNameExists(evaluatorName, testUser, ScopeType.SYSTEM);
    verifyNoMoreInteractions(llmEvaluatorRepository);
    verifyNoInteractions(modelInputService);
  }

  // --- checkIfLLMEvaluatorNameExists Tests ---

  @Test
  void checkIfLLMEvaluatorNameExists_ReturnsTrue() {
    // Arrange
    when(llmEvaluatorRepository.checkIfLLMEvaluatorNameExists(
            evaluatorName, testUser, ScopeType.SYSTEM))
        .thenReturn(true);

    // Act
    boolean exists = newLLMEvaluatorService.checkIfLLMEvaluatorNameExists(evaluatorName, testUser);

    // Assert
    assertTrue(exists);
    verify(llmEvaluatorRepository)
        .checkIfLLMEvaluatorNameExists(evaluatorName, testUser, ScopeType.SYSTEM);
  }

  @Test
  void checkIfLLMEvaluatorNameExists_ReturnsFalse() {
    // Arrange
    when(llmEvaluatorRepository.checkIfLLMEvaluatorNameExists(
            evaluatorName, testUser, ScopeType.SYSTEM))
        .thenReturn(false);

    // Act
    boolean exists = newLLMEvaluatorService.checkIfLLMEvaluatorNameExists(evaluatorName, testUser);

    // Assert
    assertFalse(exists);
    verify(llmEvaluatorRepository)
        .checkIfLLMEvaluatorNameExists(evaluatorName, testUser, ScopeType.SYSTEM);
  }

  // --- getAllLLMScorer (getAllLLMEvaluators) Tests ---

  @Test
  void getAllLLMScorer_ReturnsList() {
    // Arrange
    List<LLMEvaluator> evaluators = List.of(testEvaluator);
    when(llmEvaluatorRepository.findAllByUserOrType(testUser, ScopeType.SYSTEM))
        .thenReturn(evaluators);

    // Act
    List<LLMEvaluator> result =
        newLLMEvaluatorService.getAllLLMScorer(testUser); // Method name mismatch?

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(evaluatorId, result.get(0).getId());
    verify(llmEvaluatorRepository).findAllByUserOrType(testUser, ScopeType.SYSTEM);
  }

  @Test
  void getAllLLMScorer_ReturnsEmptyList() {
    // Arrange
    when(llmEvaluatorRepository.findAllByUserOrType(testUser, ScopeType.SYSTEM))
        .thenReturn(Collections.emptyList());

    // Act
    List<LLMEvaluator> result = newLLMEvaluatorService.getAllLLMScorer(testUser);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(llmEvaluatorRepository).findAllByUserOrType(testUser, ScopeType.SYSTEM);
  }

  // --- getById Tests ---

  @Test
  void getById_Success() {
    // Arrange
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            evaluatorId, testUser))
        .thenReturn(Optional.of(testEvaluator));

    // Act
    LLMEvaluator result = newLLMEvaluatorService.getById(testUser, evaluatorId);

    // Assert
    assertNotNull(result);
    assertEquals(evaluatorId, result.getId());
    verify(llmEvaluatorRepository)
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, testUser);
  }

  @Test
  void getById_NotFound() {
    // Arrange
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            evaluatorId, testUser))
        .thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> {
              newLLMEvaluatorService.getById(testUser, evaluatorId);
            });
    assertEquals("evaluator: " + evaluatorId + " does not exists", exception.getMessage());
    verify(llmEvaluatorRepository)
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, testUser);
  }

  // --- getEvaluatorById (DTO) Tests ---

  @Test
  void getEvaluatorById_DTO_Success() {
    // Arrange
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            evaluatorId, testUser))
        .thenReturn(Optional.of(testEvaluator));

    // Act
    LLMEvaluatorResponseDTO result = newLLMEvaluatorService.getEvaluatorById(testUser, evaluatorId);

    // Assert
    assertNotNull(result);
    assertEquals(evaluatorId, result.getId());
    assertEquals(evaluatorName, result.getName());
    verify(llmEvaluatorRepository)
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, testUser);
  }

  @Test
  void getEvaluatorById_DTO_NotFound() {
    // Arrange
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            evaluatorId, testUser))
        .thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> {
              newLLMEvaluatorService.getEvaluatorById(testUser, evaluatorId);
            });
    assertEquals("evaluator: " + evaluatorId + " does not exists", exception.getMessage());
    verify(llmEvaluatorRepository)
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, testUser);
  }

  @Test
  void getEvaluatorById_DTO_SecurityCheck_PreventsUnauthorizedAccess() {
    // Arrange - Create a different user
    User otherUser = new User();
    otherUser.setId("other-user-id");
    otherUser.setEmail("otheruser@example.com");

    // Mock that evaluator exists but belongs to different user
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            evaluatorId, otherUser))
        .thenReturn(Optional.empty());

    // Act & Assert - Should throw NotFoundException when user doesn't own evaluator
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> {
              newLLMEvaluatorService.getEvaluatorById(otherUser, evaluatorId);
            });
    assertEquals("evaluator: " + evaluatorId + " does not exists", exception.getMessage());
    verify(llmEvaluatorRepository)
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, otherUser);
  }

  @Test
  void getEvaluatorById_DTO_SecurityCheck_AllowsSystemEvaluatorAccess() {
    // Arrange - Create system evaluator
    LLMEvaluator systemEvaluator = new LLMEvaluator();
    systemEvaluator.setId(evaluatorId);
    systemEvaluator.setName(evaluatorName);
    systemEvaluator.setType(ScopeType.SYSTEM);
    systemEvaluator.setModel(new Model());
    systemEvaluator.setInputs(new ArrayList<>());

    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            evaluatorId, testUser))
        .thenReturn(Optional.of(systemEvaluator));

    // Act
    LLMEvaluatorResponseDTO result = newLLMEvaluatorService.getEvaluatorById(testUser, evaluatorId);

    // Assert - Should allow access to system evaluators
    assertNotNull(result);
    assertEquals(evaluatorId, result.getId());
    assertEquals(evaluatorName, result.getName());
    verify(llmEvaluatorRepository)
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, testUser);
  }

  // --- getByName Tests ---

  @Test
  void getByName_Success() {
    // Arrange
    when(llmEvaluatorRepository.findByNameAndUserOrType(evaluatorName, testUser, ScopeType.SYSTEM))
        .thenReturn(Optional.of(testEvaluator));

    // Act
    LLMEvaluator result = newLLMEvaluatorService.getByName(testUser, evaluatorName);

    // Assert
    assertNotNull(result);
    assertEquals(evaluatorName, result.getName());
    verify(llmEvaluatorRepository)
        .findByNameAndUserOrType(evaluatorName, testUser, ScopeType.SYSTEM);
  }

  @Test
  void getByName_NotFound() {
    // Arrange
    when(llmEvaluatorRepository.findByNameAndUserOrType(evaluatorName, testUser, ScopeType.SYSTEM))
        .thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> {
              newLLMEvaluatorService.getByName(testUser, evaluatorName);
            });
    assertEquals("evaluator: " + evaluatorName + " does not exists", exception.getMessage());
    verify(llmEvaluatorRepository)
        .findByNameAndUserOrType(evaluatorName, testUser, ScopeType.SYSTEM);
  }

  // --- updateLLMEvaluator Tests ---

  @Test
  void updateLLMEvaluator_Success() {
    // Arrange
    LLMEvaluatorUpdateDTO updateDTO = new LLMEvaluatorUpdateDTO();
    updateDTO.setName("Updated Name");
    updateDTO.setDescription("Updated Description");
    updateDTO.setOutputFormateType(ScoreType.Integer);
    updateDTO.setVariables(new ArrayList<>());
    updateDTO.setOutputCategories(new ArrayList<>());

    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            evaluatorId, testUser))
        .thenReturn(Optional.of(testEvaluator));
    when(llmEvaluatorRepository.save(any(LLMEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    LLMEvaluatorResponseDTO responseDTO =
        newLLMEvaluatorService.updateLLMEvaluator(updateDTO, evaluatorId, testUser);

    // Assert
    assertNotNull(responseDTO);
    assertEquals(evaluatorId, responseDTO.getId());
    assertEquals("Updated Name", responseDTO.getName());
    assertEquals("Updated Description", responseDTO.getDescription());

    verify(llmEvaluatorRepository)
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, testUser);
    verify(llmEvaluatorRepository).save(any(LLMEvaluator.class));
  }

  @Test
  void updateLLMEvaluator_allFields_Success() {
    // Arrange
    LLMEvaluatorUpdateDTO updateDTO = new LLMEvaluatorUpdateDTO();
    updateDTO.setOutputFormateType(ScoreType.Double);

    List<OutputCategoryDTO> outputCategories = new ArrayList<>();
    OutputCategoryDTO outputCategory = new OutputCategoryDTO();
    outputCategories.add(outputCategory);
    updateDTO.setOutputCategories(outputCategories);
    List<EvaluatorVariableDTO> variables = new ArrayList<>();
    EvaluatorVariableDTO variable = new EvaluatorVariableDTO();
    variables.add(variable);
    updateDTO.setVariables(variables);

    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            evaluatorId, testUser))
        .thenReturn(Optional.of(testEvaluator));
    when(llmEvaluatorRepository.save(any(LLMEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    LLMEvaluatorResponseDTO responseDTO =
        newLLMEvaluatorService.updateLLMEvaluator(updateDTO, evaluatorId, testUser);

    // Assert
    assertNotNull(responseDTO);
    assertEquals(evaluatorId, responseDTO.getId());
    assertEquals("Test Evaluator", responseDTO.getName());
    assertEquals("Test Description", responseDTO.getDescription());

    verify(llmEvaluatorRepository)
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, testUser);
    verify(llmEvaluatorRepository).save(any(LLMEvaluator.class));
  }

  @Test
  void updateLLMEvaluator_NoInput_Success() {
    // Arrange
    LLMEvaluatorUpdateDTO updateDTO = new LLMEvaluatorUpdateDTO();

    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            evaluatorId, testUser))
        .thenReturn(Optional.of(testEvaluator));
    when(llmEvaluatorRepository.save(any(LLMEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    LLMEvaluatorResponseDTO responseDTO =
        newLLMEvaluatorService.updateLLMEvaluator(updateDTO, evaluatorId, testUser);

    // Assert
    assertNotNull(responseDTO);
    assertEquals(evaluatorId, responseDTO.getId());
    assertEquals("Test Evaluator", responseDTO.getName());

    verify(llmEvaluatorRepository)
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, testUser);
    verify(llmEvaluatorRepository).save(any(LLMEvaluator.class));
  }

  @Test
  void updateLLMEvaluator_NotFound() {
    // Arrange
    LLMEvaluatorUpdateDTO updateDTO = new LLMEvaluatorUpdateDTO();
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            evaluatorId, testUser))
        .thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> {
              newLLMEvaluatorService.updateLLMEvaluator(updateDTO, evaluatorId, testUser);
            });
    assertEquals("Evaluator: " + evaluatorId + " does not exists", exception.getMessage());
    verify(llmEvaluatorRepository)
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, testUser);
    verifyNoMoreInteractions(llmEvaluatorRepository);
  }

  @Test
  void updateLLMEvaluator_SystemEvaluator() {
    // Arrange
    testEvaluator.setType(ScopeType.SYSTEM);
    LLMEvaluatorUpdateDTO updateDTO = new LLMEvaluatorUpdateDTO();
    when(llmEvaluatorRepository.findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(
            evaluatorId, testUser))
        .thenReturn(Optional.of(testEvaluator));

    // Act & Assert
    IllegalInputException exception =
        assertThrows(
            IllegalInputException.class,
            () -> {
              newLLMEvaluatorService.updateLLMEvaluator(updateDTO, evaluatorId, testUser);
            });
    assertEquals("System Evaluator cannot be updated", exception.getMessage());
    verify(llmEvaluatorRepository)
        .findSystemEvaluatorByIdOrUserEvaluatorByIdAndUser(evaluatorId, testUser);
    verifyNoMoreInteractions(llmEvaluatorRepository);
  }

  // --- getEvaluatorByUserOrSystemType Tests ---
  @Test
  void getEvaluatorByUserOrSystemType_ReturnsCombinedList() {
    // Arrange
    LLMEvaluator systemEvaluator = new LLMEvaluator();
    systemEvaluator.setId("sys-1");
    systemEvaluator.setType(ScopeType.SYSTEM);
    systemEvaluator.setModel(new Model());
    systemEvaluator.setInputs(new ArrayList<>());
    LLMEvaluator userEvaluator = new LLMEvaluator();
    userEvaluator.setId("user-1");
    userEvaluator.setType(ScopeType.USER);
    userEvaluator.setUser(testUser);
    userEvaluator.setModel(new Model());
    userEvaluator.setInputs(new ArrayList<>());
    List<LLMEvaluator> evaluators = List.of(systemEvaluator, userEvaluator);
    when(llmEvaluatorRepository.findAllByUserOrType(testUser, ScopeType.SYSTEM))
        .thenReturn(evaluators);

    // Act
    List<LLMEvaluatorResponseDTO> result =
        newLLMEvaluatorService.getEvaluatorByUserOrSystemType(testUser);

    // Assert
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(dto -> dto.getId().equals("sys-1")));
    assertTrue(result.stream().anyMatch(dto -> dto.getId().equals("user-1")));
    verify(llmEvaluatorRepository).findAllByUserOrType(testUser, ScopeType.SYSTEM);
  }

  // --- getEvaluatorBySystemType Tests ---
  @Test
  void getEvaluatorBySystemType_ReturnsSystemList() {
    // Arrange
    LLMEvaluator systemEvaluator = new LLMEvaluator();
    systemEvaluator.setId("sys-1");
    systemEvaluator.setType(ScopeType.SYSTEM);
    systemEvaluator.setModel(new Model());
    systemEvaluator.setInputs(new ArrayList<>());
    List<LLMEvaluator> evaluators = List.of(systemEvaluator);
    when(llmEvaluatorRepository.findAllNonDeprecatedByType(ScopeType.SYSTEM))
        .thenReturn(evaluators);

    // Act
    List<LLMEvaluatorResponseDTO> result = newLLMEvaluatorService.getEvaluatorBySystemType();
    System.out.println("getEvaluatorBySystemType_ReturnsSystemList_RESULT: " + result);

    // Assert
    assertEquals(1, result.size());
    assertEquals("sys-1", result.get(0).getId());
    verify(llmEvaluatorRepository).findAllNonDeprecatedByType(ScopeType.SYSTEM);
  }

  // --- getEvaluatorByUser Tests ---
  @Test
  void getEvaluatorByUser_ReturnsUserList() {
    // Arrange
    LLMEvaluator userEvaluator = new LLMEvaluator();
    userEvaluator.setId("user-1");
    userEvaluator.setType(ScopeType.USER);
    userEvaluator.setUser(testUser);
    userEvaluator.setModel(new Model());
    userEvaluator.setInputs(new ArrayList<>());
    List<LLMEvaluator> evaluators = List.of(userEvaluator);
    when(llmEvaluatorRepository.findAllNonDeprecatedByUser(testUser)).thenReturn(evaluators);

    // Act
    List<LLMEvaluatorResponseDTO> result = newLLMEvaluatorService.getEvaluatorByUser(testUser);

    // Assert
    assertEquals(1, result.size());
    assertEquals("user-1", result.get(0).getId());
    verify(llmEvaluatorRepository).findAllNonDeprecatedByUser(testUser);
  }

  // --- removeLLMEvaluatorById Tests ---

  @Test
  void removeLLMEvaluatorById_WhenUsed_ShouldSetDeprecated() {
    // Arrange
    when(llmEvaluatorRepository.findByIdAndUser(evaluatorId, testUser))
        .thenReturn(Optional.of(testEvaluator));
    when(evaluationStatusRepository.existsByEvaluatorId(evaluatorId)).thenReturn(true);

    // Act
    newLLMEvaluatorService.removeLLMEvaluatorById(testUser, evaluatorId);

    // Assert
    assertTrue(testEvaluator.isDeprecated());
    verify(llmEvaluatorRepository).save(testEvaluator);
    verify(llmEvaluatorRepository, never()).delete(testEvaluator);
    verify(evaluationStatusRepository).existsByEvaluatorId(evaluatorId);
  }

  @Test
  void removeLLMEvaluatorById_WhenNotUsed_ShouldDelete() {
    // Arrange
    when(llmEvaluatorRepository.findByIdAndUser(evaluatorId, testUser))
        .thenReturn(Optional.of(testEvaluator));
    when(evaluationStatusRepository.existsByEvaluatorId(evaluatorId)).thenReturn(false);

    // Act
    newLLMEvaluatorService.removeLLMEvaluatorById(testUser, evaluatorId);

    // Assert
    assertFalse(testEvaluator.isDeprecated());
    verify(llmEvaluatorRepository).delete(testEvaluator);
    verify(llmEvaluatorRepository, never()).save(testEvaluator);
    verify(evaluationStatusRepository).existsByEvaluatorId(evaluatorId);
  }

  @Test
  void removeLLMEvaluatorById_NotFound() {
    // Arrange
    when(llmEvaluatorRepository.findByIdAndUser(evaluatorId, testUser))
        .thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> newLLMEvaluatorService.removeLLMEvaluatorById(testUser, evaluatorId));
    assertEquals("Evaluator not found for user", exception.getMessage());
    verify(evaluationStatusRepository, never()).existsByEvaluatorId(anyString());
    verify(llmEvaluatorRepository, never()).save(any(LLMEvaluator.class));
    verify(llmEvaluatorRepository, never()).delete(any(LLMEvaluator.class));
  }
}

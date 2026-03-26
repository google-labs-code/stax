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
import static org.mockito.Mockito.*;

import com.planck.planck.domain.evaluationstatus.EvaluationStatusRepository;
import com.planck.planck.domain.evaluator.dto.PointwiseHeuristicEvaluatorUpdateDTO;
import com.planck.planck.domain.evaluator.heuristic.PointwiseHeuristicEvaluatorRepository;
import com.planck.planck.domain.evaluator.heuristic.dto.PointwiseHeuristicEvaluatorRequestDTO;
import com.planck.planck.domain.evaluator.heuristic.dto.PointwiseHeuristicEvaluatorResponseDTO;
import com.planck.planck.domain.evaluator.heuristic.service.PointwiseHeuristicEvaluatorServiceImpl;
import com.planck.planck.entitities.PointwiseHeuristicEvaluator;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.CriteriaType;
import com.planck.planck.exceptions.EvaluatorNameExistException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.util.CustomScorerStringConstant;
import jakarta.persistence.EntityNotFoundException;
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
class PointwiseHeuristicEvaluatorServiceTest {

  @Mock private PointwiseHeuristicEvaluatorRepository pointwiseHeuristicEvaluatorRepository;
  @Mock private EvaluationStatusRepository evaluationStatusRepository;
  @InjectMocks private PointwiseHeuristicEvaluatorServiceImpl pointwiseHeuristicEvaluatorService;

  private User testUser;
  private PointwiseHeuristicEvaluatorRequestDTO testRequestDTO;
  private PointwiseHeuristicEvaluator testEvaluator;
  private final String evaluatorId = "heuristic-eval-123";
  private final String evaluatorName = "Test Heuristic Evaluator";

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("user-xyz");
    testUser.setEmail("test@example.com");

    testRequestDTO = new PointwiseHeuristicEvaluatorRequestDTO();
    testRequestDTO.setName(evaluatorName);
    testRequestDTO.setCriteria("Test Criteria");
    testRequestDTO.setCriteriaType(CriteriaType.CONTAINS);

    testEvaluator = new PointwiseHeuristicEvaluator();
    testEvaluator.setId(evaluatorId);
    testEvaluator.setName(evaluatorName);
    testEvaluator.setCriteria("Test Criteria");
    testEvaluator.setCriteriaType(CriteriaType.CONTAINS);
    testEvaluator.setUser(testUser);
  }

  @Test
  void createEvaluator_Success() {
    when(pointwiseHeuristicEvaluatorRepository.checkIfPointwiseHeuristicEvaluatorNameExists(
            evaluatorName, testUser))
        .thenReturn(false);
    when(pointwiseHeuristicEvaluatorRepository.save(any(PointwiseHeuristicEvaluator.class)))
        .thenReturn(testEvaluator);

    PointwiseHeuristicEvaluatorResponseDTO responseDTO =
        pointwiseHeuristicEvaluatorService.createEvaluator(testRequestDTO, testUser);

    assertNotNull(responseDTO);
    assertEquals(evaluatorId, responseDTO.getId());
    assertEquals(evaluatorName, responseDTO.getName());
    verify(pointwiseHeuristicEvaluatorRepository)
        .checkIfPointwiseHeuristicEvaluatorNameExists(evaluatorName, testUser);
    verify(pointwiseHeuristicEvaluatorRepository).save(any(PointwiseHeuristicEvaluator.class));
  }

  @Test
  void createEvaluator_NameExists_ThrowsException() {
    when(pointwiseHeuristicEvaluatorRepository.checkIfPointwiseHeuristicEvaluatorNameExists(
            evaluatorName, testUser))
        .thenReturn(true);

    EvaluatorNameExistException exception =
        assertThrows(
            EvaluatorNameExistException.class,
            () -> pointwiseHeuristicEvaluatorService.createEvaluator(testRequestDTO, testUser));
    assertEquals(CustomScorerStringConstant.SCRORE_NAME_ALREADY_EXIST, exception.getMessage());
    verify(pointwiseHeuristicEvaluatorRepository)
        .checkIfPointwiseHeuristicEvaluatorNameExists(evaluatorName, testUser);
    verify(pointwiseHeuristicEvaluatorRepository, never())
        .save(any(PointwiseHeuristicEvaluator.class));
  }

  @Test
  void getEvaluatorsByUser_ReturnsNonDeprecatedList() {
    PointwiseHeuristicEvaluator evaluator2 = new PointwiseHeuristicEvaluator();
    evaluator2.setId("heuristic-eval-456");
    List<PointwiseHeuristicEvaluator> evaluators = List.of(testEvaluator, evaluator2);
    when(pointwiseHeuristicEvaluatorRepository.findAllNonDeprecatedByUser(testUser))
        .thenReturn(evaluators);

    List<PointwiseHeuristicEvaluatorResponseDTO> result =
        pointwiseHeuristicEvaluatorService.getEvaluatorsByUser(testUser);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(evaluatorId, result.get(0).getId());
    verify(pointwiseHeuristicEvaluatorRepository).findAllNonDeprecatedByUser(testUser);
  }

  @Test
  void getEvaluatorsByUser_ReturnsEmptyList() {
    when(pointwiseHeuristicEvaluatorRepository.findAllNonDeprecatedByUser(testUser))
        .thenReturn(Collections.emptyList());

    List<PointwiseHeuristicEvaluatorResponseDTO> result =
        pointwiseHeuristicEvaluatorService.getEvaluatorsByUser(testUser);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(pointwiseHeuristicEvaluatorRepository).findAllNonDeprecatedByUser(testUser);
  }

  @Test
  void getEvaluatorById_Success() {
    when(pointwiseHeuristicEvaluatorRepository.findById(evaluatorId))
        .thenReturn(Optional.of(testEvaluator));

    PointwiseHeuristicEvaluatorResponseDTO result =
        pointwiseHeuristicEvaluatorService.getEvaluatorById(evaluatorId, testUser);

    assertNotNull(result);
    assertEquals(evaluatorId, result.getId());
    assertEquals(evaluatorName, result.getName());
    verify(pointwiseHeuristicEvaluatorRepository).findById(evaluatorId);
  }

  @Test
  void getEvaluatorById_NotFound_ThrowsException() {
    when(pointwiseHeuristicEvaluatorRepository.findById(evaluatorId)).thenReturn(Optional.empty());

    EntityNotFoundException exception =
        assertThrows(
            EntityNotFoundException.class,
            () -> pointwiseHeuristicEvaluatorService.getEvaluatorById(evaluatorId, testUser));
    assertEquals("Evaluator not found with id: " + evaluatorId, exception.getMessage());
    verify(pointwiseHeuristicEvaluatorRepository).findById(evaluatorId);
  }

  @Test
  void updateEvaluator_Success_UpdatesName() {
    PointwiseHeuristicEvaluatorUpdateDTO updateDTO = new PointwiseHeuristicEvaluatorUpdateDTO();
    String updatedName = "Updated Evaluator Name";
    updateDTO.setName(updatedName);

    when(pointwiseHeuristicEvaluatorRepository.findById(evaluatorId))
        .thenReturn(Optional.of(testEvaluator));
    when(pointwiseHeuristicEvaluatorRepository.save(any(PointwiseHeuristicEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PointwiseHeuristicEvaluatorResponseDTO responseDTO =
        pointwiseHeuristicEvaluatorService.updateEvaluator(evaluatorId, updateDTO, testUser);

    assertNotNull(responseDTO);
    assertEquals(updatedName, responseDTO.getName());
    verify(pointwiseHeuristicEvaluatorRepository).findById(evaluatorId);
    verify(pointwiseHeuristicEvaluatorRepository).save(any(PointwiseHeuristicEvaluator.class));
  }

  @Test
  void updateEvaluator_Success_DoesNotUpdateNameWhenISNull() {
    PointwiseHeuristicEvaluatorUpdateDTO updateDTO = new PointwiseHeuristicEvaluatorUpdateDTO();

    when(pointwiseHeuristicEvaluatorRepository.findById(evaluatorId))
        .thenReturn(Optional.of(testEvaluator));
    when(pointwiseHeuristicEvaluatorRepository.save(any(PointwiseHeuristicEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PointwiseHeuristicEvaluatorResponseDTO responseDTO =
        pointwiseHeuristicEvaluatorService.updateEvaluator(evaluatorId, updateDTO, testUser);

    assertNotNull(responseDTO);
    assertEquals(evaluatorName, responseDTO.getName());
    verify(pointwiseHeuristicEvaluatorRepository).findById(evaluatorId);
    verify(pointwiseHeuristicEvaluatorRepository).save(any(PointwiseHeuristicEvaluator.class));
  }

  @Test
  void updateEvaluator_NotFound_ThrowsException() {
    PointwiseHeuristicEvaluatorUpdateDTO updateDTO = new PointwiseHeuristicEvaluatorUpdateDTO();
    when(pointwiseHeuristicEvaluatorRepository.findById(evaluatorId)).thenReturn(Optional.empty());

    EntityNotFoundException exception =
        assertThrows(
            EntityNotFoundException.class,
            () ->
                pointwiseHeuristicEvaluatorService.updateEvaluator(
                    evaluatorId, updateDTO, testUser));
    assertEquals("Evaluator not found with id: " + evaluatorId, exception.getMessage());
    verify(pointwiseHeuristicEvaluatorRepository).findById(evaluatorId);
    verify(pointwiseHeuristicEvaluatorRepository, never())
        .save(any(PointwiseHeuristicEvaluator.class));
  }

  @Test
  void removeById_UsedInEvaluation_SetsDeprecatedFlag() {
    when(pointwiseHeuristicEvaluatorRepository.findByIdAndUser(evaluatorId, testUser))
        .thenReturn(Optional.of(testEvaluator));
    when(evaluationStatusRepository.existsByEvaluatorId(evaluatorId)).thenReturn(true);
    when(pointwiseHeuristicEvaluatorRepository.save(any(PointwiseHeuristicEvaluator.class)))
        .thenReturn(testEvaluator);

    pointwiseHeuristicEvaluatorService.removePointwiseHeuristicEvaluatorById(evaluatorId, testUser);

    assertTrue(testEvaluator.isDeprecated());
    verify(pointwiseHeuristicEvaluatorRepository).findByIdAndUser(evaluatorId, testUser);
    verify(evaluationStatusRepository).existsByEvaluatorId(evaluatorId);
    verify(pointwiseHeuristicEvaluatorRepository).save(testEvaluator);
    verify(pointwiseHeuristicEvaluatorRepository, never())
        .delete(any(PointwiseHeuristicEvaluator.class));
  }

  @Test
  void removeById_NotUsedInEvaluation_DeletesEvaluator() {
    when(pointwiseHeuristicEvaluatorRepository.findByIdAndUser(evaluatorId, testUser))
        .thenReturn(Optional.of(testEvaluator));
    when(evaluationStatusRepository.existsByEvaluatorId(evaluatorId)).thenReturn(false);

    pointwiseHeuristicEvaluatorService.removePointwiseHeuristicEvaluatorById(evaluatorId, testUser);

    verify(pointwiseHeuristicEvaluatorRepository).findByIdAndUser(evaluatorId, testUser);
    verify(evaluationStatusRepository).existsByEvaluatorId(evaluatorId);
    verify(pointwiseHeuristicEvaluatorRepository).delete(testEvaluator);
    verify(pointwiseHeuristicEvaluatorRepository, never())
        .save(any(PointwiseHeuristicEvaluator.class));
  }

  @Test
  void removeById_NotFound_ThrowsException() {
    when(pointwiseHeuristicEvaluatorRepository.findByIdAndUser(evaluatorId, testUser))
        .thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () ->
                pointwiseHeuristicEvaluatorService.removePointwiseHeuristicEvaluatorById(
                    evaluatorId, testUser));
    assertEquals("Evaluator not found for user", exception.getMessage());
    verify(pointwiseHeuristicEvaluatorRepository).findByIdAndUser(evaluatorId, testUser);
    verifyNoInteractions(evaluationStatusRepository);
    verify(pointwiseHeuristicEvaluatorRepository, never())
        .save(any(PointwiseHeuristicEvaluator.class));
    verify(pointwiseHeuristicEvaluatorRepository, never())
        .delete(any(PointwiseHeuristicEvaluator.class));
  }
}

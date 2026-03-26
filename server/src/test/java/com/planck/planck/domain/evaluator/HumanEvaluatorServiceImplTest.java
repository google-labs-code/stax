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
import static org.mockito.Mockito.*;

import com.planck.planck.domain.evaluation.HumanEvalScoreRepository;
import com.planck.planck.domain.evaluation.ScoreV2Repository;
import com.planck.planck.domain.evaluator.human.HumanEvaluatorRepository;
import com.planck.planck.domain.evaluator.human.dto.HumanCategoryOption;
import com.planck.planck.domain.evaluator.human.dto.HumanRangeOption;
import com.planck.planck.domain.evaluator.human.dto.ScoreV2DTO;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorServiceImpl;
import com.planck.planck.entitities.*;
import com.planck.planck.enums.LinkedEntityType;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoringMechanismType;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HumanEvaluatorServiceImplTest {

  @Mock private HumanEvaluatorRepository humanEvaluatorRepository;

  @Mock private HumanEvalScoreRepository humanEvalScoreRepository;

  @Mock private ScoreV2Repository scoreV2Repository;

  @InjectMocks private HumanEvaluatorServiceImpl humanEvaluatorService;

  private User user;
  private HumanEvaluator mockEvaluator;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId("user-123");
    user.setFirstName("Test");
    user.setLastName("User");

    mockEvaluator = new HumanEvaluator();
    mockEvaluator.setId("evaluator-123");
    mockEvaluator.setName("Test Evaluator");
    mockEvaluator.setUser(user);
    mockEvaluator.setScoringMechanismType(ScoringMechanismType.CATEGORY);
  }

  @Test
  void testCreateEvaluator_Category_Success() {
    String name = "Category Evaluator";
    String description = "Evaluator with category scoring";
    ScoringMechanismType scoringMechanismType = ScoringMechanismType.CATEGORY;
    List<HumanCategoryOption> categories = new ArrayList<>();
    HumanCategoryOption categoryOption = new HumanCategoryOption("category-1", 1.0, "Positive");
    categories.add(categoryOption);

    when(humanEvaluatorRepository.save(any(HumanEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvaluator result =
        humanEvaluatorService.createEvaluator(
            user,
            name,
            description,
            scoringMechanismType,
            null,
            null,
            categories,
            LinkedEntityType.MODEL_RESPONSE);

    assertNotNull(result);
    assertEquals(name, result.getName());
    assertEquals(description, result.getDescription());
    verify(humanEvaluatorRepository, times(1)).save(any(HumanEvaluator.class));
  }

  @Test
  void testCreateEvaluator_Failure_NullScoringMechanismType() {
    String name = "Faulty Evaluator";
    String description = "Evaluator with null scoring mechanism type";
    List<HumanCategoryOption> categories = new ArrayList<>();
    LinkedEntityType linkedEntityType = LinkedEntityType.MODEL_RESPONSE;

    IllegalInputException exception =
        assertThrows(
            IllegalInputException.class,
            () ->
                humanEvaluatorService.createEvaluator(
                    user, name, description, null, null, null, categories, linkedEntityType));

    assertEquals("Scoring mechanism type is required", exception.getMessage());
  }

  @Test
  void testCreateEvaluator_Category_Failure_MissingCategories() {
    IllegalInputException exception =
        assertThrows(
            IllegalInputException.class,
            () ->
                humanEvaluatorService.createEvaluator(
                    user,
                    "Faulty Evaluator",
                    "No Categories",
                    ScoringMechanismType.CATEGORY,
                    null,
                    null,
                    null,
                    LinkedEntityType.MODEL_RESPONSE));
    assertEquals("Categories are required for CATEGORY type evaluator", exception.getMessage());
  }

  @Test
  void testCreateUserThumbsEvaluator_Success() {
    when(humanEvaluatorRepository.findByUserAndLinkedEntityType(
            user, LinkedEntityType.MODEL_RESPONSE))
        .thenReturn(new ArrayList<>()); // No existing evaluators
    when(humanEvaluatorRepository.save(any(HumanEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvaluator result = humanEvaluatorService.createUserThumbsEvaluator(user);

    assertNotNull(result);
    assertEquals("Thumbs Up/Down", result.getName());
    verify(humanEvaluatorRepository, times(1)).save(any(HumanEvaluator.class));
  }

  @Test
  void testCreateUserThumbsEvaluator_ExistingEvaluatorReturned() {
    HumanEvaluator existingEvaluator = new HumanEvaluator();
    existingEvaluator.setId("existing-evaluator-123");
    existingEvaluator.setName("Thumbs Up/Down");
    existingEvaluator.setLinkedEntityType(LinkedEntityType.MODEL_RESPONSE);
    existingEvaluator.setScoringMechanismType(ScoringMechanismType.CATEGORY);

    when(humanEvaluatorRepository.findByUserAndLinkedEntityType(
            user, LinkedEntityType.MODEL_RESPONSE))
        .thenReturn(List.of(existingEvaluator));

    HumanEvaluator result = humanEvaluatorService.createUserThumbsEvaluator(user);

    assertNotNull(result);
    assertEquals("existing-evaluator-123", result.getId());
    assertEquals("Thumbs Up/Down", result.getName());
    verify(humanEvaluatorRepository, times(1))
        .findByUserAndLinkedEntityType(user, LinkedEntityType.MODEL_RESPONSE);
    verify(humanEvaluatorRepository, never()).save(any(HumanEvaluator.class));
  }

  @Test
  void testCreateChatTurnFeedback_ExistingScore() {
    String modelResponseId = "model-response-123";
    String evaluatorId = "evaluator-123";
    Double newScore = 4.5;
    String newNotes = "Updated feedback notes";

    ModelResponse modelResponse = new ModelResponse();
    modelResponse.setId(modelResponseId);

    HumanEvaluator evaluator = new HumanEvaluator();
    evaluator.setId(evaluatorId);

    HumanEvalScore existingScore = new HumanEvalScore();
    existingScore.setId("score-123");
    existingScore.setScore(3.0, evaluator);
    existingScore.setNotes("Original notes");
    existingScore.setUser(user);
    existingScore.setModelResponse(modelResponse);

    when(humanEvalScoreRepository.findByModelResponseIdAndEvaluatorId(
            modelResponse.getId(), evaluator.getId(), user))
        .thenReturn(Optional.of(existingScore));
    when(humanEvalScoreRepository.save(any(HumanEvalScore.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvalScore result =
        humanEvaluatorService.createChatTurnFeedback(
            modelResponse, evaluator, user, newScore, newNotes);

    assertNotNull(result);
    assertEquals(newScore, result.getScore());
    assertEquals(newNotes, result.getNotes());
    assertEquals(user, result.getUser());
    assertEquals(modelResponse, result.getModelResponse());
    verify(humanEvalScoreRepository, times(1)).save(result);
  }

  @Test
  void testCreateChatTurnFeedback_NewScore() {
    String modelResponseId = "model-response-123";
    String evaluatorId = "evaluator-123";
    Double newScore = 5.0;
    String newNotes = "New feedback for the model response";

    ModelResponse modelResponse = new ModelResponse();
    modelResponse.setId(modelResponseId);

    HumanEvaluator evaluator = new HumanEvaluator();
    evaluator.setId(evaluatorId);

    when(humanEvalScoreRepository.findByModelResponseIdAndEvaluatorId(
            modelResponse.getId(), evaluator.getId(), user))
        .thenReturn(Optional.empty());
    when(humanEvalScoreRepository.save(any(HumanEvalScore.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvalScore result =
        humanEvaluatorService.createChatTurnFeedback(
            modelResponse, evaluator, user, newScore, newNotes);

    assertNotNull(result);
    assertEquals(newScore, result.getScore());
    assertEquals(newNotes, result.getNotes());
    assertEquals(user, result.getUser());
    assertEquals(modelResponse, result.getModelResponse());
    verify(humanEvalScoreRepository, times(1)).save(result);
  }

  @Test
  void testCreateChatTurnFeedback_NullScore() {
    String modelResponseId = "model-response-123";
    String evaluatorId = "evaluator-123";
    String newNotes = "Feedback with no score";

    ModelResponse modelResponse = new ModelResponse();
    modelResponse.setId(modelResponseId);

    HumanEvaluator evaluator = new HumanEvaluator();
    evaluator.setId(evaluatorId);

    when(humanEvalScoreRepository.findByModelResponseIdAndEvaluatorId(
            modelResponse.getId(), evaluator.getId(), user))
        .thenReturn(Optional.empty());
    when(humanEvalScoreRepository.save(any(HumanEvalScore.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvalScore result =
        humanEvaluatorService.createChatTurnFeedback(
            modelResponse, evaluator, user, null, newNotes);

    assertNotNull(result);
    assertNull(result.getScore());
    assertEquals(newNotes, result.getNotes());
    assertEquals(user, result.getUser());
    assertEquals(modelResponse, result.getModelResponse());
    verify(humanEvalScoreRepository, times(1)).save(result);
  }

  @Test
  void testDeleteHumanEvalScoreById_Success() {
    String scoreId = "score-123";
    HumanEvalScore humanEvalScore = new HumanEvalScore();
    humanEvalScore.setUser(user);

    when(humanEvalScoreRepository.findById(scoreId)).thenReturn(Optional.of(humanEvalScore));

    humanEvaluatorService.deleteHumanEvalScoreById(scoreId, user);

    verify(humanEvalScoreRepository, times(1)).deleteById(scoreId);
  }

  @Test
  void testDeleteHumanEvalScoreById_UserMismatch() {
    String humanEvalScoreId = "score-123";

    User differentUser = new User();
    differentUser.setId("different-user-id");

    HumanEvalScore score = new HumanEvalScore();
    score.setId(humanEvalScoreId);
    score.setUser(differentUser);

    when(humanEvalScoreRepository.findById(humanEvalScoreId)).thenReturn(Optional.of(score));

    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> humanEvaluatorService.deleteHumanEvalScoreById(humanEvalScoreId, user));

    assertEquals("Human score does not belong to the user", exception.getMessage());
    verify(humanEvalScoreRepository, times(1)).findById(humanEvalScoreId);
    verify(humanEvalScoreRepository, never()).deleteById(humanEvalScoreId);
  }

  @Test
  void testDeleteHumanEvalScoreById_NotFound() {
    String scoreId = "non-existent";
    when(humanEvalScoreRepository.findById(scoreId)).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> humanEvaluatorService.deleteHumanEvalScoreById(scoreId, user));
    assertEquals("HumanEvalScore not found", exception.getMessage());
  }

  @Test
  void testGetEvaluators_AllParametersNull() {
    List<HumanEvaluator> mockEvaluators = List.of(mockEvaluator);

    when(humanEvaluatorRepository.findByUser(user)).thenReturn(mockEvaluators);

    List<HumanEvaluator> result = humanEvaluatorService.getEvaluators(user, null, null);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(mockEvaluator.getId(), result.get(0).getId());
    verify(humanEvaluatorRepository, times(1)).findByUser(user);
  }

  @Test
  void testGetEvaluators_FilterByScopeType() {
    List<HumanEvaluator> mockEvaluators = List.of(mockEvaluator, new HumanEvaluator());
    mockEvaluator.setScopeType(ScopeType.USER);
    mockEvaluators.get(1).setScopeType(ScopeType.SYSTEM);

    when(humanEvaluatorRepository.findByUser(user)).thenReturn(mockEvaluators);

    List<HumanEvaluator> result = humanEvaluatorService.getEvaluators(user, ScopeType.USER, null);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(ScopeType.USER, result.get(0).getScopeType());
    verify(humanEvaluatorRepository, times(1)).findByUser(user);
  }

  @Test
  void testGetEvaluators_FilterByScoringMechanismType() {
    List<HumanEvaluator> mockEvaluators = List.of(mockEvaluator, new HumanEvaluator());
    mockEvaluator.setScoringMechanismType(ScoringMechanismType.CATEGORY);
    mockEvaluators.get(1).setScoringMechanismType(ScoringMechanismType.RANGE);

    when(humanEvaluatorRepository.findByUser(user)).thenReturn(mockEvaluators);

    List<HumanEvaluator> result =
        humanEvaluatorService.getEvaluators(user, null, ScoringMechanismType.CATEGORY);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(ScoringMechanismType.CATEGORY, result.get(0).getScoringMechanismType());
    verify(humanEvaluatorRepository, times(1)).findByUser(user);
  }

  @Test
  void testGetEvaluators_FilterByScopeTypeAndScoringMechanismType() {
    List<HumanEvaluator> mockEvaluators = List.of(mockEvaluator, new HumanEvaluator());
    mockEvaluator.setScopeType(ScopeType.USER);
    mockEvaluator.setScoringMechanismType(ScoringMechanismType.CATEGORY);
    mockEvaluators.get(1).setScopeType(ScopeType.SYSTEM);
    mockEvaluators.get(1).setScoringMechanismType(ScoringMechanismType.RANGE);

    when(humanEvaluatorRepository.findByUser(user)).thenReturn(mockEvaluators);

    List<HumanEvaluator> result =
        humanEvaluatorService.getEvaluators(user, ScopeType.USER, ScoringMechanismType.CATEGORY);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(ScopeType.USER, result.get(0).getScopeType());
    assertEquals(ScoringMechanismType.CATEGORY, result.get(0).getScoringMechanismType());
    verify(humanEvaluatorRepository, times(1)).findByUser(user);
  }

  @Test
  void testGetChatTurnFeedbacks_Success() {
    String chatTurnId = "chat-turn-123";

    ScoreV2 mockScore = new ScoreV2();
    List<ScoreV2> mockScores = List.of(mockScore);

    when(scoreV2Repository.findByModelResponse_Id(chatTurnId)).thenReturn(mockScores);

    List<ScoreV2DTO> result = humanEvaluatorService.getChatTurnFeedbacks(chatTurnId);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertNull(result.get(0)); // Assuming the `convertToScoreDTO()` returns null in your code
    verify(scoreV2Repository, times(1)).findByModelResponse_Id(chatTurnId);
  }

  @Test
  void testGetChatTurnFeedbacks_EmptyResult() {
    String chatTurnId = "chat-turn-123";

    when(scoreV2Repository.findByModelResponse_Id(chatTurnId)).thenReturn(new ArrayList<>());

    List<ScoreV2DTO> result = humanEvaluatorService.getChatTurnFeedbacks(chatTurnId);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(scoreV2Repository, times(1)).findByModelResponse_Id(chatTurnId);
  }

  @Test
  void testGetEvaluatorsByEntityId() {
    String entityId = "entity-123";
    List<HumanEvaluator> evaluators = List.of(mockEvaluator);
    when(humanEvaluatorRepository.findByAssociatedEntityId(entityId)).thenReturn(evaluators);

    List<HumanEvaluator> result = humanEvaluatorService.getEvaluatorsByEntityId(entityId);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Test Evaluator", result.get(0).getName());
    verify(humanEvaluatorRepository, times(1)).findByAssociatedEntityId(entityId);
  }

  @Test
  void testUpdateEvaluator_Success() {
    String evaluatorId = "evaluator-123";
    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user))
        .thenReturn(Optional.of(mockEvaluator));
    when(humanEvaluatorRepository.save(any(HumanEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvaluator updatedEvaluator =
        humanEvaluatorService.updateEvaluator(
            evaluatorId, user, "Updated Name", null, null, null, null);

    assertNotNull(updatedEvaluator);
    assertEquals("Updated Name", updatedEvaluator.getName());
    verify(humanEvaluatorRepository, times(1)).save(any(HumanEvaluator.class));
  }

  @Test
  void testUpdateEvaluator_SystemScopeFailure() {
    String evaluatorId = "system-evaluator-123";

    HumanEvaluator systemEvaluator = new HumanEvaluator();
    systemEvaluator.setId(evaluatorId);
    systemEvaluator.setScopeType(ScopeType.SYSTEM);

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user))
        .thenReturn(Optional.of(systemEvaluator));

    IllegalInputException exception =
        assertThrows(
            IllegalInputException.class,
            () ->
                humanEvaluatorService.updateEvaluator(
                    evaluatorId, user, "New Name", "New Description", null, null, null));

    assertEquals("Cannot update system evaluator", exception.getMessage());
    verify(humanEvaluatorRepository, times(1)).findByIdAndUser(evaluatorId, user);
    verify(humanEvaluatorRepository, never()).save(any(HumanEvaluator.class));
  }

  @Test
  void testUpdateEvaluator_UpdateNameSuccess() {
    String evaluatorId = "user-evaluator-123";

    HumanEvaluator userEvaluator = new HumanEvaluator();
    userEvaluator.setId(evaluatorId);
    userEvaluator.setScopeType(ScopeType.USER);
    userEvaluator.setName("Old Name");

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user))
        .thenReturn(Optional.of(userEvaluator));
    when(humanEvaluatorRepository.save(any(HumanEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvaluator result =
        humanEvaluatorService.updateEvaluator(
            evaluatorId, user, "New Name", null, null, null, null);

    assertNotNull(result);
    assertEquals("New Name", result.getName());
    verify(humanEvaluatorRepository, times(1)).save(userEvaluator);
  }

  @Test
  void testUpdateEvaluator_UpdateDescriptionSuccess() {
    String evaluatorId = "user-evaluator-123";

    HumanEvaluator userEvaluator = new HumanEvaluator();
    userEvaluator.setId(evaluatorId);
    userEvaluator.setScopeType(ScopeType.USER);
    userEvaluator.setDescription("Old Description");

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user))
        .thenReturn(Optional.of(userEvaluator));
    when(humanEvaluatorRepository.save(any(HumanEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvaluator result =
        humanEvaluatorService.updateEvaluator(
            evaluatorId, user, null, "New Description", null, null, null);

    assertNotNull(result);
    assertEquals("New Description", result.getDescription());
    verify(humanEvaluatorRepository, times(1)).save(userEvaluator);
  }

  @Test
  void testUpdateEvaluator_UpdateScoringMechanismTypeSuccess() {
    String evaluatorId = "user-evaluator-123";

    HumanEvaluator userEvaluator = new HumanEvaluator();
    userEvaluator.setId(evaluatorId);
    userEvaluator.setScopeType(ScopeType.USER);
    userEvaluator.setScoringMechanismType(ScoringMechanismType.CATEGORY);

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user))
        .thenReturn(Optional.of(userEvaluator));
    when(humanEvaluatorRepository.save(any(HumanEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvaluator result =
        humanEvaluatorService.updateEvaluator(
            evaluatorId, user, null, null, null, ScoringMechanismType.RANGE, null);

    assertNotNull(result);
    assertEquals(ScoringMechanismType.RANGE, result.getScoringMechanismType());
    verify(humanEvaluatorRepository, times(1)).save(userEvaluator);
  }

  @Test
  void testUpdateEvaluator_UpdateCommentsSuccess() {
    String evaluatorId = "user-evaluator-123";

    HumanEvaluator userEvaluator = new HumanEvaluator();
    userEvaluator.setId(evaluatorId);
    userEvaluator.setScopeType(ScopeType.USER);
    userEvaluator.setComments("Old Comments");

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user))
        .thenReturn(Optional.of(userEvaluator));
    when(humanEvaluatorRepository.save(any(HumanEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvaluator result =
        humanEvaluatorService.updateEvaluator(
            evaluatorId, user, null, null, "New Comments", null, null);

    assertNotNull(result);
    assertEquals("New Comments", result.getComments());
    verify(humanEvaluatorRepository, times(1)).save(userEvaluator);
  }

  @Test
  void testUpdateEvaluator_UpdateLinkedEntityTypeSuccess() {
    String evaluatorId = "user-evaluator-123";

    HumanEvaluator userEvaluator = new HumanEvaluator();
    userEvaluator.setId(evaluatorId);
    userEvaluator.setScopeType(ScopeType.USER);
    userEvaluator.setLinkedEntityType(LinkedEntityType.EVALUATOR);

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user))
        .thenReturn(Optional.of(userEvaluator));
    when(humanEvaluatorRepository.save(any(HumanEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvaluator result =
        humanEvaluatorService.updateEvaluator(
            evaluatorId, user, null, null, null, null, LinkedEntityType.MODEL_RESPONSE);

    assertNotNull(result);
    assertEquals(LinkedEntityType.MODEL_RESPONSE, result.getLinkedEntityType());
    verify(humanEvaluatorRepository, times(1)).save(userEvaluator);
  }

  @Test
  void testGetEvaluatorsByEntityType_Success() {
    LinkedEntityType entityType = LinkedEntityType.MODEL_RESPONSE;
    List<HumanEvaluator> mockEvaluators = List.of(mockEvaluator);

    when(humanEvaluatorRepository.findByLinkedEntityType(entityType)).thenReturn(mockEvaluators);

    List<HumanEvaluator> result = humanEvaluatorService.getEvaluatorsByEntityType(entityType);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(mockEvaluator.getId(), result.get(0).getId());
    verify(humanEvaluatorRepository, times(1)).findByLinkedEntityType(entityType);
  }

  @Test
  void testGetEvaluatorsByEntityType_EmptyResult() {
    LinkedEntityType entityType = LinkedEntityType.MODEL_RESPONSE;

    when(humanEvaluatorRepository.findByLinkedEntityType(entityType)).thenReturn(new ArrayList<>());

    List<HumanEvaluator> result = humanEvaluatorService.getEvaluatorsByEntityType(entityType);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(humanEvaluatorRepository, times(1)).findByLinkedEntityType(entityType);
  }

  @Test
  void testGetUserEvaluators_Success() {
    List<HumanEvaluator> mockEvaluators = List.of(mockEvaluator);
    when(humanEvaluatorRepository.findByUser(user)).thenReturn(mockEvaluators);

    List<HumanEvaluator> result = humanEvaluatorService.getUserEvaluators(user);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(mockEvaluator.getId(), result.get(0).getId());
    verify(humanEvaluatorRepository, times(1)).findByUser(user);
  }

  @Test
  void testGetUserEvaluators_EmptyResult() {
    when(humanEvaluatorRepository.findByUser(user)).thenReturn(new ArrayList<>());

    List<HumanEvaluator> result = humanEvaluatorService.getUserEvaluators(user);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(humanEvaluatorRepository, times(1)).findByUser(user);
  }

  @Test
  void testDeprecateEvaluator_Success() {
    String evaluatorId = "evaluator-123";

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user))
        .thenReturn(Optional.of(mockEvaluator));
    when(humanEvaluatorRepository.save(any(HumanEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvaluator deprecatedEvaluator =
        humanEvaluatorService.deprecateEvaluator(evaluatorId, user);

    assertNotNull(deprecatedEvaluator);
    assertTrue(deprecatedEvaluator.isDeprecated());
    verify(humanEvaluatorRepository, times(1)).save(any(HumanEvaluator.class));
  }

  @Test
  void testDeprecateEvaluator_SystemScopeFailure() {
    String evaluatorId = "system-evaluator-123";

    HumanEvaluator systemEvaluator = new HumanEvaluator();
    systemEvaluator.setId(evaluatorId);
    systemEvaluator.setScopeType(ScopeType.SYSTEM);

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user))
        .thenReturn(Optional.of(systemEvaluator));

    IllegalInputException exception =
        assertThrows(
            IllegalInputException.class,
            () -> humanEvaluatorService.deprecateEvaluator(evaluatorId, user));

    assertEquals("System evaluators cannot be deprecated", exception.getMessage());
    verify(humanEvaluatorRepository, times(1)).findByIdAndUser(evaluatorId, user);
  }

  @Test
  void testCreateRangeEvaluator_Success() {
    HumanRangeOption rangeOption = new HumanRangeOption();
    when(humanEvaluatorRepository.save(any(HumanEvaluator.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    HumanEvaluator result =
        humanEvaluatorService.createRangeEvaluator(
            "Range Evaluator", "Description", rangeOption, user);

    assertNotNull(result);
    assertEquals("Range Evaluator", result.getName());
    verify(humanEvaluatorRepository, times(1)).save(any(HumanEvaluator.class));
  }

  @Test
  void testGetEvaluators_Success() {
    HumanEvaluator systemEvaluator = new HumanEvaluator();
    systemEvaluator.setId("system-evaluator-456");
    systemEvaluator.setScopeType(ScopeType.SYSTEM);

    List<HumanEvaluator> mockEvaluators = List.of(mockEvaluator, systemEvaluator);

    when(humanEvaluatorRepository.findByUserOrScopeType(user, ScopeType.SYSTEM))
        .thenReturn(mockEvaluators);

    List<HumanEvaluator> result = humanEvaluatorService.getEvaluators(user);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(mockEvaluator.getId(), result.get(0).getId());
    assertEquals(systemEvaluator.getId(), result.get(1).getId());
    verify(humanEvaluatorRepository, times(1)).findByUserOrScopeType(user, ScopeType.SYSTEM);
  }

  @Test
  void testGetEvaluator_Success() {
    String evaluatorId = "evaluator-123";

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user))
        .thenReturn(Optional.of(mockEvaluator));

    HumanEvaluator result = humanEvaluatorService.getEvaluator(evaluatorId, user);

    assertNotNull(result);
    assertEquals(mockEvaluator.getId(), result.getId());
    verify(humanEvaluatorRepository, times(1)).findByIdAndUser(evaluatorId, user);
  }

  @Test
  void testGetEvaluator_NotFound() {
    String evaluatorId = "unknown-id";

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user)).thenReturn(Optional.empty());

    IllegalInputException exception =
        assertThrows(
            IllegalInputException.class,
            () -> humanEvaluatorService.getEvaluator(evaluatorId, user));

    assertEquals("Evaluator not found", exception.getMessage());
    verify(humanEvaluatorRepository, times(1)).findByIdAndUser(evaluatorId, user);
  }

  @Test
  void testDeleteEvaluator_Success() {
    String evaluatorId = "evaluator-123";

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user))
        .thenReturn(Optional.of(mockEvaluator));
    mockEvaluator.setScopeType(ScopeType.USER);

    humanEvaluatorService.deleteEvaluator(evaluatorId, user);

    verify(humanEvaluatorRepository, times(1)).delete(mockEvaluator);
  }

  @Test
  void testDeleteEvaluator_SystemScopeFailure() {
    String evaluatorId = "system-evaluator-123";

    HumanEvaluator systemEvaluator = new HumanEvaluator();
    systemEvaluator.setId(evaluatorId);
    systemEvaluator.setScopeType(ScopeType.SYSTEM);

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user))
        .thenReturn(Optional.of(systemEvaluator));

    IllegalInputException exception =
        assertThrows(
            IllegalInputException.class,
            () -> humanEvaluatorService.deleteEvaluator(evaluatorId, user));

    assertEquals("Cannot delete system evaluator", exception.getMessage());
    verify(humanEvaluatorRepository, times(1)).findByIdAndUser(evaluatorId, user);
  }

  @Test
  void testDeleteEvaluator_NotFound() {
    String evaluatorId = "unknown-id";

    when(humanEvaluatorRepository.findByIdAndUser(evaluatorId, user)).thenReturn(Optional.empty());

    IllegalInputException exception =
        assertThrows(
            IllegalInputException.class,
            () -> humanEvaluatorService.deleteEvaluator(evaluatorId, user));

    assertEquals("Evaluator not found", exception.getMessage());
    verify(humanEvaluatorRepository, times(1)).findByIdAndUser(evaluatorId, user);
  }

  @Test
  void testDeleteAllByUser_Success() {
    humanEvaluatorService.deleteAllByUser(user);

    verify(humanEvaluatorRepository, times(1)).deleteAllByUser(user);
  }
}

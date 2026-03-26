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

package com.planck.planck.domain.evaluator.human.service;

import com.planck.planck.domain.evaluation.HumanEvalScoreRepository;
import com.planck.planck.domain.evaluation.ScoreV2Repository;
import com.planck.planck.domain.evaluator.human.HumanEvaluatorRepository;
import com.planck.planck.domain.evaluator.human.dto.HumanCategoryOption;
import com.planck.planck.domain.evaluator.human.dto.HumanRangeOption;
import com.planck.planck.domain.evaluator.human.dto.ScoreV2DTO;
import com.planck.planck.entitities.HumanEvalScore;
import com.planck.planck.entitities.HumanEvaluator;
import com.planck.planck.entitities.ModelResponse;
import com.planck.planck.entitities.ScoreV2;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.LinkedEntityType;
import com.planck.planck.enums.ScopeType;
import com.planck.planck.enums.ScoringMechanismType;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.util.ObjectMapperUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class HumanEvaluatorServiceImpl implements HumanEvaluatorService {

  @Autowired private HumanEvaluatorRepository humanEvaluatorRepository;

  @Autowired private ScoreV2Repository scoreV2Repository;

  @Autowired private HumanEvalScoreRepository humanEvalScoreRepository;

  @Transactional
  @Override
  public HumanEvaluator createEvaluator(
      User user,
      String name,
      String description,
      ScoringMechanismType scoringMechanismType,
      String associatedEntityId,
      String entityType,
      List<HumanCategoryOption> categories,
      LinkedEntityType linkedEntityType) {
    if (scoringMechanismType == null) {
      throw new IllegalInputException("Scoring mechanism type is required");
    }

    HumanEvaluator evaluator = new HumanEvaluator();
    evaluator.setName(name);
    evaluator.setDescription(description);
    evaluator.setScoringMechanismType(scoringMechanismType);
    evaluator.setUser(user);
    evaluator.setScopeType(ScopeType.USER);
    evaluator.setLinkedEntityType(linkedEntityType);

    if (scoringMechanismType == ScoringMechanismType.CATEGORY) {
      if (categories == null || categories.isEmpty()) {
        throw new IllegalInputException("Categories are required for CATEGORY type evaluator");
      }
      evaluator.setCategories(ObjectMapperUtil.convertObjectToJsonString(categories));
    } else if (scoringMechanismType == ScoringMechanismType.RANGE) {
      // TODO: Fix these field assignments once fields are properly defined
      // if (evaluatorDTO.getMinValue() == null || evaluatorDTO.getMaxValue() == null) {
      //   throw new IllegalInputException("Min and max values are required for RANGE type
      // evaluator");
      // }
      // if (evaluatorDTO.getMinValue() >= evaluatorDTO.getMaxValue()) {
      //   throw new IllegalInputException("Min value must be less than max value");
      // }
      // HumanRangeOption rangeOption = new HumanRangeOption();
      // rangeOption.setMinValue(evaluatorDTO.getMinValue());
      // rangeOption.setMaxValue(evaluatorDTO.getMaxValue());
      // rangeOption.setValueType(HumanEvalRangeDataType.DOUBLE);
      // evaluator.setRangeOptions(ObjectMapperUtil.convertObjectToJsonString(rangeOption));
    }

    return humanEvaluatorRepository.save(evaluator);
  }

  @Transactional
  @Override
  public HumanEvaluator createUserThumbsEvaluator(User user) {
    // Check if user already has a thumbs evaluator
    List<HumanEvaluator> existingEvaluators =
        humanEvaluatorRepository.findByUserAndLinkedEntityType(
            user, LinkedEntityType.MODEL_RESPONSE);
    if (!existingEvaluators.isEmpty()) {
      return existingEvaluators.get(0);
    }

    // Create new thumbs evaluator
    HumanEvaluator evaluator = new HumanEvaluator();
    evaluator.setName("Thumbs Up/Down");
    evaluator.setDescription("User feedback for chat turns");
    evaluator.setScoringMechanismType(ScoringMechanismType.CATEGORY);
    evaluator.setScopeType(ScopeType.USER);
    evaluator.setLinkedEntityType(LinkedEntityType.MODEL_RESPONSE);
    evaluator.setUser(user);

    // Create categories for thumbs up/down
    List<HumanCategoryOption> categories = new ArrayList<>();

    // Thumbs Up category
    HumanCategoryOption thumbsUp = new HumanCategoryOption();
    thumbsUp.setId("thumbs-up");
    thumbsUp.setCategoryName("Thumbs Up");
    thumbsUp.setDescription("Positive feedback");
    thumbsUp.setScore(1.0);
    categories.add(thumbsUp);

    // Neutral category
    HumanCategoryOption neutral = new HumanCategoryOption();
    neutral.setId("neutral");
    neutral.setCategoryName("Neutral");
    neutral.setDescription("Neutral feedback");
    neutral.setScore(0.0);
    categories.add(neutral);

    // Thumbs Down category
    HumanCategoryOption thumbsDown = new HumanCategoryOption();
    thumbsDown.setId("thumbs-down");
    thumbsDown.setCategoryName("Thumbs Down");
    thumbsDown.setDescription("Negative feedback");
    thumbsDown.setScore(-1.0);
    categories.add(thumbsDown);

    // Set categories in evaluator
    evaluator.setCategories(ObjectMapperUtil.toJsonString(categories));

    return humanEvaluatorRepository.save(evaluator);
  }

  @Transactional
  @Override
  public HumanEvalScore createChatTurnFeedback(
      ModelResponse modelResponse,
      HumanEvaluator evaluator,
      User user,
      Double score,
      String notes) {

    // Check if score already exists
    Optional<HumanEvalScore> existingScore =
        humanEvalScoreRepository.findByModelResponseIdAndEvaluatorId(
            modelResponse.getId(), evaluator.getId(), user);

    HumanEvalScore humanEvalScore;
    if (existingScore.isPresent()) {
      // Update existing score
      humanEvalScore = existingScore.get();
      humanEvalScore.setScore(score, evaluator);
      humanEvalScore.setNotes(notes);

    } else {
      // Create new score
      humanEvalScore = new HumanEvalScore();
      humanEvalScore.setScore(score, evaluator);
      humanEvalScore.setNotes(notes);
      humanEvalScore.setModelResponse(modelResponse);
      humanEvalScore.setUser(user);
    }

    return humanEvalScoreRepository.save(humanEvalScore);
  }

  @Transactional
  @Override
  public void deleteHumanEvalScoreById(String humanEvalScoreId, User user) {
    HumanEvalScore score =
        humanEvalScoreRepository
            .findById(humanEvalScoreId)
            .orElseThrow(() -> new NotFoundException("HumanEvalScore not found"));

    if (!score.getUser().getId().equals(user.getId())) {
      throw new NotFoundException("Human score does not belong to the user");
    }

    humanEvalScoreRepository.deleteById(humanEvalScoreId);
  }

  @Override
  public List<HumanEvaluator> getEvaluatorsByEntityId(String entityId) {
    return humanEvaluatorRepository.findByAssociatedEntityId(entityId);
  }

  @Override
  public List<HumanEvaluator> getEvaluatorsByEntityType(LinkedEntityType entityType) {
    return humanEvaluatorRepository.findByLinkedEntityType(entityType);
  }

  @Override
  public List<HumanEvaluator> getUserEvaluators(User user) {
    return humanEvaluatorRepository.findByUser(user);
  }

  @Override
  public List<HumanEvaluator> getEvaluators(
      User user, ScopeType scopeType, ScoringMechanismType scoringMechanismType) {
    List<HumanEvaluator> evaluators = humanEvaluatorRepository.findByUser(user);

    if (scopeType != null) {
      evaluators =
          evaluators.stream()
              .filter(e -> e.getScopeType() == scopeType)
              .collect(Collectors.toList());
    }

    if (scoringMechanismType != null) {
      evaluators =
          evaluators.stream()
              .filter(e -> e.getScoringMechanismType() == scoringMechanismType)
              .collect(Collectors.toList());
    }

    return evaluators;
  }

  @Override
  public List<ScoreV2DTO> getChatTurnFeedbacks(String chatTurnId) {
    return scoreV2Repository.findByModelResponse_Id(chatTurnId).stream()
        .map(this::convertToScoreDTO)
        .collect(Collectors.toList());
  }

  @Override
  public HumanEvaluator findByIdAndUser(String id, User user) {
    return humanEvaluatorRepository
        .findByIdAndUser(id, user)
        .orElseThrow(() -> new NotFoundException("Evaluator not found"));
  }

  private ScoreV2DTO convertToScoreDTO(ScoreV2 score) {
    return null;
  }

  @Transactional
  @Override
  public HumanEvaluator updateEvaluator(
      String id,
      User user,
      String name,
      String description,
      String comments,
      ScoringMechanismType scoringMechanismType,
      LinkedEntityType linkedEntityType) {
    HumanEvaluator evaluator = findByIdAndUser(id, user);

    // Prevent updates to system evaluators
    if (evaluator.getScopeType() == ScopeType.SYSTEM) {
      throw new IllegalInputException("Cannot update system evaluator");
    }

    if (name != null) {
      evaluator.setName(name);
    }
    if (description != null) {
      evaluator.setDescription(description);
    }

    if (scoringMechanismType != null) {
      evaluator.setScoringMechanismType(scoringMechanismType);
    }

    if (comments != null) {
      evaluator.setComments(comments);
    }

    if (linkedEntityType != null) {
      evaluator.setLinkedEntityType(linkedEntityType);
    }

    return humanEvaluatorRepository.save(evaluator);
  }

  @Transactional
  @Override
  public HumanEvaluator deprecateEvaluator(String evaluatorId, User user) {
    HumanEvaluator evaluator = findByIdAndUser(evaluatorId, user);

    // Prevent deprecation of system evaluators
    if (evaluator.getScopeType() == ScopeType.SYSTEM) {
      throw new IllegalInputException("System evaluators cannot be deprecated");
    }

    // Set as deprecated
    evaluator.setDeprecated(true);

    return humanEvaluatorRepository.save(evaluator);
  }

  @Transactional
  @Override
  public HumanEvaluator createRangeEvaluator(
      String name, String description, HumanRangeOption rangeOption, User user) {
    HumanEvaluator evaluator = new HumanEvaluator();
    evaluator.setName(name);
    evaluator.setDescription(description);
    evaluator.setScoringMechanismType(ScoringMechanismType.RANGE);
    evaluator.setScopeType(ScopeType.USER);
    evaluator.setUser(user);

    // Set range options
    evaluator.setRangeOptions(ObjectMapperUtil.toJsonString(rangeOption));

    return humanEvaluatorRepository.save(evaluator);
  }

  @Override
  public List<HumanEvaluator> getEvaluators(User user) {
    return humanEvaluatorRepository.findByUserOrScopeType(user, ScopeType.SYSTEM);
  }

  @Override
  public HumanEvaluator getEvaluator(String id, User user) {
    return humanEvaluatorRepository
        .findByIdAndUser(id, user)
        .orElseThrow(() -> new IllegalInputException("Evaluator not found"));
  }

  @Override
  public void deleteEvaluator(String id, User user) {
    HumanEvaluator evaluator =
        humanEvaluatorRepository
            .findByIdAndUser(id, user)
            .orElseThrow(() -> new IllegalInputException("Evaluator not found"));

    if (evaluator.getScopeType() == ScopeType.SYSTEM) {
      throw new IllegalInputException("Cannot delete system evaluator");
    }

    humanEvaluatorRepository.delete(evaluator);
  }

  @Transactional
  @Override
  public void deleteAllByUser(User user) {
    // humanEvalScoreRepository.deleteAllByUser(user);
    humanEvaluatorRepository.deleteAllByUser(user);
  }
}

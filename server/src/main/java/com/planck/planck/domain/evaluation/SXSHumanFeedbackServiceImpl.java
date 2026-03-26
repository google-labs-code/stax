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

import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.project.dto.SXSChatRowDTO;
import com.planck.planck.domain.project.dto.SXSHumanFeedbackRequestDTO;
import com.planck.planck.domain.project.dto.SXSRatingCount;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.SXSHumanFeedback;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SXSHumanFeedbackServiceImpl implements SXSHumanFeedbackService {

  @Autowired private SXSHumanFeedbackRepository sxsHumanFeedbackRepository;

  @Autowired private SxsEvaluationPairRepository sxsEvaluationPairRepository;

  @Autowired private ChatTurnRepository chatTurnRepository;

  @Override
  @Transactional
  public SXSHumanFeedback submitHumanFeedback(
      User user, String projectId, String pairId, SXSHumanFeedbackRequestDTO request) {
    SxsEvaluationPair pair = getSxsPairById(pairId, user);
    if (pair.getChatA() == null || pair.getChatB() == null) {
      throw new IllegalInputException(
          "Cannot submit human feedback because Side A or Side B is missing.");
    }

    validateHumanFeedbackRequest(request);

    ChatTurn chatTurnA;
    ChatTurn chatTurnB;

    if (request.getChatTurnAId() != null && request.getChatTurnBId() != null) {
      chatTurnA = chatTurnRepository.findByIdAndUser(request.getChatTurnAId(), user);
      chatTurnB = chatTurnRepository.findByIdAndUser(request.getChatTurnBId(), user);
    } else {
      chatTurnA = pair.getChatA().getLatestTurn();
      chatTurnB = pair.getChatB().getLatestTurn();
    }

    validateTurnsForHumanFeedback(chatTurnA, chatTurnB);

    if (request.getNotes() == null && request.getRating() == null) {
      sxsHumanFeedbackRepository.deleteByPairIdAndChatTurns(
          pair.getId(), chatTurnA.getId(), chatTurnB.getId());
      return null;
    }

    Optional<SXSHumanFeedback> existingFeedback =
        sxsHumanFeedbackRepository.findByPairIdAndChatTurns(
            pair.getId(), chatTurnA.getId(), chatTurnB.getId());

    SXSHumanFeedback humanFeedback = null;

    if (existingFeedback.isPresent()) {
      humanFeedback = existingFeedback.get();
      humanFeedback.setHumanSxsRating(request.getRating());
      humanFeedback.setHumanSxsNotes(request.getNotes());
    } else {
      humanFeedback =
          SXSHumanFeedback.builder()
              .pairId(pair.getId())
              .chatTurnA(chatTurnA.getId())
              .chatTurnB(chatTurnB.getId())
              .humanSxsRating(request.getRating())
              .humanSxsNotes(request.getNotes())
              .user(user)
              .projectId(pair.getContainer().getId())
              .build();
    }

    return sxsHumanFeedbackRepository.save(humanFeedback);
  }

  @Override
  @Transactional
  public void deleteHumanFeedback(User user, String projectId, String pairId) {
    SxsEvaluationPair pair = getSxsPairById(pairId, user);
    sxsHumanFeedbackRepository.deleteByPairId(pair.getId());
  }

  @Override
  @Transactional
  public void deleteHumanFeedbackForChatTurn(
      User user, String projectId, String pairId, String chatTurnA, String chatTurnB) {
    sxsHumanFeedbackRepository.deleteByChatTurnAAndChatTurnB(chatTurnA, chatTurnB);
  }

  @Override
  @Transactional(readOnly = true)
  public SXSHumanFeedback getHumanFeedback(User user, String pairId) {
    return sxsHumanFeedbackRepository.findByPairId(pairId).stream()
        .sorted(Comparator.comparing(SXSHumanFeedback::getCreatedAt).reversed())
        .findFirst()
        .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  public void setHumanFeedbackForSxsRows(List<SXSChatRowDTO> rows, User user) {
    if (rows == null || rows.isEmpty()) {
      return;
    }

    List<String> pairIds =
        rows.stream().map(SXSChatRowDTO::getId).collect(java.util.stream.Collectors.toList());

    List<SXSHumanFeedback> feedbacks = sxsHumanFeedbackRepository.findByPairIds(pairIds);

    Map<String, SXSHumanFeedback> feedbackMap =
        feedbacks.stream()
            .collect(
                java.util.stream.Collectors.toMap(this::createFeedbackKey, feedback -> feedback));

    for (SXSChatRowDTO row : rows) {
      String feedbackKey = createFeedbackKey(row);
      SXSHumanFeedback feedback = feedbackMap.get(feedbackKey);
      if (feedback != null) {
        row.setHumanSxsRating(feedback.getHumanSxsRating());
        row.setHumanSxsNotes(feedback.getHumanSxsNotes());
      }
    }
  }

  private String createFeedbackKey(SXSChatRowDTO row) {
    String chatTurnAId = row.getChatTurnA() != null ? row.getChatTurnA().getId() : null;
    String chatTurnBId = row.getChatTurnB() != null ? row.getChatTurnB().getId() : null;
    return createFeedbackKey(row.getId(), chatTurnAId, chatTurnBId);
  }

  private String createFeedbackKey(SXSHumanFeedback feedback) {
    return createFeedbackKey(
        feedback.getPairId(), feedback.getChatTurnA(), feedback.getChatTurnB());
  }

  private String createFeedbackKey(String pairId, String chatTurnA, String chatTurnB) {
    return String.format(
        "%s|%s|%s",
        pairId != null ? pairId : "null",
        chatTurnA != null ? chatTurnA : "null",
        chatTurnB != null ? chatTurnB : "null");
  }

  SxsEvaluationPair getSxsPairById(String pairId, User user) {
    return sxsEvaluationPairRepository
        .findByIdAndUser(pairId, user)
        .orElseThrow(() -> new NotFoundException("SxsEvaluationPair not found with id: " + pairId));
  }

  private void validateTurnsForHumanFeedback(ChatTurn chatTurnA, ChatTurn chatTurnB) {
    if (chatTurnA == null || chatTurnB == null) {
      throw new NotFoundException("One of the chat turns could not be retrieved");
    }

    if (chatTurnA.getModelResponse() == null
        || chatTurnB.getModelResponse() == null
        || !StringUtils.hasText(chatTurnA.getModelResponse().getText())
        || !StringUtils.hasText(chatTurnB.getModelResponse().getText())) {
      throw new IllegalInputException(
          "Cannot submit human feedback because Side A or Side B does not have a response");
    }
  }

  private void validateHumanFeedbackRequest(SXSHumanFeedbackRequestDTO request) {
    boolean hasChatTurnA =
        request.getChatTurnAId() != null && !request.getChatTurnAId().trim().isEmpty();
    boolean hasChatTurnB =
        request.getChatTurnBId() != null && !request.getChatTurnBId().trim().isEmpty();

    if (hasChatTurnA != hasChatTurnB) {
      throw new IllegalInputException(
          "Either both chatTurnA and chatTurnB must be provided, or neither should be provided.");
    }
  }

  @Override
  public List<SXSRatingCount> getHumanEvalMetricsByProject(String projectId, User user) {
    return sxsHumanFeedbackRepository.getHumanEvalMetricsByProject(projectId, user);
  }
}

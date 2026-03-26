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

package com.planck.planck.domain.importexport;

import com.planck.planck.domain.evaluation.SXSHumanFeedbackRepository;
import com.planck.planck.domain.evaluation.SxsEvaluationPairRepository;
import com.planck.planck.domain.importexport.dto.SXSHumanEvalRatingExportDTO;
import com.planck.planck.domain.importexport.dto.SxsEvaluationPairExportDTO;
import com.planck.planck.domain.importexport.dto.message.BaseMessageExportDTO;
import com.planck.planck.domain.project.ProjectService;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.SxsEvaluationPair;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.exceptions.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SxsExportServiceImpl implements SxsExportService {

  private final ProjectService projectService;
  private final SxsEvaluationPairRepository sxsEvaluationPairRepository;
  private final ChatExportService chatExportService;
  private final SXSHumanFeedbackRepository sxsHumanFeedbackRepository;

  public SxsExportServiceImpl(
      ProjectService projectService,
      SxsEvaluationPairRepository sxsEvaluationPairRepository,
      ChatExportService chatExportService,
      SXSHumanFeedbackRepository sxsHumanFeedbackRepository) {
    this.projectService = projectService;
    this.sxsEvaluationPairRepository = sxsEvaluationPairRepository;
    this.chatExportService = chatExportService;
    this.sxsHumanFeedbackRepository = sxsHumanFeedbackRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public SxsEvaluationPairExportDTO exportSxsPair(String pairId, User user, String projectId) {
    SxsEvaluationPair sxsPair =
        sxsEvaluationPairRepository
            .findByIdAndUser(pairId, user)
            .orElseThrow(
                () -> new NotFoundException("SxsEvaluationPair not found with id: " + pairId));
    if (!Objects.equals(sxsPair.getContainer().getId(), projectId)) {
      throw new NotFoundException("SxsEvaluationPair not found with id: " + pairId);
    }
    return toSxsEvaluationPairExportDTO(sxsPair, user);
  }

  @Transactional(readOnly = true)
  @Override
  public List<SxsEvaluationPairExportDTO> exportSxsPairsForProject(String projectId, User user) {
    Project project = projectService.getProjectForUser(user, projectId);

    if (project.getEvaluationType() != EvaluationType.SXS) {
      throw new IllegalArgumentException(
          "Bulk export of SxS pairs is only available for 'SXS' type projects.");
    }

    List<SxsEvaluationPair> pairs = sxsEvaluationPairRepository.findByContainer(project);

    return pairs.stream()
        .map(pair -> toSxsEvaluationPairExportDTO(pair, user))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<SxsEvaluationPairExportDTO> exportSxsPairsByIds(
      String projectId, List<String> pairIds, User user) {

    Project project = projectService.getProjectForUser(user, projectId);

    if (pairIds == null || pairIds.isEmpty()) {
      return Collections.emptyList();
    }

    List<SxsEvaluationPair> pairs =
        sxsEvaluationPairRepository.findAllByIdInAndContainer(pairIds, project);

    return pairs.stream()
        .map(pair -> toSxsEvaluationPairExportDTO(pair, user))
        .collect(Collectors.toList());
  }

  private SxsEvaluationPairExportDTO toSxsEvaluationPairExportDTO(
      SxsEvaluationPair sxsPair, User user) {
    List<BaseMessageExportDTO> chatAHistory =
        chatExportService.getChatHistory(sxsPair.getChatA(), user);
    List<BaseMessageExportDTO> chatBHistory =
        (sxsPair.getChatB() != null)
            ? chatExportService.getChatHistory(sxsPair.getChatB(), user)
            : Collections.emptyList();

    List<SXSHumanEvalRatingExportDTO> humanSxsRatings = null;
    if (!chatAHistory.isEmpty() && !chatBHistory.isEmpty()) {
      humanSxsRatings =
          sxsHumanFeedbackRepository.getHumanFeedbackExportDTOsByPairId(sxsPair.getId());
    }

    return SxsEvaluationPairExportDTO.builder()
        .variables(sxsPair.getVariables())
        .humanSxsRatings(humanSxsRatings)
        .expectedOutput(sxsPair.getExpectedOutput())
        .chatA(chatAHistory)
        .chatB(chatBHistory)
        .build();
  }
}

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

package com.planck.planck.domain.inference.outputs;

import com.planck.planck.domain.evaluation.SxsEvaluationPairRepository;
import com.planck.planck.domain.inference.dto.SXSGenerateOutputs;
import com.planck.planck.domain.inference.dto.SXSGenerateOutputsBase;
import com.planck.planck.domain.inference.dto.SXSGenerateOutputsResponse;
import com.planck.planck.domain.project.ProjectRepository;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.SXSGenerateOutputsMode;
import com.planck.planck.exceptions.NotFoundException;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SXSGenerateOutputsServiceImpl implements SXSGenerateOutputsService {

  private final ProjectRepository projectRepository;
  private final SxsEvaluationPairRepository sxsEvaluationPairRepository;
  private final GenerateOutputsFactory generateOutputsFactory;

  public SXSGenerateOutputsServiceImpl(
      ProjectRepository projectRepository,
      SxsEvaluationPairRepository sxsEvaluationPairRepository,
      GenerateOutputsFactory generateOutputsFactory) {
    this.projectRepository = projectRepository;
    this.sxsEvaluationPairRepository = sxsEvaluationPairRepository;
    this.generateOutputsFactory = generateOutputsFactory;
  }

  @Override
  public SXSGenerateOutputsResponse generateAllOutputs(
      String projectId, SXSGenerateOutputsBase request, User user) {
    Project project = getProject(projectId, user);
    List<String> sxsPairIds = sxsEvaluationPairRepository.findIdsByContainer(project);

    if (sxsPairIds.isEmpty()) {
      return SXSGenerateOutputsResponse.builder()
          .newSxsPairIds(Collections.emptyList())
          .failedSxsPairIds(Collections.emptyList())
          .skippedSxsPairIds(Collections.emptyList())
          .build();
    }
    return generateOutputs(
        project, sxsPairIds, request.getModelA(), request.getModelB(), request.getMode(), user);
  }

  @Override
  public SXSGenerateOutputsResponse generateOutputs(
      String projectId, SXSGenerateOutputs request, User user) {
    Project project = getProject(projectId, user);
    return generateOutputs(
        project,
        request.getSxsPairIds(),
        request.getModelA(),
        request.getModelB(),
        request.getMode(),
        user);
  }

  @SuppressWarnings("unchecked")
  private SXSGenerateOutputsResponse generateOutputs(
      Project project,
      List<String> sxsPairIds,
      String modelA,
      String modelB,
      SXSGenerateOutputsMode mode,
      User user) {
    GenerateOutputsStrategy<SXSGenerateOutputsResponse> generateOutputsStrategy =
        (GenerateOutputsStrategy<SXSGenerateOutputsResponse>)
            generateOutputsFactory.getStrategy(modelA, modelB, mode, project);
    return generateOutputsStrategy.generateOutputs(sxsPairIds);
  }

  private Project getProject(String projectId, User user) {
    Project project =
        projectRepository
            .findByUserAndId(user, projectId)
            .orElseThrow(() -> new NotFoundException("Project not found for this user"));

    if (project.getEvaluationType() != EvaluationType.SXS) {
      throw new NotFoundException("Project is not an SxS project");
    }

    return project;
  }
}

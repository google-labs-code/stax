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

package com.planck.planck.domain.project;

import com.planck.planck.domain.analytics.evaluation.dto.ProjectEvaluationAnalyticsByScorer;
import com.planck.planck.domain.analytics.inference.dto.ProjectInferenceMonitoringSummaryDTO;
import com.planck.planck.domain.project.dto.CreateProjectCommand;
import com.planck.planck.domain.project.dto.HumanEvalMetricsDTO;
import com.planck.planck.domain.project.dto.ListProjectsResponse;
import com.planck.planck.domain.project.dto.ProjectDTO;
import com.planck.planck.domain.project.dto.UpdateProjectCommand;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public interface ProjectService {
  ListProjectsResponse getAllProjectsSortedByIsDefaultAndUpdatedAt(
      User user, int pageSize, int pageNumber, EvaluationType type, String include);

  @Transactional
  Project createProject(CreateProjectCommand command, User user);

  ProjectDTO createProject(User user, CreateProjectCommand command) throws RuntimeException;

  ProjectDTO createDefaultProject(User user);

  // Throws exception if project is not found / doesn't belong to the user.
  ProjectDTO getProject(User user, String projectId, String include);

  Project getProjectForUser(User user, String projectId);

  ProjectDTO updateProjectIgnoreOutputOnlyFields(
      String projectId, UpdateProjectCommand command, User user);

  Project getProjectReference(String projectId);

  List<Project> findAllByUser(User user);

  ProjectInferenceMonitoringSummaryDTO getProjectInferenceMonitoringSummary(
      User user, String projectId);

  List<com.planck.planck.enums.ModelProvider> getDistinctModelProvidersByProject(
      User user, String projectId);

  HumanEvalMetricsDTO getHumanEvalMetrics(com.planck.planck.entitities.User user, String projectId);

  ProjectEvaluationAnalyticsByScorer getProjectEvaluationAnalyticsByScorer(
      User user, String projectId, String scorerId);

  List<ProjectEvaluationAnalyticsByScorer> getProjectEvaluationAnalyticsForAllScorers(
      User user, String projectId);
}

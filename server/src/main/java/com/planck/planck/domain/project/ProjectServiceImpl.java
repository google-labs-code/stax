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

import static com.planck.planck.util.PlanckConstants.DEFAULT_PROJECT_NAME_FORMAT;
import static com.planck.planck.util.PlanckConstants.MAX_PROJECTS_PER_USER;

import com.planck.planck.domain.analytics.evaluation.UserEvalMonitoringService;
import com.planck.planck.domain.analytics.evaluation.dto.ProjectEvaluationAnalyticsByScorer;
import com.planck.planck.domain.analytics.evaluation.dto.UserEvalChartEntryDTO;
import com.planck.planck.domain.analytics.evaluation.dto.UserEvalMonitoringParams;
import com.planck.planck.domain.analytics.inference.dto.ProjectInferenceMonitoringSummaryDTO;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.evaluation.HumanEvalScoreRepository;
import com.planck.planck.domain.evaluation.ScoreV2Repository;
import com.planck.planck.domain.evaluationmonitoring.EvaluationMonitoringService;
import com.planck.planck.domain.evaluator.llm.LLMEvaluatorRepository;
import com.planck.planck.domain.inferencemonitoring.InferenceMonitoringService;
import com.planck.planck.domain.job.JobStatusService;
import com.planck.planck.domain.job.dto.JobStatusDTO;
import com.planck.planck.domain.modelresponse.service.ModelResponseService;
import com.planck.planck.domain.project.dto.CreateProjectCommand;
import com.planck.planck.domain.project.dto.HumanEvalMetricsDTO;
import com.planck.planck.domain.project.dto.LatestEvalScoreDTO;
import com.planck.planck.domain.project.dto.ListProjectsResponse;
import com.planck.planck.domain.project.dto.ProjectDTO;
import com.planck.planck.domain.project.dto.UpdateProjectCommand;
import com.planck.planck.entitities.HumanEvalScore;
import com.planck.planck.entitities.LLMEvaluator;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.exceptions.ResourceLimitExceedException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

  private static final Set<String> ALLOWED_INCLUDE_FIELDS =
      Set.of(
          "modelProviders",
          "inferenceMetrics",
          "humanEvalMetrics",
          "latestEvalScore",
          "jobStatuses");

  private static final String ALL_FIELDS = "all";

  @Autowired private ProjectRepository projectRepository;
  @Autowired private JobStatusService jobStatusService;

  @PersistenceContext private EntityManager entityManager;
  @Autowired private ChatService chatService;
  @Autowired private EvaluationMonitoringService evaluationMonitoringService;
  @Autowired private InferenceMonitoringService inferenceMonitoringService;
  @Autowired private ModelResponseService modelResponseService;
  @Autowired private HumanEvalScoreRepository humanEvalScoreRepository;
  @Autowired private UserEvalMonitoringService userEvalMonitoringService;
  @Autowired private LLMEvaluatorRepository llmEvaluatorRepository;
  @Autowired private ScoreV2Repository scoreV2Repository;

  @Autowired private SampleProjectService sampleProjectService;

  @Override
  public ListProjectsResponse getAllProjectsSortedByIsDefaultAndUpdatedAt(
      User user, int pageSize, int pageNumber, EvaluationType type, String include) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize);

    List<Project> projects;

    if (type == null) {
      projects = projectRepository.findAllByUserOrderByIsDefaultDescUpdatedAtDesc(user, pageable);
    } else {
      projects =
          projectRepository.findAllByUserAndEvaluationTypeOrderByIsDefaultDescUpdatedAtDesc(
              user, type, pageable);
    }

    Set<String> normalizedFields = parseIncludeFields(include);
    ListProjectsResponse listProjectsResponse = new ListProjectsResponse();
    listProjectsResponse.setProjects(enrichProjectsWithFields(projects, normalizedFields));
    return listProjectsResponse;
  }

  @Transactional
  @Override
  public Project createProject(CreateProjectCommand command, User user) {
    long projectCount = projectRepository.countByUser(user);
    if (projectCount >= MAX_PROJECTS_PER_USER) {
      throw new ResourceLimitExceedException("User has reached the project limit.");
    }

    String projectName =
        (command.name() == null || command.name().isEmpty())
            ? String.format(DEFAULT_PROJECT_NAME_FORMAT, projectCount + 1)
            : command.name();

    Project newProject =
        Project.builder()
            .id(command.projectId())
            .user(user)
            .name(projectName)
            .description(command.description())
            .isDefault(command.isDefaultProject())
            .evaluationType(command.type())
            .build();

    Project savedProject = projectRepository.save(newProject);

    return projectRepository.save(newProject);
  }

  @Transactional
  @Override
  public ProjectDTO createProject(User user, CreateProjectCommand command) {

    Project savedProject = createProject(command, user);

    return ProjectDTO.of(savedProject);
  }

  @Transactional(readOnly = false)
  @Override
  public ProjectDTO createDefaultProject(User user) {
    try {
      return sampleProjectService.createSampleProjectForUser(user);
    } catch (Exception e) {
      log.error("Failed to create sample project for user: {}", user.getId(), e);
      return null;
      // Don't fail the signup process if sample project creation fails
    }
  }

  @Override
  public ProjectDTO getProject(User user, String projectId, String include) {
    Project project =
        projectRepository
            .findByUserAndId(user, projectId)
            .orElseThrow(() -> new NotFoundException(projectId + " can't be found"));

    Set<String> normalizedFields = parseIncludeFields(include);
    return enrichProjectWithFields(project, normalizedFields);
  }

  @Override
  public Project getProjectForUser(User user, String projectId) {
    return projectRepository
        .findByUserAndId(user, projectId)
        .orElseThrow(() -> new NotFoundException(projectId + " can't be found"));
  }

  @Override
  public ProjectInferenceMonitoringSummaryDTO getProjectInferenceMonitoringSummary(
      User user, String projectId) {
    return inferenceMonitoringService.getProjectInferenceMonitoringSummary(user, projectId);
  }

  @Override
  public HumanEvalMetricsDTO getHumanEvalMetrics(User user, String projectId) {
    Optional<Project> project = projectRepository.findByUserAndId(user, projectId);

    if (project.isEmpty()) {
      throw new NotFoundException(projectId + " can't be found");
    }

    return buildHumanEvalMetrics(project.get());
  }

  @Transactional
  @Override
  public ProjectDTO updateProjectIgnoreOutputOnlyFields(
      String projectId, UpdateProjectCommand command, User user) {
    Project projectToUpdate =
        projectRepository
            .findByUserAndId(user, projectId)
            .orElseThrow(() -> new NotFoundException("Project " + projectId + " not found"));

    if (command.name() != null) {
      projectToUpdate.setName(command.name());
    }
    if (command.description() != null) {
      projectToUpdate.setDescription(command.description());
    }

    Project savedProject = projectRepository.save(projectToUpdate);

    return ProjectDTO.of(savedProject);
  }

  @Override
  public Project getProjectReference(String projectId) {
    if (projectId == null) {
      return null;
    }
    return entityManager.getReference(Project.class, projectId);
  }

  private ProjectDTO enrichProjectWithFields(Project project, Set<String> includeFields) {
    List<Project> projectList = List.of(project);
    return enrichProjectsWithFields(projectList, includeFields).get(0);
  }

  private List<ProjectDTO> enrichProjectsWithFields(
      List<Project> projects, Set<String> includeFields) {
    List<String> projectIds = projects.stream().map(Project::getId).toList();
    User user = projects.get(0).getUser();

    final Map<String, List<JobStatusDTO>> jobStatusMap;
    if (shouldIncludeField(includeFields, "jobStatuses")) {
      jobStatusMap = jobStatusService.findAllJobStatusGroupedByProject(user, projectIds);
    } else {
      jobStatusMap = Map.of();
    }

    return projects.stream()
        .map(project -> enrichSingleProjectWithFields(project, jobStatusMap, includeFields))
        .toList();
  }

  private ProjectDTO enrichSingleProjectWithFields(
      Project project, Map<String, List<JobStatusDTO>> jobStatusMap, Set<String> includeFields) {
    ProjectDTO.ProjectDTOBuilder builder = ProjectDTO.from(project);
    if (shouldIncludeField(includeFields, "jobStatuses")) {
      List<JobStatusDTO> jobStatuses =
          jobStatusMap.getOrDefault(project.getId(), List.of()).stream()
              .sorted(
                  Comparator.comparing(
                      JobStatusDTO::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())))
              .toList();

      Integer total = jobStatuses.stream().mapToInt(JobStatusDTO::getTotal).sum();
      Integer finished =
          jobStatuses.stream().mapToInt(JobStatusDTO::getSuccessful).sum()
              + jobStatuses.stream().mapToInt(JobStatusDTO::getFailed).sum();

      builder.jobStatuses(jobStatuses).totalJobTasks(total).finishedJobTasks(finished);
    }

    if (shouldIncludeField(includeFields, "modelProviders")) {
      List<ModelProvider> providers =
          getDistinctModelProvidersByProject(project.getUser(), project.getId());
      builder.providers(providers);
    }

    if (shouldIncludeField(includeFields, "inferenceMetrics")) {
      ProjectInferenceMonitoringSummaryDTO inferenceMonitoringSummary =
          inferenceMonitoringService.getProjectInferenceMonitoringSummary(
              project.getUser(), project.getId());
      builder.inferenceMonitoringSummary(inferenceMonitoringSummary);
    }

    if (shouldIncludeField(includeFields, "humanEvalMetrics")) {
      HumanEvalMetricsDTO humanEvalMetrics = buildHumanEvalMetrics(project);
      builder.humanEvalMetrics(humanEvalMetrics);
    }

    if (shouldIncludeField(includeFields, "latestEvalScore")) {
      LatestEvalScoreDTO latestEvalScore = buildLatestEvalScore(project);
      builder.latestEvalScore(latestEvalScore);
    }

    return builder.build();
  }

  private HumanEvalMetricsDTO buildHumanEvalMetrics(Project project) {
    // Fetch all human eval scores for this project via chatTurn.project.id
    List<HumanEvalScore> scores = humanEvalScoreRepository.findAllByContainerId(project.getId());
    Map<Integer, Long> scoreCounts =
        scores.stream()
            .filter(s -> s.getScore() != null && s.getScore().intValue() != 0)
            .collect(Collectors.groupingBy(s -> s.getScore().intValue(), Collectors.counting()));
    long passCount =
        scores.stream().filter(s -> s.getScore() != null && s.getScore().intValue() == 1).count();
    long totalCount =
        scores.stream().filter(s -> s.getScore() != null && s.getScore().intValue() != 0).count();
    Double passRate = totalCount > 0 ? (double) passCount / totalCount : null;
    return HumanEvalMetricsDTO.builder().scoreCounts(scoreCounts).passRate(passRate).build();
  }

  private LatestEvalScoreDTO buildLatestEvalScore(Project project) {
    Optional<LLMEvaluator> latestRunEvaluator =
        llmEvaluatorRepository.findLatestRunEvaluatorByContainerId(project.getId());
    if (latestRunEvaluator.isEmpty()) {
      return null;
    }
    Double avgScore =
        scoreV2Repository.findAvgScoreByEvaluatorIdAndProjectId(
            latestRunEvaluator.get().getId(), project.getId());
    return LatestEvalScoreDTO.builder()
        .name(latestRunEvaluator.get().getName())
        .avgScore(avgScore)
        .build();
  }

  @Override
  public List<Project> findAllByUser(User user) {
    return projectRepository.findAllByUser(user);
  }

  @Override
  public List<ModelProvider> getDistinctModelProvidersByProject(User user, String projectId) {
    return chatService.getDistinctModelProvidersByProject(user, projectId);
  }

  @Override
  public ProjectEvaluationAnalyticsByScorer getProjectEvaluationAnalyticsByScorer(
      User user, String projectId, String scorerId) {
    Project project = getProjectForUser(user, projectId);

    UserEvalMonitoringParams params = new UserEvalMonitoringParams();
    params.setStartTime(Instant.ofEpochMilli(0)); // beginning of unix time
    params.setProjectId(project.getId());
    params.setScorerIds(List.of(scorerId));
    Map<String, UserEvalChartEntryDTO> analytics =
        userEvalMonitoringService.getAnalyticsMonitoringData(user, params);
    if (analytics.size() == 0) {
      return new ProjectEvaluationAnalyticsByScorer();
    }

    UserEvalChartEntryDTO entry = analytics.values().stream().findFirst().orElse(null);
    if (entry == null) {
      return new ProjectEvaluationAnalyticsByScorer();
    }
    return new ProjectEvaluationAnalyticsByScorer(
        entry.getDatapoints(), entry.getScorerId(), entry.getScorerName());
  }

  @Override
  public List<ProjectEvaluationAnalyticsByScorer> getProjectEvaluationAnalyticsForAllScorers(
      User user, String projectId) {
    Project project = getProjectForUser(user, projectId);

    UserEvalMonitoringParams params = new UserEvalMonitoringParams();
    params.setStartTime(Instant.ofEpochMilli(0)); // beginning of unix time
    params.setProjectId(project.getId());
    Map<String, UserEvalChartEntryDTO> analytics =
        userEvalMonitoringService.getAnalyticsMonitoringData(user, params);

    if (analytics.isEmpty()) {
      return List.of();
    }

    return analytics.entrySet().stream()
        .map(
            entry -> {
              UserEvalChartEntryDTO chartEntry = entry.getValue();

              return new ProjectEvaluationAnalyticsByScorer(
                  chartEntry.getDatapoints(),
                  entry.getValue().getScorerId(),
                  entry.getValue().getScorerName());
            })
        .toList();
  }

  private boolean shouldIncludeField(Set<String> includeFields, String fieldName) {
    return includeFields.contains(fieldName);
  }

  private Set<String> parseIncludeFields(String include) {
    if (include == null || include.trim().isEmpty()) {
      return Set.of();
    }

    List<String> fields =
        Arrays.stream(include.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();

    if (fields.isEmpty()) {
      return Set.of();
    }

    Set<String> normalizedFields = new HashSet<>();

    for (String field : fields) {
      if (ALL_FIELDS.equalsIgnoreCase(field)) {
        return new HashSet<>(ALLOWED_INCLUDE_FIELDS);
      }
      if (ALLOWED_INCLUDE_FIELDS.contains(field)) {
        normalizedFields.add(field);
      } else {
        log.warn("Unknown include field: {}", field);
      }
    }

    return normalizedFields;
  }
}

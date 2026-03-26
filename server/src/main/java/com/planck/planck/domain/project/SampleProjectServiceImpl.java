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

import com.planck.planck.domain.evaluator.heuristic.service.PointwiseHeuristicEvaluatorService;
import com.planck.planck.domain.evaluator.human.service.HumanEvaluatorService;
import com.planck.planck.domain.importexport.ChatImportService;
import com.planck.planck.domain.importexport.dto.ChatImportRequest;
import com.planck.planck.domain.importexport.dto.ImportResultDTO;
import com.planck.planck.domain.project.dto.ProjectDTO;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ChatTurnContainerType;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class SampleProjectServiceImpl implements SampleProjectService {

  @Autowired private ProjectRepository projectRepository;

  @Autowired private ChatImportService chatImportService;

  @Autowired private UserRepository userRepository; // For creating sample project for default user

  @Autowired
  private PointwiseHeuristicEvaluatorService
      heuristicEvaluatorService; // For creating sample project for default user

  @Autowired
  private HumanEvaluatorService
      humanEvaluatorService; // For creating sample project for default user

  @Value("${sample.project.name:Sample Project}")
  private String sampleProjectName;

  @Value(
      "${sample.project.description:A sample project to help you get started with travel recommendations}")
  private String sampleProjectDescription;

  @Value("${sample.csv.filepath:sample-data.csv}")
  private String sampleCsvFilePath;

  @Value("${sample.csv.columns.input:input}")
  private String inputColumnName;

  @Value("${sample.csv.columns.output:output}")
  private String outputColumnName;

  @Value("${sample.csv.columns.expected-output:expected_output}")
  private String expectedOutputColumnName;

  @Value("${sample.csv.columns.tags:tags}")
  private String tagsColumnName;

  @Value("${sample.csv.columns.system-instruction:system_instruction}")
  private String systemInstructionColumnName;

  @Value("${sample.csv.columns.model-label:model_nickname}")
  private String modelLabelColumnName;

  @Value("${sample.csv.columns.human-eval-score:human_evaluation}")
  private String humanEvalScoreColumnName;

  @Value("${sample.csv.columns.human-eval-notes:human_evaluation_notes}")
  private String humanEvalNotesColumnName;

  @Value("${sample.csv.columns.inference-analytics:inference_analytics}")
  private String inferenceAnalyticsColumnName;

  @Value("${sample.csv.columns.llm-evaluations:llm_evaluations}")
  private String llmEvaluationColumnName;

  @Value("${sample.csv.metadata.columns:metadata}")
  private String metadataColumns;

  @Value("${auth.enabled}")
  public Boolean authEnabled;

  @Value("${user.email}")
  public String defaultUser;

  @PostConstruct
  protected void initiateSampleProjectForDefaultAuth() {
    if (!authEnabled) {
      User user = userRepository.findByEmail(defaultUser).orElse(null);
      if (user == null) {
        return; // no default user found
      }

      var projectCount = projectRepository.countByUser(user);
      if (projectCount == 0) {
        var evaluators = humanEvaluatorService.getEvaluators(user);
        if (evaluators.size() == 0) { // no default evaluators created
          humanEvaluatorService.createUserThumbsEvaluator(user);
          heuristicEvaluatorService.createDefaultHeuristicEvaluators(user);
        }
        createSampleProjectForUser(user);
      }
    }
  }

  @Override
  @Transactional
  public ProjectDTO createSampleProjectForUser(User user) {
    log.info("Starting sample project creation for user: {}", user.getId());

    Project defaultProject =
        Project.builder()
            .user(user)
            .name(sampleProjectName)
            .description(sampleProjectDescription)
            .isDefault(true)
            .build();
    ProjectDTO project = ProjectDTO.of(projectRepository.saveAndFlush(defaultProject));
    log.info("Created sample project: {} for user: {}", project.getProjectId(), user.getId());

    try {
      ImportResultDTO importResult = importSampleData(user, project.getProjectId());
      log.info(
          "Imported sample data for project: {}, successful rows: {}, failed rows: {}",
          project.getProjectId(),
          importResult.getSuccessfulRows(),
          importResult.getFailedRows());
    } catch (Exception e) {
      log.error("Failed to import sample data for user: {}", user.getId(), e);
      // Don't throw the exception - the project was created successfully
    }

    log.info("Completed sample project creation for user: {}", user.getId());
    return project;
  }

  private ImportResultDTO importSampleData(User user, String projectId) throws IOException {
    // Load the sample CSV file from classpath
    Resource resource = new ClassPathResource(sampleCsvFilePath);
    if (!resource.exists()) {
      log.warn("Sample CSV file not found: {}", sampleCsvFilePath);
      return new ImportResultDTO(); // Return empty result
    }

    // Create a MultipartFile from the resource
    MultipartFile multipartFile = createMultipartFileFromResource(resource);

    // Parse metadata columns
    List<String> metadataColumnList = new ArrayList<>();
    if (metadataColumns != null && !metadataColumns.trim().isEmpty()) {
      metadataColumnList = Arrays.asList(metadataColumns.split(","));
    }

    // Create import request
    ChatImportRequest importRequest =
        new ChatImportRequest(
            multipartFile,
            user,
            ChatTurnContainerType.PROJECT,
            projectId,
            null, // chatColumnName - not used for simple import
            metadataColumnList,
            inputColumnName,
            outputColumnName,
            expectedOutputColumnName,
            tagsColumnName,
            systemInstructionColumnName,
            modelLabelColumnName,
            humanEvalScoreColumnName,
            humanEvalNotesColumnName,
            inferenceAnalyticsColumnName,
            llmEvaluationColumnName);

    return chatImportService.importChatsFromFile(importRequest);
  }

  private MultipartFile createMultipartFileFromResource(Resource resource) throws IOException {
    return new MultipartFile() {
      @Override
      public String getName() {
        return "sample-data.csv";
      }

      @Override
      public String getOriginalFilename() {
        return "sample-data.csv";
      }

      @Override
      public String getContentType() {
        return "text/csv";
      }

      @Override
      public boolean isEmpty() {
        return false;
      }

      @Override
      public long getSize() {
        try {
          return resource.contentLength();
        } catch (IOException e) {
          return 0;
        }
      }

      @Override
      public byte[] getBytes() throws IOException {
        return resource.getInputStream().readAllBytes();
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return resource.getInputStream();
      }

      @Override
      public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        // Not implemented for this use case
        throw new UnsupportedOperationException("transferTo not supported");
      }
    };
  }
}

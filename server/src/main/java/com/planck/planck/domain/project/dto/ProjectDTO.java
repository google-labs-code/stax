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

package com.planck.planck.domain.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.planck.planck.domain.analytics.inference.dto.ProjectInferenceMonitoringSummaryDTO;
import com.planck.planck.domain.job.dto.JobStatusDTO;
import com.planck.planck.entitities.Project;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.enums.ModelProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(Include.NON_NULL)
public class ProjectDTO implements Serializable {
  // Output only field - ignored if included in input
  @Schema(description = "Project ID", accessMode = Schema.AccessMode.READ_ONLY)
  String projectId;

  @Schema(
      description =
          "Project Name. Auto-generated if missing, with name lile 'Default <N>', where N is a sequential number",
      example = "My Project")
  String name;

  @Schema(
      description = "Project Type. Auto-generated if missing, with default value POINTWISE",
      example = "POINTWISE",
      allowableValues = {"POINTWISE", "SXS"})
  EvaluationType type = EvaluationType.POINTWISE;

  @Schema(
      description = "Project Description",
      example = "This is a description of my project",
      maxLength = 1500)
  @Length(max = 1500, message = "Project description can't exceed 1500 characters")
  String description;

  // Output only field - ignored if included in input
  @Schema(description = "Project creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
  Timestamp createdAt;

  // Output only field - ignored if included in input
  @Schema(description = "Project last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
  Timestamp updatedAt;

  // Output only field - ignored if included in input
  @Schema(
      description = "Indicates if the project is the default project",
      accessMode = Schema.AccessMode.READ_ONLY)
  Boolean isDefaultProject;

  @Schema(description = "List of job statuses for the project")
  List<JobStatusDTO> jobStatuses;

  @Schema(description = "Total job tasks in incomplete Jobs")
  Integer totalJobTasks;

  @Schema(description = "Finished job tasks in incomplete jobs")
  Integer finishedJobTasks;

  @Schema(description = "List of model providers used in the project")
  List<ModelProvider> providers;

  @Schema(description = "Aggregated inference monitoring summary for the project")
  ProjectInferenceMonitoringSummaryDTO inferenceMonitoringSummary;

  @Schema(description = "Aggregated human evaluation metrics for the project")
  HumanEvalMetricsDTO humanEvalMetrics;

  @Schema(description = "Aggregated latest evaluation score for the project")
  LatestEvalScoreDTO latestEvalScore;

  public static ProjectDTOBuilder from(Project project) {
    return ProjectDTO.builder()
        .projectId(project.getId())
        .name(project.getName())
        .type(project.getEvaluationType())
        .description(project.getDescription())
        .createdAt(project.getCreatedAt())
        .updatedAt(project.getUpdatedAt())
        .isDefaultProject(project.getIsDefault());
  }

  public static ProjectDTO of(Project project) {
    return from(project).build();
  }
}

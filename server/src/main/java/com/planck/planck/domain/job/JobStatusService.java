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

package com.planck.planck.domain.job;

import com.planck.planck.domain.job.dto.GridResponse;
import com.planck.planck.domain.job.dto.JobStatusDTO;
import com.planck.planck.domain.job.dto.JobStatusPage;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.JobStatusEnum;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface JobStatusService {
  JobStatus createJobStatus(
      String type,
      String subType,
      User user,
      String projectId,
      JobStatusEnum inputStatusEnum,
      Integer total);

  void updateJobStatus(
      Integer pending,
      Integer inprogress,
      Integer failed,
      Integer stopped,
      Integer successful,
      JobStatusEnum inputStatusEnum,
      String jobId);

  void stopJobStatus(String jobStatusId, Date endTime, String projectId);

  GridResponse findAllJobStatusByUser(User user, JobStatusPage promptJobStatusPage);

  Map<String, List<JobStatusDTO>> findAllJobStatusGroupedByProject(
      User user, List<String> projectIds);

  GridResponse findAllJobStatusByProject(
      User user, String projectId, JobStatusPage promptJobStatusPage);

  JobStatus createScorerJobStatus(
      String type,
      String subType,
      User user,
      JobStatusEnum inputStatusEnum,
      Integer total,
      Project project);

  JobStatus findById(String jobId);

  Map<String, Object> fetchDetailByJob(String jobStatusIdId, User user);

  void deleteJobStatusByJobId(String jobId, User user);

  void deleteByUser(User user);

  List<JobStatus> deleteByProject(User user, String projectId);

  Integer deleteByProjectIdAndUserId(String projectId, User user);

  Integer bulkDeleteJobStatusByJobId(Collection<JobStatus> jobStatusSet, User user);

  Integer bulkDeleteJobStatusByJobId(Set<String> jobsIdSet, User user);

  boolean hasRemainingJobsByProject(User user, String projectId);

  Map<JobStatusEnum, List<String>> getJobIdsGroupedByStatus(
      User user, String projectId, List<JobStatusEnum> statuses);

  void trackJobStatusCommon(String jobId, Function<String, Map<String, Integer>> statFetcher);

  void stopAllJobsByUser(User user);
}

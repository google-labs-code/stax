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

import com.planck.planck.entitities.BulkUploadJobStatus;
import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.User;
import java.util.List;
import java.util.Map;

public interface BulkUploadJobStatusService {
  Map<String, Integer> findAllByUserAndJobId(String jobId, String userId);

  List<BulkUploadJobStatus> findByUserIdAndJobStatusId(String jobStatusId, String userId);

  Map<String, Object> getBulkDetailByJob(String jobStatusId, User userId, JobStatus jobStatus);

  void deleteByUserIdAndJobStatusId(String jobStatusId, String userId);

  void deleteByUserId(String userId);
}

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

import com.planck.planck.annotation.RateLimited;
import com.planck.planck.domain.job.dto.APIResponse;
import com.planck.planck.domain.job.dto.DeleteJobRequest;
import com.planck.planck.domain.job.dto.GridResponse;
import com.planck.planck.domain.job.dto.JobStatusPage;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.JobStatusEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/job")
public class JobStatusController {

  @Autowired private JobStatusService jobStatusService;

  @Autowired private InputStatusService inputStatusService;

  @GetMapping("/{jobId}/status")
  @RateLimited(maxRequests = 10000, windowSizeInSeconds = 60)
  public ResponseEntity<Map<String, Integer>> getJobStatusByJobId(
      @PathVariable(name = "jobId", required = true) String jobId,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(inputStatusService.findAllByJobStatusIdAndUser(jobId, user.getId()));
  }

  /**
   * Job status Detail.
   *
   * @param jobId
   * @param user
   * @return
   */
  @GetMapping("/status/{jobStatusId}") // we need to
  // delete
  // /getJobDetailByJobStatusId/{jobId}
  @RateLimited(maxRequests = 10000, windowSizeInSeconds = 60)
  public ResponseEntity<Object> getInferenceDetailByJob(
      @PathVariable(name = "jobStatusId", required = true) String jobStatusId,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(jobStatusService.fetchDetailByJob(jobStatusId, user));
  }

  /**
   * FEtch data from master Job Status
   *
   * @param pageIndex
   * @param pageSize
   * @param user
   * @return
   */
  @GetMapping("/status")
  @RateLimited(maxRequests = 10000, windowSizeInSeconds = 60)
  public ResponseEntity<GridResponse> getJobsStatus(
      @RequestParam(name = "pageIndex", defaultValue = "1") int pageIndex,
      @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
      @AuthenticationPrincipal User user) {
    JobStatusPage promptJobStatusPage = new JobStatusPage();
    promptJobStatusPage.setPageIndex(pageIndex);
    promptJobStatusPage.setPageSize(pageSize);

    return ResponseEntity.ok(jobStatusService.findAllJobStatusByUser(user, promptJobStatusPage));
  }

  @DeleteMapping("/status/job/{jobId}")
  public ResponseEntity<APIResponse> deleteJobStatusByJobId(
      @PathVariable(name = "jobId", required = true) String jobId,
      @AuthenticationPrincipal User user) {
    jobStatusService.deleteJobStatusByJobId(jobId, user);
    return new ResponseEntity<>(
        new APIResponse(true, "Job is successfully deleted.", "job", null), HttpStatus.OK);
  }

  @DeleteMapping("/status/bulk")
  public ResponseEntity<APIResponse> deleteBulkJobsByUser(
      @AuthenticationPrincipal User user,
      @Validated @RequestBody DeleteJobRequest deleteJobRequest) {

    List<String> jobsId = deleteJobRequest.getJobsId();
    Set<String> jobsIdSet = new HashSet<>(jobsId);

    Integer count = jobStatusService.bulkDeleteJobStatusByJobId(jobsIdSet, user);
    return new ResponseEntity<>(
        new APIResponse(true, count + " Jobs is successfully deleted.", "jobs", null),
        HttpStatus.OK);
  }
}

@RestController
@RequestMapping(value = {"/job/projects/{projectId}"})
class ProjectJobStatusController {

  @Autowired private JobStatusService jobStatusService;

  @GetMapping()
  @PreAuthorize("hasPermission(#projectId, 'PROJECT', 'read')")
  @RateLimited(maxRequests = 10000, windowSizeInSeconds = 60)
  public ResponseEntity<GridResponse> getJobsStatusByProject(
      @PathVariable String projectId,
      @RequestParam(name = "pageIndex", defaultValue = "1") int pageIndex,
      @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
      @AuthenticationPrincipal User user) {
    JobStatusPage promptJobStatusPage = new JobStatusPage();
    promptJobStatusPage.setPageIndex(pageIndex);
    promptJobStatusPage.setPageSize(pageSize);

    return ResponseEntity.ok(
        jobStatusService.findAllJobStatusByProject(user, projectId, promptJobStatusPage));
  }

  @Operation(
      summary = "Check if the project has pending jobs",
      description =
          "Returns true if there are any jobs in PENDING status for the specified project and user.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Returns true if pending jobs exist, false otherwise",
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true)))
      })
  @GetMapping("/has-remaining")
  @PreAuthorize("hasPermission(#projectId, 'PROJECT', 'read')")
  public ResponseEntity<Boolean> hasPendingJobs(
      @PathVariable String projectId, @AuthenticationPrincipal User user) {

    boolean hasPending = jobStatusService.hasRemainingJobsByProject(user, projectId);
    return ResponseEntity.ok(hasPending);
  }

  @Operation(
      summary = "Get job IDs by status",
      description = "Returns a list of job IDs for a given project and statuses.",
      parameters = {
        @Parameter(
            name = "projectId",
            in = ParameterIn.PATH,
            required = true,
            description = "ID of the project"),
        @Parameter(
            name = "status",
            in = ParameterIn.QUERY,
            description =
                """
        (Optional) Filter jobs by one or more statuses.
        Allowed values: PENDING, IN_PROGRESS, SUCCESSFUL, FAILED.
        Accepts comma-separated values, e.g., status=PENDING,FAILED
        """,
            schema = @Schema(type = "array", implementation = JobStatusEnum.class))
      },
      responses = {
        @ApiResponse(responseCode = "200", description = "List of job IDs with the given status"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(hidden = true))),
      })
  @GetMapping("/ids-by-status")
  @PreAuthorize("hasPermission(#projectId, 'PROJECT', 'read')")
  public ResponseEntity<Map<JobStatusEnum, List<String>>> getJobIdsByStatus(
      @PathVariable String projectId,
      @RequestParam(name = "status", required = false) List<JobStatusEnum> statuses,
      @AuthenticationPrincipal User user) {

    Map<JobStatusEnum, List<String>> result =
        jobStatusService.getJobIdsGroupedByStatus(user, projectId, statuses);

    return ResponseEntity.ok(result);
  }

  @PostMapping("/{jobId}/stop")
  @Operation(
      summary = "Stop a job",
      description =
          "Stops a running job by marking its status as STOPPED and setting endTime to now.",
      parameters = {
        @Parameter(
            name = "projectId",
            description = "Project ID",
            required = true,
            in = ParameterIn.PATH),
        @Parameter(name = "jobId", description = "Job ID", required = true, in = ParameterIn.PATH)
      },
      responses = {
        @ApiResponse(responseCode = "200", description = "Job successfully stopped"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - insufficient permissions",
            content = @Content(schema = @Schema(hidden = true)))
      })
  @PreAuthorize("hasPermission(#projectId, 'PROJECT', 'write')")
  public ResponseEntity<APIResponse> stopJob(
      @PathVariable String jobId, @PathVariable String projectId) {
    jobStatusService.stopJobStatus(jobId, new Date(), projectId);
    return ResponseEntity.ok(new APIResponse(true, "Job successfully stopped.", "job", null));
  }
}

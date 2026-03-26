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

import com.planck.planck.entitities.JobStatus;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.JobStatusEnum;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("jobStatusRepository")
public interface JobStatusRepository extends JpaRepository<JobStatus, String> {

  @Transactional
  @Modifying
  @Query(
      "UPDATE JobStatus ins set ins.status = :#{#status.key}, ins.pending=:pending, ins.inProgress=:inProgress, ins.successful=:successful,"
          + " ins.failed=:failed, ins.stopped=:stopped WHERE  ins.id = :jobStatusId")
  void updateStatusById(
      @Param("status") JobStatusEnum status,
      @Param("pending") Integer pending,
      @Param("inProgress") Integer inProgress,
      @Param("successful") Integer successful,
      @Param("failed") Integer failed,
      @Param("stopped") Integer stopped,
      @Param("jobStatusId") String jobStatusId);

  @Transactional
  @Modifying
  @Query("UPDATE JobStatus ins set ins.endTime=:endTime WHERE ins.id = :jobStatusId")
  void updateEndTimeByJobStatusId(
      @Param("endTime") Date endTime, @Param("jobStatusId") String jobStatusId);

  @Query("SELECT ins FROM JobStatus ins WHERE  ins.user = :user")
  List<JobStatus> findResultsAllByUserAndPageable(@Param("user") User user, Pageable pageable);

  @Query("SELECT ins FROM JobStatus ins WHERE ins.user = :user AND ins.project.id = :projectId")
  List<JobStatus> findResultsAllByUserAndProjectIdAndPageable(
      @Param("user") User user, @Param("projectId") String projectId, Pageable pageable);

  @Query("SELECT js FROM JobStatus js WHERE js.user = :user AND js.project.id IN :projectIds")
  List<JobStatus> findAllByUserAndProjectIds(
      @Param("user") User user, @Param("projectIds") List<String> projectIds);

  @Query("SELECT count(ins.id) FROM JobStatus ins WHERE  ins.user = :user")
  Integer findCountAllByUser(@Param("user") User user);

  @Query(
      "SELECT count(ins.id) FROM JobStatus ins WHERE  ins.user = :user AND ins.project.id = :projectId")
  Integer findCountAllByUserAndProjectId(
      @Param("user") User user, @Param("projectId") String projectId);

  @Modifying
  void deleteByUser(User user);

  @Transactional
  @Modifying
  @Query("DELETE FROM JobStatus ins WHERE ins.user = :user AND ins.project.id = :projectId")
  List<JobStatus> deleteByUserAndProjectId(
      @Param("user") User user, @Param("projectId") String projectId);

  boolean existsByUserAndProjectIdAndStatusIn(
      User user, String projectId, Collection<Integer> statuses);

  @Query(
      "SELECT js.id FROM JobStatus js WHERE js.user = :user AND js.project.id = :projectId AND js.status = :status")
  List<String> findIdsByUserAndProjectIdAndStatus(
      @Param("user") User user, @Param("projectId") String projectId, @Param("status") int status);

  @Transactional
  @Modifying
  @Query("UPDATE JobStatus js SET js.status = 5 WHERE js.user = :user AND js.status IN (1, 2)")
  void stopAllOngoingJobsByUser(User user);
}

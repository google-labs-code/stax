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

import com.planck.planck.entitities.InputStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("inputStatusRepository")
public interface InputStatusRepository extends JpaRepository<InputStatus, String> {

  @Query("SELECT t FROM InputStatus t WHERE t.jobStatusId =:jobStatusId AND t.user.id =:userId ")
  public List<InputStatus> findAllByUserAndJobStatusId(
      @Param("jobStatusId") String jobStatusId, @Param("userId") String userId);
}

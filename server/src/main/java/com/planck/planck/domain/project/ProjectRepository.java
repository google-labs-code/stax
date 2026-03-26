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

import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
  List<Project> findAllByUserOrderByIsDefaultDescUpdatedAtDesc(User user, Pageable pageable);

  List<Project> findAllByUserAndEvaluationTypeOrderByIsDefaultDescUpdatedAtDesc(
      User user, EvaluationType type, Pageable pageable);

  List<Project> findAllByUser(User user);

  Optional<Project> findByUserAndId(User user, String projectId);

  Long deleteByUserAndId(User user, String projectId);

  long countByUser(User user);
}

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

package com.planck.planck.domain.dataset;

import com.planck.planck.entitities.DataSet;
import com.planck.planck.entitities.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("dataSetRepository")
public interface DataSetRepository extends JpaRepository<DataSet, String> {
  @Query(
      "SELECT d FROM DataSet d WHERE d.id = :id AND ((d.user = :user and d.type = 'USER') or (d.type = 'SYSTEM' or d.type = 'COMMUNITY'))")
  DataSet findById(@Param("id") String id, @Param("user") User user);

  @Query("SELECT d FROM DataSet d WHERE d.id = :id AND d.user = :user and d.type = 'USER'")
  DataSet findUserDataSetById(@Param("id") String id, @Param("user") User user);

  @Query("SELECT COUNT(d) FROM DataSet d WHERE d.user = :user and d.type = 'USER'")
  long countByUser(@Param("user") User user);

  @Query(
      "SELECT d FROM DataSet d WHERE (d.user = :user and d.type = 'USER') or d.type = 'SYSTEM' or d.type = 'COMMUNITY'")
  List<DataSet> findAllByUser(@Param("user") User user);

  Optional<DataSet> findByUserAndId(User user, String dataSetId);
}

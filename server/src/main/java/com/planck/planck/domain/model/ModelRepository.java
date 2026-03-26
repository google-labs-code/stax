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

package com.planck.planck.domain.model;

import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.ModelType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends JpaRepository<Model, String>, ModelRepositoryCustom {

  @Query("SELECT m FROM Model m WHERE m.type =:type AND m.isDeprecated = false")
  List<Model> findModelsByTypeAndNotDeprecated(@Param("type") ModelType type);

  @Query("SELECT m FROM Model m WHERE m.user =:user AND m.isDeprecated = false")
  List<Model> findModelsByUserAndNotDeprecated(@Param("user") User user);

  @Query("SELECT m FROM Model m WHERE m.user =:user AND m.id =:id")
  Model findModelByIdAndUser(@Param("id") String id, @Param("user") User user);

  @Query("SELECT m FROM Model m WHERE m.id =:id and m.type =:type AND m.isDeprecated = false")
  Model findModelByIdAndTypeAndNotDeprecated(@Param("id") String id, @Param("type") ModelType type);

  @Query(
      "SELECT m FROM Model m WHERE m.user =:user OR (m.type = 'SYSTEM' AND m.isDeprecated = false) "
          + "ORDER BY m.releaseDate DESC NULLS LAST, m.label ASC")
  List<Model> findNonDeprecatedUserAndSystemModels(@Param("user") User user);

  @Query(
      "SELECT  count(m) FROM Model m WHERE m.user =:user AND m.type =:type AND m.isDeprecated ="
          + " false")
  int countModelsByUserAndType(@Param("user") User user, @Param("type") ModelType type);
}

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

package com.planck.planck.domain.tags.repository;

import com.planck.planck.entitities.Tag;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.TagType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, String>, TagRepositoryCustom {

  @Query("SELECT t FROM Tag t WHERE t.tagId IN (:tagIds) AND t.user = :user")
  public Set<Tag> findAllTagsByTagIdsAndUser(
      @Param("tagIds") Set<String> tagIds, @Param("user") User user);

  @Query(
      "SELECT COUNT(*) FROM Tag t WHERE (:tagId IS NULL OR t.tagId != :tagId) AND t.tagName = :tagName AND t.user = :user AND t.type = :type")
  public Long countTagBy(
      @Param("tagId") String tagId,
      @Param("tagName") String tagName,
      @Param("type") TagType type,
      @Param("user") User user);

  @Query("SELECT t FROM Tag t WHERE t.user = :user order by t.createdAt desc")
  public List<Tag> findAllUserTags(@Param("user") User user);

  public Optional<Tag> findByTagNameAndUser(
      @Param("tagName") String tagName, @Param("user") User user);

  public Optional<Tag> findByTagIdAndUser(@Param("tagId") String tagId, @Param("user") User user);

  public Long deleteByUserAndTagId(User user, String tagId);

  @Query(
      "SELECT t FROM Tag t WHERE (t.tagId = :idOrName OR t.tagName= :idOrName) AND t.user.id = :userId")
  public Optional<Tag> findByIdOrNameAndUserId(
      @Param("idOrName") String idOrName, @Param("userId") String userId);

  @Query("SELECT COUNT(1) FROM Tag t WHERE t.user = :user and t.type='USER'")
  public int countUserTags(User user);
}

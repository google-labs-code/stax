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

package com.planck.planck.domain.tags;

import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.tags.dto.TagFindResponse;
import com.planck.planck.entitities.Tag;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.MonitoringType;
import com.planck.planck.enums.TagType;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagService {
  Optional<Tag> findByTagIdAndUser(String tagId, User user);

  TagDTO createUserTag(String name, String color, User user);

  TagDTO updateUserTag(String tagId, User user, String name, String color);

  List<Tag> findAllUserTags(User user);

  List<Tag> saveAll(List<Tag> tags);

  TagFindResponse findTags(
      User user, TagType type, String projectId, String modelId, MonitoringType monitoringType);

  Long deleteByTagNameOrTagIdAndUser(String tagNameOrId, User user);

  Set<Tag> findAllTagsByTagIdAndUser(Set<String> tagIds, User user);
}

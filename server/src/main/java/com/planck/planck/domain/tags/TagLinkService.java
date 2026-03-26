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
import com.planck.planck.domain.tags.dto.TagLinkDTO;
import com.planck.planck.entitities.Tag;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.TagLinkTargetType;
import java.util.List;
import java.util.Map;

public interface TagLinkService {
  List<TagLinkDTO> getTagLinks(User user, TagLinkTargetType targetType);

  List<TagLinkDTO> getTagLinksDtoByEntity(TagLinkTargetType entityType, String entityId, User user);

  List<TagLink> getTagLinksByEntity(TagLinkTargetType entityType, String entityId, User user);

  List<TagLink> getTagLinksByEntities(
      TagLinkTargetType entityType, List<String> entityIds, User user);

  Map<TagLinkTargetType, List<Object>> findHydratedEntitiesByTag(
      String tagName, User user, TagLinkTargetType targetType);

  TagLinkDTO createTagLink(TagLinkTargetType entityType, String entityId, String tagId, User user);

  List<TagLinkDTO> createTagLinks(
      User user, List<String> tagIds, TagLinkTargetType entityType, List<String> entityIds);

  List<TagLink> saveAll(List<TagLink> tags);

  void deleteTagLinkById(String id, User user);

  int removeTagLinks(
      User user, List<String> tagIds, List<String> entityIds, TagLinkTargetType entityType);

  Map<String, List<TagDTO>> getUserTagsByTargetTypeAndEntityList(
      List<String> entityIds, TagLinkTargetType entityType, User user);

  //
  void syncEvaluationMonitoringTags(String evaluationMonitoringId, User user);

  void syncInferenceMonitoringTags(String inferenceMonitoringId, User user);

  List<Tag> getUserTags(List<String> tagIds, User user);

  List<TagLinkDTO> createTagLinksForProject(User user, String projectId, List<String> tagIds);

  int removeTagLinksForProject(User user, String projectId, List<String> tagIds);

  void updateTagsForEntity(
      User user, List<String> newTagNames, TagLinkTargetType entityType, String entityId);
}

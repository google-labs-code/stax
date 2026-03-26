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

import com.planck.planck.config.ApplicationLimits;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.tags.dto.TagLinkDTO;
import com.planck.planck.domain.tags.repository.EntityFetcher;
import com.planck.planck.domain.tags.repository.EntityFetcherRegistry;
import com.planck.planck.domain.tags.repository.TagLinkRepository;
import com.planck.planck.domain.tags.repository.TagRepository;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.Tag;
import com.planck.planck.entitities.TagLink;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.TagLinkTargetType;
import com.planck.planck.enums.TagType;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.exceptions.ResourceAlreadyExistsException;
import com.planck.planck.exceptions.ResourceLimitExceedException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagLinkServiceImpl implements TagLinkService {

  private static final String TAG_LIMIT_EXCEEDED_MESSAGE =
      "Tag limit of %d would be exceeded for entities: %s";

  @Autowired private ApplicationLimits applicationLimits;

  @Autowired private TagRepository tagRepository;

  @Autowired private TagLinkRepository tagLinkRepository;

  @Autowired private EntityFetcherRegistry fetcherRegistry;

  @Autowired private ChatTurnRepository chatTurnRepository;

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<TagLinkDTO> getTagLinks(User user, TagLinkTargetType targetType) {
    if (targetType != null) {
      return tagLinkRepository.findByTargetType(targetType.toString(), user).stream()
          .map(TagLinkDTO::new)
          .toList();
    }

    return tagLinkRepository.findAllByUser(user).stream().map(TagLinkDTO::new).toList();
  }

  @Override
  public List<TagLinkDTO> getTagLinksDtoByEntity(
      TagLinkTargetType entityType, String entityId, User user) {
    List<TagLink> tagLink =
        tagLinkRepository.findByTargetIdAndTargetType(entityId, entityType.toString(), user);
    return tagLink.stream().map(TagLinkDTO::new).collect(Collectors.toList());
  }

  @Override
  public List<TagLink> getTagLinksByEntity(
      TagLinkTargetType entityType, String entityId, User user) {
    return tagLinkRepository.findByTargetIdAndTargetType(entityId, entityType.toString(), user);
  }

  @Override
  public List<TagLink> getTagLinksByEntities(
      TagLinkTargetType entityType, List<String> entityIds, User user) {
    return tagLinkRepository.findByTargetIdsAndTargetType(entityIds, entityType.toString(), user);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<TagLinkTargetType, List<Object>> findHydratedEntitiesByTag(
      String tagName, User user, TagLinkTargetType targetType) {

    Tag tag =
        tagRepository
            .findByTagIdAndUser(tagName, user)
            .orElseThrow(() -> new NotFoundException("User tag not found"));

    List<TagLink> links;
    if (targetType != null) {
      links = tagLinkRepository.findByTagAndTargetType(tag, targetType.toString(), user);
    } else {
      links = tagLinkRepository.findByTag(tag, user);
    }

    Map<TagLinkTargetType, List<String>> groupedIds =
        links.stream()
            .collect(
                Collectors.groupingBy(
                    TagLink::getTargetType,
                    Collectors.mapping(TagLink::getTargetId, Collectors.toList())));

    Map<TagLinkTargetType, List<Object>> result = new HashMap<>();

    for (Map.Entry<TagLinkTargetType, List<String>> entry : groupedIds.entrySet()) {
      TagLinkTargetType entityType = entry.getKey();
      List<String> ids = entry.getValue();
      EntityFetcher<?> fetcher = fetcherRegistry.getFetcher(entityType);
      if (fetcher != null) {
        List<?> dtos = fetcher.findAllByIds(ids, user);
        result.put(entityType, (List<Object>) dtos);
      }
    }

    return result;
  }

  @Transactional
  @Override
  public TagLinkDTO createTagLink(
      TagLinkTargetType entityType, String entityId, String tagId, User user) {
    Optional<Tag> tag = tagRepository.findByTagIdAndUser(tagId, user);

    validateCreateLinkInput(tag, entityId, entityType, user);

    TagLink tagLink = new TagLink();
    tagLink.setUser(user);
    tagLink.setTargetId(entityId);
    tagLink.setTargetType(entityType);
    tagLink.setTag(tag.get());
    tag.get().setUsedCount(tag.get().getUsedCount() + 1);
    tagRepository.save(tag.get());

    TagLinkDTO tagLinkDTO = new TagLinkDTO(tagLinkRepository.save(tagLink));

    cascadeCreateLinks(entityType, List.of(entityId), List.of(tag.get()), user);

    return tagLinkDTO;
  }

  @Transactional
  @Override
  public List<TagLinkDTO> createTagLinks(
      User user, List<String> tagIds, TagLinkTargetType entityType, List<String> entityIds) {
    List<Tag> filteredTags = getUserTags(tagIds, user);

    Map<String, Set<Tag>> newTagsPerEntity =
        validateCreateLinksInput(entityIds, entityType, user, filteredTags);

    if (newTagsPerEntity.isEmpty()) {
      return new ArrayList<>();
    }

    List<TagLink> tagLinks =
        newTagsPerEntity.entrySet().stream()
            .flatMap(
                entry ->
                    entry.getValue().stream()
                        .map(
                            tag -> {
                              TagLink tagLink = new TagLink();
                              tag.setUsedCount(tag.getUsedCount() + 1);
                              tagLink.setTag(tag);
                              tagLink.setTargetId(entry.getKey());
                              tagLink.setTargetType(entityType);
                              tagLink.setUser(user);
                              return tagLink;
                            }))
            .collect(Collectors.toList());

    List<Tag> finalTagList = tagLinks.stream().map(TagLink::getTag).toList();
    tagRepository.saveAll(finalTagList);

    List<TagLinkDTO> result =
        tagLinkRepository.saveAll(tagLinks).stream()
            .map(TagLinkDTO::new)
            .collect(Collectors.toList());

    cascadeCreateLinks(entityType, entityIds, filteredTags, user);

    return result;
  }

  @Override
  public List<TagLink> saveAll(List<TagLink> tags) {
    return tagLinkRepository.saveAllAndFlush(tags);
  }

  @Transactional
  @Override
  public void deleteTagLinkById(String id, User user) {
    TagLink tagLink =
        tagLinkRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Tag link not found"));
    tagLink.getTag().setUsedCount(tagLink.getTag().getUsedCount() - 1);
    Set<TagLink> toDelete = new HashSet<>();
    toDelete.add(tagLink);

    cascadeDeleteLinks(
        tagLink.getTargetType(),
        List.of(tagLink.getTargetId()),
        List.of(tagLink.getTag().getTagId()),
        user,
        toDelete);

    updateTagUsedCountOnDelete(toDelete);
    tagLinkRepository.deleteAll(toDelete);
  }

  @Transactional
  @Override
  public int removeTagLinks(
      User user, List<String> tagIds, List<String> entityIds, TagLinkTargetType entityType) {
    if (entityIds != null && entityType == null) {
      throw new IllegalInputException("Target type is required when using target_ids");
    }

    Set<TagLink> toDelete = new HashSet<>();
    String entityTypeStr = entityType != null ? entityType.toString() : null;
    toDelete.addAll(tagLinkRepository.findByTargetOrTag(entityIds, entityTypeStr, tagIds, user));

    cascadeDeleteLinks(entityType, entityIds, tagIds, user, toDelete);

    updateTagUsedCountOnDelete(toDelete);
    if (!toDelete.isEmpty()) {
      tagLinkRepository.deleteByIdsAndUser(toDelete.stream().map(TagLink::getId).toList(), user);
    }
    return toDelete.size();
  }

  private void updateTagUsedCountOnDelete(Set<TagLink> toDelete) {
    Map<Tag, Long> tagToCount =
        toDelete.stream().collect(Collectors.groupingBy(TagLink::getTag, Collectors.counting()));

    for (Map.Entry<Tag, Long> entry : tagToCount.entrySet()) {
      Tag tag = entry.getKey();
      int count = entry.getValue().intValue();
      tag.setUsedCount(Math.max(0, tag.getUsedCount() - count)); // clamp to 0
    }

    tagRepository.saveAll(tagToCount.keySet());
  }

  /**
   * Retrieves a mapping from entity IDs to their associated TagDTOs for a given user and entity
   * type.
   *
   * @param entityIds list of entity IDs for which to retrieve tags
   * @param entityType the type of the entity
   * @param user the user whose tags are being fetched
   * @return a map where each key is an entity ID and the value is a list of associated TagDTOs
   */
  @Override
  public Map<String, List<TagDTO>> getUserTagsByTargetTypeAndEntityList(
      List<String> entityIds, TagLinkTargetType entityType, User user) {

    // Validate inputs
    if (entityIds == null || entityIds.isEmpty()) {
      return Collections.emptyMap();
    }
    if (entityType == null || user == null) {
      throw new IllegalArgumentException("Entity type and user cannot be null");
    }

    // Fetch raw tag link data (targetId, tagId)
    List<Object[]> rawTagLinks =
        tagLinkRepository.findTargetIdAndTagIdByTargetIdsAndTypeAndUser(
            entityIds, entityType.toString(), user);

    if (rawTagLinks.isEmpty()) {
      return Collections.emptyMap();
    }

    // Organize tag IDs by target ID
    Map<String, List<String>> tagsByTargetId = new HashMap<>();
    Set<String> allTagIds = new HashSet<>();

    for (Object[] row : rawTagLinks) {
      String targetId = (String) row[0];
      String tagId = (String) row[1];

      allTagIds.add(tagId);
      tagsByTargetId.computeIfAbsent(targetId, k -> new ArrayList<>()).add(tagId);
    }

    // Load all TagDTOs and map them by ID
    Map<String, TagDTO> tagById =
        tagRepository.findAllById(allTagIds).stream()
            .map(TagDTO::new)
            .collect(Collectors.toMap(TagDTO::getId, Function.identity()));

    // Build final result
    Map<String, List<TagDTO>> result = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : tagsByTargetId.entrySet()) {
      List<TagDTO> linkedTags =
          entry.getValue().stream()
              .map(tagById::get)
              .filter(Objects::nonNull)
              .collect(Collectors.toList());

      result.put(entry.getKey(), linkedTags);
    }

    return result;
  }

  // @Transactional
  @Override
  public void syncEvaluationMonitoringTags(String evaluationMonitoringId, User user) {
    List<Tag> missingTags =
        tagLinkRepository.findTagsForEvaluationMonitoring(evaluationMonitoringId, user);

    List<TagLink> newLinks =
        missingTags.stream()
            .map(
                tag -> {
                  TagLink link = new TagLink();
                  link.setTag(tag);
                  link.setTargetType(TagLinkTargetType.EVALUATION_MONITORING);
                  link.setTargetId(evaluationMonitoringId);
                  link.setUser(user);
                  return link;
                })
            .toList();

    if (!newLinks.isEmpty()) tagLinkRepository.saveAll(newLinks);
  }

  @Transactional
  @Override
  public void syncInferenceMonitoringTags(String inferenceMonitoringId, User user) {
    List<Tag> missingTags =
        tagLinkRepository.findTagsForEvaluationMonitoring(inferenceMonitoringId, user);

    List<TagLink> newLinks =
        missingTags.stream()
            .map(
                tag -> {
                  TagLink link = new TagLink();
                  link.setTag(tag);
                  link.setTargetType(TagLinkTargetType.INFERENCE_MONITORING);
                  link.setTargetId(inferenceMonitoringId);
                  link.setUser(user);
                  return link;
                })
            .toList();

    if (!newLinks.isEmpty()) tagLinkRepository.saveAll(newLinks);
  }

  @Override
  public List<Tag> getUserTags(List<String> tagIds, User user) {
    Set<String> tagIdSet = new HashSet<>(tagIds);
    Set<Tag> tags = tagRepository.findAllTagsByTagIdsAndUser(tagIdSet, user);

    return tags.stream().filter(tag -> tag.getType() == TagType.USER).collect(Collectors.toList());
  }

  private Map<String, Set<Tag>> validateCreateLinksInput(
      List<String> entityIds, TagLinkTargetType entityType, User user, List<Tag> filteredTags) {
    if (filteredTags.isEmpty()) {
      throw new IllegalInputException("No user tags found");
    }

    Map<String, Set<Tag>> newTagsPerEntity = new HashMap<>();

    List<Object[]> existingLinks =
        tagLinkRepository.findTargetIdAndTagIdByTargetIdsAndTypeAndUser(
            entityIds, entityType.toString(), user);

    Map<String, Set<String>> existingTagMap =
        existingLinks.stream()
            .collect(
                Collectors.groupingBy(
                    obj -> (String) obj[0], // targetId is at index 0
                    Collectors.mapping(
                        obj -> (String) obj[1], Collectors.toSet()) // tagId is at index 1
                    ));

    Map<String, Integer> exceedingEntities = new HashMap<>();

    for (String entityId : entityIds) {
      Set<String> existingTags = existingTagMap.getOrDefault(entityId, Set.of());

      Set<Tag> newTags =
          filteredTags.stream()
              .filter(tag -> !existingTags.contains(tag.getTagId()))
              .collect(Collectors.toSet());

      long newUniqueTags =
          filteredTags.stream().filter(tag -> !existingTags.contains(tag.getTagId())).count();

      int totalAfterInsert = existingTags.size() + (int) newUniqueTags;

      if (totalAfterInsert > applicationLimits.getMaxTagsPerEntity()) {
        exceedingEntities.put(entityId, totalAfterInsert);
      }

      if (!newTags.isEmpty()) {
        newTagsPerEntity.put(entityId, newTags);
      }
    }

    if (!exceedingEntities.isEmpty()) {
      throw new ResourceLimitExceedException(
          String.format(
              TAG_LIMIT_EXCEEDED_MESSAGE,
              applicationLimits.getMaxTagsPerEntity(),
              exceedingEntities));
    }

    return newTagsPerEntity;
  }

  private void validateCreateLinkInput(
      Optional<Tag> tag, String entityId, TagLinkTargetType entityType, User user) {
    if (!tag.isPresent()) throw new NotFoundException("User tag not found");

    List<TagLink> existingLinks =
        tagLinkRepository.findByTargetIdAndTargetType(entityId, entityType.toString(), user);

    Optional<TagLink> existingLink =
        existingLinks.stream()
            .filter(link -> link.getTag().getTagId() == tag.get().getTagId())
            .findFirst();
    if (existingLink.isPresent()) {
      throw new ResourceAlreadyExistsException("Tag link already exists");
    }

    if (existingLinks.size() + 1 > applicationLimits.getMaxTagsPerEntity()) {
      throw new IllegalInputException(
          "Operation would result in exceeding the tags per entity limit of "
              + applicationLimits.getMaxTagsPerEntity());
    }
  }

  @Override
  public List<TagLinkDTO> createTagLinksForProject(
      User user, String projectId, List<String> tagIds) {
    List<String> chatTurnIds = chatTurnRepository.findChatTurnIdsByContainerId(projectId, user);
    return createTagLinks(user, tagIds, TagLinkTargetType.CHAT_TURN, chatTurnIds);
  }

  @Override
  @Transactional
  public int removeTagLinksForProject(User user, String projectId, List<String> tagIds) {
    List<String> chatTurnIds = chatTurnRepository.findChatTurnIdsByContainerId(projectId, user);
    if (tagIds != null && tagIds.isEmpty()) tagIds = null;

    return removeTagLinks(user, tagIds, chatTurnIds, TagLinkTargetType.CHAT_TURN);
  }

  private void cascadeCreateLinks(
      TagLinkTargetType parentType, List<String> parentIds, List<Tag> tags, User user) {
    if (parentIds == null || parentIds.isEmpty()) return;

    switch (parentType) {
      case CHAT:
        List<String> chatTurnIds =
            chatTurnRepository.findChatTurnsByChatList(parentIds, user).stream()
                .map(ChatTurn::getId)
                .collect(Collectors.toList());
        createLinksForTargets(chatTurnIds, TagLinkTargetType.CHAT_TURN, tags, user);
        cascadeCreateLinks(TagLinkTargetType.CHAT_TURN, chatTurnIds, tags, user);
        break;

      case CHAT_TURN:
        List<String> evalIds =
            tagLinkRepository.findEvaluationMonitoringIdsByChatTurnIds(parentIds, user);
        createLinksForTargets(evalIds, TagLinkTargetType.EVALUATION_MONITORING, tags, user);

        List<String> inferIds =
            tagLinkRepository.findInferenceMonitoringIdsByChatTurnIds(parentIds, user);
        createLinksForTargets(inferIds, TagLinkTargetType.INFERENCE_MONITORING, tags, user);
        break;
    }
  }

  private List<TagLink> createLinksForTargets(
      List<String> entityIds, TagLinkTargetType entityType, List<Tag> tagsToApply, User user) {
    if (entityIds == null || entityIds.isEmpty() || tagsToApply == null || tagsToApply.isEmpty()) {
      return Collections.emptyList();
    }
    List<TagLink> existingLinks =
        tagLinkRepository.findByTargetIdsAndTargetType(entityIds, entityType.toString(), user);
    Map<String, Set<String>> existingLinksMap =
        existingLinks.stream()
            .collect(
                Collectors.groupingBy(
                    TagLink::getTargetId,
                    Collectors.mapping(link -> link.getTag().getTagId(), Collectors.toSet())));

    List<TagLink> newLinks = new ArrayList<>();
    for (String entityId : entityIds) {
      Set<String> existingTagIds = existingLinksMap.getOrDefault(entityId, Collections.emptySet());
      for (Tag tag : tagsToApply) {
        if (!existingTagIds.contains(tag.getTagId())) {
          newLinks.add(new TagLink(tag, entityId, entityType, user));
          tag.setUsedCount(tag.getUsedCount() + 1);
        }
      }
    }
    if (newLinks.isEmpty()) {
      return Collections.emptyList();
    }
    List<Tag> usedTags =
        newLinks.stream().map(TagLink::getTag).distinct().collect(Collectors.toList());
    tagRepository.saveAll(usedTags);
    return tagLinkRepository.saveAll(newLinks);
  }

  private void cascadeDeleteLinks(
      TagLinkTargetType parentType,
      List<String> parentIds,
      List<String> tagIds,
      User user,
      Set<TagLink> toDelete) {

    if (parentIds == null || parentIds.isEmpty() || parentType == null) {
      return;
    }

    switch (parentType) {
      case CHAT:
        List<String> chatTurnIds =
            chatTurnRepository.findChatTurnsByChatList(parentIds, user).stream()
                .map(ChatTurn::getId)
                .collect(Collectors.toList());

        if (!chatTurnIds.isEmpty()) {
          toDelete.addAll(
              tagLinkRepository.findByTargetOrTag(
                  chatTurnIds, TagLinkTargetType.CHAT_TURN.toString(), tagIds, user));

          cascadeDeleteLinks(TagLinkTargetType.CHAT_TURN, chatTurnIds, tagIds, user, toDelete);
        }
        break;

      case CHAT_TURN:
        toDelete.addAll(
            tagLinkRepository.findEvaluationTagLinksByChatTurnIdsAndTagIds(
                parentIds, tagIds, user));
        toDelete.addAll(
            tagLinkRepository.findInferenceTagLinksByChatTurnIds(parentIds, tagIds, user));
        break;
    }
  }

  @Transactional
  @Override
  public void updateTagsForEntity(
      User user, List<String> newTagIds, TagLinkTargetType entityType, String entityId) {

    List<TagLink> existingLinks =
        tagLinkRepository.findByTargetIdAndTargetType(entityId, entityType.toString(), user);

    Set<String> existingTagIds =
        existingLinks.stream().map(link -> link.getTag().getTagId()).collect(Collectors.toSet());

    Set<String> newTagIdsSet = new HashSet<>(newTagIds);

    Set<String> idsToRemove = new HashSet<>(existingTagIds);
    idsToRemove.removeAll(newTagIdsSet);

    if (!idsToRemove.isEmpty()) {
      removeTagLinks(user, new ArrayList<>(idsToRemove), List.of(entityId), entityType);
    }

    Set<String> idsToAdd = new HashSet<>(newTagIdsSet);
    idsToAdd.removeAll(existingTagIds);

    if (!idsToAdd.isEmpty()) {
      createTagLinks(user, new ArrayList<>(idsToAdd), entityType, List.of(entityId));
    }
  }
}

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
import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.tags.dto.TagFindResponse;
import com.planck.planck.domain.tags.repository.TagLinkRepository;
import com.planck.planck.domain.tags.repository.TagRepository;
import com.planck.planck.entitities.Tag;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.MonitoringType;
import com.planck.planck.enums.TagType;
import com.planck.planck.exceptions.IllegalArgumentException;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.exceptions.ResourceLimitExceedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TagServiceImpl implements TagService {

  @Autowired private TagRepository tagRepository;

  @Autowired private ApplicationLimits appLimits;

  @Autowired private TagLinkRepository tagLinkRepository;

  @Override
  public Optional<Tag> findByTagIdAndUser(String tagId, User user) {

    return tagRepository.findByTagIdAndUser(tagId, user);
  }

  @Override
  public TagDTO createUserTag(String name, String color, User user) {
    int currentTags = tagRepository.countUserTags(user);
    if (currentTags >= appLimits.getMaxUserTags())
      throw new ResourceLimitExceedException(
          String.format(
              "Maximum allowed user tags %d exceeded, delete some and try again",
              appLimits.getMaxUserTags()));
    Tag createdTag = createTag(name, TagType.USER, color, user);

    return new TagDTO(createdTag);
  }

  @Override
  public TagDTO updateUserTag(String tagId, User user, String name, String color) {
    if (name == null && color == null)
      throw new IllegalInputException("At least name or color must be provided for update");

    Optional<Tag> existingTag = tagRepository.findByTagIdAndUser(tagId, user);

    if (!existingTag.isPresent()) {
      throw new NotFoundException("Tag with provided ID does not exist");
    }

    if (existingTag.get().getType() != TagType.USER)
      throw new IllegalInputException("Only user tags can be updated");

    Tag tagToUpdate = existingTag.get();

    if (name != null) {
      checkTagNotExsits(null, name, TagType.USER, user);
      tagToUpdate.setTagName(name);
    }

    if (color != null) {
      tagToUpdate.setColor(color);
    }

    Tag updatedTag = tagRepository.saveAndFlush(tagToUpdate);

    return new TagDTO(updatedTag);
  }

  @Override
  public List<Tag> findAllUserTags(User user) {
    return tagRepository.findAllUserTags(user);
  }

  @Override
  public List<Tag> saveAll(List<Tag> tags) {
    return tagRepository.saveAllAndFlush(tags);
  }

  @Transactional(readOnly = true)
  @Override
  public TagFindResponse findTags(
      User user, TagType type, String projectId, String modelId, MonitoringType monitoringType) {
    List<Tag> result =
        tagRepository.findWithFilters(user, type, projectId, modelId, monitoringType);

    TagFindResponse response = new TagFindResponse();

    List<TagDTO> userTags = new ArrayList<>();
    List<TagDTO> modelTags = new ArrayList<>();
    List<TagDTO> dataSetTags = new ArrayList<>();

    result.stream()
        .forEach(
            tag -> {
              TagDTO tagDTO = new TagDTO(tag);
              switch (tag.getType()) {
                case USER:
                  userTags.add(tagDTO);
                  break;
                case MODEL:
                  modelTags.add(tagDTO);
                  break;
                case DATASET:
                  dataSetTags.add(tagDTO);
                  break;
                default:
                  break;
              }
            });

    response.setUserTags(userTags);
    response.setModelTags(modelTags);
    response.setDataSetTags(dataSetTags);

    return response;
  }

  @Transactional
  @Override
  public Long deleteByTagNameOrTagIdAndUser(String tagNameOrId, User user) {
    Optional<Tag> tagResponse = tagRepository.findByIdOrNameAndUserId(tagNameOrId, user.getId());
    if (tagResponse.isEmpty())
      throw new NotFoundException("tag: " + tagNameOrId + " does not exist");

    if (tagResponse.get().getType() != TagType.USER) {
      throw new IllegalInputException("Only user tags can be deleted");
    }
    Tag tag = tagResponse.get();
    tagLinkRepository.deleteByTag(tag, user);
    return tagRepository.deleteByUserAndTagId(user, tag.getTagId());
  }

  @Override
  public Set<Tag> findAllTagsByTagIdAndUser(Set<String> tagIds, User user) {
    return tagRepository.findAllTagsByTagIdsAndUser(tagIds, user);
  }

  private Tag createTag(String tagName, TagType type, String color, User user) {
    log.info("Create tag tagName::{}", tagName);
    checkTagNotExsits(null, tagName, type, user);

    Tag tag = new Tag();
    tag.setTagName(tagName);
    tag.setType(type);
    tag.setUser(user);
    tag.setColor(color);
    log.info("Going to save tag tagName::{}", tagName);
    return tagRepository.save(tag);
  }

  private void checkTagNotExsits(String tagId, String tagName, TagType type, User user) {
    log.info("Check tag not exsits tagId :{} tagName::{}", tagId, tagName);
    boolean isExists = tagRepository.countTagBy(tagId, tagName, type, user) > 0;

    if (isExists) {
      log.info("A tag with the same name and type already exists for this user.");
      throw new IllegalArgumentException(
          "A tag with the same name and type already exists for this user.");
    }
  }
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.config.ApplicationLimits;
import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.tags.dto.TagFindResponse;
import com.planck.planck.domain.tags.dto.TagUpdateRequest;
import com.planck.planck.domain.tags.repository.TagLinkRepository;
import com.planck.planck.domain.tags.repository.TagRepository;
import com.planck.planck.entitities.Tag;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.TagType;
import com.planck.planck.exceptions.IllegalInputException;
import com.planck.planck.exceptions.NotFoundException;
import com.planck.planck.exceptions.ResourceLimitExceedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class TagServiceTest {

  @Mock private TagRepository tagRepository;

  @Mock private TagLinkRepository tagLinkRepository;

  @Mock private ApplicationLimits appLimts;

  @InjectMocks private TagServiceImpl tagService;

  private User mockUser;
  private Tag mockTag;

  private String tagId = "tagId";
  private String tagName = "tagName";
  private String color = "color";

  @BeforeEach
  void setUp() {

    MockitoAnnotations.openMocks(this);
    mockUser = new User();

    mockTag = new Tag();
    mockTag.setTagId(tagId);
    mockTag.setTagName(tagName);
    mockTag.setColor(color);
    mockTag.setType(TagType.USER);

    when(appLimts.getMaxUserTags()).thenReturn(200);
  }

  @Test
  void testCreateUserTag_Success() {
    when(tagRepository.countUserTags(mockUser)).thenReturn(0);
    when(tagRepository.save(any(Tag.class))).thenReturn(mockTag);
    when(tagRepository.countTagBy(null, tagName, TagType.USER, mockUser)).thenReturn(0l);

    TagDTO createdTag = tagService.createUserTag(tagName, color, mockUser);

    assertNotNull(createdTag);
    assertEquals(tagName, createdTag.getName());
    assertEquals(color, createdTag.getColor());
  }

  @Test
  void testCreateUserTag_Conflict() {
    when(tagRepository.countUserTags(mockUser)).thenReturn(0);
    when(tagRepository.countTagBy(null, tagName, TagType.USER, mockUser)).thenReturn(1l);

    assertThrows(
        com.planck.planck.exceptions.IllegalArgumentException.class,
        () -> {
          tagService.createUserTag(tagName, color, mockUser);
        });
    verify(tagRepository, never()).save(any());
  }

  @Test
  void testCreateUserTag_Fail_LimitExceeded() {
    when(tagRepository.countUserTags(mockUser)).thenReturn(200);
    assertThrows(
        ResourceLimitExceedException.class,
        () -> {
          tagService.createUserTag(tagName, color, mockUser);
        });

    verify(tagRepository, never()).countTagBy(anyString(), anyString(), any(), any());
    verify(tagRepository, never()).save(any());
  }

  @Test
  void testUpdateTag_Success() {
    when(tagRepository.findByTagIdAndUser(tagId, mockUser)).thenReturn(Optional.of(mockTag));
    when(tagRepository.findByTagNameAndUser(tagName, mockUser)).thenReturn(Optional.empty());
    when(tagRepository.saveAndFlush(any(Tag.class))).thenReturn(mockTag);

    TagUpdateRequest updateRequest = new TagUpdateRequest(tagName, color);
    TagDTO updatedTag =
        tagService.updateUserTag(
            tagId, mockUser, updateRequest.getName(), updateRequest.getColor());
    assertNotNull(updatedTag);
    assertEquals(tagName, updatedTag.getName());
    assertEquals(color, updatedTag.getColor());
  }

  @Test
  void testUpdateTag_OnlyName_Success() {
    when(tagRepository.findByTagIdAndUser(tagId, mockUser)).thenReturn(Optional.of(mockTag));
    when(tagRepository.findByTagNameAndUser(tagName, mockUser)).thenReturn(Optional.empty());
    when(tagRepository.saveAndFlush(any(Tag.class))).thenReturn(mockTag);

    TagUpdateRequest updateRequest = new TagUpdateRequest();
    updateRequest.setName(tagName);
    TagDTO updatedTag =
        tagService.updateUserTag(
            tagId, mockUser, updateRequest.getName(), updateRequest.getColor());
    assertNotNull(updatedTag);
    assertEquals(tagName, updatedTag.getName());
  }

  @Test
  void testUpdateTag_OnlyColor_Success() {
    when(tagRepository.findByTagIdAndUser(tagId, mockUser)).thenReturn(Optional.of(mockTag));
    when(tagRepository.findByTagNameAndUser(tagName, mockUser)).thenReturn(Optional.empty());
    when(tagRepository.saveAndFlush(any(Tag.class))).thenReturn(mockTag);

    TagUpdateRequest updateRequest = new TagUpdateRequest();
    updateRequest.setColor(color);
    TagDTO updatedTag =
        tagService.updateUserTag(
            tagId, mockUser, updateRequest.getName(), updateRequest.getColor());
    assertNotNull(updatedTag);
    assertEquals(color, updatedTag.getColor());
  }

  @Test
  void testUpdateTag_Fail_NotFound() {
    when(tagRepository.findByTagIdAndUser(tagId, mockUser)).thenReturn(Optional.empty());

    TagUpdateRequest updateRequest = new TagUpdateRequest(tagName, color);
    assertThrows(
        NotFoundException.class,
        () ->
            tagService.updateUserTag(
                tagId, mockUser, updateRequest.getName(), updateRequest.getColor()));

    verify(tagRepository, never()).findByTagNameAndUser(tagName, mockUser);
    verify(tagRepository, never()).saveAndFlush(any(Tag.class));
  }

  @Test
  void testUpdateTag_Fail_NotAUserTag() {
    mockTag.setType(TagType.MODEL);
    when(tagRepository.findByTagIdAndUser(tagId, mockUser)).thenReturn(Optional.of(mockTag));

    TagUpdateRequest updateRequest = new TagUpdateRequest(tagName, color);
    assertThrows(
        IllegalInputException.class,
        () ->
            tagService.updateUserTag(
                tagId, mockUser, updateRequest.getName(), updateRequest.getColor()));

    verify(tagRepository, never()).findByTagNameAndUser(tagName, mockUser);
    verify(tagRepository, never()).saveAndFlush(any(Tag.class));
  }

  @Test
  void testUpdateTag_Fail_Conflict() {
    when(tagRepository.findByTagIdAndUser(tagId, mockUser)).thenReturn(Optional.of(mockTag));
    when(tagRepository.countTagBy(null, tagName, TagType.USER, mockUser)).thenReturn(1l);
    TagUpdateRequest updateRequest = new TagUpdateRequest(tagName, color);

    assertThrows(
        com.planck.planck.exceptions.IllegalArgumentException.class,
        () ->
            tagService.updateUserTag(
                tagId, mockUser, updateRequest.getName(), updateRequest.getColor()));

    verify(tagRepository, never()).saveAndFlush(any(Tag.class));
  }

  @Test
  void testUpdateTag_Fail_NoInput() {
    TagUpdateRequest updateRequest = new TagUpdateRequest();

    assertThrows(
        IllegalInputException.class,
        () ->
            tagService.updateUserTag(
                tagId, mockUser, updateRequest.getName(), updateRequest.getColor()));

    verify(tagRepository, never()).findByTagIdAndUser(tagId, mockUser);
    verify(tagRepository, never()).findByTagNameAndUser(tagId, mockUser);
    verify(tagRepository, never()).saveAndFlush(any(Tag.class));
  }

  @Test
  void testFindTagsByType_Success() {
    when(tagRepository.findWithFilters(any(), any(), any(), any(), any()))
        .thenReturn(getTagList(TagType.USER));

    TagFindResponse tagFindResponse = tagService.findTags(mockUser, TagType.USER, null, null, null);
    assertNotNull(tagFindResponse);
    assertEquals(0, tagFindResponse.getDataSetTags().size());
    assertEquals(0, tagFindResponse.getModelTags().size());
    assertEquals(1, tagFindResponse.getUserTags().size());
  }

  @Test
  void testFindAllUserTags_Success() {
    when(tagRepository.findWithFilters(any(), any(), any(), any(), any()))
        .thenReturn(getTagList(null));

    TagFindResponse tagFindResponse = tagService.findTags(mockUser, null, null, null, null);
    assertNotNull(tagFindResponse);
    assertEquals(1, tagFindResponse.getDataSetTags().size());
    assertEquals(1, tagFindResponse.getModelTags().size());
    assertEquals(1, tagFindResponse.getUserTags().size());
  }

  @Test
  void findByTagIdAndUserTest() {

    when(tagRepository.findByTagIdAndUser(mockTag.getTagId(), mockUser))
        .thenReturn(Optional.of(mockTag));

    Optional<Tag> tagOpt = tagService.findByTagIdAndUser(mockTag.getTagId(), mockUser);
    assertTrue(tagOpt.isPresent());
    assertEquals(tagId, tagOpt.get().getTagId());
  }

  @Test
  void findAllTagsByTagIdAndUserTest() {

    Tag tag = new Tag();
    tag.setTagId("tagId");
    when(tagRepository.findAllTagsByTagIdsAndUser(anySet(), any())).thenReturn(Set.of(tag));
    Set<Tag> result = tagService.findAllTagsByTagIdAndUser(Set.of("tagId"), mockUser);
    assertEquals(1, result.size());
    assertEquals("tagId", result.iterator().next().getTagId());
  }

  @Test
  void deleteByTagNameOrTagIdAndUserTest_Success() {
    Tag tag = new Tag();
    tag.setTagId("tagId");
    tag.setType(TagType.USER);
    when(tagRepository.findByIdOrNameAndUserId("tagId", mockUser.getId()))
        .thenReturn(Optional.of(tag));
    when(tagLinkRepository.deleteByTag(any(), any())).thenReturn(1);
    when(tagRepository.deleteByUserAndTagId(any(), any())).thenReturn(1l);
    long result = tagService.deleteByTagNameOrTagIdAndUser("tagId", mockUser);
    assertEquals(1l, result);
  }

  @Test
  void deleteByTagNameOrTagIdAndUserTest_NotFound() {
    when(tagRepository.findByIdOrNameAndUserId("tagId", mockUser.getId()))
        .thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> tagService.deleteByTagNameOrTagIdAndUser("tagId", mockUser));

    verify(tagRepository, never()).deleteByUserAndTagId(any(), any());
    verify(tagLinkRepository, never()).deleteByTag(any(), any());
  }

  @Test
  void deleteByTagNameOrTagIdAndUserTest_NotAUserTag() {
    Tag tag = new Tag();
    tag.setTagId("tagId");
    tag.setType(TagType.MODEL);
    when(tagRepository.findByIdOrNameAndUserId("tagId", mockUser.getId()))
        .thenReturn(Optional.of(tag));

    assertThrows(
        IllegalInputException.class,
        () -> tagService.deleteByTagNameOrTagIdAndUser("tagId", mockUser));

    verify(tagRepository, never()).deleteByUserAndTagId(any(), any());
    verify(tagLinkRepository, never()).deleteByTag(any(), any());
  }

  private List<Tag> getTagList(TagType type) {
    List<Tag> result = new ArrayList<>();

    if (type == null) {
      for (TagType tagType : TagType.values()) {
        Tag tag = new Tag();
        tag.setType(tagType);
        tag.setTagId("tag-" + UUID.randomUUID().toString());
        result.add(tag);
      }
    } else {
      mockTag.setType(type);
      result.add(mockTag);
    }

    return result;
  }
}

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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.config.ApplicationLimits;
import com.planck.planck.domain.chat.service.ChatService;
import com.planck.planck.domain.chatturn.ChatTurnRepository;
import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.tags.dto.TagLinkDTO;
import com.planck.planck.domain.tags.dto.TagLinkRemoveRequest;
import com.planck.planck.domain.tags.dto.TagLinksCreateRequest;
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
import com.planck.planck.exceptions.ResourceLimitExceedException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagLinkServiceTest {

  @Mock private TagRepository tagRepository;

  @Mock private TagLinkRepository tagLinkRepository;

  @Mock private ChatService chatService;
  @Mock private ChatTurnRepository chatTurnRepository;

  @Mock private EntityFetcherRegistry fetcherRegistry;

  @InjectMocks @Spy private TagLinkServiceImpl tagLinkService;

  @Mock private ApplicationLimits applicationLimits;

  private User testUser;
  private Tag tag1;
  private Tag tag2;
  private Tag tag3;
  private ChatTurn chatTurn1;
  private ChatTurn chatTurn2;
  private TagLink tagLink1;
  private TagLink tagLink2;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId("user-123");
    testUser.setEmail("test@example.com");

    tag1 = createTestTag("tag-1", "Tag One", TagType.USER, testUser);
    tag2 = createTestTag("tag-2", "Tag Two", TagType.USER, testUser);
    tag3 = createTestTag("tag-3", "Tag Еркуу", TagType.USER, testUser);

    chatTurn1 = createTestChatTurn("ct-1");
    chatTurn2 = createTestChatTurn("ct-2");

    tagLink1 = createTestTagLink("tl-1", tag1, chatTurn1, testUser);
    tagLink2 = createTestTagLink("tl-2", tag2, chatTurn1, testUser);
    lenient().when(applicationLimits.getMaxTagsPerEntity()).thenReturn(5);
  }

  // --- Helper Methods ---
  private Tag createTestTag(String id, String name, TagType type, User user) {
    Tag tag = new Tag();
    tag.setTagId(id);
    tag.setTagName(name);
    tag.setType(type);
    tag.setUser(user);
    tag.setCreatedAt(Timestamp.from(Instant.now()));
    tag.setUpdatedAt(Timestamp.from(Instant.now()));
    return tag;
  }

  private ChatTurn createTestChatTurn(String id) {
    ChatTurn chatTurn = new ChatTurn();
    chatTurn.setId(id);
    // Add other necessary fields if needed
    return chatTurn;
  }

  private TagLink createTestTagLink(String id, Tag tag, ChatTurn chatTurn, User user) {
    TagLink tagLink = new TagLink();
    tagLink.setId(id);
    tagLink.setTag(tag);
    tagLink.setTargetId(chatTurn.getId());
    tagLink.setTargetType(TagLinkTargetType.CHAT_TURN);
    tagLink.setUser(user);
    tagLink.setCreatedAt(Timestamp.from(Instant.now()));
    tagLink.setUpdatedAt(Timestamp.from(Instant.now()));
    return tagLink;
  }

  // --- Test Cases ---

  @Test
  @DisplayName("addTag - Should add tag successfully")
  void addTag_Success() {
    // Arrange
    String tagId = "tag-1";
    String entityId = "entity-1";
    TagLinkTargetType entityType = TagLinkTargetType.CHAT_TURN;
    Tag tag = new Tag();
    tag.setTagId(tagId);
    tag.setType(TagType.USER);
    when(applicationLimits.getMaxTagsPerEntity()).thenReturn(5);
    when(tagRepository.findByTagIdAndUser(tagId, testUser)).thenReturn(Optional.of(tag));
    when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(tagLinkRepository.save(any(TagLink.class)))
        .thenAnswer(
            invocation -> {
              TagLink savedTagLink = invocation.getArgument(0);
              savedTagLink.setId("tagLink-1");
              return savedTagLink;
            });

    // Act
    TagLinkDTO result = tagLinkService.createTagLink(entityType, entityId, tagId, testUser);

    // Assert
    assertNotNull(result);
    assertEquals("tagLink-1", result.getId());
    assertEquals(tagId, result.getTagId());
    assertEquals(entityId, result.getTargetId());
    assertEquals(entityType, result.getTargetType());
    verify(tagRepository, times(1)).findByTagIdAndUser(tagId, testUser);
    verify(tagLinkRepository, times(1)).save(any(TagLink.class));
  }

  @Test
  @DisplayName("addTag - Should throw NotFoundException when tag not found")
  void addTag_NotFound() {
    // Arrange
    String tagId = "tag-1";
    String entityId = "entity-1";
    TagLinkTargetType entityType = TagLinkTargetType.CHAT_TURN;
    when(tagRepository.findByTagIdAndUser(tagId, testUser)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        NotFoundException.class,
        () -> tagLinkService.createTagLink(entityType, entityId, tagId, testUser));
    verify(tagRepository, times(1)).findByTagIdAndUser(tagId, testUser);
    verify(tagLinkRepository, never()).saveAndFlush(any(TagLink.class));
  }

  @Test
  void findHydratedEntitiesByTag_Success_WithTargetType() {
    // Arrange
    String tagName = "tag-1";
    TagLinkTargetType targetType = TagLinkTargetType.CHAT_TURN;
    List<TagLink> tagLinks = Arrays.asList(tagLink1, tagLink2);
    when(tagRepository.findByTagIdAndUser(tagName, testUser)).thenReturn(Optional.of(tag1));
    when(tagLinkRepository.findByTagAndTargetType(tag1, targetType.toString(), testUser))
        .thenReturn(tagLinks);
    when(fetcherRegistry.getFetcher(targetType)).thenReturn(new TestEntityFetcher());

    // Act
    var result = tagLinkService.findHydratedEntitiesByTag(tagName, testUser, targetType);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.containsKey(targetType));
    assertEquals(1, result.get(targetType).size());
    verify(tagRepository, times(1)).findByTagIdAndUser(tagName, testUser);
    verify(tagLinkRepository, times(1))
        .findByTagAndTargetType(tag1, targetType.toString(), testUser);
    verify(fetcherRegistry, times(1)).getFetcher(targetType);
  }

  @Test
  void findHydratedEntitiesByTag_Success_WithoutTargetType() {
    // Arrange
    String tagName = "tag-1";
    TagLinkTargetType targetType = TagLinkTargetType.CHAT_TURN;
    List<TagLink> tagLinks = Arrays.asList(tagLink1, tagLink2);
    when(tagRepository.findByTagIdAndUser(tagName, testUser)).thenReturn(Optional.of(tag1));
    when(tagLinkRepository.findByTag(tag1, testUser)).thenReturn(tagLinks);
    when(fetcherRegistry.getFetcher(targetType)).thenReturn(new TestEntityFetcher());

    var result = tagLinkService.findHydratedEntitiesByTag(tagName, testUser, null);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.containsKey(targetType));
    assertEquals(1, result.get(targetType).size());
    verify(tagRepository, times(1)).findByTagIdAndUser(tagName, testUser);
    verify(tagLinkRepository, times(1)).findByTag(tag1, testUser);
    verify(fetcherRegistry, times(1)).getFetcher(targetType);
  }

  @Test
  @DisplayName("createTagLinks - Should create links successfully using targetEntityIds")
  void createTagLinks_Success_WithTargetEntityIds() {
    // Arrange
    TagLinksCreateRequest request = new TagLinksCreateRequest();
    request.setEntityIds(Arrays.asList(chatTurn1.getId(), chatTurn2.getId()));
    request.setTagIds(Arrays.asList(tag1.getTagId(), tag2.getTagId()));
    request.setEntityType(TagLinkTargetType.CHAT_TURN);

    Set<Tag> foundTags = new HashSet<>(Arrays.asList(tag1, tag2));

    when(applicationLimits.getMaxTagsPerEntity()).thenReturn(5);
    when(tagRepository.findAllTagsByTagIdsAndUser(anySet(), eq(testUser))).thenReturn(foundTags);
    when(tagRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    when(tagLinkRepository.saveAll(anyList()))
        .thenAnswer(
            invocation -> {
              List<TagLink> linksToSave = invocation.getArgument(0);
              return linksToSave;
            });

    // Act
    List<TagLinkDTO> result =
        tagLinkService.createTagLinks(
            testUser, request.getTagIds(), request.getEntityType(), request.getEntityIds());

    // Assert
    assertNotNull(result);
    // Expecting 2 tags * 2 chat turns = 4 links
    assertEquals(4, result.size());
    verify(tagRepository, times(1)).findAllTagsByTagIdsAndUser(anySet(), eq(testUser));
    verify(tagLinkRepository, times(1)).saveAll(anyList());
  }

  @Test
  @DisplayName("getTagLinksByEntity - Should return a list of TagLinkDTOs when links exist")
  void getTagLinksDtoByEntity_Success() {
    List<TagLink> tagLinks = Arrays.asList(tagLink1, tagLink2);
    when(tagLinkRepository.findByTargetIdAndTargetType(
            chatTurn1.getId(), TagLinkTargetType.CHAT_TURN.toString(), testUser))
        .thenReturn(tagLinks);

    List<TagLinkDTO> result =
        tagLinkService.getTagLinksDtoByEntity(
            TagLinkTargetType.CHAT_TURN, chatTurn1.getId(), testUser);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(tagLink1.getId(), result.get(0).getId());
    assertEquals(tagLink1.getTag().getTagId(), result.get(0).getTagId());
    assertEquals(tagLink2.getId(), result.get(1).getId());
    assertEquals(tagLink2.getTag().getTagId(), result.get(1).getTagId());
    verify(tagLinkRepository, times(1))
        .findByTargetIdAndTargetType(
            chatTurn1.getId(), TagLinkTargetType.CHAT_TURN.toString(), testUser);
  }

  @Test
  @DisplayName("getTagLinks - Should return list of TagLinkDTOs when links exist")
  void getTagLinks_ReturnsDTOs_WhenLinksExist() {
    // Arrange
    List<TagLink> tagLinks = Arrays.asList(tagLink1, tagLink2);
    when(tagLinkRepository.findAllByUser(testUser)).thenReturn(tagLinks);

    // Act
    List<TagLinkDTO> result = tagLinkService.getTagLinks(testUser, null);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(tagLink1.getId(), result.get(0).getId());
    assertEquals(tagLink1.getTag().getTagId(), result.get(0).getTagId());
    assertEquals(tagLink2.getId(), result.get(1).getId());
    assertEquals(tagLink2.getTag().getTagId(), result.get(1).getTagId());
    verify(tagLinkRepository, times(1)).findAllByUser(testUser);
  }

  @Test
  @DisplayName("getTagLinks - Should return list of TagLinkDTOs when links exist")
  void getTagLinks_ReturnsDTOs_WithTargetType() {
    // Arrange
    List<TagLink> tagLinks = Arrays.asList(tagLink1, tagLink2);
    when(tagLinkRepository.findByTargetType(any(), eq(testUser))).thenReturn(tagLinks);

    // Act
    List<TagLinkDTO> result = tagLinkService.getTagLinks(testUser, TagLinkTargetType.CHAT_TURN);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(tagLink1.getId(), result.get(0).getId());
    assertEquals(tagLink1.getTag().getTagId(), result.get(0).getTagId());
    assertEquals(tagLink2.getId(), result.get(1).getId());
    assertEquals(tagLink2.getTag().getTagId(), result.get(1).getTagId());
    verify(tagLinkRepository, times(1)).findByTargetType(any(), eq(testUser));
  }

  @Test
  @DisplayName("getTagLinks - Should return empty list when no links exist")
  void getTagLinks_ReturnsEmptyList_WhenNoLinksExist() {
    // Arrange
    when(tagLinkRepository.findAllByUser(testUser)).thenReturn(Collections.emptyList());

    // Act
    List<TagLinkDTO> result = tagLinkService.getTagLinks(testUser, null);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(tagLinkRepository, times(1)).findAllByUser(testUser);
  }

  @Test
  @DisplayName("deleteTagLinkById - Should call repository delete method")
  void deleteTagLinkById_CallsRepository() {
    // Arrange
    String linkIdToDelete = "tl-1";
    String chatId = "chat-123";

    TagLink tagLink = getTagLink(linkIdToDelete, "tag1", "tagName1", chatId);
    tagLink.setTargetType(TagLinkTargetType.CHAT);

    when(tagLinkRepository.findById(linkIdToDelete)).thenReturn(Optional.of(tagLink));

    // Act
    tagLinkService.deleteTagLinkById(linkIdToDelete, testUser);

    // Assert
    verify(tagLinkRepository).findById(linkIdToDelete);
    verify(tagLinkRepository).deleteAll(any());
  }

  @Test
  @DisplayName("removeTagLinks - Should delete by targetIds and targetType")
  void removeTagLinks_DeletesByTarget() {
    // Arrange
    TagLinkRemoveRequest request = new TagLinkRemoveRequest();
    List<String> targetIds = Arrays.asList(chatTurn1.getId(), chatTurn2.getId());
    TagLinkTargetType targetType = TagLinkTargetType.CHAT_TURN;
    request.setEntityIds(targetIds);
    request.setEntityType(targetType);

    Set<TagLink> links =
        Set.of(
            getTagLink("target1", "tag1", "tagName1"),
            getTagLink("target2", "tag2", "tagName2"),
            getTagLink("target3", "tag3", "tagName3"));
    when(tagLinkRepository.findByTargetOrTag(targetIds, targetType.toString(), null, testUser))
        .thenReturn(links);

    // Act
    int deletedCount =
        tagLinkService.removeTagLinks(
            testUser, request.getTagIds(), request.getEntityIds(), request.getEntityType());

    // Assert
    assertEquals(links.size(), deletedCount);
    verify(tagLinkRepository, times(1))
        .findByTargetOrTag(targetIds, targetType.toString(), null, testUser);

    verify(tagLinkRepository, times(1)).deleteByIdsAndUser(anyList(), any());
  }

  @Test
  @DisplayName("removeTagLinks - Should delete by tagIds")
  void removeTagLinks_DeletesByTag() {
    // Arrange
    TagLinkRemoveRequest request = new TagLinkRemoveRequest();
    List<String> tagIds = Arrays.asList(tag1.getTagId(), tag2.getTagId());
    request.setTagIds(tagIds);

    Set<TagLink> links =
        Set.of(
            getTagLink("target1", "tag1", "tagName1"),
            getTagLink("target2", "tag2", "tagName2"),
            getTagLink("target3", "tag3", "tagName3"));
    when(tagLinkRepository.findByTargetOrTag(null, null, tagIds, testUser)).thenReturn(links);

    int expectedDeletedCount = 3;
    // Act
    int deletedCount =
        tagLinkService.removeTagLinks(
            testUser, request.getTagIds(), request.getEntityIds(), request.getEntityType());

    // Assert
    assertEquals(expectedDeletedCount, deletedCount);
    verify(tagLinkRepository, times(1)).findByTargetOrTag(isNull(), isNull(), anyList(), any());
    verify(tagLinkRepository, times(1)).deleteByIdsAndUser(anyList(), any());
  }

  @Test
  @DisplayName("removeTagLinks - Should delete by both target and tag")
  void removeTagLinks_DeletesByBoth() {
    // Arrange
    TagLinkRemoveRequest request = new TagLinkRemoveRequest();
    List<String> targetIds = List.of(chatTurn1.getId());
    TagLinkTargetType targetType = TagLinkTargetType.CHAT_TURN;
    List<String> tagIds = List.of(tag1.getTagId());
    request.setEntityIds(targetIds);
    request.setEntityType(targetType);
    request.setTagIds(tagIds);

    Set<TagLink> links =
        Set.of(
            getTagLink("target1", "tag1", "tagName1"),
            getTagLink("target2", "tag2", "tagName2"),
            getTagLink("target3", "tag3", "tagName3"));
    when(tagLinkRepository.findByTargetOrTag(targetIds, targetType.toString(), tagIds, testUser))
        .thenReturn(links);

    // Act
    int deletedCount =
        tagLinkService.removeTagLinks(
            testUser, request.getTagIds(), request.getEntityIds(), request.getEntityType());

    // Assert
    assertEquals(3, deletedCount); // de-duped if needed
    verify(tagLinkRepository).deleteByIdsAndUser(anyList(), any());
  }

  @Test
  @DisplayName("removeTagLinks - Should throw exception if targetIds present without targetType")
  void removeTagLinks_ThrowsException_WhenTargetTypeMissing() {
    // Arrange
    TagLinkRemoveRequest request = new TagLinkRemoveRequest();
    request.setEntityIds(Collections.singletonList(chatTurn1.getId()));
    // request.setTargetType(TagLinkTargetType.CHAT_TURN); // Missing

    // Act & Assert
    IllegalInputException exception =
        assertThrows(
            IllegalInputException.class,
            () ->
                tagLinkService.removeTagLinks(
                    testUser,
                    request.getTagIds(),
                    request.getEntityIds(),
                    request.getEntityType()));
    assertEquals("Target type is required when using target_ids", exception.getMessage());
    verify(tagLinkRepository, never())
        .deleteByTargetIdListAndTargetType(anyList(), anyString(), any(User.class));
    verify(tagLinkRepository, never()).deleteByTagIds(anyList(), any(User.class));
  }

  @Test
  @DisplayName("removeTagLinks - Should return 0 if both lists are null or empty")
  void removeTagLinks_ReturnsZero_ForEmptyInputs() {
    TagLinkRemoveRequest request2 = new TagLinkRemoveRequest();
    request2.setTagIds(Collections.emptyList());
    request2.setEntityIds(Collections.emptyList());
    request2.setEntityType(TagLinkTargetType.CHAT_TURN);

    // Act
    int deletedCount1 =
        tagLinkService.removeTagLinks(
            testUser, request2.getTagIds(), request2.getEntityIds(), request2.getEntityType());
    int deletedCount2 =
        tagLinkService.removeTagLinks(
            testUser, request2.getTagIds(), request2.getEntityIds(), request2.getEntityType());

    // Assert
    assertEquals(0, deletedCount1);
    assertEquals(0, deletedCount2);
    verify(tagLinkRepository, never())
        .deleteByTargetIdListAndTargetType(anyList(), anyString(), any(User.class));
    verify(tagLinkRepository, never()).deleteByTagIds(anyList(), any(User.class));
  }

  @Test
  void saveAll_shouldCallRepository() {
    List<TagLink> links = List.of(tagLink1, tagLink2);
    when(tagLinkRepository.saveAllAndFlush(links)).thenReturn(links);

    List<TagLink> result = tagLinkService.saveAll(links);

    assertEquals(links, result);
    verify(tagLinkRepository).saveAllAndFlush(links);
  }

  @Test
  void getTagLinksByEntities_shouldCallRepository() {
    List<String> entityIds = List.of(chatTurn1.getId());
    TagLinkTargetType type = TagLinkTargetType.CHAT_TURN;
    List<TagLink> expectedLinks = List.of(tagLink1);

    when(tagLinkRepository.findByTargetIdsAndTargetType(entityIds, type.toString(), testUser))
        .thenReturn(expectedLinks);

    List<TagLink> result = tagLinkService.getTagLinksByEntities(type, entityIds, testUser);

    assertEquals(expectedLinks, result);
    verify(tagLinkRepository).findByTargetIdsAndTargetType(entityIds, type.toString(), testUser);
  }

  @Test
  void getUserTagsByTargetTypeAndEntityList_shouldGroupAndHydrateTags() {
    List<String> entityIds = List.of(chatTurn1.getId(), chatTurn2.getId());
    TagLinkTargetType type = TagLinkTargetType.CHAT_TURN;

    List<Object[]> rawLinks =
        List.of(
            new Object[] {chatTurn1.getId(), tag1.getTagId()},
            new Object[] {chatTurn1.getId(), tag2.getTagId()},
            new Object[] {chatTurn2.getId(), tag1.getTagId()});
    when(tagLinkRepository.findTargetIdAndTagIdByTargetIdsAndTypeAndUser(
            entityIds, type.toString(), testUser))
        .thenReturn(rawLinks);

    Set<String> allTagIds = Set.of(tag1.getTagId(), tag2.getTagId());
    when(tagRepository.findAllById(allTagIds)).thenReturn(List.of(tag1, tag2));

    // Act
    Map<String, List<TagDTO>> result =
        tagLinkService.getUserTagsByTargetTypeAndEntityList(entityIds, type, testUser);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.containsKey(chatTurn1.getId()));
    assertTrue(result.containsKey(chatTurn2.getId()));
    assertEquals(2, result.get(chatTurn1.getId()).size());
    assertEquals(1, result.get(chatTurn2.getId()).size());
    List<String> e1TagNames = result.get(chatTurn1.getId()).stream().map(TagDTO::getName).toList();
    assertTrue(e1TagNames.contains("Tag One"));
    assertTrue(e1TagNames.contains("Tag Two"));
  }

  @Test
  void syncEvaluationMonitoringTags_shouldAddMissingLinks() {
    String evalId = "eval-123";
    when(tagLinkRepository.findTagsForEvaluationMonitoring(evalId, testUser))
        .thenReturn(List.of(tag1));

    // Act
    tagLinkService.syncEvaluationMonitoringTags(evalId, testUser);

    // Assert
    ArgumentCaptor<List<TagLink>> captor = ArgumentCaptor.forClass(List.class);
    verify(tagLinkRepository).saveAll(captor.capture());
    List<TagLink> savedLinks = captor.getValue();
    assertEquals(1, savedLinks.size());
    assertEquals(tag1, savedLinks.get(0).getTag());
    assertEquals(evalId, savedLinks.get(0).getTargetId());
    assertEquals(TagLinkTargetType.EVALUATION_MONITORING, savedLinks.get(0).getTargetType());
  }

  @Test
  void syncInferenceMonitoringTags_shouldAddMissingLinks() {
    String inferId = "infer-123";
    when(tagLinkRepository.findTagsForEvaluationMonitoring(inferId, testUser))
        .thenReturn(List.of(tag2));

    // Act
    tagLinkService.syncInferenceMonitoringTags(inferId, testUser);

    // Assert
    ArgumentCaptor<List<TagLink>> captor = ArgumentCaptor.forClass(List.class);
    verify(tagLinkRepository).saveAll(captor.capture());
    List<TagLink> savedLinks = captor.getValue();
    assertEquals(1, savedLinks.size());
    assertEquals(tag2, savedLinks.get(0).getTag());
    assertEquals(inferId, savedLinks.get(0).getTargetId());
    assertEquals(TagLinkTargetType.INFERENCE_MONITORING, savedLinks.get(0).getTargetType());
  }

  @Test
  void createTagLinksForProject_shouldDelegateToCreateTagLinks() {
    String projectId = "proj-1";
    List<String> tagIds = List.of(tag1.getTagId());
    List<String> chatTurnIds = List.of(chatTurn1.getId(), chatTurn2.getId());

    when(chatTurnRepository.findChatTurnIdsByContainerId(projectId, testUser))
        .thenReturn(chatTurnIds);

    List<TagLinkDTO> expectedDTOs = List.of(new TagLinkDTO());
    doReturn(expectedDTOs)
        .when(tagLinkService)
        .createTagLinks(testUser, tagIds, TagLinkTargetType.CHAT_TURN, chatTurnIds);

    // Act
    List<TagLinkDTO> result = tagLinkService.createTagLinksForProject(testUser, projectId, tagIds);

    // Assert
    assertEquals(expectedDTOs, result);
    verify(chatTurnRepository).findChatTurnIdsByContainerId(projectId, testUser);
    verify(tagLinkService)
        .createTagLinks(testUser, tagIds, TagLinkTargetType.CHAT_TURN, chatTurnIds);
  }

  @Test
  void removeTagLinksForProject_shouldDelegateToRemoveTagLinks() {
    String projectId = "proj-1";
    List<String> tagIds = List.of(tag1.getTagId());
    List<String> chatTurnIds = List.of(chatTurn1.getId(), chatTurn2.getId());

    when(chatTurnRepository.findChatTurnIdsByContainerId(projectId, testUser))
        .thenReturn(chatTurnIds);

    doReturn(2)
        .when(tagLinkService)
        .removeTagLinks(testUser, tagIds, chatTurnIds, TagLinkTargetType.CHAT_TURN);

    // Act
    int count = tagLinkService.removeTagLinksForProject(testUser, projectId, tagIds);

    // Assert
    assertEquals(2, count);
    verify(chatTurnRepository).findChatTurnIdsByContainerId(projectId, testUser);
    verify(tagLinkService)
        .removeTagLinks(testUser, tagIds, chatTurnIds, TagLinkTargetType.CHAT_TURN);
  }

  @Test
  void updateTagsForEntity_shouldAddRemoveAndKeepTags() {
    String entityId = chatTurn1.getId();
    TagLinkTargetType type = TagLinkTargetType.CHAT_TURN;

    List<TagLink> existingLinks = List.of(tagLink1, tagLink2);
    when(tagLinkRepository.findByTargetIdAndTargetType(entityId, type.toString(), testUser))
        .thenReturn(existingLinks);

    List<String> newTagIds = List.of(tag2.getTagId(), tag3.getTagId());

    doReturn(1)
        .when(tagLinkService)
        .removeTagLinks(eq(testUser), anyList(), eq(List.of(entityId)), eq(type));
    doReturn(new ArrayList<TagLinkDTO>())
        .when(tagLinkService)
        .createTagLinks(eq(testUser), anyList(), eq(type), eq(List.of(entityId)));

    // Act
    tagLinkService.updateTagsForEntity(testUser, newTagIds, type, entityId);

    // Assert
    ArgumentCaptor<List<String>> removeCaptor = ArgumentCaptor.forClass(List.class);
    verify(tagLinkService)
        .removeTagLinks(eq(testUser), removeCaptor.capture(), eq(List.of(entityId)), eq(type));
    assertEquals(1, removeCaptor.getValue().size());
    assertEquals(tag1.getTagId(), removeCaptor.getValue().get(0));

    ArgumentCaptor<List<String>> addCaptor = ArgumentCaptor.forClass(List.class);
    verify(tagLinkService)
        .createTagLinks(eq(testUser), addCaptor.capture(), eq(type), eq(List.of(entityId)));
    assertEquals(1, addCaptor.getValue().size());
    assertEquals(tag3.getTagId(), addCaptor.getValue().get(0));
  }

  @Test
  void createTagLinks_whenLimitExceeded_shouldThrowException() {
    when(applicationLimits.getMaxTagsPerEntity()).thenReturn(2);
    List<String> entityIds = List.of(chatTurn1.getId());
    List<String> tagIds = List.of(tag1.getTagId(), tag2.getTagId(), tag3.getTagId());
    List<Tag> tags = List.of(tag1, tag2, tag3);

    doReturn(tags).when(tagLinkService).getUserTags(tagIds, testUser);

    when(tagLinkRepository.findTargetIdAndTagIdByTargetIdsAndTypeAndUser(any(), any(), any()))
        .thenReturn(Collections.emptyList());

    // Act
    // Assert
    ResourceLimitExceedException ex =
        assertThrows(
            ResourceLimitExceedException.class,
            () ->
                tagLinkService.createTagLinks(
                    testUser, tagIds, TagLinkTargetType.CHAT_TURN, entityIds));

    assertTrue(ex.getMessage().contains("Tag limit of 2 would be exceeded"));
  }

  @Test
  void createTagLinks_whenParentIsChat_shouldCascadeToChildren() {
    String chatId = "chat-1";
    List<String> chatIds = List.of(chatId);
    List<String> tagIds = List.of(tag1.getTagId());
    List<Tag> tags = List.of(tag1);
    tag1.setUsedCount(0);

    doReturn(tags).when(tagLinkService).getUserTags(tagIds, testUser);
    when(tagLinkRepository.findTargetIdAndTagIdByTargetIdsAndTypeAndUser(
            eq(chatIds), eq(TagLinkTargetType.CHAT.toString()), eq(testUser)))
        .thenReturn(Collections.emptyList());

    when(chatTurnRepository.findChatTurnsByChatList(chatIds, testUser))
        .thenReturn(List.of(chatTurn1));

    when(tagLinkRepository.findEvaluationMonitoringIdsByChatTurnIds(
            List.of(chatTurn1.getId()), testUser))
        .thenReturn(List.of("eval-1"));
    when(tagLinkRepository.findInferenceMonitoringIdsByChatTurnIds(
            List.of(chatTurn1.getId()), testUser))
        .thenReturn(List.of("infer-1"));

    when(tagLinkRepository.findByTargetIdsAndTargetType(any(), anyString(), eq(testUser)))
        .thenReturn(Collections.emptyList());

    when(tagLinkRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

    // Act
    tagLinkService.createTagLinks(testUser, tagIds, TagLinkTargetType.CHAT, chatIds);

    // Assert
    verify(tagLinkRepository, times(4)).saveAll(anyList());
    verify(tagRepository, times(4)).saveAll(anyList());
    assertEquals(4, tag1.getUsedCount());
  }

  private TagLink getTagLink(String linkIdToDelete, String tagId, String tagName) {
    TagLink tagLink = new TagLink();
    tagLink.setId(linkIdToDelete);
    Tag tag = new Tag();
    tag.setTagId(tagId);
    tag.setTagName(tagName);
    tag.setType(TagType.USER);
    tagLink.setTag(tag);
    return tagLink;
  }

  private TagLink getTagLink(String linkId, String tagId, String tagName, String targetId) {
    TagLink tagLink = new TagLink();
    tagLink.setId(linkId);
    tagLink.setTargetId(targetId);

    Tag tag = new Tag();
    tag.setTagId(tagId);
    tag.setTagName(tagName);
    tag.setType(TagType.USER);
    tagLink.setTag(tag);

    return tagLink;
  }
}

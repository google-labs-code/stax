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

import com.planck.planck.domain.tags.dto.TagLinkDTO;
import com.planck.planck.domain.tags.dto.TagLinkRemoveRequest;
import com.planck.planck.domain.tags.dto.TagLinksCreateRequest;
import com.planck.planck.domain.tags.dto.TaggedEntitiesResponse;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.TagLinkTargetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tag-links")
@Tag(name = "Tag Links API", description = "API for managing tag links")
public class TagLinkController {
  @Autowired private TagLinkService tagLinkService;

  @Operation(
      summary = "Retrieve tag links for a specific entity",
      description = "Get all tag links associated with a specific entity",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Entity not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{entityType}/{entityId}")
  public ResponseEntity<List<TagLinkDTO>> getTagLinksForEntity(
      @Parameter(
              description = "Type of the linked entity",
              schema = @Schema(implementation = TagLinkTargetType.class))
          @PathVariable
          TagLinkTargetType entityType,
      @Parameter(description = "ID of the entity", example = "entity-uuid4") @PathVariable
          String entityId,
      @AuthenticationPrincipal User user) {
    List<TagLinkDTO> tagLinks = tagLinkService.getTagLinksDtoByEntity(entityType, entityId, user);
    return ResponseEntity.ok().body(tagLinks);
  }

  @GetMapping
  @Operation(
      summary = "Retrieve all tag links",
      description = "Get all tag links for the user, optinally filtered by target type",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<List<TagLinkDTO>> getTagLinks(
      @Parameter(
              description = "Filter by target entity type",
              schema = @Schema(implementation = TagLinkTargetType.class))
          @RequestParam(required = false)
          TagLinkTargetType entityType,
      @AuthenticationPrincipal User user) {
    List<TagLinkDTO> result = tagLinkService.getTagLinks(user, entityType);
    return ResponseEntity.ok(result);
  }

  @Operation(
      summary = "Get all hydrated entities linked to a tag, grouped by type",
      description =
          """
        Returns hydrated tagged entities based on a provided tag_id
        In addition, targetType can be optionally provided to only filter out a specific entity type.""")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Tagged entities grouped by type"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "User Tag not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/entities/by-tag/{tagId}")
  public ResponseEntity<TaggedEntitiesResponse> getEntitiesByTag(
      @Parameter(description = "ID of the tag", example = "tag-uuid4") @PathVariable String tagId,
      @Parameter(
              description = "Filter by target entity type",
              schema = @Schema(implementation = TagLinkTargetType.class))
          @RequestParam(required = false)
          TagLinkTargetType entityType,
      @AuthenticationPrincipal User user) {

    var result = tagLinkService.findHydratedEntitiesByTag(tagId, user, entityType);
    return ResponseEntity.ok(new TaggedEntitiesResponse(result));
  }

  @PostMapping()
  @Operation(
      summary = "Create multiple tag links for the tag/entity id combinations",
      description =
          "Create multiple tag links from any of the supported entities. Each entity from the payload will be linked to each tag from the payload",
      responses = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<List<TagLinkDTO>> createTagLinks(
      @Validated @RequestBody TagLinksCreateRequest request, @AuthenticationPrincipal User user) {

    List<TagLinkDTO> result =
        tagLinkService.createTagLinks(
            user, request.getTagIds(), request.getEntityType(), request.getEntityIds());
    URI createdLocation = URI.create("/tag_links?entityType=" + request.getEntityType());
    return ResponseEntity.created(createdLocation).body(result);
  }

  @PostMapping("/project/{projectId}")
  @Operation(
      summary = "Create multiple tag links for all chat turns in a project",
      description = "Create multiple tag links for all chat turns in a project.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<List<TagLinkDTO>> createTagLinksForProject(
      @Validated @RequestBody List<String> tagIds,
      @PathVariable String projectId,
      @AuthenticationPrincipal User user) {

    List<TagLinkDTO> result = tagLinkService.createTagLinksForProject(user, projectId, tagIds);
    URI createdLocation = URI.create("/tag_links?entityType=" + TagLinkTargetType.CHAT_TURN);
    return ResponseEntity.created(createdLocation).body(result);
  }

  @Operation(
      summary = "Add a tag to a specific entity",
      description = "Add a tag to a specific entity by entity type and ID",
      responses = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/{entityType}/{entityId}")
  public ResponseEntity<TagLinkDTO> addTag(
      @Parameter(
              description = "Type of the entity",
              schema = @Schema(implementation = TagLinkTargetType.class))
          @PathVariable
          TagLinkTargetType entityType,
      @Parameter(description = "ID of the entity", example = "entity-uuid4") @PathVariable
          String entityId,
      @Parameter(description = "ID of the tag", example = "tag-uuid4") @RequestParam String tagId,
      @AuthenticationPrincipal User user) {
    TagLinkDTO tagLink = tagLinkService.createTagLink(entityType, entityId, tagId, user);
    URI createdLocation = URI.create(String.format("/tag_links/%s", tagLink.getId()));
    return ResponseEntity.created(createdLocation).body(tagLink);
  }

  @DeleteMapping("{id}")
  @Operation(
      summary = "Delete tag links by ID",
      description = "Delete tag links by ID",
      responses = {
        @ApiResponse(responseCode = "204", description = "No content"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Void> deleteTagLinkById(
      @Parameter(description = "ID of the tag link", example = "tag-link-uuid4") @PathVariable
          String id,
      @AuthenticationPrincipal User user) {
    tagLinkService.deleteTagLinkById(id, user);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/remove")
  @Operation(
      summary = "Remove tag links based on provided payload",
      description = "Remove tag links based on provided payload",
      responses = {
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Integer>> deleteTagLinks(
      @Validated @RequestBody TagLinkRemoveRequest request, @AuthenticationPrincipal User user) {
    int deleted =
        tagLinkService.removeTagLinks(
            user, request.getTagIds(), request.getEntityIds(), request.getEntityType());
    Map<String, Integer> response = new HashMap<>();
    response.put("deleted", deleted);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/remove/project/{projectId}")
  @Operation(
      summary = "Remove tag links based on provided payload",
      description = "Remove tag links based on provided payload",
      responses = {
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<Map<String, Integer>> deleteTagLinksForProject(
      @Validated @RequestBody List<String> tagIds,
      @PathVariable String projectId,
      @AuthenticationPrincipal User user) {
    int deleted = tagLinkService.removeTagLinksForProject(user, projectId, tagIds);
    Map<String, Integer> response = new HashMap<>();
    response.put("deleted", deleted);
    return ResponseEntity.ok(response);
  }
}

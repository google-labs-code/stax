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

import com.planck.planck.Status200Response;
import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.tags.dto.TagFindResponse;
import com.planck.planck.domain.tags.dto.TagUpdateRequest;
import com.planck.planck.domain.tags.dto.UserTagCreateRequest;
import com.planck.planck.entitities.Tag;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.MonitoringType;
import com.planck.planck.enums.TagType;
import com.planck.planck.exceptions.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// @Validated
@RestController
@RequestMapping("/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Operations related to tags")
public class TagsController {

  @Autowired private TagService tagService;

  @GetMapping()
  @Operation(
      summary = "Find all tags by user",
      description = "Retrieves all tags associated with the authenticated user.",
      responses = {@ApiResponse(responseCode = "200", description = "Successfully retrieved tags")})
  @Parameter(
      name = "tagType",
      description = "Filter tags by type. If not specified, all tags will be returned.",
      required = false,
      example = "USER",
      schema = @Schema(implementation = TagType.class))
  public ResponseEntity<TagFindResponse> findAllTags(
      @AuthenticationPrincipal User user,
      @RequestParam(value = "tagType", required = false) TagType tagType,
      @RequestParam(value = "projectId", required = false) String projectId,
      @RequestParam(value = "modelId", required = false) String modelId,
      @RequestParam(value = "type", required = false) MonitoringType type) {

    return ResponseEntity.ok(tagService.findTags(user, tagType, projectId, modelId, type));
  }

  @PostMapping("")
  @Operation(
      summary = "Create a user tag",
      description = "Creates a new user tag associated with the authenticated user.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Successfully created user tag"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Tag already exists"),
        @ApiResponse(responseCode = "429", description = "Tag limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content = @Content(mediaType = "application/json")))
  public ResponseEntity<TagDTO> createUserTag(
      @Valid @RequestBody UserTagCreateRequest createRequest, @AuthenticationPrincipal User user) {
    TagDTO tag = tagService.createUserTag(createRequest.getName(), createRequest.getColor(), user);

    URI createdLocation = URI.create("/tags/" + tag.getId());
    return ResponseEntity.created(createdLocation).body(tag);
  }

  @GetMapping("/{tagId}")
  @Operation(
      summary = "Get tag by ID",
      description = "Retrieves a tag by its ID.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tag"),
        @ApiResponse(responseCode = "403", description = "Unauthorzied"),
        @ApiResponse(responseCode = "404", description = "Tag not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      },
      parameters = {
        @Parameter(name = "tagId", description = "Tag ID", example = "tag-uuid4", required = true)
      })
  public ResponseEntity<TagDTO> getTagById(
      @PathVariable("tagId") String tagId, @AuthenticationPrincipal User user) {
    Optional<Tag> tag = tagService.findByTagIdAndUser(tagId, user);
    if (!tag.isPresent()) throw new NotFoundException("Requested tag does not exist for this user");

    return ResponseEntity.ok(new TagDTO(tag.get()));
  }

  @PatchMapping("/{tagId}")
  @Operation(
      summary = "Update tag by ID",
      description = "Updates a tag by its ID.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Tag successfully updated"),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request data, at least 1 nonnull field required"),
        @ApiResponse(responseCode = "403", description = "Unauthorzied"),
        @ApiResponse(
            responseCode = "409",
            description = "Tag with the requested name already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      },
      parameters = {@Parameter(name = "tagId", description = "Tag ID", example = "tag-uuid4")},
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content = @Content(mediaType = "application/json")))
  public ResponseEntity<TagDTO> updateTagById(
      @PathVariable("tagId") String tagId,
      @AuthenticationPrincipal User user,
      @RequestBody TagUpdateRequest updateRequest) {
    TagDTO updatedTag =
        tagService.updateUserTag(tagId, user, updateRequest.getName(), updateRequest.getColor());
    return ResponseEntity.ok(updatedTag);
  }

  @DeleteMapping("/{tagNameOrId}")
  @Operation(
      summary = "Delete tag by tag name or tag id",
      description = "Deletes a tag by its name or ID associated with the authenticated user.",
      responses = {@ApiResponse(responseCode = "200", description = "Successfully deleted tag")},
      parameters = {
        @Parameter(name = "tagNameOrId", description = "Tag name or ID", example = "myTag")
      })
  public ResponseEntity<Status200Response> deleteByTagNameAndUser(
      @PathVariable("tagNameOrId") String tagNameOrId, @AuthenticationPrincipal User user) {
    Long deletedCount = tagService.deleteByTagNameOrTagIdAndUser(tagNameOrId, user);
    return ResponseEntity.ok(
        Status200Response.builder()
            .message(deletedCount + " tag is successfully deleted.")
            .build());
  }
}

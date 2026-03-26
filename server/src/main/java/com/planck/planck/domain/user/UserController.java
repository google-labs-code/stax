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

package com.planck.planck.domain.user;

import com.planck.planck.entitities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/user"})
@RequiredArgsConstructor
@Tag(name = "User API")
public class UserController {

  @Autowired private UserDeletionServiceImpl userDeletionServiceImpl;

  @Value("${auth.enabled}")
  public Boolean authEnabled;

  @Operation(
      summary = "Delete current user and all related data",
      description =
          "Deletes the authenticated user and all associated data, such as projects, datasets, models, etc.",
      responses = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error – unexpected failure during deletion",
            content = @Content(mediaType = "application/json"))
      })
  @DeleteMapping("/delete-user-data")
  public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal User user) {
    if (authEnabled) {
      userDeletionServiceImpl.deleteUserAndAllData(user);
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/get-stale-deletion-count")
  public ResponseEntity<Integer> getStaleDeletionCount() {
    if (authEnabled) {
      Integer count = userDeletionServiceImpl.getStaleUserDeletionCount();
      return ResponseEntity.ok(count);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/retry-stale-deletions")
  public ResponseEntity<Void> retryStaleDeletions() {
    if (authEnabled) {
      userDeletionServiceImpl.retryStaleUserDeletions();
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}

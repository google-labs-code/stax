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

package com.planck.planck.domain.tos;

import com.planck.planck.domain.authentication.dto.JwtAuthenticationResponse;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.entitities.Tos;
import com.planck.planck.entitities.User;
import com.planck.planck.exceptions.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tos")
@Tag(name = "Terms of Service API")
public class TosController {

  private final TosService tosService;

  private final UserRepository userRepository;

  public TosController(TosService tosService, UserRepository userRepository) {
    this.tosService = tosService;
    this.userRepository = userRepository;
  }

  @GetMapping
  @Operation(
      summary = "Get Active TOS",
      description = "Returns a TOS if presented.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Tos.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<?> getTosPage() {

    Optional<Tos> latestTos = tosService.getActiveTos();
    if (latestTos.isPresent()) {
      return ResponseEntity.ok(latestTos.get());
    } else {
      return ResponseEntity.ok("No Terms of service available");
    }
  }

  @PostMapping("/accept")
  @Operation(
      summary = "Accept TOS",
      description = "Accept TOS and return JwtAuthenticationResponse",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Tos.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<JwtAuthenticationResponse> acceptTos(@RequestParam String tosId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Optional<User> userOptional = userRepository.findByEmail(username);
    if (!userOptional.isPresent()) {
      throw new UserNotFoundException();
    }
    return ResponseEntity.ok(tosService.acceptTos(userOptional.get(), tosId));
  }
}

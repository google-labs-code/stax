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

package com.planck.planck.domain.gcs;

import com.planck.planck.domain.gcs.dto.GCSSignedUrlRequestDTO;
import com.planck.planck.domain.gcs.dto.GCSSignedUrlResponseDTO;
import com.planck.planck.entitities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/datasets")
public class GCSSignedUrlController {

  @Autowired private GCSSignedUrlService gcsSignedUrlService;

  @PostMapping("/uploadUrl")
  public ResponseEntity<GCSSignedUrlResponseDTO> generateSignedUrl(
      @RequestBody GCSSignedUrlRequestDTO requestDTO, @AuthenticationPrincipal User user) {

    GCSSignedUrlResponseDTO responseDTO =
        gcsSignedUrlService.generateSignedUrl(
            requestDTO.getObjectName(),
            user,
            requestDTO.getInputColumn(),
            requestDTO.getOutputColumn(),
            requestDTO.getExpectedOutputColumn());

    if (responseDTO.getError() != null) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
    }

    return ResponseEntity.ok(responseDTO);
  }
}

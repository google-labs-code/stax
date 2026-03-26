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

package com.planck.planck.domain.modelresponse;

import com.planck.planck.domain.modelresponse.service.ModelResponseService;
import com.planck.planck.entitities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("model-responses")
@Validated
@Slf4j
public class ModelResponseController {

  @Autowired private ModelResponseService modelResponseService;

  @GetMapping("/{modelResponseId}")
  public ResponseEntity<ModelResponseDTO> getModelInput(
      @AuthenticationPrincipal User user, @PathVariable String modelResponseId) {
    ModelResponseDTO modelInput = modelResponseService.getModelResponse(user, modelResponseId);
    return ResponseEntity.ok(modelInput);
  }
}

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

package com.planck.planck.domain.evaluation;

import com.planck.planck.domain.evaluation.dto.EvaluationBulkRequest;
import com.planck.planck.domain.evaluation.dto.ScorerResponseDTO;
import com.planck.planck.domain.evaluation.serivce.EvaluationService;
import com.planck.planck.entitities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Evaluation", description = "APIs related to evaluation")
@RestController
@RequestMapping("/evaluations/dataset/{dataset_id}")
public class DatasetEvaluationController {

  @Autowired private EvaluationService evaluationService;

  @Operation(
      summary = "Create chat turns evaluation for all chat turns contained in a dataset",
      description = "Creates an evaluation for all chat turns contained in a dataset")
  @ApiResponse(responseCode = "200", description = "Evaluation created successfully")
  @ApiResponse(responseCode = "400", description = "Invalid input")
  @ApiResponse(responseCode = "403", description = "Forbidden")
  @PostMapping()
  @Valid
  public ResponseEntity<ScorerResponseDTO> createFullDataSetEvaluation(
      @RequestBody EvaluationBulkRequest request,
      @PathVariable("dataset_id") String dataSetId,
      @AuthenticationPrincipal User user) {
    ScorerResponseDTO scorerResponseDTO =
        evaluationService.generateScoresFromDataSet(dataSetId, request.getEvaluatorIds(), user);
    return ResponseEntity.ok(scorerResponseDTO);
  }
}

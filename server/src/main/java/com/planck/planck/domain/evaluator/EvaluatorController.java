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

package com.planck.planck.domain.evaluator;

import com.planck.planck.domain.evaluator.dto.AllEvaluatorsResponseDTO;
import com.planck.planck.domain.evaluator.heuristic.dto.PointwiseHeuristicEvaluatorResponseDTO;
import com.planck.planck.domain.evaluator.heuristic.service.PointwiseHeuristicEvaluatorService;
import com.planck.planck.domain.evaluator.llm.dto.LLMEvaluatorResponseDTO;
import com.planck.planck.domain.evaluator.llm.service.NewLLMEvaluatorService;
import com.planck.planck.domain.evaluator.pairwise.service.PairwiseLLMEvaluatorService;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/evaluator"})
@AllArgsConstructor
@Tag(name = "Evaluator API", description = "API for managing All evaluators")
public class EvaluatorController {

  private final NewLLMEvaluatorService llmEvaluatorService;

  private final PointwiseHeuristicEvaluatorService pointwiseHeuristicEvaluatorService;

  private final PairwiseLLMEvaluatorService pairwiseLlmEvaluatorService;

  @GetMapping("all")
  @Operation(
      summary = "Get all evaluators",
      description =
          "Retrieves a list of all evaluators. Use the 'evaluation_type' query parameter to filter results by POINTWISE or SXS (Side-by-Side) types. If not specified, all evaluator types are returned.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved evaluators"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
      })
  public ResponseEntity<AllEvaluatorsResponseDTO> getAllEvaluators(
      @RequestParam(name = "evaluation_type", required = false) String evaluationType,
      @AuthenticationPrincipal User user) {

    List<LLMEvaluatorResponseDTO> llmEvaluators = new ArrayList<>();
    List<PointwiseHeuristicEvaluatorResponseDTO> heuristicEvaluators =
        pointwiseHeuristicEvaluatorService.getEvaluatorsByUser(user);

    llmEvaluators.addAll(llmEvaluatorService.getEvaluatorByUser(user));
    llmEvaluators.addAll(llmEvaluatorService.getEvaluatorBySystemType());

    if (evaluationType == null || EvaluationType.SXS.name().equals(evaluationType)) {
      llmEvaluators.addAll(
          pairwiseLlmEvaluatorService.getAllPairwiseLLMEvaluators(user).stream()
              .filter(evaluator -> !evaluator.isDeprecated())
              .map(LLMEvaluatorResponseDTO::new)
              .toList());
    }

    AllEvaluatorsResponseDTO allEvaluatorsResponseDTO = new AllEvaluatorsResponseDTO();
    allEvaluatorsResponseDTO.setLlmEvaluators(llmEvaluators);
    allEvaluatorsResponseDTO.setHeuristicEvaluators(heuristicEvaluators);

    return ResponseEntity.ok(allEvaluatorsResponseDTO);
  }
}

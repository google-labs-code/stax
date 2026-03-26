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

package com.planck.planck.domain.pubsub;

import com.planck.planck.domain.inference.dto.InferenceContext;
import com.planck.planck.domain.inference.dto.InferenceDTO;
import com.planck.planck.domain.inference.service.InferencePreparationService;
import com.planck.planck.entitities.ChatTurn;
import org.springframework.stereotype.Service;

@Service
public class InferencePublisherServiceImpl implements InferencePublisherService {

  final InferencePreparationService preparationService;
  final InferencePubsubDispatcher inferenceRoutingService;

  public InferencePublisherServiceImpl(
      InferencePubsubDispatcher inferenceRoutingService,
      InferencePreparationService preparationService) {
    this.inferenceRoutingService = inferenceRoutingService;
    this.preparationService = preparationService;
  }

  @Override
  public void sendInferenceUsingExistingModel(ChatTurn chatTurn, InferenceDTO dto) {
    InferenceContext ctx = preparationService.prepareUsingExistingModel(chatTurn, dto);
    inferenceRoutingService.dispatch(ctx);
    preparationService.createInferenceStatus(dto);
  }

  @Override
  public void sendInferenceWithModel(
      ChatTurn chatTurn,
      InferenceDTO dto,
      String modelId,
      Boolean duplicateOnNoResponse,
      boolean isBulk) {
    InferenceContext ctx =
        preparationService.prepareWithExplicitModel(chatTurn, dto, modelId, duplicateOnNoResponse);
    inferenceRoutingService.dispatch(ctx, isBulk);
    preparationService.createInferenceStatus(dto);
  }
}

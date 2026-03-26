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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.planck.planck.domain.inference.dto.InferenceContext;
import com.planck.planck.domain.inference.dto.InferenceDTO;
import com.planck.planck.domain.inference.service.InferencePreparationService;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("InferencePublisherServiceImpl Tests")
class InferencePublisherServiceImplTest {

  @Mock private InferencePreparationService preparationService;
  @Mock private InferencePubsubDispatcher inferenceRoutingService;

  @InjectMocks private InferencePublisherServiceImpl publisherService;

  private ChatTurn chatTurn;
  private InferenceDTO dto;
  private InferenceContext inferenceContext;

  @BeforeEach
  void setUp() {
    chatTurn = new ChatTurn();
    dto = new InferenceDTO();
    Model model = new Model();
    inferenceContext = new InferenceContext(model, "{}");
  }

  @Nested
  @DisplayName("Tests for sendInferenceUsingExistingModel")
  class SendInferenceUsingExistingModelTests {

    @Test
    @DisplayName("Should prepare, dispatch, and create status in correct order")
    void shouldPrepareDispatchAndCreateStatusInOrder() {
      when(preparationService.prepareUsingExistingModel(chatTurn, dto))
          .thenReturn(inferenceContext);

      publisherService.sendInferenceUsingExistingModel(chatTurn, dto);

      InOrder inOrder = Mockito.inOrder(preparationService, inferenceRoutingService);

      inOrder.verify(preparationService).prepareUsingExistingModel(chatTurn, dto);

      inOrder.verify(inferenceRoutingService).dispatch(inferenceContext);

      verify(preparationService).createInferenceStatus(dto);
    }
  }

  @Nested
  @DisplayName("Tests for sendInferenceWithModel")
  class SendInferenceWithModelTests {

    private final String modelId = "test-model-id";
    private final boolean duplicateOnNoResponse = true;

    @Test
    @DisplayName("Should call dispatch with isBulk=false when specified")
    void shouldCallMainMethodWithBulkFalse() {
      boolean isBulk = false;
      when(preparationService.prepareWithExplicitModel(
              chatTurn, dto, modelId, duplicateOnNoResponse))
          .thenReturn(inferenceContext);

      publisherService.sendInferenceWithModel(
          chatTurn, dto, modelId, duplicateOnNoResponse, isBulk);

      // Assert
      InOrder inOrder = Mockito.inOrder(preparationService, inferenceRoutingService);

      inOrder
          .verify(preparationService)
          .prepareWithExplicitModel(chatTurn, dto, modelId, duplicateOnNoResponse);

      inOrder.verify(inferenceRoutingService).dispatch(inferenceContext, false);

      verify(preparationService).createInferenceStatus(dto);
    }

    @Test
    @DisplayName("Should call dispatch with isBulk=true when specified")
    void shouldCallDispatchWithBulkTrue() {
      // Arrange
      boolean isBulk = true;
      when(preparationService.prepareWithExplicitModel(
              chatTurn, dto, modelId, duplicateOnNoResponse))
          .thenReturn(inferenceContext);

      // Act
      publisherService.sendInferenceWithModel(
          chatTurn, dto, modelId, duplicateOnNoResponse, isBulk);

      // Assert
      InOrder inOrder = Mockito.inOrder(preparationService, inferenceRoutingService);

      inOrder
          .verify(preparationService)
          .prepareWithExplicitModel(chatTurn, dto, modelId, duplicateOnNoResponse);

      inOrder.verify(inferenceRoutingService).dispatch(inferenceContext, true);

      verify(preparationService).createInferenceStatus(dto);
    }
  }
}

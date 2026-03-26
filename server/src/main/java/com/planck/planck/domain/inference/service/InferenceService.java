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

package com.planck.planck.domain.inference.service;

import com.planck.planck.annotation.CheckJobActive;
import com.planck.planck.domain.chatturn.dto.ChatTurnDTO;
import com.planck.planck.domain.inference.dto.BulkInferenceResponseDTO;
import com.planck.planck.domain.inference.dto.InferenceDTO;
import com.planck.planck.entitities.ChatTurn;
import com.planck.planck.entitities.Model;
import com.planck.planck.entitities.ModelInput;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.llmproviders.dto.ChatResponseWithLatency;
import com.planck.planck.llmproviders.dto.Prompt;
import java.util.List;
import java.util.Map;

public interface InferenceService {
  ChatTurnDTO addModelResponseToChatturn(
      User user,
      Project project,
      String modelId,
      List<ChatTurn> previousChatTurns,
      ChatTurn chatTurn);

  ChatTurnDTO runInference(
      User user,
      String projectId,
      String modelId,
      List<Prompt> prompts,
      List<ChatTurn> previousChatTurns,
      Map<String, String> variables);

  BulkInferenceResponseDTO runReinferenceForProject(
      User user, String projectId, List<String> modelIds);

  BulkInferenceResponseDTO runReinference(
      User user, String projectId, List<String> chatTurnIds, List<String> modelIds);

  boolean reRunInferenceAndUpdateResponse(InferenceDTO dto);

  @CheckJobActive
  void process(InferenceDTO inferenceDTO);

  ChatResponseWithLatency runEvaluationInference(
      User user, Model model, List<ModelInput> modelInputs);
}

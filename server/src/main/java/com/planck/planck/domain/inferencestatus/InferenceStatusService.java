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

package com.planck.planck.domain.inferencestatus;

import com.planck.planck.domain.inference.dto.InferenceDTO;
import com.planck.planck.entitities.InferenceStatus;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.InferenceStatusEnum;
import java.util.Map;

public interface InferenceStatusService {
  InferenceStatus createOrUpdateInferenceStatus(
      InferenceDTO inferenceDTO, InferenceStatusEnum inferenceStatusEnum);

  void updateInferenceStatus(
      InferenceDTO inferenceDTO, InferenceStatusEnum inferenceStatusEnum, String comment);

  int updateInferenceStatus(
      InferenceStatusEnum newInferenceStatus,
      String jobId,
      String comment,
      InferenceStatusEnum oldInferenceStatus);

  Map<String, Integer> findAllByUserAndJobId(String jobId, String userId);

  InferenceStatus findByChatTurnIdAndUser(String chatTurnId, String userId);

  void stopInferenceStatusByChatTurnIdAndProjectId(String chatTurnId, String projectId, User user);
}

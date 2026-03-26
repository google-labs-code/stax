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

package com.planck.planck.domain.project;

import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface EvaluationContainerService {

  <T extends EvaluationContainer> T getContainerForUser(
      User user, String containerId, Class<T> type);

  EvaluationContainer getContainerForUser(User user, String containerId);

  void deleteContainer(User user, String containerId);

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  void deleteContainer(EvaluationContainer container);
}

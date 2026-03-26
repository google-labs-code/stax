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

package com.planck.planck.config;

import com.planck.planck.entitities.DataSet;
import com.planck.planck.entitities.EvaluationContainer;
import com.planck.planck.entitities.Project;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContainerLimitProvider {

  private final ApplicationLimits appLimits;

  public int getMaxChatsLimit(EvaluationContainer container) {
    Object realContainer = Hibernate.unproxy(container);

    if (realContainer instanceof Project) {
      return appLimits.getMaxChatsPerProject();
    } else if (realContainer instanceof DataSet) {
      return appLimits.getMaxChatsPerDataSet();
    }

    throw new UnsupportedOperationException(
        "No chat limit defined for container type: " + realContainer.getClass().getName());
  }
}

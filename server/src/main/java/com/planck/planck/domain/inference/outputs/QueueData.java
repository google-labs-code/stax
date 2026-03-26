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

package com.planck.planck.domain.inference.outputs;

import com.planck.planck.entitities.ChatTurn;
import java.util.List;
import java.util.Map;
import lombok.Value;

@Value
public class QueueData {
  private final List<ChatTurn> chatTurnsForNewModel;
  private final List<ChatTurn> chatTurnsWithExistingModel;
  private final Map<String, String> turnToModelId;

  public int getTotalCount() {
    return chatTurnsForNewModel.size() + chatTurnsWithExistingModel.size();
  }

  public boolean hasChatTurns() {
    return getTotalCount() > 0;
  }
}

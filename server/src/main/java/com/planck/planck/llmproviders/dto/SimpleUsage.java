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

package com.planck.planck.llmproviders.dto;

public class SimpleUsage {
  private final int promptTokens;
  private final int completionTokens;
  private final int totalTokens;

  public SimpleUsage(int promptTokens, int completionTokens, int totalTokens) {
    this.promptTokens = promptTokens;
    this.completionTokens = completionTokens;
    this.totalTokens = totalTokens;
  }

  public Integer getPromptTokens() {
    return promptTokens;
  }

  public Integer getCompletionTokens() {
    return completionTokens;
  }

  public Integer getTotalTokens() {
    return totalTokens;
  }
}

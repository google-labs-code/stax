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

package com.planck.planck.domain.apikeys.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiKeysResponseDTO {
  private boolean isOpenaiKeyPresent;
  private boolean isMistralKeyPresent;
  private boolean isGoogleKeyPresent;
  private boolean isAnthropicKeyPresent;
  private boolean isGrokKeyPresent;
  private boolean isOllamaKeyPresent;
  private boolean isDeepseekKeyPresent;
  private boolean isHuggingfaceKeyPresent;
  private boolean isLlamaKeyPresent;

  public ApiKeysResponseDTO() {
    this.isOpenaiKeyPresent = false;
    this.isMistralKeyPresent = false;
    this.isGoogleKeyPresent = false;
    this.isAnthropicKeyPresent = false;
    this.isGrokKeyPresent = false;
    this.isOllamaKeyPresent = false;
    this.isDeepseekKeyPresent = false;
    this.isHuggingfaceKeyPresent = false;
    this.isLlamaKeyPresent = false;
  }

  public ApiKeysResponseDTO(
      boolean openaiKeyPresent,
      boolean mistralKeyPresent,
      boolean googleKeyPresent,
      boolean anthropicKeyPresent,
      boolean grokKeyPresent,
      boolean ollamaKeyPresent,
      boolean deepseekKeyPresent,
      boolean huggingfaceKeyPresent,
      boolean llamaKeyPresent) {
    this.isOpenaiKeyPresent = openaiKeyPresent;
    this.isGoogleKeyPresent = googleKeyPresent;
    this.isMistralKeyPresent = mistralKeyPresent;
    this.isAnthropicKeyPresent = anthropicKeyPresent;
    this.isGrokKeyPresent = grokKeyPresent;
    this.isOllamaKeyPresent = ollamaKeyPresent;
    this.isDeepseekKeyPresent = deepseekKeyPresent;
    this.isHuggingfaceKeyPresent = huggingfaceKeyPresent;
    this.isLlamaKeyPresent = llamaKeyPresent;
  }
}

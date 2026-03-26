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

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import java.util.List;

public class PromptMessageAdapter {
  public static ChatMessage toLangChain4jMessage(Prompt prompt) {
    return switch (prompt.getRole()) {
      case USER -> new UserMessage(prompt.getText());
      case ASSISTANT -> new AiMessage(prompt.getText());
      case SYSTEM -> new SystemMessage(prompt.getText());
      default -> throw new UnsupportedOperationException("Unsupported role: " + prompt.getRole());
    };
  }

  public static List<ChatMessage> toLangChain4jMessages(List<Prompt> prompts) {
    return prompts.stream()
        .filter(prompt -> !prompt.getText().isEmpty())
        .map(PromptMessageAdapter::toLangChain4jMessage)
        .toList();
  }
}

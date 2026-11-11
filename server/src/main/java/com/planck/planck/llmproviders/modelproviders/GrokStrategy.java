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

package com.planck.planck.llmproviders.modelproviders;

import com.planck.planck.config.DefaultRetryProperties;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("grok")
@Scope("prototype")
public class GrokStrategy extends OpenAiCommonStrategy {

  @Autowired
  GrokStrategy(DefaultRetryProperties defaultRetryProperties) {
    super(defaultRetryProperties);
  }

  @SuppressWarnings("unchecked")
  protected OpenAiChatModel buildClientWithOptions() {
    var optionsBuilder = getBuilderWithCommonOptions();

    Integer maxTokens = resolveMaxTokens();
    if (maxTokens != null) {
      optionsBuilder.maxTokens(maxTokens);
    }

    Double frequencyPenalty = resolveFrequencyPenalty();
    if (frequencyPenalty != null) {
      optionsBuilder.frequencyPenalty(frequencyPenalty);
    }

    if (this.properties.get("presence_penalty") != null)
      optionsBuilder.presencePenalty(
          Double.valueOf(this.properties.get("presence_penalty").toString()));

    if (this.properties.get("seed") != null)
      optionsBuilder.seed(Integer.valueOf(this.properties.get("seed").toString()));

    if (this.properties.get("stop_sequences") != null
        && !((List<String>) this.properties.get("stop_sequences")).isEmpty()) {
      optionsBuilder.stop((List<String>) this.properties.get("stop_sequences"));
    }

    return optionsBuilder.build();
  }

  @SuppressWarnings("unchecked")
  protected StreamingChatModel buildStreamingClientWithOptions() {
    var optionsBuilder = getStreamingBuilderWithCommonOptions();

    Integer maxTokens = resolveMaxTokens();
    if (maxTokens != null) {
      optionsBuilder.maxTokens(maxTokens);
    }

    Double frequencyPenalty = resolveFrequencyPenalty();
    if (frequencyPenalty != null) {
      optionsBuilder.frequencyPenalty(frequencyPenalty);
    }

    if (this.properties.get("presence_penalty") != null)
      optionsBuilder.presencePenalty(
          Double.valueOf(this.properties.get("presence_penalty").toString()));

    if (this.properties.get("seed") != null)
      optionsBuilder.seed(Integer.valueOf(this.properties.get("seed").toString()));

    if (this.properties.get("stop_sequences") != null
        && !((List<String>) this.properties.get("stop_sequences")).isEmpty()) {
      optionsBuilder.stop((List<String>) this.properties.get("stop_sequences"));
    }

    return optionsBuilder.build();
  }
}

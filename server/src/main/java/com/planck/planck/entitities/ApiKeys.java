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

package com.planck.planck.entitities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
public class ApiKeys {

  @Transient private String ID_PREFIX = "api-keys-";
  @Id private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID();
    }
  }

  @Schema(description = "Anthropic API Key", example = "ant-...")
  private String anthropicKey;

  @Schema(description = "OpenAI API Key", example = "open-...")
  private String openaiKey;

  @Schema(description = "Mistral API Key", example = "mis-...")
  private String mistralKey;

  @Schema(description = "Google API Key", example = "goo-...")
  private String googleKey;

  @Schema(description = "Grok API Key", example = "grk-...")
  private String grokKey;

  @Schema(description = "Ollama API Key", example = "oll-...")
  private String ollamaKey;

  @Schema(description = "DeepSeek API Key", example = "dsk-...")
  private String deepseekKey;

  @Schema(description = "HuggingFace API Key", example = "hf-...")
  private String huggingfaceKey;

  @Schema(description = "LLAMA API Key", example = "llama-...")
  private String llamaKey;

  @OneToOne
  @JoinColumn(name = "user_id")
  private User user;

  @CreationTimestamp private Timestamp createdAt;
  @UpdateTimestamp private Timestamp updatedAt;
}

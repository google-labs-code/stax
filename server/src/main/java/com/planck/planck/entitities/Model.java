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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planck.planck.enums.ModelProvider;
import com.planck.planck.enums.ModelType;
import com.planck.planck.util.converter.ModelProviderConverter;
import com.planck.planck.util.converter.ModelTypeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Table(name = "model")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Model implements Tagable {

  @Transient private String ID_PREFIX = "model-";

  @Id
  @JsonProperty("model_id")
  @Column(name = "id", nullable = false)
  private String id;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      this.id = ID_PREFIX + UUID.randomUUID().toString();
    }
  }

  @Column(name = "provider", nullable = false)
  @Convert(converter = ModelProviderConverter.class)
  private ModelProvider provider;

  @Column(name = "name", nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // Used as display name on UI.
  @Column(name = "label", nullable = false)
  private String label;

  @Column(name = "version", nullable = true)
  private String version;

  @Column(name = "url", nullable = false)
  private String url;

  @Column(name = "type", nullable = false)
  @Convert(converter = ModelTypeConverter.class)
  private ModelType type;

  @Column(name = "is_deprecated", nullable = false)
  private boolean isDeprecated = false;

  @CreationTimestamp private Timestamp createdAt;

  @UpdateTimestamp private Timestamp updatedAt;

  @Column(name = "description", nullable = true, columnDefinition = "longtext")
  private String description;

  @Column(name = "comments", nullable = true, columnDefinition = "longtext")
  private String comments;

  @Column(name = "properties", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private String properties;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "descriptors", nullable = true)
  private String descriptors;

  @Column(name = "release_date", nullable = true)
  private Timestamp releaseDate;

  @Override
  public String getEntityId() {
    return id;
  }

  // Use this function to create a system model only.
  public Model(
      ModelProvider provider,
      String label,
      String name,
      String url,
      String description,
      String properties,
      String descriptors,
      User user) {
    this.provider = provider;
    this.label = label;
    this.name = name;
    this.url = url;
    this.description = description;
    this.type = ModelType.SYSTEM;
    this.properties = properties;
    this.descriptors = descriptors;
    this.user = user;
  }

  // Use this function to create a user model only.
  public Model(Model currentModel) {
    this.description = currentModel.getDescription();
    this.name = currentModel.getName();
    this.label = currentModel.getLabel();
    this.provider = currentModel.getProvider();
    this.version = currentModel.getVersion();
    this.url = currentModel.getUrl();
    this.comments = currentModel.getComments();
    this.descriptors = currentModel.getDescriptors();
    this.isDeprecated = currentModel.isDeprecated();
    this.releaseDate = currentModel.getReleaseDate();
    this.type = ModelType.USER;
  }

  public boolean isCustomEndpoint() {
    if (this.getType() == ModelType.SYSTEM || this.url == null) return false;

    switch (this.provider) {
      case OPENAI:
        return !this.url.contains("https://api.openai.com");
      case MISTRAL:
        return !this.url.contains("https://api.mistral.ai");
      case GOOGLE:
        return !this.url.contains("https://generativelanguage.googleapis.com");
      case ANTHROPIC:
        return !this.url.contains("https://api.anthropic.com");
      case DEEPSEEK:
        return !this.url.contains("https://api.deepseek.ai");
      case GROK:
        return !this.url.contains("https://api.grok.ai");
      case LLAMA:
        return !this.url.contains("https://api.llama.com");
      default:
        return false;
    }
  }
}

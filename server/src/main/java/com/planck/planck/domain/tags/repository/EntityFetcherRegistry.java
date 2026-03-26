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

package com.planck.planck.domain.tags.repository;

import com.planck.planck.annotation.TaggableEntityFetcher;
import com.planck.planck.enums.TagLinkTargetType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EntityFetcherRegistry {
  private final Map<TagLinkTargetType, EntityFetcher<?>> fetchers = new HashMap<>();

  public EntityFetcherRegistry(List<EntityFetcher<?>> fetcherList) {
    for (EntityFetcher<?> fetcher : fetcherList) {
      TaggableEntityFetcher annotation =
          fetcher.getClass().getAnnotation(TaggableEntityFetcher.class);
      if (annotation != null) {
        fetchers.put(annotation.value(), fetcher);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T> EntityFetcher<T> getFetcher(TagLinkTargetType entityType) {
    return (EntityFetcher<T>) fetchers.get(entityType);
  }
}

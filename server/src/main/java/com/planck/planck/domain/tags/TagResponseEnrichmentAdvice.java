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

package com.planck.planck.domain.tags;

import com.planck.planck.domain.tags.dto.TagDTO;
import com.planck.planck.domain.tags.dto.Taggable;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.TagLinkTargetType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
@Slf4j
public class TagResponseEnrichmentAdvice implements ResponseBodyAdvice<Object> {

  @Autowired private TagLinkService tagLinkService;

  // Cache for which response classes actually contain taggable response DTOs
  private final Map<Class<?>, Boolean> classContainsTaggableCache = new ConcurrentHashMap<>();

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {

    if (body == null) return null;

    Object actualBody = unwrapResponseEntity(body);

    if (actualBody instanceof Collection<?> collection && !collection.isEmpty()) {
      Class<?> elementType = collection.iterator().next().getClass();
      if (!classContainsTaggableCache.computeIfAbsent(elementType, this::containsTaggable)) {
        return body;
      }
    } else {
      Class<?> bodyClass = actualBody.getClass();
      if (!classContainsTaggableCache.computeIfAbsent(bodyClass, this::containsTaggable)) {
        return body;
      }
    }

    List<Taggable> taggables = new ArrayList<>();
    collectTaggables(actualBody, taggables, new IdentityHashMap<>());

    // Normally shouldn't happen, but just in case
    if (taggables.isEmpty()) return body;

    // Group the ids based on the target type, so we can fetch the Tags
    Map<TagLinkTargetType, List<String>> idsByType =
        taggables.stream()
            .collect(
                Collectors.groupingBy(
                    Taggable::getTagLinkTargetType,
                    Collectors.mapping(Taggable::getId, Collectors.toList())));

    // Fetch the tags
    Map<String, List<TagDTO>> idToTags = new HashMap<>();
    for (Map.Entry<TagLinkTargetType, List<String>> entry : idsByType.entrySet()) {
      idToTags.putAll(
          tagLinkService.getUserTagsByTargetTypeAndEntityList(
              entry.getValue(), entry.getKey(), getCurrentUser()));
    }

    // Set the tags on each taggable response DTO object
    boolean hasTags = false;
    for (Taggable taggable : taggables) {
      List<TagDTO> tags = idToTags.getOrDefault(taggable.getId(), List.of());
      taggable.setTags(tags);
      if (!tags.isEmpty()) {
        hasTags = true;
      }
    }

    updateEmptyColumns(actualBody, hasTags);

    return body;
  }

  @SuppressWarnings("unchecked")
  private void updateEmptyColumns(Object actualBody, boolean hasTags) {
    if (hasTags) {
      try {
        Field emptyColumnsField = actualBody.getClass().getDeclaredField("emptyColumns");
        if (emptyColumnsField != null && List.class.isAssignableFrom(emptyColumnsField.getType())) {
          emptyColumnsField.setAccessible(true);
          List<String> emptyColumns = (List<String>) emptyColumnsField.get(actualBody);
          if (emptyColumns != null) {
            emptyColumns.remove("tags");
          }
        }
      } catch (NoSuchFieldException | IllegalAccessException ignored) {
        log.warn("Could not update emptyColumns field on response body: ", ignored);
      }
    }
  }

  private Object unwrapResponseEntity(Object body) {
    if (body instanceof ResponseEntity<?> responseEntity) {
      return responseEntity.getBody() != null ? responseEntity.getBody() : body;
    }
    return body;
  }

  // Recursive check via reflection, whether the response class contains any Dto which implements
  // Taggable
  private boolean containsTaggable(Class<?> clazz) {
    if (clazz == null || clazz == Object.class) return false;

    if (Taggable.class.isAssignableFrom(clazz)) return true;

    if (clazz.isPrimitive() || clazz.getName().startsWith("java.") || clazz.isEnum()) {
      return false;
    }

    for (Field field : findAllFields(clazz)) {
      field.setAccessible(true);
      Class<?> fieldType = field.getType();

      if (Taggable.class.isAssignableFrom(fieldType)) return true;

      if (Collection.class.isAssignableFrom(fieldType)) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType pt) {
          Type arg = pt.getActualTypeArguments()[0];
          if (arg instanceof Class<?> argClass
              && (Taggable.class.isAssignableFrom(argClass) || containsTaggable(argClass))) {
            return true;
          }
        }
      }

      if (containsTaggable(fieldType)) return true;
    }

    return false;
  }

  // Recursively traverse the response instance and collect all the objects which implement Taggable
  private void collectTaggables(
      Object obj, List<Taggable> taggables, Map<Object, Boolean> visited) {
    if (obj == null || visited.containsKey(obj)) return;
    visited.put(obj, Boolean.TRUE);

    if (obj instanceof Taggable taggable) {
      taggables.add(taggable);
      return;
    }

    if (obj instanceof Collection<?> collection) {
      for (Object item : collection) {
        collectTaggables(item, taggables, visited);
      }
      return;
    }

    Class<?> clazz = obj.getClass();
    if (clazz.isPrimitive() || clazz.getName().startsWith("java.") || clazz.isEnum()) return;

    for (Field field : findAllFields(clazz)) {
      field.setAccessible(true);
      try {
        Object value = field.get(obj);
        collectTaggables(value, taggables, visited);
      } catch (IllegalAccessException ignored) {
      }
    }
  }

  private List<Field> findAllFields(Class<?> type) {
    List<Field> fields = new ArrayList<>();
    while (type != null && type != Object.class) {
      fields.addAll(Arrays.asList(type.getDeclaredFields()));
      type = type.getSuperclass();
    }
    return fields;
  }

  private User getCurrentUser() {
    return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}

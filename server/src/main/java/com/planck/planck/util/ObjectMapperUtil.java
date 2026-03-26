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

package com.planck.planck.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

// Note: at some point would be nice to refactor this into a component
// so we can properly inject it where needed and override in Integration Tests
@Slf4j
public class ObjectMapperUtil {

  private ObjectMapperUtil() {}

  private static ObjectMapper instance;
  private static ObjectMapper instanceNonNull;

  public static ObjectMapper getObectMapperInstance() {

    if (instance == null) {
      instance = new ObjectMapper();
      instance.registerModule(new JavaTimeModule());
      instance.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      instance.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      instance.setDateFormat(dateFormat);
    }
    return instance;
  }

  public static ObjectMapper getObectMapperInstanceForNonNull() {

    if (instanceNonNull == null) {
      instanceNonNull = new ObjectMapper();
      instanceNonNull.registerModule(new JavaTimeModule());
      instanceNonNull.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      instanceNonNull.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      instanceNonNull.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    return instanceNonNull;
  }

  public static String toJsonString(Object obj) {

    ObjectMapper mapper = getObectMapperInstance();
    try {
      return mapper.writeValueAsString(obj);
    } catch (Exception e) {

    }
    return null;
  }

  public static String toJsonStringNonNull(Object obj) {

    ObjectMapper mapper = getObectMapperInstanceForNonNull();
    try {
      return mapper.writeValueAsString(obj);
    } catch (Exception e) {

    }
    return null;
  }

  public static String convertMapToJsonString(Map<String, Object> map) {
    ObjectMapper mapper = getObectMapperInstance();
    try {
      return mapper.writeValueAsString(map);
    } catch (Exception e) {
      log.error("Error converting map to JSON string", e);
      return null;
    }
  }

  public static <T, F> T convertObject(F f, Class<T> clazz) {

    Objects.requireNonNull(clazz);
    ObjectMapper mapper = getObectMapperInstance();

    return mapper.convertValue(f, clazz);
  }

  public static <T> T convertStringToObject(String jsonString, Class<T> clazz) {

    Objects.requireNonNull(clazz);
    Objects.requireNonNull(jsonString);

    ObjectMapper mapper = getObectMapperInstance();
    try {
      return mapper.readValue(jsonString, clazz);
    } catch (Exception e) {
      log.error("Error converting JSON string to object", e);
    }
    return null;
  }

  public static <T> List<T> convertJsonStringToList(String jsonString, Class<T> clazz) {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      return new ArrayList<>();
    }

    String cleanedJsonString = unescapeDoubleEncodedJson(jsonString);

    try {
      return getObectMapperInstance()
          .readValue(
              cleanedJsonString,
              getObectMapperInstance().getTypeFactory().constructCollectionType(List.class, clazz));
    } catch (Exception e) {
      throw new RuntimeException("Error converting JSON string to list", e);
    }
  }

  public static Map<String, Object> convertJsonStringToMap(String jsonString) {
    if (jsonString == null || jsonString.isEmpty()) {
      return Collections.emptyMap();
    }

    String cleanedJsonString = unescapeDoubleEncodedJson(jsonString);

    ObjectMapper mapper = getObectMapperInstance();
    try {
      return mapper.readValue(cleanedJsonString, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      log.error("Error converting JSON string to map", e);
      return Collections.emptyMap();
    }
  }

  public static <T> Set<T> convertJsonStringToSet(String jsonString, Class<T> clazz) {
    try {
      ObjectMapper objectMapper = getObectMapperInstance();
      if (StringUtils.hasText(jsonString)) {
        return objectMapper.readValue(
            jsonString, objectMapper.getTypeFactory().constructCollectionType(Set.class, clazz));
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return Collections.emptySet();
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> jsonToMap(String jsonString) {
    ObjectMapper mapper = getObectMapperInstance();
    try {
      return mapper.readValue(jsonString, Map.class);
    } catch (Exception e) {
      log.error("Error converting JSON string to map", e);
      return Collections.emptyMap();
    }
  }

  @SuppressWarnings("unchecked")
  public static Map<String, String> jsonToMapString(String jsonString) {
    ObjectMapper mapper = getObectMapperInstance();
    try {
      return mapper.readValue(jsonString, Map.class);
    } catch (Exception e) {
      log.error("Error converting JSON string to map", e);
      return Collections.emptyMap();
    }
  }

  @SuppressWarnings("unchecked")
  public static List<Map<String, Object>> jsonToListOfMap(String jsonString) {
    ObjectMapper mapper = getObectMapperInstance();
    try {
      return mapper.readValue(jsonString, List.class);
    } catch (Exception e) {
      log.error("Error converting JSON string to list of maps", e);
      return Collections.emptyList();
    }
  }

  public static <T> Map<String, T> jsonToMap(
      String jsonString, TypeReference<Map<String, T>> typeReference) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(jsonString, typeReference);
    } catch (Exception e) {
      log.error("Error converting JSON string to map", e);
      return Collections.emptyMap();
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> convetJsonStringToList(String jsonString) {
    List<T> list = new ArrayList<>();
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      JsonNode rootNode = objectMapper.readTree(jsonString);
      if (rootNode.isArray()) {
        for (JsonNode node : rootNode) {
          list.add((T) node);
        }
      } else {
        list.add((T) rootNode);
      }
    } catch (JsonProcessingException e) {
      log.error("Error converting JSON string to list", e);
      return Collections.emptyList();
    }
    return list;
  }

  public static void isValidJSON(String jsonString) {

    if (jsonString == null) {
      return;
    }

    ObjectMapper mapper = getObectMapperInstance();
    try {
      mapper.readTree(jsonString);
    } catch (Exception e) {
      log.error("Error validating JSON string", e);
      throw new IllegalArgumentException("Invalid JSON String");
    }
  }

  public static <T> T convertJsonStringToObject(String jsonString, Class<T> clazz) {
    try {
      return getObectMapperInstance().readValue(jsonString, clazz);
    } catch (Exception e) {
      throw new RuntimeException("Error converting JSON string to object", e);
    }
  }

  public static String convertObjectToJsonString(Object object) {
    try {
      return getObectMapperInstance().writeValueAsString(object);
    } catch (Exception e) {
      throw new RuntimeException("Error converting object to JSON string", e);
    }
  }

  /**
   * Handles double-encoded JSON strings that can occur with H2 CSV loading. If the string is
   * wrapped in quotes and contains escaped content, it attempts to unescape it. This provides
   * compatibility between H2 (which double-encodes CSV data) and MySQL (which doesn't).
   *
   * @param jsonString The potentially double-encoded JSON string
   * @return The cleaned/unescaped JSON string
   */
  private static String unescapeDoubleEncodedJson(String jsonString) {
    if (jsonString == null || !jsonString.startsWith("\"") || !jsonString.endsWith("\"")) {
      return jsonString;
    }

    try {
      // Try to parse it as a JSON string first (which will unescape it)
      return getObectMapperInstance().readValue(jsonString, String.class);
    } catch (Exception ignored) {
      // If that fails, use the original string
      return jsonString;
    }
  }
}

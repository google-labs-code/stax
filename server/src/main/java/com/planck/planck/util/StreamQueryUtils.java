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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for filtering, sorting, and paginating collections based on fields mapped via
 * {@link JsonProperty} annotations.
 *
 * <p>Currently not used for `Workbook`, but retained for future use with JSON-annotated data
 * structures.
 */
public class StreamQueryUtils {

  private static final Logger logger = LoggerFactory.getLogger(StreamQueryUtils.class);

  /**
   * Filters, sorts, and paginates a list of items based on provided conditions and order rules.
   *
   * @param items the full list of items to process
   * @param filters the list of filtering conditions
   * @param orderByList the list of sorting instructions
   * @param page the page number (zero-based)
   * @param size the number of items per page
   * @param <T> the type of the items
   * @return a {@link PageResult} containing paginated items and the total filtered count
   */
  public static <T> PageResult<T> filterSortPaginateWithTotal(
      List<T> items, List<FilterCondition> filters, List<OrderBy> orderByList, int page, int size) {

    if (items.isEmpty()) {
      return new PageResult<>(List.of(), 0);
    }
    Class<?> clazz = items.get(0).getClass();

    Map<String, Field> jsonPropertyToFieldMap = buildJsonPropertyToFieldMap(clazz);

    List<T> filtered =
        items.stream()
            .filter(item -> applyFilters(item, filters, jsonPropertyToFieldMap))
            .sorted(buildComparator(orderByList, jsonPropertyToFieldMap))
            .toList();

    int total = filtered.size();

    List<T> paginated = filtered.stream().skip((long) page * size).limit(size).toList();

    return new PageResult<>(paginated, total);
  }

  /**
   * Applies a list of filters to an item using a map of JSON property names to fields.
   *
   * @param item the item to test
   * @param filters the list of filter conditions
   * @param jsonPropertyToFieldMap a mapping of JSON property names to corresponding class fields
   * @param <T> the type of the item
   * @return {@code true} if the item satisfies all filters; {@code false} otherwise
   */
  private static <T> boolean applyFilters(
      T item, List<FilterCondition> filters, Map<String, Field> jsonPropertyToFieldMap) {
    for (FilterCondition filter : filters) {
      String fieldName = filter.field();
      Field field = jsonPropertyToFieldMap.get(fieldName);

      if (field == null) {
        continue; // skip if no field found
      }

      try {
        field.setAccessible(true);
        Object fieldValue = field.get(item);
        if (!evaluateCondition(fieldValue, filter.op(), filter.value())) {
          return false;
        }
      } catch (Exception e) {
        logger.error("Error during filter evaluation", e);
        return false;
      }
    }
    return true;
  }

  /**
   * Evaluates a condition based on the provided actual value, operator, and filter value.
   *
   * @param actual The actual value to compare.
   * @param operator The operator to use for comparison (e.g., "=", "!=", ">", "<", ":").
   * @param filterValue The value to compare against.
   * @return true if the condition is met, false otherwise.
   */
  protected static boolean evaluateCondition(Object actual, String operator, String filterValue) {
    if (actual == null) return false;

    Comparable left = coerceToComparable(actual);
    Comparable right = coerceToComparable(filterValue, actual.getClass());

    return switch (operator) {
      case "=" -> left.compareTo(right) == 0;
      case "!=" -> left.compareTo(right) != 0;
      case ">" -> left.compareTo(right) > 0;
      case ">=" -> left.compareTo(right) >= 0;
      case "<" -> left.compareTo(right) < 0;
      case "<=" -> left.compareTo(right) <= 0;
      case ":" -> {
        // .contains(n) can be applied only with String
        if (left instanceof String leftStr && right instanceof String rightStr) {
          yield leftStr.toLowerCase().contains(rightStr.toLowerCase());
        }
        yield false;
      }
      default -> false;
    };
  }

  /**
   * Converts a value to a {@link Comparable} for comparison operations.
   *
   * @param value the value to convert
   * @return a comparable form of the value
   */
  protected static Comparable coerceToComparable(Object value) {
    if (value instanceof String s) return s;
    if (value instanceof Number n) return ((Number) value).doubleValue();
    if (value instanceof Boolean b) return b;
    if (value instanceof Comparable c) return c;
    return value.toString();
  }

  /**
   * Converts a string to a {@link Comparable} value of the target type.
   *
   * @param raw the raw string value
   * @param targetType the expected target type
   * @return a comparable version of the string coerced into the target type
   */
  protected static Comparable coerceToComparable(String raw, Class<?> targetType) {
    try {
      if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(raw);
      if (targetType == Long.class || targetType == long.class) return Long.parseLong(raw);
      if (targetType == Double.class || targetType == double.class) return Double.parseDouble(raw);
      if (targetType == Boolean.class || targetType == boolean.class)
        return Boolean.parseBoolean(raw);
      if (targetType == String.class) return raw;
      if (targetType == LocalDateTime.class) return LocalDateTime.parse(raw);
      // todo add formats if needed
      if (targetType == Date.class) return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(raw);
    } catch (Exception ignored) {
    }
    return raw;
  }

  /**
   * Builds a comparator for sorting items based on a list of {@link OrderBy} conditions.
   *
   * @param orderByList the list of sorting conditions
   * @param jsonPropertyToFieldMap a mapping of JSON property names to corresponding class fields
   * @param <T> the type of the items to compare
   * @return a combined comparator for sorting the items
   */
  private static <T> Comparator<T> buildComparator(
      List<OrderBy> orderByList, Map<String, Field> jsonPropertyToFieldMap) {
    Comparator<T> combined = Comparator.comparing(x -> 0);

    for (OrderBy order : orderByList) {
      String fieldName = order.field();
      Field field = jsonPropertyToFieldMap.get(fieldName); // get field from jsonPropertyToFieldMap

      if (field == null) {
        continue; // skip if not found
      }

      Comparator<T> comp =
          Comparator.comparing(
              x -> {
                try {
                  field.setAccessible(true);
                  Object value = field.get(x);
                  return (Comparable) value;
                } catch (Exception e) {
                  logger.error("Error during sort evaluation", e);
                  return null;
                }
              },
              Comparator.nullsLast(Comparator.naturalOrder()));

      if (order.desc()) {
        comp = comp.reversed();
      }

      combined = combined.thenComparing(comp);
    }

    return combined;
  }

  /**
   * Builds a map that links JSON property names (via {@link JsonProperty}) to class fields.
   *
   * @param clazz the class to scan for annotated fields
   * @return a map from JSON property names to corresponding {@link Field} objects
   */
  private static Map<String, Field> buildJsonPropertyToFieldMap(Class<?> clazz) {

    Map<String, Field> jsonPropertyToFieldMap = new HashMap<>();
    for (Field field : clazz.getDeclaredFields()) {
      JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
      if (jsonProperty != null) {
        jsonPropertyToFieldMap.put(jsonProperty.value(), field);
      } else {
        // if there is no annotation - use the field directly
        jsonPropertyToFieldMap.put(field.getName(), field);
      }
    }
    return jsonPropertyToFieldMap;
  }
}

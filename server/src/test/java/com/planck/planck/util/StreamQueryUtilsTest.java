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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class StreamQueryUtilsTest {

  static class Dummy {
    @JsonProperty("name")
    String name;

    int age;

    Dummy(String name, int age) {
      this.name = name;
      this.age = age;
    }
  }

  @Test
  void test_Filter_EqualsCondition() {
    List<Dummy> list = List.of(new Dummy("Alice", 25), new Dummy("Bob", 30));

    List<FilterCondition> filters = List.of(new FilterCondition("name", "=", "Alice"));
    List<OrderBy> orderBy = List.of();
    PageResult<Dummy> result =
        StreamQueryUtils.filterSortPaginateWithTotal(list, filters, orderBy, 0, 10);

    assertThat(result.items()).hasSize(1);
    assertThat(result.items().get(0).name).isEqualTo("Alice");
  }

  @Test
  void test_Sort_ByAgeDesc() {
    List<Dummy> list = List.of(new Dummy("A", 20), new Dummy("B", 30));

    List<OrderBy> orderBy = List.of(new OrderBy("age", true));
    PageResult<Dummy> result =
        StreamQueryUtils.filterSortPaginateWithTotal(list, List.of(), orderBy, 0, 10);

    assertThat(result.items()).hasSize(2);
    assertThat(result.items().get(0).name).isEqualTo("B");
  }

  @Test
  void test_Pagination_Page1Size1() {
    List<Dummy> list = List.of(new Dummy("A", 20), new Dummy("B", 30));

    PageResult<Dummy> result =
        StreamQueryUtils.filterSortPaginateWithTotal(list, List.of(), List.of(), 1, 1);

    assertThat(result.items()).hasSize(1);
    assertThat(result.items().get(0).name).isEqualTo("B");
  }

  @Test
  void testEqualsOperator() {
    assertThat(StreamQueryUtils.evaluateCondition("test", "=", "test")).isTrue();
    assertThat(
            StreamQueryUtils.evaluateCondition(StreamQueryUtils.coerceToComparable(10), "=", "10"))
        .isTrue();
  }

  @Test
  void testNotEqualsOperator() {
    assertThat(StreamQueryUtils.evaluateCondition("test", "!=", "other")).isTrue();
    assertThat(StreamQueryUtils.evaluateCondition("10", "!=", "5")).isTrue();
    assertThat(
            StreamQueryUtils.evaluateCondition(StreamQueryUtils.coerceToComparable(10), "!=", "5"))
        .isTrue();
  }

  @Test
  void testGreaterThanOperator() {
    assertThat(
            StreamQueryUtils.evaluateCondition(StreamQueryUtils.coerceToComparable(10), ">", "5"))
        .isTrue();
    assertThat(StreamQueryUtils.evaluateCondition("z", ">", "a")).isTrue();
  }

  @Test
  void testLessThanOperator() {
    assertThat(StreamQueryUtils.evaluateCondition(StreamQueryUtils.coerceToComparable(3), "<", "5"))
        .isTrue();
  }

  @Test
  void testGreaterThanOrEqualOperator() {
    assertThat(
            StreamQueryUtils.evaluateCondition(StreamQueryUtils.coerceToComparable(5), ">=", "5"))
        .isTrue();
    assertThat(
            StreamQueryUtils.evaluateCondition(StreamQueryUtils.coerceToComparable(10), ">=", "5"))
        .isTrue();
  }

  @Test
  void testLessThanOrEqualOperator() {
    assertThat(
            StreamQueryUtils.evaluateCondition(StreamQueryUtils.coerceToComparable(5), "<=", "5"))
        .isTrue();
    assertThat(
            StreamQueryUtils.evaluateCondition(StreamQueryUtils.coerceToComparable(3), "<=", "5"))
        .isTrue();
  }

  @Test
  void testContainsOperator() {
    assertThat(StreamQueryUtils.evaluateCondition("hello world", ":", "world")).isTrue();
    assertThat(StreamQueryUtils.evaluateCondition("TestCase", ":", "test")).isTrue();
    assertThat(StreamQueryUtils.evaluateCondition("abc", ":", "x")).isFalse();
  }

  @Test
  void testNullActualReturnsFalse() {
    assertThat(StreamQueryUtils.evaluateCondition(null, "=", "something")).isFalse();
  }

  @Test
  void testUnknownOperatorReturnsFalse() {
    assertThat(StreamQueryUtils.evaluateCondition("abc", "@", "abc")).isFalse();
  }

  @Test
  void testLocalDateTimeParsing() {
    LocalDateTime actual = LocalDateTime.parse("2024-04-23T12:00:00");
    assertThat(StreamQueryUtils.evaluateCondition(actual, "=", "2024-04-23T12:00:00")).isTrue();
    assertThat(StreamQueryUtils.evaluateCondition(actual, ">", "2024-04-22T12:00:00")).isTrue();
  }
}

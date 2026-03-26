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

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObjectMapperUtilTest {

  private ObjectMapper objectMapper;
  private ObjectMapper objectMapperNonNull;

  @BeforeEach
  public void setUp() {

    objectMapper = ObjectMapperUtil.getObectMapperInstance();
    objectMapperNonNull = ObjectMapperUtil.getObectMapperInstanceForNonNull();
  }

  @Test
  public void testGetObjectMapperInstance() {

    assertNotNull(objectMapper);
    assertFalse(
        objectMapper
            .getSerializationConfig()
            .isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    assertFalse(
        objectMapper
            .getDeserializationConfig()
            .isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    assertEquals(
        "yyyy-MM-dd HH:mm:ss.SSSSSS",
        ((SimpleDateFormat) objectMapper.getDateFormat()).toPattern());
  }

  @Test
  public void testGetObjectMapperInstanceForNonNull() {
    assertNotNull(objectMapperNonNull);
    assertFalse(
        objectMapperNonNull
            .getSerializationConfig()
            .isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    assertFalse(
        objectMapperNonNull
            .getDeserializationConfig()
            .isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    assertEquals(
        JsonInclude.Include.NON_NULL,
        objectMapperNonNull
            .getSerializationConfig()
            .getDefaultPropertyInclusion()
            .getValueInclusion());
  }

  @Test
  public void testToJsonString() {
    TestObject testObject = new TestObject("Test", 123);
    String jsonString = ObjectMapperUtil.toJsonString(testObject);
    assertEquals("{\"name\":\"Test\",\"value\":123}", jsonString);
  }

  @Test
  public void testToJsonStringNonNull() {
    TestObject testObject = new TestObject("Test", 123);
    String jsonString = ObjectMapperUtil.toJsonStringNonNull(testObject);
    assertEquals("{\"name\":\"Test\",\"value\":123}", jsonString);
  }

  @Test
  public void testConvertObject() {
    TestObject testObject = new TestObject("Test", 123);
    TestObject2 convertedObject = ObjectMapperUtil.convertObject(testObject, TestObject2.class);
    assertNotNull(convertedObject);
    assertEquals(testObject.getName(), convertedObject.getName());
    assertEquals(testObject.getValue(), convertedObject.getValue());
  }

  @Test
  public void testConvertStringToObject() {
    String jsonString = "{\"name\":\"Test\",\"value\":123}";
    TestObject testObject = ObjectMapperUtil.convertStringToObject(jsonString, TestObject.class);
    assertNotNull(testObject);
    assertEquals("Test", testObject.getName());
    assertEquals(123, testObject.getValue());
  }

  @Test
  public void testConvertJsonStringToList() {
    String jsonString = "[{\"name\":\"Test1\",\"value\":123},{\"name\":\"Test2\",\"value\":456}]";
    List<TestObject> testObjectList =
        ObjectMapperUtil.convertJsonStringToList(jsonString, TestObject.class);
    assertNotNull(testObjectList);
    assertEquals(2, testObjectList.size());
    assertEquals("Test1", testObjectList.get(0).getName());
    assertEquals(123, testObjectList.get(0).getValue());
    assertEquals("Test2", testObjectList.get(1).getName());
    assertEquals(456, testObjectList.get(1).getValue());
  }

  @Test
  public void testConvertJsonStringToSet() {
    String jsonString = "[{\"name\":\"Test1\",\"value\":123},{\"name\":\"Test2\",\"value\":456}]";
    Set<TestObject> testObjectSet =
        ObjectMapperUtil.convertJsonStringToSet(jsonString, TestObject.class);
    assertNotNull(testObjectSet);
    assertEquals(2, testObjectSet.size());
  }

  @Test
  public void testJsonToMap() {
    String jsonString = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
    Map<String, Object> map = ObjectMapperUtil.jsonToMap(jsonString);
    assertNotNull(map);
    assertEquals(2, map.size());
    assertEquals("value1", map.get("key1"));
    assertEquals("value2", map.get("key2"));
  }

  @Test
  public void testJsonToMapString() {
    String jsonString = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
    Map<String, String> map = ObjectMapperUtil.jsonToMapString(jsonString);
    assertNotNull(map);
    assertEquals(2, map.size());
    assertEquals("value1", map.get("key1"));
    assertEquals("value2", map.get("key2"));
  }

  // @Test
  // public void testConvertJsonPriceToObject() {
  // String jsonString = "{\"key1\":{\"subkey1\":{\"price\":123.45}}}";
  // Map<String, Map<String, InputOutputPrice>> map =
  // ObjectMapperUtil.convertJsonPriceToObject(jsonString);
  // assertNotNull(map);
  // assertTrue(map.containsKey("key1"));
  // assertTrue(map.get("key1").containsKey("subkey1"));
  // assertEquals(123.45, map.get("key1").get("subkey1").getPrice());
  // }

  @Getter
  @Setter
  public class InputOutputPrice {

    private double price;

    public InputOutputPrice() {}

    public InputOutputPrice(double price) {
      this.price = price;
    }
  }

  @Test
  public void isValidJSON_metadataNull() {
    String jsonString = null;
    ObjectMapperUtil.isValidJSON(jsonString);
  }

  @Test
  public void isValidJSON_metadataValid() {

    String jsonString = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
    ObjectMapperUtil.isValidJSON(jsonString);
  }

  @Test
  public void isValidJSON_metadataNotValid() {

    String jsonString = "{\"key1\":,\"key2\":\"value2\"}";
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          ObjectMapperUtil.isValidJSON(jsonString);
        });
  }
}

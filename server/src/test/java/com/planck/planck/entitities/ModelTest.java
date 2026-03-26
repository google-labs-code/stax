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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.planck.planck.enums.ModelProvider;
import com.planck.planck.enums.ModelType;
import org.junit.jupiter.api.Test;

public class ModelTest {

  @Test
  public void testModelCreation() {
    Model model = new Model();
    model.setName("test-model");
    model.setLabel("Test Label");
    model.setProvider(ModelProvider.GOOGLE);
    model.setUrl("http://example.com");
    model.setType(ModelType.SYSTEM);
    model.setDescription("A test model");
    model.setComments("Some comments");
    model.setProperties("{\"property\": \"value\"}");
    model.setDescriptors("{\"descriptor\": \"value\"}");

    assertEquals("test-model", model.getName());
    assertEquals("Test Label", model.getLabel());
    assertEquals(ModelProvider.GOOGLE, model.getProvider());
    assertEquals("http://example.com", model.getUrl());
    assertEquals(ModelType.SYSTEM, model.getType());
    assertEquals("A test model", model.getDescription());
    assertEquals("Some comments", model.getComments());
    assertEquals("{\"property\": \"value\"}", model.getProperties());
    assertEquals("{\"descriptor\": \"value\"}", model.getDescriptors());
  }
}

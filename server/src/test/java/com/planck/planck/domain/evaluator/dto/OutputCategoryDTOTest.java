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

package com.planck.planck.domain.evaluator.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class OutputCategoryDTOTest {

  @Test
  public void testSetColor_AutomaticallySetsColorName() {
    OutputCategoryDTO dto = new OutputCategoryDTO();
    dto.setColor("var(--color-red)");

    assertEquals("var(--color-red)", dto.color);
    assertEquals("Red", dto.getColorName());
  }

  @Test
  public void testGetColorName_WhenColorNameIsNull() {
    OutputCategoryDTO dto = new OutputCategoryDTO();
    dto.setColor("var(--color-green)");

    // Clear the colorName to test the getter logic
    dto.colorName = null;

    assertEquals("Green", dto.getColorName());
  }

  @Test
  public void testJsonSerialization_IncludesColorName() throws Exception {
    OutputCategoryDTO dto = new OutputCategoryDTO();
    dto.name = "High Quality";
    dto.setColor("var(--color-red)");
    dto.value = "high";

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(dto);

    assertTrue(json.contains("color_name"));
    assertTrue(json.contains("Red"));
  }

  @Test
  public void testJsonDeserialization_WithColorName() throws Exception {
    String json =
        """
            {
                "name": "High Quality",
                "color": "var(--color-red)",
                "color_name": "Red",
                "value": "high"
            }
            """;

    ObjectMapper mapper = new ObjectMapper();
    OutputCategoryDTO dto = mapper.readValue(json, OutputCategoryDTO.class);

    assertEquals("High Quality", dto.name);
    assertEquals("var(--color-red)", dto.color);
    assertEquals("Red", dto.getColorName());
    assertEquals("high", dto.value);
  }

  @Test
  public void testJsonDeserialization_WithoutColorName() throws Exception {
    String json =
        """
            {
                "name": "High Quality",
                "color": "var(--color-red)",
                "value": "high"
            }
            """;

    ObjectMapper mapper = new ObjectMapper();
    OutputCategoryDTO dto = mapper.readValue(json, OutputCategoryDTO.class);

    assertEquals("High Quality", dto.name);
    assertEquals("var(--color-red)", dto.color);
    assertEquals("Red", dto.getColorName()); // Should be automatically derived
    assertEquals("high", dto.value);
  }

  @Test
  public void testProjectSpecificColors() {
    OutputCategoryDTO dto = new OutputCategoryDTO();

    dto.setColor("#737284");
    assertEquals("Slate Gray", dto.getColorName());

    dto.setColor("#FFE3E3");
    assertEquals("Light Red", dto.getColorName());

    dto.setColor("#FFF3BF");
    assertEquals("Light Yellow", dto.getColorName());

    dto.setColor("#D3F9D8");
    assertEquals("Light Green", dto.getColorName());
  }

  @Test
  public void testCustomColorMappings() {
    OutputCategoryDTO dto = new OutputCategoryDTO();

    dto.setColor("var(--color-red)");
    assertEquals("Red", dto.getColorName());

    dto.setColor("var(--color-purple)");
    assertEquals("Purple", dto.getColorName());

    dto.setColor("var(--color-pink)");
    assertEquals("Pink", dto.getColorName());

    dto.setColor("var(--color-orange)");
    assertEquals("Orange", dto.getColorName());

    dto.setColor("var(--color-green)");
    assertEquals("Green", dto.getColorName());

    dto.setColor("#5F5BFF");
    assertEquals("Violet", dto.getColorName());

    dto.setColor("#3D95FC");
    assertEquals("Blue", dto.getColorName());

    dto.setColor("#8595AB");
    assertEquals("Grey", dto.getColorName());
  }

  @Test
  public void testCaseInsensitiveColorHandling() {
    OutputCategoryDTO dto = new OutputCategoryDTO();

    // Test with lowercase
    dto.setColor("var(--color-red)");
    assertEquals("Red", dto.getColorName());

    // Test with mixed case
    dto.setColor("var(--color-purple)");
    assertEquals("Purple", dto.getColorName());

    // Test with hex codes (legacy support)
    dto.setColor("#f84464");
    assertEquals("Red", dto.getColorName());

    // Test with uppercase hex
    dto.setColor("#B66EFF");
    assertEquals("Purple", dto.getColorName());

    // Test with mixed case hex
    dto.setColor("#03c89A");
    assertEquals("Green", dto.getColorName());
  }
}

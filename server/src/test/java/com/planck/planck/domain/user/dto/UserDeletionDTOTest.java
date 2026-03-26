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

package com.planck.planck.domain.user.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.planck.planck.util.ObjectMapperUtil;
import java.util.List;
import org.junit.jupiter.api.Test;

public class UserDeletionDTOTest {

  @Test
  void testDeserializeFromJson() throws Exception {
    // Test JSON that matches the actual entity_deletion_order.json structure
    String json =
        """
        {
          "user_id": "user-123",
          "deletion_steps": [
            {
              "table_name": "score"
            },
            {
              "table_name": "evaluation_score"
            }
          ]
        }
        """;

    UserDeletionDTO dto = ObjectMapperUtil.convertStringToObject(json, UserDeletionDTO.class);

    assertNotNull(dto);
    assertEquals("user-123", dto.getUserId());
    assertNotNull(dto.getDeletionSteps());
    assertEquals(2, dto.getDeletionSteps().size());

    UserDeletionDTO.DeletionStep firstStep = dto.getDeletionSteps().get(0);
    assertEquals("score", firstStep.getTableName());

    UserDeletionDTO.DeletionStep secondStep = dto.getDeletionSteps().get(1);
    assertEquals("evaluation_score", secondStep.getTableName());
  }

  @Test
  void testDeserializeListFromJson() throws Exception {
    // Test JSON array that matches the entity_deletion_order.json structure
    String json =
        """
        [
          {
            "table_name": "score"
          },
          {
            "table_name": "evaluation_score"
          }
        ]
        """;

    List<UserDeletionDTO.DeletionStep> steps =
        ObjectMapperUtil.convertJsonStringToList(json, UserDeletionDTO.DeletionStep.class);

    assertNotNull(steps);
    assertEquals(2, steps.size());

    UserDeletionDTO.DeletionStep firstStep = steps.get(0);
    assertEquals("score", firstStep.getTableName());

    UserDeletionDTO.DeletionStep secondStep = steps.get(1);
    assertEquals("evaluation_score", secondStep.getTableName());
  }
}

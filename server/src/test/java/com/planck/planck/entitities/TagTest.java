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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.planck.planck.enums.TagType;
import java.sql.Timestamp;
import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void getterSetterTag() {

    String ID_PREFIX = "tag-";
    String tagId = ID_PREFIX;
    String tagName = "tagName";
    TagType type = TagType.USER;
    String firstName = "firstName";
    User user = new User();
    user.setFirstName(firstName);
    String color = "color";
    Timestamp createdAt = new Timestamp(1234567890);
    Timestamp updatedAt = new Timestamp(1234567890);

    Tag tag = new Tag();
    tag.prePersist();
    tag.prePersist();
    tag.setTagId(tagId);
    tag.setTagName(tagName);
    tag.setType(type);
    tag.setUser(user);
    tag.setColor(color);
    tag.setCreatedAt(createdAt);
    tag.setUpdatedAt(updatedAt);

    assertTrue(tag.getTagId().contains(tagId));
    assertEquals(tagName, tag.getTagName());
    assertEquals(type, tag.getType());
    assertEquals(firstName, tag.getUser().getFirstName());
    assertEquals(color, tag.getColor());
    assertNotNull(tag.getCreatedAt());
    assertNotNull(tag.getUpdatedAt());
  }
}

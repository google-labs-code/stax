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

import com.planck.planck.enums.Role;
import java.sql.Timestamp;
import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void userGetterSetterAndDefaultConstructorTest() {

    String ID_PREFIX = "user-";
    String id = ID_PREFIX;
    String firstName = "firstName";
    String lastName = "lastName";
    String email = "abc@gmail.com";
    Timestamp createdAt = new Timestamp(1234567890);
    Timestamp updatedAt = new Timestamp(1234567890);
    Role role = Role.USER;

    User user = new User();
    user.prePersist();
    user.prePersist();
    user.setId(id);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setCreatedAt(createdAt);
    user.setUpdatedAt(updatedAt);
    user.setRole(role);

    assertTrue(user.getId().contains(ID_PREFIX));
    assertEquals(firstName, user.getFirstName());
    assertEquals(lastName, user.getLastName());
    assertEquals(email, user.getEmail());
    assertNotNull(user.getCreatedAt());
    assertNotNull(user.getUpdatedAt());
    assertEquals(role, user.getRole());
  }

  @Test
  public void parameterizedConstructorAndMethodsTest() {

    String firstName = "firstName";
    String lastName = "lastName";
    String email = "email";
    Role role = Role.USER;

    User user = new User(firstName, lastName, email, role);
    user.getAuthorities()
        .forEach(
            auth -> {
              assertEquals(role.name(), auth.getAuthority());
            });

    assertEquals(email, user.getUsername());
    assertTrue(user.isAccountNonExpired());
    assertTrue(user.isAccountNonLocked());
    assertTrue(user.isCredentialsNonExpired());
    assertTrue(user.isEnabled());
  }
}

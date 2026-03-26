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

package com.planck.planck.mock;

import com.planck.planck.entitities.User;
import com.planck.planck.enums.Role;
import java.util.UUID;

public class UserMockData {

  public static User createMockUser() {

    User user = new User();
    user.setId("user-" + UUID.randomUUID().toString());
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("johndoe@example.com");
    user.setRole(Role.USER);

    return user;
  }

  // Example with a specific role and email verification status
  public static User createMockUser(Role role, boolean isEmailVerified) {

    User user = new User();
    user.setId("user-" + UUID.randomUUID().toString());
    user.setFirstName("Jane");
    user.setLastName("Smith");
    user.setEmail("janesmith@example.com");
    user.setRole(role);

    return user;
  }
}

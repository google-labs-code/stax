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

public class MockUser {

  public User getMockedUserTestData() {

    User user = new User();
    user.setFirstName("Rajesh");
    user.setEmail("rajesh.vashist.01@gmail.com");
    user.setLastName("kumar");

    return user;
  }

  public User getMockedUserSuccessResponse() {

    User user = new User();
    user.setFirstName("Rajesh");
    user.setEmail("rajesh1@gmail.com");
    user.setLastName("kumar");

    return user;
  }
}

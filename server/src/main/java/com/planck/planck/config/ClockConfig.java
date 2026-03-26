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

package com.planck.planck.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

  /**
   * Provides a Clock bean representing the system clock fixed to UTC. Using UTC is generally
   * recommended for server-side time consistency. Injecting Clock makes components that depend on
   * the current time easily testable.
   *
   * @return A Clock instance using the system time in UTC.
   */
  @Bean
  public Clock clock() {
    // For consistent time handling independent of server's default timezone
    return Clock.systemUTC();
  }
}

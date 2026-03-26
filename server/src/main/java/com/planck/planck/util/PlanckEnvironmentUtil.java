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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component("planckEnvironmentUtil")
public class PlanckEnvironmentUtil implements ApplicationContextAware {

  private static Environment environment;

  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
    environment = applicationContext.getEnvironment();
  }

  public static String getProperty(String name) {
    return environment.getProperty(name);
  }

  @Nullable
  public static <T> T getProperty(String key, Class<T> targetType) {
    return environment.getProperty(key, targetType);
  }

  public static void setEnvironment(Environment environment) {
    PlanckEnvironmentUtil.environment = environment;
  }

  public static <T> T getBean(Class<T> requiredType) {
    return context.getBean(requiredType);
  }
}

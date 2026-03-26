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

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Needed to support automatic retries for acquiring database lock in a multi-server environment */
@Configuration
@EnableConfigurationProperties({LiquibaseRetryProperties.class})
public class LiquibaseConfig {
  @Bean
  @ConfigurationProperties(prefix = "spring.liquibase")
  public LiquibaseProperties liquibaseProperties() {
    return new LiquibaseProperties();
  }

  @Value("${user.email}")
  private String adminUserMail;

  @Bean
  public SpringLiquibase liquibase(
      DataSource dataSource,
      LiquibaseProperties liquibaseProperties,
      LiquibaseRetryProperties retryProps) {
    SpringLiquibase liquibase =
        new SpringLiquibase() {
          @Override
          public void afterPropertiesSet() throws LiquibaseException {
            int attempts = retryProps.getMaxAttempts();
            int wait = retryProps.getWaitSeconds();

            for (int i = 1; i <= attempts; i++) {
              try {
                super.afterPropertiesSet();
                return;
              } catch (LockException e) {
                if (i == attempts) throw e;
                try {
                  Thread.sleep(wait * 1000L);
                } catch (InterruptedException ignored) {
                }
              }
            }
          }
        };

    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog(liquibaseProperties.getChangeLog());
    liquibase.setContexts(liquibaseProperties.getContexts());
    liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
    liquibase.setDropFirst(liquibaseProperties.isDropFirst());
    liquibase.setShouldRun(liquibaseProperties.isEnabled());

    Map<String, String> changelogParams = new HashMap<>();
    if (liquibaseProperties.getParameters() != null) {
      changelogParams.putAll(liquibaseProperties.getParameters());
    }

    Map<String, String> params = new HashMap<>();
    params.put("adminUserMail", adminUserMail);
    liquibase.setChangeLogParameters(params);

    return liquibase;
  }
}

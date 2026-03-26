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

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("monitoring")
@ConfigurationProperties(prefix = "monitoring.schedule")
public class MonitoringScheduleConfig {

  private boolean enabled = true;
  private String cron = "0 0 9,21 * * ?"; // default: every day at 9&21 server time
  private Email email = new Email();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }

  // for future use, if/when we switch to a mail provider instead of monitoring and alerting.
  public Email getEmail() {
    return email;
  }

  public void setEmail(Email email) {
    this.email = email;
  }

  public static class Email {
    private boolean enabled = true;
    private List<String> toAddresses;
    private String fromAddress = "noreply@planck.com";
    private String subject = "Stale User Deletion Alert";

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public List<String> getToAddresses() {
      return toAddresses;
    }

    public void setToAddresses(List<String> toAddresses) {
      this.toAddresses = toAddresses;
    }

    public String getFromAddress() {
      return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
      this.fromAddress = fromAddress;
    }

    public String getSubject() {
      return subject;
    }

    public void setSubject(String subject) {
      this.subject = subject;
    }
  }
}

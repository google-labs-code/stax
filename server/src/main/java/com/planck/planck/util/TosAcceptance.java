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

import java.sql.Timestamp;

public class TosAcceptance {

  private String tosId;
  private Timestamp acceptedAt;

  public TosAcceptance() {}

  public TosAcceptance(String tosId, Timestamp acceptedAt) {
    this.tosId = tosId;
    this.acceptedAt = acceptedAt;
  }

  public String getTosId() {
    return tosId;
  }

  public void setTosId(String tosId) {
    this.tosId = tosId;
  }

  public Timestamp getAcceptedAt() {
    return acceptedAt;
  }

  public void setAcceptedAt(Timestamp acceptedAt) {
    this.acceptedAt = acceptedAt;
  }
}

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

package com.planck.planck.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Job status enumeration")
public enum JobStatusEnum {
  @Schema(description = "Job is queued and waiting to start")
  PENDING(1, "Pending"),
  @Schema(description = "Job is currently in progress")
  IN_PROGRESS(2, "In-Progress"),
  @Schema(description = "Job completed successfully")
  COMPLETED(3, "Completed"),
  @Schema(description = "Job failed during execution")
  FAILED(4, "Failed"),
  @Schema(description = "Job stopped manually")
  STOPPED(5, "Stopped");

  private Integer key;
  private String value;

  private JobStatusEnum(Integer key, String value) {
    this.key = key;
    this.value = value;
  }

  public Integer getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public static String getValueByKey(Integer key) {
    for (JobStatusEnum status : JobStatusEnum.values()) {
      if (status.getKey().equals(key)) {
        return status.getValue();
      }
    }
    return null;
  }

  public static JobStatusEnum getJobStatusEnumByKey(Integer key) {
    for (JobStatusEnum status : JobStatusEnum.values()) {
      if (status.getKey().equals(key)) {
        return status;
      }
    }
    return null;
  }
}

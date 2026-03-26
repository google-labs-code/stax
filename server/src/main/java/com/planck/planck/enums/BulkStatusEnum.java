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

public enum BulkStatusEnum {
  PENDING(1, "Pending"),
  IN_PROGRESS(2, "In-Progress"),
  SUCCESSFUL(3, "Successful"),
  FAILED(4, "Failed");

  private Integer key;
  private String value;

  private BulkStatusEnum(Integer key, String value) {
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
    for (BulkStatusEnum status : BulkStatusEnum.values()) {
      if (status.getKey().equals(key)) {
        return status.getValue();
      }
    }
    return null;
  }

  public static BulkStatusEnum getScroreStatusEnumByKey(Integer key) {
    for (BulkStatusEnum status : BulkStatusEnum.values()) {
      if (status.getKey().equals(key)) {
        return status;
      }
    }
    return null;
  }
}

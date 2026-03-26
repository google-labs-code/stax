/**
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

import {
  ApiMappedColumnOption,
  ApiMappedColumnValue,
  UPLOAD_CHAT_FORMAT_COLUMNS,
  UPLOAD_POINTWISE_COLUMNS,
  UPLOAD_SXS_CHAT_FORMAT_COLUMNS,
  UPLOAD_SXS_COLUMNS,
} from "@/config/constants";
import { ProjectType } from "@/types";

export function getColumnConfiguration(
  projectType: ProjectType,
  isChatFormat: boolean,
): {
  columns: ApiMappedColumnOption[];
  requiredColumn: ApiMappedColumnValue;
} {
  if (projectType === ProjectType.SIDE_BY_SIDE) {
    if (isChatFormat) {
      return {
        columns: [...UPLOAD_SXS_CHAT_FORMAT_COLUMNS],
        requiredColumn: ApiMappedColumnValue.chatAColumn,
      };
    } else {
      return {
        columns: [...UPLOAD_SXS_COLUMNS],
        requiredColumn: ApiMappedColumnValue.inputColumn,
      };
    }
  } else {
    if (isChatFormat) {
      return {
        columns: [...UPLOAD_CHAT_FORMAT_COLUMNS],
        requiredColumn: ApiMappedColumnValue.chatColumn,
      };
    } else {
      return {
        columns: [...UPLOAD_POINTWISE_COLUMNS],
        requiredColumn: ApiMappedColumnValue.inputColumn,
      };
    }
  }
}

export function createInitialSelectedState(): Record<string, boolean> {
  return Object.values(ApiMappedColumnValue).reduce(
    (acc, value) => {
      acc[value] = false;

      return acc;
    },
    {} as Record<string, boolean>,
  );
}

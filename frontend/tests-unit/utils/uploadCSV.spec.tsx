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
import {
  createInitialSelectedState,
  getColumnConfiguration,
} from "@/utils/uploadCSV";

describe("Column Configuration Utilities", () => {
  describe("getColumnConfiguration", () => {
    it("returns correct configuration for side-by-side chat format", () => {
      const config = getColumnConfiguration(ProjectType.SIDE_BY_SIDE, true);
      expect(config.columns).toEqual(UPLOAD_SXS_CHAT_FORMAT_COLUMNS);
      expect(config.requiredColumn).toBe(ApiMappedColumnValue.chatAColumn);
    });

    it("returns correct configuration for side-by-side non-chat format", () => {
      const config = getColumnConfiguration(ProjectType.SIDE_BY_SIDE, false);
      expect(config.columns).toEqual(UPLOAD_SXS_COLUMNS);
      expect(config.requiredColumn).toBe(ApiMappedColumnValue.inputColumn);
    });

    it("returns correct configuration for pointwise chat format", () => {
      const config = getColumnConfiguration(ProjectType.POINTWISE, true);
      expect(config.columns).toEqual(UPLOAD_CHAT_FORMAT_COLUMNS);
      expect(config.requiredColumn).toBe(ApiMappedColumnValue.chatColumn);
    });

    it("returns correct configuration for pointwise non-chat format", () => {
      const config = getColumnConfiguration(ProjectType.POINTWISE, false);
      expect(config.columns).toEqual(UPLOAD_POINTWISE_COLUMNS);
      expect(config.requiredColumn).toBe(ApiMappedColumnValue.inputColumn);
    });

    it("returns a copy of the columns array, not a reference", () => {
      const config = getColumnConfiguration(ProjectType.SIDE_BY_SIDE, true);
      const originalLength = config.columns.length;

      config.columns.push({} as ApiMappedColumnOption);

      expect(UPLOAD_SXS_CHAT_FORMAT_COLUMNS.length).toBe(originalLength);
    });
  });

  describe("createInitialSelectedState", () => {
    it("creates an object with all ApiMappedColumnValue values as keys and false as values", () => {
      const initialState = createInitialSelectedState();

      Object.values(ApiMappedColumnValue).forEach((value) => {
        expect(initialState).toHaveProperty(value);
        expect(initialState[value]).toBe(false);
      });
    });

    it("creates an object with exactly the same keys as ApiMappedColumnValue", () => {
      const initialState = createInitialSelectedState();
      const stateKeys = Object.keys(initialState).sort();
      const enumValues = Object.values(ApiMappedColumnValue).sort();

      expect(stateKeys).toEqual(enumValues);
      expect(stateKeys.length).toBe(enumValues.length);
    });

    it("sets all values to false", () => {
      const initialState = createInitialSelectedState();

      expect(
        Object.values(initialState).every((value) => value === false),
      ).toBe(true);
    });
  });
});

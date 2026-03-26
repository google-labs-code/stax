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

import ConvertMsToSeconds from "@/utils/ConvertMsToSeconds";

describe("ConvertMsToSeconds", () => {
  it("converts milliseconds to seconds with 2 decimal places", () => {
    expect(ConvertMsToSeconds(1000)).toBe(1);
    expect(ConvertMsToSeconds(1500)).toBe(1.5);
    expect(ConvertMsToSeconds(1230)).toBe(1.23);
    expect(ConvertMsToSeconds(1250)).toBe(1.25);
  });

  it("rounds to 2 decimal places", () => {
    expect(ConvertMsToSeconds(1234)).toBe(1.23);
    expect(ConvertMsToSeconds(1235)).toBe(1.24); 
    expect(ConvertMsToSeconds(1236)).toBe(1.24);
  });

  it("handles zero", () => {
    expect(ConvertMsToSeconds(0)).toBe(0);
  });

  it("handles negative numbers", () => {
    expect(ConvertMsToSeconds(-1000)).toBe(-1);
    expect(ConvertMsToSeconds(-1500)).toBe(-1.5);
    expect(ConvertMsToSeconds(-1234)).toBe(-1.23);
    expect(ConvertMsToSeconds(-1235)).toBe(-1.24); 
  });

  it("handles large numbers", () => {
    expect(ConvertMsToSeconds(60000)).toBe(60);
    expect(ConvertMsToSeconds(3600000)).toBe(3600);
    expect(ConvertMsToSeconds(3600500)).toBe(3600.5);
  });

  it("handles small fractions", () => {
    expect(ConvertMsToSeconds(1)).toBe(0);
    expect(ConvertMsToSeconds(4)).toBe(0); 
    expect(ConvertMsToSeconds(5)).toBe(0.01); 
    expect(ConvertMsToSeconds(9)).toBe(0.01);
  });

  it("handles floating point precision correctly", () => {
    expect(ConvertMsToSeconds(1234.56)).toBe(1.23);
    expect(ConvertMsToSeconds(1235.5)).toBe(1.24);
  });
});
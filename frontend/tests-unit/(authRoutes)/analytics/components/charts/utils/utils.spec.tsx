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
  getDotDataDodgeChart,
  getDynamicRadiusOfDot,
} from "@/app/(authRoutes)/analytics/components/charts/utils/utils";

describe("getDotDataDodgeChart", () => {
  it("returns empty array for empty input", () => {
    const result = getDotDataDodgeChart([]);
    expect(result).toEqual([]);
  });

  it("skips entries with falsy or invalid count", () => {
    const data = [
      { count: 0 },
      { count: null },
      { count: NaN },
      { count: -5 },
    ] as any;

    const result = getDotDataDodgeChart(data);
    expect(result).toEqual([]);
  });

  it("limits count to 130", () => {
    const data = [
      {
        category: "Test",
        count: 150,
        score: 0.8,
        rawScore: 0.8,
        filter: "A",
        color: "red",
        label: "TestLabel",
      },
    ];

    const result = getDotDataDodgeChart(data as any);
    expect(result.length).toBe(130);
    expect(result[0].x).toBe(0.8);
  });

  it("creates entries equal to count", () => {
    const data = [
      {
        category: "Cat1",
        count: 3,
        score: 0.6,
        rawScore: 0.6,
        filter: "B",
        color: "blue",
        label: "LabelB",
      },
    ];

    const result = getDotDataDodgeChart(data as any);
    expect(result).toHaveLength(3);
    expect(result.every((r) => r.score === 0.6)).toBe(true);
  });
});

describe("getDynamicRadiusOfDot", () => {
  it("returns 8 when maxCount < 50 and isSecondFilterOn is false", () => {
    const dotData = [{ count: 30 }];
    const r = getDynamicRadiusOfDot(dotData, false);
    expect(r).toBe(8);
  });

  it("returns 5 when 50 <= maxCount < 80 and isSecondFilterOn is false", () => {
    const dotData = [{ count: 60 }];
    const r = getDynamicRadiusOfDot(dotData, false);
    expect(r).toBe(5);
  });

  it("returns 3 when maxCount >= 80 and isSecondFilterOn is false", () => {
    const dotData = [{ count: 100 }];
    const r = getDynamicRadiusOfDot(dotData, false);
    expect(r).toBe(3);
  });

  it("halves radius when isSecondFilterOn is true and maxCount > 50", () => {
    const dotData = [{ count: 100 }];
    const r = getDynamicRadiusOfDot(dotData, true);
    expect(r).toBe(1.5);
  });

  it("does not halve radius when isSecondFilterOn is true and maxCount <= 50", () => {
    const dotData = [{ count: 40 }];
    const r = getDynamicRadiusOfDot(dotData, true);
    expect(r).toBe(8);
  });
});

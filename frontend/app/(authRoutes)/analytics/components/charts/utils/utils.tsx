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

import { generateRange } from "../../utils";
import { DodgePlotData } from "../types";

export const getDotDataDodgeChart = (data: DodgePlotData[]) =>
  data.flatMap((d) => {
    if (!d.count || isNaN(d.count) || d.count < 1) return [];
    const count = d.count > 130 ? 130 : d.count;
    const xRange = generateRange(0, count - 1, 1);

    return xRange.map(() => ({
      category: d.category,
      x: d.score,
      score: d.score || 0,
      filter: d.filter,
      color: d.color,
      label: d.label,
      rawScore: d.rawScore,
      count,
    }));
  });

export const getDynamicRadiusOfDot = (
  dotData: any,
  isSecondFilterOn: boolean,
) => {
  const maxCount = Math.max(...dotData.map((d: any) => d.count || 0));
  const rSize = maxCount < 50 ? 8 : maxCount < 80 ? 5 : 3;

  return rSize / (isSecondFilterOn && maxCount > 50 ? 2 : 1);
};

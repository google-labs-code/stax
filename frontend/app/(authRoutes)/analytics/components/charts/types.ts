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

export interface BarChartData {
  readonly category: number;
  readonly count: number;
  readonly color: string;
  readonly filter?: string;
  readonly label?: string;
  readonly categoryName?: string;
}

export interface BarChartProps {
  readonly data: BarChartData[];
  readonly title?: string;
  readonly subtitle?: string;
  readonly yLabel?: string;
  readonly isSecondFilterOn: boolean;
}

export interface DodgePlotData {
  readonly category: string;
  readonly score: number;
  readonly count: number;
  readonly color: string;
  readonly filter?: string;
  readonly label?: string;
  readonly x?: number;
  readonly rawScore?: string;
}

export interface DodgePlotProps {
  readonly data: DodgePlotData[];
  readonly isSecondFilterOn: boolean;
}

export interface LineItemData {
  readonly windowStart: string | Date | undefined;
  readonly value: number;
  readonly filter?: string;
  readonly label: string;
}

export interface LineChartProps {
  readonly data: LineItemData[];
  readonly isSecondFilterOn: boolean;
}

export interface StackChartItem {
  readonly windowStart: Date;
  readonly type: string;
  readonly value: number;
  readonly filter?: string;
}

export interface StackedBarChartProps {
  readonly data: StackChartItem[];
  readonly x: string;
  readonly y: string;
  readonly fill: string;
}

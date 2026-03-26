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

import { Dispatch, SetStateAction } from "react";

import { EvaluationChartDataItem } from "../../types/charts";

export type EvaluatorsOptions = {
  score: string;
  checked: boolean;
  visible: boolean;
};
export interface ChartsRenderingProps {
  readonly chart: EvaluationChartDataItem;
  readonly isSecondFilterOn: boolean;
}
export interface EvaluationChartProps {
  readonly data: EvaluationChartDataItem[] | null;
  readonly isLoading: boolean;
  readonly isSecondFilterOn: boolean;
}
export interface PopoverEvaluatorMenuProps {
  readonly selectedEvals: EvaluatorsOptions[];
  readonly setSelectedEvals: Dispatch<SetStateAction<EvaluatorsOptions[]>>;
}

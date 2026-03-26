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

import { EvaluationStatusColor } from "@/types";

import { MetricPromptColorType } from "../../types";

export const MetricPromptColors: MetricPromptColorType[] = [
  {
    label: "Red",
    value: EvaluationStatusColor.RED,
  },
  {
    label: "Orange",
    value: EvaluationStatusColor.ORANGE,
  },
  {
    label: "Lime",
    value: EvaluationStatusColor.LIME,
  },
  {
    label: "Green",
    value: EvaluationStatusColor.GREEN,
  },
  {
    label: "Blue",
    value: EvaluationStatusColor.BLUE,
  },
  {
    label: "Violet",
    value: EvaluationStatusColor.VIOLET,
  },
  {
    label: "Purple",
    value: EvaluationStatusColor.PURPLE,
  },
  {
    label: "Pink",
    value: EvaluationStatusColor.PINK,
  },
  {
    label: "Grey",
    value: EvaluationStatusColor.GREY,
  },
];

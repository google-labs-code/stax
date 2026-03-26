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

import { SXSRow, WorkbookItem } from "@/app/(authRoutes)/projects/[id]/types";

export function buildSxSRowData(sxsRow: SXSRow): WorkbookItem {
  return {
    ...sxsRow.chat_turn_a, // @TODO: TEMP SOLUTION, REFACTOR THIS AFTER LAUNCH
    input: sxsRow.input,
    expected_output: sxsRow.expected_output,
    chat_turn_b: sxsRow.chat_turn_b,
    human_sxs_rating: sxsRow.human_sxs_rating,
    human_sxs_notes: sxsRow.human_sxs_notes,
    pairId: sxsRow.id,
    point_evaluations: sxsRow.point_evaluations,
    sxs_evaluations: sxsRow.sxs_evaluations,
    chat_turn_a: sxsRow?.chat_turn_a,
  };
}

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
  PointEvaluation,
  WorkbookItem,
} from "@/app/(authRoutes)/projects/[id]/types";
import { LLMEvaluation, LLMEvaluations, ProjectType } from "@/types";

export function findChatIdForTurnInData(
  data: WorkbookItem[] | undefined,
  turnId: string,
  projectType?: ProjectType,
): string | null {
  if (projectType === ProjectType.SIDE_BY_SIDE) {
    if (turnId?.startsWith("sxs-pair-")) {
      return turnId;
    }

    if (data) {
      for (const item of data) {
        if (
          (item.chat_turn_id === turnId ||
            item.chat_turn_a?.chat_turn_id === turnId ||
            item.chat_turn_b?.chat_turn_id === turnId) &&
          item.pairId
        ) {
          return item.pairId;
        }
      }

      for (const item of data) {
        if (item.subRows) {
          for (const subRow of item.subRows) {
            if (
              (subRow.chat_turn_id === turnId ||
                subRow.chat_turn_a?.chat_turn_id === turnId ||
                subRow.chat_turn_b?.chat_turn_id === turnId) &&
              subRow.pairId
            ) {
              return subRow.pairId;
            }
          }
        }
      }
    }

    return null;
  } else {
    if (!data) return null;

    for (const item of data) {
      if (item.chat_turn_id === turnId) {
        return item.chat_id;
      }

      if (item.subRows) {
        for (const subRow of item.subRows) {
          if (subRow.chat_turn_id === turnId) {
            return subRow.chat_id;
          }
        }
      }
    }

    return null;
  }
}

export const updateTableDataWithEvaluations = (
  data: WorkbookItem[] | undefined,
  turnId: string,
  evaluations: LLMEvaluations,
): WorkbookItem[] | undefined => {
  if (!data) return data;

  const isPointEvaluation = (
    evaluation: unknown,
  ): evaluation is PointEvaluation => {
    return (
      !!evaluation &&
      typeof evaluation === "object" &&
      ("chatTurnA" in evaluation || "chatTurnB" in evaluation)
    );
  };

  const firstEvalKey = Object.keys(evaluations)[0];
  const isPointEvals =
    firstEvalKey && isPointEvaluation(evaluations[firstEvalKey]);

  return data.map((row) => {
    if (row.chat_turn_id === turnId || row.id === turnId) {
      if (isPointEvals && (row.point_evaluations || row?.sxs_evaluations)) {
        return {
          ...row,
          point_evaluations: {
            ...row.point_evaluations,
            ...(evaluations as unknown as Record<string, PointEvaluation>),
          },
          sxs_evaluations: {
            ...row?.sxs_evaluations,
            ...(evaluations as unknown as Record<string, LLMEvaluation>),
          },
        };
      } else if (!isPointEvals) {
        return {
          ...row,
          llm_evaluations: {
            ...row.llm_evaluations,
            ...evaluations,
          },
        };
      }
    }

    if (
      (row.chat_turn_a?.chat_turn_id === turnId ||
        row.chat_turn_b?.chat_turn_id === turnId) &&
      isPointEvals &&
      (row.point_evaluations || row?.sxs_evaluations)
    ) {
      return {
        ...row,
        point_evaluations: {
          ...row.point_evaluations,
          ...(evaluations as unknown as Record<string, PointEvaluation>),
        },
        sxs_evaluations: {
          ...row?.sxs_evaluations,
          ...(evaluations as unknown as Record<string, LLMEvaluation>),
        },
      };
    }

    if (row.subRows) {
      const updatedSubRows = row.subRows.map((subRow) => {
        if (subRow.chat_turn_id === turnId || subRow.id === turnId) {
          if (
            isPointEvals &&
            (subRow.point_evaluations || subRow?.sxs_evaluations)
          ) {
            return {
              ...subRow,
              point_evaluations: {
                ...subRow.point_evaluations,
                ...(evaluations as unknown as Record<string, PointEvaluation>),
              },
              sxs_evaluations: {
                ...subRow?.sxs_evaluations,
                ...(evaluations as unknown as Record<string, LLMEvaluation>),
              },
            };
          } else if (!isPointEvals) {
            return {
              ...subRow,
              llm_evaluations: {
                ...subRow.llm_evaluations,
                ...evaluations,
              },
            };
          }
        }

        if (
          (subRow.chat_turn_a?.chat_turn_id === turnId ||
            subRow.chat_turn_b?.chat_turn_id === turnId) &&
          isPointEvals &&
          (subRow.point_evaluations || subRow?.sxs_evaluations)
        ) {
          return {
            ...subRow,
            point_evaluations: {
              ...subRow.point_evaluations,
              ...(evaluations as unknown as Record<string, PointEvaluation>),
            },
            sxs_evaluations: {
              ...subRow?.sxs_evaluations,
              ...(evaluations as unknown as Record<string, LLMEvaluation>),
            },
          };
        }

        return subRow;
      });

      return { ...row, subRows: updatedSubRows };
    }

    return row;
  });
};

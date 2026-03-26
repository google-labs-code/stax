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

import { WorkbookItem } from "@/app/(authRoutes)/projects/[id]/types";
import { EvaluationScoreStatus, ProjectType } from "@/types";
import { MRT_FilterFn, MRT_SortingFn } from "mantine-react-table";

export const createTokenAccessorFn = (
  tokenField: "output_tokens" | "total_tokens",
) => {
  return (row: WorkbookItem) => {
    if (row.chat_turn_a && row.chat_turn_b) {
      const valueA = row.chat_turn_a?.inference_tokens?.[tokenField] || 0;
      const valueB = row.chat_turn_b?.inference_tokens?.[tokenField] || 0;
      
      return `${valueA} ${valueB}`;
    }

    const tokens = row.inference_tokens;

    if (!tokens) return "";

    return tokens[tokenField];
  };
};

export const getEvaluationSortingFn = (
  evaluationKey: string,
  projectType?: ProjectType,
): MRT_SortingFn<WorkbookItem> => {
  return (rowA, rowB) => {
    if (projectType === ProjectType.SIDE_BY_SIDE) {
      const scoreAA = parseFloat(
        rowA.original.point_evaluations?.[evaluationKey]?.chatTurnA?.score ||
          "0",
      );
      const scoreAB = parseFloat(
        rowA.original.point_evaluations?.[evaluationKey]?.chatTurnB?.score ||
          "0",
      );
      const scoreBA = parseFloat(
        rowB.original.point_evaluations?.[evaluationKey]?.chatTurnA?.score ||
          "0",
      );
      const scoreBB = parseFloat(
        rowB.original.point_evaluations?.[evaluationKey]?.chatTurnB?.score ||
          "0",
      );

      const maxA = Math.max(
        isNaN(scoreAA) ? 0 : scoreAA,
        isNaN(scoreAB) ? 0 : scoreAB,
      );
      const maxB = Math.max(
        isNaN(scoreBA) ? 0 : scoreBA,
        isNaN(scoreBB) ? 0 : scoreBB,
      );

      if (maxA === 0 && maxB === 0) return 0;
      if (maxA === 0) return 1;
      if (maxB === 0) return -1;

      return maxB - maxA;
    } else {
      const scoreA = parseFloat(
        rowA.original.llm_evaluations?.[evaluationKey]?.score || "0",
      );
      const scoreB = parseFloat(
        rowB.original.llm_evaluations?.[evaluationKey]?.score || "0",
      );

      if (
        (scoreA === undefined || scoreA === null) &&
        (scoreB === undefined || scoreB === null)
      )
        return 0;

      if (scoreA === undefined || scoreA === null) return 1;
      if (scoreB === undefined || scoreB === null) return -1;

      return scoreB - scoreA;
    }
  };
};

export const getEvaluationFilterFn = (
  evaluationKey: string,
  projectType?: ProjectType,
): MRT_FilterFn<WorkbookItem> => {
  return (row, columnId, filterValue) => {
    if (
      filterValue === undefined ||
      filterValue === null ||
      filterValue === ""
    ) {
      return true;
    }

    const filterString = String(filterValue).toLowerCase();

    if (projectType === ProjectType.SIDE_BY_SIDE) {
      const evaluationA =
        row.original.point_evaluations?.[evaluationKey]?.chatTurnA;
      const evaluationB =
        row.original.point_evaluations?.[evaluationKey]?.chatTurnB;

      const evaluationStatusA = evaluationA?.evaluationStatus;
      const evaluationStatusB = evaluationB?.evaluationStatus;

      if (
        (!evaluationStatusA ||
          evaluationStatusA !== EvaluationScoreStatus.SUCCESSFUL) &&
        (!evaluationStatusB ||
          evaluationStatusB !== EvaluationScoreStatus.SUCCESSFUL)
      ) {
        return false;
      }

      const scoreA = parseFloat(evaluationA?.score || "0");
      const scoreB = parseFloat(evaluationB?.score || "0");

      const threshold = parseFloat(filterString);
      if (!isNaN(threshold)) {
        return (
          (evaluationStatusA === EvaluationScoreStatus.SUCCESSFUL &&
            scoreA >= threshold) ||
          (evaluationStatusB === EvaluationScoreStatus.SUCCESSFUL &&
            scoreB >= threshold)
        );
      }

      const reasoningA = (evaluationA?.reasoning || "").toLowerCase();
      const reasoningB = (evaluationB?.reasoning || "").toLowerCase();
      const categoryA = (evaluationA?.category || "").toLowerCase();
      const categoryB = (evaluationB?.category || "").toLowerCase();

      return (
        (evaluationStatusA === EvaluationScoreStatus.SUCCESSFUL &&
          (reasoningA.includes(filterString) ||
            categoryA.includes(filterString) ||
            String(scoreA).includes(filterString))) ||
        (evaluationStatusB === EvaluationScoreStatus.SUCCESSFUL &&
          (reasoningB.includes(filterString) ||
            categoryB.includes(filterString) ||
            String(scoreB).includes(filterString)))
      );
    } else {
      const evaluation = row.original.llm_evaluations?.[evaluationKey];
      if (!evaluation) {
        return false;
      }

      const evaluationStatus = evaluation.evaluationStatus;
      if (
        !evaluationStatus ||
        evaluationStatus !== EvaluationScoreStatus.SUCCESSFUL
      ) {
        return false;
      }

      const threshold = parseFloat(filterString);
      const score = parseFloat(evaluation.score || "0");

      if (!isNaN(threshold)) {
        return score >= threshold;
      }

      const reasoning = (evaluation.reasoning || "").toLowerCase();
      const category = (evaluation.category || "").toLowerCase();
      const scoreString = String(score);

      return (
        reasoning.includes(filterString) ||
        category.includes(filterString) ||
        scoreString.includes(filterString)
      );
    }
  };
};

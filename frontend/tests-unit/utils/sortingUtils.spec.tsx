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
import {
  createTokenAccessorFn,
  getEvaluationFilterFn,
  getEvaluationSortingFn,
} from "@/utils/sortingUtils";
import type { MRT_Row } from "mantine-react-table";

describe("Sorting Utils", () => {
  const createMockWorkbookItem = (overrides = {}) =>
    ({
      id: "test-id",
      inference_tokens: {
        output_tokens: 100,
        total_tokens: 200,
        input_tokens: 300,
      },
      llm_evaluations: {
        quality: {
          score: "8.5",
          reasoning: "Good quality response",
          category: "High",
          evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
        },
      },
      point_evaluations: {
        quality: {
          chatTurnA: {
            score: "7.5",
            reasoning: "Pretty good response A",
            category: "Medium",
            evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
          },
          chatTurnB: {
            score: "9.0",
            reasoning: "Excellent response B",
            category: "High",
            evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
          },
          delta: "1.5",
        },
      },
      ...overrides,
    }) as unknown as WorkbookItem;

  function testSorting(sortFn: unknown, rowA: unknown, rowB: unknown): number {
    const sortingFunction = sortFn as unknown as (
      rowA: MRT_Row<any>,
      rowB: MRT_Row<any>,
      columnId: string,
    ) => number;

    return sortingFunction(
      { original: rowA } as MRT_Row<any>,
      { original: rowB } as MRT_Row<any>,
      "quality",
    );
  }

  function testFiltering(
    filterFn: unknown,
    row: unknown,
    filterValue: string,
  ): boolean {
    const filteringFunction = filterFn as unknown as (
      row: MRT_Row<any>,
      columnId: string,
      filterValue: string,
    ) => boolean;

    return filteringFunction(
      { original: row } as MRT_Row<any>,
      "quality",
      filterValue,
    );
  }

  describe("createTokenAccessorFn", () => {
    it("returns combined tokens for chat turn scenario", () => {
      const mockItem = createMockWorkbookItem({
        chat_turn_a: {
          inference_tokens: {
            output_tokens: 50,
            total_tokens: 100,
            input_tokens: 25,
          },
        },
        chat_turn_b: {
          inference_tokens: {
            output_tokens: 75,
            total_tokens: 150,
            input_tokens: 30,
          },
        },
      });

      const outputAccessor = createTokenAccessorFn("output_tokens");
      const totalAccessor = createTokenAccessorFn("total_tokens");

      expect(outputAccessor(mockItem)).toBe("50 75");
      expect(totalAccessor(mockItem)).toBe("100 150");
    });

    it("returns individual token values for non-chat scenario", () => {
      const mockItem = createMockWorkbookItem();

      const outputAccessor = createTokenAccessorFn("output_tokens");
      const totalAccessor = createTokenAccessorFn("total_tokens");

      expect(outputAccessor(mockItem)).toBe(100);
      expect(totalAccessor(mockItem)).toBe(200);
    });

    it("handles missing token data", () => {
      const mockItemNoTokens = createMockWorkbookItem({
        inference_tokens: null,
      });
      const mockItemEmptyTurns = createMockWorkbookItem({
        chat_turn_a: { inference_tokens: null },
        chat_turn_b: { inference_tokens: null },
      });

      const outputAccessor = createTokenAccessorFn("output_tokens");

      expect(outputAccessor(mockItemNoTokens)).toBe("");
      expect(outputAccessor(mockItemEmptyTurns)).toBe("0 0");
    });
  });

  describe("getEvaluationSortingFn", () => {
    it("sorts correctly for SIDE_BY_SIDE project type", () => {
      const mockItemA = createMockWorkbookItem();
      const mockItemB = createMockWorkbookItem({
        point_evaluations: {
          quality: {
            delta: "1.0",
            chatTurnA: {
              score: "5.0",
              evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
              reasoning: "OK response",
              category: "Medium",
            },
            chatTurnB: {
              score: "6.0",
              evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
              reasoning: "Better response",
              category: "Medium",
            },
          },
        },
      });

      const sortFn = getEvaluationSortingFn(
        "quality",
        ProjectType.SIDE_BY_SIDE,
      );
      const result = testSorting(sortFn, mockItemA, mockItemB);

      expect(result).toBeLessThan(0);
    });

    it("sorts correctly for non-SIDE_BY_SIDE project type", () => {
      const mockItemA = createMockWorkbookItem();
      const mockItemB = createMockWorkbookItem({
        llm_evaluations: {
          quality: {
            score: "9.5",
            evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
            reasoning: "Excellent response",
            category: "High",
          },
        },
      });

      const sortFn = getEvaluationSortingFn("quality");
      const result = testSorting(sortFn, mockItemA, mockItemB);

      expect(result).toBeGreaterThan(0);
    });

    it("handles missing evaluation data", () => {
      const mockItemA = createMockWorkbookItem({
        llm_evaluations: {},
      });
      const mockItemB = createMockWorkbookItem();

      const sortFn = getEvaluationSortingFn("quality");
      const result = testSorting(sortFn, mockItemA, mockItemB);

      expect(result).toBeGreaterThan(0);
    });

    it("handles zeros for SIDE_BY_SIDE", () => {
      const mockItemA = createMockWorkbookItem({
        point_evaluations: {
          quality: {
            delta: "0",
            chatTurnA: {
              score: "0",
              evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
              reasoning: "",
              category: "",
            },
            chatTurnB: {
              score: "0",
              evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
              reasoning: "",
              category: "",
            },
          },
        },
      });
      const mockItemB = createMockWorkbookItem({
        point_evaluations: {
          quality: {
            delta: "0",
            chatTurnA: {
              score: "0",
              evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
              reasoning: "",
              category: "",
            },
            chatTurnB: {
              score: "0",
              evaluationStatus: EvaluationScoreStatus.SUCCESSFUL,
              reasoning: "",
              category: "",
            },
          },
        },
      });

      const sortFn = getEvaluationSortingFn(
        "quality",
        ProjectType.SIDE_BY_SIDE,
      );
      const result = testSorting(sortFn, mockItemA, mockItemB);

      expect(result).toBe(0);
    });
  });

  describe("getEvaluationFilterFn", () => {
    it("filters correctly by numeric threshold for SIDE_BY_SIDE", () => {
      const mockItem = createMockWorkbookItem();
      const filterFn = getEvaluationFilterFn(
        "quality",
        ProjectType.SIDE_BY_SIDE,
      );

      expect(testFiltering(filterFn, mockItem, "7")).toBe(true);
      expect(testFiltering(filterFn, mockItem, "8")).toBe(true);
      expect(testFiltering(filterFn, mockItem, "9.5")).toBe(false);
    });

    it("filters correctly by text for SIDE_BY_SIDE", () => {
      const mockItem = createMockWorkbookItem();
      const filterFn = getEvaluationFilterFn(
        "quality",
        ProjectType.SIDE_BY_SIDE,
      );

      expect(testFiltering(filterFn, mockItem, "excellent")).toBe(true);
      expect(testFiltering(filterFn, mockItem, "high")).toBe(true);
      expect(testFiltering(filterFn, mockItem, "poor")).toBe(false);
    });

    it("filters correctly by numeric threshold for non-SIDE_BY_SIDE", () => {
      const mockItem = createMockWorkbookItem();
      const filterFn = getEvaluationFilterFn("quality");

      expect(testFiltering(filterFn, mockItem, "8")).toBe(true);
      expect(testFiltering(filterFn, mockItem, "9")).toBe(false);
    });

    it("handles empty or undefined filter values", () => {
      const mockItem = createMockWorkbookItem();
      const filterFn = getEvaluationFilterFn("quality");

      expect(testFiltering(filterFn, mockItem, "")).toBe(true);
      expect(testFiltering(filterFn, mockItem, null as unknown as string)).toBe(
        true,
      );
      expect(
        testFiltering(filterFn, mockItem, undefined as unknown as string),
      ).toBe(true);
    });

    it("handles missing evaluations for SIDE_BY_SIDE", () => {
      const mockItem = createMockWorkbookItem({
        point_evaluations: {},
      });
      const filterFn = getEvaluationFilterFn(
        "quality",
        ProjectType.SIDE_BY_SIDE,
      );

      expect(testFiltering(filterFn, mockItem, "8")).toBe(false);
    });

    it("handles missing evaluations for non-SIDE_BY_SIDE", () => {
      const mockItem = createMockWorkbookItem({
        llm_evaluations: {},
      });
      const filterFn = getEvaluationFilterFn("quality");

      expect(testFiltering(filterFn, mockItem, "8")).toBe(false);
    });

    it("handles unsuccessful evaluation status", () => {
      const mockItem = createMockWorkbookItem({
        llm_evaluations: {
          quality: {
            score: "8.5",
            reasoning: "Quality reasoning",
            category: "Medium",
            evaluationStatus: EvaluationScoreStatus.FAILED,
          },
        },
      });
      const filterFn = getEvaluationFilterFn("quality");

      expect(testFiltering(filterFn, mockItem, "8")).toBe(false);
    });
  });
});

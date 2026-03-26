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
import { EvaluationScoreStatus, LLMEvaluations, ProjectType } from "@/types";
import {
  findChatIdForTurnInData,
  updateTableDataWithEvaluations,
} from "@/utils/workbookHelpers";

describe("findChatIdForTurnInData", () => {
  const baseTurnId = "turn-123";

  it("returns the turnId itself for SIDE_BY_SIDE if it starts with 'sxs-pair-'", () => {
    const result = findChatIdForTurnInData(
      undefined,
      "sxs-pair-456",
      ProjectType.SIDE_BY_SIDE,
    );
    expect(result).toBe("sxs-pair-456");
  });

  it("finds pairId in main row for SIDE_BY_SIDE", () => {
    const data: WorkbookItem[] = [
      {
        chat_turn_id: "turn-1",
        pairId: "pair-1",
      } as WorkbookItem,
    ];

    const result = findChatIdForTurnInData(
      data,
      "turn-1",
      ProjectType.SIDE_BY_SIDE,
    );
    expect(result).toBe("pair-1");
  });

  it("finds pairId in chat_turn_a or chat_turn_b for SIDE_BY_SIDE", () => {
    const data: WorkbookItem[] = [
      {
        chat_turn_a: { chat_turn_id: "turn-a" },
        pairId: "pair-a",
      } as WorkbookItem,
    ];

    expect(
      findChatIdForTurnInData(data, "turn-a", ProjectType.SIDE_BY_SIDE),
    ).toBe("pair-a");
  });

  it("finds pairId in subRows for SIDE_BY_SIDE", () => {
    const data: WorkbookItem[] = [
      {
        subRows: [
          {
            chat_turn_id: "sub-turn",
            pairId: "pair-sub",
          },
        ],
      } as WorkbookItem,
    ];

    expect(
      findChatIdForTurnInData(data, "sub-turn", ProjectType.SIDE_BY_SIDE),
    ).toBe("pair-sub");
  });

  it("returns null if not found in SIDE_BY_SIDE", () => {
    expect(
      findChatIdForTurnInData([], "missing-id", ProjectType.SIDE_BY_SIDE),
    ).toBeNull();
  });

  it("returns chat_id in default mode (not SIDE_BY_SIDE)", () => {
    const data: WorkbookItem[] = [
      {
        chat_turn_id: baseTurnId,
        chat_id: "chat-123",
      } as WorkbookItem,
    ];

    expect(
      findChatIdForTurnInData(data, baseTurnId, ProjectType.POINTWISE),
    ).toBe("chat-123");
  });

  it("returns chat_id from subRow if not in main row (not SIDE_BY_SIDE)", () => {
    const data: WorkbookItem[] = [
      {
        subRows: [
          {
            chat_turn_id: baseTurnId,
            chat_id: "chat-sub",
          },
        ],
      } as WorkbookItem,
    ];

    expect(
      findChatIdForTurnInData(data, baseTurnId, ProjectType.POINTWISE),
    ).toBe("chat-sub");
  });

  it("returns null if no match found and not SIDE_BY_SIDE", () => {
    expect(
      findChatIdForTurnInData([], "nope", ProjectType.POINTWISE),
    ).toBeNull();
  });

  it("returns null if data is undefined", () => {
    expect(
      findChatIdForTurnInData(undefined, "any", ProjectType.POINTWISE),
    ).toBeNull();
  });
});

describe("updateTableDataWithEvaluations", () => {
  const pointEval: PointEvaluation = {
    chatTurnA: {
      score: "1",
      evaluationStatus: EvaluationScoreStatus.PENDING,
    },
    chatTurnB: {
      score: "2",
      evaluationStatus: EvaluationScoreStatus.PENDING,
    },
    delta: "",
  };

  const llmEval: LLMEvaluations = {
    evaluation1: {
      evaluationStatus: EvaluationScoreStatus.PENDING,
    },
  };

  it("updates point_evaluations in main row", () => {
    const data = [
      {
        id: "row-1",
        point_evaluations: {},
      },
    ];

    const updated = updateTableDataWithEvaluations(data, "row-1", {
      eval1: pointEval,
    });

    expect(updated?.[0].point_evaluations?.eval1).toEqual(pointEval);
  });

  it("updates llm_evaluations in main row", () => {
    const data = [
      {
        id: "row-2",
        llm_evaluations: {},
      },
    ];

    const updated = updateTableDataWithEvaluations(data, "row-2", llmEval);
    expect(updated?.[0].llm_evaluations?.evaluation1).toEqual({
      evaluationStatus: "0",
    });
  });

  it("updates point_evaluations in subRow", () => {
    const data = [
      {
        subRows: [
          {
            id: "sub-1",
            point_evaluations: {},
          },
        ],
      },
    ];

    const updated = updateTableDataWithEvaluations(data, "sub-1", {
      eval1: pointEval,
    });

    expect(updated?.[0].subRows?.[0].point_evaluations?.eval1).toEqual(
      pointEval,
    );
  });

  it("updates llm_evaluations in subRow", () => {
    const data = [
      {
        subRows: [
          {
            id: "sub-2",
            llm_evaluations: {},
          },
        ],
      },
    ];

    const updated = updateTableDataWithEvaluations(data, "sub-2", llmEval);
    expect(updated?.[0].subRows?.[0].llm_evaluations?.evaluation1).toEqual({
      evaluationStatus: "0",
    });
  });

  it("updates point_evaluations from chat_turn_a", () => {
    const data = [
      {
        chat_turn_a: { chat_turn_id: "turn-a" },
        point_evaluations: {},
      },
    ];

    const updated = updateTableDataWithEvaluations(data, "turn-a", {
      eval1: pointEval,
    });

    expect(updated?.[0].point_evaluations?.eval1).toEqual(pointEval);
  });

  it("updates point_evaluations from subRow.chat_turn_b", () => {
    const data = [
      {
        subRows: [
          {
            chat_turn_b: { chat_turn_id: "turn-b" },
            point_evaluations: {},
          },
        ],
      },
    ];

    const updated = updateTableDataWithEvaluations(data, "turn-b", {
      eval1: pointEval,
    });

    expect(updated?.[0].subRows?.[0].point_evaluations?.eval1).toEqual(
      pointEval,
    );
  });

  it("returns unchanged data if turnId not found", () => {
    const data = [
      {
        id: "no-match",
        llm_evaluations: {},
      },
    ];

    const updated = updateTableDataWithEvaluations(data, "some-id", llmEval);
    expect(updated).toEqual(data);
  });

  it("returns undefined if data is undefined", () => {
    expect(
      updateTableDataWithEvaluations(undefined, "any", llmEval),
    ).toBeUndefined();
  });
});

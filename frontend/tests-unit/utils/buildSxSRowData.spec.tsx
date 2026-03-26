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

import { SXSRow } from "@/app/(authRoutes)/projects/[id]/types";
import { buildSxSRowData } from "@/utils/buildSxSRowData";

describe("buildSxSRowData", () => {
  it("correctly builds a WorkbookItem from an SXSRow", () => {
    const mockSxSRow = {
      id: "pair123",
      input: "Test input",
      expected_output: "Expected output",
      human_sxs_rating: "BETTER_A",
      human_sxs_notes: "Test notes",
      point_evaluations: {
        quality: {
          delta: "0.1",
          chatTurnA: { score: "8.5" },
          chatTurnB: { score: "8.4" },
        },
      },
      chat_turn_a: {
        id: "a123",
        output: "Output A",
        model_name: "Model A",
      },
      chat_turn_b: {
        id: "b123",
        output: "Output B",
        model_name: "Model B",
      },
    } as unknown as SXSRow;

    const result = buildSxSRowData(mockSxSRow);

    expect(result.id).toBe(mockSxSRow.chat_turn_a.id);
    expect(result.output).toBe(mockSxSRow.chat_turn_a.output);
    expect(result.model_name).toBe(mockSxSRow.chat_turn_a.model_name);

    expect(result.input).toBe(mockSxSRow.input);
    expect(result.expected_output).toBe(mockSxSRow.expected_output);
    expect(result.chat_turn_b).toBe(mockSxSRow.chat_turn_b);
    expect(result.human_sxs_rating).toBe(mockSxSRow.human_sxs_rating);
    expect(result.human_sxs_notes).toBe(mockSxSRow.human_sxs_notes);
    expect(result.pairId).toBe(mockSxSRow.id);
    expect(result.point_evaluations).toBe(mockSxSRow.point_evaluations);
    expect(result.chat_turn_a).toBe(mockSxSRow.chat_turn_a);
  });

  it("handles missing optional properties correctly", () => {
    const mockSxSRow = {
      id: "pair123",
      input: "Test input",
      chat_turn_a: {
        id: "a123",
        output: "Output A",
      },
      chat_turn_b: {
        id: "b123",
        output: "Output B",
      },
    } as unknown as SXSRow;

    const result = buildSxSRowData(mockSxSRow);

    expect(result.input).toBe(mockSxSRow.input);
    expect(result.pairId).toBe(mockSxSRow.id);
    expect(result.expected_output).toBeUndefined();
    expect(result.human_sxs_notes).toBeUndefined();
    expect(result.human_sxs_rating).toBeUndefined();
    expect(result.point_evaluations).toBeUndefined();
  });

  it("safely handles undefined chat_turn_a", () => {
    const mockSxSRow = {
      id: "pair123",
      input: "Test input",
      expected_output: null,
      chat_turn_b: { id: "b123" },
    } as unknown as SXSRow;

    const result = buildSxSRowData(mockSxSRow);

    expect(result.input).toBe(mockSxSRow.input);
    expect(result.pairId).toBe(mockSxSRow.id);
    expect(result.chat_turn_b).toBe(mockSxSRow.chat_turn_b);
    expect(result.chat_turn_a).toBeUndefined();
  });

  it("preserves all properties when chat_turn_a has complex data", () => {
    const complexTurnA = {
      id: "complex123",
      output: "Complex output",
      inference_tokens: {
        input_tokens: 10,
        output_tokens: 100,
        total_tokens: 110,
      },
      variables: { key: "value" },
      model_properties: JSON.stringify({ temperature: 0.7 }),
      custom_field: "Should be preserved",
    };

    const mockSxSRow = {
      id: "pair123",
      input: "Test input",
      chat_turn_a: complexTurnA,
      chat_turn_b: { id: "b123" },
    } as unknown as SXSRow;

    const result = buildSxSRowData(mockSxSRow);

    expect(result.inference_tokens).toBe(complexTurnA.inference_tokens);
    expect(result.variables).toBe(complexTurnA.variables);
    expect(result.model_properties).toBe(complexTurnA.model_properties);

    expect(result.input).toBe(mockSxSRow.input);
    expect(result.pairId).toBe(mockSxSRow.id);
  });
});

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

import { AllEvaluatorsResponse, ProjectType } from "@/types";
import { getGroupedEvaluatorsList } from "@/utils/evaluators";

describe("getGroupedEvaluatorsList", () => {
  const createMockEvaluator = (
    id: string,
    name: string,
    outputCategories: any[] = [],
  ) => ({
    id,
    name,
    description: `Description for ${name}`,
    model: {
      id: "model-1",
      label: "Test Model",
      name: "gpt-4",
      provider: "OPENAI" as any,
      url: "",
      api_key: "",
      description: "",
      additional_headers: {},
      properties: {
        temperature: 0.7,
        max_tokens: 512,
        top_p: 1,
        seed: 42,
      },
      version: "",
      tag: "",
      model_type: "",
    },
    output_categories: outputCategories,
    output_format_type: "json",
    prompts: [],
    type: "USER" as any,
    evaluation_type: ProjectType.POINTWISE,
    variables: [],
    created_at: "2024-01-01T00:00:00Z",
    updated_at: "2024-01-01T00:00:00Z",
  });

  it("should return empty array when input has empty llm array", () => {
    const input: AllEvaluatorsResponse = {
      llm: [],
      heuristic: [],
    };

    const result = getGroupedEvaluatorsList(input);

    expect(result).toEqual([]);
  });

  it("should handle only pointwise evaluators", () => {
    const input: AllEvaluatorsResponse = {
      llm: [
        createMockEvaluator("eval-1", "Evaluator 1"),
        createMockEvaluator("eval-2", "Evaluator 2"),
      ],
      heuristic: [],
    };

    const result = getGroupedEvaluatorsList(input);

    expect(result).toHaveLength(2);
    expect(result[0]).toMatchObject({
      id: "eval-1",
      name: "Evaluator 1",
      evaluationTypes: [{ id: "eval-1", type: ProjectType.POINTWISE }],
    });
    expect(result[1]).toMatchObject({
      id: "eval-2",
      name: "Evaluator 2",
      evaluationTypes: [{ id: "eval-2", type: ProjectType.POINTWISE }],
    });
  });

  it("should handle only side-by-side evaluators", () => {
    const input: AllEvaluatorsResponse = {
      llm: [
        createMockEvaluator("pairwise-eval-1", "Evaluator 1"),
        createMockEvaluator("pairwise-eval-2", "Evaluator 2"),
      ],
      heuristic: [],
    };

    const result = getGroupedEvaluatorsList(input);

    expect(result).toHaveLength(2);
    expect(result[0]).toMatchObject({
      id: "pairwise-eval-1",
      name: "Evaluator 1",
      evaluationTypes: [
        { id: "pairwise-eval-1", type: ProjectType.SIDE_BY_SIDE },
      ],
    });
    expect(result[1]).toMatchObject({
      id: "pairwise-eval-2",
      name: "Evaluator 2",
      evaluationTypes: [
        { id: "pairwise-eval-2", type: ProjectType.SIDE_BY_SIDE },
      ],
    });
  });

  it("should merge pointwise and side-by-side evaluators with matching names", () => {
    const pointwiseCategories = [
      { name: "Good", value: "good", color: "#00ff00", color_name: "green" },
    ];
    const sideBySideCategories = [
      { name: "Better", value: "better", color: "#0000ff", color_name: "blue" },
    ];

    const input: AllEvaluatorsResponse = {
      llm: [
        createMockEvaluator("eval-1", "Test Evaluator", pointwiseCategories),
        createMockEvaluator(
          "pairwise-eval-1",
          "Test Evaluator",
          sideBySideCategories,
        ),
      ],
      heuristic: [],
    };

    const result = getGroupedEvaluatorsList(input);

    expect(result).toHaveLength(1);
    expect(result[0]).toMatchObject({
      id: "pairwise-eval-1", // Should use side-by-side id
      name: "Test Evaluator",
      evaluationTypes: [
        { id: "eval-1", type: ProjectType.POINTWISE },
        { id: "pairwise-eval-1", type: ProjectType.SIDE_BY_SIDE },
      ],
    });
    expect(result[0].output_categories).toEqual([
      ...pointwiseCategories,
      ...sideBySideCategories,
    ]);
  });

  it("should merge output_categories when pointwise evaluator already has categories", () => {
    const pointwiseCategories = [
      { name: "Good", value: "good", color: "#00ff00", color_name: "green" },
      { name: "Bad", value: "bad", color: "#ff0000", color_name: "red" },
    ];
    const sideBySideCategories = [
      { name: "Better", value: "better", color: "#0000ff", color_name: "blue" },
    ];

    const input: AllEvaluatorsResponse = {
      llm: [
        createMockEvaluator("eval-1", "Test Evaluator", pointwiseCategories),
        createMockEvaluator(
          "pairwise-eval-1",
          "Test Evaluator",
          sideBySideCategories,
        ),
      ],
      heuristic: [],
    };

    const result = getGroupedEvaluatorsList(input);

    expect(result[0].output_categories).toEqual([
      ...pointwiseCategories,
      ...sideBySideCategories,
    ]);
  });

  it("should set output_categories when pointwise evaluator has no categories", () => {
    const sideBySideCategories = [
      { name: "Better", value: "better", color: "#0000ff", color_name: "blue" },
    ];

    const input: AllEvaluatorsResponse = {
      llm: [
        createMockEvaluator("eval-1", "Test Evaluator", []),
        createMockEvaluator(
          "pairwise-eval-1",
          "Test Evaluator",
          sideBySideCategories,
        ),
      ],
      heuristic: [],
    };

    const result = getGroupedEvaluatorsList(input);

    expect(result[0].output_categories).toEqual(sideBySideCategories);
  });

  it("should handle pointwise and side-by-side evaluators with different names", () => {
    const input: AllEvaluatorsResponse = {
      llm: [
        createMockEvaluator("eval-1", "Pointwise Evaluator"),
        createMockEvaluator("pairwise-eval-1", "Side-by-Side Evaluator"),
      ],
      heuristic: [],
    };

    const result = getGroupedEvaluatorsList(input);

    expect(result).toHaveLength(2);
    expect(result[0]).toMatchObject({
      id: "eval-1",
      name: "Pointwise Evaluator",
      evaluationTypes: [{ id: "eval-1", type: ProjectType.POINTWISE }],
    });
    expect(result[1]).toMatchObject({
      id: "pairwise-eval-1",
      name: "Side-by-Side Evaluator",
      evaluationTypes: [
        { id: "pairwise-eval-1", type: ProjectType.SIDE_BY_SIDE },
      ],
    });
  });

  it("should handle multiple evaluators with mixed pointwise and side-by-side", () => {
    const input: AllEvaluatorsResponse = {
      llm: [
        createMockEvaluator("eval-1", "Evaluator A"),
        createMockEvaluator("eval-2", "Evaluator B"),
        createMockEvaluator("pairwise-eval-1", "Evaluator A"),
        createMockEvaluator("pairwise-eval-2", "Evaluator C"),
      ],
      heuristic: [],
    };

    const result = getGroupedEvaluatorsList(input);

    expect(result).toHaveLength(3);

    // Evaluator A should be merged
    const evaluatorA = result.find((e) => e.name === "Evaluator A");
    expect(evaluatorA).toBeDefined();
    expect(evaluatorA?.evaluationTypes).toHaveLength(2);
    expect(evaluatorA?.evaluationTypes).toContainEqual({
      id: "eval-1",
      type: ProjectType.POINTWISE,
    });
    expect(evaluatorA?.evaluationTypes).toContainEqual({
      id: "pairwise-eval-1",
      type: ProjectType.SIDE_BY_SIDE,
    });
    expect(evaluatorA?.id).toBe("pairwise-eval-1");

    // Evaluator B should remain pointwise only
    const evaluatorB = result.find((e) => e.name === "Evaluator B");
    expect(evaluatorB).toBeDefined();
    expect(evaluatorB?.evaluationTypes).toHaveLength(1);
    expect(evaluatorB?.evaluationTypes?.[0]).toEqual({
      id: "eval-2",
      type: ProjectType.POINTWISE,
    });

    // Evaluator C should be side-by-side only
    const evaluatorC = result.find((e) => e.name === "Evaluator C");
    expect(evaluatorC).toBeDefined();
    expect(evaluatorC?.evaluationTypes).toHaveLength(1);
    expect(evaluatorC?.evaluationTypes?.[0]).toEqual({
      id: "pairwise-eval-2",
      type: ProjectType.SIDE_BY_SIDE,
    });
  });

  it("should handle side-by-side evaluator with 'pairwise-' prefix in different positions", () => {
    const input: AllEvaluatorsResponse = {
      llm: [
        createMockEvaluator("some-pairwise-eval", "Test Evaluator"),
        createMockEvaluator("eval-pairwise-1", "Test Evaluator"),
      ],
      heuristic: [],
    };

    const result = getGroupedEvaluatorsList(input);

    // Both contain "pairwise-" so both are side-by-side
    // The function merges items with the same name, regardless of type
    // So the second one merges with the first
    expect(result).toHaveLength(1);
    expect(result[0].id).toBe("eval-pairwise-1"); // Last processed id is used
    expect(result[0].name).toBe("Test Evaluator");
    // The first one is treated as pointwise (since it was added first), then merged with the second
    expect(result[0].evaluationTypes).toHaveLength(2);
    expect(result[0].evaluationTypes).toContainEqual({
      id: "some-pairwise-eval", // First id (treated as pointwise)
      type: ProjectType.POINTWISE,
    });
    expect(result[0].evaluationTypes).toContainEqual({
      id: "eval-pairwise-1", // Second id (side-by-side)
      type: ProjectType.SIDE_BY_SIDE,
    });
  });

  it("should preserve all other properties from the original evaluator", () => {
    const input: AllEvaluatorsResponse = {
      llm: [createMockEvaluator("eval-1", "Test Evaluator")],
      heuristic: [],
    };

    const result = getGroupedEvaluatorsList(input);

    expect(result[0]).toMatchObject({
      description: "Description for Test Evaluator",
      output_format_type: "json",
      type: "USER",
      evaluation_type: ProjectType.POINTWISE,
      created_at: "2024-01-01T00:00:00Z",
      updated_at: "2024-01-01T00:00:00Z",
    });
  });

  it("should handle evaluators with undefined output_categories", () => {
    const evaluatorWithoutCategories = createMockEvaluator(
      "eval-1",
      "Test Evaluator",
    );
    delete (evaluatorWithoutCategories as any).output_categories;

    const sideBySideCategories = [
      { name: "Better", value: "better", color: "#0000ff", color_name: "blue" },
    ];

    const input: AllEvaluatorsResponse = {
      llm: [
        evaluatorWithoutCategories,
        createMockEvaluator(
          "pairwise-eval-1",
          "Test Evaluator",
          sideBySideCategories,
        ),
      ],
      heuristic: [],
    };

    const result = getGroupedEvaluatorsList(input);

    expect(result[0].output_categories).toEqual(sideBySideCategories);
  });

  it("should handle multiple side-by-side evaluators matching the same pointwise evaluator", () => {
    const input: AllEvaluatorsResponse = {
      llm: [
        createMockEvaluator("eval-1", "Test Evaluator"),
        createMockEvaluator("pairwise-eval-1", "Test Evaluator"),
        createMockEvaluator("pairwise-eval-2", "Test Evaluator"),
      ],
      heuristic: [],
    };

    const result = getGroupedEvaluatorsList(input);

    // Both side-by-side evaluators merge with the pointwise one
    // The id gets updated each time, and evaluationTypes uses the current id
    expect(result).toHaveLength(1);

    const merged = result[0];
    expect(merged.name).toBe("Test Evaluator");
    expect(merged.id).toBe("pairwise-eval-2"); // Last processed id is used

    // After first merge: id becomes "pairwise-eval-1", evaluationTypes uses that id
    // After second merge: id becomes "pairwise-eval-2", evaluationTypes uses "pairwise-eval-1" (previous id) for POINTWISE
    expect(merged.evaluationTypes).toHaveLength(2);
    expect(merged.evaluationTypes).toContainEqual({
      id: "pairwise-eval-1", // Previous id after first merge
      type: ProjectType.POINTWISE,
    });
    expect(merged.evaluationTypes).toContainEqual({
      id: "pairwise-eval-2", // Current side-by-side id
      type: ProjectType.SIDE_BY_SIDE,
    });
  });
});

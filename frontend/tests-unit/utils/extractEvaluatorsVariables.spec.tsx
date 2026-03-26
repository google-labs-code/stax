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

import extractEvaluatorsVariables from "@/utils/extractEvaluatorsVariables";

describe("extractEvaluatorsVariables", () => {
  test("should return empty array for empty input", () => {
    expect(extractEvaluatorsVariables("")).toEqual([]);
    expect(extractEvaluatorsVariables(undefined as unknown as string)).toEqual(
      [],
    );
    expect(extractEvaluatorsVariables(null as unknown as string)).toEqual([]);
  });

  test("should return empty array when no variables are present", () => {
    expect(
      extractEvaluatorsVariables("This is a string with no variables"),
    ).toEqual([]);
  });

  test("should extract a single variable", () => {
    const input = "This is a string with {{variable}}";
    expect(extractEvaluatorsVariables(input)).toEqual([
      { name: "variable", required: false },
    ]);
  });

  test("should extract multiple variables", () => {
    const input = "This has {{first}} and {{second}} variables";
    expect(extractEvaluatorsVariables(input)).toEqual([
      { name: "first", required: false },
      { name: "second", required: false },
    ]);
  });

  test("should remove duplicate variables", () => {
    const input = "{{var}} and another {{var}}";
    expect(extractEvaluatorsVariables(input)).toEqual([
      { name: "var", required: false },
    ]);
  });

  test("should extract variables with spaces", () => {
    const input = "This has {{variable with spaces}}";
    expect(extractEvaluatorsVariables(input)).toEqual([
      { name: "variable with spaces", required: false },
    ]);
  });

  test("should handle variables at beginning, middle and end", () => {
    const input = "{{start}} in the middle {{end}}";
    expect(extractEvaluatorsVariables(input)).toEqual([
      { name: "start", required: false },
      { name: "end", required: false },
    ]);
  });

  test("should handle complex prompt with multiple variables", () => {
    const input = `# Instruction
    You are an expert evaluator. Your task is to evaluate the quality of responses.
    {{history}}
    {{input}}
    {{output}}`;

    expect(extractEvaluatorsVariables(input)).toEqual([
      { name: "history", required: false },
      { name: "input", required: false },
      { name: "output", required: false },
    ]);
  });

  test("should ignore empty variable names", () => {
    const input = "Empty {{}} variable";
    expect(extractEvaluatorsVariables(input)).toEqual([]);
  });

  test("should handle adjacent variables", () => {
    const input = "Adjacent {{first}}{{second}} variables";
    expect(extractEvaluatorsVariables(input)).toEqual([
      { name: "first", required: false },
      { name: "second", required: false },
    ]);
  });
});

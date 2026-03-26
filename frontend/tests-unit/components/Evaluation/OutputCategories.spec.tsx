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

import OutputCategories from "@/components/Evaluation/OutputCategories";
import { testRenderLite } from "@/tests-unit/render";
import { LLMEvaluatorOutputCategory } from "@/types";
import { screen } from "@testing-library/react";

jest.mock("@/utils/colorUtils", () => ({
  useProcessedColor: (color: string) => ({
    backgroundColor: color,
    textColor: "#000000",
  }),
}));

describe("OutputCategories", () => {
  it("renders categories sorted by numeric value", () => {
    const categories: LLMEvaluatorOutputCategory[] = [
      {
        name: "Cat A",
        value: "50%",
        color: "#ff0000",
        color_name: "Red",
      },
      {
        name: "Cat B",
        value: "1/4",
        color: "#00ff00",
        color_name: "Green",
      },
      {
        name: "Cat C",
        value: "0.5",
        color: "#0000ff",
        color_name: "Blue",
      },
    ];

    testRenderLite(<OutputCategories categories={categories} />);

    const renderedValues = screen
      .getAllByText(/.+/)
      .map((el) => el.textContent);
    expect(renderedValues).toEqual(["1/4", "50%", "0.5"]);
  });

  it("renders category colors and values", () => {
    const categories = [{ name: "Cat A", value: "10", color: "#abc123" }];

    testRenderLite(
      <OutputCategories
        categories={categories as LLMEvaluatorOutputCategory[]}
      />,
    );

    expect(screen.getByText("10")).toBeInTheDocument();

    expect(screen.getByTitle("Cat A")).toBeInTheDocument();
    expect(screen.getByLabelText("Cat A: 10")).toBeInTheDocument();
  });
});

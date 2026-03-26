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

import PopoverModelCard from "@/app/(authRoutes)/projects/[id]/components/PopoverModelCard";
import { WorkbookItem } from "@/app/(authRoutes)/projects/[id]/types";
import { testRender } from "@/tests-unit/render";
import { Provider } from "@/types";
import { formatModelName } from "@/utils/helpers";
import { act, fireEvent, screen } from "@testing-library/react";

describe("PopoverModelCard", () => {
  const baseRowData = {
    model_name: "Gemini",
    model_provider: "",
    model_properties: JSON.stringify({
      temperature: 0.7,
      max_output_tokens: 256,
      top_p: 0.9,
      seed: 42,
    }),
  };

  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
  });

  it("renders PopoverModelCard with model properties", async () => {
    testRender(
      <PopoverModelCard rowData={{ ...baseRowData } as WorkbookItem} />,
    );

    expect(screen.getByText("Gemini")).toBeInTheDocument();

    const target = screen.getByText("Show configurations");

    // Open the dropdown
    fireEvent.mouseEnter(target);

    // Fast-forward the delay
    act(() => {
      jest.advanceTimersByTime(100);
    });

    expect(await screen.getByText("Temperature:")).toBeInTheDocument();
    expect(screen.getByText("Max Tokens:")).toBeInTheDocument();
    expect(screen.getByText("Top P:")).toBeInTheDocument();
    expect(screen.getByText("Seed:")).toBeInTheDocument();
  });

  it("renders PopoverModelCard without model properties", () => {
    testRender(
      <PopoverModelCard
        rowData={
          {
            ...baseRowData,
            model_name: "",
            model_properties: "",
          } as WorkbookItem
        }
      />,
    );

    expect(screen.queryByText("Gemini")).not.toBeInTheDocument();

    const target = screen.getByText("Show configurations");

    // Open the dropdown
    fireEvent.mouseEnter(target);

    // Fast-forward the delay
    act(() => {
      jest.advanceTimersByTime(100);
    });

    expect(screen.getByText("No configuration available")).toBeInTheDocument();
  });

  it("renders PopoverModelCard with google icon", () => {
    testRender(
      <PopoverModelCard
        rowData={
          { ...baseRowData, model_provider: Provider.GOOGLE } as WorkbookItem
        }
      />,
    );

    expect(screen.getByTestId("google-icon")).toBeInTheDocument();
  });

  it("renders PopoverModelCard with mistralai icon", () => {
    testRender(
      <PopoverModelCard
        rowData={
          { ...baseRowData, model_provider: Provider.MISTRAL } as WorkbookItem
        }
      />,
    );

    expect(screen.getByTestId("mistralai-icon")).toBeInTheDocument();
  });

  it("renders PopoverModelCard with anthropic icon", () => {
    testRender(
      <PopoverModelCard
        rowData={
          { ...baseRowData, model_provider: Provider.ANTHROPIC } as WorkbookItem
        }
      />,
    );

    expect(screen.getByTestId("anthropicsai-icon")).toBeInTheDocument();
  });

  it("renders PopoverModelCard with openaai icon", () => {
    testRender(
      <PopoverModelCard rowData={{ ...baseRowData } as WorkbookItem} />,
    );

    expect(screen.getByTestId("openai-icon")).toBeInTheDocument();
  });

  it("should format the name correctly", () => {
    const testName = "test name";

    expect(formatModelName(testName)).toBe("Test name");
  });
});

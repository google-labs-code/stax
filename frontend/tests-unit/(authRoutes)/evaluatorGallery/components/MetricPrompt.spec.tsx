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

import MetricPrompt from "@/app/(authRoutes)/evaluatorGallery/[id]/components/MetricPrompt";
import { EvaluatorPageAction } from "@/app/(authRoutes)/evaluatorGallery/types";
import { EvaluatorFormData, LLMEvaluatorOutputCategory } from "@/queries/types";
import { testRender } from "@/tests-unit/render";
import { EvaluationStatusColor, ProjectType } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

const useParamsMock = jest.fn();

jest.mock("next/navigation", () => ({
  useParams: () => useParamsMock(),
}));

const defaultCategory: LLMEvaluatorOutputCategory = {
  name: "Test Category",
  color: EvaluationStatusColor.RED,
  value: "1.0",
  color_name: "Red",
};

const defaultData: EvaluatorFormData = {
  name: "Test Eval",
  description: "desc",
  selectedType: ProjectType.POINTWISE,
  [ProjectType.POINTWISE]: {
    id: "1",
    model_id: "model-1",
    output_categories: [defaultCategory],
    variables: [],
    prompt: "Prompt text",
    output_format_type: "Json" as const,
  },
  [ProjectType.SIDE_BY_SIDE]: {
    id: "2",
    model_id: "model-2",
    output_categories: [defaultCategory],
    variables: [],
    prompt: "Prompt text 2",
    output_format_type: "Json" as const,
  },
};

describe("MetricPrompt", () => {
  const mockSetData = jest.fn();
  const categoryKey = 0;

  beforeEach(() => {
    jest.clearAllMocks();
    useParamsMock.mockReturnValue({});
  });

  it("renders all input fields correctly", () => {
    testRender(
      <MetricPrompt
        category={defaultCategory}
        data={defaultData}
        setData={mockSetData}
        categoryKey={categoryKey}
      />,
    );

    expect(screen.getByDisplayValue("Test Category")).toBeInTheDocument();
    expect(screen.getByDisplayValue("1.0")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Red")).toBeInTheDocument();
  });

  it("updates category name when name input changes", () => {
    testRender(
      <MetricPrompt
        category={defaultCategory}
        data={defaultData}
        setData={mockSetData}
        categoryKey={categoryKey}
      />,
    );

    const nameInput = screen.getByDisplayValue("Test Category");
    fireEvent.change(nameInput, { target: { value: "Updated Category" } });

    expect(mockSetData).toHaveBeenCalledWith(expect.any(Function));

    const setDataCallback = mockSetData.mock.calls[0][0];
    const result = setDataCallback(defaultData);

    expect(result[ProjectType.POINTWISE].output_categories[0].name).toBe(
      "Updated Category",
    );
  });

  it("updates category value when value input changes with valid number", () => {
    testRender(
      <MetricPrompt
        category={defaultCategory}
        data={defaultData}
        setData={mockSetData}
        categoryKey={categoryKey}
      />,
    );

    const valueInput = screen.getByDisplayValue("1.0");
    fireEvent.change(valueInput, { target: { value: "2.5" } });

    expect(mockSetData).toHaveBeenCalledWith(expect.any(Function));

    const setDataCallback = mockSetData.mock.calls[0][0];
    const result = setDataCallback(defaultData);

    expect(result[ProjectType.POINTWISE].output_categories[0].value).toBe(
      "2.5",
    );
  });

  it("does not update category value when invalid input is entered", () => {
    testRender(
      <MetricPrompt
        category={defaultCategory}
        data={defaultData}
        setData={mockSetData}
        categoryKey={categoryKey}
      />,
    );

    const valueInput = screen.getByDisplayValue("1.0");
    fireEvent.change(valueInput, { target: { value: "abc" } });

    expect(mockSetData).not.toHaveBeenCalled();
  });

  it("allows decimal values in value input", () => {
    testRender(
      <MetricPrompt
        category={defaultCategory}
        data={defaultData}
        setData={mockSetData}
        categoryKey={categoryKey}
      />,
    );

    const valueInput = screen.getByDisplayValue("1.0");
    fireEvent.change(valueInput, { target: { value: "0.75" } });

    expect(mockSetData).toHaveBeenCalledWith(expect.any(Function));

    const setDataCallback = mockSetData.mock.calls[0][0];
    const result = setDataCallback(defaultData);

    expect(result[ProjectType.POINTWISE].output_categories[0].value).toBe(
      "0.75",
    );
  });

  it("updates category color when color is selected", () => {
    testRender(
      <MetricPrompt
        category={defaultCategory}
        data={defaultData}
        setData={mockSetData}
        categoryKey={categoryKey}
      />,
    );

    const colorSelect = screen.getByDisplayValue("Red");
    fireEvent.click(colorSelect);

    // Find and click on a color option (Green)
    const greenOption = screen.getByText("Green");
    fireEvent.click(greenOption);

    expect(mockSetData).toHaveBeenCalledTimes(2); // Called twice - once for color, once for color_name

    const setDataCallback = mockSetData.mock.calls[0][0];
    const result = setDataCallback(defaultData);

    expect(result[ProjectType.POINTWISE].output_categories[0].color).toBe(
      EvaluationStatusColor.GREEN,
    );
  });

  it("removes category when delete button is clicked", () => {
    testRender(
      <MetricPrompt
        category={defaultCategory}
        data={defaultData}
        setData={mockSetData}
        categoryKey={categoryKey}
      />,
    );

    const deleteButton = screen.getByRole("button");
    fireEvent.click(deleteButton);

    expect(mockSetData).toHaveBeenCalledWith(expect.any(Function));

    const setDataCallback = mockSetData.mock.calls[0][0];
    const result = setDataCallback(defaultData);

    expect(result[ProjectType.POINTWISE].output_categories).toHaveLength(0);
  });

  it("disables all inputs in read-only mode", () => {
    useParamsMock.mockReturnValue({ action: EvaluatorPageAction.VIEW });

    testRender(
      <MetricPrompt
        category={defaultCategory}
        data={defaultData}
        setData={mockSetData}
        categoryKey={categoryKey}
      />,
    );

    const nameInput = screen.getByDisplayValue("Test Category");
    const valueInput = screen.getByDisplayValue("1.0");
    const colorDisplay = screen.getByText("Red");

    expect(nameInput).toBeDisabled();
    expect(valueInput).toBeDisabled();
    expect(colorDisplay).toBeInTheDocument();
  });

  it("hides delete button in read-only mode", () => {
    useParamsMock.mockReturnValue({ action: EvaluatorPageAction.VIEW });

    testRender(
      <MetricPrompt
        category={defaultCategory}
        data={defaultData}
        setData={mockSetData}
        categoryKey={categoryKey}
      />,
    );

    expect(screen.queryByRole("button")).not.toBeInTheDocument();
  });

  it("displays fallback color when no color is selected", () => {
    const categoryWithoutColor: LLMEvaluatorOutputCategory = {
      ...defaultCategory,
      color: "",
      color_name: "",
    };

    testRender(
      <MetricPrompt
        category={categoryWithoutColor}
        data={{
          ...defaultData,
          [ProjectType.POINTWISE]: {
            ...defaultData[ProjectType.POINTWISE],
            output_categories: [categoryWithoutColor],
          },
        }}
        setData={mockSetData}
        categoryKey={categoryKey}
      />,
    );

    expect(screen.getByText("Select color")).toBeInTheDocument();
  });

  it("handles multiple category updates correctly", () => {
    const dataWithMultipleCategories: EvaluatorFormData = {
      ...defaultData,
      [ProjectType.POINTWISE]: {
        ...defaultData[ProjectType.POINTWISE],
        output_categories: [
          defaultCategory,
          {
            name: "Second Category",
            color: EvaluationStatusColor.BLUE,
            value: "2.0",
            color_name: "Blue",
          },
        ],
      },
    };

    testRender(
      <MetricPrompt
        category={defaultCategory}
        data={dataWithMultipleCategories}
        setData={mockSetData}
        categoryKey={0}
      />,
    );

    const nameInput = screen.getByDisplayValue("Test Category");
    fireEvent.change(nameInput, {
      target: { value: "Updated First Category" },
    });

    const setDataCallback = mockSetData.mock.calls[0][0];
    const result = setDataCallback(dataWithMultipleCategories);

    expect(result[ProjectType.POINTWISE].output_categories[0].name).toBe(
      "Updated First Category",
    );
    expect(result[ProjectType.POINTWISE].output_categories[1].name).toBe(
      "Second Category",
    ); // Unchanged
  });

  it("maintains other categories when deleting one category", () => {
    const dataWithMultipleCategories: EvaluatorFormData = {
      ...defaultData,
      [ProjectType.POINTWISE]: {
        ...defaultData[ProjectType.POINTWISE],
        output_categories: [
          defaultCategory,
          {
            name: "Second Category",
            color: EvaluationStatusColor.BLUE,
            value: "2.0",
            color_name: "Blue",
          },
        ],
      },
    };

    testRender(
      <MetricPrompt
        category={defaultCategory}
        data={dataWithMultipleCategories}
        setData={mockSetData}
        categoryKey={0}
      />,
    );

    const deleteButton = screen.getByRole("button");
    fireEvent.click(deleteButton);

    const setDataCallback = mockSetData.mock.calls[0][0];
    const result = setDataCallback(dataWithMultipleCategories);

    expect(result[ProjectType.POINTWISE].output_categories).toHaveLength(1);
    expect(result[ProjectType.POINTWISE].output_categories[0].name).toBe(
      "Second Category",
    );
  });
});

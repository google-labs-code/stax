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

import MetricPrompts from "@/app/(authRoutes)/evaluatorGallery/[id]/components/MetricPrompts";
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

describe("MetricPrompts", () => {
  const mockSetData = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    useParamsMock.mockReturnValue({});
  });

  it("renders the component with correct header and description", () => {
    testRender(<MetricPrompts data={defaultData} setData={mockSetData} />);
    expect(
      screen.getByText(
        "Define how rubric scores from the evaluator prompt are graded and colored in analytics.",
      ),
    ).toBeInTheDocument();
  });

  it("renders column headers with tooltips", () => {
    testRender(<MetricPrompts data={defaultData} setData={mockSetData} />);

    expect(screen.getByText("Rubric category")).toBeInTheDocument();
    expect(screen.getByText("Score mapping")).toBeInTheDocument();
    expect(screen.getByText("Score color")).toBeInTheDocument();

    // Check that info icons are present
    const infoIcons = screen.getAllByText("info");
    expect(infoIcons).toHaveLength(3);
  });

  it("renders MetricPrompt components for each category", () => {
    const dataWithMultipleCategories: EvaluatorFormData = {
      ...defaultData,
      [ProjectType.POINTWISE]: {
        ...defaultData[ProjectType.POINTWISE],
        output_categories: [
          defaultCategory,
          {
            name: "Second Category",
            color: EvaluationStatusColor.GREEN,
            value: "2.0",
            color_name: "Green",
          },
        ],
      },
    };

    testRender(
      <MetricPrompts data={dataWithMultipleCategories} setData={mockSetData} />,
    );

    // Should render both categories
    expect(screen.getByDisplayValue("Test Category")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Second Category")).toBeInTheDocument();
    expect(screen.getByDisplayValue("1.0")).toBeInTheDocument();
    expect(screen.getByDisplayValue("2.0")).toBeInTheDocument();
  });

  it("shows 'Add score' button when not in view mode", () => {
    testRender(<MetricPrompts data={defaultData} setData={mockSetData} />);

    const addButton = screen.getByRole("button", { name: /add score/i });
    expect(addButton).toBeInTheDocument();
    expect(addButton).toBeEnabled();
  });

  it("hides 'Add score' button when in view mode", () => {
    useParamsMock.mockReturnValue({ action: EvaluatorPageAction.VIEW });

    testRender(<MetricPrompts data={defaultData} setData={mockSetData} />);

    const addButton = screen.queryByRole("button", { name: /add score/i });
    expect(addButton).not.toBeInTheDocument();
  });

  it("adds a new category when 'Add score' button is clicked", () => {
    testRender(<MetricPrompts data={defaultData} setData={mockSetData} />);

    const addButton = screen.getByRole("button", { name: /add score/i });
    fireEvent.click(addButton);

    expect(mockSetData).toHaveBeenCalledWith(expect.any(Function));

    const setDataCallback = mockSetData.mock.calls[0][0];
    const result = setDataCallback(defaultData);

    expect(result[ProjectType.POINTWISE].output_categories).toHaveLength(2);
    expect(result[ProjectType.POINTWISE].output_categories[1]).toEqual({
      name: "",
      value: "",
      color: "",
      color_name: "",
    });
  });

  it("handles empty output_categories array", () => {
    const emptyData: EvaluatorFormData = {
      ...defaultData,
      [ProjectType.POINTWISE]: {
        ...defaultData[ProjectType.POINTWISE],
        output_categories: [],
      },
    };

    testRender(<MetricPrompts data={emptyData} setData={mockSetData} />);

    // Should still render the component structure
    expect(screen.getByText("Rubric category")).toBeInTheDocument();

    // Should not render any MetricPrompt components
    expect(screen.queryByDisplayValue("Test Category")).not.toBeInTheDocument();
  });

  it("passes correct props to MetricPrompt components", () => {
    const dataWithMultipleCategories: EvaluatorFormData = {
      ...defaultData,
      [ProjectType.POINTWISE]: {
        ...defaultData[ProjectType.POINTWISE],
        output_categories: [
          defaultCategory,
          {
            name: "Second Category",
            color: EvaluationStatusColor.GREEN,
            value: "2.0",
            color_name: "Green",
          },
        ],
      },
    };

    testRender(
      <MetricPrompts data={dataWithMultipleCategories} setData={mockSetData} />,
    );

    // Verify that both categories are rendered with their respective values
    expect(screen.getByDisplayValue("Test Category")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Second Category")).toBeInTheDocument();
    expect(screen.getByDisplayValue("1.0")).toBeInTheDocument();
    expect(screen.getByDisplayValue("2.0")).toBeInTheDocument();
  });

  it("maintains existing categories when adding new ones", () => {
    const dataWithMultipleCategories: EvaluatorFormData = {
      ...defaultData,
      [ProjectType.POINTWISE]: {
        ...defaultData[ProjectType.POINTWISE],
        output_categories: [
          defaultCategory,
          {
            name: "Second Category",
            color: EvaluationStatusColor.GREEN,
            value: "2.0",
            color_name: "Green",
          },
        ],
      },
    };

    testRender(
      <MetricPrompts data={dataWithMultipleCategories} setData={mockSetData} />,
    );

    const addButton = screen.getByRole("button", { name: /add score/i });
    fireEvent.click(addButton);

    const setDataCallback = mockSetData.mock.calls[0][0];
    const result = setDataCallback(dataWithMultipleCategories);

    expect(result[ProjectType.POINTWISE].output_categories).toHaveLength(3);
    expect(result[ProjectType.POINTWISE].output_categories[0]).toEqual(
      defaultCategory,
    );
    expect(result[ProjectType.POINTWISE].output_categories[1]).toEqual({
      name: "Second Category",
      color: EvaluationStatusColor.GREEN,
      value: "2.0",
      color_name: "Green",
    });
    expect(result[ProjectType.POINTWISE].output_categories[2]).toEqual({
      name: "",
      value: "",
      color: "",
      color_name: "",
    });
  });

  it("handles edit mode correctly", () => {
    useParamsMock.mockReturnValue({ action: EvaluatorPageAction.EDIT });

    testRender(<MetricPrompts data={defaultData} setData={mockSetData} />);

    // Should show the add button in edit mode
    const addButton = screen.getByRole("button", { name: /add score/i });
    expect(addButton).toBeInTheDocument();
  });

  it("handles duplicate mode correctly", () => {
    useParamsMock.mockReturnValue({ action: EvaluatorPageAction.DUPLICATE });

    testRender(<MetricPrompts data={defaultData} setData={mockSetData} />);

    // Should show the add button in duplicate mode
    const addButton = screen.getByRole("button", { name: /add score/i });
    expect(addButton).toBeInTheDocument();
  });
});

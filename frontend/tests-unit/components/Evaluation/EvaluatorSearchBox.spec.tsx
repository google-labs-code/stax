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

import EvaluatorSearchBox from "@/components/Evaluation/EvaluatorSearchBox";
import { Model } from "@/queries/types";
import { testRender } from "@/tests-unit/render";
import { EvaluatorType, LLMEvaluatorItem, Provider } from "@/types";
import { act, fireEvent, screen } from "@testing-library/react";

jest.mock(
  "@/app/(authRoutes)/evaluatorGallery/components/NewEvaluatorButton",
  () => {
    return {
      __esModule: true,
      default: () => <button>Add evaluator</button>,
    };
  },
);

jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: jest.fn(),
    replace: jest.fn(),
    prefetch: jest.fn(),
  }),
  usePathname: () => "/mock-path",
  useSearchParams: () => ({
    get: jest.fn(),
  }),
}));

describe("EvaluatorSearchBox", () => {
  const mockModel: Model = {
    id: "model-1",
    name: "Test Model",
    version: "1.0",
    label: "Test Model Label",
    url: "https://example.com/model",
    tag: "test",
    provider: Provider.OPENAI,
    model_type: "chat",
    properties: {},
    additional_headers: {},
  };

  const mockEvaluators: LLMEvaluatorItem[] = [
    {
      id: "eval-1",
      name: "Alpha",
      description: "First evaluator",
      model: mockModel,
      output_categories: [],
      output_format_type: "Json",
      prompts: [],
      type: EvaluatorType.USER,
      variables: [],
      created_at: "2024-01-01T00:00:00Z",
      updated_at: "2024-01-01T00:00:00Z",
    },
    {
      id: "eval-2",
      name: "Beta",
      description: "Second evaluator",
      model: mockModel,
      output_categories: [],
      output_format_type: "Json",
      prompts: [],
      type: EvaluatorType.SYSTEM,
      variables: [],
      created_at: "2024-01-02T00:00:00Z",
      updated_at: "2024-01-02T00:00:00Z",
    },
    {
      id: "eval-3",
      name: "Gamma",
      description: "Third evaluator",
      model: mockModel,
      output_categories: [],
      output_format_type: "Json",
      prompts: [],
      type: EvaluatorType.USER,
      variables: [],
      created_at: "2024-01-03T00:00:00Z",
      updated_at: "2024-01-03T00:00:00Z",
    },
  ];

  const defaultProps = {
    data: mockEvaluators,
    setData: jest.fn(),
    selectedCards: [],
    setSelectedCards: jest.fn(),
    onSelect: jest.fn(),
    initialData: mockEvaluators,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders input and search functionality", () => {
    testRender(<EvaluatorSearchBox {...defaultProps} />);
    expect(screen.getByPlaceholderText("Search Evaluator")).toBeInTheDocument();
  });

  it("filters options based on search input in non-page mode", async () => {
    testRender(<EvaluatorSearchBox {...defaultProps} />);

    const input = screen.getByPlaceholderText("Search Evaluator");

    await act(async () => {
      fireEvent.focus(input);
    });

    await act(async () => {
      fireEvent.change(input, { target: { value: "Alpha" } });
    });

    expect(screen.getByText("Alpha")).toBeInTheDocument();
    expect(screen.queryByText("Beta")).not.toBeInTheDocument();
  });

  it("calls onSelect when an option is selected", async () => {
    const onSelect = jest.fn();
    testRender(<EvaluatorSearchBox {...defaultProps} onSelect={onSelect} />);

    const input = screen.getByPlaceholderText("Search Evaluator");

    await act(async () => {
      fireEvent.focus(input);
    });

    await act(async () => {
      fireEvent.change(input, { target: { value: "Alpha" } });
    });

    await act(async () => {
      fireEvent.click(screen.getByText("Alpha"));
    });

    expect(onSelect).toHaveBeenCalledWith(
      "eval-1",
      "Alpha",
      expect.any(Function),
    );
  });

  it("renders search input and new evaluator button in page mode", () => {
    testRender(<EvaluatorSearchBox {...defaultProps} isPage={true} />);

    expect(screen.getByPlaceholderText("Search Evaluator")).toBeInTheDocument();
    expect(screen.getByText("Add evaluator")).toBeInTheDocument();
  });

  it("filters evaluators in page mode", async () => {
    const setData = jest.fn();
    testRender(
      <EvaluatorSearchBox {...defaultProps} isPage={true} setData={setData} />,
    );

    const input = screen.getByPlaceholderText("Search Evaluator");

    await act(async () => {
      fireEvent.change(input, { target: { value: "Alpha" } });
    });

    expect(setData).toHaveBeenCalled();
  });

  it("resets data when search is emptied in page mode", async () => {
    const setData = jest.fn();
    testRender(
      <EvaluatorSearchBox {...defaultProps} isPage={true} setData={setData} />,
    );

    const input = screen.getByPlaceholderText("Search Evaluator");

    await act(async () => {
      fireEvent.change(input, { target: { value: "Alpha" } });
      setData.mockClear();
      fireEvent.change(input, { target: { value: "" } });
    });

    expect(setData).toHaveBeenCalledWith(
      expect.arrayContaining(mockEvaluators),
    );
  });

  it("handles combobox dropdown opening on focus", async () => {
    testRender(<EvaluatorSearchBox {...defaultProps} />);

    const input = screen.getByPlaceholderText("Search Evaluator");

    await act(async () => {
      fireEvent.focus(input);
    });

    expect(screen.getByText("Alpha")).toBeInTheDocument();
    expect(screen.getByText("Beta")).toBeInTheDocument();
    expect(screen.getByText("Gamma")).toBeInTheDocument();
  });

  it("shows 'No scores found' when search has no results", async () => {
    testRender(<EvaluatorSearchBox {...defaultProps} />);

    const input = screen.getByPlaceholderText("Search Evaluator");

    await act(async () => {
      fireEvent.focus(input);
      fireEvent.change(input, { target: { value: "NonExistent" } });
    });

    expect(screen.getByText("No scores found")).toBeInTheDocument();
  });

  it("excludes already selected cards from dropdown options", async () => {
    testRender(
      <EvaluatorSearchBox
        {...defaultProps}
        selectedCards={[{ id: "eval-1", title: "Alpha" }]}
      />,
    );

    const input = screen.getByPlaceholderText("Search Evaluator");

    await act(async () => {
      fireEvent.focus(input);
    });

    expect(screen.queryByText("Alpha")).not.toBeInTheDocument();
    expect(screen.getByText("Beta")).toBeInTheDocument();
  });

  it("handles input change and maintains dropdown state", async () => {
    testRender(<EvaluatorSearchBox {...defaultProps} />);

    const input = screen.getByPlaceholderText("Search Evaluator");

    await act(async () => {
      fireEvent.focus(input);
      fireEvent.change(input, { target: { value: "Be" } });
    });

    expect(screen.getByText("Beta")).toBeInTheDocument();
    expect(screen.queryByText("Alpha")).not.toBeInTheDocument();
    expect(screen.queryByText("Gamma")).not.toBeInTheDocument();
  });

  it("opens dropdown on input focus", async () => {
    testRender(<EvaluatorSearchBox {...defaultProps} />);

    const input = screen.getByPlaceholderText("Search Evaluator");

    await act(async () => {
      fireEvent.focus(input);
    });

    expect(screen.getByText("Alpha")).toBeInTheDocument();
    expect(screen.getByText("Beta")).toBeInTheDocument();
    expect(screen.getByText("Gamma")).toBeInTheDocument();
  });
});

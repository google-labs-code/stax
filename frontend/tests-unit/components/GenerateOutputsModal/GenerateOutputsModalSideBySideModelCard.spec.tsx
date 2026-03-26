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

import GenerateOutputsModalSideBySideModelCard from "@/components/GenerateOutputsModal/GenerateOutputsModalSideBySideModelCard";
import { useModelsContext } from "@/hooks/useModelsContext";
import { Model } from "@/queries/types";
import { testRender } from "@/tests-unit/render";
import { Provider } from "@/types";
import { act, fireEvent, screen } from "@testing-library/react";

jest.mock("@/components/AccordionModelProviders", () => {
  return {
    __esModule: true,
    default: ({
      handleSelection,
      filteredOptions,
    }: {
      handleSelection: (modelId: string) => void;
      filteredOptions: Model[];
    }) => (
      <div data-testid="accordion-mock">
        {filteredOptions.map((model: Model) => (
          <button
            key={model.id}
            data-testid={`model-option-${model.id}`}
            onClick={() => handleSelection(model.id)}
          >
            Select {model.label}
          </button>
        ))}
      </div>
    ),
  };
});

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

const MockIcon = ({ size = 24 }: { size?: number }) => (
  <svg data-testid="fake-icon" width={size} height={size} />
);

const models: Model[] = [
  {
    id: "model-1",
    label: "Model One",
    icon: MockIcon,
    provider: Provider.OPENAI,
    properties: {
      max_tokens: undefined,
      n: undefined,
      temperature: undefined,
      top_p: undefined,
      top_k: undefined,
      seed: undefined,
    },
    name: "OPENAI",
    version: "",
    url: "",
    tag: "",
    model_type: "",
    additional_headers: {},
  },
  {
    id: "model-2",
    label: "Model Two",
    icon: MockIcon,
    provider: Provider.OPENAI,
    properties: {
      max_tokens: undefined,
      n: undefined,
      temperature: undefined,
      top_p: undefined,
      top_k: undefined,
      seed: undefined,
    },
    name: "",
    version: "",
    url: "",
    tag: "",
    model_type: "",
    additional_headers: {},
  },
];

describe("GenerateOutputsModalSideBySideModelCard", () => {
  beforeEach(() => {
    (useModelsContext as jest.Mock).mockReturnValue({
      allModels: models,
    });
  });

  it("renders with no model selected", () => {
    const mockSelect = jest.fn();

    testRender(
      <GenerateOutputsModalSideBySideModelCard
        model={{ properties: {} } as Model}
        label="A"
        onModelSelect={mockSelect}
      />,
    );

    expect(screen.getByText("A")).toBeInTheDocument();
    expect(screen.getByText("Model")).toBeInTheDocument();
    expect(screen.getByText("Select or search model")).toBeInTheDocument();
  });

  it("renders with a model selected", () => {
    const mockSelect = jest.fn();

    testRender(
      <GenerateOutputsModalSideBySideModelCard
        model={models[0]}
        label="B"
        onModelSelect={mockSelect}
      />,
    );

    expect(screen.getByText("Model One")).toBeInTheDocument();
    expect(screen.getByTestId("fake-icon")).toBeInTheDocument();
  });

  it("handles model selection from dropdown", async () => {
    const mockSelect = jest.fn();

    testRender(
      <GenerateOutputsModalSideBySideModelCard
        model={{ properties: {} } as Model}
        label="A"
        onModelSelect={mockSelect}
      />,
    );

    const dropdownTrigger = screen.getByText("Select or search model");
    expect(dropdownTrigger).toBeInTheDocument();

    await act(async () => {
      fireEvent.click(dropdownTrigger);
    });

    expect(screen.getByTestId("accordion-mock")).toBeInTheDocument();

    await act(async () => {
      fireEvent.click(screen.getByTestId("model-option-model-1"));
    });

    expect(mockSelect).toHaveBeenCalledWith(models[0]);
  });
});

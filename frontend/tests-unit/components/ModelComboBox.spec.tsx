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

import ModelCombobox from "@/components/ModelComboBox";
import { getProviders } from "@/config/constants";
import { useModelsContext } from "@/hooks/useModelsContext";
import { ModelTypeEnum } from "@/queries/types";
import { testRender } from "@/tests-unit/render";
import { ModelProvider, Provider } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/config/constants", () => ({
  getProviders: jest.fn(),
}));

const mockModels = [
  {
    id: "1",
    name: "model-one",
    version: "1.0",
    label: "Model One",
    url: "https://example.com/model-one",
    tag: "test",
    properties: {},
    provider: Provider.OPENAI,
    model_type: ModelTypeEnum.SYSTEM,
    icon: undefined,
    latency: undefined,
    additional_headers: {},
  },
  {
    id: "2",
    name: "model-two",
    version: "1.0",
    label: "Model Two",
    url: "https://example.com/model-two",
    tag: "test",
    properties: {},
    provider: Provider.ANTHROPIC,
    model_type: ModelTypeEnum.SYSTEM,
    icon: undefined,
    latency: undefined,
    additional_headers: {},
  },
];

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

describe("ModelComboBox", () => {
  beforeEach(() => {
    jest.clearAllMocks();

    (useModelsContext as jest.Mock).mockReturnValue({
      allModels: mockModels,
    });

    (getProviders as jest.Mock).mockReturnValue([
      {
        key: Provider.OPENAI,
        name: Provider.OPENAI,
        icon: <div data-testid="openai-icon">OpenAI</div>,
        accordionName: "OpenAI",
      },
      {
        key: Provider.ANTHROPIC,
        name: Provider.ANTHROPIC,
        icon: <div data-testid="anthropic-icon">Anthropic</div>,
        accordionName: "Antropic",
      },
    ] as ModelProvider[]);
  });

  it("renders with placeholder", () => {
    testRender(
      <ModelCombobox
        onSelect={jest.fn()}
        isExpanded={false}
        isLoadingModels={false}
      />,
    );
    expect(
      screen.getByPlaceholderText("Select or search model"),
    ).toBeInTheDocument();
  });

  it("shows loading state", () => {
    testRender(
      <ModelCombobox
        onSelect={jest.fn()}
        isExpanded={false}
        isLoadingModels={true}
      />,
    );
    expect(screen.getByPlaceholderText("Loading...")).toBeInTheDocument();
  });

  it("opens dropdown and filters options when typing", async () => {
    testRender(
      <ModelCombobox
        onSelect={jest.fn()}
        isExpanded={false}
        isLoadingModels={false}
      />,
    );

    const input = screen.getByPlaceholderText("Select or search model");
    fireEvent.click(input);

    expect(screen.getByTestId("model-accordion")).toBeInTheDocument();

    fireEvent.change(input, { target: { value: "One" } });
    expect(input).toHaveValue("One");
  });

  it("selects a model when option is clicked", async () => {
    const onSelect = jest.fn();
    testRender(
      <ModelCombobox
        onSelect={onSelect}
        isExpanded={false}
        isLoadingModels={false}
      />,
    );

    fireEvent.click(screen.getByPlaceholderText("Select or search model"));

    fireEvent.click(screen.getAllByTestId("accordion-model-option-0")[0]);

    expect(onSelect).toHaveBeenCalledWith(mockModels[0]);
  });

  it("respects openedModel prop changes", async () => {
    const onSelect = jest.fn();

    const { rerender } = testRender(
      <ModelCombobox
        onSelect={onSelect}
        isExpanded={false}
        isLoadingModels={false}
      />,
    );

    expect(screen.getByPlaceholderText("Select or search model")).toHaveValue(
      "",
    );

    rerender(
      <ModelCombobox
        onSelect={onSelect}
        isExpanded={false}
        isLoadingModels={false}
        openedModel={mockModels[0]}
      />,
    );

    expect(screen.getByDisplayValue("Model One")).toBeInTheDocument();

    rerender(
      <ModelCombobox
        onSelect={onSelect}
        isExpanded={false}
        isLoadingModels={false}
        openedModel={mockModels[1]}
      />,
    );

    expect(screen.getByDisplayValue("Model Two")).toBeInTheDocument();

    rerender(
      <ModelCombobox
        onSelect={onSelect}
        isExpanded={false}
        isLoadingModels={false}
        openedModel={null}
      />,
    );

    expect(screen.getByPlaceholderText("Select or search model")).toHaveValue(
      "",
    );
  });

  it("respects closeDropdownTrigger prop", async () => {
    const setDropdownTrigger = jest.fn();

    const { rerender } = testRender(
      <ModelCombobox
        onSelect={jest.fn()}
        isExpanded={false}
        isLoadingModels={false}
        closeDropdownTrigger={false}
        setDropdownTrigger={setDropdownTrigger}
      />,
    );

    fireEvent.click(screen.getByPlaceholderText("Select or search model"));

    rerender(
      <ModelCombobox
        onSelect={jest.fn()}
        isExpanded={false}
        isLoadingModels={false}
        closeDropdownTrigger={true}
        setDropdownTrigger={setDropdownTrigger}
      />,
    );

    expect(setDropdownTrigger).toHaveBeenCalledWith(false);
  });

  it("disables input when isDisabled is true", () => {
    testRender(
      <ModelCombobox
        onSelect={jest.fn()}
        isExpanded={false}
        isLoadingModels={false}
        isDisabled={true}
      />,
    );
    expect(screen.getByRole("textbox")).toBeDisabled();
  });

  it("shows loading indicator in right section when loading", () => {
    testRender(
      <ModelCombobox
        onSelect={jest.fn()}
        isExpanded={false}
        isLoadingModels={true}
      />,
    );

    const loaderElement = document.querySelector(".mantine-Loader-root");
    expect(loaderElement).toBeInTheDocument();
  });
});

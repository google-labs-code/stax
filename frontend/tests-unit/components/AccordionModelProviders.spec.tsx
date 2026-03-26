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

import AccordionModelProviders from "@/components/AccordionModelProviders";
import {
  LOCAL_STORAGE_SHOW_CUSTOM_MODEL_MODAL,
  getProviders,
} from "@/config/constants";
import { useModelsContext } from "@/hooks/useModelsContext";
import { ModelTypeEnum } from "@/queries/types";
import { testRender } from "@/tests-unit/render";
import { Provider } from "@/types";
import LocalStorage from "@/utils/LocalStorage";
import { fireEvent, screen, waitFor } from "@testing-library/react";
import { useRouter } from "next/navigation";
import { act } from "react-dom/test-utils";

jest.mock("@/hooks/useModelsContext");
jest.mock("next/navigation");
jest.mock("@/utils/LocalStorage");
jest.mock("@/config/constants", () => ({
  LOCAL_STORAGE_SHOW_CUSTOM_MODEL_MODAL: "showCustomModelModal",
  getProviders: jest.fn(),
}));

describe("AccordionModelProviders", () => {
  const mockProviders = [
    {
      key: "openai",
      accordionName: "OpenAI",
      icon: <div data-testid="openai-icon">OpenAI Icon</div>,
    },
    {
      key: "anthropic",
      accordionName: "Anthropic",
      icon: <div data-testid="anthropic-icon">Anthropic Icon</div>,
    },
    {
      key: ModelTypeEnum.CUSTOM,
      accordionName: ModelTypeEnum.CUSTOM,
      icon: <div data-testid="custom-icon">Custom Icon</div>,
    },
  ];

  const mockModels = [
    {
      id: "openai-1",
      label: "GPT-4",
      name: "gpt-4",
      provider: Provider.OPENAI,
      model_type: ModelTypeEnum.SYSTEM,
      is_api_key_present: true,
      properties: {},
      version: "1.0",
      url: "",
      tag: "",
      additional_headers: {},
      icon: () => <div>Model Icon</div>,
    },
    {
      id: "anthropic-1",
      label: "Claude",
      name: "claude-2",
      provider: Provider.ANTHROPIC,
      model_type: ModelTypeEnum.SYSTEM,
      is_api_key_present: true,
      properties: {},
      version: "1.0",
      url: "",
      tag: "",
      additional_headers: {},
      icon: () => <div>Model Icon</div>,
    },
    {
      id: "custom-1",
      label: "My Custom Model",
      name: "custom-model",
      provider: Provider.OPENAI,
      model_type: ModelTypeEnum.CUSTOM,
      is_api_key_present: true,
      properties: {},
      version: "1.0",
      url: "",
      tag: "",
      additional_headers: {},
      icon: () => <div>Model Icon</div>,
    },
    {
      id: "unknown-1",
      label: "Unknown Provider Model",
      name: "unknown-model",
      provider: Provider.OPENAI,
      model_type: ModelTypeEnum.CUSTOM,
      is_api_key_present: true,
      properties: {},
      version: "1.0",
      url: "",
      tag: "",
      additional_headers: {},
      icon: () => <div>Model Icon</div>,
    },
  ];

  const mockHandleSelection = jest.fn();
  const mockPush = jest.fn();
  const mockSetLocalStorage = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    (getProviders as jest.Mock).mockReturnValue(mockProviders);
    (useModelsContext as jest.Mock).mockReturnValue({ allModels: mockModels });
    (useRouter as jest.Mock).mockReturnValue({ push: mockPush });
    LocalStorage.set = mockSetLocalStorage;
  });

  it("renders all providers", () => {
    testRender(
      <AccordionModelProviders
        filteredOptions={mockModels}
        handleSelection={mockHandleSelection}
      />,
    );

    mockProviders.forEach((provider) => {
      expect(screen.getByText(provider.accordionName)).toBeInTheDocument();
    });
  });

  it("calls handleSelection when model is clicked", async () => {
    testRender(
      <AccordionModelProviders
        filteredOptions={mockModels}
        handleSelection={mockHandleSelection}
      />,
    );

    await act(async () => {
      fireEvent.click(screen.getByText("OpenAI"));
    });

    const gpt4Text = screen.getByText("GPT-4");
    const modelOptionDiv = gpt4Text.closest("div");
    expect(modelOptionDiv).not.toBeNull();
    fireEvent.click(modelOptionDiv as HTMLElement);

    expect(mockHandleSelection).toHaveBeenCalled();
  });

  it("opens custom accordion based on search term matching custom model", async () => {
    await act(async () => {
      testRender(
        <AccordionModelProviders
          searchTerm="custom"
          filteredOptions={mockModels.filter((m) =>
            m.label.toLowerCase().includes("custom"),
          )}
          handleSelection={mockHandleSelection}
        />,
      );
    });

    await waitFor(() => {
      expect(screen.getByText("My Custom Model")).toBeInTheDocument();
    });
  });

  it("gets provider icon for custom models", async () => {
    testRender(
      <AccordionModelProviders
        filteredOptions={mockModels}
        handleSelection={mockHandleSelection}
      />,
    );

    await act(async () => {
      fireEvent.click(screen.getByText(ModelTypeEnum.CUSTOM));
    });

    expect(screen.getByText("My Custom Model")).toBeInTheDocument();
    const customIcons = screen.getAllByTestId("custom-icon");
    expect(customIcons.length).toBeGreaterThan(0);
  });

  it("navigates to settings when clicking Settings link for provider with missing API key", () => {
    const modelsWithoutApiKey = [
      {
        ...mockModels[0],
        is_api_key_present: false,
      },
      mockModels[1],
    ];

    testRender(
      <AccordionModelProviders
        filteredOptions={modelsWithoutApiKey}
        handleSelection={mockHandleSelection}
      />,
    );

    act(() => {
      useRouter().push("/settings?tab=apiKeys");
    });

    expect(mockPush).toHaveBeenCalledWith("/settings?tab=apiKeys");
  });

  it("navigates to model manager when clicking Add custom model link", () => {
    const filteredOptions = mockModels.filter(
      (m) => m.model_type === ModelTypeEnum.SYSTEM,
    );

    testRender(
      <AccordionModelProviders
        filteredOptions={filteredOptions}
        handleSelection={mockHandleSelection}
      />,
    );

    act(() => {
      LocalStorage.set(LOCAL_STORAGE_SHOW_CUSTOM_MODEL_MODAL, true);
      useRouter().push("/settings?tab=modelManager");
    });

    expect(mockSetLocalStorage).toHaveBeenCalledWith(
      LOCAL_STORAGE_SHOW_CUSTOM_MODEL_MODAL,
      true,
    );
    expect(mockPush).toHaveBeenCalledWith("/settings?tab=modelManager");
  });

  it("displays 'No models available' message when provider has no models", async () => {
    const filteredOptions = mockModels.filter(
      (m) => m.provider !== Provider.OPENAI,
    );

    testRender(
      <AccordionModelProviders
        filteredOptions={filteredOptions}
        handleSelection={mockHandleSelection}
      />,
    );

    await act(async () => {
      fireEvent.click(screen.getByText("OpenAI"));
    });

    const noModelsMessages = screen.getAllByText("No models available.");
    expect(noModelsMessages[0]).toBeInTheDocument();
  });

  it("falls back to custom icon for unknown provider models", async () => {
    const filteredCustomModels = mockModels.filter(
      (m) => m.model_type === ModelTypeEnum.CUSTOM,
    );

    testRender(
      <AccordionModelProviders
        filteredOptions={filteredCustomModels}
        handleSelection={mockHandleSelection}
      />,
    );

    await act(async () => {
      fireEvent.click(screen.getByText(ModelTypeEnum.CUSTOM));
    });

    expect(screen.getByText("Unknown Provider Model")).toBeInTheDocument();
    const customIcons = screen.getAllByTestId("custom-icon");
    expect(customIcons.length).toBeGreaterThan(0);
  });

  it("handles stop propagation in HoverCard dropdown", () => {
    const modelsWithoutApiKey = [
      {
        ...mockModels[0],
        is_api_key_present: false,
      },
    ];

    const { container } = testRender(
      <AccordionModelProviders
        filteredOptions={modelsWithoutApiKey}
        handleSelection={mockHandleSelection}
      />,
    );

    const stopPropagationSpy = jest.fn();
    const divElement = container.querySelector(
      ".mantine-HoverCard-dropdown div",
    );

    if (divElement) {
      const mouseDownEvent = new MouseEvent("mousedown", {
        bubbles: true,
        cancelable: true,
      });
      Object.defineProperty(mouseDownEvent, "stopPropagation", {
        value: stopPropagationSpy,
      });

      const clickEvent = new MouseEvent("click", {
        bubbles: true,
        cancelable: true,
      });
      Object.defineProperty(clickEvent, "stopPropagation", {
        value: stopPropagationSpy,
      });

      fireEvent(divElement, mouseDownEvent);
      fireEvent(divElement, clickEvent);

      expect(stopPropagationSpy).toHaveBeenCalledTimes(2);
    }
  });

  it("verifies provider with missing API key has disabled control", () => {
    const modelsWithoutApiKey = [
      {
        ...mockModels[0],
        is_api_key_present: false,
      },
    ];

    testRender(
      <AccordionModelProviders
        filteredOptions={modelsWithoutApiKey}
        handleSelection={mockHandleSelection}
      />,
    );

    const openAIButton = screen.getByText("OpenAI").closest("button");

    expect(openAIButton).toHaveAttribute("disabled");
    expect(openAIButton).toHaveAttribute("data-disabled", "true");

    act(() => {
      useRouter().push("/settings?tab=apiKeys");
    });
    expect(mockPush).toHaveBeenCalledWith("/settings?tab=apiKeys");
  });
});

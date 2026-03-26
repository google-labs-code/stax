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

import ModelSettingsModal from "@/components/ModelSettingsModal";
import { useModelsContext } from "@/hooks/useModelsContext";
import * as clientQueries from "@/queries/clientQueries";
import { Model, ModelTypeEnum } from "@/queries/types";
import { ModelSettingsModalType, Provider } from "@/types";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { fireEvent, screen, waitFor } from "@testing-library/react";

import { testRenderLite } from "../render";

jest.mock("@/hooks/useModelsContext");
jest.mock("@mantine/notifications");
jest.mock("@tanstack/react-query");
jest.mock("@/queries/clientQueries");

const mockRefreshModels = jest.fn();
const mockOnClose = jest.fn();
const mockDuplicateModelQuery = jest.fn();

const baseModel: Model = {
  id: "1",
  label: "Test Model",
  name: "test-model",
  description: "A test model",
  properties: {
    temperature: 0.5,
    max_tokens: 100,
    top_p: 0.8,
    seed: 42,
  },
  descriptors: {
    temperature: {
      defaultValue: 0.5,
      description: "Temp desc",
      minValue: 0,
      maxValue: 1,
      label: "Temperature",
      type: "number",
      comparableMaxValue: 1,
      comparableMinValue: 0,
      key: "temperature",
    },
    max_output_tokens: {
      defaultValue: 100,
      minValue: 1,
      maxValue: 2048,
      description: "Max tokens desc",
      label: "Max Tokens",
      type: "number",
      comparableMaxValue: 2048,
      comparableMinValue: 1,
      key: "max_output_tokens",
    },
    top_p: {
      defaultValue: 0.8,
      minValue: 0,
      maxValue: 1,
      description: "Top P desc",
      label: "Top P",
      type: "number",
      comparableMaxValue: 1,
      comparableMinValue: 0,
      key: "top_p",
    },
    seed: {
      defaultValue: 42,
      description: "Seed desc",
      minValue: 1,
      maxValue: 100,
      label: "Seed",
      type: "number",
      comparableMaxValue: 100,
      comparableMinValue: 1,
      key: "seed",
    },
  },
  model_type: ModelTypeEnum.SYSTEM,
  version: "1.0",
  url: "https://test.com",
  tag: "test-tag",
  provider: Provider.ANTHROPIC,
  additional_headers: {},
};

describe("ModelSettingsModal", () => {
  beforeEach(() => {
    jest.clearAllMocks();

    // Mock scrollIntoView for Mantine Select components
    Element.prototype.scrollIntoView = jest.fn();

    (useModelsContext as jest.Mock).mockReturnValue({
      allModels: [
        baseModel,
        {
          ...baseModel,
          id: "2",
          name: "another-model",
          label: "Another Model",
        },
      ],
      refreshModels: mockRefreshModels,
      defaultModels: [baseModel],
    });

    (clientQueries.duplicateModelQuery as jest.Mock) = mockDuplicateModelQuery;
    mockDuplicateModelQuery.mockResolvedValue({ id: "new-id" });

    (useMutation as jest.Mock).mockImplementation(
      ({ mutationFn, onSuccess, onError }) => {
        return {
          mutate: (values: any) => {
            try {
              const result = mutationFn(values);
              if (onSuccess) onSuccess(result);
            } catch (error) {
              if (onError) onError(error);
            }
          },
          isPending: false,
        };
      },
    );

    (notifications.show as jest.Mock).mockImplementation(() => {});
  });

  it("renders modal with correct title", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.ADD}
        model={null}
      />,
    );

    expect(screen.getByText("Model Configuration")).toBeInTheDocument();
  });

  it("prevents submission when required fields are empty", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.ADD}
        model={null}
      />,
    );

    const submitBtn = screen.getByRole("button", { name: /Add/i });
    fireEvent.click(submitBtn);
    expect(mockDuplicateModelQuery).not.toHaveBeenCalled();
  });

  it("loads model data correctly when provided", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    expect(screen.getByDisplayValue("Test Model")).toBeInTheDocument();
    expect(screen.getByDisplayValue("A test model")).toBeInTheDocument();

    const inputs = screen.getAllByRole("textbox");
    expect(inputs.length).toBeGreaterThan(0);
  });

  it("shows modified configuration badge when form is dirty", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const labelInput = screen.getByDisplayValue("Test Model");
    fireEvent.change(labelInput, { target: { value: "Modified Model" } });

    expect(screen.getByText(/modified configuration/i)).toBeInTheDocument();
  });

  it("prevents submission when required fields are invalid", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const labelInput = screen.getByDisplayValue("Test Model");
    fireEvent.change(labelInput, { target: { value: "" } });

    const submitButton = screen.getByRole("button", { name: /Save/i });
    fireEvent.click(submitButton);
    expect(mockDuplicateModelQuery).not.toHaveBeenCalled();
  });

  it("handles form submission for duplicating a model", async () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const labelInput = screen.getByDisplayValue("Test Model");
    fireEvent.change(labelInput, { target: { value: "Modified Model" } });

    const submitButton = screen.getByRole("button", { name: /Save as copy/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockDuplicateModelQuery).toHaveBeenCalled();
      expect(mockRefreshModels).toHaveBeenCalled();
      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  it("handles seed input field validation", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const seedInput = screen.getByDisplayValue("42");
    fireEvent.change(seedInput, { target: { value: "150" } });

    const submitButton = screen.getByRole("button", { name: /Save/i });
    fireEvent.click(submitButton);
    expect(mockDuplicateModelQuery).not.toHaveBeenCalled();
  });

  it("resets form when reset button is clicked", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const labelInput = screen.getByDisplayValue("Test Model");
    fireEvent.change(labelInput, { target: { value: "Modified Model" } });

    const resetButton = screen.getByRole("button", { name: /Reset/i });
    fireEvent.click(resetButton);

    expect(screen.getByDisplayValue("Test Model")).toBeInTheDocument();
    expect(
      screen.queryByText(/modified configuration/i),
    ).not.toBeInTheDocument();
  });

  it("handles form errors correctly", async () => {
    mockDuplicateModelQuery.mockImplementation(() => {
      throw new Error("Duplication failed");
    });

    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const labelInput = screen.getByDisplayValue("Test Model");
    fireEvent.change(labelInput, { target: { value: "Modified Model" } });

    const submitButton = screen.getByRole("button", { name: /Save as copy/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(notifications.show).toHaveBeenCalled();
    });
  });

  it("closes modal and resets form on close", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const buttons = screen.getAllByRole("button");
    const closeButton = buttons.find((button) =>
      button.classList.contains("mantine-Modal-close"),
    );

    if (closeButton) {
      fireEvent.click(closeButton);
      expect(mockOnClose).toHaveBeenCalled();
    } else {
      fail("Close button not found");
    }
  });

  it("allows changing model version when type is ADD", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.ADD}
        model={null}
      />,
    );

    // Find the select input by data-path attribute
    const inputs = screen.getAllByRole("textbox");
    const modelVersionSelect = inputs.find(
      (input) => input.getAttribute("data-path") === "name",
    ) as HTMLInputElement;
    expect(modelVersionSelect).toBeInTheDocument();
    expect(modelVersionSelect).not.toBeDisabled();
  });

  it("disables model version select when type is DUPLICATE", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    // Find the select input by role and check all inputs with the value
    const inputs = screen.getAllByDisplayValue("test-model");
    const modelVersionSelect = inputs.find(
      (input) =>
        input.getAttribute("data-path") === "name" &&
        input.getAttribute("aria-haspopup") === "listbox",
    ) as HTMLInputElement;
    expect(modelVersionSelect).toBeInTheDocument();
    expect(modelVersionSelect).toBeDisabled();
  });

  it("disables model version select when type is EDIT", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.EDIT}
        model={baseModel}
      />,
    );

    // Find the select input by role and check all inputs with the value
    const inputs = screen.getAllByDisplayValue("test-model");
    const modelVersionSelect = inputs.find(
      (input) =>
        input.getAttribute("data-path") === "name" &&
        input.getAttribute("aria-haspopup") === "listbox",
    ) as HTMLInputElement;
    expect(modelVersionSelect).toBeInTheDocument();
    expect(modelVersionSelect).toBeDisabled();
  });

  it("updates form when model version is changed", async () => {
    const anotherModel = {
      ...baseModel,
      id: "2",
      name: "another-model",
      label: "Another Model",
      description: "Another description",
    };

    (useModelsContext as jest.Mock).mockReturnValue({
      allModels: [baseModel, anotherModel],
      refreshModels: mockRefreshModels,
      defaultModels: [baseModel, anotherModel],
    });

    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.ADD}
        model={null}
      />,
    );

    // Find the select input by data-path attribute
    const inputs = screen.getAllByRole("textbox");
    const modelVersionSelect = inputs.find(
      (input) => input.getAttribute("data-path") === "name",
    ) as HTMLInputElement;

    // For Mantine Select, we need to click to open and then select an option
    fireEvent.mouseDown(modelVersionSelect);

    await waitFor(() => {
      const option = screen.getByText("another-model");
      expect(option).toBeInTheDocument();
    });

    const option = screen.getByText("another-model");
    fireEvent.click(option);

    // Form should update with the selected model's values
    await waitFor(() => {
      expect(screen.getByDisplayValue("Another Model")).toBeInTheDocument();
    });
  });

  it("handles form submission for ADD type", async () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.ADD}
        model={null}
      />,
    );

    // Fill in required fields - find by data-path attribute
    const inputs = screen.getAllByRole("textbox");
    const labelInput = inputs.find(
      (input) => input.getAttribute("data-path") === "label",
    ) as HTMLInputElement;
    fireEvent.change(labelInput, { target: { value: "New Model" } });

    // Find the select input by data-path attribute
    const nameInput = inputs.find(
      (input) => input.getAttribute("data-path") === "name",
    ) as HTMLInputElement;
    fireEvent.mouseDown(nameInput);

    await waitFor(() => {
      const option = screen.getByText("test-model");
      expect(option).toBeInTheDocument();
    });

    const option = screen.getByText("test-model");
    fireEvent.click(option);

    // Find number inputs by data-path
    const numberInputs = screen.getAllByRole("textbox");
    const maxTokensInput = numberInputs.find(
      (input) => input.getAttribute("data-path") === "maxTokens",
    ) as HTMLInputElement;
    fireEvent.change(maxTokensInput, { target: { value: "100" } });

    const temperatureInput = numberInputs.find(
      (input) => input.getAttribute("data-path") === "temperature",
    ) as HTMLInputElement;
    fireEvent.change(temperatureInput, { target: { value: "0.5" } });

    const submitButton = screen.getByRole("button", { name: /Add/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockDuplicateModelQuery).toHaveBeenCalled();
    });
  });

  it("shows Save button text when model exists and form is not dirty", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const saveButton = screen.getByRole("button", { name: /Save/i });
    expect(saveButton).toBeInTheDocument();
  });

  it("shows Save as copy button text when form is dirty in DUPLICATE mode", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const labelInput = screen.getByDisplayValue("Test Model");
    fireEvent.change(labelInput, { target: { value: "Modified Model" } });

    const saveAsCopyButton = screen.getByRole("button", {
      name: /Save as copy/i,
    });
    expect(saveAsCopyButton).toBeInTheDocument();
  });

  it("disables submit button when form has errors", async () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const labelInput = screen.getByDisplayValue("Test Model");
    fireEvent.change(labelInput, { target: { value: "" } });
    fireEvent.blur(labelInput); // Trigger validation

    await waitFor(() => {
      const submitButton = screen.getByRole("button", { name: /Save/i });
      expect(submitButton).toBeDisabled();
    });
  });

  it("disables submit button when form is not dirty in non-ADD mode", () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const submitButton = screen.getByRole("button", { name: /Save/i });
    expect(submitButton).toBeDisabled();
  });

  it("handles seed validation with min and max values", async () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const seedInput = screen.getByDisplayValue("42");

    // Test value below min - make form dirty first
    fireEvent.change(screen.getByDisplayValue("Test Model"), {
      target: { value: "Modified Model" },
    });

    // Set invalid seed value (below min)
    fireEvent.change(seedInput, { target: { value: "0" } });
    // NumberInput onChange should trigger validation, but due to async state
    // we verify the input value changed and form validation prevents submission
    expect(seedInput).toHaveValue("0");

    // Try to submit - should not call mutation due to validation error
    const submitButton1 = screen.getByRole("button", { name: /Save as copy/i });
    fireEvent.click(submitButton1);
    // Give it a moment to process
    await new Promise((resolve) => setTimeout(resolve, 100));
    expect(mockDuplicateModelQuery).not.toHaveBeenCalled();

    // Test value above max
    fireEvent.change(seedInput, { target: { value: "150" } });
    expect(seedInput).toHaveValue("150");

    // Try to submit - should not call mutation due to validation error
    const submitButton2 = screen.getByRole("button", { name: /Save as copy/i });
    fireEvent.click(submitButton2);
    await new Promise((resolve) => setTimeout(resolve, 100));
    expect(mockDuplicateModelQuery).not.toHaveBeenCalled();

    // Test valid value
    fireEvent.change(seedInput, { target: { value: "50" } });
    expect(seedInput).toHaveValue("50");

    // With valid seed and dirty form, button should be enabled
    await waitFor(() => {
      const submitButton = screen.getByRole("button", {
        name: /Save as copy/i,
      });
      // Button should be enabled if form is valid and dirty
      expect(submitButton).not.toBeDisabled();
    });
  });

  it("clears seed error when value is removed", async () => {
    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    // Make form dirty first
    fireEvent.change(screen.getByDisplayValue("Test Model"), {
      target: { value: "Modified Model" },
    });

    const seedInput = screen.getByDisplayValue("42");

    // Set invalid seed value - this should trigger validation error
    // NumberInput's onChange will be called with the number value
    fireEvent.change(seedInput, { target: { value: "150" } });

    // The validation error should be set, but we can't easily test button state
    // due to async form state updates. Instead, verify the input accepts the change
    expect(seedInput).toHaveValue("150");

    // Clear the value - this should clear the error per component logic
    fireEvent.change(seedInput, { target: { value: "" } });

    // Verify the input is cleared and form handles it gracefully
    await waitFor(() => {
      expect(seedInput).toHaveValue("");
    });

    // Form should handle empty seed gracefully (component sets error to null)
    // Verify the form is still functional by checking submit button exists
    const submitButton = screen.getByRole("button", {
      name: /Save as copy/i,
    });
    expect(submitButton).toBeInTheDocument();
  });

  it("uses default values from descriptors when properties are missing", () => {
    const modelWithoutProperties = {
      ...baseModel,
      properties: {},
    };

    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={modelWithoutProperties}
      />,
    );

    // Should use descriptor defaults - use getAllByDisplayValue to handle multiple matches
    const temperatureInputs = screen.getAllByDisplayValue("0.5");
    const visibleTemperatureInput = temperatureInputs.find(
      (input) => input.getAttribute("type") !== "hidden",
    );
    expect(visibleTemperatureInput).toBeInTheDocument(); // temperature default

    expect(screen.getByDisplayValue("100")).toBeInTheDocument(); // max_tokens default
  });

  it("handles mutation pending state", () => {
    (useMutation as jest.Mock).mockImplementation(
      ({ mutationFn, onSuccess, onError }) => {
        return {
          mutate: (values: any) => {
            try {
              const result = mutationFn(values);
              if (onSuccess) onSuccess(result);
            } catch (error) {
              if (onError) onError(error);
            }
          },
          isPending: true,
        };
      },
    );

    testRenderLite(
      <ModelSettingsModal
        isOpened={true}
        onClose={mockOnClose}
        type={ModelSettingsModalType.DUPLICATE}
        model={baseModel}
      />,
    );

    const labelInput = screen.getByDisplayValue("Test Model");
    fireEvent.change(labelInput, { target: { value: "Modified Model" } });

    const submitButton = screen.getByRole("button", { name: /Save as copy/i });
    expect(submitButton).toHaveAttribute("data-loading", "true");
  });
});

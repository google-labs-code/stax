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

import EditDatasetModal from "@/app/(authRoutes)/datasets/components/EditDatasetModal";
import { Dataset } from "@/app/(authRoutes)/datasets/types";
import * as clientQueries from "@/queries/clientQueries";
import { useMutation } from "@tanstack/react-query";
import "@testing-library/jest-dom";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

// Mock dependencies
jest.mock("@/components/ModalFooter", () => {
  return function MockModalFooterButton({
    onClick,
    label,
    isLoading,
    isDisabled,
    tooltipLabel,
  }: any) {
    return (
      <button
        data-testid="modal-footer-button"
        onClick={onClick}
        disabled={isDisabled}
        data-loading={isLoading}
        data-tooltip={tooltipLabel}
      >
        {label}
      </button>
    );
  };
});

jest.mock("@/components/ModalInputLabel", () => {
  return function MockModalInputLabel({ label, isRequired }: any) {
    return (
      <label
        data-testid={`label-${label.toLowerCase()}`}
        data-required={isRequired}
      >
        {label}
        {isRequired && <span>*</span>}
      </label>
    );
  };
});

jest.mock("@/config/constants", () => ({
  TOOLTIPS: {
    DATASET_NAME_REQUIRED_MESSAGE: "Dataset name is required",
  },
}));

jest.mock("@/queries/clientQueries", () => ({
  updateDatasetQuery: jest.fn(),
}));

jest.mock("@mantine/core", () => ({
  Modal: ({ children, opened, onClose, title, ...props }: any) => {
    if (!opened) return null;

    return (
      <div data-testid="modal" {...props}>
        <div data-testid="modal-header">{title}</div>
        <div data-testid="modal-content">{children}</div>
        <button data-testid="modal-close-button" onClick={onClose}>
          Close
        </button>
      </div>
    );
  },
  Stack: ({ children, ...props }: any) => (
    <div data-testid="stack" {...props}>
      {children}
    </div>
  ),
  Group: ({ children, ...props }: any) => (
    <div data-testid="group" {...props}>
      {children}
    </div>
  ),
  Text: ({ children, ...props }: any) => (
    <span data-testid="text" {...props}>
      {children}
    </span>
  ),
  TextInput: ({ value, onChange, placeholder, error, ...props }: any) => (
    <input
      data-testid="text-input"
      type="text"
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      data-error={error}
      {...props}
    />
  ),
  Textarea: ({ value, onChange, placeholder, ...props }: any) => (
    <textarea
      data-testid="textarea"
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      {...props}
    />
  ),
}));

jest.mock("@tanstack/react-query", () => ({
  useMutation: jest.fn(),
}));

describe("EditDatasetModal Component", () => {
  const mockOnClose = jest.fn();
  const mockUpdateDatasetQuery = clientQueries.updateDatasetQuery as jest.Mock;

  const createMockDataset = (overrides?: Partial<Dataset>): Dataset =>
    ({
      id: "1",
      name: "Test Dataset",
      description: "Test Description",
      ...overrides,
    }) as Dataset;

  const createMockMutationState = (overrides?: any) => ({
    mutate: jest.fn(),
    isPending: false,
    isError: false,
    error: null,
    data: null,
    ...overrides,
  });

  beforeEach(() => {
    jest.clearAllMocks();
    (useMutation as jest.Mock).mockImplementation((config: any) => {
      const mutation = createMockMutationState();
      mutation.mutate = jest.fn(() => {
        if (config.onSettled) {
          config.onSettled();
        }
      });

      return mutation;
    });
  });

  describe("Rendering", () => {
    it("should render modal when isOpened is true", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );
      expect(screen.getByTestId("modal")).toBeInTheDocument();
    });

    it("should not render modal when isOpened is false", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={false} onClose={mockOnClose} data={data} />,
      );
      expect(screen.queryByTestId("modal")).not.toBeInTheDocument();
    });

    it("should render modal with correct title", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );
      expect(screen.getByTestId("modal-header")).toHaveTextContent(
        "Edit dataset",
      );
    });

    it("should render name input field", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );
      expect(screen.getByTestId("text-input")).toBeInTheDocument();
    });

    it("should render description textarea field", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );
      expect(screen.getByTestId("textarea")).toBeInTheDocument();
    });

    it("should render update button", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );
      expect(screen.getByTestId("modal-footer-button")).toBeInTheDocument();
      expect(screen.getByTestId("modal-footer-button")).toHaveTextContent(
        "Update dataset",
      );
    });

    it("should render with correct number of input fields", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );
      const textInputs = screen.getAllByTestId("text-input");
      const textareas = screen.getAllByTestId("textarea");
      expect(textInputs).toHaveLength(1);
      expect(textareas).toHaveLength(1);
    });

    it("should render modal input labels", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );
      expect(screen.getByTestId("label-name")).toBeInTheDocument();
      expect(screen.getByTestId("label-description")).toBeInTheDocument();
    });
  });

  describe("Form Values Initialization", () => {
    it("should initialize name field with dataset name", () => {
      const data = createMockDataset({ name: "My Dataset" });
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );
      const input = screen.getByTestId("text-input") as HTMLInputElement;
      expect(input.value).toBe("My Dataset");
    });

    it("should initialize description field with dataset description", () => {
      const data = createMockDataset({ description: "My Description" });
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );
      const textarea = screen.getByTestId("textarea") as HTMLTextAreaElement;
      expect(textarea.value).toBe("My Description");
    });

    it("should handle empty name", () => {
      const data = createMockDataset({ name: "" });
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );
      const input = screen.getByTestId("text-input") as HTMLInputElement;
      expect(input.value).toBe("");
    });

    it("should handle empty description", () => {
      const data = createMockDataset({ description: "" });
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );
      const textarea = screen.getByTestId("textarea") as HTMLTextAreaElement;
      expect(textarea.value).toBe("");
    });

    it("should handle undefined dataset properties", () => {
      const data = createMockDataset({
        name: undefined,
        description: undefined,
      } as any);
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );
      const input = screen.getByTestId("text-input") as HTMLInputElement;
      const textarea = screen.getByTestId("textarea") as HTMLTextAreaElement;
      expect(input.value).toBe("");
      expect(textarea.value).toBe("");
    });
  });

  describe("Form Input Changes", () => {
    it("should update name field on input change", async () => {
      const user = userEvent.setup();
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      await user.clear(input);
      await user.type(input, "New Dataset Name");

      expect(input).toHaveValue("New Dataset Name");
    });

    it("should update description field on input change", async () => {
      const user = userEvent.setup();
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const textarea = screen.getByTestId("textarea");
      await user.clear(textarea);
      await user.type(textarea, "New Description");

      expect(textarea).toHaveValue("New Description");
    });

    it("should allow editing both fields independently", async () => {
      const user = userEvent.setup();
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      const textarea = screen.getByTestId("textarea");

      await user.clear(input);
      await user.type(input, "Updated Name");

      await user.clear(textarea);
      await user.type(textarea, "Updated Description");

      expect(input).toHaveValue("Updated Name");
      expect(textarea).toHaveValue("Updated Description");
    });

    it("should handle special characters in name", async () => {
      const user = userEvent.setup();
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      await user.clear(input);
      await user.type(input, "Dataset @#$% 2024");

      expect(input).toHaveValue("Dataset @#$% 2024");
    });

    it("should handle multiline text in description", async () => {
      const user = userEvent.setup();
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const textarea = screen.getByTestId("textarea");
      await user.clear(textarea);
      await user.type(textarea, "Line 1{Enter}Line 2{Enter}Line 3");

      expect(textarea).toHaveValue("Line 1\nLine 2\nLine 3");
    });

    it("should handle very long name", async () => {
      const longName = "A".repeat(500);
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      fireEvent.change(input, { target: { value: longName } });

      expect(input).toHaveValue(longName);
    });
  });

  describe("Form Validation", () => {
    it("should show error state for empty name", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      fireEvent.change(input, { target: { value: "" } });

      expect(input).toHaveAttribute("data-error", "true");
    });

    it("should show error state for whitespace-only name", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      fireEvent.change(input, { target: { value: "   " } });

      expect(input).toHaveAttribute("data-error", "true");
    });

    it("should not show error for valid name", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      fireEvent.change(input, { target: { value: "Valid Name" } });

      expect(input).toHaveAttribute("data-error", "false");
    });

    it("should not validate description field", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const textarea = screen.getByTestId("textarea");
      fireEvent.change(textarea, { target: { value: "" } });

      // Description textarea should not have error attribute
      expect(textarea).not.toHaveAttribute("data-error");
    });

    it("should disable button when name is empty", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      fireEvent.change(input, { target: { value: "" } });

      const button = screen.getByTestId("modal-footer-button");
      expect(button).toBeDisabled();
    });

    it("should disable button when name is only whitespace", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      fireEvent.change(input, { target: { value: "   " } });

      const button = screen.getByTestId("modal-footer-button");
      expect(button).toBeDisabled();
    });

    it("should enable button when name has content", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      fireEvent.change(input, { target: { value: "Valid Dataset" } });

      const button = screen.getByTestId("modal-footer-button");
      expect(button).not.toBeDisabled();
    });

    it("should show tooltip label for disabled button", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      fireEvent.change(input, { target: { value: "" } });

      const button = screen.getByTestId("modal-footer-button");
      expect(button).toHaveAttribute(
        "data-tooltip",
        "Dataset name is required",
      );
    });
  });

  describe("Form Submission", () => {
    it("should not submit when name is empty", async () => {
      const user = userEvent.setup();
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      await user.clear(input);

      const button = screen.getByTestId("modal-footer-button");
      expect(button).toBeDisabled();
      expect(mockUpdateDatasetQuery).not.toHaveBeenCalled();
    });
  });

  describe("Mutation States", () => {
    it("should show loading state on button during mutation", () => {
      const data = createMockDataset();
      (useMutation as jest.Mock).mockReturnValue(
        createMockMutationState({
          isPending: true,
        }),
      );

      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const button = screen.getByTestId("modal-footer-button");
      expect(button).toHaveAttribute("data-loading", "true");
    });
  });

  describe("Modal Close Handling", () => {
    it("should call onClose when close button clicked", async () => {
      const user = userEvent.setup();
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const closeButton = screen.getByTestId("modal-close-button");
      await user.click(closeButton);

      expect(mockOnClose).toHaveBeenCalled();
    });

    it("should call onClose after successful mutation", async () => {
      const user = userEvent.setup();
      mockUpdateDatasetQuery.mockResolvedValue({ id: "1" });

      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const button = screen.getByTestId("modal-footer-button");
      await user.click(button);

      await waitFor(() => {
        expect(mockOnClose).toHaveBeenCalled();
      });
    });

    it("should not call onClose when form is invalid", async () => {
      const user = userEvent.setup();
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      await user.clear(input);

      const button = screen.getByTestId("modal-footer-button");
      expect(button).toBeDisabled();
      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });

  describe("Effect - Form Reset on Modal Open", () => {
    it("should reset form when modal opens", () => {
      const { rerender } = render(
        <EditDatasetModal
          isOpened={false}
          onClose={mockOnClose}
          data={createMockDataset({ name: "Initial Name" })}
        />,
      );

      rerender(
        <EditDatasetModal
          isOpened={true}
          onClose={mockOnClose}
          data={createMockDataset({ name: "New Name" })}
        />,
      );

      const input = screen.getByTestId("text-input") as HTMLInputElement;
      expect(input.value).toBe("New Name");
    });

    it("should reset description when modal opens", () => {
      const { rerender } = render(
        <EditDatasetModal
          isOpened={false}
          onClose={mockOnClose}
          data={createMockDataset({ description: "Old Description" })}
        />,
      );

      rerender(
        <EditDatasetModal
          isOpened={true}
          onClose={mockOnClose}
          data={createMockDataset({ description: "New Description" })}
        />,
      );

      const textarea = screen.getByTestId("textarea") as HTMLTextAreaElement;
      expect(textarea.value).toBe("New Description");
    });
  });

  describe("Placeholders", () => {
    it("should have correct placeholder for name input", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      expect(input).toHaveAttribute("placeholder", "Dataset name");
    });

    it("should have correct placeholder for description textarea", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const textarea = screen.getByTestId("textarea");
      expect(textarea).toHaveAttribute("placeholder", "Optional description");
    });
  });

  describe("Input Labels", () => {
    it("should mark name field as required", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const label = screen.getByTestId("label-name");
      expect(label).toHaveAttribute("data-required", "true");
    });
  });

  describe("Edge Cases", () => {
    it("should handle very long dataset name", () => {
      const longName = "A".repeat(1000);
      const data = createMockDataset({ name: longName });
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input") as HTMLInputElement;
      expect(input.value).toBe(longName);
    });

    it("should handle very long description", () => {
      const longDescription = "A".repeat(5000);
      const data = createMockDataset({ description: longDescription });
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const textarea = screen.getByTestId("textarea") as HTMLTextAreaElement;
      expect(textarea.value).toBe(longDescription);
    });

    it("should handle unicode characters in name", async () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      fireEvent.change(input, { target: { value: "数据集 🚀 データセット" } });

      expect(input).toHaveValue("数据集 🚀 データセット");
    });

    it("should handle unicode characters in description", async () => {
      const user = userEvent.setup();
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const textarea = screen.getByTestId("textarea");
      await user.clear(textarea);
      await user.type(textarea, "Описание 数据 مرحبا");

      expect(textarea).toHaveValue("Описание 数据 مرحبا");
    });

    it("should handle names with only numbers", () => {
      const data = createMockDataset({ name: "123456" });
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input") as HTMLInputElement;
      expect(input.value).toBe("123456");
      expect(input).not.toHaveAttribute("data-error", "true");
    });

    it("should handle names with only special characters and spaces", async () => {
      const user = userEvent.setup();
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const input = screen.getByTestId("text-input");
      await user.clear(input);
      await user.type(input, "!@#$%");

      const button = screen.getByTestId("modal-footer-button");
      expect(button).not.toBeDisabled();
    });
  });

  describe("Modal Size and Layout", () => {
    it("should render modal with correct size", () => {
      const data = createMockDataset();
      render(
        <EditDatasetModal isOpened={true} onClose={mockOnClose} data={data} />,
      );

      const modal = screen.getByTestId("modal");
      expect(modal).toHaveAttribute("padding", "24px");
    });
  });
});

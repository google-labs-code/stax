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

import EditEvaluatorModal from "@/app/(authRoutes)/evaluatorGallery/[id]/components/EditEvaluatorModal";
import {
  EvaluatorPageAction,
  STORAGE_NEW_EVALUATOR_DATA,
} from "@/app/(authRoutes)/evaluatorGallery/types";
import * as clientQueries from "@/queries/clientQueries";
import { EvaluatorFormData } from "@/queries/types";
import { ProjectType } from "@/types";
import LocalStorage from "@/utils/LocalStorage";
import { useMutation } from "@tanstack/react-query";
import "@testing-library/jest-dom";
import { fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useParams } from "next/navigation";

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

jest.mock("@/components/ModalInput", () => {
  return function MockModalInput({ placeholder, value, onChange }: any) {
    return (
      <input
        data-testid="modal-input"
        type="text"
        placeholder={placeholder}
        value={value}
        onChange={onChange}
      />
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
    EVALUATOR_NAME_REQUIRED: "Evaluator name is required",
  },
}));

jest.mock("@/queries/clientQueries", () => ({
  updateLLMEvaluatorQuery: jest.fn(),
  updateSxSLLMEvaluatorQuery: jest.fn(),
}));

jest.mock("@/utils/LocalStorage", () => ({
  set: jest.fn(),
  get: jest.fn(),
}));

jest.mock("next/navigation", () => ({
  useParams: jest.fn(),
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
  Text: ({ children, ...props }: any) => (
    <span data-testid="text" {...props}>
      {children}
    </span>
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

describe("EditEvaluatorModal Component", () => {
  const mockOnClose = jest.fn();
  const mockSetData = jest.fn();

  const createMockEvaluatorFormData = (
    overrides?: Partial<EvaluatorFormData>,
  ): EvaluatorFormData =>
    ({
      [ProjectType.POINTWISE]: {
        id: "pointwise-1",
        name: "Pointwise Evaluator",
        description: "Pointwise Description",
      },
      [ProjectType.SIDE_BY_SIDE]: {
        id: "sxs-1",
        name: "SxS Evaluator",
        description: "SxS Description",
      },
      name: "Test Evaluator",
      description: "Test Description",
      ...overrides,
    }) as any;

  const createMockMutationState = (overrides?: any) => ({
    mutate: jest.fn(),
    isPending: false,
    isError: false,
    error: null,
    data: null,
    ...overrides,
  });

  const mockUpdateLLMEvaluatorQuery =
    clientQueries.updateLLMEvaluatorQuery as jest.Mock;
  const mockUpdateSxSLLMEvaluatorQuery =
    clientQueries.updateSxSLLMEvaluatorQuery as jest.Mock;
  const mockUseParams = useParams as jest.Mock;

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseParams.mockReturnValue({
      action: EvaluatorPageAction.EDIT,
      id: "1",
    });

    (useMutation as jest.Mock).mockImplementation((config: any) => {
      const mutation = createMockMutationState();
      mutation.mutate = jest.fn(() => {
        if (config.onSuccess) {
          config.onSuccess({ id: "1" });
        }
      });

      return mutation;
    });
  });

  describe("Rendering", () => {
    it("should render modal when isOpened is true", () => {
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );
      expect(screen.getByTestId("edit-evaluator-modal")).toBeInTheDocument();
    });

    it("should not render modal when isOpened is false", () => {
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={false}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );
      expect(
        screen.queryByTestId("edit-evaluator-modal"),
      ).not.toBeInTheDocument();
    });

    it("should render modal with correct title", () => {
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );
      expect(screen.getByTestId("modal-header")).toHaveTextContent(
        "Edit evaluator",
      );
    });

    it("should render name input field", () => {
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );
      expect(screen.getByTestId("modal-input")).toBeInTheDocument();
    });

    it("should render description textarea", () => {
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );
      expect(screen.getByTestId("textarea")).toBeInTheDocument();
    });

    it("should render update button", () => {
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );
      expect(screen.getByTestId("modal-footer-button")).toBeInTheDocument();
      expect(screen.getByTestId("modal-footer-button")).toHaveTextContent(
        "Update evaluator",
      );
    });

    it("should render labels with required indicator for name", () => {
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );
      const nameLabel = screen.getByTestId("label-name");
      expect(nameLabel).toHaveAttribute("data-required", "true");
    });
  });

  describe("Form Initialization", () => {
    it("should initialize name field with evaluator name", () => {
      const data = createMockEvaluatorFormData({ name: "My Evaluator" });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );
      const input = screen.getByTestId("modal-input") as HTMLInputElement;
      expect(input.value).toBe("My Evaluator");
    });

    it("should initialize description field with evaluator description", () => {
      const data = createMockEvaluatorFormData({
        description: "My Description",
      });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );
      const textarea = screen.getByTestId("textarea") as HTMLTextAreaElement;
      expect(textarea.value).toBe("My Description");
    });

    it("should handle empty name", () => {
      const data = createMockEvaluatorFormData({ name: "" });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );
      const input = screen.getByTestId("modal-input") as HTMLInputElement;
      expect(input.value).toBe("");
    });

    it("should handle undefined name", () => {
      const data = createMockEvaluatorFormData({ name: undefined });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );
      const input = screen.getByTestId("modal-input") as HTMLInputElement;
      expect(input.value).toBe("");
    });

    it("should handle empty description", () => {
      const data = createMockEvaluatorFormData({ description: "" });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );
      const textarea = screen.getByTestId("textarea") as HTMLTextAreaElement;
      expect(textarea.value).toBe("");
    });

    it("should reset form when modal opens", () => {
      const { rerender } = render(
        <EditEvaluatorModal
          isOpened={false}
          onClose={mockOnClose}
          data={createMockEvaluatorFormData({ name: "Initial Name" })}
          setData={mockSetData}
        />,
      );

      rerender(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={createMockEvaluatorFormData({ name: "New Name" })}
          setData={mockSetData}
        />,
      );

      const input = screen.getByTestId("modal-input") as HTMLInputElement;
      expect(input.value).toBe("New Name");
    });
  });

  describe("Form Input Changes", () => {
    it("should update name field on input change", async () => {
      const user = userEvent.setup();
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const input = screen.getByTestId("modal-input");
      await user.clear(input);
      await user.type(input, "New Evaluator Name");

      expect(input).toHaveValue("New Evaluator Name");
    });

    it("should update description field on input change", async () => {
      const user = userEvent.setup();
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const textarea = screen.getByTestId("textarea");
      await user.clear(textarea);
      await user.type(textarea, "New Description");

      expect(textarea).toHaveValue("New Description");
    });

    it("should call setData when name changes", async () => {
      const user = userEvent.setup();
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const input = screen.getByTestId("modal-input");
      await user.clear(input);
      await user.type(input, "Updated Name");

      expect(mockSetData).toHaveBeenCalledWith(expect.any(Function));
    });

    it("should call setData when description changes", async () => {
      const user = userEvent.setup();
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const textarea = screen.getByTestId("textarea");
      await user.clear(textarea);
      await user.type(textarea, "Updated Description");

      expect(mockSetData).toHaveBeenCalledWith(expect.any(Function));
    });

    it("should handle special characters in name", async () => {
      const user = userEvent.setup();
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const input = screen.getByTestId("modal-input");
      await user.clear(input);
      await user.type(input, "Evaluator @#$% 2024");

      expect(input).toHaveValue("Evaluator @#$% 2024");
    });

    it("should handle multiline text in description", async () => {
      const user = userEvent.setup();
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const textarea = screen.getByTestId("textarea");
      await user.clear(textarea);
      await user.type(textarea, "Line 1{Enter}Line 2{Enter}Line 3");

      expect(textarea).toHaveValue("Line 1\nLine 2\nLine 3");
    });
  });

  describe("Form Validation", () => {
    it("should disable button when name is empty", () => {
      const data = createMockEvaluatorFormData({ name: "" });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const button = screen.getByTestId("modal-footer-button");
      expect(button).toBeDisabled();
    });

    it("should disable button when name is only whitespace", () => {
      const data = createMockEvaluatorFormData({ name: "   " });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const button = screen.getByTestId("modal-footer-button");
      expect(button).toBeDisabled();
    });

    it("should enable button when name has content", () => {
      const data = createMockEvaluatorFormData({ name: "Valid Name" });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const button = screen.getByTestId("modal-footer-button");
      expect(button).not.toBeDisabled();
    });

    it("should show tooltip when button is disabled", () => {
      const data = createMockEvaluatorFormData({ name: "" });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const button = screen.getByTestId("modal-footer-button");
      expect(button).toHaveAttribute(
        "data-tooltip",
        "Evaluator name is required",
      );
    });

    it("should not show tooltip when button is enabled", () => {
      const data = createMockEvaluatorFormData({ name: "Valid Name" });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const button = screen.getByTestId("modal-footer-button");
      expect(button).not.toHaveAttribute("data-tooltip");
    });

    it("should allow empty description", () => {
      const data = createMockEvaluatorFormData({
        name: "Valid Name",
        description: "",
      });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const button = screen.getByTestId("modal-footer-button");
      expect(button).not.toBeDisabled();
    });
  });

  describe("Edit Action - Button Click", () => {
    it("should call both mutations on edit action", async () => {
      const user = userEvent.setup();
      mockUseParams.mockReturnValue({
        action: EvaluatorPageAction.EDIT,
        id: "1",
      });

      const mockPointwiseMutate = jest.fn();
      const mockSxSMutate = jest.fn();

      (useMutation as jest.Mock)
        .mockReturnValueOnce({
          ...createMockMutationState(),
          mutate: mockPointwiseMutate,
        })
        .mockReturnValueOnce({
          ...createMockMutationState(),
          mutate: mockSxSMutate,
        });

      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const button = screen.getByTestId("modal-footer-button");
      await user.click(button);

      expect(mockPointwiseMutate).toHaveBeenCalled();
      expect(mockSxSMutate).toHaveBeenCalled();
    });
  });

  describe("New Evaluator Action", () => {
    it("should save to localStorage when creating new evaluator", async () => {
      const user = userEvent.setup();
      mockUseParams.mockReturnValue({});

      const data = createMockEvaluatorFormData({
        name: "New Evaluator",
        description: "New Description",
      });

      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const button = screen.getByTestId("modal-footer-button");
      await user.click(button);

      expect(LocalStorage.set).toHaveBeenCalledWith(
        STORAGE_NEW_EVALUATOR_DATA,
        {
          name: "New Evaluator",
          description: "New Description",
        },
      );
    });

    it("should call onClose with null for new evaluator", async () => {
      const user = userEvent.setup();
      mockUseParams.mockReturnValue({});

      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const button = screen.getByTestId("modal-footer-button");
      await user.click(button);

      expect(mockOnClose).toHaveBeenCalledWith(null);
    });

    it("should not call mutations for new evaluator", async () => {
      const user = userEvent.setup();
      mockUseParams.mockReturnValue({});

      const mockPointwiseMutate = jest.fn();
      const mockSxSMutate = jest.fn();

      (useMutation as jest.Mock)
        .mockReturnValueOnce({
          ...createMockMutationState(),
          mutate: mockPointwiseMutate,
        })
        .mockReturnValueOnce({
          ...createMockMutationState(),
          mutate: mockSxSMutate,
        });

      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const button = screen.getByTestId("modal-footer-button");
      await user.click(button);

      expect(mockPointwiseMutate).not.toHaveBeenCalled();
      expect(mockSxSMutate).not.toHaveBeenCalled();
    });
  });

  describe("Mutation States", () => {
    it("should show loading state during mutation", () => {
      mockUseParams.mockReturnValue({
        action: EvaluatorPageAction.EDIT,
        id: "1",
      });

      (useMutation as jest.Mock)
        .mockReturnValueOnce({
          ...createMockMutationState({ isPending: true }),
        })
        .mockReturnValueOnce({
          ...createMockMutationState({ isPending: false }),
        });

      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const button = screen.getByTestId("modal-footer-button");
      expect(button).toHaveAttribute("data-loading", "true");
    });

    it("should show loading state when both mutations are pending", () => {
      mockUseParams.mockReturnValue({
        action: EvaluatorPageAction.EDIT,
        id: "1",
      });

      (useMutation as jest.Mock)
        .mockReturnValueOnce({
          ...createMockMutationState({ isPending: true }),
        })
        .mockReturnValueOnce({
          ...createMockMutationState({ isPending: true }),
        });

      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const button = screen.getByTestId("modal-footer-button");
      expect(button).toHaveAttribute("data-loading", "true");
    });
  });

  describe("Modal Close", () => {
    it("should call onClose with null when closing modal", async () => {
      const user = userEvent.setup();
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const closeButton = screen.getByTestId("modal-close-button");
      await user.click(closeButton);

      expect(mockOnClose).toHaveBeenCalledWith(null);
    });
  });

  describe("Input Placeholders", () => {
    it("should have correct placeholder for name input", () => {
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const input = screen.getByTestId("modal-input");
      expect(input).toHaveAttribute("placeholder", "Evaluator name");
    });

    it("should have correct placeholder for description textarea", () => {
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const textarea = screen.getByTestId("textarea");
      expect(textarea).toHaveAttribute(
        "placeholder",
        "Optional description for evaluator",
      );
    });
  });

  describe("Complex Scenarios", () => {
    it("should handle editing both fields and submit on edit action", async () => {
      const user = userEvent.setup();
      mockUseParams.mockReturnValue({
        action: EvaluatorPageAction.EDIT,
        id: "1",
      });

      mockUpdateLLMEvaluatorQuery.mockResolvedValue({ id: "pointwise-1" });
      mockUpdateSxSLLMEvaluatorQuery.mockResolvedValue({ id: "sxs-1" });

      const data = createMockEvaluatorFormData({
        name: "Original Name",
        description: "Original Description",
      });

      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const input = screen.getByTestId("modal-input");
      const textarea = screen.getByTestId("textarea");

      await user.clear(input);
      await user.type(input, "Updated Name");
      await user.clear(textarea);
      await user.type(textarea, "Updated Description");

      const button = screen.getByTestId("modal-footer-button");
      expect(button).not.toBeDisabled();
    });

    it("should handle switching between edit and new states", () => {
      mockUseParams.mockReturnValue({
        action: EvaluatorPageAction.EDIT,
        id: "1",
      });

      const { rerender } = render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={createMockEvaluatorFormData({
            name: "Edit Mode",
          })}
          setData={mockSetData}
        />,
      );

      let input = screen.getByTestId("modal-input") as HTMLInputElement;
      expect(input.value).toBe("Edit Mode");

      mockUseParams.mockReturnValue({});

      rerender(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={createMockEvaluatorFormData({
            name: "Edit Mode",
          })}
          setData={mockSetData}
        />,
      );

      input = screen.getByTestId("modal-input") as HTMLInputElement;
      expect(input.value).toBe("Edit Mode");
    });

    it("should handle unicode characters in name and description", async () => {
      const user = userEvent.setup();
      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const input = screen.getByTestId("modal-input");
      const textarea = screen.getByTestId("textarea");

      await user.clear(input);
      await user.type(input, "评估员 🚀 エバリュエーター");
      await user.clear(textarea);
      await user.type(textarea, "Описание 数据 مرحبا");

      expect(input).toHaveValue("评估员 🚀 エバリュエーター");
      expect(textarea).toHaveValue("Описание 数据 مرحبا");
    });

    it("should handle very long name and description", async () => {
      const longName = "A".repeat(500);
      const longDescription = "B".repeat(1000);

      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const input = screen.getByTestId("modal-input");
      const textarea = screen.getByTestId("textarea");

      fireEvent.change(input, { target: { value: longName } });
      fireEvent.change(textarea, { target: { value: longDescription } });

      expect(input).toHaveValue(longName);
      expect(textarea).toHaveValue(longDescription);
    });

    it("should preserve description when editing only name", async () => {
      const user = userEvent.setup();
      const data = createMockEvaluatorFormData({
        name: "Original Name",
        description: "Should be preserved",
      });

      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const input = screen.getByTestId("modal-input");
      const textarea = screen.getByTestId("textarea") as HTMLTextAreaElement;

      await user.clear(input);
      await user.type(input, "New Name");

      expect(textarea.value).toBe("Should be preserved");
    });
  });

  describe("Edge Cases", () => {
    it("should handle name with leading and trailing spaces", () => {
      const data = createMockEvaluatorFormData({ name: "  Name  " });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const input = screen.getByTestId("modal-input") as HTMLInputElement;
      expect(input.value).toBe("  Name  ");

      const button = screen.getByTestId("modal-footer-button");
      expect(button).not.toBeDisabled();
    });

    it("should disable button when name becomes whitespace-only after edit", async () => {
      const user = userEvent.setup();
      const data = createMockEvaluatorFormData({ name: "Valid Name" });
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      const input = screen.getByTestId("modal-input");
      await user.clear(input);
      await user.type(input, "   ");

      const button = screen.getByTestId("modal-footer-button");
      expect(button).toBeDisabled();
    });

    it("should handle null params gracefully", () => {
      mockUseParams.mockReturnValue(null);

      const data = createMockEvaluatorFormData();
      render(
        <EditEvaluatorModal
          isOpened={true}
          onClose={mockOnClose}
          data={data}
          setData={mockSetData}
        />,
      );

      expect(screen.getByTestId("edit-evaluator-modal")).toBeInTheDocument();
    });
  });
});

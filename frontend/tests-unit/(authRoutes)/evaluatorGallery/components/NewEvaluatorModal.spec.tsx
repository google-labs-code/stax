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

import NewEvaluatorModal from "@/app/(authRoutes)/evaluatorGallery/components/NewEvaluatorModal";
import { STORAGE_NEW_EVALUATOR_DATA } from "@/app/(authRoutes)/evaluatorGallery/types";
import { testRender } from "@/tests-unit/render";
import logGAevent from "@/utils/logGAevent";
import { fireEvent, screen } from "@testing-library/react";
import React from "react";

jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: jest.fn(),
  }),
}));

const mockSet = jest.fn();
jest.mock("@/utils/LocalStorage", () => ({
  __esModule: true,
  default: { set: (...args: any[]) => mockSet(...args) },
}));

jest.mock("@/utils/logGAevent", () => jest.fn());

const mockOnClose = jest.fn();

describe("NewEvaluatorModal", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders modal with inputs and calls createEvaluatorAction on submit", () => {
    testRender(<NewEvaluatorModal isOpened={true} onClose={mockOnClose} />);
    // Modal title
    expect(screen.getByText(/new evaluator/i)).toBeInTheDocument();
    // Name input
    const nameInput = screen.getByPlaceholderText(/evaluator name/i);
    expect(nameInput).toBeInTheDocument();
    // Description textarea
    const descInput = screen.getByPlaceholderText(
      /optional description for evaluator/i,
    );
    expect(descInput).toBeInTheDocument();
    // Button should be disabled initially
    const createBtn = screen.getByRole("button", { name: /create evaluator/i });
    expect(createBtn).toBeDisabled();

    // Fill name
    fireEvent.change(nameInput, { target: { value: "Test Evaluator" } });
    expect(createBtn).not.toBeDisabled();
    // Fill description
    fireEvent.change(descInput, { target: { value: "A test description" } });

    // Click create
    fireEvent.click(createBtn);
    // logGAevent called
    expect(logGAevent).toHaveBeenCalled();
    // LocalStorage.set called with correct key and data
    expect(mockSet).toHaveBeenCalledWith(STORAGE_NEW_EVALUATOR_DATA, {
      name: "Test Evaluator",
      description: "A test description",
    });
    // onClose called
    expect(mockOnClose).toHaveBeenCalled();
  });

  it("clears inputs when opened", () => {
    const { rerender } = testRender(
      <NewEvaluatorModal isOpened={false} onClose={mockOnClose} />,
    );
    // Open modal
    rerender(<NewEvaluatorModal isOpened={true} onClose={mockOnClose} />);
    expect(screen.getByPlaceholderText(/evaluator name/i)).toHaveValue("");
    expect(
      screen.getByPlaceholderText(/optional description for evaluator/i),
    ).toHaveValue("");
  });
});

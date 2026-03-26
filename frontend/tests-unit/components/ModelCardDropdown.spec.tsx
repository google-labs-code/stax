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

import ModelCardDropdown from "@/components/ModelCardDropdown";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

describe("ModelCardDropdown", () => {
  const mockHandlers = {
    onDeleteModel: jest.fn(),
    onShowDetails: jest.fn(),
    onCopyModel: jest.fn(),
    onEdit: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders the dropdown button", () => {
    testRender(
      <ModelCardDropdown
        onShowDetails={mockHandlers.onShowDetails}
        onCopyModel={mockHandlers.onCopyModel}
      />,
    );
    expect(screen.getByText("more_vert")).toBeInTheDocument();
  });

  it("opens dropdown when clicking the trigger button", () => {
    testRender(
      <ModelCardDropdown
        onShowDetails={mockHandlers.onShowDetails}
        onCopyModel={mockHandlers.onCopyModel}
      />,
    );

    fireEvent.click(screen.getByText("more_vert"));

    expect(screen.getByText("Show details")).toBeInTheDocument();
    expect(screen.getByText("Make a copy")).toBeInTheDocument();
  });

  it("closes dropdown when clicking outside", () => {
    testRender(
      <ModelCardDropdown
        onShowDetails={mockHandlers.onShowDetails}
        onCopyModel={mockHandlers.onCopyModel}
      />,
    );

    fireEvent.click(screen.getByText("more_vert"));
    expect(screen.getByText("Show details")).toBeInTheDocument();

    const overlay = document.querySelector(
      ".fixed.inset-0.z-\\[99\\].bg-transparent",
    );
    expect(overlay).toBeInTheDocument();
    fireEvent.click(overlay!);

    expect(screen.queryByText("Show details")).not.toBeInTheDocument();
  });

  it("calls onShowDetails and closes dropdown when clicking Show details", () => {
    testRender(
      <ModelCardDropdown
        onShowDetails={mockHandlers.onShowDetails}
        onCopyModel={mockHandlers.onCopyModel}
      />,
    );

    fireEvent.click(screen.getByText("more_vert"));

    fireEvent.click(screen.getByText("Show details"));

    expect(mockHandlers.onShowDetails).toHaveBeenCalledTimes(1);
    expect(screen.queryByText("Show details")).not.toBeInTheDocument();
  });

  it("calls onCopyModel and closes dropdown when clicking Make a copy", () => {
    testRender(
      <ModelCardDropdown
        onShowDetails={mockHandlers.onShowDetails}
        onCopyModel={mockHandlers.onCopyModel}
      />,
    );

    fireEvent.click(screen.getByText("more_vert"));

    fireEvent.click(screen.getByText("Make a copy"));

    expect(mockHandlers.onCopyModel).toHaveBeenCalledTimes(1);
    expect(screen.queryByText("Make a copy")).not.toBeInTheDocument();
  });

  it("shows Edit button when onEdit prop is provided", () => {
    testRender(
      <ModelCardDropdown
        onShowDetails={mockHandlers.onShowDetails}
        onCopyModel={mockHandlers.onCopyModel}
        onEdit={mockHandlers.onEdit}
      />,
    );

    fireEvent.click(screen.getByText("more_vert"));

    expect(screen.getByText("Edit")).toBeInTheDocument();
  });

  it("calls onEdit and closes dropdown when clicking Edit", () => {
    testRender(
      <ModelCardDropdown
        onShowDetails={mockHandlers.onShowDetails}
        onCopyModel={mockHandlers.onCopyModel}
        onEdit={mockHandlers.onEdit}
      />,
    );

    fireEvent.click(screen.getByText("more_vert"));

    fireEvent.click(screen.getByText("Edit"));

    expect(mockHandlers.onEdit).toHaveBeenCalledTimes(1);
    expect(screen.queryByText("Edit")).not.toBeInTheDocument();
  });

  it("does not show Edit button when onEdit prop is not provided", () => {
    testRender(
      <ModelCardDropdown
        onShowDetails={mockHandlers.onShowDetails}
        onCopyModel={mockHandlers.onCopyModel}
      />,
    );

    fireEvent.click(screen.getByText("more_vert"));

    expect(screen.queryByText("Edit")).not.toBeInTheDocument();
  });

  it("shows Delete button when onDeleteModel prop is provided", () => {
    testRender(
      <ModelCardDropdown
        onShowDetails={mockHandlers.onShowDetails}
        onCopyModel={mockHandlers.onCopyModel}
        onDeleteModel={mockHandlers.onDeleteModel}
      />,
    );

    fireEvent.click(screen.getByText("more_vert"));

    expect(screen.getByText("Delete model")).toBeInTheDocument();
  });

  it("calls onDeleteModel and closes dropdown when clicking Delete model", () => {
    testRender(
      <ModelCardDropdown
        onShowDetails={mockHandlers.onShowDetails}
        onCopyModel={mockHandlers.onCopyModel}
        onDeleteModel={mockHandlers.onDeleteModel}
      />,
    );

    fireEvent.click(screen.getByText("more_vert"));

    fireEvent.click(screen.getByText("Delete model"));

    expect(mockHandlers.onDeleteModel).toHaveBeenCalledTimes(1);
    expect(screen.queryByText("Delete model")).not.toBeInTheDocument();
  });

  it("does not show Delete button when onDeleteModel prop is not provided", () => {
    testRender(
      <ModelCardDropdown
        onShowDetails={mockHandlers.onShowDetails}
        onCopyModel={mockHandlers.onCopyModel}
      />,
    );

    fireEvent.click(screen.getByText("more_vert"));

    expect(screen.queryByText("Delete model")).not.toBeInTheDocument();
  });

  it("prevents event propagation when clicking buttons", () => {
    const parentClickHandler = jest.fn();

    testRender(
      <div onClick={parentClickHandler}>
        <ModelCardDropdown
          onShowDetails={mockHandlers.onShowDetails}
          onCopyModel={mockHandlers.onCopyModel}
          onEdit={mockHandlers.onEdit}
          onDeleteModel={mockHandlers.onDeleteModel}
        />
      </div>,
    );

    fireEvent.click(screen.getByText("more_vert"));

    expect(parentClickHandler).not.toHaveBeenCalled();

    fireEvent.click(screen.getByText("Edit"));

    expect(parentClickHandler).not.toHaveBeenCalled();
    expect(mockHandlers.onEdit).toHaveBeenCalled();

    fireEvent.click(screen.getByText("more_vert"));
    parentClickHandler.mockClear();

    fireEvent.click(screen.getByText("Show details"));
    expect(parentClickHandler).not.toHaveBeenCalled();
    expect(mockHandlers.onShowDetails).toHaveBeenCalled();

    fireEvent.click(screen.getByText("more_vert"));
    parentClickHandler.mockClear();

    fireEvent.click(screen.getByText("Make a copy"));
    expect(parentClickHandler).not.toHaveBeenCalled();
    expect(mockHandlers.onCopyModel).toHaveBeenCalled();

    fireEvent.click(screen.getByText("more_vert"));
    parentClickHandler.mockClear();

    fireEvent.click(screen.getByText("Delete model"));
    expect(parentClickHandler).not.toHaveBeenCalled();
    expect(mockHandlers.onDeleteModel).toHaveBeenCalled();
  });
});

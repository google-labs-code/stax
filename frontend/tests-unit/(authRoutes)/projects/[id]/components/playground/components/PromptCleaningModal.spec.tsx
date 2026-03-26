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

import PromptCleaningModal from "@/app/(authRoutes)/projects/[id]/playground/components/PromptCleaningModal";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { testRender } from "@/tests-unit/render";
import LocalStorage from "@/utils/LocalStorage";
import { fireEvent, screen } from "@testing-library/react";
import { useRouter } from "next/navigation";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock("@/utils/LocalStorage", () => ({
  get: jest.fn(),
  set: jest.fn(),
}));

describe("PromptCleaningModal", () => {
  const mockResetPlayground = jest.fn();
  const mockRouterPush = jest.fn();
  const mockOnClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    (useRouter as jest.Mock).mockReturnValue({
      push: mockRouterPush,
    });

    (usePlaygroundContext as jest.Mock).mockReturnValue({
      resetPlayground: mockResetPlayground,
    });

    (useProjectContext as jest.Mock).mockReturnValue({
      projectState: { project: { project_id: "proj-123" } },
    });

    (LocalStorage.get as jest.Mock).mockReturnValue(false);
  });

  const defaultProps = {
    isOpened: true,
    onClose: mockOnClose,
  };

  it("renders the modal with correct title, text, and buttons", () => {
    testRender(<PromptCleaningModal {...defaultProps} />);

    expect(screen.getByText("Test case saved to project!")).toBeInTheDocument();

    expect(screen.getByAltText("Completed Icon")).toBeInTheDocument();

    expect(
      screen.getByText(
        "You’re building a dataset for AI evaluation. Add more test cases to build a robust dataset so you can compare AI performance.",
      ),
    ).toBeInTheDocument();

    expect(screen.getByText("Add another test case")).toBeInTheDocument();
    expect(screen.getByText("Return to project")).toBeInTheDocument();
  });

  it("does not render the modal if `SHOW_PLAYGROUND_INFO_BAR` is set in LocalStorage", () => {
    (LocalStorage.get as jest.Mock).mockReturnValue(true);

    testRender(<PromptCleaningModal {...defaultProps} />);

    expect(
      screen.queryByText("Test case saved to project!"),
    ).not.toBeInTheDocument();
  });

  it("calls `resetPlayground` and sets `playgroundInfoBar` when 'Add another test case' is clicked", () => {
    testRender(<PromptCleaningModal {...defaultProps} />);

    fireEvent.click(screen.getByText("Add another test case"));

    expect(mockResetPlayground).toHaveBeenCalledTimes(1);

    expect(LocalStorage.set).toHaveBeenCalledWith("playgroundInfoBar", "true");
  });

  it("calls `onClose`, navigates to project, and sets `playgroundInfoBar` when 'Return to project' is clicked", () => {
    testRender(<PromptCleaningModal {...defaultProps} />);

    fireEvent.click(screen.getByText("Return to project"));

    expect(mockOnClose).toHaveBeenCalledTimes(1);
    expect(mockRouterPush).toHaveBeenCalledWith("/projects/proj-123");
    expect(LocalStorage.set).toHaveBeenCalledWith("playgroundInfoBar", "true");
  });

  it("calls `onClose` directly when modal is closed", () => {
    testRender(<PromptCleaningModal {...defaultProps} />);

    mockOnClose();

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it("checks if resetPlayground is not called when modal is not opened", () => {
    const closedProps = { ...defaultProps, isOpened: false };

    testRender(<PromptCleaningModal {...closedProps} />);

    const button = screen.queryByText("Add another test case");
    expect(button).not.toBeInTheDocument();

    expect(mockResetPlayground).not.toHaveBeenCalled();
  });

  it("handles LocalStorage updates correctly on modal close", () => {
    testRender(<PromptCleaningModal {...defaultProps} />);

    fireEvent.click(screen.getByText("Return to project"));

    expect(LocalStorage.set).toHaveBeenCalledTimes(1);
    expect(LocalStorage.set).toHaveBeenCalledWith("playgroundInfoBar", "true");
  });
});

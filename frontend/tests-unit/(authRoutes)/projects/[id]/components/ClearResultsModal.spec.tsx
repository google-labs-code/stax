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

import ClearResultsModal from "@/app/(authRoutes)/projects/[id]/components/ClearResultsModal";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import * as clientQueries from "@/queries/clientQueries";
import { testRender } from "@/tests-unit/render";
import { useMutation } from "@tanstack/react-query";
import { fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: jest.fn(),
}));

jest.mock("@tanstack/react-query", () => ({
  ...jest.requireActual("@tanstack/react-query"),
  useMutation: jest.fn(),
}));

jest.mock("@/queries/clientQueries", () => ({
  clearAllResultsQuery: jest.fn(),
  clearResultsQuery: jest.fn(),
}));

describe("ClearResultsModal", () => {
  const mockSetRowSelection = jest.fn();
  const mockSetActiveRows = jest.fn();
  const mockOnClose = jest.fn();
  const mockRefetchProject = jest.fn().mockResolvedValue({});

  beforeEach(() => {
    (useProjectsContext as jest.Mock).mockReturnValue({
      defaultProjectId: "default-project-id",
    });

    jest.clearAllMocks();
  });

  function setup(overrides = {}) {
    const defaultProps = {
      isOpened: true,
      onClose: mockOnClose,
      setRowSelection: mockSetRowSelection,
      setActiveRows: mockSetActiveRows,
      chatTurnIds: ["chat-1", "chat-2"],
      refetchProject: mockRefetchProject,
      selectAllRowsInProject: false,
      ...overrides,
    };

    testRender(<ClearResultsModal {...defaultProps} />);
  }

  it("renders modal with correct text", () => {
    setup();
    expect(screen.getByText("Clear results?")).toBeInTheDocument();
    expect(
      screen.getByText(
        /This will delete all generated results such as model outputs/,
      ),
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Clear/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Cancel/i })).toBeInTheDocument();
  });

  it("calls onClose and clears state when cancel is clicked", () => {
    setup();
    fireEvent.click(screen.getByRole("button", { name: /Cancel/i }));
    expect(mockSetRowSelection).toHaveBeenCalledWith({});
    expect(mockSetActiveRows).toHaveBeenCalledWith([]);
    expect(mockOnClose).toHaveBeenCalled();
  });

  it("clears specific results when selectAllRowsInProject is false", async () => {
    const mutateFn = jest.fn();
    (useMutation as jest.Mock).mockImplementation(({ onSuccess }) => ({
      mutate: (data: any) => {
        mutateFn(data);
        onSuccess();
      },
    }));

    setup({ selectAllRowsInProject: false });

    fireEvent.click(screen.getByRole("button", { name: /Clear/i }));

    await waitFor(() => {
      expect(mutateFn).toHaveBeenCalledWith({
        chat_turn_ids: ["chat-1", "chat-2"],
      });
      expect(mockRefetchProject).toHaveBeenCalled();
    });
  });

  it("clears all results when selectAllRowsInProject is true", async () => {
    const mutateFn = jest.fn();
    (useMutation as jest.Mock).mockImplementation(({ onSuccess }) => ({
      mutate: () => {
        mutateFn();
        onSuccess();
      },
    }));

    setup({ selectAllRowsInProject: true });

    fireEvent.click(screen.getByRole("button", { name: /Clear/i }));

    await waitFor(() => {
      expect(mutateFn).toHaveBeenCalled();
      expect(mockRefetchProject).toHaveBeenCalled();
    });
  });

  it("uses provided projectId if passed", async () => {
    const mockClear = jest.fn();
    (clientQueries.clearResultsQuery as jest.Mock).mockImplementation(
      (data, projectId) => {
        mockClear(data, projectId);

        return Promise.resolve();
      },
    );

    (useMutation as jest.Mock).mockImplementation(
      ({ mutationFn, onSuccess }) => ({
        mutate: (data: any) => {
          mutationFn(data).then(onSuccess);
        },
      }),
    );

    setup({
      projectId: "custom-project-id",
      selectAllRowsInProject: false,
    });

    fireEvent.click(screen.getByRole("button", { name: /Clear/i }));

    await waitFor(() => {
      expect(mockClear).toHaveBeenCalledWith(
        { chat_turn_ids: ["chat-1", "chat-2"] },
        "custom-project-id",
      );
    });
  });

  it("displays additional warning if clearing all rows", () => {
    setup({ selectAllRowsInProject: true });
    expect(
      screen.getByText(/This will clear results for all rows in the project/i),
    ).toBeInTheDocument();
  });

  it("calls onModalClose when modal is closed via backdrop or escape", () => {
    setup();

    const modal = screen.getByRole("dialog");
    fireEvent.keyDown(modal, { key: "Escape", code: "Escape" });

    fireEvent.click(screen.getByRole("dialog"));

    expect(mockSetRowSelection).toHaveBeenCalledWith({});
    expect(mockSetActiveRows).toHaveBeenCalledWith([]);
    expect(mockOnClose).toHaveBeenCalled();
  });

  it("calls onModalClose when Modal onClose is triggered", () => {
    setup();

    fireEvent.click(document.querySelector(".mantine-Modal-overlay")!);

    expect(mockSetRowSelection).toHaveBeenCalledWith({});
    expect(mockSetActiveRows).toHaveBeenCalledWith([]);
    expect(mockOnClose).toHaveBeenCalled();
  });
});

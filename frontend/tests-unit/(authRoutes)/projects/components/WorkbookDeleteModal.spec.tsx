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

import WorkbookDeleteModal from "@/app/(authRoutes)/projects/[id]/components/WorkbookDeleteModal";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import * as clientQueries from "@/queries/clientQueries";
import { testRender } from "@/tests-unit/render";
import { ProjectType, Provider } from "@/types";
import { fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: () => ({ defaultProjectId: "proj-123" }),
}));

jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

jest.mock("@/queries/clientQueries", () => ({
  deleteAllWorkbookRowsQuery: jest.fn(),
  deleteAllSxSRowsQuery: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext");

describe("WorkbookDeleteModal", () => {
  const defaultProps = {
    isOpened: true,
    onClose: jest.fn(),
    selectedRows: [
      {
        chat_id: "chat-123",
        input: "Test input",
        expected_output: "Expected",
        model_provider: Provider.OPENAI,
        model_id: "model-123",
      },
    ],
    setRowSelection: jest.fn(),
    projectType: ProjectType.POINTWISE,
  };

  afterEach(() => {
    jest.clearAllMocks();
  });

  it("renders the modal with title and table for selected rows", () => {
    testRender(<WorkbookDeleteModal {...defaultProps} />);
    expect(screen.getByText("Delete selected row(s)")).toBeInTheDocument();
    expect(screen.getByText("Delete")).toBeInTheDocument();
    expect(screen.getByText("Cancel")).toBeInTheDocument();
  });

  it("renders the modal with title and table with headers for selected rows", () => {
    const initialColumnOrder = [
      { accessorKey: "input", header: "Input" },
      { accessorKey: "updated_at", header: "Updated at" },
    ];
    testRender(
      <WorkbookDeleteModal
        {...defaultProps}
        initialColumnOrder={initialColumnOrder}
      />,
    );
    expect(screen.getByText("Delete selected row(s)")).toBeInTheDocument();
    expect(screen.getByText("Delete")).toBeInTheDocument();
    expect(screen.getByText("Cancel")).toBeInTheDocument();
  });

  it("calls onClose when Cancel is clicked", () => {
    testRender(<WorkbookDeleteModal {...defaultProps} />);
    fireEvent.click(screen.getByText("Cancel"));
    expect(defaultProps.onClose).toHaveBeenCalled();
  });

  it("calls deleteAllSxSRowsQuery for SIDE_BY_SIDE projects", async () => {
    const deleteAllSxSRowsQuery =
      clientQueries.deleteAllSxSRowsQuery as jest.Mock;
    deleteAllSxSRowsQuery.mockResolvedValue({});

    testRender(
      <WorkbookDeleteModal
        {...defaultProps}
        projectType={ProjectType.SIDE_BY_SIDE}
        selectAllRowsInProject={true}
        projectId="proj-sxs"
      />,
    );

    fireEvent.click(screen.getByText("Delete"));

    await waitFor(() => {
      expect(deleteAllSxSRowsQuery).toHaveBeenCalledWith("proj-sxs");
    });
  });

  it("calls onBeforeDelete, onDelete and setAddedRowsCount if provided", async () => {
    const onBeforeDelete = jest.fn();
    const onDelete = jest.fn();
    const onRowDelete = { mutate: jest.fn() };
    const mockSetAddedRowsCount = jest.fn();

    testRender(
      <WorkbookDeleteModal
        {...defaultProps}
        onBeforeDelete={onBeforeDelete}
        onDelete={onDelete}
        onRowDelete={onRowDelete as any}
        setAddedRowsCount={mockSetAddedRowsCount}
      />,
    );

    fireEvent.click(screen.getByText("Delete"));

    await waitFor(() => {
      expect(onBeforeDelete).toHaveBeenCalled();
      expect(onDelete).toHaveBeenCalled();
      expect(onRowDelete.mutate).toHaveBeenCalled();
      expect(mockSetAddedRowsCount).toHaveBeenCalledWith(0);
    });
  });

  it("calls onClearBannerState when deleting all", async () => {
    const onClearBannerState = jest.fn();
    const deleteAllWorkbookRowsQuery =
      clientQueries.deleteAllWorkbookRowsQuery as jest.Mock;
    deleteAllWorkbookRowsQuery.mockResolvedValue({});

    testRender(
      <WorkbookDeleteModal
        {...defaultProps}
        selectAllRowsInProject={true}
        projectId="proj-789"
        onClearBannerState={onClearBannerState}
      />,
    );

    fireEvent.click(screen.getByText("Delete"));

    await waitFor(() => {
      expect(onClearBannerState).toHaveBeenCalled();
    });
  });

  it("calls deleteAllWorkbookRowsQuery for projects with refetchProject", async () => {
    const deleteAllWorkbookRowsQuery =
      clientQueries.deleteAllWorkbookRowsQuery as jest.Mock;
    deleteAllWorkbookRowsQuery.mockResolvedValue({});

    const mockRefetchProject = jest.fn();
    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectQueries: {
        refetchProject: mockRefetchProject,
      },
    }));

    testRender(
      <WorkbookDeleteModal
        {...defaultProps}
        projectType={ProjectType.POINTWISE}
        selectAllRowsInProject={true}
        projectId="proj-sxs"
      />,
    );

    fireEvent.click(screen.getByText("Delete"));

    await waitFor(() => {
      expect(deleteAllWorkbookRowsQuery).toHaveBeenCalledWith("proj-sxs");
      expect(mockRefetchProject).toHaveBeenCalled();
    });
  });
});

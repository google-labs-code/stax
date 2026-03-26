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

import WorkbookHeader from "@/app/(authRoutes)/projects/[id]/components/WorkbookHeader";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { getErrorNotificationConfig } from "@/config/notifications";
import * as clientQueries from "@/queries/clientQueries";
import { testRender } from "@/tests-unit/render";
import { ProjectType, Provider } from "@/types";
import { fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: jest.fn(),
  }),
}));

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: () => ({
    setSelectedChatIds: jest.fn(),
    setSelectedChatTurnIds: jest.fn(),
  }),
}));

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: () => ({
    allModels: [{ is_api_key_present: true }],
  }),
}));

jest.mock("@/queries/clientQueries", () => ({
  getChatsExportQuery: jest.fn().mockResolvedValue([]),
  getExportALLQuery: jest.fn().mockResolvedValue([]),
  getSxsProjectBulkExport: jest.fn().mockResolvedValue([]),
  getSxsProjectExport: jest.fn().mockResolvedValue([]),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock("@/utils/jsonToCsvExport", () => ({
  jsonToCsvExport: jest.fn(),
}));

jest.mock("@/config/notifications", () => {
  const actual = jest.requireActual("@/config/notifications");

  return {
    ...actual,
    getErrorNotificationConfig: jest.fn(),
  };
});

(useProjectContext as jest.Mock).mockReturnValue({
  isSideBySide: false,
  projectType: ProjectType.POINTWISE,
});

describe("WorkbookHeader", () => {
  const mockOnAddData = jest.fn();
  const mockOnEvaluate = jest.fn();
  const mockOnGenerateModels = jest.fn();
  const mockOpenDeleteModal = jest.fn();

  const baseProps: any = {
    onAddData: mockOnAddData,
    onEvaluate: mockOnEvaluate,
    onGenerateModels: mockOnGenerateModels,
    onAddTags: jest.fn(),
    onRemoveTags: jest.fn(),
    onAddToProject: jest.fn(),
    onClearResults: jest.fn(),
    onAddToDataset: jest.fn(),
    openWorkbookDeleteModal: mockOpenDeleteModal,
    projectId: "proj-123",
    hideTooltips: true,
    projectType: ProjectType.POINTWISE,
    table: {
      getSelectedRowModel: () => ({
        flatRows: [
          {
            depth: 0,
            original: {
              chat_id: "chat-1",
              raw_input: "Input",
              output: "Output",
              model_provider: Provider.OPENAI,
              model_id: "model-1",
            },
          },
        ],
      }),
      getRowCount: () => 2,
      getAllColumns: () => [
        {
          id: "columnA",
          columnDef: { header: "Column A" },
          getIsVisible: () => true,
        },
      ],
      getColumn: () => ({
        getIsVisible: () => true,
      }),
      setColumnVisibility: jest.fn(),
      getRowModel: () => ({
        rows: [
          {
            original: {
              output: "Output",
            },
          },
        ],
      }),
      getState: () => ({
        rowSelection: {
          "chat-1": true,
        },
      }),
    },
    selectedRows: [
      {
        depth: 0,
        original: {
          chat_id: "chat-1",
          raw_input: "Input",
          output: "Output",
          model_provider: Provider.OPENAI,
          model_id: "model-1",
        },
      },
    ],
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders all header buttons", () => {
    testRender(<WorkbookHeader {...baseProps} />);
    expect(screen.getByText("Open Playground")).toBeInTheDocument();
    expect(screen.getByText("Upload Dataset")).toBeInTheDocument();
    expect(screen.getByText("Columns")).toBeInTheDocument();
    expect(screen.getByText("More")).toBeInTheDocument();
    expect(screen.getByText("Generate outputs")).toBeInTheDocument();
    expect(screen.getByText("Evaluate")).toBeInTheDocument();
  });

  it("calls onAddData when Upload Dataset is clicked", () => {
    testRender(<WorkbookHeader {...baseProps} />);
    fireEvent.click(screen.getByText("Upload Dataset"));
    expect(mockOnAddData).toHaveBeenCalled();
  });

  it("calls onGenerateModels when Generate outputs is clicked", () => {
    testRender(<WorkbookHeader {...baseProps} />);
    fireEvent.click(screen.getByText("Generate outputs"));
    expect(mockOnGenerateModels).toHaveBeenCalled();
  });

  it("calls onEvaluate when Evaluate is clicked", () => {
    testRender(<WorkbookHeader {...baseProps} />);
    fireEvent.click(screen.getByText("Evaluate"));
    expect(mockOnEvaluate).toHaveBeenCalled();
  });

  it("calls delete modal function when Delete clicked from More menu", async () => {
    testRender(<WorkbookHeader {...baseProps} />);
    fireEvent.click(screen.getByText("More"));
    await waitFor(() => {
      fireEvent.click(screen.getByText("Delete"));
    });
    expect(mockOpenDeleteModal).toHaveBeenCalled();
  });

  it("calls getChatsExportQuery when Export clicked (Pointwise)", async () => {
    const getChatsExportQuery = clientQueries.getChatsExportQuery as jest.Mock;

    testRender(<WorkbookHeader {...baseProps} />);
    fireEvent.click(screen.getByText("More"));
    await waitFor(() => fireEvent.click(screen.getByText("Export")));

    await waitFor(() =>
      expect(getChatsExportQuery).toHaveBeenCalledWith(["chat-1"]),
    );
  });

  it("calls getSxsProjectBulkExport when Export clicked (SideBySide)", async () => {
    const getSxsProjectBulkExport =
      clientQueries.getSxsProjectBulkExport as jest.Mock;

    (useProjectContext as jest.Mock).mockReturnValue({
      isSideBySide: true,
      projectType: ProjectType.SIDE_BY_SIDE,
    });

    testRender(<WorkbookHeader {...baseProps} />);
    fireEvent.click(screen.getByText("More"));
    await waitFor(() => fireEvent.click(screen.getByText("Export")));

    await waitFor(() =>
      expect(getSxsProjectBulkExport).toHaveBeenCalledWith("proj-123", []),
    );
  });

  it("calls getExportALLQuery when selectAllRowsInProject is true", async () => {
    const getExportALLQuery = clientQueries.getExportALLQuery as jest.Mock;

    (useProjectContext as jest.Mock).mockReturnValue({
      isSideBySide: false,
      projectType: ProjectType.POINTWISE,
    });

    testRender(
      <WorkbookHeader
        {...baseProps}
        selectionBanner={{
          selectAllRowsInProject: true,
          onClearSelection: jest.fn(),
        }}
      />,
    );

    fireEvent.click(screen.getByText("More"));
    await waitFor(() => fireEvent.click(screen.getByText("Export")));

    expect(getExportALLQuery).toHaveBeenCalledWith("proj-123");
  });

  it("calls getExportALLQuery when selectAllRowsInProject is true and catches error", async () => {
    const getExportALLQuery = clientQueries.getExportALLQuery as jest.Mock;
    getExportALLQuery.mockRejectedValue(new Error("Test error"));

    (useProjectContext as jest.Mock).mockReturnValue({
      isSideBySide: false,
      projectType: ProjectType.POINTWISE,
    });

    testRender(
      <WorkbookHeader
        {...baseProps}
        selectionBanner={{
          selectAllRowsInProject: true,
          onClearSelection: jest.fn(),
        }}
      />,
    );

    fireEvent.click(screen.getByText("More"));
    await waitFor(() => fireEvent.click(screen.getByText("Export")));

    expect(getErrorNotificationConfig).toHaveBeenCalled();
  });

  it("calls getSxsProjectBulkExport when Export clicked (SideBySide) and catches error", async () => {
    const getSxsProjectBulkExport =
      clientQueries.getSxsProjectBulkExport as jest.Mock;
    getSxsProjectBulkExport.mockRejectedValue(new Error("Test error"));

    (useProjectContext as jest.Mock).mockReturnValue({
      isSideBySide: true,
      projectType: ProjectType.SIDE_BY_SIDE,
    });

    testRender(<WorkbookHeader {...baseProps} />);
    fireEvent.click(screen.getByText("More"));
    await waitFor(() => fireEvent.click(screen.getByText("Export")));

    await waitFor(() => expect(getErrorNotificationConfig).toHaveBeenCalled());
  });

  it("calls onHideAll when Hide All is clicked", () => {
    const mockSetColumnVisibility = jest.fn((cb: () => void) => cb());
    baseProps.table.setColumnVisibility = mockSetColumnVisibility;

    testRender(<WorkbookHeader {...baseProps} />);
    fireEvent.click(screen.getByText("Columns"));
    fireEvent.click(screen.getByText("Hide All"));
    expect(mockSetColumnVisibility).toHaveBeenCalled();
  });

  it("toggles the swtich column where selected column in undefined", () => {
    const mockSetColumnVisibility = jest.fn((cb: ({ columnA }: any) => void) =>
      cb({ columnA: undefined }),
    );
    baseProps.table.setColumnVisibility = mockSetColumnVisibility;

    testRender(<WorkbookHeader {...baseProps} />);
    fireEvent.click(screen.getByText("Columns"));

    const switchEl = screen.getAllByTestId("switch-element")[0];
    fireEvent.click(switchEl);

    expect(switchEl).toBeChecked();
    expect(mockSetColumnVisibility).toHaveBeenCalled();
  });

  it("toggles the swtich column where selected column in false", () => {
    const mockSetColumnVisibility = jest.fn((cb: ({ columnA }: any) => void) =>
      cb({ columnA: false }),
    );
    baseProps.table.setColumnVisibility = mockSetColumnVisibility;

    testRender(<WorkbookHeader {...baseProps} />);
    fireEvent.click(screen.getByText("Columns"));

    const switchEl = screen.getAllByTestId("switch-element")[0];
    fireEvent.click(switchEl);

    expect(switchEl).toBeChecked();
    expect(mockSetColumnVisibility).toHaveBeenCalled();
  });

  it("toggles the swtich column where selected column in true", () => {
    const mockSetColumnVisibility = jest.fn((cb: ({ columnA }: any) => void) =>
      cb({ columnA: true }),
    );
    baseProps.table.setColumnVisibility = mockSetColumnVisibility;

    testRender(<WorkbookHeader {...baseProps} />);
    fireEvent.click(screen.getByText("Columns"));

    const switchEl = screen.getAllByTestId("switch-element")[0];
    fireEvent.click(switchEl);

    expect(switchEl).toBeChecked();
    expect(mockSetColumnVisibility).toHaveBeenCalled();
  });

  it("toggles the swtich column with localStorage props", () => {
    jest.spyOn(Storage.prototype, "getItem").mockImplementation((key) => {
      if (key === "projectTableConfig") {
        return JSON.stringify({ columnVisibility: { columnA: false } });
      }

      return null;
    });
    const mockSetColumnVisibility = jest.fn((cb: () => void) => cb());
    baseProps.table.setColumnVisibility = mockSetColumnVisibility;

    testRender(<WorkbookHeader {...baseProps} />);
    fireEvent.click(screen.getByText("Columns"));

    const switchEl = screen.getAllByTestId("switch-element")[0];
    fireEvent.click(switchEl);

    expect(switchEl).toBeChecked();
    expect(mockSetColumnVisibility).toHaveBeenCalled();
  });
});

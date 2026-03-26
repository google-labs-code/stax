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

import ColumnHeader, {
  renderColumnActionsMenuItems,
} from "@/app/(authRoutes)/projects/[id]/components/ColumnHeader";
import { WorkbookItem } from "@/app/(authRoutes)/projects/[id]/types";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { triggerDateRangeInputChange } from "@/tests-unit/helpers";
import { testRenderLite } from "@/tests-unit/render";
import { Menu } from "@mantine/core";
import { fireEvent, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  MRT_Column,
  MRT_DefinedColumnDef,
  MRT_TableInstance,
} from "mantine-react-table";

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext");

const mockSetDisplayedFilters = jest.fn();
const mockSetSorting = jest.fn();
const mockSetColumnVisibility = jest.fn();
const mockSetColumnOrder = jest.fn();

const baseColumn: any = {
  id: "name",
  columnDef: { header: "Name" },
  setFilterValue: jest.fn(),
  getFilterValue: () => "",
};

(useProjectContext as jest.Mock).mockReturnValue({
  projectState: {
    displayedFilters: {},
  },
  projectActions: {
    setDisplayedFilters: mockSetDisplayedFilters,
    setSorting: mockSetSorting,
  },
});

const createMockColumn = (
  options: Partial<MRT_Column<WorkbookItem>>,
  colName?: string,
): MRT_Column<WorkbookItem> =>
  ({
    id: colName || "name",
    columnDef: {
      id: colName || "name",
      header: colName || "Name",
      enableColumnFilter: true,
      enableSorting: true,
    },
    getFilterValue: jest.fn(),
    setFilterValue: jest.fn(),
    ...options,
  }) as unknown as MRT_Column<WorkbookItem>;

const createMockTable = (): MRT_TableInstance<WorkbookItem> =>
  ({
    options: {
      state: {
        columnVisibility: { name: true },
      },
    },
    getVisibleLeafColumns: () =>
      [
        { id: "select" },
        { id: "firstColumn" },
        { id: "name" },
        { id: "lastColumn" },
        { id: "actions" },
      ] as MRT_Column<WorkbookItem>[],
    setColumnVisibility: mockSetColumnVisibility,
    setColumnOrder: mockSetColumnOrder,
  }) as unknown as MRT_TableInstance<WorkbookItem>;

describe("renderColumnActionsMenuItems", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders menu items and calls setDisplayedFilters on "Search"', () => {
    const column = createMockColumn({});
    const table = createMockTable();

    const { getByText } = testRenderLite(
      <Menu opened>{renderColumnActionsMenuItems({ column, table })}</Menu>,
    );

    const searchItem = getByText("Search");
    expect(searchItem).toBeInTheDocument();

    fireEvent.click(searchItem);
    expect(mockSetDisplayedFilters).toHaveBeenCalledWith(
      expect.objectContaining({ name: true }),
    );
  });

  it('renders menu items and calls setColumnVisibility on "Hide column"', () => {
    const column = createMockColumn({});
    const table = createMockTable();

    const { getByText } = testRenderLite(
      <Menu opened>{renderColumnActionsMenuItems({ column, table })}</Menu>,
    );

    const hideColumnItem = getByText("Hide column");
    expect(hideColumnItem).toBeInTheDocument();

    fireEvent.click(hideColumnItem);
    expect(mockSetColumnVisibility).toHaveBeenCalledWith(
      expect.objectContaining({ name: false }),
    );
  });

  it("should move the column to the left", () => {
    const column = createMockColumn({});
    const table = createMockTable();

    const { getByText } = testRenderLite(
      <Menu opened>{renderColumnActionsMenuItems({ column, table })}</Menu>,
    );

    const moveColumnItem = getByText("Move left");
    expect(moveColumnItem).toBeInTheDocument();

    fireEvent.click(moveColumnItem);
    expect(mockSetColumnOrder).toHaveBeenCalledWith([
      "select",
      "name",
      "firstColumn",
      "lastColumn",
      "actions",
    ]);
  });

  it("should move the column to the left if it is a first column", () => {
    const column = createMockColumn({}, "firstColumn");
    const table = createMockTable();

    const { getByText } = testRenderLite(
      <Menu opened>{renderColumnActionsMenuItems({ column, table })}</Menu>,
    );

    const moveColumnItem = getByText("Move left");
    expect(moveColumnItem).toBeInTheDocument();

    fireEvent.click(moveColumnItem);
    expect(mockSetColumnOrder).not.toHaveBeenCalled();
  });

  it("should move the column to the right", () => {
    const column = createMockColumn({});
    const table = createMockTable();

    const { getByText } = testRenderLite(
      <Menu opened>{renderColumnActionsMenuItems({ column, table })}</Menu>,
    );

    const moveColumnItem = getByText("Move right");
    expect(moveColumnItem).toBeInTheDocument();

    fireEvent.click(moveColumnItem);
    expect(mockSetColumnOrder).toHaveBeenCalledWith([
      "select",
      "firstColumn",
      "lastColumn",
      "name",
      "actions",
    ]);
  });

  it("should not move the column to the right if it is the last column", () => {
    const column = createMockColumn({}, "lastColumn");
    const table = createMockTable();

    const { getByText } = testRenderLite(
      <Menu opened>{renderColumnActionsMenuItems({ column, table })}</Menu>,
    );

    const moveColumnItem = getByText("Move right");
    expect(moveColumnItem).toBeInTheDocument();

    fireEvent.click(moveColumnItem);
    expect(mockSetColumnOrder).not.toHaveBeenCalled();
  });

  it('calls setSorting on "Sort A to Z"', () => {
    const column = createMockColumn({});
    const table = createMockTable();

    const { getByText } = testRenderLite(
      <Menu opened>{renderColumnActionsMenuItems({ column, table })}</Menu>,
    );

    fireEvent.click(getByText("Sort A to Z"));
    expect(mockSetSorting).toHaveBeenCalledWith([{ id: "name", desc: false }]);
  });

  it('calls setSorting on "Sort Z to A"', () => {
    const column = createMockColumn({});
    const table = createMockTable();

    const { getByText } = testRenderLite(
      <Menu opened>{renderColumnActionsMenuItems({ column, table })}</Menu>,
    );

    fireEvent.click(getByText("Sort Z to A"));
    expect(mockSetSorting).toHaveBeenCalledWith([{ id: "name", desc: true }]);
  });

  it("disables movement actions for special columns", () => {
    const column = createMockColumn({
      id: "select",
      columnDef: {
        id: "select",
        header: "mocked column",
      } as MRT_DefinedColumnDef<WorkbookItem>,
    });
    const table = createMockTable();

    const { queryByText } = testRenderLite(
      <Menu opened>{renderColumnActionsMenuItems({ column, table })}</Menu>,
    );

    expect(queryByText("Move left")).not.toBeInTheDocument();
    expect(queryByText("Move right")).not.toBeInTheDocument();
  });
});

describe("ColumnHeader", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders tooltip and no input when useTableStateContext is null", () => {
    (useProjectContext as jest.Mock).mockReturnValue(null);

    const { getByText } = testRenderLite(
      <ColumnHeader column={baseColumn} tooltip="My tooltip" />,
    );

    expect(getByText("Name")).toBeInTheDocument();
  });

  it('renders DateRangeInput when filterType is "date-range"', () => {
    const column: any = {
      ...baseColumn,
      setFilterValue: jest.fn(),
      getFilterValue: () => "",
    };

    (useProjectContext as jest.Mock).mockReturnValue({
      projectActions: {
        setDisplayedFilters: mockSetDisplayedFilters,
      },
      projectState: {
        displayedFilters: { name: true },
      },
    });

    const { getByText } = testRenderLite(
      <ColumnHeader column={column} filterType="date-range" />,
    );

    expect(getByText("Last 15 days")).toBeInTheDocument();
  });

  it("calls setFilterValue and does NOT hide filter when input has value", () => {
    const setFilterValue = jest.fn();
    const column: any = {
      ...baseColumn,
      setFilterValue,
      getFilterValue: () => "",
    };

    (useProjectContext as jest.Mock).mockReturnValue({
      projectActions: {
        setDisplayedFilters: mockSetDisplayedFilters,
      },
      projectState: {
        displayedFilters: { name: true },
      },
    });

    const { getByPlaceholderText } = testRenderLite(
      <ColumnHeader column={column} />,
    );

    const input = getByPlaceholderText("SEARCH");
    fireEvent.change(input, { target: { value: "Test" } });

    expect(setFilterValue).toHaveBeenCalledWith("Test");
    expect(mockSetDisplayedFilters).not.toHaveBeenCalledWith(
      expect.objectContaining({ name: false }),
    );
  });

  it("hides filter on blur if input is empty", () => {
    const setFilterValue = jest.fn();
    const column: any = {
      ...baseColumn,
      setFilterValue,
      getFilterValue: () => "",
    };

    (useProjectContext as jest.Mock).mockReturnValue({
      projectActions: {
        setDisplayedFilters: mockSetDisplayedFilters,
      },
      projectState: {
        displayedFilters: { name: true },
      },
    });

    const { getByPlaceholderText } = testRenderLite(
      <ColumnHeader column={column} />,
    );

    const input = getByPlaceholderText("SEARCH");
    fireEvent.blur(input);

    expect(mockSetDisplayedFilters).toHaveBeenCalledWith(
      expect.objectContaining({ name: false }),
    );
  });

  it("hides filter if empty string is passed for input", () => {
    const setFilterValue = jest.fn();
    const column: any = {
      ...baseColumn,
      setFilterValue,
      getFilterValue: () => "Test",
    };

    (useProjectContext as jest.Mock).mockReturnValue({
      projectActions: {
        setDisplayedFilters: mockSetDisplayedFilters,
      },
      projectState: {
        displayedFilters: { name: true },
      },
    });

    const { getByPlaceholderText } = testRenderLite(
      <ColumnHeader column={column} />,
    );

    const input = getByPlaceholderText("SEARCH");
    fireEvent.change(input, { target: { value: "" } });

    expect(setFilterValue).toHaveBeenCalledWith("");
    expect(mockSetDisplayedFilters).toHaveBeenCalledWith(
      expect.objectContaining({ name: false }),
    );
  });

  it("renders tooltip if there is useProjectContext and tooltip is set", () => {
    const column: any = {
      ...baseColumn,
      setFilterValue: jest.fn(),
      getFilterValue: () => "",
    };

    (useProjectContext as jest.Mock).mockReturnValue({
      projectActions: {
        setDisplayedFilters: mockSetDisplayedFilters,
      },
      projectState: {
        displayedFilters: {},
      },
    });

    const { getByText } = testRenderLite(
      <ColumnHeader column={column} tooltip={"Test tooltip"} />,
    );

    expect(getByText("Name")).toBeInTheDocument();
  });

  it("should call setFilterValue if a date is selected", async () => {
    const setFilterValue = jest.fn();
    const column: any = {
      ...baseColumn,
      setFilterValue,
      getFilterValue: () => "",
    };

    (useProjectContext as jest.Mock).mockReturnValue({
      projectActions: {
        setDisplayedFilters: mockSetDisplayedFilters,
      },
      projectState: {
        displayedFilters: { name: true },
      },
    });

    const customScreen = testRenderLite(
      <ColumnHeader column={column} filterType="date-range" />,
    );

    await triggerDateRangeInputChange({ customScreen });

    expect(setFilterValue).toHaveBeenCalled();
  });

  it("should clear the selected filter", async () => {
    const setFilterValue = jest.fn();
    const column: any = {
      ...baseColumn,
      setFilterValue,
      getFilterValue: () => "",
    };

    (useProjectContext as jest.Mock).mockReturnValue({
      projectActions: {
        setDisplayedFilters: mockSetDisplayedFilters,
      },
      projectState: {
        displayedFilters: { name: true },
      },
    });

    const customScreen = testRenderLite(
      <ColumnHeader column={column} filterType="date-range" />,
    );

    await triggerDateRangeInputChange({ customScreen });

    await waitFor(() => {
      expect(
        customScreen.getByRole("button", { name: /clear value/i }),
      ).toBeInTheDocument();
    });

    await userEvent.click(
      customScreen.getByRole("button", { name: /clear value/i }),
    );

    expect(mockSetDisplayedFilters).toHaveBeenCalledWith(
      expect.objectContaining({ name: false }),
    );
  });
});

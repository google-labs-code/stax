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

import { CustomBottomToolbar } from "@/components/CustomBottomToolbar";
import { fireEvent, screen } from "@testing-library/react";
import { testRender } from "../render";

const mockTable = {
  getState: () => ({
    rowSelection: { 0: true, 1: true },
  }),
  getRowCount: () => 100,
  setRowSelection: jest.fn(),
  getSelectedRowModel: jest.fn().mockReturnValue({ flatRows: [] }),
  options: {
    data: [{ chat_turn_id: "testId1" }, { chat_turn_id: "testId2" }],
  },
};

describe("CustomBottomToolbar", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders selected row count and clear selection", () => {
    testRender(
      <CustomBottomToolbar
        table={mockTable as any}
        totalSize={200}
        selectAllRowsInProject={false}
      />
    );

    expect(screen.getByText("2 of 200 selected")).toBeInTheDocument();

    fireEvent.click(screen.getByText("Clear selection"));
    expect(mockTable.setRowSelection).toHaveBeenCalledWith({});
  });

  it("displays total size if selectAllRowsInProject is true", () => {
    testRender(
      <CustomBottomToolbar
        table={mockTable as any}
        totalSize={999}
        selectAllRowsInProject
      />
    );

    expect(screen.getByText("999 of 999 selected")).toBeInTheDocument();
  });

  it("renders Pagination component and handles page change", () => {
    const setPage = jest.fn();
    const refetchProject = jest.fn();

    testRender(
      <CustomBottomToolbar
        table={mockTable as any}
        page={2}
        setPage={setPage}
        pageSize={15}
        totalPages={5}
        refetchProject={refetchProject}
      />
    );
    const pagination = screen.getByText("Show");
    expect(pagination).toBeInTheDocument();
  });

  it("calls onClearSelection when provided", () => {
    const onClearSelection = jest.fn();
    
    testRender(
      <CustomBottomToolbar
        table={mockTable as any}
        totalSize={200}
        onClearSelection={onClearSelection}
      />
    );

    fireEvent.click(screen.getByText("Clear selection"));
    expect(onClearSelection).toHaveBeenCalled();
    expect(mockTable.setRowSelection).not.toHaveBeenCalled();
  });

  it("handles page size changes correctly", () => {
    const setPage = jest.fn();
    const setPageSize = jest.fn();
    const refetchProject = jest.fn();

    const { container } = testRender(
      <CustomBottomToolbar
        table={mockTable as any}
        setPage={setPage}
        setPageSize={setPageSize}
        refetchProject={refetchProject}
        pageSize={15}
      />
    );

    const selectInput = container.querySelector(".mantine-Select-input");
    if (selectInput) {
      fireEvent.click(selectInput);
    }
    
    const option100 = screen.getByText("100");
    fireEvent.click(option100);

    expect(setPage).toHaveBeenCalledWith(1);
    expect(setPageSize).toHaveBeenCalledWith(100);
    expect(refetchProject).toHaveBeenCalled();
  });

  it("applies different width classes based on pageSize", () => {
    const { container } = testRender(
      <CustomBottomToolbar
        table={mockTable as any}
        pageSize={-1}
      />
    );

    const selectInput = container.querySelector(".mantine-Select-input");
    expect(selectInput).toHaveClass("!w-[75px]");
  });

  it("includes addedRowsCount in the total when provided", () => {
    testRender(
      <CustomBottomToolbar
        table={mockTable as any}
        totalSize={100}
        addedRowsCount={50}
      />
    );

    expect(screen.getByText("2 of 150 selected")).toBeInTheDocument();
  });

  it("hides the page size selector when showPageSizeSelector is false", () => {
    testRender(
      <CustomBottomToolbar
        table={mockTable as any}
        showPageSizeSelector={false}
      />
    );

    expect(screen.queryByText("Show")).not.toBeInTheDocument();
  });
});
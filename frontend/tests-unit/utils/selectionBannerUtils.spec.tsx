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

import { useSelectionBanner } from "@/utils/selectionBannerUtils";
import { act, renderHook } from "@testing-library/react";

describe("useSelectionBanner", () => {
  const sampleData = [
    { id: 1, chat_turn_id: "testId1" },
    { id: 2, chat_turn_id: "testId2" },
    { id: 3, chat_turn_id: "testId3" },
  ];
  const totalSize = 100;

  let rowSelection: Record<string, boolean>;
  let setRowSelection: jest.Mock;
  let setRowDataSelection: jest.Mock;
  let setRowTableSelection: jest.Mock;

  beforeEach(() => {
    rowSelection = {};
    setRowSelection = jest.fn((newSelection) => {
      rowSelection = newSelection;
    });
    setRowDataSelection = jest.fn();
    setRowTableSelection = jest.fn();
  });

  it("initializes with correct default values", () => {
    const { result } = renderHook(() =>
      useSelectionBanner({
        rowSelection,
        setRowSelection,
        setRowDataSelection,
        setRowTableSelection,
        data: sampleData,
        page: 1,
        totalSize,
      }),
    );

    expect(result.current.selectAllRowsInProject).toBe(false);
    expect(result.current.showBanner).toBe(false);
    expect(result.current.allRowsSelected).toBe(false);
    expect(result.current.currentPageSize).toBe(3);
    expect(result.current.totalInProject).toBe(100);
    expect(result.current.tableRef.current).toBeNull();
  });

  it("sets selectAllRowsInProject to true when handleSelectAllRows is called", () => {
    const { result } = renderHook(() =>
      useSelectionBanner({
        rowSelection,
        setRowSelection,
        setRowDataSelection,
        setRowTableSelection,
        data: sampleData,
        page: 1,
        totalSize,
      }),
    );

    act(() => {
      result.current.handleSelectAllRows();
    });

    expect(result.current.selectAllRowsInProject).toBe(false);
  });

  it("clears selection when handleClearSelection is called", () => {
    const { result } = renderHook(() =>
      useSelectionBanner({
        rowSelection: { "1": true },
        setRowSelection,
        setRowDataSelection,
        setRowTableSelection,
        data: sampleData,
        page: 1,
        totalSize,
      }),
    );

    act(() => {
      result.current.handleClearSelection();
    });

    expect(setRowSelection).toHaveBeenCalledWith({});
    expect(result.current.selectAllRowsInProject).toBe(false);
  });

  it("clears data and table selection when rowSelection is empty", () => {
    renderHook(() =>
      useSelectionBanner({
        rowSelection: {},
        setRowSelection,
        setRowDataSelection,
        setRowTableSelection,
        data: sampleData,
        page: 1,
        totalSize,
      }),
    );

    expect(setRowDataSelection).toHaveBeenCalledWith([]);
    expect(setRowTableSelection).toHaveBeenCalledWith([]);
  });

  it("detects all rows selected on page", () => {
    const selected = {
      testId1: true,
      testId2: true,
      testId3: true,
    };

    const { result } = renderHook(() =>
      useSelectionBanner({
        rowSelection: selected,
        setRowSelection,
        setRowDataSelection,
        setRowTableSelection,
        data: sampleData,
        page: 1,
        totalSize,
      }),
    );

    expect(result.current.allRowsSelected).toBe(true);
    expect(result.current.showBanner).toBe(true);
  });

  it("hides banner if no rows selected", () => {
    const { result } = renderHook(() =>
      useSelectionBanner({
        rowSelection: {},
        setRowSelection,
        setRowDataSelection,
        setRowTableSelection,
        data: sampleData,
        page: 1,
        totalSize,
      }),
    );

    expect(result.current.showBanner).toBe(false);
  });

  it("turns off selectAllRowsInProject if a row is unchecked", () => {
    const { result, rerender } = renderHook(
      (props: any) => useSelectionBanner(props),
      {
        initialProps: {
          rowSelection: { "1": true },
          setRowSelection,
          setRowDataSelection,
          setRowTableSelection,
          data: sampleData,
          page: 1,
          totalSize,
        },
      },
    );

    act(() => {
      result.current.handleSelectAllRows();
    });

    rerender({
      rowSelection: {
        1: false,
      },
      setRowSelection,
      setRowDataSelection,
      setRowTableSelection,
      data: sampleData,
      page: 1,
      totalSize,
    });

    expect(result.current.selectAllRowsInProject).toBe(false);
  });

  it("should select all rows automatically if selectAllRowsInProject is true", () => {
    const { result } = renderHook((props: any) => useSelectionBanner(props), {
      initialProps: {
        rowSelection: { "1": false },
        setRowSelection,
        setRowDataSelection,
        setRowTableSelection,
        data: sampleData,
        page: 1,
        totalSize,
      },
    });

    act(() => {
      result.current.setSelectAllRowsInProject(true);
    });

    const payload: Record<string, boolean> = {};
    for (const item of sampleData) {
      payload[item.chat_turn_id] = true;
    }

    expect(setRowSelection).toHaveBeenCalledWith(payload);
  });

  it("should not select all rows automatically if selectAllRowsInProject is false", () => {
    const { result } = renderHook((props: any) => useSelectionBanner(props), {
      initialProps: {
        rowSelection: { "1": true },
        setRowSelection,
        setRowDataSelection,
        setRowTableSelection,
        data: sampleData,
        page: 1,
        totalSize,
      },
    });

    act(() => {
      result.current.setSelectAllRowsInProject(false);
    });

    expect(setRowSelection).not.toHaveBeenCalled();
  });

  it("should reset allRowsSelected and hide banner if the page is changed", () => {
    const selected = {
      testId1: true,
      testId2: true,
      testId3: true,
    };
    const unSelected = {
      testId1: false,
      testId2: false,
      testId3: true,
    };

    const { result, rerender } = renderHook(
      (props: any) => useSelectionBanner(props),
      {
        initialProps: {
          rowSelection: selected,
          setRowSelection,
          setRowDataSelection,
          setRowTableSelection,
          data: sampleData,
          page: 1,
          totalSize,
        },
      },
    );

    rerender({
      rowSelection: unSelected,
      setRowSelection,
      setRowDataSelection,
      setRowTableSelection,
      data: sampleData,
      page: 2,
      totalSize,
    });

    expect(result.current.allRowsSelected).toBe(false);
    expect(result.current.showBanner).toBe(false);
  });
});

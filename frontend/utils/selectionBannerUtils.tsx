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

import { WorkbookItem } from "@/app/(authRoutes)/projects/[id]/types";
import { Row } from "@tanstack/react-table";
import { useEffect, useMemo, useRef, useState } from "react";

interface UseSelectionBannerProps {
  rowSelection: Record<string, boolean>;
  setRowSelection: (selection: Record<string, boolean>) => void;
  setRowDataSelection: (selection: WorkbookItem[]) => void;
  setRowTableSelection: (selection: Row<WorkbookItem>[]) => void;
  data: any[] | undefined;
  page: number;
  totalSize: number;
}

interface SelectionBannerState {
  tableRef: React.RefObject<HTMLDivElement>;
  showBanner: boolean;
  allRowsSelected: boolean;
  selectAllRowsInProject: boolean;
  setSelectAllRowsInProject: React.Dispatch<React.SetStateAction<boolean>>;
  handleSelectAllRows: () => void;
  handleClearSelection: () => void;
  currentPageSize: number;
  totalInProject: number;
}

export function useSelectionBanner({
  rowSelection,
  setRowSelection,
  setRowDataSelection,
  setRowTableSelection,
  data,
  page,
  totalSize,
}: UseSelectionBannerProps): SelectionBannerState {
  const [selectAllRowsInProject, setSelectAllRowsInProject] =
    useState<boolean>(false);
  const [allRowsSelected, setAllRowsSelected] = useState<boolean>(false);
  const tableRef = useRef<HTMLDivElement>(null);

  const selectedRowsCount = Object.values(rowSelection).filter(Boolean).length;

  useEffect(() => {
    if (selectAllRowsInProject && selectedRowsCount === 0) {
      setSelectAllRowsInProject(false);
    }
  }, [selectedRowsCount, selectAllRowsInProject]);

  const handleSelectAllRows = () => {
    setSelectAllRowsInProject(true);
  };

  const handleClearSelection = () => {
    setSelectAllRowsInProject(false);
    setRowSelection({});
  };

  useEffect(() => {
    if (selectedRowsCount === 0) {
      setRowDataSelection([]);
      setRowTableSelection([]);
    }
  }, [selectedRowsCount]);

  useEffect(() => {
    if (selectAllRowsInProject && data) {
      const selectedRows: Record<string, boolean> = data.reduce(
        (acc, item) => {
          acc[item.chat_turn_id] = true;

          return acc;
        },
        {} as Record<string, boolean>,
      );

      setRowSelection(selectedRows);
    }
  }, [data, selectAllRowsInProject]);

  const areAllRowsOnPageSelected = useMemo(() => {
    return data ? data.every((i: any) => rowSelection[i.chat_turn_id]) : false;
  }, [data, rowSelection]);

  // update allRowsSelected if rowSelection, data or page changes
  useEffect(() => {
    if (areAllRowsOnPageSelected) {
      setAllRowsSelected(true);
    } else {
      setAllRowsSelected(false);
    }
  }, [areAllRowsOnPageSelected, page]);

  const showBanner =
    (allRowsSelected || selectAllRowsInProject) && selectedRowsCount > 0;

  return {
    tableRef,
    showBanner,
    allRowsSelected,
    selectAllRowsInProject,
    setSelectAllRowsInProject,
    handleSelectAllRows,
    handleClearSelection,
    currentPageSize: data?.length || 0,
    totalInProject: totalSize,
  };
}

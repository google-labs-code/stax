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

import {
  getTableBodyCellClassName,
  getTableHeadCellClassName,
  getTableMantineContainerProps,
  getTableMantinePaperProps,
} from "@/components/table/tableProps";
import { getBaseTableConfig } from "@/config/getBaseTableConfig";
import { getSuccessNotificationConfig } from "@/config/notifications";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import {
  deleteAllSxSRowsQuery,
  deleteAllWorkbookRowsQuery,
} from "@/queries/clientQueries";
import { ProjectType } from "@/types";
import { Button, Group, Modal, ScrollArea, Stack, Text } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { UseMutationResult, useMutation } from "@tanstack/react-query";
import {
  MRT_ColumnDef,
  MRT_VisibilityState,
  MantineReactTable,
  useMantineReactTable,
} from "mantine-react-table";
import { Dispatch, SetStateAction, useCallback, useMemo } from "react";

import { useProjectContext } from "../../hooks/useProjectContext";
import { WorkbookItem } from "../types";

type WorkbookDeleteModalProps = {
  isOpened: boolean;
  onClose: () => void;
  initialColumnVisibility?: MRT_VisibilityState | undefined;
  initialColumnOrder?: MRT_ColumnDef<WorkbookItem>[];
  onRowDelete?: UseMutationResult<unknown, Error, string[], unknown>;
  selectedRows: WorkbookItem[];
  setRowSelection: Dispatch<SetStateAction<{}>>;
  setAddedRowsCount?: Dispatch<SetStateAction<number>>;
  onDelete?: () => void;
  onBeforeDelete?: () => void;
  selectAllRowsInProject?: boolean;
  projectId?: string;
  onClearBannerState?: () => void;
  projectType: ProjectType;
};

export default function WorkbookDeleteModal({
  isOpened,
  onClose,
  initialColumnOrder,
  selectedRows,
  initialColumnVisibility,
  setRowSelection,
  onRowDelete,
  onDelete,
  onBeforeDelete,
  selectAllRowsInProject = false,
  projectId,
  onClearBannerState,
  projectType,
  setAddedRowsCount,
}: WorkbookDeleteModalProps) {
  const { defaultProjectId } = useProjectsContext();
  const projectContext = useProjectContext?.();
  const refetchProject = projectContext?.projectQueries?.refetchProject;
  const finalProjectId = projectId || defaultProjectId;
  const selectedIds = useMemo(
    () =>
      selectedRows.map((row) =>
        projectType === ProjectType.SIDE_BY_SIDE
          ? row?.pairId || row?.id
          : row.chat_id,
      ),
    [selectedRows, projectType],
  );

  const deleteAllRowsMutation = useMutation({
    mutationFn: (projectId: string) => {
      notifications.show(
        getSuccessNotificationConfig(
          "Deleting all rows in the project. This may take some time for large projects.",
          "delete-all-started",
        ),
      );

      if (projectType === ProjectType.SIDE_BY_SIDE) {
        return deleteAllSxSRowsQuery(projectId);
      }

      return deleteAllWorkbookRowsQuery(projectId);
    },
    onSuccess: () => {
      notifications.show(
        getSuccessNotificationConfig(
          "All rows in the project have been deleted successfully.",
          "delete-all-success",
        ),
      );

      if (refetchProject) refetchProject();
    },
  });

  const handleDeletion = useCallback(() => {
    if (onBeforeDelete) {
      onBeforeDelete();
    }

    const shouldDeleteAll = selectAllRowsInProject && Boolean(finalProjectId);

    if (shouldDeleteAll) {
      deleteAllRowsMutation.mutate(finalProjectId as string);
      if (onClearBannerState) {
        onClearBannerState();
      }
    } else {
      if (onDelete) {
        onDelete();
      }

      if (onRowDelete) {
        onRowDelete.mutate(selectedIds as string[]);
      }
    }

    setAddedRowsCount && setAddedRowsCount(0);
    setRowSelection({});
    onClose();
  }, [
    selectAllRowsInProject,
    finalProjectId,
    selectedIds,
    onBeforeDelete,
    onDelete,
    onRowDelete,
    deleteAllRowsMutation,
    onClose,
    setRowSelection,
    onClearBannerState,
  ]);

  const isDeleteAllMode = selectAllRowsInProject && Boolean(finalProjectId);

  const tableConfig = getBaseTableConfig<WorkbookItem>();

  const modalTable = useMantineReactTable({
    ...tableConfig,
    columns: (initialColumnOrder || []).map((column) => {
      if (column.id === "updated_at") {
        return {
          ...column,
          enableResizing: false,
        };
      }

      return column;
    }),
    meta: {
      projectType,
    },
    data: selectedRows,
    enablePagination: false,
    enableColumnResizing: true,
    enableRowActions: false,
    enableTopToolbar: false,
    enableColumnActions: false,
    enableBottomToolbar: false,
    enableColumnFilters: false,
    enableSelectAll: false,
    enableSubRowSelection: false,
    enableRowSelection: false,
    enableSorting: false,
    enableFilters: false,
    layoutMode: "grid",
    initialState: {
      columnVisibility: initialColumnVisibility,
    },
    mantineTableHeadCellProps: ({ column }) => ({
      className: getTableHeadCellClassName(column.id),
    }),
    mantineTableBodyCellProps: ({ column }) => ({
      className: getTableBodyCellClassName(column.id),
    }),
    mantinePaperProps: ({ table }) => getTableMantinePaperProps(table),
    mantineTableContainerProps: ({ table }) =>
      getTableMantineContainerProps(table),
    defaultColumn: {
      Cell: (props) => (
        <Text className="text-secondaryDark text-body-14">
          {props.renderedCellValue}
        </Text>
      ),
    },
    positionToolbarAlertBanner: "bottom",
    displayColumnDefOptions: {
      "mrt-row-actions": {
        enableResizing: false,
      },
    },
  });

  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      size="xl"
      title={
        <Text className="flex items-center gap-2 !font-medium text-title-22">
          {isDeleteAllMode ? "Delete all rows" : "Delete selected row(s)"}
        </Text>
      }
      centered
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[25px]",
      }}
    >
      <Stack>
        {isDeleteAllMode ? (
          <Text className="text-secondaryDark text-body-14 mb-4">
            You are about to delete all rows in this project. This action cannot
            be undone.
          </Text>
        ) : (
          <ScrollArea.Autosize maw="100%" scrollbars="xy" mah={500}>
            <MantineReactTable table={modalTable} />
          </ScrollArea.Autosize>
        )}

        <Group justify="flex-end">
          <Button
            onClick={onClose}
            className="text-secondaryDark h-[48px] border-neutrals-300 large-btn hover:bg-veryLightSilver"
            variant="outline"
          >
            <Text className="text-title-14">Cancel</Text>
          </Button>
          <Button
            className="h-[48px] large-btn"
            onClick={handleDeletion}
            size="xl"
            variant="filled"
          >
            Delete
          </Button>
        </Group>
      </Stack>
    </Modal>
  );
}

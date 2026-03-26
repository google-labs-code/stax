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

"use client";

import AddRunsToProjectModal from "@/components/AddRunsToProjectModal";
import BreadcrumbSegment from "@/components/BreadcrumbSegment";
import BulkActionMenu from "@/components/BulkActionMenu";
import CreateNewEntityModal from "@/components/CreateNewProjectModal";
import { CustomBottomToolbar } from "@/components/CustomBottomToolbar";
import FormatDate from "@/components/FormatDate";
import ManageTagsModal from "@/components/ManageTagsModal";
import MaterialIcon from "@/components/MaterialIcon";
import Page from "@/components/Page";
import PageHeader from "@/components/PageHeader";
import { UploadDatasetModal } from "@/components/UploadDatasetModal";
import { tagsColumn } from "@/components/table/columns/tagsColumn";
import {
  expectedOutputColumn,
  inputDatasetColumn,
  outputColumn,
} from "@/components/table/tableColumns";
import {
  getTableBodyCellClassName,
  getTableHeadCellClassName,
  getTableMantineContainerProps,
  getTableMantinePaperProps,
} from "@/components/table/tableProps";
import { getBaseTableConfig } from "@/config/getBaseTableConfig";
import { getSuccessNotificationConfig } from "@/config/notifications";
import { routes } from "@/config/routes";
import {
  createDatasetRowQuery,
  deleteDatasetRowsBulkQuery,
  getChatsExportQuery,
  getDatasetQuery,
  getDatasetRowsQuery,
  updateDatasetRowQuery,
} from "@/queries/clientQueries";
import { DatasetRowPayload } from "@/queries/types";
import {
  BulkActionMenuItem,
  DatasetTableMeta,
  ManageTagsModalType,
  ProjectComboboxItem,
  ProjectType,
  TableName,
  TagLinkEntityType,
  UploadDatasetModalSource,
} from "@/types";
import { jsonToCsvExport } from "@/utils/jsonToCsvExport";
import {
  ActionIcon,
  Box,
  Group,
  Menu,
  Text,
  UnstyledButton,
} from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import {
  MRT_ColumnDef,
  MantineReactTable,
  useMantineReactTable,
} from "mantine-react-table";
import { useParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";

import WorkbookDeleteModal from "../../projects/[id]/components/WorkbookDeleteModal";
import EditDatasetModal from "../components/EditDatasetModal";
import { Dataset, DatasetRow, DatasetRowsResponse } from "../types";

export default function DatasetPage() {
  const [
    isEditDatasetModalOpened,
    { open: openEditDatasetModal, close: closeEditDatasetModal },
  ] = useDisclosure(false);
  const [
    isManageTagsModalOpened,
    { open: openManageTagsModal, close: closeManageTagsModal },
  ] = useDisclosure(false);
  const [selectedProject, setSelectedProject] =
    useState<ProjectComboboxItem | null>(null);
  const [manageTagsType, setManageTagsType] = useState<ManageTagsModalType>(
    ManageTagsModalType.ADD,
  );
  const [
    isCreateNewProjectOpened,
    { open: openCreateNewProjectModal, close: closeCreateNewProjectModal },
  ] = useDisclosure(false);
  const [
    isDeleteModalOpened,
    { open: openDeleteModal, close: closeDeleteModal },
  ] = useDisclosure(false);
  const [
    isAddToProjectOpened,
    { open: openAddToProject, close: closeAddToProject },
  ] = useDisclosure(false);
  const [
    isUploadModalOpened,
    { open: openUploadModal, close: closeUploadModal },
  ] = useDisclosure(false);

  const openCreateProject = () => {
    closeAddToProject();
    openCreateNewProjectModal();
  };

  const onBackClick = () => {
    closeCreateNewProjectModal();
    openAddToProject();
  };

  const [pageSize, setPageSize] = useState(30);
  const [page, setPage] = useState(1);

  const columns: MRT_ColumnDef<DatasetRow>[] = [
    {
      ...inputDatasetColumn,
    },
    {
      ...outputColumn,
    },
    {
      ...expectedOutputColumn,
    },
    {
      ...tagsColumn,
    },
    {
      accessorKey: "created_at",
      header: "Date added",
      size: 99,
      enableEditing: false,
      Cell: (props) => {
        const value = props.renderedCellValue;

        return (
          value && (
            <FormatDate
              className="items-center text-body-12"
              date={value as number}
            />
          )
        );
      },
    },
  ];
  const [dataset, setDataset] = useState<Dataset>();
  const [data, setData] = useState<DatasetRowsResponse>({
    workbook_rows: [],
    next_page_token: null,
    total_size: 0,
  });
  const [rowSelection, setRowSelection] = useState({});
  const [activeRows, setActiveRows] = useState<DatasetRow[]>([]);
  const navParams = useParams();
  const getDatasetData = useMutation({
    mutationFn: getDatasetQuery,
    onSuccess: (response) => {
      setDataset(response);
    },
  });

  const getData = useMutation({
    mutationFn: () => {
      return getDatasetRowsQuery(navParams.id?.toString(), pageSize, page - 1);
    },
    onSuccess: (response) => {
      setData(response);
    },
  });

  const createDatasetRow = useMutation({
    mutationFn: (params: { datasetId: string; data: DatasetRowPayload }) => {
      return createDatasetRowQuery(params.datasetId, params.data);
    },
    onSuccess: (res) => {
      if (page === 1) {
        setData((prevData) => {
          const updatedRows = [res, ...prevData.workbook_rows];
          if (updatedRows.length > pageSize) {
            updatedRows.pop();
          }

          return {
            ...prevData,
            workbook_rows: updatedRows,
            total_size: (prevData.total_size || 0) + 1,
          };
        });
      } else {
        setData((prevData) => ({
          ...prevData,
          total_size: (prevData.total_size || 0) + 1,
        }));
      }
    },
  });

  const updateInputMutation = useMutation({
    mutationFn: (params: {
      datasetId: string;
      rowOriginal: DatasetRow;
      data: DatasetRowPayload;
    }) => {
      return updateDatasetRowQuery(
        params.datasetId,
        params.rowOriginal.chat_turn_id || "",
        params.data,
      );
    },
    onSuccess: (_, params) => {
      showDataChangesNotification();
      params.rowOriginal.isInputLoading = false;

      setData((prevData) => {
        const workbookRows = prevData.workbook_rows.map((row) => {
          if (row.chat_turn_id === params.rowOriginal.chat_turn_id) {
            row.input = params.data.model_prompt || "";
          }

          return row;
        });

        return { ...prevData, workbook_rows: workbookRows };
      });
    },
  });

  const updateExpectedOutputMutation = useMutation({
    mutationFn: (params: {
      datasetId: string;
      rowOriginal: DatasetRow;
      data: DatasetRowPayload;
    }) => {
      return updateDatasetRowQuery(
        params.datasetId,
        params.rowOriginal.chat_turn_id || "",
        params.data,
      );
    },
    onSuccess: (_, params) => {
      showDataChangesNotification();
      params.rowOriginal.isExpectedOutputLoading = false;

      setData((prevData) => {
        const workbookRows = prevData.workbook_rows.map((row) => {
          if (row.chat_turn_id === params.rowOriginal.chat_turn_id) {
            row.expected_output = params.data.expected_output || "";
          }

          return row;
        });

        return { ...prevData, workbook_rows: workbookRows };
      });
    },
  });

  const showDataChangesNotification = () => {
    notifications.show(
      getSuccessNotificationConfig(
        "Changes saved to this dataset, but not existing projects. To apply to projects, re-import dataset.",
        "changes-saved-dataset",
      ),
    );
  };

  const getSelectedRows = () => {
    const rows = table.getSelectedRowModel().rows;
    const rowsData = rows && rows.map((row) => row.original);

    return rowsData;
  };

  const getChatsExportMutation = useMutation({
    mutationFn: (chatIds: string[]) => {
      return getChatsExportQuery(chatIds);
    },
    onSuccess: (res: any) => {
      jsonToCsvExport(
        res,
        `export-chats-${new Date().toISOString().slice(0, 10)}.csv`,
      );
    },
  });

  const updateRowFieldData = (
    chatTurnId: string,
    field: keyof DatasetRow,
    newValue: any,
  ) => {
    setData((prevData) => {
      return {
        ...prevData,
        workbook_rows: prevData.workbook_rows.map((row) => {
          if (row.chat_turn_id === chatTurnId) {
            return {
              ...row,
              [field]: newValue,
            };
          }

          return row;
        }),
      };
    });
  };

  const menuItems: BulkActionMenuItem[] = [
    {
      label: "Add tags",
      icon: "label",
      onClick: () => {
        setManageTagsType(ManageTagsModalType.ADD);
        setActiveRows(getSelectedRows());
        openManageTagsModal();
      },
    },
    {
      label: "Remove tags",
      icon: "label",
      onClick: () => {
        setManageTagsType(ManageTagsModalType.REMOVE);
        setActiveRows(getSelectedRows());
        openManageTagsModal();
      },
    },
    {
      label: "Export",
      icon: "download",
      onClick: () =>
        getChatsExportMutation.mutate(
          getSelectedRows().map((row) => row.chat_id),
        ),
    },
    {
      label: "Add to project",
      icon: "add",
      onClick: () => openAddToProject(),
    },
    {
      label: "Delete",
      icon: "delete",
      onClick: () => {
        setActiveRows(getSelectedRows());
        openDeleteModal();
      },
    },
  ];

  const tableConfig = getBaseTableConfig<DatasetRow>();

  const table = useMantineReactTable({
    ...tableConfig,
    columns,
    data: data?.workbook_rows || [],
    state: {
      isLoading: getData.isPending || createDatasetRow.isPending,
      rowSelection,
    },
    meta: {
      openTagsModal: (rows: DatasetRow[]) => {
        setManageTagsType(ManageTagsModalType.ADD);
        setActiveRows(rows);
        openManageTagsModal();
      },
      onTagRemove: (tagId: string, rowOriginal: DatasetRow) => {
        updateRowFieldData(
          rowOriginal.chat_turn_id || "",
          "tags",
          rowOriginal.tags?.filter((tag) => tag.id !== tagId),
        );
      },
      onTagAdd: (
        tag: { id: string; name: string },
        rowOriginal: DatasetRow,
      ) => {
        updateRowFieldData(rowOriginal.chat_turn_id || "", "tags", [
          ...(rowOriginal.tags || []),
          tag,
        ]);
      },
      onInputChange: (input: string, _: string, rowOriginal: DatasetRow) => {
        rowOriginal.isInputLoading = true;
        updateInputMutation.mutate({
          datasetId: dataset?.id || "",
          rowOriginal,
          data: {
            model_prompt: input,
          },
        });
      },
      onExpectedOutputChange: (
        newValue: string,
        currentValue: string,
        rowOriginal: DatasetRow,
      ) => {
        if (!rowOriginal.chat_turn_id) {
          return;
        }

        if (newValue !== currentValue && newValue !== "") {
          rowOriginal.isExpectedOutputLoading = true;
          updateExpectedOutputMutation.mutate({
            datasetId: dataset?.id || "",
            rowOriginal,
            data: {
              expected_output: newValue,
            },
          });
        }
      },
      tableName: TableName.DATASET,
    } as DatasetTableMeta,
    enablePagination: true,
    enableTopToolbar: true,
    enableSorting: false,
    enableColumnActions: false,
    onRowSelectionChange: setRowSelection,
    enableRowSelection: true,
    enableFilters: false,
    enableDensityToggle: false,
    enableFullScreenToggle: false,
    enableHiding: false,
    enableEditing: false,
    layoutMode: "grid",
    positionActionsColumn: "last",
    mantineSelectAllCheckboxProps: { size: "sm" },
    mantineSelectCheckboxProps: { size: "sm" },
    initialState: {
      pagination: { pageSize, pageIndex: 0 },
      columnPinning: {
        left: ["mrt-row-select"],
        right: ["mrt-row-actions"],
      },
    },
    mantineTableHeadCellProps: ({ column }) => {
      return { className: getTableHeadCellClassName(column.id) };
    },
    mantineTableBodyCellProps: ({ column }) => {
      return { className: getTableBodyCellClassName(column.id) };
    },
    mantinePaperProps: ({ table }) => {
      return getTableMantinePaperProps(table);
    },
    mantinePaginationProps: {
      showRowsPerPage: false,
    },
    mantineTableContainerProps: ({ table }) => {
      return getTableMantineContainerProps(table);
    },
    renderTopToolbar: ({ table }) => {
      const selectedRows = [];
      for (const rowNr in table.getState().rowSelection) {
        selectedRows.push(rowNr);
      }

      const disabled = !selectedRows.length;

      return (
        <Group className="mb-[15px] pt-1 px-1 flex flex-row justify-between gap-0">
          <Menu position="bottom">
            <Menu.Target>
              <UnstyledButton className="gap-sm z-10 flex items-center justify-center py-[8px] pl-[6px] pr-[12px] rounded-sm hover:bg-veryLightSilver">
                <MaterialIcon name="add" size={20} />
                <Text className="text-title-12">Add data</Text>
              </UnstyledButton>
            </Menu.Target>

            <Menu.Dropdown className="ml-[40px] mt-[-5px] rounded-lg">
              <Menu.Item
                className="p-[12px]"
                onClick={() => {
                  createDatasetRow.mutate({
                    datasetId: dataset?.id || "",
                    data: {
                      model_prompt: "",
                    },
                  });
                  showDataChangesNotification();
                }}
              >
                <Group gap="12px">
                  <MaterialIcon name="add" size={20} />
                  <Text className="text-body-14">Add row</Text>
                </Group>
              </Menu.Item>
              <Menu.Item className="p-[12px]" onClick={openUploadModal}>
                <Group gap="12px">
                  <MaterialIcon name="upload" size={20} />
                  <Text className="text-body-14">Import dataset</Text>
                </Group>
              </Menu.Item>
            </Menu.Dropdown>
          </Menu>

          <BulkActionMenu menuItems={menuItems} isDisabled={disabled} />
        </Group>
      );
    },
    renderRowActions: ({ row }) => (
      <Group className="flex w-[100%] justify-center">
        <Menu
          position="bottom-end"
          width={185}
          shadow="md"
          classNames={{ dropdown: "rounded-md", item: "py-[12px]" }}
        >
          <Menu.Target>
            <ActionIcon size={24} variant="transparent">
              <MaterialIcon name="more_vert" size={24} />
            </ActionIcon>
          </Menu.Target>

          <Menu.Dropdown>
            <Menu.Item
              onClick={() => {
                setManageTagsType(ManageTagsModalType.ADD);
                setActiveRows([row.original]);
                openManageTagsModal();
              }}
            >
              <Group>
                <MaterialIcon name="label" className="!font-light" size={20} />
                <Text>Add tags</Text>
              </Group>
            </Menu.Item>
            <Menu.Item
              onClick={() => {
                setManageTagsType(ManageTagsModalType.REMOVE);
                setActiveRows([row.original]);
                openManageTagsModal();
              }}
            >
              <Group>
                <MaterialIcon name="label" className="!font-light" size={20} />
                <Text>Remove tags</Text>
              </Group>
            </Menu.Item>
            <Menu.Item
              onClick={() => {
                setActiveRows([row.original]);
                openDeleteModal();
              }}
            >
              <Group>
                <MaterialIcon name="delete" className="!font-light" size={20} />
                <Text>Delete</Text>
              </Group>
            </Menu.Item>
          </Menu.Dropdown>
        </Menu>
      </Group>
    ),
    renderBottomToolbar: ({ table }) => {
      const totalPages = useMemo(() => {
        return data.total_size && Math.ceil(data.total_size / pageSize);
      }, [data.total_size, pageSize]);

      return (
        <CustomBottomToolbar
          table={table}
          nextPageToken={data.next_page_token}
          totalSize={data.total_size}
          totalPages={totalPages}
          setPage={setPage}
          pageSize={pageSize}
          page={page}
          setPageSize={setPageSize}
        />
      );
    },
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
        header: "",
      },
    },
  });
  const deleteDatasetRowsMutation = useMutation({
    mutationFn: deleteDatasetRowsBulkQuery,
    onSuccess: () => {
      getData.mutate();
      notifications.show(
        getSuccessNotificationConfig(
          "Dataset rows deleted successfully.",
          "dataset-rows-deleted-success",
        ),
      );
      closeDeleteModal();
    },
  });

  useEffect(() => {
    if (navParams.id) {
      const id = navParams?.id?.toString();
      getDatasetData.mutate(id);
      getData.mutate();
    }
  }, [navParams.id, page, pageSize]);

  return (
    <Page fullHeight>
      <Group>
        <PageHeader
          title={dataset?.name || ""}
          iconButton={{
            icon: "edit",
            onClick: () => {
              openEditDatasetModal();
            },
          }}
          breadcrumbs={
            <BreadcrumbSegment label="Datasets" routes={routes.datasets.root} />
          }
        />
      </Group>
      <Box className="rounded-xl border border-solid border-neutrals-300 bg-neutrals-50 p-[24px] md:h-[95%] 2xl:h-[98%]">
        {dataset && (
          <>
            <AddRunsToProjectModal
              isOpened={isAddToProjectOpened}
              onCreateNewProjectClick={openCreateProject}
              selectedProject={selectedProject}
              setSelectedProject={setSelectedProject}
              isDataset
              selectedWorkbookChatIds={getSelectedRows().map(
                (row) => row.chat_id,
              )}
              onClose={closeAddToProject}
              sourceProjectId={dataset.id}
            />

            <CreateNewEntityModal
              isOpened={isCreateNewProjectOpened}
              onClose={closeCreateNewProjectModal}
              setSelectedProject={setSelectedProject}
              onBackClick={onBackClick}
            />
            <UploadDatasetModal
              isOpened={isUploadModalOpened}
              onClose={() => {
                closeUploadModal();
              }}
              dataset={dataset}
              onSuccess={() => {
                notifications.show(
                  getSuccessNotificationConfig(
                    "Dataset uploaded successfully.",
                    "dataset-uploaded-success",
                  ),
                );
                getData.mutate();
              }}
              tooltipLabel={
                "Map the columns from your uploaded file to the corresponding columns in this dataset."
              }
              source={UploadDatasetModalSource.DATASET}
              projectType={ProjectType.POINTWISE}
            />

            {activeRows.length > 0 && (
              <ManageTagsModal
                isOpened={isManageTagsModalOpened}
                onClose={(addedTags, removedTagIds) => {
                  closeManageTagsModal();

                  if (addedTags || removedTagIds) {
                    setData((prevData) => {
                      if (!prevData) return prevData;

                      return {
                        ...prevData,
                        workbook_rows: prevData.workbook_rows.map((row) => {
                          if (
                            activeRows.some(
                              (activeRow) =>
                                activeRow.chat_turn_id === row.chat_turn_id,
                            )
                          ) {
                            let updatedTags = removedTagIds
                              ? (row.tags || []).filter(
                                  (tag) =>
                                    tag.id !== undefined &&
                                    !removedTagIds.includes(tag.id),
                                )
                              : row.tags || [];

                            if (addedTags) {
                              updatedTags = [...updatedTags, ...addedTags];
                            }

                            return {
                              ...row,
                              tags: updatedTags,
                            };
                          }
                          
                          return row;
                        }),
                      };
                    });
                  }
                }}
                entityType={TagLinkEntityType.CHAT}
                entityIds={activeRows.map((r) => r.chat_id)}
                type={manageTagsType}
              />
            )}

            <WorkbookDeleteModal
              isOpened={isDeleteModalOpened}
              onClose={closeDeleteModal}
              selectedRows={activeRows}
              setRowSelection={setRowSelection}
              initialColumnOrder={columns}
              initialColumnVisibility={{}}
              onDelete={async () => {
                if (activeRows.length > 0) {
                  deleteDatasetRowsMutation.mutate({
                    id: dataset?.id || "",
                    payload: {
                      chat_turn_ids: activeRows.map((r) => r.chat_turn_id),
                    },
                  });
                }
              }}
              projectType={ProjectType.POINTWISE}
            />

            <EditDatasetModal
              data={dataset}
              isOpened={isEditDatasetModalOpened}
              onClose={() => {
                closeEditDatasetModal();
                if (navParams.id) {
                  getDatasetData.mutate(navParams?.id?.toString());
                }
              }}
            />
          </>
        )}

        <MantineReactTable table={table} />
      </Box>
    </Page>
  );
}

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

import Card from "@/components/Card";
import { CustomBottomToolbar } from "@/components/CustomBottomToolbar";
import DeleteModal from "@/components/DeleteModal";
import MaterialIcon from "@/components/MaterialIcon";
import Page from "@/components/Page";
import PageHeader from "@/components/PageHeader";
import { UploadDatasetModal } from "@/components/UploadDatasetModal";
import { getBaseTableConfig } from "@/config/getBaseTableConfig";
import { getSuccessNotificationConfig } from "@/config/notifications";
import { routes } from "@/config/routes";
import {
  createDatasetQuery,
  createDatasetRowQuery,
  deleteDatasetQuery,
  getDatasetExportQuery,
  getDatasetsQuery,
} from "@/queries/clientQueries";
import { DatasetRowPayload } from "@/queries/types";
import {
  ProjectComboboxItem,
  ProjectType,
  UploadDatasetModalSource,
} from "@/types";
import dayjs from "@/utils/dayjsSetup";
import { jsonToCsvExport } from "@/utils/jsonToCsvExport";
import { ActionIcon, Group, Menu, Stack, Text } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { notifications } from "@mantine/notifications";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  MRT_ColumnDef,
  MRT_Row,
  MantineReactTable,
  useMantineReactTable,
} from "mantine-react-table";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

import AddRunsToProjectModal from "../../../components/AddRunsToProjectModal";
import CreateNewProjectModal from "../../../components/CreateNewProjectModal";
import CreateDatasetModal from "../projects/[id]/components/CreateDataset/Modal";
import { DatasetsTopToolbar } from "./components/DatasetsTopToolbar";
import EditDatasetModal from "./components/EditDatasetModal";
import { ShowDetailsModal } from "./components/ShowDetailsModal";
import { Dataset } from "./types";

export default function Datasets() {
  const [
    isUploadModalOpened,
    { open: openUploadModal, close: closeUploadModal },
  ] = useDisclosure(false);
  const [
    isEditDatasetModalOpened,
    { open: openEditDatasetModal, close: closeEditDatasetModal },
  ] = useDisclosure(false);
  const [isCreateDatasetModalOpened, { close: closeCreateDatasetModal }] =
    useDisclosure(false);
  const [
    isCreateNewProjectOpened,
    { open: openCreateNewProjectModal, close: closeCreateNewProjectModal },
  ] = useDisclosure(false);
  const [
    isAddToProjectOpened,
    { open: openAddToProject, close: closeAddToProject },
  ] = useDisclosure(false);
  const [
    isDeleteModalOpened,
    { open: openDeleteModal, close: closeDeleteModal },
  ] = useDisclosure(false);
  const [
    isShowDetailsModalOpened,
    { open: openShowDetailsModal, close: closeShowDetailsModal },
  ] = useDisclosure(false);
  const router = useRouter();
  const [selectedDatasetId, setSelectedDatasetId] = useState<string | null>(
    null,
  );
  const [selectedProject, setSelectedProject] =
    useState<ProjectComboboxItem | null>(null);
  const { data, refetch } = useQuery({
    queryKey: ["datasets"],
    queryFn: getDatasetsQuery,
  });
  const [rowSelection, setRowSelection] = useState<Record<string, boolean>>({});

  useEffect(() => {
    refetch();
  }, []);

  const [datasets, setDatasets] = useState<Dataset[]>([]);
  const [activeDatasetData, setActiveDatasetData] = useState<Dataset>();

  useEffect(() => {
    if (data?.user_data_sets) {
      setDatasets(data.user_data_sets);
    }
  }, [data]);

  const deleteDatasetMutation = useMutation({
    mutationFn: deleteDatasetQuery,
    onSuccess: () => {
      refetch();
    },
  });

  const openCreateProject = () => {
    closeAddToProject();
    openCreateNewProjectModal();
  };

  const onBackClick = () => {
    closeCreateNewProjectModal();
    openAddToProject();
  };

  const createDatasetRowMutation = useMutation({
    mutationFn: (params: { id: string; data: DatasetRowPayload }) => {
      return createDatasetRowQuery(params.id, params.data);
    },
    onSuccess: (_, params) => {
      router.push(`${routes.datasets.root}/${params.id}`);
    },
  });

  const createDatasetMutation = useMutation({
    mutationFn: createDatasetQuery,
    onSuccess: (res) => {
      createDatasetRowMutation.mutate({
        id: res.id,
        data: {
          model_prompt: "",
        },
      });
    },
  });

  const getDatasetExportMutation = useMutation({
    mutationFn: (dataset: Dataset) => {
      return getDatasetExportQuery(dataset.id);
    },
    onSuccess: (res: any, params: Dataset) => {
      jsonToCsvExport(
        res,
        `dataset-export-${params.name || "data"}-${new Date().toISOString().slice(0, 10)}.csv`,
      );
    },
  });

  const actionButtons = [
    {
      label: "Show details",
      icon: "view_list",
      onClick: (data: MRT_Row<Dataset>) => {
        setActiveDatasetData(data.original);
        openShowDetailsModal();
      },
    },
    {
      label: "Export",
      icon: "download",
      onClick: (data: MRT_Row<Dataset>) => {
        getDatasetExportMutation.mutate(data.original);
      },
    },
    {
      label: "Add to project",
      icon: "add",
      onClick: (data: MRT_Row<Dataset>) => {
        setSelectedDatasetId(data.original.id);
        openAddToProject();
      },
    },
    {
      label: "Edit",
      icon: "edit",
      onClick: (data: MRT_Row<Dataset>) => {
        setActiveDatasetData(data.original);
        openEditDatasetModal();
      },
    },
    {
      label: "Delete",
      icon: "delete",
      onClick: (data: MRT_Row<Dataset>) => {
        setActiveDatasetData(data.original);
        openDeleteModal();
      },
    },
  ];

  const columns: MRT_ColumnDef<Dataset>[] = [
    {
      accessorKey: "name",
      header: "Name",
      size: 65,
      Cell: ({ row, renderedCellValue }) => (
        <Link
          href={`datasets/${row.original.id}`}
          className="text-brand text-title-14"
        >
          {renderedCellValue}
        </Link>
      ),
    },
    {
      accessorKey: "description",
      header: "Description",
      size: 270,
    },
    {
      accessorKey: "updated_at",
      header: "Updated",
      size: 52,
      Cell: ({ row }) => (
        <Text> {dayjs(row.original.updated_at).fromNow()}</Text>
      ),
    },
  ];

  const tableConfig = getBaseTableConfig<Dataset>();

  const table = useMantineReactTable({
    ...tableConfig,
    columns,
    data: datasets,
    enablePagination: false,
    enableRowActions: true,
    onRowSelectionChange: setRowSelection,
    getRowId: (row) => row.id,
    enableTopToolbar: true,
    enableSorting: false,
    enableColumnActions: false,
    enableRowSelection: true,
    enableFilters: false,
    enableDensityToggle: false,
    enableFullScreenToggle: false,
    enableHiding: false,
    layoutMode: "grid",
    state: { rowSelection },
    positionActionsColumn: "last",
    mantineSelectAllCheckboxProps: { size: "sm" },
    mantineSelectCheckboxProps: { size: "sm" },
    initialState: {
      pagination: { pageSize: 10, pageIndex: 0 },
    },
    mantineTableHeadCellProps: ({ column }) => {
      let className =
        "p-[12px] text-title-11 uppercase !font-medium text-secondary bg-veryLightSilver border-0 border-lightSilver border-solid";

      if (column.id === "mrt-row-actions") {
        className += " actions-column";
      } else if (column.id === "mrt-row-select") {
        className +=
          " row-select-column !p-[10px] !bg-veryLightSilver no-shadow-column !shadow-none border-r-[1px] border-lightSilver";
      } else {
        className += " border-r-[1px]";
      }

      return { className };
    },

    mantineTableBodyCellProps: ({ column }) => {
      let className =
        "p-[12px] min-h-[44px] border-0 border-lightSilver border-solid";

      if (column.id === "mrt-row-actions") {
        className += " actions-column";
      } else if (column.id === "mrt-row-select") {
        className +=
          " row-select-column !p-[10px] no-shadow-column !shadow-none border-r-[1px] border-lightSilver";
      } else {
        className += " border-r-[1px]";
      }

      return { className };
    },
    mantinePaperProps: {
      ...tableConfig.mantinePaperProps,
      className: ` border-0 shadow-none`,
      "aria-label": "table",
    },
    mantineTableHeadRowProps: ({ table }) => {
      if (table.getRowCount() === 0) {
        return {
          className: "border-0",
        };
      }

      return {};
    },
    mantineTableBodyProps: ({ table }) => {
      if (table.getRowCount() === 0) {
        return {
          className: "!hidden",
        };
      }

      return {};
    },
    mantinePaginationProps: {
      showRowsPerPage: false,
    },
    mantineTableContainerProps: {
      className: "!rounded-lg border-[1px] border-solid border-lightSilver",
    },
    mantineBottomToolbarProps: ({ table }) => {
      if (table.getRowCount() === 0) {
        return {
          className: "!hidden",
        };
      }

      return {
        className: `border-0 shadow-none`,
      };
    },
    renderRowActions: ({ row }) => {
      return (
        <Group className="flex w-[100%] justify-center">
          <Menu
            position="bottom-end"
            disabled={!rowSelection}
            shadow="md"
            classNames={{
              dropdown: "rounded-lg shadow-sm border-0",
            }}
          >
            <Menu.Target>
              <ActionIcon size={24} variant="transparent">
                <MaterialIcon name="more_vert" size={24} />
              </ActionIcon>
            </Menu.Target>

            <Menu.Dropdown>
              {actionButtons.map((button, key) => (
                <Menu.Item
                  key={key}
                  className="p-[12px]"
                  onClick={() => {
                    button.onClick(row);
                  }}
                >
                  <Group className="align-center gap-lg flex flex-row items-center">
                    <MaterialIcon
                      name={button.icon}
                      size={20}
                      className="!font-light text-secondary"
                    />
                    <Text className="text-body-14">{button.label}</Text>
                  </Group>
                </Menu.Item>
              ))}
            </Menu.Dropdown>
          </Menu>
        </Group>
      );
    },
    renderBottomToolbar: ({ table }) => (
      <CustomBottomToolbar
        table={table}
        nextPageToken={null}
        totalSize={datasets.length}
        showPageSizeSelector={false}
      />
    ),
    defaultColumn: {
      Cell: (props) => (
        <Text className="text-secondaryDark text-body-14">
          {props.renderedCellValue}
        </Text>
      ),
    },
    displayColumnDefOptions: {
      "mrt-row-actions": {
        enableResizing: false,
        header: "",
      },
      "mrt-row-select": {
        enableResizing: false,
        size: 50,
      },
    },
    positionToolbarAlertBanner: "bottom",

    renderTopToolbar: () => (
      <DatasetsTopToolbar
        table={table}
        openUploadModal={openUploadModal}
        createDatasetMutation={createDatasetMutation}
        openDeleteModal={openDeleteModal}
        setActiveDatasetData={setActiveDatasetData}
      />
    ),
  });

  return (
    <Page fullHeight>
      <Group>
        <PageHeader title="Datasets" />
      </Group>

      <UploadDatasetModal
        isOpened={isUploadModalOpened}
        onClose={closeUploadModal}
        onSuccess={() => {
          refetch();
          notifications.show(
            getSuccessNotificationConfig(
              "Dataset uploaded successfully.",
              "dataset-uploaded-success",
            ),
          );
          closeUploadModal();
        }}
        onDatasetCreated={() => {
          refetch();
        }}
        tooltipLabel={
          "Map the columns from your uploaded file to the corresponding columns in this dataset."
        }
        source={UploadDatasetModalSource.DATASET}
        projectType={ProjectType.POINTWISE}
      />

      <CreateDatasetModal
        isOpened={isCreateDatasetModalOpened}
        onClose={closeCreateDatasetModal}
      />

      {activeDatasetData && (
        <EditDatasetModal
          data={activeDatasetData}
          isOpened={isEditDatasetModalOpened}
          onClose={() => {
            closeEditDatasetModal();
            refetch();
          }}
        />
      )}

      {activeDatasetData && (
        <ShowDetailsModal
          dataset={activeDatasetData}
          isOpened={isShowDetailsModalOpened}
          onClose={closeShowDetailsModal}
        />
      )}

      <AddRunsToProjectModal
        isOpened={isAddToProjectOpened}
        onCreateNewProjectClick={openCreateProject}
        selectedProject={selectedProject}
        setSelectedProject={setSelectedProject}
        isDataset
        useDataTransferAll
        onClose={closeAddToProject}
        sourceProjectId={selectedDatasetId || ""}
      />

      <CreateNewProjectModal
        isOpened={isCreateNewProjectOpened}
        onClose={closeCreateNewProjectModal}
        setSelectedProject={setSelectedProject}
        onBackClick={onBackClick}
      />

      <DeleteModal
        isOpen={isDeleteModalOpened}
        onClose={closeDeleteModal}
        onConfirm={() => {
          if (activeDatasetData) {
            if (Array.isArray(activeDatasetData)) {
              const deletePromises = activeDatasetData.map((dataset) =>
                deleteDatasetQuery(dataset.id),
              );

              Promise.all(deletePromises).then(() => {
                table.resetRowSelection();
                refetch();
                closeDeleteModal();
              });
            } else {
              deleteDatasetMutation.mutate(activeDatasetData.id, {
                onSuccess: () => {
                  table.resetRowSelection();
                  closeDeleteModal();
                },
              });
            }
          } else {
            closeDeleteModal();
          }
        }}
        isLoading={deleteDatasetMutation.isPending}
        title={
          Array.isArray(activeDatasetData) && activeDatasetData.length > 1
            ? `Delete ${activeDatasetData.length} Datasets`
            : "Delete Dataset"
        }
        description={
          Array.isArray(activeDatasetData) && activeDatasetData.length > 1
            ? `Are you sure you want to delete these ${activeDatasetData.length} datasets? This cannot be undone.`
            : "Are you sure you want to delete this dataset? This cannot be undone."
        }
      />

      <Card className="p-[24px]">
        <Stack gap={30}>
          <MantineReactTable table={table} />
          {!datasets.length && (
            <Stack
              gap={30}
              className="flex items-center py-[20px] text-secondary text-body-14"
            >
              <Text className="text-secondary">
                You do not have any datasets.
              </Text>
              <Text className="text-secondary">
                Generate a dataset to get started or upload your own.
              </Text>
            </Stack>
          )}
        </Stack>
      </Card>
    </Page>
  );
}

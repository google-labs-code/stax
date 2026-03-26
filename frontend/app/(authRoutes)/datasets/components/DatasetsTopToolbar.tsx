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

import MaterialIcon from "@/components/MaterialIcon";
import { TOOLTIPS } from "@/config/constants";
import { getSuccessNotificationConfig } from "@/config/notifications";
import { getDatasetExportQuery } from "@/queries/clientQueries";
import { jsonToCsvExport } from "@/utils/jsonToCsvExport";
import {
  Button,
  Group,
  Menu,
  Text,
  Tooltip,
  UnstyledButton,
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { UseMutationResult } from "@tanstack/react-query";
import { MRT_TableInstance } from "mantine-react-table";
import { Dispatch, SetStateAction } from "react";

import { Dataset } from "../types";

interface DatasetsTopToolbarProps {
  table: MRT_TableInstance<Dataset>;
  openUploadModal: () => void;
  createDatasetMutation: UseMutationResult<any, Error, any, unknown>;
  openDeleteModal: () => void;
  setActiveDatasetData: Dispatch<SetStateAction<Dataset | undefined>>;
}

export const DatasetsTopToolbar = ({
  table,
  openUploadModal,
  createDatasetMutation,
  openDeleteModal,
  setActiveDatasetData,
}: DatasetsTopToolbarProps) => {
  const { getState } = table;
  const { rowSelection } = getState();
  const selectedCount = Object.keys(rowSelection).length;
  const selectedDatasets =
    selectedCount > 0
      ? table.getSelectedRowModel().flatRows.map((row) => row.original)
      : [];

  const handleBulkExport = () => {
    if (selectedDatasets.length > 0) {
      const exportPromises = selectedDatasets.map((dataset) =>
        getDatasetExportQuery(dataset.id).then((res) => {
          jsonToCsvExport(
            res as any,
            `dataset-export-${dataset.name || "data"}-${new Date().toISOString().slice(0, 10)}.csv`,
          );
        }),
      );

      Promise.all(exportPromises).then(() => {
        notifications.show(
          getSuccessNotificationConfig(
            `${selectedDatasets.length} dataset${
              selectedDatasets.length > 1 ? "s" : ""
            } exported successfully.`,
            "datasets-exported-success",
          ),
        );
      });
    }
  };

  const handleBulkDelete = () => {
    (setActiveDatasetData as any)(selectedDatasets);
    openDeleteModal();
  };

  return (
    <Group className="mb-[20px]" justify="space-between">
      <Group gap="md">
        <Text className="text-title-16">My Datasets</Text>

        <Menu
          shadow="md"
          position="bottom-start"
          withinPortal
          offset={4}
          classNames={{ dropdown: "rounded-lg shadow-sm !w-[120px]" }}
          disabled={!selectedCount}
        >
          <Menu.Target>
            {!selectedCount ? (
              <Tooltip
                label={TOOLTIPS.NO_ITEMS_SELECTED_MESSAGE}
                position="bottom"
                withArrow
              >
                <span className="inline-block cursor-not-allowed">
                  <UnstyledButton
                    className="gap-sm flex items-center justify-center py-[8px] px-[6px] rounded-md hover:bg-veryLightSilver opacity-50"
                    disabled={true}
                  >
                    <MaterialIcon
                      name="more_vert"
                      size={20}
                      className="text-secondary"
                    />
                  </UnstyledButton>
                </span>
              </Tooltip>
            ) : (
              <UnstyledButton className="gap-sm flex items-center justify-center py-[8px] px-[6px] rounded-md hover:bg-veryLightSilver">
                <MaterialIcon
                  name="more_vert"
                  size={20}
                  className="text-secondary"
                />
              </UnstyledButton>
            )}
          </Menu.Target>

          <Menu.Dropdown>
            <Menu.Item
              onClick={handleBulkExport}
              className="p-[12px]"
              leftSection={
                <MaterialIcon
                  name="download"
                  size={20}
                  className="!font-light text-secondary"
                />
              }
            >
              <Text className="text-body-14">Export</Text>
            </Menu.Item>

            <Menu.Item
              onClick={handleBulkDelete}
              className="p-[12px]"
              leftSection={
                <MaterialIcon
                  name="delete"
                  size={20}
                  className="!font-light text-secondary"
                />
              }
            >
              <Text className="text-body-14">Delete</Text>
            </Menu.Item>
          </Menu.Dropdown>
        </Menu>
      </Group>

      <Group className="pt-1 px-1">
        <Button
          size="lg"
          variant="outline"
          onClick={() => {
            createDatasetMutation.mutate({});
          }}
          className="border-neutrals-300 text-neutrals-900 large-btn hover:bg-veryLightSilver"
        >
          Create empty dataset
        </Button>
        <Button
          className="bg-brand large-btn hover:bg-brand-600"
          size="lg"
          variant="filled"
          onClick={openUploadModal}
        >
          Upload CSV
        </Button>
      </Group>
    </Group>
  );
};

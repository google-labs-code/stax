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

import { Dataset } from "@/app/(authRoutes)/datasets/types";
import CustomSelectOption from "@/components/CustomSelectOption";
import MaterialIcon from "@/components/MaterialIcon";
import { TOOLTIPS } from "@/config/constants";
import { getSuccessNotificationConfig } from "@/config/notifications";
import {
  dataTransferAllQuery,
  getDatasetsQuery,
} from "@/queries/clientQueries";
import {
  Button,
  ComboboxItem,
  Group,
  Modal,
  Select,
  Stack,
  Text,
  Tooltip,
  UnstyledButton,
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

import { AddDatasetModalProps } from "../types";

export default function AddDatasetModal({
  isOpened,
  onClose,
  onUploadCSV,
  onImport,
  importedDataset,
  isCsvUploadRef,
}: AddDatasetModalProps) {
  const [data, setData] = useState<Dataset[]>([]);
  const [selectedValue, setSelectedValue] = useState<ComboboxItem | null>(null);
  const params = useParams<{ id: string }>();

  const getDatasets = useMutation({
    mutationFn: getDatasetsQuery,
    onSuccess: (res) => {
      if (isCsvUploadRef) isCsvUploadRef.current = true;

      setData(res.user_data_sets);
      if (importedDataset) {
        setSelectedValue({
          value: importedDataset.id,
          label: importedDataset.name,
        });
      } else {
        setSelectedValue(null);
      }
    },
  });

  const dataTransfer = useMutation({
    mutationFn: dataTransferAllQuery,
    onSuccess: () => {
      notifications.show(
        getSuccessNotificationConfig(
          "Dataset imported successfully.",
          "dataset-imported-success",
        ),
      );
      onImport();
    },
  });

  useEffect(() => {
    if (isOpened) {
      getDatasets.mutate();
    }
  }, [isOpened, importedDataset]);

  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      closeButtonProps={{ "aria-label": "close button" }}
      size="700px"
      title={
        <Text className="flex items-center gap-2 !font-medium text-title-22">
          Import a dataset to your project
        </Text>
      }
      centered
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[32px]",
      }}
      data-testid="add-dataset-modal"
    >
      <Stack className="gap-md">
        <Text className="text-title-14">Select a data source</Text>
        <Group gap={22}>
          <Group w="80%">
            <Select
              w="90%"
              classNames={{
                input:
                  "h-[40px] border border-neutrals-300 rounded-sm text-body-14",
                option: "px-1",
              }}
              placeholder="Select from datasets"
              data={data.map((dataset) => {
                return {
                  value: dataset.id,
                  label: dataset.name,
                };
              })}
              renderOption={(option) => <CustomSelectOption option={option} />}
              value={selectedValue ? selectedValue.value : null}
              onChange={(_, selectedItem) => setSelectedValue(selectedItem)}
            />
            <Text className="text-secondary text-body-12">or</Text>
          </Group>

          <UnstyledButton
            className="flex items-center gap-1"
            onClick={onUploadCSV}
          >
            <MaterialIcon name="cloud_upload" size={16} />
            <Text className="text-title-12">Upload CSV</Text>
          </UnstyledButton>
        </Group>
        <Group className="mt-2" justify="flex-end">
          {!selectedValue ? (
            <Tooltip
              label={TOOLTIPS.NO_DATASET_SELECTED_MESSAGE}
              position="bottom"
              withArrow
            >
              <span>
                <Button
                  size="lg"
                  color="brand"
                  className="large-btn"
                  disabled={true}
                >
                  Import
                </Button>
              </span>
            </Tooltip>
          ) : (
            <Button
              size="lg"
              color="brand"
              className="large-btn"
              loading={dataTransfer.isPending}
              data-testid="active-import-btn"
              onClick={() => {
                if (selectedValue) {
                  dataTransfer.mutate({
                    source_id: selectedValue.value,
                    source_type: "DATASET",
                    target_id: params?.id,
                    target_type: "PROJECT",
                  });
                }
              }}
            >
              Import
            </Button>
          )}
        </Group>
      </Stack>
    </Modal>
  );
}

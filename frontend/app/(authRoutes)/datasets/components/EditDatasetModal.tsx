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

import ModalFooterButton from "@/components/ModalFooter";
import ModalInputLabel from "@/components/ModalInputLabel";
import { TOOLTIPS } from "@/config/constants";
import { updateDatasetQuery } from "@/queries/clientQueries";
import { DatasetResponse } from "@/queries/types";
import { Group, Modal, Stack, Text, TextInput, Textarea } from "@mantine/core";
import { useMutation } from "@tanstack/react-query";
import { useEffect, useState } from "react";

import { Dataset } from "../types";

type EditScoreModalProps = {
  isOpened: boolean;
  onClose: (res?: DatasetResponse) => void;
  data: Dataset;
};
export default function EditDatasetModal({
  isOpened,
  onClose,
  data,
}: EditScoreModalProps) {
  const [name, setName] = useState(data?.name || "");
  const [description, setDescription] = useState(data?.description || "");

  const updateDataset = useMutation({
    mutationFn: () =>
      updateDatasetQuery({
        id: data?.id,
        name,
        description,
      }),
    onSettled: () => {
      onClose();
    },
  });

  useEffect(() => {
    setName(data?.name || "");
    setDescription(data?.description || "");
  }, [isOpened]);

  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      closeButtonProps={{ "aria-label": "close button" }}
      size="700px"
      title={
        <Text className="flex items-center gap-2 !font-medium text-title-22">
          Edit dataset
        </Text>
      }
      centered
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[32px]",
      }}
    >
      <Stack className="base-modal-form-two-rows-inputs">
        <Group className="flex flex-col" gap={10}>
          <ModalInputLabel label="Name" isRequired />

          <TextInput
            className="w-[100%]"
            classNames={{
              input: `rounded-lg border-neutrals-300 !placeholder-resting`,
            }}
            value={name || ""}
            error={name ? !name.trim() : true}
            placeholder="Dataset name"
            onChange={(event) => {
              setName(event.currentTarget.value);
            }}
          />
        </Group>

        <Group className="flex flex-col" gap={10}>
          <ModalInputLabel label="Description" />

          <Textarea
            className="w-[100%]"
            classNames={{
              input: `h-[110px] rounded-lg border-neutrals-300 !placeholder-resting`,
            }}
            value={description || ""}
            placeholder="Optional description"
            onChange={(event) => {
              setDescription(event.currentTarget.value);
            }}
          />
        </Group>

        <ModalFooterButton
          onClick={() => {
            updateDataset.mutate();
          }}
          label="Update dataset"
          isLoading={updateDataset.isPending}
          isDisabled={!name || !name.trim()}
          tooltipLabel={TOOLTIPS.DATASET_NAME_REQUIRED_MESSAGE}
        />
      </Stack>
    </Modal>
  );
}

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

import dayjs from "@/utils/dayjsSetup";
import { Group, Modal, Stack, Text } from "@mantine/core";

import { Dataset } from "../types";

type ShowDetailsModalProps = {
  isOpened: boolean;
  onClose: () => void;
  dataset: Dataset;
};

export const ShowDetailsModal = ({
  isOpened,
  onClose,
  dataset,
}: ShowDetailsModalProps) => {
  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      title={<Text className="text-title-22">{dataset?.name} details</Text>}
      centered
      padding="24px"
      size="sm"
      classNames={{
        header: "p-[24px] pb-0 mb-[32px]",
        body: "flex flex-col flex-1",
        content: "flex flex-col",
      }}
    >
      <Stack gap={28}>
        {/* <Group className="items-baseline" gap={70}>
          <Text>Projects</Text>
          <Text className="text-brand max-w-[55px] cursor-pointer ml-[20px]">
            Untitled Test run
          </Text>
        </Group> */}
        {/* <Divider /> */}
        <Group gap={70}>
          <Text>Date added</Text>
          <Text className="text-secondary">
            {dayjs(dataset?.created_at).format("MM/DD/YYYY H:m")}
          </Text>
        </Group>
        {/* <Group gap={70}>
          <Text>Data added</Text>
          <Text className="text-secondary">3,256</Text>
        </Group> */}
      </Stack>
    </Modal>
  );
};

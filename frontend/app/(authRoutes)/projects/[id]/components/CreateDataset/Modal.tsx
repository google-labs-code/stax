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

import MaterialIcon from "@/components/MaterialIcon";
import ModalCard from "@/components/ModalCard";
import {
  Button,
  Group,
  Modal,
  ScrollArea,
  Select,
  Stack,
  Text,
  Textarea,
} from "@mantine/core";
import { useState } from "react";

import DatasetAccordion from "./Accordion";
import CreateDatasetHeader from "./Header";

type CreateDatasetModal = {
  isOpened: boolean;
  onClose: () => void;
};

const PROMPT_TYPES = ["User", "Assistant", "System"];

export default function CreateDatasetModal({
  isOpened,
  onClose,
}: CreateDatasetModal) {
  const [promptType, setPromptType] = useState(PROMPT_TYPES[0]);

  return (
    <Modal
      opened={isOpened}
      closeButtonProps={{ "aria-label": "close button" }}
      onClose={onClose}
      title={
        <Stack gap="xs">
          <Text className="!font-medium text-title-22">Create a dataset</Text>
          <Text className="text-secondary text-body-16">
            {`Generate test data for evaluation by defining prompt. Use optional
            {{variable}} syntax and define variable values to generate dataset.`}
          </Text>
        </Stack>
      }
      centered
      padding="24px"
      size="931px"
      classNames={{
        header: "p-[24px] pb-0 mb-[32px]",
      }}
    >
      <Stack>
        <ScrollArea
          component="div"
          classNames={{
            viewport: "block [&>div]:!block h-[650px] md:h-[600px] 2xl:h-full",
          }}
          offsetScrollbars
        >
          <Stack>
            <Stack gap={8}>
              <CreateDatasetHeader
                label="Define Prompt Template"
                tooltip="Enter your base prompt here. Use {{variable}} syntax for parts you want to replace with variable values. Use '+ Add Turn' for multi-turn formats."
              />
              <ModalCard>
                <Stack gap={8}>
                  <Group gap={8} wrap="wrap" className="items-start">
                    <Select
                      data={PROMPT_TYPES}
                      className="max-w-[120px]"
                      value={promptType}
                      onChange={setPromptType}
                      renderOption={(option) => (
                        <Group gap={4}>
                          <MaterialIcon
                            name="check"
                            className={`!text-[16px] ${!option.checked && "text-transparent"}`}
                          />
                          <Text className="text-title-12">
                            {option.option.label}
                          </Text>
                        </Group>
                      )}
                      classNames={{
                        input: "rounded-sm border-neutrals-300",
                      }}
                    />
                    <Textarea
                      classNames={{
                        root: "border-neutrals-300 flex-1",
                        input:
                          "min-h-[80px] placeholder:text-title-14 placeholder:!text-resting",
                      }}
                      placeholder={`Type prompt here, you can add a {{variable}} and define it below.\n# Example format for prompt:\nWhat landmark is at {{latitude}} {{longitude}}?`}
                      radius="md"
                      autosize
                    />
                  </Group>

                  <Button
                    w="164px"
                    variant="default"
                    leftSection={<MaterialIcon name="add" size={18} />}
                  >
                    Add Prompt Turn
                  </Button>
                </Stack>
              </ModalCard>
            </Stack>
            <DatasetAccordion />
          </Stack>
        </ScrollArea>

        <Group className="mt-[32px] flex justify-between">
          <Button
            variant="transparent"
            color="gray"
            size="lg"
            className="!pl-0 hover:text-brand"
          >
            Reset
          </Button>

          <div className="flex items-center gap-4">
            <Text className="text-title-14">
              Estimated cost to run:{" "}
              <span className="text-brand text-title-14">$1.32</span>
            </Text>
            <Button size="xl" variant="primery" className="rounded-sm">
              Generate dataset
            </Button>
          </div>
        </Group>
      </Stack>
    </Modal>
  );
}

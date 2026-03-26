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

import { Group, Popover, Text, UnstyledButton } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";

import MaterialIcon from "./MaterialIcon";

export default function ModelCardDropdown({
  onDeleteModel,
  onShowDetails,
  onCopyModel,
  onEdit,
}: {
  onDeleteModel?: () => void;
  onShowDetails: () => void;
  onCopyModel: () => void;
  onEdit?: () => void;
}) {
  const [opened, { open, close }] = useDisclosure(false);

  return (
    <>
      {opened && (
        <div
          className="fixed inset-0 z-[99] bg-transparent"
          onClick={(e) => {
            e.stopPropagation();
            close();
          }}
        />
      )}
      <Popover
        position="bottom-start"
        shadow="md"
        opened={opened}
        onClose={close}
        classNames={{
          dropdown: "rounded-md !w-[185px] p-0",
        }}
      >
        <Popover.Target>
          <UnstyledButton
            className="flex flex-row items-center justify-center"
            onClick={(e) => {
              e.stopPropagation();
              open();
            }}
          >
            <MaterialIcon
              name="more_vert"
              size={20}
              className="text-secondaryDark"
            />
          </UnstyledButton>
        </Popover.Target>
        <Popover.Dropdown>
          {onEdit && (
            <UnstyledButton
              className="flex w-full flex-row items-center justify-start gap-lg p-4"
              onClick={(e) => {
                e.stopPropagation();
                close();
                onEdit();
              }}
            >
              <MaterialIcon name="tune" size={20} className="text-secondary" />
              <Text className="text-secondaryDark">Edit</Text>
            </UnstyledButton>
          )}
          <UnstyledButton
            className="flex w-full flex-row items-center justify-start gap-lg p-4"
            onClick={(e) => {
              e.stopPropagation();
              close();
              onShowDetails();
            }}
          >
            <MaterialIcon
              name="view_list"
              size={20}
              className="text-secondary"
            />
            <Text className="text-secondaryDark">Show details</Text>
          </UnstyledButton>
          <UnstyledButton
            className="flex w-[100%] flex-row items-center justify-start gap-lg p-4"
            onClick={(e) => {
              e.stopPropagation();
              close();
              onCopyModel();
            }}
          >
            <MaterialIcon
              name="file_copy"
              size={20}
              className="text-secondary"
            />
            <Text className="text-secondaryDark">Make a copy</Text>
          </UnstyledButton>

          {onDeleteModel && (
            <Group className="p-3 pb-2">
              <UnstyledButton
                onClick={(e) => {
                  e.stopPropagation();
                  close();
                  onDeleteModel();
                }}
                className="flex w-[100%] flex-row items-center justify-center rounded-sm bg-supporting-red-500 px-4 py-3 text-center text-white text-title-12"
              >
                Delete model
              </UnstyledButton>
            </Group>
          )}
        </Popover.Dropdown>
      </Popover>
    </>
  );
}

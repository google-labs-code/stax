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
import { Popover, Text, UnstyledButton } from "@mantine/core";
import { useState } from "react";

export default function GenerateOutputsModalDropdownButton({
  onModelRemove,
}: {
  onModelRemove: () => void;
}) {
  const [isOpenDropdownDetails, setOpenDropdownDetails] = useState(false);

  return (
    <Popover
      position="bottom-start"
      shadow="md"
      opened={isOpenDropdownDetails}
      onChange={setOpenDropdownDetails}
    >
      <Popover.Target>
        <UnstyledButton
          onClick={() => setOpenDropdownDetails((o) => !o)}
          className="flex flex-row items-center"
        >
          <MaterialIcon name="more_vert" className="text-secondaryDark" />
        </UnstyledButton>
      </Popover.Target>
      <Popover.Dropdown className="rounded-sm">
        <UnstyledButton
          className="flex flex-row items-center gap-2"
          onClick={onModelRemove}
        >
          <MaterialIcon
            name="delete"
            className="text-secondaryDark"
            size={20}
          />
          <Text className="text-body-14">Remove model</Text>
        </UnstyledButton>
      </Popover.Dropdown>
    </Popover>
  );
}

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

import AccordionModelProviders from "@/components/AccordionModelProviders";
import MaterialIcon from "@/components/MaterialIcon";
import { useModelsContext } from "@/hooks/useModelsContext";
import { Model } from "@/queries/types";
import {
  Group,
  Popover,
  PopoverDropdown,
  PopoverTarget,
  Text,
} from "@mantine/core";
import { useState } from "react";

export default function GenerateOutputsModalSideBySideModelCard({
  model,
  label,
  onModelSelect,
}: {
  model: Model;
  label: string;
  onModelSelect: (model: Model) => void;
}) {
  const { allModels } = useModelsContext();
  const [isOpenDropdown, setOpenDropdown] = useState(false);

  return (
    <Group className="flex flex-col gap-[16px] p-[16px] bg-veryLightSilver rounded-md w-[50%] max-w-[50%] border-default">
      <Group className="flex flex-row gap-[12px] justify-start w-[100%]">
        <div className="bg-lightBlue w-[24px] h-[20px] rounded-[3.33px] text-center flex flex-row items-center justify-center !font-medium uppercase text-brand text-title-11">
          {label}
        </div>
        <Text className="text-title-14">Model</Text>
      </Group>
      <Popover
        opened={isOpenDropdown}
        onChange={setOpenDropdown}
        position="bottom-start"
        closeOnClickOutside
        withinPortal
      >
        <PopoverTarget>
          <Group
            className="flex w-full cursor-pointer flex-row flex-nowrap justify-between gap-0 px-[16px] py-[12px] bg-white rounded-sm border-default"
            onClick={() => setOpenDropdown((o) => !o)}
          >
            {model?.id ? (
              <Group className="gap-md w-[90%] flex-nowrap">
                <Group className="gap-0 w-[24px] h-[24px]">
                  {model?.icon && <model.icon size={24} />}
                </Group>
                <Text className="gap-0 truncate text-secondaryDark text-body-16">
                  {model.label}
                </Text>
              </Group>
            ) : (
              <Text className="w-[90%] gap-0 text-resting text-body-16">
                Select or search model
              </Text>
            )}
            <MaterialIcon
              name="keyboard_arrow_down"
              size={20}
              className="w-[10%] flex flex-row justify-end"
            />
          </Group>
        </PopoverTarget>

        <PopoverDropdown className="!w-[240px] max-h-[300px] overflow-y-auto rounded-lg !p-0">
          <AccordionModelProviders
            filteredOptions={allModels}
            handleSelection={(modelId) => {
              setOpenDropdown(false);
              onModelSelect(allModels.find((m) => m.id === modelId) as Model);
            }}
          />
        </PopoverDropdown>
      </Popover>
    </Group>
  );
}

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

import { Model } from "@/queries/types";
import { Group, Text, Tooltip } from "@mantine/core";

import MaterialIcon from "./MaterialIcon";
import ModelCardDropdown from "./ModelCardDropdown";
import TokenAutoIcon from "./icons/TokenAutoIcon";

export default function CustomModelCard({
  model,
  onDeleteModel,
  onShowDetails,
  onCopyModel,
  onEdit,
}: {
  model: Model;
  onDeleteModel: () => void;
  onShowDetails: () => void;
  onCopyModel: () => void;
  onEdit: () => void;
}) {
  return (
    <Group
      data-testid="model-card"
      onClick={(e) => {
        e.stopPropagation();
        onShowDetails();
      }}
      className={`flex min-w-[161px] max-w-[300px] cursor-pointer flex-col items-start justify-start gap-md rounded-2xl border-[1px] border-solid border-neutrals-300 bg-veryLightSilver px-[8px] py-[12px]`}
    >
      <Group className="flex w-full flex-row items-center justify-between">
        <Group className="flex items-center gap-md">
          {model.icon && <model.icon size={18} />}
          <Tooltip label={model.label} position="top">
            <Text className="max-w-[190px] truncate text-secondaryDark text-title-14">
              {model.label}
            </Text>
          </Tooltip>
        </Group>
        <ModelCardDropdown
          onCopyModel={onCopyModel}
          onDeleteModel={onDeleteModel}
          onShowDetails={onShowDetails}
          onEdit={onEdit}
        />
      </Group>

      <Group className="align-center flex gap-2">
        <Text className="text-secondary text-title-12">Base model:</Text>
        <Text className="text-secondary text-title-12">{model.name}</Text>
      </Group>

      <Group className="align-center flex flex-row gap-lg">
        <Group gap="4px">
          <MaterialIcon
            name="thermostat"
            className="text-secondary"
            size={20}
          />
          <Text className="text-secondary text-title-12">
            {model?.properties?.temperature}
          </Text>
        </Group>

        <Group gap="4px">
          <TokenAutoIcon size={20} />
          <Text className="text-secondary text-title-12">
            {model?.properties?.max_tokens}
          </Text>
        </Group>
      </Group>
    </Group>
  );
}

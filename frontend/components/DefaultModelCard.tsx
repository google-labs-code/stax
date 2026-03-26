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
import { Group, Text } from "@mantine/core";

import ModelCardDropdown from "./ModelCardDropdown";

export default function DefaultModelCard({
  model,
  onShowDetails,
  onCopyModel,
}: {
  model: Model;
  onShowDetails: () => void;
  onCopyModel: () => void;
}) {
  return (
    <Group
      data-testid="model-card"
      className={`bg-veryLightSilver flex h-[40px] min-w-[160px] cursor-pointer flex-row items-center justify-between rounded-2xl border-[1px] border-solid border-neutrals-300 px-[12px] py-[8px]`}
      onClick={(e) => {
        e.stopPropagation();
        onShowDetails();
      }}
    >
      <Group className="gap-md flex flex-row items-center justify-between">
        {model.icon && <model.icon size={16} />}
        <Text className="!text-secondaryDark text-title-14">{model.label}</Text>
      </Group>
      <ModelCardDropdown
        onShowDetails={onShowDetails}
        onCopyModel={onCopyModel}
      />
    </Group>
  );
}

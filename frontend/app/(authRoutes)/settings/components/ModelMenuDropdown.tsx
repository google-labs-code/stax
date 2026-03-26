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

import { getModelDetails } from "@/config/constants";
import { useModelsContext } from "@/hooks/useModelsContext";
import { Group, Menu, Switch, Text } from "@mantine/core";

interface ModelMenuDropdownProps {
  readonly visibleProviders?: string[];
  readonly onClick: (provider: string) => void;
  readonly width?: number;
}

export default function ModelMenuDropdown({
  visibleProviders,
  onClick,
  width,
}: ModelMenuDropdownProps) {
  const { providers } = useModelsContext();

  return (
    <Menu.Dropdown w={width ?? 260} data-testid="model-menu-dropdown">
      {Object.entries(providers).map(([providerKey, providerName], index) => {
        const providerDetails = getModelDetails(providerKey);
        const ModelIcon = providerDetails?.icon;
        if (providerDetails) {
          return (
            <Group
              key={index}
              className="flex cursor-pointer flex-row items-center justify-between px-4 py-3"
              onClick={(e) => {
                e.preventDefault();
                onClick(providerKey);
              }}
            >
              <Group gap={12}>
                {ModelIcon && <ModelIcon size={20} />}
                <Text className="text-secondaryDark text-title-14">
                  {providerName}
                </Text>
              </Group>
              {visibleProviders && (
                <Switch
                  checked={visibleProviders.includes(providerKey)}
                  size="xs"
                  onClick={(e) => {
                    e.stopPropagation();
                  }}
                  onChange={(e) => {
                    e.stopPropagation();
                  }}
                />
              )}
            </Group>
          );
        }
      })}
    </Menu.Dropdown>
  );
}

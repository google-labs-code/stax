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

import { Box, Group, Text, UnstyledButton } from "@mantine/core";
import { ReactNode } from "react";

import GlobalActionMenu from "./GlobalActionMenu";
import MaterialIcon from "./MaterialIcon";

type PageHeaderProps = {
  title: string;
  icon?: string;
  iconButton?: {
    icon: string;
    onClick: () => void;
  };
  onBack?: () => void;
  breadcrumbs?: ReactNode;
  rightSection?: ReactNode;
};

export default function PageHeader({
  title,
  icon,
  iconButton,
  onBack,
  breadcrumbs,
  rightSection,
}: PageHeaderProps) {
  return (
    <Group
      justify="space-between"
      className="w-full"
      gap={0}
      data-testid="page-header"
    >
      <Group gap={4}>
        {breadcrumbs}
        {onBack && <MaterialIcon name="arrow_back" onClick={onBack} />}
        <Text className="text-title-21" data-testid="page-title">
          {title}
        </Text>
        {icon && (
          <Box className="flex items-center">
            <MaterialIcon name={icon} size={24} />
          </Box>
        )}

        {iconButton && (
          <UnstyledButton
            onClick={iconButton.onClick}
            className="flex items-center"
            data-testid="icon-button"
          >
            <MaterialIcon
              name={iconButton.icon}
              size={20}
              className="text-secondaryDark"
            />
          </UnstyledButton>
        )}
      </Group>

      <Group gap={0}>
        {rightSection}
        <GlobalActionMenu />
      </Group>
    </Group>
  );
}

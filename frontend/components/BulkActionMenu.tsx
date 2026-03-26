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
import { TOOLTIPS } from "@/config/constants";
import { BulkActionMenuItem } from "@/types";
import { Menu, Text, Tooltip } from "@mantine/core";
import { useState } from "react";

type BulkActionMenuProps = {
  isDisabled?: boolean;
  menuItems: BulkActionMenuItem[];
  label?: string;
  customClassName?: string;
};
export default function BulkActionMenu({
  isDisabled = true,
  menuItems,
  label,
  customClassName,
}: BulkActionMenuProps) {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  if (isDisabled) {
    return (
      <Tooltip
        label={TOOLTIPS.NO_ITEMS_SELECTED_MESSAGE}
        position="bottom"
        withArrow
      >
        <div
          className={`gap-sm flex cursor-not-allowed items-center justify-center opacity-50 ${customClassName}`}
        >
          <MaterialIcon name="more_vert" size={20} />
          {label && <Text className="!font-medium text-body-12">{label}</Text>}
        </div>
      </Tooltip>
    );
  }

  return (
    <Menu
      shadow="md"
      withinPortal
      position="bottom-end"
      onOpen={() => setIsMenuOpen(true)}
      onClose={() => setIsMenuOpen(false)}
      data-testid="bulk-action-menu"
    >
      <Menu.Target>
        <div
          className={`gap-sm flex cursor-pointer items-center justify-center transition-all rounded-sm ${customClassName} ${
            isMenuOpen ? "bg-veryLightSilver" : "hover:bg-veryLightSilver"
          }`}
        >
          <MaterialIcon name="more_vert" size={20} />
          {label && <Text className="!font-medium text-body-12">{label}</Text>}
        </div>
      </Menu.Target>
      <Menu.Dropdown className="rounded-sm !p-0">
        {menuItems.map((menuItem, key: number) => (
          <Menu.Item
            key={key}
            onClick={menuItem.onClick}
            data-testid="menu-item"
            className="!p-0"
            classNames={{
              itemLabel: "flex items-center p-[12px] gap-lg",
            }}
          >
            <MaterialIcon
              name={menuItem.icon}
              size={20}
              className="!font-normal text-secondary"
            />
            <Text className="text-secondaryDark !font-normal text-title-14">
              {menuItem.label}
            </Text>
          </Menu.Item>
        ))}
      </Menu.Dropdown>
    </Menu>
  );
}

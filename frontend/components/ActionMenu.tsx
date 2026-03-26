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
import { ActionIcon, Menu } from "@mantine/core";
import { useState } from "react";

type MenuItemProps = {
  label: string;
  leftSection: React.ReactNode;
  onClick?: () => void;
  component?: any;
  href?: string;
  disabled?: boolean;
};

type ActionMenuProps = {
  menuItems: MenuItemProps[];
};

export default function ActionMenu({ menuItems }: ActionMenuProps) {
  const [, setIsMenuOpen] = useState<boolean>(false);

  return (
    <Menu
      shadow="md"
      onOpen={() => setIsMenuOpen(true)}
      onClose={() => setIsMenuOpen(false)}
      width={200}
      radius={8}
      position="bottom-end"
      offset={4}
    >
      <Menu.Target>
        <ActionIcon variant="subtle" aria-label="ActionIcon">
          <MaterialIcon
            name="more_vert"
            className="cursor-pointer !font-light"
          />
        </ActionIcon>
      </Menu.Target>

      <Menu.Dropdown className="top-2">
        {menuItems.map((item, key) => (
          <Menu.Item
            key={key}
            leftSection={item.leftSection}
            className="gap-1 p-[12px] !font-light"
            onClick={item.onClick}
            component={item.component}
            href={item.href}
            disabled={item.disabled}
          >
            {item.label}
          </Menu.Item>
        ))}
      </Menu.Dropdown>
    </Menu>
  );
}

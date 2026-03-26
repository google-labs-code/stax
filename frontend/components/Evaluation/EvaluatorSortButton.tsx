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
import SortZToAIcon from "@/components/icons/SortZToAIcon";
import { TOOLTIPS } from "@/config/constants";
import { EvaluatorCardItem, EvaluatorTab, EvaluatorType } from "@/types";
import { Group, Menu, MenuTarget, Text, Tooltip } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { Dispatch, SetStateAction } from "react";

type EvaluatorSortButtonType = {
  data: EvaluatorCardItem[];
  setData: Dispatch<SetStateAction<EvaluatorCardItem[]>>;
  activeTab?: EvaluatorTab;
};

export default function EvaluatorSortButton({
  data,
  setData,
  activeTab = EvaluatorTab.DEFAULT,
}: EvaluatorSortButtonType) {
  const [, { open: openMenu, close: closeMenu }] = useDisclosure(false);

  const filteredData = activeTab
    ? data.filter((item) =>
        activeTab === EvaluatorTab.DEFAULT
          ? item.type === EvaluatorType.SYSTEM
          : item.type === EvaluatorType.USER,
      )
    : data;

  const hasData = filteredData.length > 0;

  const actionItems = [
    {
      label: "Sort A to Z",
      icon: (
        <MaterialIcon
          name="sort_by_alpha"
          size={20}
          className="!font-light text-secondary"
        />
      ),
      onClick: () => {
        const sortedData = [...data].sort((a, b) =>
          a.name.localeCompare(b.name),
        );
        setData(sortedData);
      },
    },
    {
      label: "Sort Z to A",
      icon: <SortZToAIcon />,
      onClick: () => {
        const sortedData = [...data].sort((a, b) =>
          b.name.localeCompare(a.name),
        );
        setData(sortedData);
      },
    },
  ];

  const sortButtonContent = (
    <Group
      className={`gap-md px-2 py-1 rounded transition-colors ${
        hasData
          ? "cursor-pointer hover:bg-lightSilver"
          : "cursor-not-allowed opacity-50"
      }`}
    >
      <MaterialIcon name="sort" className="!text-[18px] text-secondary" />
      <Text className="text-title-12">Sort</Text>
    </Group>
  );

  return (
    <Menu
      offset={8}
      onOpen={hasData ? openMenu : undefined}
      onClose={closeMenu}
      shadow="md"
      position="bottom-end"
      disabled={!hasData}
      classNames={{
        dropdown: "rounded-lg min-w-[185px]",
        item: "gap-md",
      }}
    >
      <MenuTarget>
        {hasData ? (
          sortButtonContent
        ) : (
          <Tooltip
            label={TOOLTIPS.NO_DATA_TO_SORT}
            position="bottom"
            withArrow
            withinPortal
          >
            {sortButtonContent}
          </Tooltip>
        )}
      </MenuTarget>
      <Menu.Dropdown>
        {actionItems.map((action, key) => (
          <Menu.Item
            key={key}
            className="p-[12px] hover:bg-veryLightSilver"
            onClick={action.onClick}
            leftSection={action.icon}
          >
            <Text className="text-body-14">{action.label}</Text>
          </Menu.Item>
        ))}
      </Menu.Dropdown>
    </Menu>
  );
}

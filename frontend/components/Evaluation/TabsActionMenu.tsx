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

import { EvaluatorPageAction } from "@/app/(authRoutes)/evaluatorGallery/types";
import MaterialIcon from "@/components/MaterialIcon";
import { routes } from "@/config/routes";
import { EvaluatorTab } from "@/types";
import { Menu, MenuTarget, Text } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { useRouter } from "next/navigation";

type TabsActionMenuType = {
  tab: string | null;
  dataId: string;
  openDeleteModal?: () => void;
};

export default function TabsActionMenu({
  tab,
  dataId,
  openDeleteModal,
}: TabsActionMenuType) {
  const router = useRouter();
  const [isMenuOpened, { open: openMenu, close: closeMenu }] =
    useDisclosure(false);

  const duplicateEvaluator = () => {
    router.push(
      routes.evaluatorGallery.root +
        "/" +
        dataId +
        "/" +
        EvaluatorPageAction.DUPLICATE,
    );
  };

  const actionItems =
    tab === EvaluatorTab.DEFAULT
      ? [
          {
            label: "Duplicate",
            icon: "copy_all",
            onClick: (evt: Event) => {
              evt.stopPropagation();
              duplicateEvaluator();
            },
          },
        ]
      : [
          {
            label: "Edit",
            icon: "edit",
            onClick: (evt: Event) => {
              evt.stopPropagation();
              router.push(
                routes.evaluatorGallery.root +
                  "/" +
                  dataId +
                  "/" +
                  EvaluatorPageAction.EDIT,
              );
            },
          },
          {
            label: "Duplicate",
            icon: "copy_all",
            onClick: (evt: Event) => {
              evt.stopPropagation();
              duplicateEvaluator();
            },
          },
          {
            label: "Delete",
            icon: "delete",
            onClick: (evt: Event) => {
              evt.stopPropagation();
              openDeleteModal && openDeleteModal();
            },
          },
        ];

  return (
    <Menu
      offset={3}
      onOpen={openMenu}
      onClose={closeMenu}
      shadow="sm"
      position="bottom-end"
      classNames={{
        dropdown: "rounded-lg min-w-[185px]",
        item: "gap-lg",
      }}
    >
      <MenuTarget>
        <div
          className={`flex h-5 w-5 justify-center ${isMenuOpened ? "rounded-100 bg-lightBlue" : null}`}
          onClick={(event) => {
            event.stopPropagation();
          }}
        >
          <MaterialIcon
            name="more_vert"
            className="cursor-pointer"
            size={18}
            tooltipLabel="More options"
            tooltipClassName={` ${isMenuOpened ? "hidden" : ""}`}
          />
        </div>
      </MenuTarget>
      <Menu.Dropdown>
        {actionItems.map((action, key) => (
          <Menu.Item
            key={key}
            className="p-[12px]"
            onClick={action.onClick as () => void}
            leftSection={
              <MaterialIcon
                name={action.icon}
                size={16}
                className="!font-light text-secondary"
              />
            }
          >
            <Text>{action.label}</Text>
          </Menu.Item>
        ))}
      </Menu.Dropdown>
    </Menu>
  );
}

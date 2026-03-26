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

import StaxLogo from "@/assets/StaxLogo.svg";
import MaterialIcon from "@/components/MaterialIcon";
import SidenavLink from "@/components/SideNav/SidenavLink";
import {
  AI_POLICY_TEXT,
  DOCUMENTATION_LINK,
} from "@/components/SideNav/consts";
import { routes } from "@/config/routes";
import { Group, Stack, Text, Tooltip, UnstyledButton } from "@mantine/core";
import Image from "next/image";
import Link from "next/link";
import { useReducer } from "react";

export default function SideNavigation() {
  const [isOpen, toggleNav] = useReducer((isCurrOpen) => !isCurrOpen, false);

  // Example autorater
  const recentAutoraters = [
    { name: "Career Compass AI", icon: "psychology" },
    { name: "Illuminate The Truth", icon: "lightbulb" },
    { name: "Perfect Syntax Proto", icon: "description" },
    { name: "Edit Every Essay", icon: "edit_document" },
    { name: "Code Bug Catcher", icon: "bug_report" },
  ];

  return (
    <Stack
      className={`justify-between self-stretch p-4 ${
        isOpen ? "!max-w-[240px] border-r-default" : "!max-w-[68px] !w-[68px]"
      } h-full bg-white gap-0`}
    >
      <Group className="h-[5%] justify-between items-center pb-2">
        {isOpen ? (
          <>
            <Link
              href={routes.projects}
              className="flex flex-row items-center ml-2 h-[40px]"
            >
              <Image alt="navbarLogo" src={StaxLogo} priority />
              <Text className="text-logo ml-4">Stax</Text>
            </Link>
            <Tooltip
              label="Close sidebar"
              position="right"
              offset={18}
              openDelay={300}
            >
              <Group
                onClick={toggleNav}
                className="cursor-pointer p-2 rounded-md hover:bg-veryLightSilver group"
              >
                <MaterialIcon
                  className="text-secondary group-hover:text-secondaryDark"
                  name="left_panel_close"
                  size={24}
                />
              </Group>
            </Tooltip>
          </>
        ) : (
          <Tooltip
            label="Open sidebar"
            position="right"
            offset={18}
            openDelay={300}
          >
            <Group
              onClick={toggleNav}
              className="cursor-pointer ml-2 p-2 rounded-md hover:bg-veryLightSilver group"
            >
              <MaterialIcon
                className="text-secondary group-hover:text-secondaryDark"
                name="left_panel_open"
                size={24}
              />
            </Group>
          </Tooltip>
        )}
      </Group>
      <Group
        unstyled
        className="h-[95%] mt-0 mb-2 justify-between flex flex-col"
      >
        <Stack className="w-full gap-0">
          {isOpen ? (
            <>
              <UnstyledButton className="flex w-full items-center gap-4 px-2 py-2 rounded-md hover:bg-veryLightSilver group mb-2">
                <div className="flex items-center justify-center w-6 h-6 bg-brand rounded-full">
                  <MaterialIcon name="add" className="text-white" size={16} />
                </div>
                <Text className="text-title-14 text-secondary group-hover:text-secondaryDark">
                  New Autorater
                </Text>
              </UnstyledButton>
              <Text className="text-title-12 text-resting pl-2 mt-1 mb-2">
                Recent Autoraters
              </Text>

              <div className="flex flex-col w-full">
                {recentAutoraters.map((autorater, index) => (
                  <UnstyledButton
                    key={index}
                    className="flex w-full items-center justify-between pr-2 py-2.5 rounded-md hover:bg-veryLightSilver group"
                  >
                    <div className="flex items-center flex-grow gap-2">
                      <div className="w-9 flex justify-center">
                        <MaterialIcon
                          name={autorater.icon}
                          size={18}
                          className="text-secondary group-hover:text-secondaryDark"
                        />
                      </div>
                      <Text className="text-title-14 !text-secondary group-hover:!text-secondaryDark">
                        {autorater.name}
                      </Text>
                    </div>
                    <MaterialIcon
                      name="more_vert"
                      size={18}
                      className="text-secondary opacity-0 group-hover:opacity-100 group-hover:text-secondaryDark transition-opacity"
                    />
                  </UnstyledButton>
                ))}
              </div>
            </>
          ) : (
            <div className="mt-10"></div>
          )}
        </Stack>

        <Group gap={8} className="flex-col">
          <Group className="mb-0 gap-0">
            {isOpen ? (
              <Text className="text-resting text-body-12 ml-3">
                {AI_POLICY_TEXT}
              </Text>
            ) : (
              <MaterialIcon
                name="info"
                className="flex cursor-default text-secondary hover:bg-veryLightSilver rounded-md p-2 hover:text-secondaryDark"
                tooltipClassName="ml-6 max-w-[240px]"
                size={24}
                tooltipOffset={20}
                tooltipPosition="top-end"
                tooltipLabel={AI_POLICY_TEXT}
              />
            )}
          </Group>
          <Stack gap={0} className="w-full">
            <SidenavLink
              listItem={{
                link: DOCUMENTATION_LINK,
                key: "documentation",
                label: "Documentation",
                icon: "open_in_new",
                target: "_blank",
              }}
              className="[&_.text-inverted]:text-secondary hover:!bg-veryLightSilver group-hover:[&_.text-inverted]:text-secondaryDark"
              iconClassName="text-secondary group-hover:text-secondaryDark"
              isSidenavOpen={isOpen}
              hoverTextColor="secondaryDark"
            />
            <SidenavLink
              listItem={{
                link: `${routes.settings}?tab=apiKeys`,
                key: "settings",
                label: "Settings",
                icon: "settings",
              }}
              className="[&_.text-inverted]:text-secondary hover:!bg-veryLightSilver group-hover:[&_.text-inverted]:text-secondaryDark"
              iconClassName="text-secondary group-hover:text-secondaryDark"
              isSidenavOpen={isOpen}
              hoverTextColor="secondaryDark"
            />
          </Stack>
        </Group>
      </Group>
    </Stack>
  );
}

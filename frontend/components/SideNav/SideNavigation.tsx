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
import { ALL_PAGE_LINKS, routes } from "@/config/routes";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import { Box, Group, Stack, Text, Tooltip } from "@mantine/core";
import Image from "next/image";
import Link from "next/link";
import { useParams } from "next/navigation";
import { Fragment, useEffect, useReducer, useState } from "react";

import MaterialIcon from "../MaterialIcon";
import SidenavLink from "./SidenavLink";
import UserButton from "./UserButton";
import { AI_POLICY_TEXT, DOCUMENTATION_LINK, PROTECTED_ROUTES } from "./consts";
import { IExpandedMenuItems, NavigationLinkData } from "./types";

export default function SideNavigation() {
  const [isOpen, toggleNav] = useReducer((isCurrOpen) => !isCurrOpen, true);
  const [allLinks, setAllLinks] = useState<NavigationLinkData[]>([]);
  const { allProjects, isPendingProjects } = useProjectsContext();
  const [expandedMenuItems, setExpandedMenuItems] =
    useState<IExpandedMenuItems>({});

  const params = useParams<{ id: string }>();
  const projectId = params.id;

  useEffect(() => {
    const allExpandedMenuItems: IExpandedMenuItems = {};
    const availableLinks: NavigationLinkData[] = [];
    ALL_PAGE_LINKS.forEach((linkDetails) => {
      if (linkDetails.key === "projects") {
        if (!isPendingProjects && allProjects.length > 0) {
          const currentProject = allProjects.find(
            (p) => p.project_id === projectId,
          );
          linkDetails.childLinks = currentProject
            ? [
                {
                  link: routes.projects + "/" + currentProject.project_id,
                  key: currentProject.project_id,
                  label: currentProject?.name,
                },
              ]
            : [];
        }
      }

      allExpandedMenuItems[linkDetails.key] = linkDetails.isExpanded
        ? true
        : false;

      if (!PROTECTED_ROUTES.includes(linkDetails.key)) {
        availableLinks.push(linkDetails);
      }
    });
    setAllLinks(availableLinks);
    setExpandedMenuItems(allExpandedMenuItems);
  }, [allProjects, params]);

  return (
    <Stack className="justify-between self-stretch px-4 !max-w-[280px] h-full">
      <Group className="h-[5%]">
        <Link
          href={routes.projects}
          className="flex flex-row items-center ml-2 h-[40px] mt-3"
        >
          <Image alt="navbarLogo" src={StaxLogo} priority />
          {isOpen && <Text className="text-white text-logo ml-4">Stax</Text>}
        </Link>
        {isOpen && (
          <Box className="mt-3 border-default rounded-xl text-neutrals-50 border-black text-body-12 px-[12px] py-[7px]">
            EXPERIMENT
          </Box>
        )}
      </Group>
      <Group
        unstyled
        className="h-[95%] mt-5 mb-2 justify-between flex flex-col"
      >
        <Group gap={8} className="flex-col">
          {allLinks.map((listItem) => (
            <Fragment key={listItem.key}>
              <SidenavLink
                listItem={listItem}
                isSidenavOpen={isOpen}
                onLinkClick={() => {
                  if (listItem.link !== "#") {
                    return;
                  }

                  const menuItems = { ...expandedMenuItems };
                  menuItems[listItem.key] = !menuItems[listItem.key];
                  setExpandedMenuItems(menuItems);
                }}
              />

              {isOpen &&
                expandedMenuItems?.[listItem.key] === true &&
                listItem.childLinks &&
                listItem.childLinks.map((subLink) => (
                  <SidenavLink
                    key={subLink.key}
                    className="pl-11"
                    listItem={subLink}
                    isSidenavOpen
                  />
                ))}
            </Fragment>
          ))}
        </Group>
        <Group gap={8} className="flex-col">
          <Group className="mb-2">
            {isOpen ? (
              <Text className="text-disabled text-body-sans-12 ml-3">
                {AI_POLICY_TEXT}
              </Text>
            ) : (
              <MaterialIcon
                name="info"
                className="flex cursor-default text-neutrals-50 hover:bg-inverted/10 rounded-md p-2"
                tooltipClassName="ml-6 max-w-[240px]"
                size={24}
                tooltipOffset={20}
                tooltipPosition="top-end"
                tooltipLabel={AI_POLICY_TEXT}
              />
            )}
          </Group>
          <SidenavLink
            listItem={{
              link: DOCUMENTATION_LINK,
              key: "documentation",
              label: "Documentation",
              icon: "open_in_new",
              target: "_blank",
            }}
            isSidenavOpen={isOpen}
          />
          <SidenavLink
            listItem={{
              link: `${routes.settings}?tab=apiKeys`,
              key: "settings",
              label: "Settings",
              icon: "settings",
            }}
            isSidenavOpen={isOpen}
          />
          <UserButton isSidenavOpen={isOpen} />
          <Group
            unstyled
            className="p-2 self-start rounded-md hover:bg-inverted/10"
          >
            <Tooltip
              label={isOpen ? "Close sidebar" : "Open sidebar"}
              position="right"
              offset={18}
              openDelay={300}
            >
              <Group>
                <MaterialIcon
                  onClick={toggleNav}
                  className="text-white"
                  name={isOpen ? "chevron_left" : "chevron_right"}
                  size={24}
                />
              </Group>
            </Tooltip>
          </Group>
        </Group>
      </Group>
    </Stack>
  );
}

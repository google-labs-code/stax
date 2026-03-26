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

import { Badge, Group, Loader, Text, Tooltip, Transition } from "@mantine/core";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useCallback, useMemo } from "react";

import MaterialIcon from "../MaterialIcon";
import { SidenavLinkProps } from "./types";

export default function SidenavLink(props: SidenavLinkProps) {
  const pathname = usePathname();
  const {
    listItem,
    isSidenavOpen,
    isDisabled,
    isLoading,
    onLinkClick,
    iconClassName,
    hoverTextColor,
  } = props;

  const isSelected = useMemo(() => {
    const page = pathname.split("/")[1];
    const subpage = pathname.split("/")[2];

    return listItem.key === (subpage && page === "projects" ? subpage : page);
  }, [pathname, listItem]);

  const generateIcon = useCallback(() => {
    if (listItem.icon) {
      if (typeof listItem.icon === "string") {
        return (
          <MaterialIcon
            name={listItem.icon}
            className={iconClassName || "text-white"}
          />
        );
      }

      return (
        <Group className="">
          <listItem.icon />
        </Group>
      );
    }
  }, [listItem, iconClassName]);

  const textClassName =
    hoverTextColor === "secondaryDark"
      ? "text-secondary group-hover:text-secondaryDark text-title-14"
      : "text-inverted text-title-14";

  return (
    <Tooltip
      label={listItem.label}
      position="right"
      disabled={isSidenavOpen}
      offset={15}
      openDelay={200}
    >
      <Link
        aria-disabled={isDisabled}
        href={isDisabled ? "#" : listItem.link}
        target={listItem.target}
        className={`gap-lg flex w-full items-center justify-center rounded-md self-start px-2 py-3 hover:!bg-inverted/10 group ${
          isSelected ? "bg-inverted/10" : ""
        } ${isDisabled ? "cursor-not-allowed" : "cursor-pointer"} ${
          props.className
        } `}
        data-testid={`${listItem.key}-link`}
        onClick={onLinkClick}
      >
        {generateIcon()}
        <Transition
          mounted={isSidenavOpen}
          transition="slide-right"
          duration={0}
          timingFunction="ease"
        >
          {(styles) => (
            <Group className="relative w-full justify-between items-center">
              <Text className={textClassName} style={styles}>
                {listItem.label}
              </Text>
              {isDisabled ? (
                <Badge className="absolute right-0 h-[unset] rounded-[7px] bg-neutrals-950 !p-[5px] text-[0.625rem] font-bold leading-3 tracking-[1px]">
                  Soon
                </Badge>
              ) : null}
            </Group>
          )}
        </Transition>

        {isLoading && <Loader size="sm" />}
      </Link>
    </Tooltip>
  );
}

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

import { useGlobalContext } from "@/hooks/useGlobalContext";
import LocalStorage from "@/utils/LocalStorage";
import { UserDetails } from "@/utils/userDetailsStorage";
import {
  Group,
  Menu,
  MenuDropdown,
  MenuTarget,
  Text,
  Transition,
  UnstyledButton,
} from "@mantine/core";
import { useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";

import MaterialIcon from "../MaterialIcon";
import { UserButtonProps } from "./types";

export default function UserButton({ isSidenavOpen }: UserButtonProps) {
  const queryClient = useQueryClient();
  const { userDetails } = useGlobalContext();
  const router = useRouter();

  const signOut = async () => {
    queryClient.clear();
    UserDetails.remove();
    LocalStorage.clear();
    router.push("/login");
  };

  if (!userDetails) {
    return null;
  }

  return (
    <Menu position="bottom-end">
      <MenuTarget>
        <li
          className={`flex h-[44px] w-full cursor-pointer items-center hover:!bg-inverted/10 px-1 py-1 rounded-md`}
          onClick={(e) => {
            e.preventDefault();
          }}
        >
          <Group className="h-[33px] flex justify-center bg-darkBlue rounded-xl self-start">
            <MaterialIcon
              name="person"
              size={24}
              className="flex h-[32px] w-[32px] flex-row items-center justify-center text-white"
            />
          </Group>
          <Transition
            mounted={isSidenavOpen}
            transition="slide-right"
            duration={0}
            timingFunction="ease"
          >
            {(styles) => (
              <Group className="relative w-full justify-between">
                <Text
                  className="text-inverted text-title-14 truncate max-w-[200px] ml-2"
                  style={styles}
                >
                  {userDetails?.email}
                </Text>
              </Group>
            )}
          </Transition>
        </li>
      </MenuTarget>
      <MenuDropdown className="flex w-full flex-col gap-xl rounded-sm px-[16px] pb-[16px]">
        <div className="flex flex-col gap-xs">
          <Text className="text-primary text-title-16">
            {userDetails?.firstName} {userDetails?.lastName}
          </Text>
        </div>
        <Menu.Divider />
        <UnstyledButton
          onClick={signOut}
          data-testid="sign-out-button"
          className="text-accent-red text-title-14"
        >
          Logout
        </UnstyledButton>
      </MenuDropdown>
    </Menu>
  );
}

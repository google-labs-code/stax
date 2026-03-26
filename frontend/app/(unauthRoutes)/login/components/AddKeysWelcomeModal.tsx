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

import { InitialKeys } from "@/app/(authRoutes)/settings/config/initialKeys";
import { addUserKeyQuery } from "@/app/(authRoutes)/settings/state/queries";
import { KeyDetails } from "@/app/(authRoutes)/settings/types";
import MaterialIcon from "@/components/MaterialIcon";
import { ModelIcon } from "@/components/ModelIcon";
import { TOOLTIPS } from "@/config/constants";
import { getSuccessNotificationConfig } from "@/config/notifications";
import {
  Button,
  Group,
  Modal,
  Popover,
  PopoverDropdown,
  PopoverTarget,
  Text,
  TextInput,
  Tooltip,
  UnstyledButton,
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import Link from "next/link";
import { useState } from "react";

type AddKeysWelcomeModalProps = {
  isOpened: boolean;
  onProceed: () => void;
};

export default function AddKeysWelcomeModal({
  isOpened,
  onProceed,
}: AddKeysWelcomeModalProps) {
  const [keyData, setKeyData] = useState<string>("");
  const [selectedKey, setSelectedKey] = useState<KeyDetails>(InitialKeys[0]);
  const [keys, setKeys] = useState<KeyDetails[]>(InitialKeys);
  const [isDropdownOpen, setIsDropdownOpen] = useState<boolean>(false);

  const addKeyMutation = useMutation({
    mutationFn: addUserKeyQuery,
    onSuccess: (_, variables) => {
      notifications.show(
        getSuccessNotificationConfig(
          "API key added successfully.",
          `user-key-added-${variables.provider}`,
        ),
      );
      setKeys((prevKeys) => {
        return prevKeys.map((item) => {
          if (item.provider === variables.provider) {
            return { ...item, value: keyData };
          }

          return item;
        });
      });

      onProceed();
    },
  });

  return (
    <Modal
      opened={isOpened}
      onClose={() => {}}
      withCloseButton={false}
      size="700px"
      centered
      padding="0"
      data-testid="welcome-modal"
      classNames={{
        content: "rounded-[48px] flex flex-col items-center",
        body: "!h-[500px] w-[100%]",
      }}
    >
      <Group className="flex h-[100%] w-[100%] flex-col items-center justify-start gap-0 bg-veryLightSilver p-[32px] pt-[20px]">
        <UnstyledButton
          onClick={onProceed}
          className="mr-[10px] self-end !font-medium text-secondary text-body-14 hover:text-brand"
        >
          Skip
        </UnstyledButton>

        <div className="mb-[50px] flex w-full flex-col items-center justify-center gap-2">
          <Text className="text-center text-title-44">
            Get ready to{" "}
            <span className="text-brand text-title-44">evaluate</span>
          </Text>
          <Text className="max-w-[462px] text-center text-secondary text-body-20">
            Provide an API key to get started. It allows Stax to test your
            AI&apos;s output and use LLMs for fast, automated evaluation.
          </Text>
        </div>
        <Group gap={0} className="flex flex-col justify-between gap-[102px]">
          <Group gap={0}>
            <Group
              gap="32px"
              className="flex w-[502px] flex-col justify-between"
            >
              <Group
                gap={0}
                className="flex w-[100%] flex-row justify-between gap-md self-stretch"
              >
                <Popover
                  shadow="lg"
                  position="bottom-start"
                  offset={{ mainAxis: 10, crossAxis: 0 }}
                  opened={isDropdownOpen}
                  onChange={(open) => {
                    setIsDropdownOpen(open);
                  }}
                >
                  <PopoverTarget>
                    <UnstyledButton
                      onClick={() => {
                        setIsDropdownOpen(!isDropdownOpen);
                      }}
                      className="flex !h-[42px] w-[68px] flex-row items-center justify-between rounded-sm bg-white px-[8px] border-default"
                    >
                      <ModelIcon provider={selectedKey.provider} />
                      <MaterialIcon
                        name={
                          isDropdownOpen
                            ? "keyboard_arrow_up"
                            : "keyboard_arrow_down"
                        }
                        size={20}
                      />
                    </UnstyledButton>
                  </PopoverTarget>

                  <PopoverDropdown className="max-h-[424px] !w-[263px] overflow-y-auto !rounded-lg p-0">
                    {keys.map((item, key) => (
                      <UnstyledButton
                        key={key}
                        data-testid="models-dropdown-option"
                        className="flex cursor-pointer items-center gap-lg p-[16px]"
                        onClick={() => {
                          setSelectedKey(item);
                          setIsDropdownOpen(false);
                          setKeyData(item?.value || "");
                        }}
                      >
                        <p className="flex w-[24px] items-center justify-center text-title-12">
                          <ModelIcon provider={item.provider} />
                        </p>
                        <p className="!text-secondaryDark text-body-14">
                          {item.label}
                        </p>
                      </UnstyledButton>
                    ))}
                  </PopoverDropdown>
                </Popover>

                <TextInput
                  placeholder="Enter your API key here"
                  className="flex-1"
                  classNames={{
                    input: "!h-[42px] width-[100%]",
                  }}
                  value={keyData}
                  onChange={(e) => {
                    setKeyData(e.target.value);
                  }}
                />
              </Group>
              <Group gap={0}>
                {!keyData ? (
                  <Tooltip
                    label={TOOLTIPS.API_KEY_REQUIRED}
                    position="bottom"
                    withArrow
                    withinPortal
                  >
                    <div className="w-full">
                      <Button
                        variant="primary"
                        classNames={{
                          label: "!text-body-14 !font-medium",
                          root: "w-full",
                        }}
                        className="!h-[48px] !px-[24px] !py-[12px] large-btn"
                        disabled={!keyData}
                      >
                        Add Key
                      </Button>
                    </div>
                  </Tooltip>
                ) : (
                  <Button
                    variant="primary"
                    classNames={{
                      label: "!text-body-14 !font-medium",
                      root: "w-full",
                    }}
                    className="!h-[48px] !px-[24px] !py-[12px] large-btn"
                    loading={addKeyMutation.isPending}
                    onClick={() => {
                      addKeyMutation.mutate({
                        provider: selectedKey.provider,
                        key: keyData,
                      });
                    }}
                  >
                    Add Key
                  </Button>
                )}
              </Group>
            </Group>
          </Group>

          <p className="text-center text-secondary text-body-14">
            Don&apos;t have an API key yet?{" "}
            <Link
              href={selectedKey.link}
              className="text-brand text-body-14"
              target="_blank"
            >
              Get {selectedKey.label} API Key
            </Link>
          </p>
        </Group>
      </Group>
    </Modal>
  );
}

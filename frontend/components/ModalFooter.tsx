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

import { Button, Group, Text, Tooltip } from "@mantine/core";

type ModalFooterProps = {
  onClick: () => void;
  label: string;
  isLoading?: boolean;
  isDisabled?: boolean;
  tooltipLabel?: string;
};

export default function ModalFooterButton({
  onClick,
  label,
  isLoading,
  isDisabled,
  tooltipLabel,
}: ModalFooterProps) {
  const buttonElement = (
    <Button
      size="xl"
      className="pr-[22px]"
      variant="filled"
      onClick={onClick}
      loading={isLoading}
      disabled={isDisabled}
    >
      {label}
    </Button>
  );

  return (
    <Group className="flex w-full justify-between" gap={0}>
      <Group gap="4px">
        <Text className="text-red mb-[-2px]">*</Text>
        <Text className="text-secondary text-body-11">
          Required information
        </Text>
      </Group>

      {tooltipLabel && isDisabled ? (
        <Tooltip
          label={tooltipLabel}
          position="bottom"
          withArrow
          withinPortal={true}
        >
          <div>{buttonElement}</div>
        </Tooltip>
      ) : (
        buttonElement
      )}
    </Group>
  );
}

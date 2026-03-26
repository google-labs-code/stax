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
import TokenAutoIcon from "@/components/icons/TokenAutoIcon";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { convertMsToS, handleCopy } from "@/utils/helpers";
import { Group, Loader, Text, Tooltip, UnstyledButton } from "@mantine/core";
import { useMemo, useRef } from "react";

import { scrollToCardsContainer } from "../../utils";

interface OutputCardItemHeaderProps {
  text: string;
  isLoading: boolean;
  promptName: string;
  latency: number;
  tokens: number;
  hasExpandButton?: boolean;
  onClose?: () => void;
}

export default function OutputCardItemHeader({
  text,
  isLoading,
  promptName,
  latency,
  tokens,
  hasExpandButton,
}: OutputCardItemHeaderProps) {
  const {
    setIsOutputExpanded,
    isOutputExpanded,
    setOutputExpandAnimationStarted,
  } = usePlaygroundContext();
  const isDisabled = useMemo(() => !text || isLoading, [text, isLoading]);
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  const ExpandButton = () => (
    <UnstyledButton
      className="bg-veryLightSilver rounded-sm w-[36px] h-[36px] justify-center items-center flex"
      onClick={() => {
        scrollToCardsContainer(0, 0);
        setOutputExpandAnimationStarted(true);
        setIsOutputExpanded(!isOutputExpanded);

        timeoutRef.current = setTimeout(() => {
          setOutputExpandAnimationStarted(false);
          clearTimeout(timeoutRef.current as NodeJS.Timeout);
          timeoutRef.current = null;
        }, 1000);
      }}
    >
      <MaterialIcon
        name={isOutputExpanded ? "unfold_less" : "unfold_more"}
        size={20}
      />
    </UnstyledButton>
  );

  return (
    <Group className="h-[36px] gap-0 flex-row justify-between items-center w-full">
      <Group gap={8}>
        <Text className="flex h-6 w-6 items-center justify-center rounded-[4px] border bg-lightBlue text-brand text-title-14">
          {promptName}
        </Text>
        <Text className="text-secondaryDark text-title-16">Output</Text>
        {isLoading && (
          <Text className="flex h-[24px] w-[105px] items-center rounded-sm bg-veryLightSilver text-secondary">
            <Loader size={12} className="ml-2 text-secondary" />
            <span className="ml-2 whitespace-nowrap !leading-none text-title-11">
              IN PROGRESS
            </span>
          </Text>
        )}
        {text && !isLoading && (
          <Text className="flex h-[24px] w-[105px] items-center rounded-sm bg-veryLightSilver text-secondary">
            <MaterialIcon name="check_circle" size={16} className="ml-2" />
            <span className="ml-1 !leading-none text-title-11">COMPLETED</span>
          </Text>
        )}
      </Group>
      <Group
        gap={12}
        className={isDisabled ? "text-resting" : "text-secondary"}
      >
        {!isDisabled && (
          <Group gap={12} className="cursor-default">
            <Group gap={0}>
              <MaterialIcon
                name="timer"
                size={16}
                tooltipLabel="Average latency"
              />
              <Text className="flex pl-1">{convertMsToS(latency)}</Text>
            </Group>
            <Group gap={0}>
              <Tooltip label="Total tokens" position="bottom">
                <Group>
                  <TokenAutoIcon size={14} />
                </Group>
              </Tooltip>
              <Text className="flex pl-1">{tokens}</Text>
            </Group>
          </Group>
        )}
        <MaterialIcon
          name="content_copy"
          className={
            isDisabled ? "!cursor-default text-disabled" : "cursor-pointer"
          }
          disabled={isDisabled}
          size={24}
          onClick={() => !isDisabled && text && handleCopy(text)}
          tooltipLabel="Copy"
        />
        {hasExpandButton && <ExpandButton />}
      </Group>
    </Group>
  );
}

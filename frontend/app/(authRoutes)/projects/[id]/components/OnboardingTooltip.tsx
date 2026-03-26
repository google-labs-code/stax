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
import LocalStorage from "@/utils/LocalStorage";
import { Button, FloatingPosition, Group, Text, Tooltip } from "@mantine/core";
import { ReactNode, useEffect, useState } from "react";

interface OnboardingTooltipProps {
  readonly children: ReactNode;
  readonly opened: boolean;
  readonly onNext: () => void;
  readonly onPrevious?: () => void;
  readonly onClose?: () => void;
  readonly index: number;
  readonly position?: FloatingPosition;
  readonly content: { title: string; body: string }[];
  readonly localStorageItemName: string;
  readonly forceHide?: boolean;
}

export default function OnboardingTooltip({
  children,
  opened,
  onNext,
  onPrevious,
  onClose,
  index,
  position,
  content,
  localStorageItemName,
  forceHide,
}: OnboardingTooltipProps) {
  const hasSeen = LocalStorage.get(localStorageItemName);
  const [showTooltip, setShowTooltip] = useState(!hasSeen);
  const isLastStep = index === content.length - 1;

  useEffect(() => {
    if (!hasSeen && !forceHide) {
      setShowTooltip(true);
      LocalStorage.set(localStorageItemName, "true");
    }
  }, [localStorageItemName, hasSeen, forceHide]);

  if (!showTooltip || forceHide) return children;

  return (
    <>
      {showTooltip && opened && (
        <div
          className="fixed inset-0 z-[99] bg-transparent"
          onClick={onClose}
        />
      )}
      <Tooltip
        withArrow
        position={position}
        arrowSize={16}
        arrowOffset={24}
        arrowPosition="side"
        opened={showTooltip && opened}
        withinPortal={true}
        classNames={{
          arrow: "bg-secondaryDark",
          tooltip:
            "pointer-events-auto !bg-secondaryDark rounded-lg p-5 w-[420px] z-[100]",
        }}
        label={
          <Group className="flex-col justify-between text-white" gap={12}>
            <Group>
              <Group className="flex-1 justify-between" gap={8}>
                <Text className="text-title-16">{content[index].title}</Text>
                <Text className="whitespace-normal break-words text-body-14">
                  {content[index].body}
                </Text>
              </Group>
              <Group className="cursor-pointer self-start" onClick={onClose}>
                <MaterialIcon name="close" />
              </Group>
            </Group>
            <Group className="w-full justify-between">
              <Text className="text-title-14">
                {index + 1} of {content.length}
              </Text>
              <Group gap={8}>
                {index > 0 && (
                  <Button
                    variant="subtle"
                    className="min-w-[120px] border-white text-white"
                    onClick={() => onPrevious && onPrevious()}
                  >
                    Back
                  </Button>
                )}
                <Button variant="defaultDisabled" onClick={() => onNext()}>
                  {isLastStep ? "Get Started" : "Next"}
                </Button>
              </Group>
            </Group>
          </Group>
        }
      >
        {children}
      </Tooltip>
    </>
  );
}

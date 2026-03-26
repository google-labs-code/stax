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

import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import Card from "@/components/Card";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { Group } from "@mantine/core";

import OnboardingTooltip from "../../../components/OnboardingTooltip";
import {
  PLAYGROUND_ONBOARDING,
  SHOW_PLAYGROUND_ONBOARDING,
} from "../../consts";
import { PROMPTS } from "../../types";
import OutputCardEvaluator from "./OutputCardEvaluator";
import OutputCardItem from "./OutputCardItem";

interface OutputCardProps {
  className?: string;
  isInputClosing?: boolean;
  isExpanded?: boolean;
}

export default function OutputCard({
  className = "",
  isInputClosing = false,
  isExpanded = false,
}: OutputCardProps) {
  const { isSideBySide } = useProjectContext();
  const {
    outputs,
    isLoading,
    onboardingIndex,
    setOnboardingIndex,
    isOutputExpanded,
  } = usePlaygroundContext();

  return (
    <Card
      data-testid="output-card"
      className={`flex flex-col border-default w-full justify-between rounded-[16px] p-0 pt-3 shadow-none 
        ${isOutputExpanded ? "min-h-[calc(100%-72px)]" : "min-h-[28%]"} 
        transition-all duration-1000 ease-in-out
        ${className}
        ${isInputClosing ? "opacity-100 scale-100" : isExpanded ? "opacity-0 scale-95" : "opacity-100 scale-100"}`}
    >
      <Group
        gap={0}
        className="flex-1 overflow-hidden w-full flex-row justify-between"
      >
        <Group
          className={`h-full self-start items-start ${isSideBySide ? "w-[50%]" : "w-full"} px-4`}
          gap={0}
        >
          <OutputCardItem
            text={outputs?.[0]?.text || ""}
            tokens={outputs?.[0]?.tokens || 0}
            latency={outputs?.[0]?.latency || 0}
            error={outputs?.[0]?.error || ""}
            isLoading={isLoading?.[0]}
            promptName={PROMPTS.PROMPT_A}
            data-testid="output-card-item-A"
            hasExpandButton={!isSideBySide}
          />
        </Group>
        {isSideBySide && (
          <Group
            gap={0}
            className={`h-full self-start items-start w-[50%] border-l-default px-4`}
          >
            <OutputCardItem
              text={outputs?.[1]?.text || ""}
              tokens={outputs?.[1]?.tokens || 0}
              latency={outputs?.[1]?.latency || 0}
              error={outputs?.[1]?.error || ""}
              isLoading={isLoading?.[1]}
              promptName={PROMPTS.PROMPT_B}
              data-testid="output-card-item-B"
              hasExpandButton={true}
            />
          </Group>
        )}
      </Group>
      <Group className="flex-row justify-between w-full p-4" gap={8}>
        <Group className="w-full justify-center">
          <OnboardingTooltip
            localStorageItemName={SHOW_PLAYGROUND_ONBOARDING}
            opened={onboardingIndex === 3}
            onClose={() => setOnboardingIndex(null)}
            onNext={() => setOnboardingIndex(4)}
            onPrevious={() => setOnboardingIndex(2)}
            index={3}
            position="top-start"
            content={PLAYGROUND_ONBOARDING}
            data-testid="onboarding-tooltip"
          >
            <Group />
          </OnboardingTooltip>
        </Group>

        <OutputCardEvaluator />
      </Group>
    </Card>
  );
}

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
import MaterialIcon from "@/components/MaterialIcon";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { Model } from "@/queries/types";
import { InferenceChatCompletionPromptRole } from "@/types";
import { Group, Loader, Text } from "@mantine/core";
import { useMemo } from "react";

import OnboardingTooltip from "../../../components/OnboardingTooltip";
import {
  PLAYGROUND_ONBOARDING,
  SHOW_PLAYGROUND_ONBOARDING,
} from "../../consts";
import { InputsPlaygroundType, PROMPTS } from "../../types";
import ManageVariablesModal from "../ManageVariablesModal";
import InputCardModelSelector from "./InputCardModelSelector";
import InputMessageBox from "./InputMessageBox";

interface InputCardProps {
  readonly promptName: PROMPTS;
  readonly onDeleteInput: (id: string) => void;
  readonly systemInstruction: InputsPlaygroundType;
  readonly onSystemInstructionChange: (text: string) => void;
  readonly model: Model;
  readonly setModel: (model: Model | null) => void;
}

export default function InputCard({
  promptName,
  onDeleteInput,
  systemInstruction,
  onSystemInstructionChange,
  model,
  setModel,
}: InputCardProps) {
  const {
    isHumanEvalReady,
    isSaving,
    isSaved,
    onboardingIndex,
    setOnboardingIndex,
    inputs,
  } = usePlaygroundContext();
  const { isSideBySide } = useProjectContext();

  const filteredInputs = useMemo(() => {
    return [...inputs].filter(
      (input) =>
        input.promptName === promptName || input.promptName === PROMPTS.COMMON,
    );
  }, [inputs, promptName]);

  return (
    <Card
      w={isSideBySide ? "50%" : "100%"}
      className={`h-full min-h-[100%] flex justify-between self-start p-4 shadow-none border-none rounded-none relative overflow-visible ${isSideBySide && promptName === PROMPTS.PROMPT_B && "pl-4"} ${isSideBySide && promptName === PROMPTS.PROMPT_A && "pr-4"}`}
    >
      <ManageVariablesModal />

      <Group gap={1}>
        <Group className="mb-3 w-full" gap={8}>
          <Text className="flex h-6 w-6 items-center justify-center rounded-xs border bg-lightBlue text-brand text-title-14">
            {promptName}
          </Text>
          <Text className="text-secondaryDark text-title-16">Prompt</Text>
          {isSaving && (
            <Text className="flex h-[24px] w-[73px] items-center rounded-sm bg-veryLightSilver text-secondary">
              <Loader size={12} className="ml-2 text-secondary" />
              <span className="ml-1 whitespace-nowrap !leading-none text-title-11">
                SAVING
              </span>
            </Text>
          )}
          {isSaved && !isSaving && isHumanEvalReady && (
            <Text className="flex h-[24px] w-[73px] items-center rounded-sm bg-veryLightSilver text-secondary">
              <MaterialIcon name="check_circle" size={16} className="ml-2" />
              <span className="ml-1 !leading-none text-title-11">SAVED</span>
            </Text>
          )}
        </Group>
        <Card
          className={`flex w-full rounded-lg border-borderColor bg-veryLightSilver p-2 shadow-none`}
        >
          <InputCardModelSelector model={model} setModel={setModel} />
        </Card>
        <OnboardingTooltip
          localStorageItemName={SHOW_PLAYGROUND_ONBOARDING}
          opened={onboardingIndex === 0}
          onNext={() => setOnboardingIndex && setOnboardingIndex(1)}
          onClose={() => setOnboardingIndex && setOnboardingIndex(null)}
          index={0}
          position="bottom-start"
          content={PLAYGROUND_ONBOARDING}
        >
          <Group className="ml-8 self-start" />
        </OnboardingTooltip>
        <Group className={`w-full`}>
          <InputMessageBox
            key={systemInstruction?.id || ""}
            id={systemInstruction?.id || ""}
            role={InferenceChatCompletionPromptRole.SYSTEM}
            isSystemInstruction
            text={systemInstruction?.text || ""}
            onInstructionsChange={(text: string) =>
              onSystemInstructionChange(text)
            }
            filteredInputs={filteredInputs}
          />
        </Group>

        {filteredInputs.map(
          (input, index) =>
            input.id &&
            !input.hidden && (
              <Group
                key={index}
                className="flex-nowrap justify-between w-full relative"
              >
                <Group unstyled className="w-full">
                  <InputMessageBox
                    id={input.id}
                    role={input.role}
                    text={input.text}
                    filteredInputs={filteredInputs}
                    onDeleteInput={onDeleteInput}
                    attachment={input?.attachment}
                  />
                </Group>

                {isSideBySide && promptName === PROMPTS.PROMPT_A && (
                  <Group
                    h={28}
                    w={28}
                    className="rounded-sm absolute right-[-30px] top-[36px] z-[90] border-default bg-white flex-row justify-center items-center"
                  >
                    <MaterialIcon name="link" size={20} />
                  </Group>
                )}
              </Group>
            ),
        )}
        <OnboardingTooltip
          localStorageItemName={SHOW_PLAYGROUND_ONBOARDING}
          opened={onboardingIndex === 1}
          onNext={() => setOnboardingIndex && setOnboardingIndex(2)}
          onPrevious={() => setOnboardingIndex && setOnboardingIndex(0)}
          onClose={() => setOnboardingIndex && setOnboardingIndex(null)}
          index={1}
          position="bottom-start"
          content={PLAYGROUND_ONBOARDING}
        >
          <Group className="ml-8 self-start" />
        </OnboardingTooltip>
      </Group>
    </Card>
  );
}

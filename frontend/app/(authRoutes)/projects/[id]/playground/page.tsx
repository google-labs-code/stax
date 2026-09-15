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

import BreadcrumbSegment from "@/components/BreadcrumbSegment";
import MaterialIcon from "@/components/MaterialIcon";
import Page from "@/components/Page";
import PageHeader from "@/components/PageHeader";
import { PLAYGROUND_CUSTOM_INPUT_NAME } from "@/config/constants";
import { routes } from "@/config/routes";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { Model } from "@/queries/types";
import { InferenceChatCompletionPromptRole } from "@/types";
import { Group, ScrollArea, Text } from "@mantine/core";
import { useParams, useRouter } from "next/navigation";
import { useCallback, useEffect, useMemo, useState } from "react";

import { useProjectContext } from "../../hooks/useProjectContext";
import OnboardingTooltip from "../components/OnboardingTooltip";
import InputCard from "./components/Input/InputCard";
import InputCardActionsRow from "./components/Input/InputCardActionsRow";
import InputMessageBox from "./components/Input/InputMessageBox";
import ManageVariablesModal from "./components/ManageVariablesModal";
import OutputCard from "./components/Output/OutputCard";
import PromptCleaningInfoBar from "./components/PromptCleaningInfoBar";
import PromptCleaningModal from "./components/PromptCleaningModal";
import { PLAYGROUND_ONBOARDING, SHOW_PLAYGROUND_ONBOARDING } from "./consts";
import { InputCardItemProps, PROMPTS } from "./types";

export default function ProjectPlayground() {
  const { projectState, isSideBySide } = useProjectContext();
  const params = useParams<{ id: string }>();
  const activeProjectId =
    projectState?.projectId ||
    projectState?.project?.project_id ||
    (params?.id as string) ||
    "";
  const {
    isPromptCleaningModalOpen,
    closePromptCleaningModalOpen,
    inputs,
    setInputs,
    setModels,
    models,
    systemInstructions,
    setSystemInstructions,
    expandedInputCard,
    setExpandedInputCard,
    resetPlayground,
    onboardingIndex,
    setOnboardingIndex,
    isOutputExpanded,
    hasShownCleaningBar,
    isSaving,
    isHumanEvalReady,
    outputExpandAnimationStarted,
    setIsPlaygroundModified,
    setIsNewChat,
  } = usePlaygroundContext();

  const [isExpanded, setIsExpanded] = useState(false);
  const [isInputClosing, setIsInputClosing] = useState(false);
  const [closingInputCard, setClosingInputCard] =
    useState<InputCardItemProps | null>(null);
  const [inputContainerHidden, setInputContainerHidden] = useState(false);

  useEffect(() => {
    setIsExpanded(!!expandedInputCard);
  }, [expandedInputCard]);

  useEffect(() => {
    setIsExpanded(!!expandedInputCard);

    if (expandedInputCard) {
      const timer = setTimeout(() => {
        setInputContainerHidden(true);
      }, 1000);
      
      return () => clearTimeout(timer);
    }
  }, [expandedInputCard]);

  useEffect(() => {
    return () => {
      resetPlayground();
    };
  }, []);

  const router = useRouter();
  const onNavigateTo = useCallback(() => {
    router.push(`${routes.projects}/${activeProjectId}`);
  }, [router, activeProjectId]);

  const onDeleteInput = (id: string) => {
    const customInputs = inputs.filter((input) =>
      input.id?.includes(PLAYGROUND_CUSTOM_INPUT_NAME),
    );
    const isInputCustom = id.includes(PLAYGROUND_CUSTOM_INPUT_NAME);

    // If we delete non custom input
    // If there is only 1 custom input left and it's deleted
    if (!isInputCustom || customInputs?.[0]?.id === id) {
      setIsNewChat(true);
    }
    setIsPlaygroundModified(true);

    const removeIds: string[] = [];
    for (let i = 0; i < inputs.length; i++) {
      const input = inputs[i];

      // If the message is the one to delete
      if (input?.id === id) {
        // If the previous message is assistant also remove it
        if (
          inputs?.[i - 1] &&
          inputs?.[i - 1]?.role === InferenceChatCompletionPromptRole.ASSISTANT
        ) {
          removeIds.push(inputs[i - 1]?.id || "");
        }
        removeIds.push(input?.id || "");
      }
    }

    setInputs((prevState) =>
      prevState.filter((input) => !removeIds.includes(input?.id || "")),
    );
  };

  const handleInputCollapse = useCallback(() => {
    if (expandedInputCard) {
      setInputContainerHidden(false);

      requestAnimationFrame(() => {
        setIsInputClosing(true);
        setClosingInputCard(expandedInputCard);
      });

      const timer = setTimeout(() => {
        setExpandedInputCard(undefined);
        setClosingInputCard(null);
        setIsInputClosing(false);
      }, 700);

      return () => clearTimeout(timer);
    }
  }, [expandedInputCard, setExpandedInputCard]);

  const isPromptInfoBarVisible = useMemo(() => {
    return isHumanEvalReady && !isSaving && !hasShownCleaningBar;
  }, [isHumanEvalReady, isSaving, hasShownCleaningBar]);

  const isOutputExpandedAndNotAnimating = useMemo(() => {
    return isOutputExpanded && !outputExpandAnimationStarted;
  }, [isOutputExpanded, outputExpandAnimationStarted]);

  const filteredInputs = useMemo(() => {
    return inputs.filter((input) => !input.hidden);
  }, [inputs]);

  const renderOutput = !isExpanded || isInputClosing;

  return (
    <Page fullHeight className={isOutputExpanded ? "overflow-hidden" : ""}>
      <ManageVariablesModal />
      <PageHeader
        title={""}
        breadcrumbs={
          <>
            <BreadcrumbSegment
              label="Evaluation Projects"
              routes={routes.projects}
            />
            <BreadcrumbSegment
              label={projectState?.project?.name || "Untitled"}
              routes={`${routes.projects}/${activeProjectId}`}
              showArrow={false}
            />
            <Group>
              <Group onClick={onNavigateTo} gap={1} className="cursor-pointer">
                <Text className="flex justify-center items-center ml-2 flex h-[24px] w-[91px] rounded-l-xs border bg-lightBlue p-2 text-brand text-title-11">
                  <span className="!leading-none text-title-11">
                    PLAYGROUND
                  </span>
                </Text>
                <Text className="flex justify-center items-center h-[24px] w-[24px] rounded-r-xs border bg-lightBlue text-center text-brand">
                  <MaterialIcon
                    className="!text-[13px] !leading-none"
                    name="close"
                  />
                </Text>
              </Group>
              <OnboardingTooltip
                localStorageItemName={SHOW_PLAYGROUND_ONBOARDING}
                opened={onboardingIndex === 4}
                onClose={() => setOnboardingIndex(null)}
                onNext={() => setOnboardingIndex(null)}
                onPrevious={() => setOnboardingIndex(3)}
                index={4}
                position="top-start"
                content={PLAYGROUND_ONBOARDING}
              >
                <Group className="mb-[-32px] ml-[-64px]"></Group>
              </OnboardingTooltip>
            </Group>
          </>
        }
      />

      <Group className="flex h-full gap-4 overflow-hidden flex-col justify-between flex-nowrap">
        <Group
          gap={0}
          className={`mb-1 flex-nowrap justify-between flex-col items-stretch overflow-hidden !w-full rounded-lg bg-white border-[1px] border-solid border-lightSilver 
            ${isExpanded && !isInputClosing ? "h-0 min-h-0 m-0 p-0 border-0 opacity-0" : isOutputExpanded ? "min-h-[54px]" : "min-h-[68%]"} 
            ${isOutputExpanded && !outputExpandAnimationStarted && "h-[54px]"}
            ${inputContainerHidden ? "hidden" : ""} 
            transition-all duration-1000 ease-in-out`}
        >
          <ScrollArea
            scrollbarSize={10}
            type="auto"
            className="flex flex-row w-full min-h-[54px] h-[calc(100%-70px)]"
            classNames={{
              viewport: `playground-scroll-area-viewport ${isOutputExpandedAndNotAnimating && "playground-scroll-area-viewport-expanded"}`,
            }}
          >
            <InputCard
              promptName={PROMPTS.PROMPT_A}
              model={models?.[0]}
              setModel={(model: Model | null) => {
                setModels((prev) => {
                  const finalModels = [{ ...model }, null];
                  if (prev[1]) {
                    finalModels[1] = { ...prev[1] };
                  }

                  return finalModels as Model[];
                });
              }}
              onDeleteInput={onDeleteInput}
              systemInstruction={systemInstructions?.[0]}
              onSystemInstructionChange={(text: string) => {
                setSystemInstructions((prev) => [
                  {
                    ...prev?.[0],
                    role: InferenceChatCompletionPromptRole.SYSTEM,
                    text: text || "",
                    promptName: PROMPTS.PROMPT_A,
                  },
                  {
                    ...(prev[1] || null),
                  },
                ]);
              }}
            />

            {isSideBySide && (
              <InputCard
                promptName={PROMPTS.PROMPT_B}
                model={models?.[1]}
                setModel={(model: Model | null) => {
                  setModels((prev) => {
                    const finalModels = [null, { ...model }];
                    if (prev[0]) {
                      finalModels[0] = { ...prev[0] };
                    }

                    return finalModels as Model[];
                  });
                }}
                onDeleteInput={onDeleteInput}
                systemInstruction={systemInstructions?.[1]}
                onSystemInstructionChange={(text: string) => {
                  setSystemInstructions((prev) => [
                    {
                      ...(prev[0] || null),
                    },
                    {
                      ...prev?.[1],
                      role: InferenceChatCompletionPromptRole.SYSTEM,
                      text: text || "",
                      promptName: PROMPTS.PROMPT_B,
                    },
                  ]);
                }}
              />
            )}

            {isSideBySide && (
              <div className="w-[0.1px] bg-borderColor h-[100%] absolute left-[50%] translate-x-[-50%]"></div>
            )}
          </ScrollArea>

          <Group
            className={`w-full flex-col justify-between items-start gap-0 p-4 ${isPromptInfoBarVisible ? "h-[124px]" : "h-[70px]"} flex-nowrap`}
          >
            {isPromptInfoBarVisible && <PromptCleaningInfoBar />}
            {!isOutputExpanded && <InputCardActionsRow />}
          </Group>
        </Group>

        {(expandedInputCard?.id ||
          (closingInputCard?.id && isInputClosing)) && (
          <InputMessageBox
            id={expandedInputCard?.id || closingInputCard?.id || ""}
            role={
              expandedInputCard?.role ||
              closingInputCard?.role ||
              InferenceChatCompletionPromptRole.USER
            }
            text={expandedInputCard?.text || closingInputCard?.text || ""}
            filteredInputs={filteredInputs}
            onDeleteInput={onDeleteInput}
            attachment={
              inputs.find(
                (i) => i.id === (expandedInputCard?.id || closingInputCard?.id),
              )?.attachment
            }
            isExpandCard={true}
            isClosing={isInputClosing}
            onCollapse={handleInputCollapse}
          />
        )}

        {renderOutput && (
          <OutputCard isInputClosing={isInputClosing} isExpanded={isExpanded} />
        )}
      </Group>
      <PromptCleaningModal
        isOpened={isPromptCleaningModalOpen}
        onClose={closePromptCleaningModalOpen}
      />
    </Page>
  );
}

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

import {
  InputCardItemProps,
  InputsPlaygroundType,
  OutputCardItemProps,
  PROMPTS,
} from "@/app/(authRoutes)/projects/[id]/playground/types";
import {
  generateId,
  scrollToCardsContainer,
} from "@/app/(authRoutes)/projects/[id]/playground/utils";
import { InferenceStatus } from "@/app/(authRoutes)/projects/[id]/types";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { PLAYGROUND_CUSTOM_INPUT_NAME } from "@/config/constants";
import {
  getChatHistory,
  getChatHistoryFromCurrentChatTurnId,
} from "@/queries/clientQueries";
import { Model } from "@/queries/types";
import {
  ChatHistory,
  InferenceChatCompletionPromptRole,
  ProjectType,
} from "@/types";
import { useDisclosure } from "@mantine/hooks";
import {
  Dispatch,
  PropsWithChildren,
  SetStateAction,
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";

import { useModelsContext } from "./useModelsContext";

type PlaygroundContextType = {
  outputs: (OutputCardItemProps | undefined)[];
  setOutputs: Dispatch<SetStateAction<(OutputCardItemProps | undefined)[]>>;
  isPromptCleaningModalOpen: boolean;
  openPromptCleaningModalOpen: () => void;
  closePromptCleaningModalOpen: () => void;
  selectedChatTurnIds: string[];
  setSelectedChatTurnIds: (value: string[]) => void;
  humanEvaluator: {
    value: number | string | null;
    notes: string | null;
  };
  setHumanEvaluator: (value: {
    value: number | string | null;
    notes: string | null;
  }) => void;
  selectedChatIds: string[];
  setSelectedChatIds: (value: string[]) => void;
  inputs: InputsPlaygroundType[];
  setInputs: Dispatch<SetStateAction<InputsPlaygroundType[]>>;
  models: Model[];
  setModels: Dispatch<SetStateAction<Model[]>>;
  systemInstructions: InputsPlaygroundType[];
  setSystemInstructions: Dispatch<SetStateAction<InputsPlaygroundType[]>>;
  pairId: string;
  setPairId: Dispatch<SetStateAction<string>>;
  isHumanEvalReady: boolean;
  setHumanEvalReady: Dispatch<SetStateAction<boolean>>;
  variables: { [key: string]: string } | null;
  setVariables: Dispatch<SetStateAction<{ [key: string]: string } | null>>;
  isManageVariablesModalOpened: boolean;
  openManageVariablesModal: () => void;
  closeManageVariablesModal: () => void;
  isLoading: boolean[];
  setIsLoading: Dispatch<SetStateAction<boolean[]>>;
  isSaving: boolean;
  setIsSaving: Dispatch<SetStateAction<boolean>>;
  isSaved: boolean;
  setIsSaved: Dispatch<SetStateAction<boolean>>;
  lastChatTurnId: string;
  setLastChatTurnId: Dispatch<SetStateAction<string>>;
  setExpandedInputCard: Dispatch<
    SetStateAction<InputCardItemProps | undefined>
  >;
  expandedInputCard: InputCardItemProps | undefined;
  setIsOutputExpanded: Dispatch<SetStateAction<boolean>>;
  isOutputExpanded: boolean;
  resetPlayground: () => void;
  onboardingIndex: number | null;
  setOnboardingIndex: Dispatch<SetStateAction<number | null>>;
  loadData: (projectType: ProjectType) => Promise<void>;
  hasShownCleaningBar: boolean;
  setHasShownCleaningBar: Dispatch<SetStateAction<boolean>>;
  outputExpandAnimationStarted: boolean;
  setOutputExpandAnimationStarted: Dispatch<SetStateAction<boolean>>;
  isPlaygroundModified: boolean;
  setIsPlaygroundModified: Dispatch<SetStateAction<boolean>>;
  isNewChat: boolean;
  setIsNewChat: Dispatch<SetStateAction<boolean>>;
};

const PlaygroundContext = createContext<PlaygroundContextType | null>(null);

export const PlaygroundProvider = (props: PropsWithChildren) => {
  const { children } = props;
  const { setOpenedModel } = useModelsContext();

  const [outputs, setOutputs] = useState<(OutputCardItemProps | undefined)[]>([
    undefined,
    undefined,
  ]);
  const [
    isPromptCleaningModalOpen,
    { open: openPromptCleaningModalOpen, close: closePromptCleaningModalOpen },
  ] = useDisclosure(false);
  const [selectedChatTurnIds, setSelectedChatTurnIds] = useState<string[]>([]);
  const [humanEvaluator, setHumanEvaluator] = useState<{
    value: number | string | null;
    notes: string | null;
  }>({ value: null, notes: null });
  const [selectedChatIds, setSelectedChatIds] = useState<string[]>([]);
  const [inputs, setInputs] = useState<InputsPlaygroundType[]>([]);
  const [models, setModels] = useState<Model[]>([]);
  const [systemInstructions, setSystemInstructions] = useState<
    InputsPlaygroundType[]
  >([]);
  const [pairId, setPairId] = useState<string>("");
  const [isHumanEvalReady, setHumanEvalReady] = useState(false);
  const [variables, setVariables] = useState<{ [key: string]: string } | null>(
    null,
  );
  const [
    isManageVariablesModalOpened,
    { open: openManageVariablesModal, close: closeManageVariablesModal },
  ] = useDisclosure(false);
  const [isLoading, setIsLoading] = useState<boolean[]>([]);
  const [isSaving, setIsSaving] = useState(false);
  const [isSaved, setIsSaved] = useState(false);
  const [lastChatTurnId, setLastChatTurnId] = useState<string>("");
  const [expandedInputCard, setExpandedInputCard] = useState<
    InputCardItemProps | undefined
  >(undefined);
  const [isOutputExpanded, setIsOutputExpanded] = useState(false);
  const [onboardingIndex, setOnboardingIndex] = useState<number | null>(0);
  const [hasShownCleaningBar, setHasShownCleaningBar] = useState(false);
  const [isPlaygroundModified, setIsPlaygroundModified] =
    useState<boolean>(false);
  const [isNewChat, setIsNewChat] = useState<boolean>(false);
  const [outputExpandAnimationStarted, setOutputExpandAnimationStarted] =
    useState(false);
  const { isSideBySide, projectState } = useProjectContext();

  const generateDefaultInputs = useCallback(() => {
    return [
      {
        id: `${PLAYGROUND_CUSTOM_INPUT_NAME}-${generateId()}`,
        role: InferenceChatCompletionPromptRole.USER,
        promptName: isSideBySide ? PROMPTS.COMMON : PROMPTS.PROMPT_A,
        text: "",
      },
    ];
  }, [isSideBySide]);

  const resetPlayground = () => {
    setInputs(generateDefaultInputs());
    setOutputs([]);
    setHumanEvalReady(false);
    setHumanEvaluator({
      value: null,
      notes: null,
    });
    setIsOutputExpanded(false);
    setExpandedInputCard(undefined);
    setSelectedChatTurnIds([]);
    setSelectedChatIds([]);
    setSystemInstructions([]);
    setModels([]);
    setOpenedModel(null);
    setOnboardingIndex(0);
    setLastChatTurnId("");
    setVariables(null);
    setHasShownCleaningBar(false);
    setPairId("");
    setIsPlaygroundModified(false);
    setIsNewChat(false);
  };

  const loadData = async () => {
    const outputs = [];
    const models = [];
    const responses: ChatHistory[] = [];
    const finalSystemInstructions = [];

    for (let i = 0; i < (isSideBySide ? 2 : 1); i++) {
      if (selectedChatTurnIds?.[i]) {
        responses[i] = await getChatHistoryFromCurrentChatTurnId(
          selectedChatIds?.[i] ?? "",
          selectedChatTurnIds?.[i],
        );
      } else if (selectedChatIds?.[i]) {
        responses[i] = await getChatHistory(selectedChatIds[i]);
      }

      const chatTurns = responses[i]?.chat_turns;
      const lastOutput = chatTurns?.[chatTurns.length - 1]?.model_output;
      const lastChatTurn = chatTurns?.[chatTurns.length - 1];

      outputs.push({
        text: lastOutput?.text,
        tokens: lastOutput?.inference_monitoring?.turn_total_tokens,
        latency: lastOutput?.inference_monitoring?.turn_latency,
        promptName: i === 0 ? PROMPTS.PROMPT_A : PROMPTS.PROMPT_B,
        error:
          lastChatTurn?.inference_status === InferenceStatus.FAILED &&
          lastChatTurn?.inference_reason
            ? lastChatTurn?.inference_reason
            : "",
      });
      models.push((chatTurns || []).slice().reverse()?.[0]?.model || null);
      setLastChatTurnId(chatTurns?.[chatTurns.length - 1]?.chat_turn_id);

      const currentSystemInstructions = (chatTurns ?? [])
        .flatMap((turn) => turn.model_inputs)
        .filter(
          (input) => input.role === InferenceChatCompletionPromptRole.SYSTEM,
        );
      if (currentSystemInstructions[currentSystemInstructions.length - 1]) {
        finalSystemInstructions.push({
          ...currentSystemInstructions[currentSystemInstructions.length - 1],
          promptName: i === 0 ? PROMPTS.PROMPT_A : PROMPTS.PROMPT_B,
        });
      } else {
        finalSystemInstructions.push({
          id: `systemInstruction${i === 0 ? "A" : "B"}`,
          text: "",
          role: InferenceChatCompletionPromptRole.SYSTEM,
          promptName: i === 0 ? PROMPTS.PROMPT_A : PROMPTS.PROMPT_B,
        });
      }
    }

    const existingChat =
      responses[0]?.chat_turns && responses[0]?.chat_turns?.length > 0
        ? responses[0]?.chat_turns
        : responses[1]?.chat_turns && responses[1]?.chat_turns?.length > 0
          ? responses[1].chat_turns
          : [];
    const lastOutputId =
      existingChat?.[existingChat.length - 1]?.model_output?.id;

    const finalInputs: InputsPlaygroundType[] = [];
    existingChat.forEach((chat, index) => {
      const { model_inputs, model_output } = chat;
      const userInputs = model_inputs
        .filter((i) => i.role === InferenceChatCompletionPromptRole.USER)
        .map((input) => ({
          ...input,
          text: input?.raw_text || input?.text,
          promptName: isSideBySide ? PROMPTS.COMMON : PROMPTS.PROMPT_A,
        }));

      const userOutputs = [];
      if (model_output?.id) {
        userOutputs.push({
          id: model_output?.id,
          text: model_output?.text,
          role: InferenceChatCompletionPromptRole.ASSISTANT,
          promptName: PROMPTS.PROMPT_A,
          hidden: model_output?.id === lastOutputId,
          model_id: model_output?.model_id,
        });

        if (isSideBySide) {
          userOutputs.push({
            id: model_output?.id,
            text: responses[1].chat_turns?.[index]?.model_output?.text,
            role: InferenceChatCompletionPromptRole.ASSISTANT,
            promptName: PROMPTS.PROMPT_B,
            hidden: model_output?.id === lastOutputId,
            model_id: responses[1].chat_turns?.[index]?.model_output?.model_id,
          });
        }
      }

      finalInputs.push(...userInputs, ...userOutputs);
    });
    setVariables(
      responses?.[0]?.variables || responses?.[1]?.variables || null,
    );
    setOutputs(outputs);
    setModels(models);
    setExpandedInputCard(undefined);
    setOnboardingIndex(null);
    setSystemInstructions(finalSystemInstructions);

    if (finalInputs.length > 0) {
      setInputs([...finalInputs]);
    } else {
      setInputs(generateDefaultInputs());
    }

    scrollToCardsContainer();
  };

  useEffect(() => {
    if (projectState?.project?.type) {
      loadData();
    }
  }, [selectedChatTurnIds, selectedChatIds, projectState?.project?.type]);

  const playgroundContext = {
    outputs,
    setOutputs,
    isPromptCleaningModalOpen,
    openPromptCleaningModalOpen,
    closePromptCleaningModalOpen,
    selectedChatTurnIds,
    setSelectedChatTurnIds,
    humanEvaluator,
    setHumanEvaluator,
    setSelectedChatIds,
    selectedChatIds,
    inputs,
    setInputs,
    models,
    setModels,
    systemInstructions,
    setSystemInstructions,
    setPairId,
    pairId,
    isHumanEvalReady,
    setHumanEvalReady,
    variables,
    setVariables,
    isManageVariablesModalOpened,
    openManageVariablesModal,
    closeManageVariablesModal,
    isLoading,
    setIsLoading,
    isSaving,
    setIsSaving,
    isSaved,
    setIsSaved,
    lastChatTurnId,
    setLastChatTurnId,
    expandedInputCard,
    setExpandedInputCard,
    isOutputExpanded,
    setIsOutputExpanded,
    resetPlayground,
    onboardingIndex,
    setOnboardingIndex,
    loadData,
    hasShownCleaningBar,
    setHasShownCleaningBar,
    outputExpandAnimationStarted,
    setOutputExpandAnimationStarted,
    isPlaygroundModified,
    setIsPlaygroundModified,
    isNewChat,
    setIsNewChat,
  };

  return (
    <PlaygroundContext.Provider value={playgroundContext}>
      {children}
    </PlaygroundContext.Provider>
  );
};

export function usePlaygroundContext() {
  return useContext(PlaygroundContext) as PlaygroundContextType;
}

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
import MaterialIcon from "@/components/MaterialIcon";
import { MainConfig } from "@/config/config";
import { PLAYGROUND_CUSTOM_INPUT_NAME } from "@/config/constants";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import {
  continueChatBSxSQuery,
  inferenceChatCompletionQuery,
  inferenceChatCompletionSxSQuery,
} from "@/queries/clientQueries";
import { inferenceChatCompletionStreamingConnection } from "@/queries/streamingConnections";
import {
  ChatCompletionResponse,
  ChatCompletionSxSResponse,
  GAevents,
  InferenceChatCompletionPayload,
  InferenceChatCompletionPromptRole,
  StreamingResponse,
} from "@/types";
import logGAevent from "@/utils/logGAevent";
import {
  Button,
  Group,
  Loader,
  Text,
  Tooltip,
  UnstyledButton,
} from "@mantine/core";
import { useMutation } from "@tanstack/react-query";
import { useCallback, useMemo } from "react";

import OnboardingTooltip from "../../../components/OnboardingTooltip";
import {
  PLAYGROUND_ONBOARDING,
  SHOW_PLAYGROUND_ONBOARDING,
} from "../../consts";
import {
  InputsPlaygroundType,
  OutputCardItemProps,
  PROMPTS,
} from "../../types";
import {
  generateId,
  scrollDownOutputCardsContainer,
  scrollToCardsContainer,
} from "../../utils";
import ManageVariablesModal from "../ManageVariablesModal";

export default function InputCardActionsRow() {
  const {
    inputs,
    systemInstructions,
    openManageVariablesModal,
    variables,
    models,
    isLoading,
    setIsLoading,
    setHumanEvaluator,
    lastChatTurnId,
    setSelectedChatTurnIds,
    setSelectedChatIds,
    setInputs,
    setOutputs,
    setSystemInstructions,
    pairId,
    setPairId,
    setIsSaving,
    setIsSaved,
    onboardingIndex,
    setOnboardingIndex,
    setLastChatTurnId,
    setIsOutputExpanded,
    setIsPlaygroundModified,
    isPlaygroundModified,
    isNewChat,
    setIsNewChat,
  } = usePlaygroundContext();
  const { projectState, isSideBySide } = useProjectContext();

  const allCustomInputs = useMemo(() => {
    return inputs
      .filter(
        (v) =>
          v.text &&
          v.id?.includes(PLAYGROUND_CUSTOM_INPUT_NAME) &&
          (v.promptName === PROMPTS.PROMPT_A ||
            v.promptName === PROMPTS.COMMON),
      )
      .map((v: InputsPlaygroundType) => ({
        role: v.role,
        text: v.text || "",
      }));
  }, [inputs]);

  const onSavingData = useCallback(() => {
    setIsSaving(true);
    const timeoutId = setTimeout(() => {
      setIsSaving(false);
      setIsSaved(true);
    }, 2000);

    return () => clearTimeout(timeoutId);
  }, []);

  const inferenceChatCompletionMutation = useMutation({
    mutationFn: () => {
      const payload: InferenceChatCompletionPayload = {
        model_id: models?.[0]?.id,
        variables: variables || {},
      };

      if (!isNewChat) {
        payload.previous_chat_turn_id = lastChatTurnId;
        payload.prompts = [...allCustomInputs];
      } else {
        payload.prompts = [
          {
            role: InferenceChatCompletionPromptRole.SYSTEM,
            text: systemInstructions?.[0]?.text || "",
          },
          ...inputs
            .filter((v) => v.text && !v.hidden)
            .map((v: InputsPlaygroundType) => ({
              role: v.role,
              text: v.text || "",
            })),
        ];
      }

      return inferenceChatCompletionQuery(
        payload,
        projectState?.project?.project_id || "",
      );
    },
    onSuccess: (response: ChatCompletionResponse) => {
      setLastChatTurnId(response?.chat_turn_id);

      setSystemInstructions([
        {
          ...systemInstructions?.[0],
        },
      ]);
      setIsPlaygroundModified(false);
      setIsNewChat(false);
      setIsLoading([false]);
      setOutputs([
        {
          text: response.model_output?.text,
          tokens:
            response.model_output?.inference_monitoring?.turn_total_tokens,
          latency: response.model_output?.inference_monitoring?.turn_latency,
          promptName: PROMPTS.PROMPT_A,
        },
      ]);
      setHumanEvaluator({
        value: null,
        notes: null,
      });
      onSavingData();
      setSelectedChatIds([response?.chat_id]);
      setSelectedChatTurnIds([response?.chat_turn_id]);
      scrollToCardsContainer();
    },
    onError: () => {
      setIsLoading([false]);
    },
  });

  const startPointwiseStreaming = async () => {
    try {
      const payload: InferenceChatCompletionPayload = {
        model_id: models?.[0]?.id,
        variables: variables || {},
      };

      if (!isNewChat) {
        payload.previous_chat_turn_id = lastChatTurnId;
        payload.prompts = [...allCustomInputs];
      } else {
        payload.prompts = [
          {
            role: InferenceChatCompletionPromptRole.SYSTEM,
            text: systemInstructions?.[0]?.text || "",
          },
          ...inputs
            .filter((v) => v.text && !v.hidden)
            .map((v: InputsPlaygroundType) => ({
              role: v.role,
              text: v.text || "",
            })),
        ];
      }

      await inferenceChatCompletionStreamingConnection(
        projectState?.project?.project_id || "",
        {
          payload,
          onStart: () => {
            //
          },
          onData: (res: StreamingResponse) => {
            setOutputs((prev) => {
              let newText = "";
              if (prev[0]?.text) {
                newText += prev[0]?.text + res?.content;
              } else if (res?.content) {
                newText += res?.content;
              }

              return [
                {
                  ...(prev[0] as OutputCardItemProps),
                  text: newText,
                },
              ];
            });
            scrollDownOutputCardsContainer();
          },
          onError: (error: string) => {
            setOutputs((prev) => {
              return [{ ...(prev?.[0] as OutputCardItemProps), error }];
            });
            setIsLoading([false]);
          },
          onComplete: (data: any) => {
            setLastChatTurnId(data?.finalChatDTO?.chat_turn_id);
            setIsPlaygroundModified(false);
            setIsNewChat(false);
            setSystemInstructions([
              {
                ...systemInstructions?.[0],
              },
            ]);
            setIsLoading([false]);

            setOutputs([
              {
                text: data?.finalChatDTO.model_output?.text,
                tokens:
                  data?.finalChatDTO.model_output?.inference_monitoring
                    ?.turn_total_tokens,
                latency:
                  data?.finalChatDTO.model_output?.inference_monitoring
                    ?.turn_latency,
                promptName: PROMPTS.PROMPT_A,
              },
            ]);
            setHumanEvaluator({
              value: null,
              notes: null,
            });
            onSavingData();
            setSelectedChatIds([data?.finalChatDTO?.chat_id]);
            setSelectedChatTurnIds([data?.finalChatDTO?.chat_turn_id]);
            scrollToCardsContainer();
          },
        },
      );
    } catch (error) {
      setOutputs((prev) => {
        return [
          {
            ...(prev?.[0] as OutputCardItemProps),
            error: error instanceof Error ? error.message : "Unknown error",
          },
        ];
      });
    }
  };

  const onSxSQuerySuccess = (response: ChatCompletionSxSResponse) => {
    setIsPlaygroundModified(false);
    setIsNewChat(false);
    setHumanEvaluator({
      value: null,
      notes: null,
    });

    setOutputs([
      {
        text: response?.chat_turn_a?.output,
        tokens: response?.chat_turn_a?.inference_tokens?.total_tokens || 0,
        latency: response?.chat_turn_a?.inference_latency || 0,
      },
      {
        text: response?.chat_turn_b?.output,
        tokens: response?.chat_turn_b?.inference_tokens?.total_tokens || 0,
        latency: response?.chat_turn_b?.inference_latency || 0,
      },
    ]);
    setPairId(response.id);
    setSelectedChatIds([
      response?.chat_turn_a?.chat_id,
      response?.chat_turn_b?.chat_id,
    ]);
    setSelectedChatTurnIds([
      response?.chat_turn_a?.chat_turn_id,
      response?.chat_turn_b?.chat_turn_id,
    ]);
    onSavingData();
    setIsLoading([false, false]);
    scrollToCardsContainer();
  };

  const continueChatBSxSQueryMutation = useMutation({
    mutationFn: () =>
      continueChatBSxSQuery(
        {
          model_id_a: models?.[0]?.id || null,
          model_id_b: models?.[1]?.id || null,
          prompts: [...allCustomInputs],
        },
        projectState?.project?.project_id || "",
        pairId || "",
      ),
    onSuccess: (response) => {
      onSxSQuerySuccess(response);
    },
    onError: () => {
      setIsLoading([false, false]);
    },
  });

  const inferenceChatCompletionSxSMutation = useMutation({
    mutationFn: () =>
      inferenceChatCompletionSxSQuery(
        {
          model_id_a: models?.[0]?.id,
          model_id_b: models?.[1]?.id,
          model_a_instruction: systemInstructions?.[0]?.text,
          model_b_instruction: systemInstructions?.[1]?.text,
          prompts: inputs
            .filter(
              (v) =>
                !!v.text &&
                !v.hidden &&
                (v.promptName === PROMPTS.PROMPT_A ||
                  v.promptName === PROMPTS.COMMON),
            )
            .map((v: InputsPlaygroundType) => ({
              role: v.role,
              text: v.text || "",
            })),
          variables: variables || {},
        },
        projectState?.project?.project_id || "",
      ),
    onSuccess: (response) => {
      onSxSQuerySuccess(response);
    },
    onError: () => {
      setIsLoading([false, false]);
    },
  });

  const onClickGenerateOutput = () => {
    setIsLoading([true, true]);
    setOutputs([undefined, undefined]);
    setIsOutputExpanded(true);

    if (isSideBySide) {
      if (!isNewChat) {
        continueChatBSxSQueryMutation.mutate();
      } else {
        inferenceChatCompletionSxSMutation.mutate();
      }
    } else {
      if (MainConfig.isPlaygroundStreamingEnabled) {
        startPointwiseStreaming();
      } else {
        inferenceChatCompletionMutation.mutate();
      }
    }
  };

  const generateOutputNoInputsPresent = useMemo(() => {
    const visibleInputs = inputs.filter((i) => !i.hidden);

    return visibleInputs.length === 0;
  }, [inputs]);

  const generateOutputNoModelsPresent = useMemo(() => {
    let hasMissingModels = false;
    if (isSideBySide) {
      hasMissingModels = !models?.[0]?.id || !models?.[1]?.id;
    } else {
      hasMissingModels = !models?.[0]?.id;
    }

    return hasMissingModels;
  }, [isSideBySide, models]);

  const generateOutputEmptyUserInputs = useMemo(() => {
    const visibleInputs = inputs.filter((i) => !i.hidden);

    return visibleInputs.some(
      (i) => !i.text && i?.role === InferenceChatCompletionPromptRole.USER,
    );
  }, [inputs]);

  const isGenerateOutputDisabled = useMemo(() => {
    const visibleInputs = inputs.filter((i) => !i.hidden);
    const lastItem = visibleInputs[visibleInputs.length - 1];
    const isChatEndingWithAssistant =
      lastItem?.role === InferenceChatCompletionPromptRole.ASSISTANT &&
      !lastItem.hidden;

    const firstItem = inputs[0];
    const isChatStartingWithAssistant =
      firstItem?.role === InferenceChatCompletionPromptRole.ASSISTANT &&
      !firstItem.hidden;

    return (
      generateOutputNoInputsPresent || // Disable generate output when there are no inputs
      generateOutputNoModelsPresent || // Disable generate output when there are missing models
      isChatStartingWithAssistant || // Disable generate output when the chat is starting with an assistant
      isChatEndingWithAssistant || // Disable generate output when the chat is ending with an assistant
      generateOutputEmptyUserInputs || // Disable generate output when there are empty user inputs
      !isPlaygroundModified
    );
  }, [isSideBySide, models, inputs]);

  const generateOutputTooltip = useMemo(() => {
    let tooltip = "";
    if (generateOutputNoInputsPresent) {
      tooltip = "There are no inputs to generate an output.";
    } else if (generateOutputNoModelsPresent) {
      tooltip = "There are no selected models to generate an output.";
    } else if (generateOutputEmptyUserInputs) {
      tooltip = "There are empty user inputs.";
    } else if (!isPlaygroundModified) {
      tooltip = "There is no change in the inputs.";
    }

    return tooltip;
  }, [
    generateOutputNoInputsPresent,
    generateOutputNoModelsPresent,
    generateOutputEmptyUserInputs,
    isPlaygroundModified,
  ]);

  const isAddingMessageDisabled = useMemo(
    () =>
      isSideBySide ? !models?.[0]?.id || !models?.[1]?.id : !models?.[0]?.id,
    [isSideBySide, models],
  );

  const onAddMessage = () => {
    setOutputs([undefined, undefined]);
    setInputs((prevState: InputsPlaygroundType[]) => [
      ...prevState.map((p) => ({ ...p, hidden: false })),
      {
        id: `${PLAYGROUND_CUSTOM_INPUT_NAME}-${generateId()}`,
        role: InferenceChatCompletionPromptRole.USER,
        text: "",
        promptName: isSideBySide ? PROMPTS.COMMON : PROMPTS.PROMPT_A,
      },
    ]);

    scrollToCardsContainer();
  };

  return (
    <Group className="flex-row justify-between items-center w-full gap-0 h-[60px]">
      <ManageVariablesModal />
      <Group gap={10}>
        <Tooltip
          label={
            isAddingMessageDisabled ? "Please, select model(s) first." : ""
          }
          hidden={!isAddingMessageDisabled}
        >
          <UnstyledButton
            onClick={onAddMessage}
            variant="subtle"
            disabled={isAddingMessageDisabled}
            className={`flex items-center justify-center gap-sm rounded-sm p-[6px] border-default ${!isAddingMessageDisabled && "hover:bg-veryLightSilver"}`}
          >
            <MaterialIcon
              name="add"
              className={`${isAddingMessageDisabled ? "text-disabled cursor-not-allowed" : "text-secondaryDark"}`}
              size={20}
            />
            <Text
              className={` text-title-12 ${isAddingMessageDisabled ? "text-disabled cursor-not-allowed" : "text-secondaryDark"}`}
            >
              Add message
            </Text>
          </UnstyledButton>
        </Tooltip>

        <UnstyledButton
          onClick={() => openManageVariablesModal()}
          variant="subtle"
          className="flex items-center justify-center gap-sm rounded-sm p-[6px] border-default hover:bg-veryLightSilver"
        >
          <MaterialIcon
            name="data_object"
            className="text-secondaryDark"
            size={20}
          />
          <Text className="text-secondaryDark text-title-12">
            Manage variables
          </Text>
        </UnstyledButton>
      </Group>

      <div>
        <OnboardingTooltip
          localStorageItemName={SHOW_PLAYGROUND_ONBOARDING}
          opened={onboardingIndex === 2}
          onNext={() => setOnboardingIndex && setOnboardingIndex(3)}
          onPrevious={() => setOnboardingIndex && setOnboardingIndex(1)}
          onClose={() => setOnboardingIndex && setOnboardingIndex(null)}
          index={2}
          position="top-end"
          content={PLAYGROUND_ONBOARDING}
        >
          <Group className="mb-2 self-start" />
        </OnboardingTooltip>
        <Tooltip
          label={generateOutputTooltip}
          hidden={!isGenerateOutputDisabled}
        >
          <Button
            variant="defaultDisabled"
            onClick={() => {
              logGAevent(GAevents.GENERATE_OUTPUT, {
                source: "playground",
              });
              !isLoading?.[0] && onClickGenerateOutput();
            }}
            disabled={isGenerateOutputDisabled}
            classNames={{
              root: "h-[34px] !w-[127px]",
              label: "contents",
            }}
          >
            {isLoading?.[0] ? (
              <Loader color="white" size="xs" />
            ) : (
              <span className="text-title-12">Generate output</span>
            )}
          </Button>
        </Tooltip>
      </div>
    </Group>
  );
}

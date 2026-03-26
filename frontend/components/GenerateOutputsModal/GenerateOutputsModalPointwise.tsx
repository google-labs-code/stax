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

import AccordionModelProviders from "@/components/AccordionModelProviders";
import MaterialIcon from "@/components/MaterialIcon";
import { useModelsContext } from "@/hooks/useModelsContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import {
  generateOutputsQuery,
  inferenceAllChatCompletionBulkQuery,
} from "@/queries/clientQueries";
import { Model } from "@/queries/types";
import { GenerateOutputsPayload, GenerateOutputsType } from "@/types";
import {
  Button,
  Group,
  Modal,
  Popover,
  PopoverDropdown,
  PopoverTarget,
  Text,
  UnstyledButton,
} from "@mantine/core";
import { useMutation } from "@tanstack/react-query";
import { useCallback, useState } from "react";

import GenerateOutputsModalDetails from "./GenerateOutputsModalDetails";
import GenerateOutputsModalDropdownButton from "./GenerateOutputsModalDropdownButton";
import { GenerateOutputsModalProps } from "./types";

export default function GenerateOutputsModalPointwise({
  isOpened,
  onClose,
  setRowSelection,
  setActiveRows,
  chatTurnIds,
  onGenerateOutputsSuccess,
  projectId,
  isThereChatWithoutModel,
  onGenerateOutputsStart,
  selectAllRowsInProject = false,
  onClearBannerState,
  type,
}: GenerateOutputsModalProps) {
  const [models, setModels] = useState<Model[]>([]);
  const [isOpenDropdown, setOpenDropdown] = useState(false);

  const onReset = useCallback(() => {
    setModels([]);
  }, [setModels]);

  const { allModels, isLoadingModels } = useModelsContext();
  const { defaultProjectId } = useProjectsContext();

  const onModelClick = useCallback(
    (model: string) => {
      if (!models.find((m) => model === m.id)) {
        const currentModel = allModels.find((m) => m.id === model);
        currentModel && setModels((prevItems) => [...prevItems, currentModel]);
      }
      setOpenDropdown(false);
    },
    [models, setModels, allModels],
  );

  const onModalClose = useCallback(() => {
    onReset();
    setRowSelection && setRowSelection({});
    setActiveRows && setActiveRows([]);
    onClose();
  }, [setRowSelection, setActiveRows, onReset, onClose]);

  const generateSelectedOutputs = useMutation({
    mutationFn: (data: GenerateOutputsPayload) =>
      generateOutputsQuery(data, projectId || defaultProjectId),
    onSuccess: () => {
      onGenerateOutputsSuccess && onGenerateOutputsSuccess();
    },
  });

  const generateAllOutputs = useMutation({
    mutationFn: (payload: { model_ids: string[] }) =>
      inferenceAllChatCompletionBulkQuery(
        payload,
        projectId || defaultProjectId,
      ),
    onSuccess: () => {
      onGenerateOutputsSuccess && onGenerateOutputsSuccess();
      if (onClearBannerState) {
        onClearBannerState();
      }
    },
  });

  const onGenerateOutputs = useCallback(() => {
    onModalClose();
    onGenerateOutputsStart && onGenerateOutputsStart();

    const modelIds = models.map((m) => m.id);

    if (selectAllRowsInProject || type === GenerateOutputsType.ALL) {
      generateAllOutputs.mutate({ model_ids: modelIds });
    } else {
      generateSelectedOutputs.mutate({
        chat_turn_ids: chatTurnIds,
        model_ids: modelIds,
      });
    }
  }, [
    generateAllOutputs,
    generateSelectedOutputs,
    models,
    chatTurnIds,
    selectAllRowsInProject,
    onModalClose,
    onGenerateOutputsStart,
  ]);

  return (
    <Modal
      opened={isOpened}
      onClose={onModalClose}
      size="700px"
      centered
      title={
        <Text className="!font-medium text-title-22">
          Select models to generate output
        </Text>
      }
      padding="24px"
      classNames={{
        body: "flex flex-col justify-between",
        header: "pb-0 mb-[8px] rounded-lg",
      }}
    >
      <Text className="text-secondaryDark pr-0 text-body-16 mb-[24px]">
        Choose the model(s) you want to run on your input on.
      </Text>

      <Group className="flex flex-nowrap w-[100%] overflow-x-auto rounded-md flex-row items-start justify-start gap-[12px]">
        {models.map((model, key) => (
          <Group
            key={key}
            gap={4}
            className="flex flex-row items-center justify-between space-between bg-veryLightSilver flex h-[40px] flex-nowrap whitespace-nowrap rounded-sm border-default px-3 py-2"
          >
            <GenerateOutputsModalDetails model={model}>
              <div className="flex cursor-pointer items-center gap-2">
                {model.icon && <model.icon size={24} />}
                <Text className="text-title-14">{model.label}</Text>
              </div>
            </GenerateOutputsModalDetails>
            <GenerateOutputsModalDropdownButton
              onModelRemove={() => {
                setModels(models.filter((m) => m.id !== model.id));
              }}
            />
          </Group>
        ))}

        <Popover
          opened={isOpenDropdown}
          onChange={setOpenDropdown}
          position="bottom-start"
          closeOnClickOutside
          withinPortal={true}
        >
          <PopoverTarget>
            <UnstyledButton
              onClick={() => setOpenDropdown((o) => !o)}
              disabled={models.length > 2}
              data-testid="add-model"
              className={`bg-veryLightSilver flex min-h-[40px] min-w-[160px] items-center justify-center !rounded-sm border-default !px-3 !py-2 ${models.length > 2 ? "cursor-default text-gray-400" : "text-secondaryDark cursor-pointer"}`}
            >
              <MaterialIcon name="add" />
              <Text className="pl-2 text-title-14">Add Model</Text>
            </UnstyledButton>
          </PopoverTarget>

          <PopoverDropdown className="!w-[273px] max-h-[300px] overflow-y-auto rounded-lg p-0">
            {isLoadingModels ? (
              <span className="p-[16px] text-title-14">Loading...</span>
            ) : (
              <AccordionModelProviders
                filteredOptions={allModels}
                handleSelection={onModelClick}
              />
            )}
          </PopoverDropdown>
        </Popover>
      </Group>
      <Group className="flex justify-between gap-0 mt-[24px]">
        <UnstyledButton
          variant="transparent"
          className="items-center text-secondary text-title-14"
          onClick={onReset}
        >
          Reset
        </UnstyledButton>
        <Button
          variant="defaultDisabled"
          onClick={onGenerateOutputs}
          disabled={isThereChatWithoutModel && models.length === 0}
        >
          Generate outputs
        </Button>
      </Group>
    </Modal>
  );
}

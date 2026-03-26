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

import { useProjectsContext } from "@/hooks/useProjectsContext";
import {
  generateOutputsSxSAllQuery,
  generateOutputsSxSQuery,
} from "@/queries/clientQueries";
import { Model } from "@/queries/types";
import {
  GenerateOutputsSxSAllPayload,
  GenerateOutputsSxSMode,
  GenerateOutputsSxSPayload,
  GenerateOutputsType,
} from "@/types";
import { Button, Group, Modal, Text, UnstyledButton } from "@mantine/core";
import { useMutation } from "@tanstack/react-query";
import { useCallback, useState } from "react";

import GenerateOutputsModalSideBySideModelCard from "./GenerateOutputsModalSideBySideModelCard";
import { GenerateOutputsModalProps } from "./types";

export default function GenerateOutputsModalSideBySide({
  isOpened,
  onClose,
  setRowSelection,
  setActiveRows,
  chatTurnIds,
  onGenerateOutputsSuccess,
  projectId,
  onGenerateOutputsStart,
  sxsPairIds,
  type,
  selectAllRowsInProject = false,
  isThereChatWithModel,
}: GenerateOutputsModalProps) {
  const [models, setModels] = useState<Model[]>([{} as Model, {} as Model]);

  const onReset = useCallback(() => {
    setModels([{} as Model, {} as Model]);
  }, [setModels]);

  const { defaultProjectId } = useProjectsContext();

  const onModalClose = useCallback(() => {
    onReset();
    setRowSelection && setRowSelection({});
    setActiveRows && setActiveRows([]);
    onClose();
  }, [setRowSelection, setActiveRows, onReset, onClose]);

  const generateOutputs = useMutation({
    mutationFn: (data: GenerateOutputsSxSPayload) =>
      generateOutputsSxSQuery(projectId || defaultProjectId, data),
    onSuccess: () => {
      onGenerateOutputsSuccess && onGenerateOutputsSuccess();
    },
  });

  const generateOutputsAll = useMutation({
    mutationFn: (data: GenerateOutputsSxSAllPayload) =>
      generateOutputsSxSAllQuery(projectId || defaultProjectId, data),
    onSuccess: () => {
      onGenerateOutputsSuccess && onGenerateOutputsSuccess();
    },
  });

  const onGenerateOutputs = useCallback(() => {
    onModalClose();
    onGenerateOutputsStart && onGenerateOutputsStart();
    if (type === GenerateOutputsType.ALL || selectAllRowsInProject) {
      generateOutputsAll.mutate({
        modelA: models[0]?.id || null,
        modelB: models[1]?.id || null,
        mode: GenerateOutputsSxSMode.AUTORESOLVE,
      });
    } else {
      generateOutputs.mutate({
        sxsPairIds: sxsPairIds || [],
        modelA: models[0]?.id || null,
        modelB: models[1]?.id || null,
        mode: GenerateOutputsSxSMode.AUTORESOLVE,
      });
    }
  }, [
    generateOutputs,
    generateOutputsAll,
    models,
    chatTurnIds,
    defaultProjectId,
    selectAllRowsInProject,
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

      <Group className="flex flex-row gap-[8px] flex-nowrap">
        <GenerateOutputsModalSideBySideModelCard
          model={models[0]}
          label="A"
          onModelSelect={(model) => {
            setModels([model, models[1]]);
          }}
        />
        <GenerateOutputsModalSideBySideModelCard
          model={models[1]}
          label="B"
          onModelSelect={(model) => {
            setModels([models[0], model]);
          }}
        />
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
          disabled={
            models.filter((m) => m?.id).length < 1 &&
            type === GenerateOutputsType.SELECTED_ROWS &&
            !isThereChatWithModel
          }
        >
          Generate outputs
        </Button>
      </Group>
    </Modal>
  );
}

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

import JsonCodeMirror from "@/components/JsonCodeMirror";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { updateChatVariablesQuery } from "@/queries/clientQueries";
import { ProjectType } from "@/types";
import { parseJsonSafely, validateJson } from "@/utils/jsonUtils";
import { Button, Group, Modal, Stack, Text } from "@mantine/core";
import { useMutation } from "@tanstack/react-query";
import { useEffect, useState } from "react";

import { useProjectContext } from "../../../hooks/useProjectContext";

interface ManageVariablesModalProps {
  isOpen?: boolean;
  onClose?: () => void;
  variables?: Record<string, string>;
  onUpdate?: (variables: Record<string, string>) => void;
  chatIds?: string[];
}

export default function ManageVariablesModal({
  isOpen,
  onClose,
  variables: externalVariables,
  onUpdate,
  chatIds: externalChatIds,
}: ManageVariablesModalProps = {}) {
  const playgroundContext = usePlaygroundContext();
  const {
    variables: contextVariables,
    setVariables: setContextVariables,
    isManageVariablesModalOpened: contextModalOpened,
    closeManageVariablesModal: contextCloseModal,
    selectedChatIds: contextChatIds,
  } = playgroundContext || {};

  const isModalOpened = isOpen !== undefined ? isOpen : contextModalOpened;
  const closeModal = onClose || contextCloseModal;
  const selectedChatIds = externalChatIds || contextChatIds;

  const projectContext = useProjectContext();

  const { projectState } = projectContext || {};
  const [localVariables, setLocalVariables] = useState<string>("");
  const [jsonError, setJsonError] = useState<string | null>(null);

  const handleValidateJson = (jsonString: string): boolean => {
    const result = validateJson(jsonString);
    setJsonError(result.error);

    return result.valid;
  };

  const handleCodeMirrorChange = (value: string) => {
    setLocalVariables(value);
    handleValidateJson(value);
  };

  const updateVariables = () => {
    const parsedVars = parseJsonSafely(localVariables);
    if (!parsedVars) return;

    if (onUpdate) {
      onUpdate(parsedVars);
    } else if (setContextVariables) {
      setContextVariables(parsedVars);
    }
  };

  const formatVariables = (variables: string) => {
    const parsed = parseJsonSafely(variables);

    return parsed || {};
  };

  const updateVariablesMutation = useMutation({
    mutationFn: () => {
      return updateChatVariablesQuery(
        selectedChatIds?.[0] || "",
        formatVariables(localVariables),
      );
    },
    onSuccess: () => {
      updateVariables();
    },
  });

  const updateSxSVariablesMutation = useMutation({
    mutationFn: (chatId: string) => {
      return updateChatVariablesQuery(chatId, formatVariables(localVariables));
    },
    onSuccess: () => {
      updateVariables();
    },
  });

  useEffect(() => {
    if (isModalOpened) {
      const varsToUse =
        externalVariables !== undefined ? externalVariables : contextVariables;

      if (varsToUse) {
        const variablesString =
          varsToUse && Object.keys(varsToUse).length > 0
            ? JSON.stringify(varsToUse, null, 2)
            : "";
        setLocalVariables(variablesString);
        handleValidateJson(variablesString);
      } else {
        setLocalVariables("");
      }
    }
  }, [isModalOpened, externalVariables, contextVariables]);

  if (!isModalOpened) return null;

  return (
    <Modal
      opened={true}
      onClose={closeModal}
      closeButtonProps={{ "aria-label": "close button" }}
      size="700px"
      title={
        <Text className="text-title-22 flex items-center gap-2 !font-medium">
          Manage variables
        </Text>
      }
      centered
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[16px]",
      }}
    >
      <Group className="gap-2 mb-[16px]">
        <Text className="text-secondaryDark text-body-14">
          Define <span className="font-bold">variables</span> in JSON format.
          You can then insert them anywhere in your prompt using the{" "}
          <span className="font-bold">{"{{variable}}"}</span> syntax. This keeps
          your data separate from your instructions, making prompts flexible and
          easy to test.
        </Text>
        <Text className="text-secondary text-body-14">
          Example: {'{"company":"Google", "product": "Stax"}'}
        </Text>
      </Group>

      <Stack className="modal-form-two-rows-inputs">
        <JsonCodeMirror
          value={localVariables}
          onChange={handleCodeMirrorChange}
          height="400px"
          className="gap-0 border-default text-code-14 p-2 rounded-sm !outline-none items-start overflow-y-auto"
          error={jsonError}
        />

        <Text className="text-red text-body-14 min-h-[20px]">{jsonError}</Text>

        <Group className="flex w-full justify-end" gap={0}>
          <Button
            size="xl"
            variant="filled"
            onClick={() => {
              if (onUpdate) {
                updateVariables();
                closeModal();
              } else if (playgroundContext) {
                if (projectState?.project?.type === ProjectType.SIDE_BY_SIDE) {
                  if (selectedChatIds?.[0]) {
                    updateSxSVariablesMutation.mutate(selectedChatIds?.[0]);
                  }
                  if (selectedChatIds?.[1]) {
                    updateSxSVariablesMutation.mutate(selectedChatIds?.[1]);
                  }
                  updateVariables();
                } else {
                  if (selectedChatIds?.[0]) {
                    updateVariablesMutation.mutate();
                  } else {
                    updateVariables();
                  }
                }
                closeModal();
              }
            }}
            loading={
              updateVariablesMutation.isPending ||
              updateSxSVariablesMutation.isPending
            }
            disabled={!localVariables || !!jsonError}
          >
            Update
          </Button>
        </Group>
      </Stack>
    </Modal>
  );
}

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
  clearAllResultsQuery,
  clearResultsQuery,
} from "@/queries/clientQueries";
import { ClearResultsPayload } from "@/types";
import { Button, Group, Modal, Text, UnstyledButton } from "@mantine/core";
import {
  QueryObserverResult,
  RefetchOptions,
  useMutation,
} from "@tanstack/react-query";
import { Dispatch, SetStateAction, useCallback } from "react";

import { Workbook, WorkbookItem } from "../types";

type ClearResultsModalProps = {
  isOpened: boolean;
  onClose: () => void;
  chatTurnIds: string[];
  setRowSelection?: Dispatch<SetStateAction<{}>>;
  refetchProject?: (
    options?: RefetchOptions,
  ) => Promise<QueryObserverResult<Workbook | null, Error>>;
  projectId?: string;
  setActiveRows?: Dispatch<SetStateAction<WorkbookItem[]>>;
  selectAllRowsInProject?: boolean;
};

export default function ClearResultsModal({
  isOpened,
  onClose,
  setRowSelection,
  setActiveRows,
  chatTurnIds,
  refetchProject,
  projectId,
  selectAllRowsInProject = false,
}: ClearResultsModalProps) {
  const { defaultProjectId } = useProjectsContext();
  const finalProjectId = projectId || defaultProjectId;

  const onModalClose = useCallback(() => {
    setRowSelection && setRowSelection({});
    setActiveRows && setActiveRows([]);
    onClose();
  }, [setRowSelection, setActiveRows, onClose]);

  const clearAllResultsMutation = useMutation({
    mutationFn: () => clearAllResultsQuery(finalProjectId),
    onSuccess: () => {
      refetchProject && refetchProject();
    },
  });

  const clearSpecificResultsMutation = useMutation({
    mutationFn: (data: ClearResultsPayload) =>
      clearResultsQuery(data, finalProjectId),
    onSuccess: () => {
      refetchProject && refetchProject();
    },
  });

  const onClearResults = useCallback(() => {
    onModalClose();

    if (selectAllRowsInProject) {
      clearAllResultsMutation.mutate();
    } else {
      clearSpecificResultsMutation.mutate({
        chat_turn_ids: chatTurnIds,
      });
    }
  }, [
    clearAllResultsMutation,
    clearSpecificResultsMutation,
    chatTurnIds,
    selectAllRowsInProject,
    finalProjectId,
    onModalClose,
  ]);

  return (
    <Modal
      opened={isOpened}
      onClose={onModalClose}
      size="470px"
      title={<Text className="!font-medium text-title-22">Clear results?</Text>}
      centered
      padding="24px"
      classNames={{
        body: "h-[200px] flex flex-col justify-between",
        header: " p-[24px]",
      }}
    >
      <Text className="text-secondaryDark text-body-14">
        This will delete all generated results such as model outputs,
        performance metrics, and evaluation results. Input and expected output
        will remain.
        {selectAllRowsInProject && (
          <strong> This will clear results for all rows in the project.</strong>
        )}
      </Text>
      <Group className="flex justify-between">
        <UnstyledButton
          variant="transparent"
          className="items-center text-secondary text-title-14"
          onClick={onModalClose}
        >
          Cancel
        </UnstyledButton>
        <Button variant="defaultDisabled" onClick={onClearResults}>
          Clear
        </Button>
      </Group>
    </Modal>
  );
}

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
import { REPORT_LEGAL_ISSUE, TOOLTIPS } from "@/config/constants";
import { getSuccessNotificationConfig } from "@/config/notifications";
import {
  generateSxSInferenceQuery,
  inferenceChatCompletionQuery,
  updateExpectedOutput,
} from "@/queries/clientQueries";
import { SxSInferencePayload } from "@/queries/types";
import { InferenceChatCompletionPromptRole, Provider } from "@/types";
import { buildSxSRowData } from "@/utils/buildSxSRowData";
import { ActionIcon, Group, Menu, Text, Tooltip } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { notifications } from "@mantine/notifications";
import { UseMutationResult, useMutation } from "@tanstack/react-query";
import { MRT_ColumnDef, MRT_VisibilityState } from "mantine-react-table";
import { Dispatch, SetStateAction } from "react";

import { useProjectContext } from "../../hooks/useProjectContext";
import { SXSRow, WorkbookItem } from "../types";
import WorkbookDeleteModal from "./WorkbookDeleteModal";

type WorkbookActionMenuProps = {
  onRowDelete: UseMutationResult<unknown, Error, string[], unknown>;
  initialColumnVisibility?: MRT_VisibilityState | undefined;
  initialColumnOrder?: MRT_ColumnDef<WorkbookItem>[];
  selectedRow: WorkbookItem;
  projectId: string;
  setRowSelection: Dispatch<SetStateAction<{}>>;
  setAddedRowsCount: Dispatch<SetStateAction<number>>;
  onAddTags: (id: string) => void;
  onRemoveTags: (id: string) => void;
  onClearResults: () => void;
  onDuplicateSuccess: (res: WorkbookItem) => void;
};

export default function WorkbookActionMenu({
  initialColumnVisibility,
  onRowDelete,
  initialColumnOrder,
  setRowSelection,
  setAddedRowsCount,
  selectedRow,
  projectId,
  onAddTags,
  onRemoveTags,
  onClearResults,
  onDuplicateSuccess,
}: WorkbookActionMenuProps) {
  const [
    isWorkbookDeleteModalOpen,
    { open: openWorkbookDeleteModal, close: closeWorkbookDeleteModal },
  ] = useDisclosure(false);
  const { isSideBySide, projectType } = useProjectContext();

  const updateExpectedOutputMutation = useMutation({
    mutationFn: (params: {
      chatTurnId: string;
      value: string;
      row: WorkbookItem;
    }) => updateExpectedOutput(params.chatTurnId, params.value),
    onSuccess: (_, params) => {
      onDuplicateSuccess(params.row);
      setRowSelection({});
      notifications.show(
        getSuccessNotificationConfig(
          "Successfully duplicated the row.",
          "success-duplicate-row",
        ),
      );
    },
  });

  const inferenceChatCompletionMutation = useMutation({
    mutationFn: () =>
      inferenceChatCompletionQuery(
        {
          model_id: selectedRow.model_id,
          prompts: [
            {
              role: InferenceChatCompletionPromptRole.USER,
              text: selectedRow.input,
            },
            ...(selectedRow.system_instructions
              ? [
                  {
                    role: InferenceChatCompletionPromptRole.SYSTEM,
                    text: selectedRow.system_instructions,
                  },
                ]
              : []),
          ],
        },
        projectId,
      ),
    onSuccess: (res) => {
      updateExpectedOutputMutation.mutate({
        chatTurnId: res.chat_turn_id,
        value: selectedRow.expected_output || "",
        row: {
          ...selectedRow,
          ...res,
          output: res.model_output?.text,
          isSubRow: false,
          subRows: [],
        } as any,
      });
    },
  });

  const generateSxSInferenceMutation = useMutation({
    mutationFn: (payload: SxSInferencePayload) =>
      generateSxSInferenceQuery(projectId, payload),
    onSuccess: (res: SXSRow) => {
      setRowSelection({});
      onDuplicateSuccess({
        ...buildSxSRowData(res),
      } as WorkbookItem);
      notifications.show(
        getSuccessNotificationConfig(
          "Successfully duplicated the row.",
          "success-duplicate-row",
        ),
      );
    },
  });

  const isSubRow = Boolean((selectedRow as any).isSubRow);
  const isGoogleProvider = selectedRow.model_provider === Provider.GOOGLE;

  return (
    <>
      <WorkbookDeleteModal
        isOpened={isWorkbookDeleteModalOpen}
        selectedRows={[selectedRow]}
        setRowSelection={setRowSelection}
        onRowDelete={onRowDelete}
        initialColumnOrder={initialColumnOrder}
        initialColumnVisibility={initialColumnVisibility}
        onClose={closeWorkbookDeleteModal}
        projectType={projectType}
        setAddedRowsCount={setAddedRowsCount}
      />

      <Menu
        shadow="md"
        position="bottom-end"
        classNames={{ dropdown: "rounded-lg", item: "p-[16px]" }}
      >
        <Menu.Target>
          <ActionIcon size={24} variant="transparent" color="secondary">
            <MaterialIcon
              name="more_vert"
              size={24}
              className="text-secondary"
            />
          </ActionIcon>
        </Menu.Target>

        <Menu.Dropdown>
          {[
            ...(!isSubRow
              ? [
                  {
                    icon: "delete",
                    label: "Delete",
                    onClick: () => openWorkbookDeleteModal(),
                  },
                ]
              : []),
            {
              icon: "label",
              label: "Add tags",
              onClick: () => onAddTags(selectedRow.chat_id),
            },
            {
              icon: "label",
              label: "Remove tags",
              onClick: () => onRemoveTags(selectedRow.chat_id),
            },
            {
              icon: "copy_all",
              label: "Duplicate",
              isDisabled: !selectedRow.model_id,
              tooltip: !selectedRow.model_id
                ? TOOLTIPS.NO_MODEL_SELECTED_MESSAGE
                : undefined,
              onClick: () => {
                notifications.show(
                  getSuccessNotificationConfig(
                    "Duplicating the row, please wait.",
                    "duplicating-row",
                  ),
                );

                if (isSideBySide) {
                  generateSxSInferenceMutation.mutate({
                    model_id_a: selectedRow.model_id,
                    model_id_b: selectedRow?.chat_turn_b?.model_id,
                    prompts: [
                      {
                        role: InferenceChatCompletionPromptRole.USER,
                        text: selectedRow.input,
                      },
                    ],
                    expected_output: selectedRow.expected_output || "",
                    variables: selectedRow?.variables
                      ? Object.fromEntries(
                          Object.entries(selectedRow.variables).map(
                            ([key, value]) => [key, value.value],
                          ),
                        )
                      : {},
                  });
                } else {
                  inferenceChatCompletionMutation.mutate();
                }
              },
            },
            ...(isGoogleProvider
              ? [
                  {
                    icon: "flag",
                    label: "Report legal issue",
                    onClick: () => {
                      window.open(
                        REPORT_LEGAL_ISSUE,
                        "_blank",
                        "noopener,noreferrer",
                      );
                    },
                  },
                ]
              : []),
            {
              icon: "clear",
              label: "Clear results",
              onClick: () => onClearResults(),
            },
          ].map((item, index) =>
            item.isDisabled && item.tooltip ? (
              <Tooltip
                key={index}
                label={item.tooltip}
                position="left"
                withArrow
              >
                <div>
                  <Menu.Item onClick={item.onClick} disabled={item.isDisabled}>
                    <Group>
                      <MaterialIcon
                        className="text-secondary"
                        size={20}
                        name={item.icon}
                      />
                      <Text>{item.label}</Text>
                    </Group>
                  </Menu.Item>
                </div>
              </Tooltip>
            ) : (
              <Menu.Item
                key={index}
                onClick={item.onClick}
                disabled={item.isDisabled}
              >
                <Group>
                  <MaterialIcon
                    className="text-secondary"
                    size={20}
                    name={item.icon}
                  />
                  <Text>{item.label}</Text>
                </Group>
              </Menu.Item>
            ),
          )}
        </Menu.Dropdown>
      </Menu>
    </>
  );
}

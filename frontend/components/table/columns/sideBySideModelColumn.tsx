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

import EvaluatorStatusIndicator from "@/app/(authRoutes)/projects/[id]/components/EvaluatorStatusIndicator";
import {
  InferenceStatus,
  WorkbookItem,
} from "@/app/(authRoutes)/projects/[id]/types";
import EditableTableField from "@/components/EditableTableField";
import MaterialIcon from "@/components/MaterialIcon";
import TruncatedTextWithPopover from "@/components/TruncatedTextWithPopover";
import { useModelsContext } from "@/hooks/useModelsContext";
import { Group, Text, Tooltip, UnstyledButton } from "@mantine/core";
import { MRT_ColumnDef } from "mantine-react-table";
import { useCallback } from "react";

export function sideBySideModelColumn(
  label: string,
  tooltip?: string,
): MRT_ColumnDef<WorkbookItem> {
  return {
    accessorKey: "side_by_side_model_" + label,
    header: "Side by side models",
    Header: () => {
      const TextContent = (
        <Group gap="8px">
          <div className="bg-lightBlue w-[20px] rounded-[3.33px] text-center">
            <span className="cursor-pointer py-[8px] !font-medium uppercase text-brand text-title-11">
              {label}
            </span>
          </div>
          <Text className="cursor-pointer py-[8px] !font-medium uppercase text-secondary text-title-11">
            MODEL NICKNAME
          </Text>
        </Group>
      );

      return tooltip ? (
        <Tooltip label={tooltip} className="max-w-[250px]" multiline>
          {TextContent}
        </Tooltip>
      ) : (
        TextContent
      );
    },
    Cell: (props: any) => {
      const originalRow =
        label === "A"
          ? props.row.original.chat_turn_a
          : props.row.original.chat_turn_b;
      const { allModels } = useModelsContext();
      const model =
        allModels?.find((model) => model.name === originalRow?.model_name) ||
        null;
      const allowEdit = !originalRow?.output;
      const value = originalRow?.output;
      const status = originalRow?.inference_status;

      const ModelOutput = useCallback(() => {
        if (
          status !== undefined &&
          status !== null &&
          status !== InferenceStatus.SUCCESSFUL
        ) {
          return (
            <Group className="flex w-full gap-md items-center">
              <Group className="gap-0">
                <EvaluatorStatusIndicator
                  status={status.toString()}
                  toolTipText={
                    status === InferenceStatus.FAILED
                      ? originalRow?.inference_reason
                      : null
                  }
                  chipGroupClassName="!py-0"
                  chipIconSize={14}
                />
              </Group>

              {status === InferenceStatus.FAILED &&
                props.table.options.meta?.onOutputRerun && (
                  <UnstyledButton
                    className="flex h-[20px] w-[20px] items-center justify-center rounded-sm p-[4px] border-default"
                    onClick={() => {
                      props.table.options.meta?.onOutputRerun(originalRow);
                    }}
                  >
                    <MaterialIcon
                      name="refresh"
                      size={14}
                      className="text-secondary"
                    />
                  </UnstyledButton>
                )}
            </Group>
          );
        }

        if (!value) return "";

        return (
          <TruncatedTextWithPopover text={String(value)} popoverWidth={500} />
        );
      }, [status, value]);

      return (
        <Group className="flex-col items-start justify-center gap-0 truncate p-0">
          <Group className="border-b-default h-[40px] w-full gap-0 border-0 px-[12px]">
            {model ? (
              <Group className="gap-md w-[90%] flex-nowrap">
                {model?.icon && <model.icon size={16} />}
                <Text className="gap-0 truncate text-secondary text-body-12">
                  {model.label}
                </Text>
              </Group>
            ) : (
              <Text className="gap-0 truncate text-body-11 text-resting">
                No model selected
              </Text>
            )}
          </Group>
          <Group
            h={56}
            className="border-b-default gap-sm border-0 pt-2 pb-3 px-3 w-full"
          >
            <Text className="!font-medium text-resting text-body-11">
              System Instruction
            </Text>

            <Group
              gap="6px"
              className="box-content flex w-[100%] flex-row flex-nowrap"
            >
              <EditableTableField
                value={originalRow?.system_instructions || ""}
                placeholderText={
                  allowEdit ? "Type system instruction here" : ""
                }
                isLoading={props.row?.original?.isSystemInstructionsLoading}
                onUpdate={(val) => {
                  props.table.options.meta?.onSystemInstructionChange?.(
                    val,
                    originalRow?.system_instructions,
                    originalRow,
                  );
                }}
                allowEdit={allowEdit}
              />
            </Group>
          </Group>
          <Group className="gap-sm h-[56px] px-[12px] pb-[12px] pt-[8px]">
            <Text className="!font-medium text-resting text-body-11">
              Model Output
            </Text>
            <ModelOutput />
          </Group>
        </Group>
      );
    },
  };
}

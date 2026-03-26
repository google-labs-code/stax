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

import ColumnHeader from "@/app/(authRoutes)/projects/[id]/components/ColumnHeader";
import EvaluatorStatusIndicator from "@/app/(authRoutes)/projects/[id]/components/EvaluatorStatusIndicator";
import { CHAT_THUMBS } from "@/app/(authRoutes)/projects/[id]/playground/consts";
import {
  InferenceStatus,
  InferenceTokens,
  WorkbookItem,
} from "@/app/(authRoutes)/projects/[id]/types";
import { TOOLTIPS } from "@/config/constants";
import { getErrorNotificationConfig } from "@/config/notifications";
import { ProjectType, TableName, WorkbookMeta } from "@/types";
import { createTokenAccessorFn } from "@/utils/sortingUtils";
import { Group, Loader, Tooltip, UnstyledButton } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { MRT_ColumnDef } from "mantine-react-table";

import EditableTableField from "../EditableTableField";
import MaterialIcon from "../MaterialIcon";
import TruncatedTextWithPopover from "../TruncatedTextWithPopover";
import SideBySideDefaultColumn from "./columns/SideBySideDefaultColumn";

export const expectedOutputColumn: MRT_ColumnDef<WorkbookItem> = {
  id: "expected_output",
  accessorKey: "output",
  enableSorting: true,
  enableColumnFilter: true,
  accessorFn: (row: WorkbookItem) => {
    if (typeof row.expected_output === "string") return row.expected_output;
    if (row.expected_output === null || row.expected_output === undefined)
      return "";

    return String(row.expected_output);
  },
  header: "Expected output",
  Header: (props) => (
    <ColumnHeader {...props} tooltip={TOOLTIPS.EXPECTED_OUTPUT_COLUMN_HEADER} />
  ),
  Cell: (props) => {
    const value = props.row.original.expected_output || "";
    const tableMeta = props?.table?.options?.meta as WorkbookMeta;

    return (
      <EditableTableField
        value={value}
        placeholderText="Type expected output here"
        isEnabled={!!props.row.original.input}
        isLoading={props.row?.original?.isExpectedOutputLoading}
        onUpdate={(val) => {
          tableMeta?.onExpectedOutputChange?.(val, value, props.row.original);
        }}
        allowEdit
      />
    );
  },
  filterVariant: "text" as const,
};

export const systemInstructionsColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "system_instructions",
  id: "system_instructions",
  enableSorting: true,
  enableColumnFilter: true,
  accessorFn: (row: WorkbookItem) => {
    if (typeof row.system_instructions === "string")
      return row.system_instructions;
    if (
      row.system_instructions === null ||
      row.system_instructions === undefined
    )
      return "";

    return String(row.system_instructions);
  },
  header: "System instructions",
  Header: (props) => (
    <ColumnHeader
      {...props}
      tooltip={TOOLTIPS.SYSTEM_INSTRUCTION_COLUMN_HEADER}
    />
  ),
  Cell: (props) => {
    const value = props.row.original.system_instructions || "";
    // Allow editing system instructions, only if there is no output
    const allowEdit = !props.row?.original?.output;
    const tableMeta = props?.table?.options?.meta as WorkbookMeta;

    return (
      <Group
        gap="6px"
        className="box-content flex w-[100%] flex-row flex-nowrap"
      >
        <EditableTableField
          value={value}
          placeholderText={allowEdit ? "Type system instruction here" : ""}
          isLoading={props.row?.original?.isSystemInstructionsLoading}
          onUpdate={(val) => {
            tableMeta?.onSystemInstructionChange?.(
              val,
              value,
              props.row.original,
            );
          }}
          allowEdit={allowEdit}
        />
      </Group>
    );
  },
  filterVariant: "text" as const,
};

export const inputDatasetColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "input",
  id: "input",
  accessorFn: (row: WorkbookItem) => {
    if (typeof row.input === "string") return row.input;
    if (row.input === null || row.input === undefined) return "";

    return String(row.input);
  },
  header: "Input",
  Cell: (props) => {
    const value = props.row.original.input;
    const tableMeta = props.table.options.meta as WorkbookMeta;

    return (
      <EditableTableField
        value={value}
        placeholderText="Type input here"
        isLoading={props.row?.original?.isInputLoading}
        onUpdate={(val) => {
          tableMeta?.onInputChange?.(val, "", props.row.original);
        }}
        textClassName={`!text-secondaryDark ${
          props.row?.original?.isSubRow && "pl-[10px]"
        }`}
        allowEdit={tableMeta?.tableName === TableName.DATASET}
      />
    );
  },
  Header: (props) => (
    <ColumnHeader {...props} tooltip={TOOLTIPS.INPUT_COLUMN_HEADER} />
  ),
  filterVariant: "text" as const,
};

export const inputWorkbookColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "input",
  id: "input",
  enableSorting: true,
  enableColumnFilter: true,
  accessorFn: (row: WorkbookItem) => {
    if (typeof row.input === "string") return row.input;
    if (row.input === null || row.input === undefined) return "";

    return String(row.input);
  },
  header: "Input",
  Cell: (props) => {
    const value = props.row.original.input;
    const isExpanded = props.row?.getIsExpanded();
    const hasArrow =
      props.row?.original?.is_chat && !props.row?.original?.isSubRow;
    const tableMeta = props.table.options.meta as WorkbookMeta;
    const isSubRowsLoading = tableMeta?.loadingRows?.has(
      props.row?.original?.chat_id,
    );

    // Allow editing input, only if output does not exist.
    const isEditingInputEnabled = !props.row.original?.output;

    return (
      <Group className="flex w-full flex-row flex-nowrap items-center gap-0">
        <Group className="flex w-full flex-row flex-nowrap">
          {props.row?.original?.isSubRow && (
            <MaterialIcon
              name="prompt_suggestion"
              size={20}
              className="ml-[15px] text-resting"
            />
          )}
          <EditableTableField
            value={value || ""}
            placeholderText={isEditingInputEnabled ? "Type input here" : ""}
            isLoading={props.row?.original?.isInputLoading}
            onUpdate={(val) => {
              tableMeta?.onInputChange?.(val, value, props.row.original);
            }}
            textClassName={`!text-secondaryDark ${hasArrow && "pr-[15px]"}`}
            hasHoverPlaygroundButton={true}
            rowOriginal={props.row?.original}
            allowEdit={isEditingInputEnabled}
          />
        </Group>

        {hasArrow && (
          <UnstyledButton
            onClick={(e) => {
              e.stopPropagation();
              tableMeta?.onExpandClick?.(props.row);
            }}
            className="ml-[-20px] flex h-[20px] w-[20px] items-center justify-center rounded-full hover:bg-gray-100"
            title={isExpanded ? "Collapse row" : "Expand row"}
          >
            {isSubRowsLoading ? (
              <Loader size="xs" />
            ) : (
              <MaterialIcon
                name={isExpanded ? "arrow_drop_up" : "arrow_drop_down"}
                size={20}
                className="text-resting"
              />
            )}
          </UnstyledButton>
        )}
      </Group>
    );
  },
  Header: (props) => (
    <ColumnHeader {...props} tooltip={TOOLTIPS.INPUT_COLUMN_HEADER} />
  ),
  filterVariant: "text" as const,
};

export const outputColumn: MRT_ColumnDef<WorkbookItem> = {
  id: "output",
  accessorKey: "output",
  enableSorting: true,
  enableColumnFilter: true,
  accessorFn: (row: WorkbookItem) => {
    if (typeof row.output === "string") return row.output;
    if (row.output === null || row.output === undefined) return "";

    return String(row.output);
  },
  header: "Output",
  Cell: (props) => {
    const value = props.row.original.output;
    const status = props.row.original.inference_status;
    const tableMeta = props?.table?.options?.meta as WorkbookMeta;

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
                  ? props.row.original.inference_reason
                  : null
              }
            />
          </Group>

          {status === InferenceStatus.FAILED && tableMeta?.onOutputRerun && (
            <UnstyledButton
              className="flex h-[24px] w-[24px] items-center justify-center rounded-sm p-[4px] border-default"
              onClick={() => {
                tableMeta?.onOutputRerun &&
                  tableMeta.onOutputRerun(props.row.original);
              }}
            >
              <MaterialIcon
                name="refresh"
                size={16}
                className="text-secondary"
              />
            </UnstyledButton>
          )}
        </Group>
      );
    }

    if (!value) return "";

    return <TruncatedTextWithPopover text={String(value)} popoverWidth={500} />;
  },
  Header: (props) => (
    <ColumnHeader {...props} tooltip={TOOLTIPS.OUTPUT_COLUMN_HEADER} />
  ),
  filterVariant: "text" as const,
};

export const outputTokensColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "inference_tokens",
  id: "output_tokens",
  size: 140,
  enableSorting: true,
  enableColumnFilter: true,
  accessorFn: createTokenAccessorFn("output_tokens"),
  header: "Output Tokens",
  Cell: (props) => {
    const originalRow = props.row.original;
    const value = originalRow.inference_tokens as InferenceTokens;
    const status = originalRow.inference_status;
    const output = originalRow.output;
    const tableMeta = props.table.options.meta as WorkbookMeta;

    // Don't show token values if inference wasn't successful or if there's no output
    if (!value || status !== InferenceStatus.SUCCESSFUL || !output) return "";

    const isSideBySide = tableMeta?.projectType === ProjectType.SIDE_BY_SIDE;

    if (isSideBySide) {
      const valueA = originalRow?.chat_turn_a?.inference_tokens?.output_tokens;
      const valueB = originalRow?.chat_turn_b?.inference_tokens?.output_tokens;
      const difference = valueB ? valueA - valueB : null;

      return (
        <SideBySideDefaultColumn
          valueA={valueA ? valueA.toString() : ""}
          valueB={valueB ? valueB.toString() : ""}
          difference={difference ? difference.toString() : null}
        />
      );
    }

    return (
      <div className="text-body-12 text-secondary">{value.output_tokens}</div>
    );
  },
  Header: (props) => (
    <ColumnHeader {...props} tooltip={TOOLTIPS.TOKENS_COLUMN_HEADER} />
  ),
  filterVariant: "text" as const,
};

export const totalTokensColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "inference_tokens",
  id: "total_tokens",
  size: 130,
  enableSorting: true,
  enableColumnFilter: true,
  accessorFn: createTokenAccessorFn("total_tokens"),
  header: "Total Tokens",
  Cell: (props) => {
    const originalRow = props.row.original;
    const value = originalRow.inference_tokens as InferenceTokens;
    const status = originalRow.inference_status;
    const output = originalRow.output;
    const tableMeta = props.table.options.meta as WorkbookMeta;

    // Don't show token values if inference wasn't successful or if there's no output
    if (!value || status !== InferenceStatus.SUCCESSFUL || !output) return "";

    const isSideBySide = tableMeta?.projectType === ProjectType.SIDE_BY_SIDE;

    if (isSideBySide) {
      const valueA = originalRow?.chat_turn_a?.inference_tokens?.total_tokens;
      const valueB = originalRow?.chat_turn_b?.inference_tokens?.total_tokens;
      const difference = valueB ? valueA - valueB : null;

      return (
        <SideBySideDefaultColumn
          valueA={valueA ? valueA.toString() : ""}
          valueB={valueB ? valueB.toString() : ""}
          difference={difference ? difference.toString() : null}
        />
      );
    }

    return (
      <div className="text-body-12 text-secondary">{value.total_tokens}</div>
    );
  },
  Header: (props) => (
    <ColumnHeader {...props} tooltip={TOOLTIPS.TOKENS_COLUMN_HEADER} />
  ),
  filterVariant: "text" as const,
};

export const humanEvaluationColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "human_evaluation",
  header: "Human evaluation",
  minSize: 50,
  size: 120,
  enableSorting: true,
  enableColumnFilter: true,
  filterVariant: "text",
  Header: (props) => (
    <ColumnHeader
      {...props}
      tooltip={TOOLTIPS.HUMAN_EVALUATION_COLUMN_HEADER}
    />
  ),
  Cell: (props) => {
    const value = props.row.original.human_eval_scores?.[0]?.score;
    const isHumanEvalDisabled = !props.row?.original?.output;
    const tableMeta = props.table.options.meta as WorkbookMeta;

    return (
      <Group
        gap="6px"
        className="box-content flex w-[100%] flex-row flex-nowrap"
      >
        {CHAT_THUMBS.map((thumb) => (
          <Tooltip
            label={
              isHumanEvalDisabled
                ? "Human evaluation is disabled due to missing output"
                : thumb.tooltipLabel
            }
            position="bottom"
            key={thumb.name}
          >
            <UnstyledButton
              disabled={isHumanEvalDisabled}
              onClick={() => {
                tableMeta?.onHumanEvalScoreChange?.(
                  thumb.value,
                  value,
                  props.row.original,
                );
              }}
              className={`border-default flex w-[50%] bg-white text-disabled items-center justify-center rounded-sm p-[4px] transition-all duration-200 ${
                value === thumb.value && thumb.activeStyle
              } ${isHumanEvalDisabled ? "cursor-not-allowed" : "hover:bg-veryLightSilver hover:text-secondary "}`}
            >
              <MaterialIcon name={thumb.icon} size={20} />
            </UnstyledButton>
          </Tooltip>
        ))}
      </Group>
    );
  },
};

export const humanEvaluationNotesColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "human_evaluation_notes",
  header: "Human Eval Notes",
  minSize: 50,
  size: 120,
  enableSorting: true,
  enableColumnFilter: true,
  Header: (props) => <ColumnHeader {...props} />,
  Cell: (props) => {
    const value = props.row.original.human_eval_scores?.[0]?.notes;
    const tableMeta = props.table.options.meta as WorkbookMeta;

    // Allow editing human evaluation notes, only if there is output.
    const allowEditing = !!props.row?.original?.output;

    return (
      <Group
        gap="6px"
        className="box-content flex w-[100%] flex-row flex-nowrap"
      >
        <EditableTableField
          value={value || ""}
          placeholderText={
            allowEditing ? "Type human evaluation notes here" : ""
          }
          isLoading={props.row?.original?.isHumanEvalNotesLoading}
          onUpdate={(val) => {
            tableMeta?.onHumanEvalNotesChange?.(val, value, props.row.original);
          }}
          allowEdit={allowEditing}
        />
      </Group>
    );
  },
};

export const variablesColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "variables",
  id: "variables",
  enableSorting: true,
  enableColumnFilter: true,
  accessorFn: (row: WorkbookItem) => {
    if (!row.variables) return "";
    if (typeof row.variables === "string") return row.variables;

    return JSON.stringify(row.variables);
  },
  header: "Variables",
  Cell: (props) => {
    const value = props.row.original?.variables || {};
    const tableMeta = props.table.options.meta as WorkbookMeta;

    return (
      <EditableTableField
        value={
          value && Object.keys(value).length > 0 ? JSON.stringify(value) : ""
        }
        placeholderText="Manage variables here"
        isEnabled={true}
        allowEdit={true}
        isVariableColumn
        onUpdate={(val) => {
          let parsedVal: any = null;
          const isValEmpty = !/[^\s]/.test(val);
          if (!isValEmpty) {
            try {
              parsedVal = val ? JSON.parse(val) : null;
            } catch {
              parsedVal = null;
              notifications.show(
                getErrorNotificationConfig(
                  "Failed to save variables, due to invalid format. Please try again.",
                  "update-variables-error",
                ),
              );
            }
          }
          tableMeta?.onUpdateChatVariables?.(
            isValEmpty
              ? {}
              : parsedVal && Object.keys(parsedVal).length > 0
                ? parsedVal
                : value,
            value && typeof value === "object" ? value : JSON.parse(value),
            props.row.original,
          );
        }}
      />
    );
  },
  Header: (props) => (
    <ColumnHeader {...props} tooltip={TOOLTIPS.VARIABLES_COLUMN_HEADER} />
  ),
  filterVariant: "text" as const,
};

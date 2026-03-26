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
import { SIDE_BY_SIDE_EVAL_OPTIONS } from "@/app/(authRoutes)/projects/[id]/playground/consts";
import { WorkbookItem } from "@/app/(authRoutes)/projects/[id]/types";
import EditableTableField from "@/components/EditableTableField";
import MaterialIcon from "@/components/MaterialIcon";
import { WorkbookMeta } from "@/types";
import { Group, Tooltip, UnstyledButton } from "@mantine/core";
import { MRT_ColumnDef } from "mantine-react-table";

const toSentenceCase = (text: string) => {
  return text.toLowerCase().replace(/^\w/, (c) => c.toUpperCase()) + ".";
};

const hasRequiredOutputs = (rowOriginal: any): boolean => {
  const isSubRow = !!rowOriginal.isSubRow;

  if (isSubRow) {
    return (
      !!rowOriginal.chat_turn_a?.output && !!rowOriginal.chat_turn_b?.output
    );
  } else {
    return (
      !!rowOriginal.output &&
      !!rowOriginal.chat_turn_a?.output &&
      !!rowOriginal.chat_turn_b?.output
    );
  }
};

export const sideBySideEvalColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "side_by_side_eval",
  header: "Side by side evals",
  Header: () => <MaterialIcon name="compare_arrows" size={20} />,
  Cell: (props) => {
    const isHumanEvalDisabled = !hasRequiredOutputs(props.row.original);
    const baseClassName = `transition-all duration-200 p-[4px] border-default h-[28px] rounded-sm flex flex-row justify-center items-center text-disabled ${isHumanEvalDisabled ? "cursor-not-allowed" : "hover:bg-veryLightSilver hover:text-secondary"}`;

    return (
      <Group className="gap-md flex-col justify-center">
        {SIDE_BY_SIDE_EVAL_OPTIONS.map((thumb, index) => {
          const isActive = thumb.value === props.row.original.human_sxs_rating;
          const tableMeta = props.table.options.meta as WorkbookMeta;

          return (
            <Tooltip
              key={index}
              label={toSentenceCase(thumb.name)}
              position="bottom"
              withArrow
              disabled={isHumanEvalDisabled}
            >
              <UnstyledButton
                disabled={isHumanEvalDisabled}
                className={`${baseClassName} ${isActive ? thumb.activeStyle : ""}`}
                onClick={() => {
                  tableMeta?.onHumanEvalSxSRatingChange?.(
                    thumb.value,
                    props.row.original,
                  );
                }}
              >
                <MaterialIcon name={thumb.icon} size={16} />
              </UnstyledButton>
            </Tooltip>
          );
        })}
      </Group>
    );
  },
};

export const sideBySideEvalNotesColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "side_by_side_eval_notes",
  header: "Human Eval Notes",
  minSize: 50,
  size: 120,
  enableSorting: true,
  enableColumnFilter: true,
  Header: (props) => <ColumnHeader {...props} />,
  Cell: (props) => {
    const value = props.row.original?.human_sxs_notes || "";
    const allowEditing = hasRequiredOutputs(props.row.original);
    const tableMeta = props.table.options.meta as WorkbookMeta;

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
            tableMeta?.onHumanEvalSxSNotesChange?.(
              val,
              value,
              props.row.original,
            );
          }}
          allowEdit={allowEditing}
        />
      </Group>
    );
  },
};

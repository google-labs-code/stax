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
import {
  InferenceStatus,
  WorkbookItem,
} from "@/app/(authRoutes)/projects/[id]/types";
import { TOOLTIPS } from "@/config/constants";
import { ProjectType } from "@/types";
import { convertMsToS } from "@/utils/helpers";
import { Text } from "@mantine/core";
import { MRT_ColumnDef } from "mantine-react-table";

import SideBySideDefaultColumn from "./SideBySideDefaultColumn";

export const inferenceLatencyColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "inference_latency",
  header: "Latency",
  enableEditing: false,
  enableSorting: true,
  enableColumnFilter: true,
  size: 100,
  Header: (props) => (
    <ColumnHeader {...props} tooltip={TOOLTIPS.LATENCY_COLUMN_HEADER} />
  ),
  Cell: (props) => {
    const originalRow = props.row.original;
    const value = originalRow.inference_latency;
    const status = originalRow.inference_status;
    const output = originalRow.output;
    const isSideBySide =
      props.table.options.meta?.projectType === ProjectType.SIDE_BY_SIDE;

    if (isSideBySide) {
      const valueA = originalRow?.chat_turn_a?.inference_latency;
      const valueB = originalRow?.chat_turn_b?.inference_latency;
      const difference = valueB ? valueA - valueB : null;

      return (
        <SideBySideDefaultColumn
          valueA={valueA?.toString() || ""}
          valueB={valueB?.toString() || ""}
          difference={difference ? convertMsToS(difference) : null}
        />
      );
    }

    // Don't show latency if inference wasn't successful, if there's no output, or if value is <= 0
    if (value <= 0 || status !== InferenceStatus.SUCCESSFUL || !output)
      return null;

    return (
      value && (
        <Text className="text-secondary text-body-12">
          {convertMsToS(value)}
        </Text>
      )
    );
  },
};

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

import { additionalColumns } from "@/components/table/columns/additionalColumns";
import { createdAtColumn } from "@/components/table/columns/createdAtColumn";
import { inferenceLatencyColumn } from "@/components/table/columns/inferenceLatencyColumn";
import { modelColumn } from "@/components/table/columns/modelColumn";
import {
  sideBySideEvalColumn,
  sideBySideEvalNotesColumn,
} from "@/components/table/columns/sideBySideEvalColumn";
import { sideBySideModelColumn } from "@/components/table/columns/sideBySideModelColumn";
import { tagsColumn } from "@/components/table/columns/tagsColumn";
import {
  expectedOutputColumn,
  humanEvaluationColumn,
  humanEvaluationNotesColumn,
  inputWorkbookColumn,
  outputColumn,
  outputTokensColumn,
  systemInstructionsColumn,
  totalTokensColumn,
  variablesColumn,
} from "@/components/table/tableColumns";
import { TagRaw } from "@/types";
import { MRT_ColumnDef } from "mantine-react-table";

import { WorkbookItem } from "../types";

export const WORKBOOK_TABLE_COLUMNS_POINTWISE: MRT_ColumnDef<WorkbookItem>[] = [
  {
    ...inputWorkbookColumn,
  },
  // { @TODO Hide until BE is integrated
  //   ...inputAttachmentsColumn,
  // },
  {
    ...systemInstructionsColumn,
  },
  {
    ...outputColumn,
  },
  {
    ...expectedOutputColumn,
  },
  {
    ...modelColumn,
  },
  {
    ...humanEvaluationColumn,
  },
  {
    ...humanEvaluationNotesColumn,
  },
  ...additionalColumns,
  {
    ...tagsColumn,
    filterFn: (row, id, filterValue) => {
      const tags = row.getValue(id) as TagRaw[];
      if (!tags || !filterValue) return true;

      return tags.some((tag) =>
        tag.name.toLowerCase().includes(filterValue.toLowerCase()),
      );
    },
  },
  {
    ...outputTokensColumn,
  },
  {
    ...totalTokensColumn,
  },
  {
    ...inferenceLatencyColumn,
  },
  {
    ...variablesColumn,
  },
  {
    ...createdAtColumn,
  },
];

export const WORKBOOK_TABLE_COLUMNS_SIDE_BY_SIDE: MRT_ColumnDef<WorkbookItem>[] =
  [
    {
      ...inputWorkbookColumn,
    },
    // { @TODO Hide until BE is integrated
    //   ...inputAttachmentsColumn,
    // },
    {
      ...expectedOutputColumn,
    },
    {
      ...sideBySideModelColumn("A"),
    },
    {
      ...sideBySideEvalColumn,
    },
    {
      ...sideBySideModelColumn("B"),
    },
    {
      ...sideBySideEvalNotesColumn,
    },
    ...additionalColumns,
    {
      ...tagsColumn,
      filterFn: (row, id, filterValue) => {
        const tags = row.getValue(id) as TagRaw[];
        if (!tags || !filterValue) return true;

        return tags.some((tag) =>
          tag.name.toLowerCase().includes(filterValue.toLowerCase()),
        );
      },
    },
    {
      ...inferenceLatencyColumn,
      enableSorting: true,
      enableColumnFilter: true,
    },
    {
      ...outputTokensColumn,
      enableSorting: true,
      enableColumnFilter: true,
    },
    {
      ...totalTokensColumn,
      enableSorting: true,
      enableColumnFilter: true,
    },
    {
      ...variablesColumn,
    },
    {
      ...createdAtColumn,
    },
  ];

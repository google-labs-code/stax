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
import { WorkbookItem } from "@/app/(authRoutes)/projects/[id]/types";
import FormatDate from "@/components/FormatDate";
import dayjs from "@/utils/dayjsSetup";
import { MRT_ColumnDef } from "mantine-react-table";

export const createdAtColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "created_at",
  header: "Date added",
  enableEditing: false,
  enableSorting: true,
  enableColumnFilter: true,
  Header: (props) => <ColumnHeader filterType="date-range" {...props} />,
  Cell: (props) => {
    const value = props.renderedCellValue;

    return (
      value && (
        <FormatDate
          className="text-secondary text-body-12"
          date={value as number}
        />
      )
    );
  },
  filterFn: (row: any, id: any, filterValue: any) => {
    if (filterValue.length === 0 || !filterValue[0] || !filterValue[1])
      return true;

    return dayjs(row.getValue(id)).isBetween(
      filterValue[0],
      filterValue[1],
      undefined,
      "[]",
    );
  },
};

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

import hasSubstring from "@/utils/hasSubstring";
import { Text } from "@mantine/core";
import { MRT_Icons, MRT_RowData, MRT_TableOptions } from "mantine-react-table";
import { BsArrowDownUp } from "react-icons/bs";
import { HiSortAscending, HiSortDescending } from "react-icons/hi";
import { PiDotsThreeVerticalBold } from "react-icons/pi";

const cellHeaderIcons: Partial<MRT_Icons> = {
  IconArrowsSort: () => (
    <div className="ml-2">
      <BsArrowDownUp size={10} className="text-secondary" />
    </div>
  ),
  IconSortAscending: () => (
    <div className="ml-2">
      <HiSortAscending size={20} className="text-secondary" />
    </div>
  ),

  IconSortDescending: () => (
    <div className="ml-2">
      <HiSortDescending size={20} className="text-secondary" />
    </div>
  ),
  IconDotsVertical: () => (
    <div className="ml-2">
      <PiDotsThreeVerticalBold size={15} className="text-secondary" />
    </div>
  ),
};

export const getBaseTableConfig = <T extends MRT_RowData>(): Partial<
  MRT_TableOptions<T>
> => {
  return {
    icons: cellHeaderIcons,
    enableColumnOrdering: true,
    enableDensityToggle: false,
    enableStickyHeader: true,
    enableStickyFooter: true,
    enableRowSelection: true,
    enableColumnResizing: true,
    enableClickToCopy: false,
    enableRowActions: true,
    enableColumnDragging: false,
    enableColumnPinning: true,
    layoutMode: "grid",
    positionActionsColumn: "last",
    mantineSelectAllCheckboxProps: { size: "sm" },
    initialState: {
      pagination: { pageSize: 30, pageIndex: 0 },
      columnPinning: {
        right: ["mrt-row-actions"],
      },
    },
    mantineSelectCheckboxProps: {
      className: "h-[20px] w-[20px] !p-0",
      size: "sm",
    },
    mantineFilterTextInputProps: {
      className: "border-b-0 mt-0 p-0 bg-transparent",
    },
    mantineFilterSelectProps: {
      className: "border-b-0 mt-0 p-0 bg-transparent",
    },
    mantineFilterMultiSelectProps: {
      className: "border-b-0 mt-0 p-0 bg-transparent",
    },
    defaultColumn: {
      mantineTableBodyCellProps: {
        classNames: () => {
          return {
            td: "border-0 border-r-[1px] last:border-r-0 border-borderColor border-solid first:px-[20px] px-[12px] py-[10px]",
          };
        },
      },
      Cell: (props) => (
        <Text className="text-secondaryDark text-body-14">
          {props.renderedCellValue}
        </Text>
      ),
      filterFn: (row, columnKey, searchTerm) =>
        hasSubstring(
          row.original[columnKey as keyof typeof row.original] as string,
          searchTerm,
        ),
    },
    mantinePaperProps: {
      className: "border-r rounded-2xl border-borderColor border-solid",
      style: {
        "--mrt-row-hover-background-color": "rgb(var(--color-neutrals-50))",
      },
    },
    mantineTableContainerProps: {
      className: "rounded-sm max-h-[70vh]",
    },
    mantineTableHeadCellProps: {
      classNames: {
        th: "bg-primary text-secondary text-caps-10 uppercase first:px-[20px] pl-[12px] py-[5px] min-h-[32px]",
      },
      style: {
        backgroundColor: "rgb(var(--color-neutrals-100))",
      },
    },
    displayColumnDefOptions: {
      "mrt-row-actions": {
        // minSize: 130,
      },
    },
  };
};

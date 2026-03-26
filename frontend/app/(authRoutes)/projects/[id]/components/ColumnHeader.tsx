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

import DateRangeInput from "@/components/DateRangeInput";
import MaterialIcon from "@/components/MaterialIcon";
import SortZToAIcon from "@/components/icons/SortZToAIcon";
import { Input, Menu, Text, Tooltip } from "@mantine/core";
import { MRT_Column, MRT_TableInstance } from "mantine-react-table";
import { SVGProps } from "react";

import {
  FiltersDisplayed,
  useProjectContext,
} from "../../hooks/useProjectContext";
import { WorkbookItem } from "../types";

type ActionsMenuItemProps = {
  icon:
    | React.ComponentType<SVGProps<SVGSVGElement> & { size?: number }>
    | string;
  onClick: () => void;
  label: string;
  disabled?: boolean;
};

export const renderColumnActionsMenuItems = ({
  column,
  table,
}: {
  column: MRT_Column<WorkbookItem>;
  table: MRT_TableInstance<WorkbookItem>;
}) => {
  const ActionsMenuItem = (props: ActionsMenuItemProps) => {
    const leftSection =
      typeof props.icon === "string" ? (
        <MaterialIcon name={props.icon} size={20} className="text-secondary" />
      ) : (
        <props.icon className="text-secondary material-symbols" />
      );

    return (
      <Menu.Item
        className="flex h-[44px] items-center"
        leftSection={leftSection}
        onClick={props.onClick}
        disabled={props.disabled}
      >
        <span
          className={`self-center text-body-14 ${props.disabled ? "text-gray-400" : "text-secondaryDark"}`}
        >
          {props.label}
        </span>
      </Menu.Item>
    );
  };

  const { projectActions, projectState } = useProjectContext();

  const columnVisibility = table.options.state.columnVisibility;

  const visibleColumns = table.getVisibleLeafColumns();
  const columnIndex = visibleColumns.findIndex((col) => col.id === column.id);

  const isCheckboxColumn =
    column.id === "select" || column.columnDef.id === "select";
  const isActionsColumn =
    column.id === "actions" || column.columnDef.id === "actions";
  const isSpecialColumn =
    isCheckboxColumn || isActionsColumn || column.id === "input";

  const isFirstDataColumn = columnIndex === 1;
  const isLastDataColumn = columnIndex === visibleColumns.length - 2;

  const moveColumnLeft = () => {
    const currentOrderIds = visibleColumns.map((col) => col.id);
    const newOrderIds = [...currentOrderIds];

    const temp = newOrderIds[columnIndex];
    newOrderIds[columnIndex] = newOrderIds[columnIndex - 1];
    newOrderIds[columnIndex - 1] = temp;

    table.setColumnOrder(newOrderIds);
  };

  const moveColumnRight = () => {
    const currentOrderIds = visibleColumns.map((col) => col.id);
    const newOrderIds = [...currentOrderIds];

    const temp = newOrderIds[columnIndex];
    newOrderIds[columnIndex] = newOrderIds[columnIndex + 1];
    newOrderIds[columnIndex + 1] = temp;

    table.setColumnOrder(newOrderIds);
  };

  const showMovementOptions = !isSpecialColumn;

  return (
    <Menu.Dropdown className="!w-[200px] rounded-2xl p-0">
      {column.columnDef?.enableColumnFilter && (
        <ActionsMenuItem
          icon="search"
          label="Search"
          onClick={() =>
            projectActions.setDisplayedFilters({
              ...projectState.displayedFilters,
              [column.id]: true,
            })
          }
        />
      )}

      <ActionsMenuItem
        icon="visibility_off"
        label="Hide column"
        onClick={() => {
          table.setColumnVisibility({
            ...columnVisibility,
            [column.id]: false,
          });
        }}
        disabled={isSpecialColumn}
      />
      {showMovementOptions && (
        <>
          <ActionsMenuItem
            icon="move_selection_left"
            label="Move left"
            onClick={moveColumnLeft}
            disabled={isFirstDataColumn}
          />
          <ActionsMenuItem
            icon="move_selection_right"
            label="Move right"
            onClick={moveColumnRight}
            disabled={isLastDataColumn}
          />
        </>
      )}
      {column.columnDef?.enableSorting ? (
        <>
          <ActionsMenuItem
            icon="sort_by_alpha"
            label="Sort A to Z"
            onClick={() =>
              projectActions.setSorting([{ id: column.id, desc: false }])
            }
          />
          <ActionsMenuItem
            icon={SortZToAIcon}
            label="Sort Z to A"
            onClick={() =>
              projectActions.setSorting([{ id: column.id, desc: true }])
            }
          />
        </>
      ) : null}
    </Menu.Dropdown>
  );
};

export default function ColumnHeader({
  column,
  filterType,
  tooltip,
}: {
  column: MRT_Column<WorkbookItem>;
  filterType?: string;
  tooltip?: string;
}) {
  const TextContent = (
    <Text className="cursor-pointer py-[8px] !font-medium uppercase text-secondary text-title-11">
      {column.columnDef.header}
    </Text>
  );

  if (!useProjectContext()) {
    return tooltip ? (
      <Tooltip label={tooltip} className="max-w-[250px]" multiline>
        {TextContent}
      </Tooltip>
    ) : (
      TextContent
    );
  }

  const { projectState, projectActions } = useProjectContext();
  const isFilterDisplayed =
    projectState.displayedFilters[column.id as keyof FiltersDisplayed];

  const showSearchIcon = column.id === "output";

  const FilterInput =
    filterType === "date-range" ? (
      <DateRangeInput
        onChange={(selectedDates) => {
          column.setFilterValue(selectedDates);
        }}
        hideFilter={() =>
          projectActions.setDisplayedFilters({
            ...projectState.displayedFilters,
            [column.id]: false,
          })
        }
        clearable
      />
    ) : (
      <Input
        placeholder="SEARCH"
        leftSection={
          showSearchIcon ? <MaterialIcon name="search" size={20} /> : null
        }
        value={(column.getFilterValue() as string) || ""}
        onChange={(e) => {
          column.setFilterValue(e.target.value);

          if (e.target.value === "") {
            projectActions.setDisplayedFilters({
              ...projectState.displayedFilters,
              [column.id]: false,
            });
          }
        }}
        onBlur={() => {
          if (!column.getFilterValue()) {
            projectActions.setDisplayedFilters({
              ...projectState.displayedFilters,
              [column.id]: false,
            });
          }
        }}
        classNames={{
          input: "rounded-lg",
        }}
      />
    );

  return isFilterDisplayed ? (
    FilterInput
  ) : tooltip ? (
    <Tooltip label={tooltip} className="max-w-[250px]" multiline>
      {TextContent}
    </Tooltip>
  ) : (
    TextContent
  );
}

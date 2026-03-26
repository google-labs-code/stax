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

import { Box, Flex, Group, Select, Text } from "@mantine/core";
import { MRT_TableInstance } from "mantine-react-table";
import { useMemo } from "react";

import CustomSelectOption from "./CustomSelectOption";
import MaterialIcon from "./MaterialIcon";
import Pagination from "./Pagination";

interface CustomBottomToolbarProps<TData extends Record<string, any>> {
  table: MRT_TableInstance<TData>;
  nextPageToken?: number | null | undefined;
  page?: number | undefined;
  setPageSize?: (page: number) => void;
  pageSize?: number;
  setPage?: (page: number) => void;
  totalPages?: number;
  totalSize?: number | undefined;
  addedRowsCount?: number | undefined;
  showPageSizeSelector?: boolean;
  selectAllRowsInProject?: boolean;
  refetchProject?: () => void;
  onClearSelection?: () => void;
}

export const CustomBottomToolbar = <TData extends Record<string, any>>({
  table,
  nextPageToken,
  setPageSize = () => {},
  page = 1,
  setPage = () => {},
  pageSize = 15,
  totalPages = 1,
  totalSize,
  addedRowsCount = 0,
  showPageSizeSelector = true,
  selectAllRowsInProject = false,
  refetchProject,
  onClearSelection,
}: CustomBottomToolbarProps<TData>) => {
  const { getState } = table;
  const { rowSelection } = getState();
  const selectedRowsCount = Object.keys(rowSelection).length;

  const displayCount = selectAllRowsInProject
    ? totalSize || 0
    : selectedRowsCount;

  const selectWidth = useMemo(() => {
    if (pageSize === -1) return "!w-[75px]";
    if (pageSize >= 1000) return "!w-[65px]";
    if (pageSize >= 100) return "!w-[55px]";

    return "!w-[50px]";
  }, [pageSize]);

  const pageSizeOptions = [
    { value: "15", label: "15" },
    { value: "30", label: "30" },
    { value: "100", label: "100" },
    { value: "1000", label: "1000" },
    { value: "-1", label: "All data" },
  ];

  const handlePageSizeChange = (value: string | null) => {
    if (value) {
      setPage(1);
      setPageSize(Number(value));
      refetchProject && refetchProject();
    }
  };

  const handleClearSelection = () => {
    table.setRowSelection({});
  };

  return (
    <Box className="gap-md p-[8px]">
      <Flex justify="space-between" align="center">
        <Box>
          {(selectedRowsCount > 0 || selectAllRowsInProject) && (
            <Group>
              <Text className="text-secondary text-body-12">
                {displayCount} of{" "}
                {(totalSize || table.getRowCount()) + addedRowsCount} selected
              </Text>
              <Text
                className="cursor-pointer !font-[500] text-brand text-body-12"
                onClick={onClearSelection || handleClearSelection}
              >
                Clear selection
              </Text>
            </Group>
          )}
        </Box>
        {showPageSizeSelector && (
          <Group>
            <Group gap={0}>
              <Text className="!mt-[1px] mr-1 !font-[500] text-body-12">
                Show
              </Text>
              <Select
                data={pageSizeOptions}
                classNames={{
                  input: `pl-[5px] border-0 ${selectWidth} text-body-12 !font-[500]`,
                  dropdown: "rounded-md !w-[100px]",
                }}
                renderOption={(option) => (
                  <CustomSelectOption option={option} />
                )}
                rightSection={<MaterialIcon name="arrow_drop_down" />}
                value={pageSize.toString()}
                onChange={handlePageSizeChange}
                size="xs"
              />
            </Group>

            <Pagination
              nextPageToken={nextPageToken}
              totalPages={totalPages || 1}
              currentPage={page || 1}
              onPageChange={(page) => {
                setPage(page);
                refetchProject && refetchProject();
              }}
            />
          </Group>
        )}
      </Flex>
    </Box>
  );
};

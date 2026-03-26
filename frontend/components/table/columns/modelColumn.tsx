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
import { TOOLTIPS } from "@/config/constants";
import { useModelsContext } from "@/hooks/useModelsContext";
import { Group, ScrollArea, Text } from "@mantine/core";
import { MRT_ColumnDef } from "mantine-react-table";

export const modelColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "model_label",
  id: "model_label",
  enableSorting: true,
  enableColumnFilter: true,
  accessorFn: (row: WorkbookItem) => {
    return row.model_label || "";
  },
  header: "Model nickname",
  Header: (props) => (
    <ColumnHeader {...props} tooltip={TOOLTIPS.MODEL_COLUMN_HEADER} />
  ),
  Cell: (props) => {
    const value = props.row.original.model_label;
    const { allModels } = useModelsContext();

    const model = allModels?.find((model) => model.label === value) || null;

    if (model) {
      return (
        <ScrollArea className="w-full" scrollbarSize={5}>
          <Group className="flex w-full flex-row flex-nowrap justify-between gap-0 p-[12px]">
            <Group className="w-full flex-nowrap gap-md">
              {model?.icon && <model.icon size={16} />}
              <Text className="gap-0 truncate text-secondary text-body-12">
                {model.label}
              </Text>
            </Group>
          </Group>
        </ScrollArea>
      );
    }

    return null;
  },
  filterVariant: "text" as const,
};

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
import MaterialIcon from "@/components/MaterialIcon";
import { Box, Group, Image } from "@mantine/core";
import { MRT_ColumnDef } from "mantine-react-table";

export const inputAttachmentsColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "input_attachments",
  id: "input_attachments",
  enableSorting: false,
  enableColumnFilter: true,
  header: "Input Attachments",
  size: 200,
  Header: (props) => (
    <ColumnHeader {...props} tooltip="Images attached to this input" />
  ),
  Cell: () => {
    const attachment = {
      url: "https://example.com/image.png",
    };
    // const attachment = props.row.original.input_attachments?.[0];
    const imgSrc = attachment?.url || attachment;

    return (
      <Box className="relative py-1">
        <Group className="w-full justify-center">
          <Image
            src={imgSrc}
            width={185}
            height={105}
            className="rounded-2xl"
            alt="Image attachment"
          />
        </Group>
        <Box className="absolute top-3 left-3 flex h-[20px] w-[20px] items-center justify-center rounded-[4px] bg-secondaryDark backdrop-blur-[50px]">
          <MaterialIcon name="cloud_upload" size={16} className="text-white" />
        </Box>
      </Box>
    );
  },
};

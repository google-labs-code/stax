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
import { TOOLTIPS } from "@/config/constants";
import { getSuccessNotificationConfig } from "@/config/notifications";
import { deleteTagLinksQuery } from "@/queries/clientQueries";
import { TagLinkEntityType, TagRaw, WorkbookMeta } from "@/types";
import { Group, Loader, ScrollArea } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { MRT_ColumnDef } from "mantine-react-table";

export const tagsColumn: MRT_ColumnDef<WorkbookItem> = {
  accessorKey: "tags",
  header: "Tags",
  enableSorting: true,
  enableColumnFilter: true,
  Header: (props) => <ColumnHeader {...props} tooltip={TOOLTIPS.TAGS} />,
  Cell: (props) => {
    const value = props.renderedCellValue as unknown as TagRaw[];
    const row = props.row.original;
    const tableMeta = props.table.options.meta as WorkbookMeta;

    const handleOpenTagsModal = (e: React.MouseEvent) => {
      e.stopPropagation();
      if (tableMeta?.openTagsModal) {
        tableMeta.openTagsModal([row]);
      }
    };

    const deleteTagLinkMutation = useMutation({
      mutationFn: deleteTagLinksQuery,
      onSuccess: (_, params) => {
        notifications.show(
          getSuccessNotificationConfig(
            "Tag successfully removed.",
            `removed-tag-link`,
          ),
        );

        tableMeta?.onTagRemove?.(params.tag_ids?.[0] || "", props.row.original);
      },
    });

    const SingleTag = ({ tag }: { tag: TagRaw }) => {
      return (
        <Group className="box-content flex flex-row flex-nowrap gap-sm rounded-sm bg-veryLightSilver py-[4px] pl-[8px] pr-[4px]">
          <span className="max-w-[130px] truncate uppercase text-secondary text-body-11">
            {tag.name}
          </span>
          <MaterialIcon
            name="close"
            size={18}
            className="cursor-pointer text-secondary"
            onClick={() =>
              deleteTagLinkMutation.mutate({
                entity_type: TagLinkEntityType.CHAT,
                entity_ids: [row.chat_id],
                tag_ids: [tag.id || ""],
              })
            }
          />
        </Group>
      );
    };

    const hasTags = value?.length > 0;

    const PlusButton = ({ hasTags }: { hasTags: boolean }) => {
      return (
        <MaterialIcon
          name="add"
          className={`tag-cell-plus-button cursor-pointer rounded-sm p-[4px] text-secondary border-default hover:bg-veryLightSilver ${
            hasTags && "hidden self-end"
          }`}
          size={18}
          onClick={handleOpenTagsModal}
        />
      );
    };

    return deleteTagLinkMutation.isPending ? (
      <Loader size="xs" />
    ) : (
      <Group
        className={`gap-0 flex flex-row ${hasTags && "justify-between"} flex-nowrap w-full tag-cell-with-tags`}
      >
        <ScrollArea className={`${hasTags && "pr-[5px]"}`} scrollbarSize={5}>
          <Group className={`flex flex-row flex-nowrap tags-content`}>
            {value?.map((tag, key) => <SingleTag key={key} tag={tag} />)}
          </Group>
        </ScrollArea>

        <PlusButton hasTags={hasTags} />
      </Group>
    );
  },
};

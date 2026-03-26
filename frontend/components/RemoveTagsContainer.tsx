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

import CreateDatasetHeader from "@/app/(authRoutes)/projects/[id]/components/CreateDataset/Header";
import {
  getErrorNotificationConfig,
  getSuccessNotificationConfig,
} from "@/config/notifications";
import { useTagsContext } from "@/hooks/useTagsContext";
import {
  deleteAllTagsLinksQuery,
  deleteTagLinksQuery,
  getTagLinksQuery,
} from "@/queries/clientQueries";
import { TagLinkEntityType, TagRaw } from "@/types";
import { Group, Loader, Stack } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { Dispatch, SetStateAction, useEffect, useState } from "react";

import ModalCard from "./ModalCard";
import Tag from "./Tag";

type RemoveTagsProps = {
  entityType?: TagLinkEntityType;
  entityIds?: string[];
  setRemovedTagIds: Dispatch<SetStateAction<string[]>>;
  selectAllRowsInProject?: boolean;
  projectId?: string;
  onClearBannerState?: () => void;
};

type SelectedTag = {
  linkIds: string[];
  tagName: string;
  tagId: string;
};

export default function RemoveTagsContainer({
  entityType,
  entityIds,
  setRemovedTagIds,
  selectAllRowsInProject = false,
  projectId,
  onClearBannerState,
}: RemoveTagsProps) {
  const [selectedTags, setSelectedTags] = useState<SelectedTag[]>([]);
  const [tags, setTags] = useState<TagRaw[]>([]);
  const [isRemovingTag, setIsRemovingTag] = useState(false);

  const { userTags } = useTagsContext();

  const getTagLinksMutation = useMutation({
    mutationFn: getTagLinksQuery,
    onSuccess: (response) => {
      if (entityIds && entityIds.length > 0 && entityType) {
        const entityTagLinks = response.filter(
          (tagLink) =>
            tagLink.target_entity === entityType &&
            entityIds.includes(tagLink.target_entity_id),
        );

        const newTags = tags
          .filter((tag) => {
            return entityTagLinks.some((tagLink) => tagLink.tag_id === tag.id);
          })
          .map((tag) => {
            return {
              tagId: tag.id,
              tagName: tag.name,
              linkIds: entityTagLinks
                .filter((tagLink) => tagLink.tag_id === tag.id)
                .map((tagLink) => tagLink.id),
            };
          });

        setSelectedTags(newTags as SelectedTag[]);
      }
    },
  });

  useEffect(() => {
    if (entityIds && entityIds.length > 0 && entityType) {
      getTagLinksMutation.mutate();
    }
  }, []);

  useEffect(() => {
    setTags(userTags);
  }, [userTags]);

  const deleteTagLinksMutation = useMutation({
    mutationFn: deleteTagLinksQuery,
    onSuccess: () => {
      setIsRemovingTag(false);
      notifications.show(
        getSuccessNotificationConfig(
          "Tag successfully removed.",
          `removed-tag-link`,
        ),
      );
    },
    onError: () => {
      notifications.show(
        getErrorNotificationConfig("Failed to remove tag from selected rows."),
      );
      setIsRemovingTag(false);
    },
  });

  const deleteAllTagsLinksMutation = useMutation({
    mutationFn: (params: { projectId: string; tagIds: string[] }) => {
      notifications.show(
        getSuccessNotificationConfig(
          "Removing tag from all rows in the project. This may take some time for large projects.",
          "remove-tag-all-started",
        ),
      );

      return deleteAllTagsLinksQuery(params.projectId, params.tagIds);
    },
    onSuccess: () => {
      setIsRemovingTag(false);
      notifications.show(
        getSuccessNotificationConfig(
          "Tag has been removed from all rows in the project.",
          "remove-tag-all-success",
        ),
      );
      if (onClearBannerState) {
        onClearBannerState();
      }
    },
    onError: () => {
      notifications.show(
        getErrorNotificationConfig(
          "Failed to remove tag from all rows in the project.",
        ),
      );
      setIsRemovingTag(false);
    },
  });

  const handleRemoveTag = (tag: SelectedTag) => {
    setIsRemovingTag(true);

    if (selectAllRowsInProject && projectId) {
      deleteAllTagsLinksMutation.mutate({
        projectId,
        tagIds: [tag.tagId],
      });
    } else if (entityIds && entityIds.length > 0) {
      deleteTagLinksMutation.mutate({
        tag_ids: [tag.tagId],
        entity_type: entityType || TagLinkEntityType.CHAT,
        entity_ids: entityIds,
      });
    }

    setRemovedTagIds((prevTags) => [...prevTags, tag?.tagId]);
    setSelectedTags((prev) =>
      prev.filter((prevTag) => prevTag.tagId !== tag.tagId),
    );
  };

  return (
    <Stack className="w-[100%]">
      <Stack gap={10}>
        <CreateDatasetHeader
          label={"Tags for this items"}
          tooltip="Previously assigned tags."
        />
        <ModalCard className="bg-neutrals-100">
          <Stack gap={6}>
            {getTagLinksMutation.isPending ||
            deleteTagLinksMutation.isPending ||
            deleteAllTagsLinksMutation.isPending ||
            isRemovingTag ? (
              <Loader size={20} />
            ) : (
              <Group gap={6}>
                {selectedTags.map((tag, key) => (
                  <Tag
                    key={key}
                    tag={tag?.tagName}
                    withCloseIcon
                    onClick={() => handleRemoveTag(tag)}
                  />
                ))}
              </Group>
            )}
          </Stack>
        </ModalCard>
      </Stack>
    </Stack>
  );
}

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
import { Workbook } from "@/app/(authRoutes)/projects/[id]/types";
import { TOOLTIPS } from "@/config/constants";
import {
  getErrorNotificationConfig,
  getSuccessNotificationConfig,
} from "@/config/notifications";
import { useTagsContext } from "@/hooks/useTagsContext";
import {
  addAllTagsLinksQuery,
  createNewTagQuery,
  createTagLinksQuery,
  deleteTagQuery,
  getTagLinksQuery,
} from "@/queries/clientQueries";
import { TagLinkEntityType, TagRaw } from "@/types";
import {
  Button,
  Group,
  Input,
  Loader,
  Stack,
  Text,
  Tooltip,
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import {
  QueryObserverResult,
  RefetchOptions,
  useMutation,
} from "@tanstack/react-query";
import { Dispatch, SetStateAction, useEffect, useState } from "react";

import MaterialIcon from "./MaterialIcon";
import ModalCard from "./ModalCard";
import Tag from "./Tag";

type ManageTagsProps = {
  inputPlaceholder?: string;
  searchTagHeaderLabel?: string;
  searchTagHeaderTooltip?: string;
  tagsHeaderLabel?: string;
  tagsHeaderTooltip?: string;
  onAddAction?: (selectedTags: TagRaw[]) => void;
  showSelectedTags?: boolean;
  showMyTags?: boolean;
  canDeleteTags?: boolean;
  entityType?: TagLinkEntityType;
  entityIds?: string[];
  refetchProject?: (
    options?: RefetchOptions,
  ) => Promise<QueryObserverResult<Workbook | null, Error>>;
  setAddedTags: Dispatch<SetStateAction<TagRaw[]>>;
  selectAllRowsInProject?: boolean;
  projectId?: string;
  onClearBannerState?: () => void;
};

export default function AddTagsContainer({
  inputPlaceholder,
  searchTagHeaderLabel,
  searchTagHeaderTooltip,
  tagsHeaderLabel,
  tagsHeaderTooltip,
  onAddAction,
  showSelectedTags = true,
  showMyTags = true,
  canDeleteTags = false,
  entityType,
  entityIds,
  setAddedTags,
  selectAllRowsInProject = false,
  projectId,
  onClearBannerState,
}: ManageTagsProps) {
  const [selectedTags, setSelectedTags] = useState<TagRaw[]>([]);
  const [, setEntityTags] = useState<TagRaw[]>([]);
  const [inputValue, setInputValue] = useState<string>("");
  const [tags, setTags] = useState<TagRaw[]>([]);
  const { userTags, refreshTags, isLoadingTags } = useTagsContext();

  const getTagLinksMutation = useMutation({
    mutationFn: getTagLinksQuery,
    onSuccess: (response) => {
      if (entityIds && entityIds.length > 0 && entityType) {
        const tagIds = response
          .filter(
            (tagLink) =>
              tagLink.target_entity === entityType &&
              entityIds.includes(tagLink.target_entity_id),
          )
          .map((tagLink) => tagLink.tag_id);

        const finalEntityTags = tags.filter(
          (tag) => tag.id && tagIds.includes(tag.id),
        );
        setEntityTags(finalEntityTags);
        setSelectedTags(finalEntityTags);
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

  const createTagLinksMutation = useMutation({
    mutationFn: createTagLinksQuery,
    onSuccess: () => {
      notifications.show(
        getSuccessNotificationConfig("Tag successfully added.", `new-tag-link`),
      );
    },
  });

  const addAllTagsLinksMutation = useMutation({
    mutationFn: (params: { projectId: string; tagIds: string[] }) => {
      notifications.show(
        getSuccessNotificationConfig(
          "Adding tag to all rows in the project. This may take some time for large projects.",
          "add-tag-all-started",
        ),
      );

      return addAllTagsLinksQuery(params.projectId, params.tagIds);
    },
    onSuccess: () => {
      notifications.show(
        getSuccessNotificationConfig(
          "Tag has been added to all rows in the project.",
          "add-tag-all-success",
        ),
      );
      if (onClearBannerState) {
        onClearBannerState();
      }
    },
  });

  const createTagMutation = useMutation({
    mutationFn: createNewTagQuery,
    onSuccess: (response) => {
      if (showSelectedTags) {
        const selectedTagExists = selectedTags.find(
          (t) => t === response?.name,
        );

        if (!selectedTagExists) {
          setSelectedTags([...selectedTags, response]);
          setEntityTags([...selectedTags, response]);
        }
      }

      setInputValue("");
      refreshTags();

      if (!canDeleteTags && response.id) {
        if (selectAllRowsInProject && projectId) {
          addAllTagsLinksMutation.mutate({
            projectId,
            tagIds: [response.id],
          });
        } else if (entityIds && entityIds.length > 0) {
          createTagLinksMutation.mutate({
            entity_type: entityType as TagLinkEntityType,
            entity_ids: entityIds,
            tag_ids: [response.id],
          });
        }

        setAddedTags((prevTags) => [...prevTags, response]);
      } else {
        notifications.show(
          getSuccessNotificationConfig("Tag created successful", `new-tag`),
        );
      }
    },
  });

  const deleteTagMutation = useMutation({
    mutationFn: deleteTagQuery,
    onSuccess: () => {
      refreshTags();
      notifications.show(
        getSuccessNotificationConfig("Tag deleted successful", `deleted-tag`),
      );
    },
  });

  const handleAddTag = () => {
    const tag = inputValue.trim();

    if (tag === "") return;

    const splittedTags = tag
      .split(",")
      .map((t) => t.trim())
      .filter((t) => t !== "");

    let hasExistingTags = false;
    const existingTags: string[] = [];

    splittedTags.forEach((t) => {
      const tagExists = tags.find(
        (existingTag: TagRaw) =>
          existingTag.name.toLowerCase() === t.toLowerCase(),
      );

      if (tagExists) {
        hasExistingTags = true;
        existingTags.push(t);
      } else {
        createTagMutation.mutate({
          name: t,
          color: "",
        });
      }
    });

    if (hasExistingTags) {
      const message =
        existingTags.length > 1
          ? `Tags "${existingTags.join('", "')}" already exist.`
          : `Tag "${existingTags[0]}" already exists.`;

      notifications.show(getErrorNotificationConfig(message));
    }

    if (onAddAction) {
      onAddAction(selectedTags);
    }
  };

  return (
    <Stack className="w-[100%]" data-testid="add-tags-container">
      <Stack gap={10}>
        <CreateDatasetHeader
          label={searchTagHeaderLabel || "Tags for this items"}
          tooltip={searchTagHeaderTooltip}
        />
        <ModalCard className="bg-neutrals-100">
          <Stack gap={6}>
            <Group>
              <Input
                value={inputValue}
                onChange={(event) => setInputValue(event.currentTarget.value)}
                className="flex flex-grow"
                classNames={{ input: "rounded-md h-[40px]" }}
                placeholder={
                  inputPlaceholder || "Add additional or select from below"
                }
              />
              {inputValue.trim() === "" ? (
  <Tooltip
    label={TOOLTIPS.TAG_NAME_REQUIRED_MESSAGE}
    position="bottom"
    withArrow
  >
    <div> 
      <Button
        disabled
        leftSection={<MaterialIcon name="add" size={18} />}
        data-testid="add-tag-button-disabled"
      >
        Add
      </Button>
    </div>
  </Tooltip>
) : (
  <Button
    loading={
      isLoadingTags ||
      deleteTagMutation.isPending ||
      createTagMutation.isPending ||
      createTagLinksMutation.isPending ||
      addAllTagsLinksMutation.isPending
    }
    leftSection={<MaterialIcon name="add" size={18} />}
    onClick={handleAddTag}
    data-testid="add-tag-button-enabled"
  >
    Add
  </Button>
)}
            </Group>
            {showSelectedTags && selectedTags.length > 0 && (
              <Group gap={6}>
                {selectedTags.map((tag, key) => (
                  <Tag key={key} tag={tag?.name} />
                ))}
              </Group>
            )}
          </Stack>
        </ModalCard>
      </Stack>
      {showMyTags && (
        <Stack gap={10}>
          <CreateDatasetHeader
            label={tagsHeaderLabel || "My tags"}
            tooltip={tagsHeaderTooltip}
          />
          <ModalCard>
            {isLoadingTags ||
            deleteTagMutation.isPending ||
            createTagMutation.isPending ||
            createTagLinksMutation.isPending ||
            addAllTagsLinksMutation.isPending ? (
              <Loader size="xs" />
            ) : (
              <Group gap={6}>
                {tags?.map((tag: TagRaw, key: number) => (
                  <Tag
                    key={key}
                    tag={tag.name}
                    withCloseIcon={canDeleteTags}
                    onClick={() => {
                      if (!canDeleteTags && tag.id) {
                        if (!selectedTags.find((t) => t.id === tag.id)) {
                          setSelectedTags([...selectedTags, tag]);
                        }

                        if (selectAllRowsInProject && projectId) {
                          addAllTagsLinksMutation.mutate({
                            projectId,
                            tagIds: [tag.id],
                          });
                        } else if (entityIds && entityIds.length > 0) {
                          createTagLinksMutation.mutate({
                            entity_type: entityType as TagLinkEntityType,
                            entity_ids: entityIds,
                            tag_ids: [tag.id],
                          });
                        }

                        setAddedTags((prevTags) => [...prevTags, tag]);
                      }
                      if (canDeleteTags && tag.id) {
                        deleteTagMutation.mutate(tag.id);
                      }
                    }}
                    checked={selectedTags.map((t) => t.id).includes(tag.id)}
                  />
                ))}
                {tags?.length === 0 && (
                  <Text className="text-neutrals-500 text-title-12">
                    No tags added yet.
                  </Text>
                )}
              </Group>
            )}
          </ModalCard>
        </Stack>
      )}
    </Stack>
  );
}

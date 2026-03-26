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

import { ManageTagsModalType, TagLinkEntityType, TagRaw } from "@/types";
import { Modal, Text } from "@mantine/core";
import { useEffect, useState } from "react";

import AddTagsContainer from "./AddTagsContainer";
import RemoveTagsContainer from "./RemoveTagsContainer";

type ManageTagsModalProps = {
  isOpened: boolean;
  onClose: (addedTags?: TagRaw[], removedTagIds?: string[]) => void;
  entityType?: TagLinkEntityType;
  entityIds?: string[];
  type?: ManageTagsModalType;
  selectAllRowsInProject?: boolean;
  projectId?: string;
  onClearBannerState?: () => void;
};

export default function ManageTagsModal({
  isOpened,
  onClose,
  entityType,
  entityIds,
  type,
  selectAllRowsInProject = false,
  projectId,
  onClearBannerState,
}: ManageTagsModalProps) {
  const [addedTags, setAddedTags] = useState<TagRaw[]>([]);
  const [removedTagIds, setRemovedTagIds] = useState<string[]>([]);

  useEffect(() => {
    if (isOpened) {
      setAddedTags([]);
      setRemovedTagIds([]);
    }
  }, [isOpened]);

  return (
    <Modal
      opened={isOpened}
      onClose={() => {
        onClose(addedTags, removedTagIds);
      }}
      closeButtonProps={{ "aria-label": "close button" }}
      size="700px"
      title={
        <Text className="flex items-center gap-2 !font-medium text-title-22">
          {type === ManageTagsModalType.REMOVE ? "Remove tags" : "Add tags"}
        </Text>
      }
      centered
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[32px]",
      }}
    >
      {type === ManageTagsModalType.REMOVE ? (
        <RemoveTagsContainer
          entityType={entityType}
          entityIds={entityIds}
          setRemovedTagIds={setRemovedTagIds}
          selectAllRowsInProject={selectAllRowsInProject}
          projectId={projectId}
          onClearBannerState={onClearBannerState}
        />
      ) : (
        <AddTagsContainer
          setAddedTags={setAddedTags}
          entityType={entityType}
          entityIds={entityIds}
          searchTagHeaderTooltip="Select or create new tag(s) for this item. Add tags to enable filtering and grouping on the analytics page."
          tagsHeaderTooltip="Previously created tags."
          selectAllRowsInProject={selectAllRowsInProject}
          projectId={projectId}
          onClearBannerState={onClearBannerState}
        />
      )}
    </Modal>
  );
}

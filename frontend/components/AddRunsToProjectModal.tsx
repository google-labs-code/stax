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

"use client";

import MaterialIcon from "@/components/MaterialIcon";
import { getSuccessNotificationConfig } from "@/config/notifications";
import { useChatContext } from "@/hooks/useChatContext";
import { useDatasetsContext } from "@/hooks/useDatasetsContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import {
  dataTransferAllQuery,
  dataTransferQuery,
} from "@/queries/clientQueries";
import { ProjectComboboxItem } from "@/types";
import {
  Badge,
  Button,
  Combobox,
  ComboboxItem,
  Group,
  Input,
  InputBase,
  Modal,
  Text,
  UnstyledButton,
  useCombobox,
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { useEffect, useState } from "react";

type AddRunsToProjectModalProps = {
  isOpened: boolean;
  onClose: () => void;
  onCreateNewProjectClick: () => void;
  selectedProject: ProjectComboboxItem | null;
  setSelectedProject: (project: ProjectComboboxItem | null) => void;
  selectedWorkbookChatIds?: string[];
  sourceProjectId: string;
  isDataset?: boolean;
  useDataTransferAll?: boolean;
  selectAllRowsInProject?: boolean;
  onClearBannerState?: () => void;
  transferToDataset?: boolean;
};

export default function AddRunsToProjectModal({
  isOpened,
  onClose,
  onCreateNewProjectClick,
  selectedProject,
  setSelectedProject,
  selectedWorkbookChatIds,
  sourceProjectId,
  isDataset,
  useDataTransferAll,
  selectAllRowsInProject = false,
  onClearBannerState,
  transferToDataset = false,
}: AddRunsToProjectModalProps) {
  const { allProjects } = useProjectsContext();
  const { allDatasets, isLoadingDatasets } = useDatasetsContext();
  const { chats } = useChatContext();
  const [filteredProjects, setFilteredProjects] = useState<ComboboxItem[]>();

  const transferDataToProjectMutation = useMutation({
    mutationFn: (chatIds: string[]) => {
      return dataTransferQuery({
        chat_ids: chatIds,
        source_id: sourceProjectId,
        source_type: isDataset ? "DATASET" : "PROJECT",
        target_id: selectedProject?.value || "",
        target_type: transferToDataset ? "DATASET" : "PROJECT",
      });
    },
    onSuccess: () => {
      onClose();
      notifications.show(
        getSuccessNotificationConfig(
          transferToDataset
            ? `Data is added to the dataset ${selectedProject?.label} successfully.`
            : `Data is added to the project ${selectedProject?.label} successfully.`,
          "data-added-success",
        ),
      );
    },
  });

  const dataTransferAllMutation = useMutation({
    mutationFn: () => {
      return dataTransferAllQuery({
        source_id: sourceProjectId,
        source_type: isDataset ? "DATASET" : "PROJECT",
        target_id: selectedProject?.value || "",
        target_type: transferToDataset ? "DATASET" : "PROJECT",
      });
    },
    onSuccess: () => {
      if (onClearBannerState) {
        onClearBannerState();
      }
      onClose();
      notifications.show(
        getSuccessNotificationConfig(
          isDataset
            ? "Dataset imported successfully."
            : "Data transferred successfully.",
          isDataset ? "dataset-imported-success" : "data-transferred-success",
        ),
      );
    },
  });

  const onAddToTarget = () => {
    if (!selectedProject) return;

    if (useDataTransferAll || selectAllRowsInProject) {
      dataTransferAllMutation.mutate();
    } else if (selectedWorkbookChatIds) {
      transferDataToProjectMutation.mutate(selectedWorkbookChatIds);
    } else {
      transferDataToProjectMutation.mutate(chats.map((chat) => chat.id));
    }
  };

  const combobox = useCombobox({
    onDropdownClose: () => {
      combobox.resetSelectedOption();
    },
  });

  useEffect(() => {
    if (!isOpened) return;

    if (transferToDataset) {
      setFilteredProjects(
        allDatasets
          .sort((a, b) => a.name.localeCompare(b.name))
          .map((dataset) => {
            return {
              value: dataset.id,
              label: dataset.name,
            };
          }),
      );
    } else {
      setFilteredProjects(
        allProjects
          .filter((project) => {
            if (isDataset) {
              return true;
            } else {
              return (
                !project.is_default_project &&
                project.project_id !== sourceProjectId
              );
            }
          })
          .sort((a, b) => a.name.localeCompare(b.name))
          .map((project) => {
            return {
              value: project.project_id,
              label: project.name,
            };
          }),
      );
    }
  }, [
    isOpened,
    allProjects,
    allDatasets,
    sourceProjectId,
    isDataset,
    transferToDataset,
  ]);

  const modalTitle = transferToDataset
    ? "Add data to a dataset"
    : "Add data to a project";

  const buttonLabel = transferToDataset ? "Add to dataset" : "Add to project";

  const placeholderText = transferToDataset
    ? "Select from datasets"
    : "Select from projects";

  const createNewText = transferToDataset
    ? "Create new dataset"
    : "Create new project";

  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      centered
      size="700px"
      title={<Text className="!font-medium text-title-22">{modalTitle}</Text>}
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[32px]",
      }}
    >
      <Group gap="md" className="flex flex-row">
        <Combobox store={combobox} withinPortal>
          <Combobox.Target>
            <InputBase
              variant="default"
              component="button"
              type="button"
              pointer
              className="flex-1"
              classNames={{
                input:
                  "text-body-14 !text-black rounded-lg h-[40px] placeholder:text-secondaryDark",
              }}
              rightSectionPointerEvents="none"
              rightSection={
                <MaterialIcon
                  name="keyboard_arrow_down"
                  className="cursor-pointer !font-light"
                />
              }
              onClick={() => combobox.toggleDropdown()}
            >
              {selectedProject ? (
                <>
                  <span>{selectedProject.label}</span>
                  {selectedProject.isNew && (
                    <Badge
                      size="sm"
                      className="ml-2 cursor-pointer rounded-lg bg-lightBlue text-brand"
                    >
                      New
                    </Badge>
                  )}
                </>
              ) : (
                <Input.Placeholder>{placeholderText}</Input.Placeholder>
              )}
            </InputBase>
          </Combobox.Target>

          <Combobox.Dropdown className="max-h-[260px] overflow-y-auto rounded-lg px-4 py-2">
            <Combobox.Options>
              {isLoadingDatasets && transferToDataset ? (
                <Combobox.Empty>Loading datasets...</Combobox.Empty>
              ) : filteredProjects && filteredProjects.length > 0 ? (
                filteredProjects?.map((item) => (
                  <Combobox.Option
                    key={item.value}
                    value={item.value}
                    onClick={() => {
                      setSelectedProject(item);
                      combobox.closeDropdown();
                    }}
                  >
                    <Text className="text-title-14">{item.label}</Text>
                  </Combobox.Option>
                ))
              ) : (
                <Combobox.Empty>
                  {transferToDataset
                    ? "No datasets found"
                    : "No projects found"}
                </Combobox.Empty>
              )}
            </Combobox.Options>
          </Combobox.Dropdown>
        </Combobox>
        <Text className="text-secondary text-body-12">or</Text>

        <UnstyledButton
          className="flex items-center gap-1 rounded-sm hover:bg-veryLightSilver p-2"
          onClick={onCreateNewProjectClick}
        >
          <MaterialIcon name="add" size={20} />
          <Text className="text-title-12">{createNewText}</Text>
        </UnstyledButton>
      </Group>
      <Group className="mt-8" justify="flex-end">
        <Button
          w={160}
          size="lg"
          color="brand"
          onClick={onAddToTarget}
          loading={
            transferDataToProjectMutation.isPending ||
            dataTransferAllMutation.isPending
          }
        >
          {buttonLabel}
        </Button>
      </Group>
    </Modal>
  );
}

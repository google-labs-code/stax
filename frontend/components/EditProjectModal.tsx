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

import { TOOLTIPS } from "@/config/constants";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import { updateProjectQuery } from "@/queries/clientQueries";
import { Project } from "@/types";
import { Loader, Modal, Stack, Text, Textarea } from "@mantine/core";
import { useMutation } from "@tanstack/react-query";
import { useEffect, useState } from "react";

import ModalFooterButton from "./ModalFooter";
import ModalInput from "./ModalInput";
import ModalInputLabel from "./ModalInputLabel";

type EditProjectModalProps = {
  isOpened: boolean;
  onClose: () => void;
  projectId?: string | null;
};

export default function EditProjectModal({
  isOpened,
  onClose,
  projectId,
}: EditProjectModalProps) {
  const { allProjects, refreshProjects, isLoadingProjects } =
    useProjectsContext();
  const [project, setProject] = useState<Project>({} as Project);
  const updateProjectMutation = useMutation({
    mutationFn: updateProjectQuery,
    onSuccess: async () => refreshProjects(),
  });

  useEffect(() => {
    if (projectId) {
      setProject(
        allProjects.filter(
          (project: Project) => project.project_id === projectId,
        )[0],
      );
    }
  }, [projectId]);

  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      closeButtonProps={{ "aria-label": "close button" }}
      size="700px"
      title={
        <Text className="text-title-22 flex items-center gap-2 !font-medium">
          Edit project name and description
        </Text>
      }
      centered
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[32px]",
      }}
      data-testid="edit-project-modal"
    >
      {!isLoadingProjects ? (
        <Stack className="modal-form-two-rows-inputs">
          <Stack gap={6}>
            <ModalInputLabel label="Name" isRequired />

            <ModalInput
              placeholder="Name of the project"
              value={project?.name}
              onChange={(e) =>
                setProject({ ...project, name: e.currentTarget.value })
              }
            />
          </Stack>

          <Stack gap={6}>
            <ModalInputLabel label="Description" />

            <Textarea
              className="w-[100%]"
              variant="modern"
              classNames={{
                input: `h-[110px] !px-[16px]`,
              }}
              value={project?.description}
              placeholder="Add a description for your project here"
              onChange={(event) => {
                setProject({
                  ...project,
                  description: event.currentTarget.value,
                });
              }}
            />
          </Stack>

          <ModalFooterButton
            label="Update"
            isDisabled={!project.name}
            isLoading={updateProjectMutation.isPending}
            onClick={() => {
              updateProjectMutation.mutate(project);
              onClose();
            }}
            tooltipLabel={
              !project.name ? TOOLTIPS.PROJECT_NAME_REQUIRED_MESSAGE : undefined
            }
          />
        </Stack>
      ) : (
        <Loader size="sm" />
      )}
    </Modal>
  );
}

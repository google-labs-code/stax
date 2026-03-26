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
import ModalInput from "@/components/ModalInput";
import ModalInputLabel from "@/components/ModalInputLabel";
import { getErrorNotificationConfig } from "@/config/notifications";
import { useDatasetsContext } from "@/hooks/useDatasetsContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import {
  createDatasetQuery,
  createProjectQuery,
} from "@/queries/clientQueries";
import { Project, ProjectComboboxItem, ProjectType } from "@/types";
import { Button, Group, Modal, Stack, Text, Textarea } from "@mantine/core";
import { useForm } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";

import { Dataset } from "../app/(authRoutes)/datasets/types";

type CreateNewEntityModalProps = {
  isOpened: boolean;
  onClose: () => void;
  onBackClick: () => void;
  setSelectedProject: (project: ProjectComboboxItem | null) => void;
  isDataset?: boolean;
};

export default function CreateNewEntityModal({
  isOpened,
  onClose,
  onBackClick,
  setSelectedProject,
  isDataset = false,
}: CreateNewEntityModalProps) {
  const { refreshProjects } = useProjectsContext();
  const { refreshDatasets } = useDatasetsContext();

  const form = useForm({
    initialValues: {
      name: "",
      description: "",
      type: ProjectType.POINTWISE,
    },
    validate: {
      name: (val) => (!val || val.trim() === "" ? "Name is required." : null),
    },
  });

  const createProjectMutation = useMutation({
    mutationFn: createProjectQuery,
    onSuccess: async (project: Project) => {
      refreshProjects();
      setSelectedProject({
        value: project.project_id,
        label: project.name,
        isNew: true,
      });
      form.reset();
      onBackClick();
    },
    onError: (err) => {
      notifications.show(getErrorNotificationConfig(err.message));
      onBackClick();
    },
  });

  const createDatasetMutation = useMutation({
    mutationFn: createDatasetQuery,
    onSuccess: async (dataset: Dataset) => {
      refreshDatasets();
      setSelectedProject({
        value: dataset.id,
        label: dataset.name,
        isNew: true,
      });
      form.reset();
      onBackClick();
    },
    onError: (err) => {
      notifications.show(getErrorNotificationConfig(err.message));
      onBackClick();
    },
  });

  const handleCreateEntity = () => {
    const validation = form.validate();
    if (validation.hasErrors) {
      return;
    }

    const formValues = {
      name: form.values.name,
      description: form.values.description,
    };

    if (isDataset) {
      createDatasetMutation.mutate(formValues as any);
    } else {
      createProjectMutation.mutate({
        ...formValues,
        type:
          form.values.type === ProjectType.SIDE_BY_SIDE
            ? ProjectType.SIDE_BY_SIDE
            : ProjectType.POINTWISE,
      } as Project);
    }
  };

  const entityType = isDataset ? "dataset" : "project";
  const isPending = isDataset
    ? createDatasetMutation.isPending
    : createProjectMutation.isPending;

  const isNameEmpty = !form.values.name || form.values.name.trim() === "";

  const handleTypeChange = (newType: ProjectType) => {
    form.setFieldValue("type", newType);
  };

  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      centered
      size="700px"
      title={
        <Text className="text-title-22 !font-medium">
          Create new {entityType}
        </Text>
      }
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[24px]",
      }}
    >
      <Stack className="modal-form-two-rows-inputs">
        <Stack gap={6}>
          <div className="flex items-center justify-between">
            <div>
              <ModalInputLabel label="Name" isRequired />
            </div>

            {!isDataset && (
              <div className="flex items-center">
                <div className="rounded-lg flex">
                  <Button
                    onClick={() => handleTypeChange(ProjectType.POINTWISE)}
                    variant={
                      form.values.type === ProjectType.POINTWISE
                        ? "filled"
                        : "default"
                    }
                    color={
                      form.values.type === ProjectType.POINTWISE
                        ? "brand"
                        : "gray"
                    }
                    classNames={{
                      root: "px-3 py-0 h-8 rounded-l-lg border border-borderColor",
                      inner: "font-normal",
                      label: "text-body-12",
                    }}
                    className={
                      form.values.type === ProjectType.POINTWISE
                        ? "!bg-brand"
                        : "!bg-veryLightSilver"
                    }
                    leftSection={
                      <MaterialIcon
                        name="linear_scale"
                        size={14}
                        className={
                          form.values.type === ProjectType.POINTWISE
                            ? "text-white"
                            : "text-gray-600"
                        }
                      />
                    }
                  >
                    Pointwise
                  </Button>

                  <Button
                    onClick={() => handleTypeChange(ProjectType.SIDE_BY_SIDE)}
                    variant={
                      form.values.type === ProjectType.SIDE_BY_SIDE
                        ? "filled"
                        : "default"
                    }
                    color={
                      form.values.type === ProjectType.SIDE_BY_SIDE
                        ? "brand"
                        : "gray"
                    }
                    classNames={{
                      root: "px-3 py-0 h-8 rounded-r-lg border border-borderColor",
                      inner: "font-normal",
                      label: "text-body-12",
                    }}
                    className={
                      form.values.type === ProjectType.SIDE_BY_SIDE
                        ? "!bg-brand"
                        : "!bg-veryLightSilver"
                    }
                    leftSection={
                      <MaterialIcon
                        name="split_scene"
                        size={14}
                        className={
                          form.values.type === ProjectType.SIDE_BY_SIDE
                            ? "text-white"
                            : "text-gray-600"
                        }
                      />
                    }
                  >
                    Side by Side
                  </Button>
                </div>
              </div>
            )}
          </div>

          <ModalInput
            placeholder="Name"
            value={form.values.name}
            onChange={(e) => form.setFieldValue("name", e.currentTarget.value)}
          />
        </Stack>

        <Stack gap={6}>
          <ModalInputLabel label="Description" />
          <Textarea
            {...form.getInputProps("description")}
            classNames={{
              input: "!h-[110px]",
            }}
            value={form.values.description}
            onChange={(event) =>
              form.setFieldValue("description", event.currentTarget.value)
            }
            placeholder={`Add a description for your ${entityType} here`}
          />
        </Stack>
      </Stack>

      <Group className="mt-8" justify="flex-end">
        <Button w={160} size="lg" onClick={onBackClick} variant="outlineLight">
          Back
        </Button>
        <Button
          w={160}
          size="lg"
          color="brand"
          onClick={handleCreateEntity}
          loading={isPending}
          disabled={isNameEmpty}
        >
          Create {entityType}
        </Button>
      </Group>
    </Modal>
  );
}

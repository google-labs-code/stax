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

import CustomModelCard from "@/components/CustomModelCard";
import DefaultModelCard from "@/components/DefaultModelCard";
import DeleteModal from "@/components/DeleteModal";
import MaterialIcon from "@/components/MaterialIcon";
import ModelSettingsModal from "@/components/ModelSettingsModal";
import { LOCAL_STORAGE_SHOW_CUSTOM_MODEL_MODAL } from "@/config/constants";
import { getSuccessNotificationConfig } from "@/config/notifications";
import { useModelsContext } from "@/hooks/useModelsContext";
import { deprecateModelQuery } from "@/queries/clientQueries";
import { Model } from "@/queries/types";
import { CustomModelModalType, ModelSettingsModalType } from "@/types";
import LocalStorage from "@/utils/LocalStorage";
import {
  Button,
  Card,
  Group,
  Loader,
  Menu,
  Stack,
  Text,
  UnstyledButton,
} from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { useCallback, useEffect, useState } from "react";

import CustomModelModal from "./CustomModelModal";
import ModelMenuDropdown from "./ModelMenuDropdown";

export default function ModelManager() {
  const {
    defaultModels,
    customModels,
    refreshModels,
    isLoadingModels,
    providers,
  } = useModelsContext();
  const [selectedModel, setSelectedModel] = useState<Model | null>(null);
  const [filteredDefaultModels, setFilteredDefaultModels] =
    useState<Model[]>(defaultModels);
  const [filteredCustomModels, setFilteredCustomModels] =
    useState<Model[]>(customModels);

  const [customModelModalType, setCustomModelModalType] =
    useState<CustomModelModalType>(CustomModelModalType.ADD);
  const [
    isSettingsModalOpened,
    { open: openSettingsModal, close: closeSettingsModal },
  ] = useDisclosure(false);
  const [
    isCustomModelModal,
    { open: openCustomModelModal, close: closeCustomModelModal },
  ] = useDisclosure(false);

  const [
    isDeleteModalOpened,
    { open: openDeleteModal, close: closeDeleteModal },
  ] = useDisclosure(false);
  const [modalSettingsType, setModalSettingsType] =
    useState<ModelSettingsModalType>(ModelSettingsModalType.ADD);
  const deprecateModelMutation = useMutation({
    mutationFn: deprecateModelQuery,
    onSuccess: () => {
      refreshModels();
      setSelectedModel(null);
      notifications.show(
        getSuccessNotificationConfig(
          "Model deleted successfully!",
          "model-deleted",
        ),
      );
      closeDeleteModal();
    },
  });
  const [visibleProviders, setVisibleProviders] = useState<string[]>([]);

  useEffect(() => {
    if (providers && typeof providers === "object") {
      setVisibleProviders(Object.keys(providers));
    }
  }, [providers]);

  const showCustomModelModal = () => {
    setSelectedModel(null);
    setCustomModelModalType(CustomModelModalType.ADD);
    openCustomModelModal();
  };

  const AddModelButton = () => (
    <Menu position="bottom-end" shadow="md">
      <Menu.Target>
        <Group
          h={44}
          className="gap-4xl rounded-sm border border-solid border-lightSilver justify-center items-center px-3 cursor-pointer"
        >
          <Text className="text-title-12">Add model</Text>
          <MaterialIcon name="keyboard_arrow_down" size={20} />
        </Group>
      </Menu.Target>
      <Menu.Dropdown>
        <Group className="flex-col p-2 text-title-12 cursor-pointer" gap={16}>
          <Group
            gap={4}
            className="text-title-12 justify-center items-center self-start"
            onClick={() => {
              setSelectedModel(null);
              setModalSettingsType(ModelSettingsModalType.ADD);
              openSettingsModal();
            }}
          >
            <MaterialIcon name="settings" size={16} />
            Configure existing model
          </Group>
          <Group
            gap={4}
            className="text-title-12 justify-center items-center self-start"
            onClick={showCustomModelModal}
          >
            <MaterialIcon name="add" size={16} />
            Add custom model
          </Group>
        </Group>
      </Menu.Dropdown>
    </Menu>
  );

  const onModelClick = useCallback(
    (provider: string) => {
      if (visibleProviders.includes(provider)) {
        setVisibleProviders(visibleProviders.filter((p) => p !== provider));
      } else {
        setVisibleProviders([...visibleProviders, provider]);
      }
    },
    [visibleProviders, providers],
  );

  useEffect(() => {
    setFilteredDefaultModels(defaultModels);
    setFilteredCustomModels(customModels);
  }, [defaultModels, customModels]);

  useEffect(() => {
    setFilteredDefaultModels([
      ...defaultModels.filter((model) =>
        visibleProviders?.includes(model.provider),
      ),
    ]);
    setFilteredCustomModels(
      customModels.filter((model) =>
        visibleProviders?.includes(model.provider),
      ),
    );
  }, [visibleProviders, defaultModels, customModels, providers]);

  useEffect(() => {
    if (LocalStorage.get(LOCAL_STORAGE_SHOW_CUSTOM_MODEL_MODAL)) {
      showCustomModelModal();
      LocalStorage.remove(LOCAL_STORAGE_SHOW_CUSTOM_MODEL_MODAL);
    }
  }, []);

  return (
    <Card
      className="gap-2xl rounded-xl border border-solid border-lightSilver"
      shadow="md"
      data-testid="model-manager"
    >
      <DeleteModal
        isOpen={isDeleteModalOpened}
        onClose={closeDeleteModal}
        onConfirm={() => {
          if (selectedModel?.id) {
            deprecateModelMutation.mutate(selectedModel.id);
          }
        }}
        isLoading={deprecateModelMutation.isPending}
        title="Delete Model"
        description="Are you sure you want to delete this model? This cannot be undone."
      />
      <ModelSettingsModal
        isOpened={isSettingsModalOpened}
        onClose={closeSettingsModal}
        type={modalSettingsType}
        model={selectedModel}
      />

      <CustomModelModal
        isOpened={isCustomModelModal}
        onClose={closeCustomModelModal}
        model={selectedModel}
        type={customModelModalType}
      />

      <Group gap={0} className="flex flex-row justify-between">
        <Text className="!font-medium text-title-22">Model Manager</Text>

        <Group gap={8}>
          <Menu
            position="bottom-end"
            width={185}
            shadow="md"
            classNames={{
              dropdown: "rounded-md !w-[263px]",
              item: "py-3",
            }}
          >
            <Menu.Target>
              <Button
                variant="outlineLight"
                h={35}
                className="!p-2 !pl-4"
                classNames={{
                  label: "flex flex-row items-center justify-center gap-md",
                }}
              >
                <Group gap="4px">
                  <Text className="text-title-12">Model makers</Text>
                  <span className="w-[16px] rounded-[50%] bg-brand text-white text-title-11">
                    {visibleProviders.length}
                  </span>
                </Group>

                <MaterialIcon name="keyboard_arrow_down" size={20} />
              </Button>
            </Menu.Target>
            <ModelMenuDropdown
              onClick={onModelClick}
              visibleProviders={visibleProviders}
            />
          </Menu>
          <AddModelButton />
        </Group>
      </Group>

      {isLoadingModels ? (
        <Loader size="sm" />
      ) : (
        <Stack gap={32}>
          <Group gap={16} className="flex flex-col items-start">
            <Group gap={4}>
              <Text className="text-title-16">Custom Models</Text>
              <MaterialIcon
                name="info"
                className="cursor-pointer text-resting"
                tooltipPosition="top"
                tooltipClassName="max-w-[240px]"
                tooltipLabel="Add your own fine-tuned AI models or specialized agents by providing an API url. The custom model must return data in the same schema as chosen model provider."
              />
            </Group>

            {filteredCustomModels.length > 0 ? (
              <Group gap={12}>
                {filteredCustomModels.map((model, key) => (
                  <CustomModelCard
                    key={key}
                    model={model}
                    onEdit={() => {
                      setSelectedModel(model);
                      setCustomModelModalType(CustomModelModalType.EDIT);
                      openCustomModelModal();
                    }}
                    onCopyModel={() => {
                      setSelectedModel(model);
                      setCustomModelModalType(CustomModelModalType.ADD);
                      openCustomModelModal();
                    }}
                    onDeleteModel={() => {
                      setSelectedModel(model);
                      openDeleteModal();
                    }}
                    onShowDetails={() => {
                      setSelectedModel(model);
                      setCustomModelModalType(CustomModelModalType.SHOW);
                      openCustomModelModal();
                    }}
                  />
                ))}
              </Group>
            ) : (
              <Group
                gap={4}
                className="w-full justify-center items-center self-start"
              >
                <UnstyledButton
                  className="text-title-12 h-[36px] gap-2 rounded-sm border border-solid border-lightSilver px-2 cursor-pointer flex justify-center items-center"
                  onClick={showCustomModelModal}
                >
                  <MaterialIcon name="add" size={16} />
                  Add custom model
                </UnstyledButton>
              </Group>
            )}
          </Group>
          <Group gap={16} className="flex flex-col items-start">
            <Group gap={4}>
              <Text className="text-title-16">System Models</Text>
              <MaterialIcon
                name="info"
                size={24}
                className="cursor-pointer text-resting"
                tooltipPosition="top"
                tooltipClassName="max-w-[240px]"
                tooltipLabel="System provided base models from model providers. Edits will create a new custom model version."
              />
            </Group>

            <Group>
              {filteredDefaultModels.map((model) => (
                <DefaultModelCard
                  key={model.id}
                  model={model}
                  onShowDetails={() => {
                    setSelectedModel(model);
                    setModalSettingsType(ModelSettingsModalType.DUPLICATE);
                    openSettingsModal();
                  }}
                  onCopyModel={() => {
                    setSelectedModel(model);
                    setModalSettingsType(ModelSettingsModalType.ADD);
                    openSettingsModal();
                  }}
                />
              ))}
            </Group>
          </Group>
        </Stack>
      )}
    </Card>
  );
}

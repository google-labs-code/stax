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

import MaterialIcon from "@/components/MaterialIcon";
import ModelCombobox from "@/components/ModelComboBox";
import ModelConfigurationModal from "@/components/ModelConfigurationModal";
import { useModelsContext } from "@/hooks/useModelsContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { Model } from "@/queries/types";
import { GAevents } from "@/types";
import logGAevent from "@/utils/logGAevent";
import { Card, Divider, UnstyledButton } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";

type InputCardModelSelectorProps = {
  readonly model: Model;
  readonly setModel: (model: Model | null) => void;
};

export default function InputCardModelSelector({
  model,
  setModel,
}: InputCardModelSelectorProps) {
  const { setIsPlaygroundModified, setIsNewChat } = usePlaygroundContext();
  const { isLoadingModels, setOpenedModel } = useModelsContext();
  const [
    isSettingsModalOpened,
    { open: openSettingsModal, close: closeSettingsModal },
  ] = useDisclosure(false);

  const onSetModel = (m: Model | null) => {
    setIsPlaygroundModified(true);
    setIsNewChat(true);
    setOpenedModel(m);
    setModel(m);
  };

  return (
    <Card className="flex w-full flex-row rounded-sm bg-white p-0 shadow-none">
      <ModelCombobox
        openedModel={model}
        isExpanded={false}
        isLoadingModels={isLoadingModels}
        onSelect={(selectedModel) => {
          logGAevent(GAevents.SELECT_MODEL, {
            source: "playground",
            provider: selectedModel?.provider,
          });
          onSetModel(selectedModel);
        }}
        className="!h-[48px] !border-0"
      />
      <Divider orientation="vertical" />
      <UnstyledButton
        className={`flex h-[48px] w-[48px] items-center justify-center bg-white ${!model?.provider && "cursor-not-allowed"}`}
        onClick={() => {
          if (model?.provider) {
            onSetModel(model);
            openSettingsModal && openSettingsModal();
          }
        }}
      >
        <MaterialIcon
          name="tune"
          size={20}
          className={model?.provider ? "text-secondaryDark" : "text-disabled"}
          disabled={!model?.provider}
        />
      </UnstyledButton>
      <ModelConfigurationModal
        isOpened={isSettingsModalOpened}
        onClose={closeSettingsModal}
      />
    </Card>
  );
}

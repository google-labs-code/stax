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

import { getProviders } from "@/config/constants";
import { useModelsContext } from "@/hooks/useModelsContext";
import { Model } from "@/queries/types";
import { Combobox, Loader, TextInput, useCombobox } from "@mantine/core";
import React, { ReactElement, useEffect, useMemo, useState } from "react";

import AccordionModelProviders from "./AccordionModelProviders";
import MaterialIcon from "./MaterialIcon";

interface ModelComboboxProps {
  onSelect: (model: Model | null) => void;
  isExpanded: boolean;
  isLoadingModels: boolean;
  closeDropdownTrigger?: boolean;
  setDropdownTrigger?: (value: boolean) => void;
  openedModel?: Model | null | undefined;
  className?: string;
  clearable?: boolean;
  isDisabled?: boolean;
  inputClassName?: string;
  iconSize?: number;
}
export default function ModelCombobox({
  onSelect,
  isExpanded,
  isLoadingModels,
  closeDropdownTrigger,
  setDropdownTrigger,
  openedModel,
  className,
  clearable,
  isDisabled,
  inputClassName,
  iconSize = 24,
}: ModelComboboxProps) {
  const { allModels } = useModelsContext();
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [selectedModel, setSelectedModel] = useState<Model | null>(null);
  const [selectedProviderIcon, setSelectedProviderIcon] =
    useState<ReactElement | null>(null);

  const combobox = useCombobox({
    onDropdownClose: () => {
      if (selectedModel) {
        setSearchTerm(selectedModel?.label);
        const provider = getProviders().find(
          (provider) =>
            provider.key.toLowerCase() ===
            selectedModel?.provider?.toLowerCase(),
        );
        if (provider) {
          const iconWithCustomSize = React.cloneElement(provider.icon, {
            size: iconSize,
          });
          setSelectedProviderIcon(() => iconWithCustomSize);
        }
      }
    },
    onDropdownOpen: () => {
      setSearchTerm("");
      setSelectedProviderIcon(null);
    },
  });

  useEffect(() => {
    if (closeDropdownTrigger) {
      combobox.closeDropdown();
      if (setDropdownTrigger) setDropdownTrigger(false);
    }
  }, [closeDropdownTrigger]);

  useEffect(() => {
    if (openedModel) {
      setSelectedModel(openedModel);
      setSearchTerm(openedModel.label);

      const provider = getProviders().find(
        (provider) =>
          provider.key.toLowerCase() === openedModel?.provider?.toLowerCase(),
      );

      if (provider) {
        const iconWithCustomSize = React.cloneElement(provider.icon, {
          size: iconSize,
        });
        setSelectedProviderIcon(() => iconWithCustomSize);
      }
    } else {
      setSelectedModel(null);
      setSearchTerm("");
      setSelectedProviderIcon(null);
    }
  }, [openedModel]);

  const filteredOptions = useMemo(() => {
    return searchTerm
      ? [...allModels].filter((model) =>
          model?.label?.toLowerCase().includes(searchTerm?.toLowerCase()),
        )
      : [...allModels];
  }, [searchTerm, allModels]);

  const handleSelection = (value: string) => {
    const selectedModelObj = allModels.find((model) => model.id === value);
    if (selectedModelObj) {
      setSelectedModel(selectedModelObj);
      setSearchTerm("");
      onSelect(selectedModelObj);

      const provider = getProviders().find(
        (provider) =>
          provider.key.toLowerCase() ===
          selectedModelObj.provider.toLowerCase(),
      );
      if (provider) {
        const iconWithCustomSize = React.cloneElement(provider.icon, {
          size: iconSize,
        });
        setSelectedProviderIcon(() => iconWithCustomSize);
      }

      combobox.closeDropdown();
    }
  };

  const clearSelection = () => {
    setSelectedModel(null);
    setSearchTerm("");
    onSelect(null);
  };

  return (
    <Combobox store={combobox} shadow="md" position="bottom-start" offset={6}>
      <Combobox.Target>
        <div
          data-testid="model-combobox"
          className={`flex w-full items-center ${
            isExpanded ? "h-[54px]" : "h-[42px]"
          } cursor-pointer rounded-sm px-[0px] py-[0px] border-default ${className}`}
          onClick={() => {
            if (isDisabled) {
              return;
            }

            combobox.openDropdown();
          }}
        >
          <TextInput
            disabled={isDisabled ? isDisabled : false}
            classNames={{
              root: "w-full",
              wrapper: "bg-transparent",
              input: `w-full ${selectedProviderIcon ? "pl-[40px]" : "!p-2"} ${inputClassName}
               border-none shadow-none px-0 focus:ring-0 focus:outline-none
               text-secondaryDark placeholder-resting ${
                 isExpanded ? "text-[16px]" : "text-[14px]"
               }`,
            }}
            data-testid="select-model"
            placeholder={
              isLoadingModels ? "Loading..." : "Select or search model"
            }
            onChange={(event) => {
              const newValue = event.currentTarget.value;
              setSearchTerm(newValue);
              if (newValue) setSelectedProviderIcon(null);
              combobox.openDropdown();
            }}
            value={searchTerm}
            leftSection={
              searchTerm === "" && selectedProviderIcon
                ? null
                : selectedProviderIcon
            }
            leftSectionWidth={40}
            rightSection={
              searchTerm === "" && isLoadingModels ? (
                <Loader size="xs" />
              ) : selectedModel && clearable ? (
                <MaterialIcon
                  name="close"
                  className="cursor-pointer !font-light"
                  onClick={clearSelection}
                  dataTestId="material-icon-close"
                />
              ) : (
                <MaterialIcon
                  name="keyboard_arrow_down"
                  dataTestId="material-icon-keyboard_arrow_down"
                />
              )
            }
          />
        </div>
      </Combobox.Target>
      <Combobox.Dropdown className="!min-w-[270px] rounded-lg">
        {isLoadingModels ? (
          <Combobox.Empty>
            <p className="p-[16px] text-title-14">Loading...</p>
          </Combobox.Empty>
        ) : (
          <AccordionModelProviders
            searchTerm={searchTerm}
            filteredOptions={filteredOptions}
            handleSelection={handleSelection}
          />
        )}
      </Combobox.Dropdown>
    </Combobox>
  );
}

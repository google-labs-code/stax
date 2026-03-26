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

import {
  LOCAL_STORAGE_SHOW_CUSTOM_MODEL_MODAL,
  getProviders,
} from "@/config/constants";
import { useModelsContext } from "@/hooks/useModelsContext";
import { Model, ModelTypeEnum } from "@/queries/types";
import { ModelProvider } from "@/types";
import LocalStorage from "@/utils/LocalStorage";
import {
  Accordion,
  Group,
  HoverCard,
  ScrollArea,
  Stack,
  Text,
} from "@mantine/core";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";

interface AccordionModelProvidersProps {
  searchTerm?: string;
  filteredOptions: Model[];
  handleSelection: (value: string) => void;
}

interface AccordionModelProvider extends ModelProvider {
  providerModels?: Model[];
  noApiKey?: boolean;
}

export default function AccordionModelProviders({
  searchTerm,
  filteredOptions,
  handleSelection,
}: AccordionModelProvidersProps) {
  const { allModels } = useModelsContext();
  const [openedAccordion, setOpenedAccordion] = useState<string | null>(null);
  const router = useRouter();
  const [providers, setProviders] = useState<AccordionModelProvider[]>([]);

  useEffect(() => {
    if (searchTerm) {
      const matchingAccordion = getProviders().find(
        ({ key, accordionName }) => {
          if (accordionName === ModelTypeEnum.CUSTOM) {
            return [...allModels].some(
              (model) =>
                model.model_type !== ModelTypeEnum.SYSTEM &&
                model.label.toLowerCase().includes(searchTerm.toLowerCase()),
            );
          } else {
            return [...allModels].some(
              (model) =>
                model.provider.toLowerCase() === key.toLowerCase() &&
                model.model_type === ModelTypeEnum.SYSTEM &&
                model.is_api_key_present &&
                model.label.toLowerCase().includes(searchTerm.toLowerCase()),
            );
          }
        },
      );

      setOpenedAccordion(
        matchingAccordion ? matchingAccordion.accordionName : null,
      );
    } else {
      setOpenedAccordion(null);
    }
  }, [searchTerm, allModels]);

  useEffect(() => {
    const finalProviders = getProviders().map((provider) => {
      const providerModels = [...filteredOptions].filter((model) => {
        if (provider.accordionName === ModelTypeEnum.CUSTOM) {
          return model.model_type !== ModelTypeEnum.SYSTEM;
        }

        return (
          model.model_type === ModelTypeEnum.SYSTEM &&
          model.provider.toLowerCase() === provider.key.toLowerCase()
        );
      });

      return {
        ...provider,
        providerModels,
        noApiKey: providerModels.some((model) => !model.is_api_key_present),
      };
    });

    setProviders(finalProviders);
  }, [filteredOptions]);

  const getProviderIcon = useCallback(
    (providerName: string) => {
      const provider = providers.find(
        (p) => p.key.toLowerCase() === providerName.toLowerCase(),
      );

      return (
        provider?.icon ||
        providers.find((p) => p.key === ModelTypeEnum.CUSTOM)?.icon
      );
    },
    [providers],
  );

  const DisabledContentHoverCardDropdown = ({
    title,
    description,
    onClick,
    linkText,
  }: {
    title: string;
    description: string;
    onClick: () => void;
    linkText: string;
  }) => {
    return (
      <HoverCard.Dropdown>
        <div
          onMouseDown={(e) => e.stopPropagation()} // Avoid closing on mousedown
          onClick={(e) => e.stopPropagation()}
        >
          <Stack gap={4}>
            <p className="text-title-14">{title}</p>
            <p className="text-secondary text-body-14">{description}</p>
            <Group justify="flex-end">
              <Text
                onClick={onClick}
                className="cursor-pointer text-brand text-body-14"
              >
                {linkText}
              </Text>
            </Group>
          </Stack>
        </div>
      </HoverCard.Dropdown>
    );
  };

  return (
    <Accordion
      variant="separated"
      value={openedAccordion}
      onChange={setOpenedAccordion}
      classNames={{ label: "gap-8" }}
      data-testid="model-accordion"
    >
      {providers.map(
        ({ accordionName, icon: Icon, providerModels, noApiKey }) => {
          return (
            <Accordion.Item
              value={accordionName}
              className="m-0 !min-h-0 rounded-lg border-0 bg-neutrals-50 !p-0"
              key={accordionName}
            >
              <Accordion.Control
                className="!p-[16px]"
                classNames={{
                  label: "!p-0",
                }}
                disabled={providerModels?.length === 0 || noApiKey}
              >
                <div>
                  <HoverCard
                    key={accordionName}
                    width={200}
                    position="right"
                    shadow="md"
                    offset={25}
                    openDelay={200}
                    classNames={{
                      dropdown: "rounded-lg mt-[50px] p-[16px]",
                    }}
                    withinPortal={true}
                  >
                    <HoverCard.Target>
                      <div className="flex items-center gap-4 ">
                        {Icon}
                        <span>{accordionName}</span>
                      </div>
                    </HoverCard.Target>
                    {noApiKey && accordionName !== ModelTypeEnum.CUSTOM && (
                      <DisabledContentHoverCardDropdown
                        title="Missing API key."
                        description="Go to your settings to add API key to start using this model."
                        onClick={() => {
                          router.push("/settings?tab=apiKeys");
                        }}
                        linkText="Settings"
                      />
                    )}
                    {accordionName === ModelTypeEnum.CUSTOM &&
                      providerModels?.length === 0 && (
                        <DisabledContentHoverCardDropdown
                          title="Missing custom models."
                          description="Go to your settings to add custom models."
                          onClick={() => {
                            LocalStorage.set(
                              LOCAL_STORAGE_SHOW_CUSTOM_MODEL_MODAL,
                              true,
                            );
                            router.push("/settings?tab=modelManager");
                          }}
                          linkText="Add custom model"
                        />
                      )}
                  </HoverCard>
                </div>
              </Accordion.Control>
              <Accordion.Panel className="border-none !p-0">
                <ScrollArea.Autosize
                  classNames={{
                    viewport: "!p-0",
                    root: "md:max-h-[180px] 2xl:max-h-[300px]",
                  }}
                  type="always"
                  offsetScrollbars
                  scrollbarSize={6}
                >
                  {providerModels && providerModels.length > 0 ? (
                    providerModels.map((model, modelKey) => (
                      <HoverCard
                        key={model.id}
                        width={200}
                        position="right"
                        shadow="md"
                        offset={25}
                        openDelay={200}
                        classNames={{
                          dropdown: "rounded-lg mt-[50px] p-[16px]",
                        }}
                        withinPortal={true}
                      >
                        <HoverCard.Target>
                          <div
                            data-testid={`accordion-model-option-${modelKey}`}
                            onClick={() => handleSelection(model.id)}
                            className={`flex items-center gap-2 rounded-md p-2 p-[16px] ${accordionName === ModelTypeEnum.CUSTOM ? "pl-[20px]" : "pl-[40px]"} cursor-pointer hover:bg-gray-100`}
                          >
                            {accordionName === ModelTypeEnum.CUSTOM && (
                              <div className="mr-2">
                                {getProviderIcon(model.provider)}
                              </div>
                            )}
                            <Stack gap={4}>
                              <p className="text-title-14">{model.label}</p>
                              <p className="text-secondary text-body-12">
                                {model.name}
                              </p>
                            </Stack>
                          </div>
                        </HoverCard.Target>
                      </HoverCard>
                    ))
                  ) : (
                    <p className="text-secondary text-body-14">
                      No models available.
                    </p>
                  )}
                </ScrollArea.Autosize>
              </Accordion.Panel>
            </Accordion.Item>
          );
        },
      )}
    </Accordion>
  );
}

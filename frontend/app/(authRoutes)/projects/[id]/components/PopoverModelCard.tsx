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

import ModalCard from "@/components/ModalCard";
import AnthropicsAiIcon from "@/components/icons/AnthropicsAiIcon";
import GoogleIcon from "@/components/icons/GoogleIcon";
import MistralAiIcon from "@/components/icons/MistralAiIcon";
import OpenAiIconNew from "@/components/icons/OpenAiIconNew";
import { Provider } from "@/types";
import { formatModelName } from "@/utils/helpers";
import { Group, HoverCard, Stack, Text } from "@mantine/core";

import { WorkbookItem } from "../types";

type PopoverModelCardProps = {
  rowData: WorkbookItem;
};

export default function PopoverModelCard({ rowData }: PopoverModelCardProps) {
  function extractParameters(configString: string) {
    const configObject = JSON.parse(configString);
    const { seed, top_p, temperature, max_output_tokens } = configObject;

    return {
      seed,
      top_p,
      temperature,
      max_output_tokens,
    };
  }

  const params =
    rowData && rowData?.model_properties
      ? extractParameters(rowData?.model_properties)
      : null;

  return (
    <Stack gap={8}>
      <Text className="text-title-16">Model</Text>
      <ModalCard className="rounded-xl">
        <ModalCard className="min-h-[75px] max-w-[30%] rounded-lg bg-neutrals-100 px-[12px] py-[8px]">
          <Stack gap={8}>
            <Group gap={8}>
              {rowData?.model_provider === Provider.GOOGLE ? (
                <GoogleIcon data-testid="google-icon" />
              ) : rowData?.model_provider === Provider.MISTRAL ? (
                <MistralAiIcon data-testid="mistralai-icon" />
              ) : rowData?.model_provider === Provider.ANTHROPIC ? (
                <AnthropicsAiIcon data-testid="anthropicsai-icon" />
              ) : (
                <OpenAiIconNew data-testid="openai-icon" />
              )}
              <Text className="text-title-14">
                {formatModelName(rowData?.model_name || "")}
              </Text>
            </Group>

            <HoverCard
              width={280}
              position="bottom-start"
              shadow="md"
              openDelay={100}
              closeDelay={100}
            >
              <HoverCard.Target>
                <Text className="cursor-pointer text-brand decoration-brand text-body-11">
                  Show configurations
                </Text>
              </HoverCard.Target>
              <HoverCard.Dropdown className="rounded-xl">
                <Stack className="p-[24px]">
                  <Text className="text-title-16">Model Configurations</Text>
                  {params && (
                    <>
                      {params.temperature && (
                        <Group>
                          <Text className="text-body-14">Temperature:</Text>
                          <Text className="text-secondary text-body-14">
                            {[params.temperature]}
                          </Text>
                        </Group>
                      )}
                      {params.max_output_tokens && (
                        <Group>
                          <Text className="text-body-14">Max Tokens:</Text>
                          <Text className="text-secondary text-body-14">
                            {params.max_output_tokens}
                          </Text>
                        </Group>
                      )}
                      {params.top_p && (
                        <Group>
                          <Text className="text-body-14">Top P:</Text>
                          <Text className="text-secondary text-body-14">
                            {params.top_p}
                          </Text>
                        </Group>
                      )}

                      {params.seed && (
                        <Group>
                          <Text className="text-body-14">Seed:</Text>
                          <Text className="text-secondary text-body-14">
                            {params.seed}
                          </Text>
                        </Group>
                      )}
                    </>
                  )}
                  {!params && (
                    <Text size="sm" c="dimmed">
                      No configuration available
                    </Text>
                  )}
                </Stack>
              </HoverCard.Dropdown>
            </HoverCard>
          </Stack>
        </ModalCard>
      </ModalCard>
    </Stack>
  );
}

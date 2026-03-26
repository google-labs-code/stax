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

import { Model } from "@/queries/types";
import { Group, HoverCard, Stack, Text } from "@mantine/core";
import { ReactNode } from "react";

export default function GenerateOutputsModalDetails({
  model,
  children,
}: {
  model: Model;
  children: ReactNode;
}) {
  const { temperature, max_tokens, top_p, seed } = model.properties;

  return (
    <HoverCard
      width={280}
      position="bottom-start"
      shadow="md"
      openDelay={100}
      closeDelay={100}
    >
      <HoverCard.Target>{children}</HoverCard.Target>
      <HoverCard.Dropdown className="rounded-xl">
        <Stack className="p-[8px]">
          <Text className="text-title-16">Model Configurations</Text>
          <>
            {temperature && (
              <Group>
                <Text className="text-body-14">Temperature:</Text>
                <Text className="text-secondary text-body-14">
                  {[temperature]}
                </Text>
              </Group>
            )}
            {max_tokens && (
              <Group>
                <Text className="text-body-14">Max Tokens:</Text>
                <Text className="text-secondary text-body-14">
                  {max_tokens}
                </Text>
              </Group>
            )}
            {top_p && (
              <Group>
                <Text className="text-body-14">Top P:</Text>
                <Text className="text-secondary text-body-14">{top_p}</Text>
              </Group>
            )}

            {seed && (
              <Group>
                <Text className="text-body-14">Seed:</Text>
                <Text className="text-secondary text-body-14">{seed}</Text>
              </Group>
            )}
          </>
          {!model.properties && (
            <Text size="sm" c="dimmed">
              No configuration available
            </Text>
          )}
        </Stack>
      </HoverCard.Dropdown>
    </HoverCard>
  );
}

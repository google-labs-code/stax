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
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { Group, Text } from "@mantine/core";

export default function PromptCleaningInfoBar() {
  const { resetPlayground, setHumanEvalReady, setHasShownCleaningBar } =
    usePlaygroundContext();

  return (
    <Group className="bg-veryLightSilver w-full flex-row justify-between rounded-lg px-4 py-3 mb-3 h-[44px]">
      <Group gap={8}>
        <MaterialIcon
          name="info"
          size={20}
          className="cursor-default text-brand"
        />
        <Text className="text-secondaryDark text-body-14">
          Your prompt has been saved to your project.
        </Text>
      </Group>
      <Group gap={8}>
        <Text
          className="cursor-pointer text-brand text-title-14"
          onClick={() => resetPlayground()}
        >
          Add another prompt
        </Text>
        <MaterialIcon
          name="close"
          size={20}
          className="text-secondaryDark cursor-pointer"
          onClick={() => {
            setHumanEvalReady(false);
            setHasShownCleaningBar(true);
          }}
        />
      </Group>
    </Group>
  );
}

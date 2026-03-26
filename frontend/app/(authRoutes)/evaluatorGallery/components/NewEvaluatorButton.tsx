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
import { Button, Menu, Text } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";

import NewEvaluatorModal from "./NewEvaluatorModal";

export default function NewEvaluatorButton() {
  const [
    isNewLLMEvaluatorModalOpened,
    { open: openNewLLMEvaluatorModal, close: closeNewLLMEvaluatorModal },
  ] = useDisclosure(false);

  return (
    <Menu
      shadow="sm"
      position="bottom-end"
      width={185}
      withinPortal={false}
      classNames={{
        dropdown: "rounded-sm shadow-sm top-20",
        item: "gap-lg p-[12px]",
      }}
    >
      <NewEvaluatorModal
        isOpened={isNewLLMEvaluatorModalOpened}
        onClose={closeNewLLMEvaluatorModal}
      />

      <Button
        className="min-w-[120px] h-9 !rounded-sm pl-3 pr-2"
        leftSection={<MaterialIcon name="add" size={16} />}
        onClick={openNewLLMEvaluatorModal}
        variant="filled"
      >
        <Text className="text-title-12 whitespace-nowrap">Add evaluator</Text>
      </Button>
    </Menu>
  );
}

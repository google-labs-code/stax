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
import ModalCard from "@/components/ModalCard";
import { Accordion, Button, Group, Stack, Textarea } from "@mantine/core";

import CreateDatasetHeader from "./Header";

export default function DatasetAccordion() {
  const menuOptions = [
    {
      id: "define-variables",
      value: (
        <CreateDatasetHeader
          label="Define Variables (optional)"
          tooltip="Upload or add variable definitions in csv format. Optionally include expected output."
        />
      ),
      content: (
        <ModalCard>
          <Stack gap={8}>
            <Stack className="mx-[2px]" gap={8}>
              <Textarea
                radius="md"
                classNames={{
                  input:
                    "border-neutrals-300 placeholder:text-title-14 placeholder:!text-resting min-h-[160px]",
                }}
                placeholder={`// Example format to define variables: \n{latitude,longitude,expected output\n40.70555556,-73.99638889,Brooklyn Bridge\n41.89,12.49222222,Colosseum\n-12.83666667,-72.54555556,Machu Picchu\n27.98805556,86.92527778,Mount Everest}`}
                autosize
              />
              <Group justify="end">
                <Button
                  variant="default"
                  leftSection={<MaterialIcon name="cloud_upload" size={18} />}
                >
                  Upload CSV
                </Button>
              </Group>
            </Stack>
          </Stack>
        </ModalCard>
      ),
    },
  ];

  const items = menuOptions.map((item) => (
    <Accordion.Item key={item.id} value={item.id}>
      <Accordion.Control>{item.value}</Accordion.Control>
      <Accordion.Panel className="!py-0">{item.content}</Accordion.Panel>
    </Accordion.Item>
  ));

  return <Accordion variant="separate">{items}</Accordion>;
}

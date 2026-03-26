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
import { EvaluatorFormData } from "@/queries/types";
import { ProjectType } from "@/types";
import { Button, Group, Stack, Text } from "@mantine/core";
import { useParams } from "next/navigation";
import { Dispatch, SetStateAction } from "react";

import { EvaluatorPageAction } from "../../types";
import MetricPrompt from "./MetricPrompt";
import SectionHeader from "./SectionHeader";

type MetricPromptsType = {
  data: EvaluatorFormData;
  setData: Dispatch<SetStateAction<EvaluatorFormData>>;
};

export default function MetricPrompts({ data, setData }: MetricPromptsType) {
  const navParams = useParams();

  return (
    <Group
      gap="24px"
      className="flex flex-col items-start self-stretch"
      data-testid="metric-prompts"
    >
      <SectionHeader
        title="Map Evaluator Score to Analytics"
        description="Define how rubric scores from the evaluator prompt are graded and colored in analytics."
      />
      <Group gap="24px" className="flex w-[100%] flex-col">
        <Group gap="8px" className="flex w-[100%] flex-col">
          <Group className="gap-xl flex w-[100%] flex-row flex-nowrap self-stretch">
            <Group gap="4px" className="flex w-[32%] flex-row">
              <Text className="!text-secondaryDark !font-medium text-body-14">
                Rubric category
              </Text>
              <MaterialIcon
                name="info"
                className="cursor-pointer text-secondary"
                tooltipClassName="min-w-[350px]"
                tooltipLabel="Enter the exact category from the Rating Rubric that your evaluator will output as the first line."
                size={18}
              />
            </Group>
            <Group gap="4px" className="flex w-[32%] flex-row">
              <Text className="!text-secondaryDark !font-medium text-body-14">
                Score mapping
              </Text>
              <MaterialIcon
                name="info"
                className="cursor-pointer text-secondary"
                tooltipClassName="min-w-[350px]"
                tooltipLabel="Assign a score to each category your evaluator will output. Scores should be on a scale of 0-1."
                size={18}
              />
            </Group>
            <Group gap="4px" className="flex w-[32%] flex-row">
              <Text className="!text-secondaryDark !font-medium text-body-14">
                Score color
              </Text>
              <MaterialIcon
                name="info"
                className="cursor-pointer text-secondary"
                tooltipClassName="min-w-[350px]"
                tooltipLabel="Select a color to represent this category in Projects and Analytics."
                size={18}
              />
            </Group>
          </Group>
          <Stack gap={0} className="gap-xl flex w-[100%] flex-col">
            {data[ProjectType.POINTWISE].output_categories.map(
              (category, key) => (
                <MetricPrompt
                  category={category}
                  categoryKey={key}
                  key={key}
                  data={data}
                  setData={setData}
                />
              ),
            )}
          </Stack>
        </Group>

        {navParams?.action !== EvaluatorPageAction.VIEW && (
          <Stack className="flex w-[100%] flex-row">
            <Button
              variant="outlineLight"
              className="w-min-[120px] text-secondaryDark !px-[16px] !py-[8px] !font-medium !text-body-12"
              onClick={() => {
                setData((prevData) => {
                  return {
                    ...prevData,
                    [ProjectType.POINTWISE]: {
                      ...prevData[ProjectType.POINTWISE],
                      output_categories: [
                        ...prevData[ProjectType.POINTWISE].output_categories,
                        {
                          name: "",
                          value: "",
                          color: "",
                          color_name: "",
                        },
                      ],
                    },
                  };
                });
              }}
            >
              <MaterialIcon name="add" />
              Add score
            </Button>
          </Stack>
        )}
      </Group>
    </Group>
  );
}

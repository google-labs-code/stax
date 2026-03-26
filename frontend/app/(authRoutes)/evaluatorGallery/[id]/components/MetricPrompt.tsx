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
import { EvaluatorFormData, LLMEvaluatorOutputCategory } from "@/queries/types";
import { useProcessedColor } from "@/utils/colorUtils";
import { Group, Select, Text, TextInput, UnstyledButton } from "@mantine/core";
import { useParams } from "next/navigation";
import { Dispatch, SetStateAction } from "react";

import { EvaluatorPageAction } from "../../types";
import { MetricPromptColors } from "../config/MetricPromptColors";

type MetricPromptType = {
  category: LLMEvaluatorOutputCategory;
  data: EvaluatorFormData;
  setData: Dispatch<SetStateAction<EvaluatorFormData>>;
  categoryKey: number;
};

export default function MetricPrompt({
  category,
  setData,
  categoryKey,
  data,
}: MetricPromptType) {
  const navParams = useParams();
  const isReadOnly = navParams?.action === EvaluatorPageAction.VIEW;

  const updateCategoryData = (
    newValue: string,
    key: keyof LLMEvaluatorOutputCategory,
  ) => {
    setData((prevData) => {
      const updatedCategories = [
        ...prevData[prevData.selectedType].output_categories,
      ];
      updatedCategories[categoryKey] = {
        ...updatedCategories[categoryKey],
        [key]: newValue,
      };

      return {
        ...prevData,
        [prevData.selectedType]: {
          ...prevData[prevData.selectedType],
          output_categories: updatedCategories,
        },
      };
    });
  };

  const selectedColorFromPalette = MetricPromptColors.find(
    (c) => c.value === category.color,
  );

  const { backgroundColor } = useProcessedColor(
    category.color,
    "var(--color-very-light-silver)",
    "var(--color-very-light-silver)",
  );

  return (
    <Group className="flex w-[100%] flex-row flex-nowrap gap-xl self-stretch">
      <Group gap="4px" className="flex w-[32%] flex-row">
        <TextInput
          classNames={{
            root: "w-[100%]",
            wrapper: isReadOnly ? "!bg-veryLightSilver" : "",
            input: isReadOnly
              ? "!bg-veryLightSilver !text-secondary border-default"
              : "",
          }}
          value={category.name}
          disabled={isReadOnly}
          onChange={(evt) => updateCategoryData(evt.target.value, "name")}
        />
      </Group>
      <Group gap="4px" className="flex w-[32%] flex-row flex-col">
        <TextInput
          classNames={{
            root: "w-[100%]",
            wrapper: isReadOnly ? "!bg-veryLightSilver" : "",
            input: (() => {
              const numValue = parseFloat(category.value);
              if (
                category.value &&
                (isNaN(numValue) || numValue < 0 || numValue > 1)
              ) {
                return "border-supporting-red border-2";
              }

              return isReadOnly
                ? "!bg-veryLightSilver !text-secondary border-default"
                : "";
            })(),
          }}
          value={category.value}
          disabled={isReadOnly}
          onChange={(evt) => {
            const val = evt.target.value;
            if (/^\d*(\.\d*)?$/.test(val)) {
              updateCategoryData(val, "value");
            }
          }}
        />
        {category.value &&
          (() => {
            const numValue = parseFloat(category.value);
            if (isNaN(numValue) || numValue < 0 || numValue > 1) {
              return (
                <Text className="text-supporting-red text-body-12 self-start">
                  Scores should be on a scale of 0-1.
                </Text>
              );
            }

            return null;
          })()}
      </Group>
      <Group
        gap="4px"
        className="flex w-[32%] flex-row flex-nowrap items-center"
      >
        <div className="relative w-[calc(100%-50px)]">
          {isReadOnly ? (
            <div className="flex h-[40px] w-full items-center rounded-sm bg-veryLightSilver px-3 border-default">
              <div
                className="h-5 w-5 min-w-[20px] rounded-full mr-2"
                style={{ backgroundColor }}
              />
              <span className="truncate text-secondary max-w-[150px]">
                {selectedColorFromPalette?.label ||
                  category.color_name ||
                  "Select color"}
              </span>
            </div>
          ) : (
            <div className="relative">
              <div className="w-full">
                <Select
                  data={MetricPromptColors}
                  allowDeselect={false}
                  value={category.color}
                  placeholder=" "
                  classNames={{
                    root: "w-full",
                    input:
                      "h-[40px] rounded-sm pl-[50px] border-borderColor text-transparent caret-transparent",
                    dropdown: "rounded-sm border-borderColor",
                    option: "py-[8px]",
                  }}
                  renderOption={({ option }) => {
                    const { backgroundColor: optionBg } = useProcessedColor(
                      option.value,
                    );

                    return (
                      <Group gap="xs">
                        <div
                          className="h-5 w-5 min-w-[20px] rounded-full mr-2"
                          style={{ backgroundColor: optionBg }}
                        />
                        <span className="text-title-14">{option.label}</span>
                      </Group>
                    );
                  }}
                  onChange={(value) => {
                    if (value) {
                      updateCategoryData(value, "color");
                      const colorObj = MetricPromptColors.find(
                        (c) => c.value === value,
                      );
                      if (colorObj?.label) {
                        updateCategoryData(colorObj.label, "color_name");
                      }
                    }
                  }}
                />
              </div>

              <div className="pointer-events-none absolute left-3 top-1/2 flex -translate-y-1/2 items-center">
                <div
                  className="h-5 w-5 min-w-[20px] rounded-full mr-2"
                  style={{ backgroundColor }}
                />
                <span className="max-w-[150px] truncate text-primary text-title-14">
                  {selectedColorFromPalette?.label ||
                    category.color_name ||
                    "Select color"}
                </span>
              </div>
            </div>
          )}
        </div>

        {!isReadOnly && (
          <UnstyledButton
            className="ml-[16px] h-[24px] w-[24px]"
            onClick={() => {
              const outputCategories = data[
                data?.selectedType
              ]?.output_categories.filter((_, index) => index !== categoryKey);

              setData((prevData) => ({
                ...prevData,
                [prevData.selectedType]: {
                  ...prevData[prevData.selectedType],
                  output_categories: [...outputCategories],
                },
              }));
            }}
          >
            <MaterialIcon
              name="delete"
              size={20}
              className="justify-self-end text-secondaryDark"
            />
          </UnstyledButton>
        )}
      </Group>
    </Group>
  );
}

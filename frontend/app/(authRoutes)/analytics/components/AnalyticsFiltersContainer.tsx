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

import Card from "@/components/Card";
import CustomSelectOption from "@/components/CustomSelectOption";
import MaterialIcon from "@/components/MaterialIcon";
import ModelCombobox from "@/components/ModelComboBox";
import { FilterType, useAnalyticsContext } from "@/hooks/useAnalyticsContext";
import { useModelsContext } from "@/hooks/useModelsContext";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import { Loader, Select, Text } from "@mantine/core";
import { useCallback, useMemo } from "react";

import { FILTER_LABELS } from "../types/charts";
import "./../styles/main.scss";
import TagsSelect from "./TagsSelect";

export default function AnalyticsFiltersContainer() {
  const { selectedFilters, setSelectedFilters } = useAnalyticsContext();

  const { isLoadingModels } = useModelsContext();
  const { allProjects, isLoadingProjects } = useProjectsContext();

  const allProjectsNames = useMemo(
    () =>
      allProjects
        .map(({ name, project_id }) => ({ label: name, value: project_id }))
        .sort((a, b) => a.label.localeCompare(b.label)),
    [allProjects],
  );

  const removeFilterGroupAtIndex = useCallback(
    (index: number) =>
      setSelectedFilters((prevFilters) =>
        prevFilters.filter((_, ind) => ind !== index),
      ),
    [selectedFilters, setSelectedFilters],
  );

  const updateFilterProperty = useCallback(
    (index: number, propertyName: string, value: unknown) => {
      setSelectedFilters((prevFilters: FilterType[]) =>
        prevFilters.map((filter, ind) =>
          ind === index ? { ...filter, [propertyName]: value } : filter,
        ),
      );
    },
    [selectedFilters, setSelectedFilters],
  );

  return (
    <div className="gap-xl flex flex-1 flex-col">
      {selectedFilters.map((selectedFilter, index) => (
        <div key={index} className="flex flex-1 flex-col gap-4">
          <Text className="text-secondaryDark">{FILTER_LABELS[index]}</Text>
          <Card
            padding="sm"
            className="bg-veryLightSilver flex flex-row items-center justify-between gap-3 rounded-md border-none shadow-none"
          >
            <Select
              clearable
              className="w-[33%]"
              placeholder={isLoadingProjects ? "Loading..." : "Select Project"}
              data={allProjectsNames}
              value={selectedFilter.project}
              onChange={(value) =>
                updateFilterProperty(index, "project", value)
              }
              renderOption={(option) => <CustomSelectOption option={option} />}
              rightSection={
                isLoadingProjects ? (
                  <Loader size="xs" />
                ) : selectedFilter.project ? (
                  <MaterialIcon
                    name="close"
                    className="!cursor-pointer !font-light"
                    onClick={() => updateFilterProperty(index, "project", null)}
                    dataTestId="material-icon-close"
                  />
                ) : (
                  <MaterialIcon
                    name="keyboard_arrow_down"
                    dataTestId="material-icon-keyboard_arrow_down"
                  />
                )
              }
              classNames={{
                input:
                  "h-[40px] border border-neutrals-300 rounded-sm text-title-14 placeholder-resting",
                option: "px-1",
              }}
              searchable
            />

            <div className="w-[33%]">
              <ModelCombobox
                openedModel={selectedFilter.model}
                isLoadingModels={isLoadingModels}
                onSelect={(value) =>
                  updateFilterProperty(index, "model", value)
                }
                isExpanded={false}
                className="!h-[40px] !rounded-sm !bg-white"
                clearable
                inputClassName="bg-transparent"
              />
            </div>

            <div className="w-[33%] overflow-hidden">
              <TagsSelect
                selectedTags={selectedFilter.tags}
                setSelectedTags={(value) =>
                  updateFilterProperty(index, "tags", value)
                }
              />
            </div>

            {selectedFilters.length > 1 ? (
              <MaterialIcon
                name="cancel"
                className="text-secondaryDark cursor-pointer !font-light"
                onClick={() => removeFilterGroupAtIndex(index)}
                dataTestId="material-icon-cancel"
              />
            ) : (
              <MaterialIcon
                name="add_circle"
                className="text-secondaryDark cursor-pointer !font-light"
                onClick={() =>
                  setSelectedFilters([
                    ...selectedFilters,
                    {
                      project: "",
                      model: null,
                      tags: [],
                    },
                  ])
                }
                dataTestId="material-icon-add_circle"
              />
            )}
          </Card>
        </div>
      ))}
    </div>
  );
}

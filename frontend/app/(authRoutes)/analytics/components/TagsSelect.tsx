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
import { useTagsContext } from "@/hooks/useTagsContext";
import {
  Combobox,
  Group,
  Loader,
  Pill,
  PillsInput,
  Text,
  useCombobox,
} from "@mantine/core";
import { useState } from "react";

import { TagsSelectProps } from "./types";

export default function TagsSelect({
  selectedTags,
  setSelectedTags,
}: TagsSelectProps) {
  const { userTags, isLoadingTags } = useTagsContext();
  const combobox = useCombobox({
    onDropdownClose: () => combobox.resetSelectedOption(),
  });
  const [search, setSearch] = useState("");

  const handleTagSelect = (value: string) =>
    setSelectedTags(
      selectedTags.includes(value)
        ? selectedTags.filter((v: string) => v !== value)
        : [...selectedTags, value],
    );

  const handleTagRemove = (value: string) =>
    setSelectedTags(selectedTags.filter((v) => v !== value));

  const options = userTags
    .filter((tag) =>
      tag.name.toLowerCase().includes(search.trim().toLowerCase()),
    )
    .map(
      (tag, key) =>
        tag.id && (
          <Combobox.Option
            value={tag.id}
            key={key}
            active={selectedTags.includes(tag.id)}
          >
            <Group gap={4}>
              <MaterialIcon
                name="check"
                className={`!text-[16px] ${!selectedTags.includes(tag.id) && "text-transparent"}`}
              />
              <Text className="overflow-hidden truncate whitespace-nowrap text-title-12">
                {tag.name}
              </Text>
            </Group>
          </Combobox.Option>
        ),
    );

  const pills = selectedTags.map((tagId, key) => {
    const tagName = userTags.find((t) => t.id === tagId)?.name;

    return (
      <Pill
        key={key}
        data-testid={`tag-${tagId}`}
        className="border-resting rounded-sm border-[1px] border-solid bg-white uppercase text-secondary"
        withRemoveButton
        onRemove={() => handleTagRemove(tagId)}
      >
        {tagName}
      </Pill>
    );
  });

  return (
    <Combobox store={combobox} onOptionSubmit={handleTagSelect}>
      <Combobox.DropdownTarget>
        <PillsInput
          data-testid="tags-select"
          onClick={() => combobox.openDropdown()}
          rightSection={search === "" && isLoadingTags && <Loader size="xs" />}
          classNames={{
            input: "border border-neutrals-300 rounded-sm h-[40px]",
          }}
        >
          <Pill.Group className="scroll-hidden flex h-[100%] flex-nowrap items-center gap-2 overflow-y-scroll">
            {pills}

            <Combobox.EventsTarget>
              <PillsInput.Field
                onFocus={() => combobox.openDropdown()}
                onBlur={() => combobox.closeDropdown()}
                value={search}
                placeholder="Add tags"
                classNames={{
                  field:
                    "text-title-14 placeholder-resting h-[36px] flex overflow-hidden ",
                }}
                onChange={(event) => {
                  combobox.updateSelectedOptionIndex();
                  setSearch(event.currentTarget.value);
                }}
                onKeyDown={(event) => {
                  if (
                    event.key === "Backspace" &&
                    search.length === 0 &&
                    selectedTags.length > 0
                  ) {
                    event.preventDefault();
                    handleTagRemove(selectedTags[selectedTags.length - 1]);
                  }
                }}
              />
            </Combobox.EventsTarget>
          </Pill.Group>
        </PillsInput>
      </Combobox.DropdownTarget>

      <Combobox.Dropdown>
        <Combobox.Options classNames={{ options: "" }}>
          {options.length > 0 ? (
            options
          ) : isLoadingTags ? (
            <Combobox.Empty>
              <p className="p-[16px] text-title-14">Loading...</p>
            </Combobox.Empty>
          ) : (
            <Combobox.Empty>No tags available.</Combobox.Empty>
          )}
        </Combobox.Options>
      </Combobox.Dropdown>
    </Combobox>
  );
}

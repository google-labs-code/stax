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
import {
  Group,
  Input,
  Popover,
  PopoverDropdown,
  PopoverTarget,
  Switch,
  Text,
  UnstyledButton,
} from "@mantine/core";
import { useInputState } from "@mantine/hooks";
import { useCallback, useEffect, useState } from "react";

import { PopoverEvaluatorMenuProps } from "./types";

export default function PopoverEvaluatorMenu({
  selectedEvals,
  setSelectedEvals,
}: PopoverEvaluatorMenuProps) {
  const [isSearchFocused, setIsSearchFocused] = useState<boolean>(false);
  const [searchValue, setSearchValue] = useInputState("");

  const onSelectEval = useCallback((score: string) => {
    setSelectedEvals((prev) =>
      prev.map((item) =>
        item.score === score ? { ...item, checked: !item.checked } : item,
      ),
    );
  }, []);

  const onDeleteAllEvals = useCallback(() => {
    const currentSelectedEvals = selectedEvals.map((e) => ({
      score: e.score,
      checked: false,
      visible: true,
    }));
    setSelectedEvals(currentSelectedEvals);
    setSearchValue("");
  }, [selectedEvals]);

  useEffect(() => {
    if (searchValue !== "") {
      const currentFilteredEvals = selectedEvals.map((e) => ({
        ...e,
        visible: e.score.toLowerCase().includes(searchValue.toLowerCase()),
      }));
      setSelectedEvals(currentFilteredEvals);
    } else {
      const currentFilteredEvals = selectedEvals.map((e) => ({
        ...e,
        visible: true,
      }));
      setSelectedEvals(currentFilteredEvals);
    }
  }, [searchValue]);

  return (
    <Popover position="bottom-start">
      <PopoverTarget>
        <UnstyledButton
          disabled={selectedEvals.length === 0}
          variant="subtle"
          className={`flex items-center justify-center gap-sm ${selectedEvals.length === 0 && "cursor-not-allowed text-secondary"}`}
        >
          <MaterialIcon
            name="filter_alt"
            className="text-secondaryDark"
            size={20}
          />
          <Text className="text-secondaryDark text-title-12">
            View evaluator
          </Text>
        </UnstyledButton>
      </PopoverTarget>
      <PopoverDropdown className="min-w-[200px] rounded-lg !p-0">
        <Input
          value={isSearchFocused ? searchValue : "Search"}
          variant={isSearchFocused ? "default" : "unstyled"}
          onChange={setSearchValue}
          leftSection={
            !isSearchFocused && (
              <MaterialIcon
                name="search"
                className="pl-4 pr-3 pt-4 text-secondaryDark"
                size={20}
              />
            )
          }
          rightSection={
            isSearchFocused && (
              <MaterialIcon
                name="search"
                className="pl-4 pr-3 pt-4 text-secondaryDark"
                size={20}
              />
            )
          }
          size="sm"
          classNames={{
            wrapper: `h-[44px] p-[12px]`,
            input: `transition-all duration-300 ease-in-out rounded-xl h-[16px] pb-0 w-[170px] ${isSearchFocused && "pl-[30px]"}`,
          }}
          onFocus={() => setIsSearchFocused(true)}
          onBlur={() => {
            if (!searchValue) {
              setIsSearchFocused(false);
            }
          }}
        />
        <UnstyledButton
          onClick={onDeleteAllEvals}
          className="flex w-[100%] flex-row items-center gap-lg rounded-tl-[16px] rounded-tr-[16px] border-0 p-[12px] border-b-default"
        >
          <MaterialIcon
            name="visibility_off"
            size={20}
            className="text-secondary"
          />
          <Text className="text-body-14">Hide All</Text>
        </UnstyledButton>

        {selectedEvals
          .filter((e) => e.visible)
          .map((selectedEval, key) => (
            <Group
              key={key}
              className="flex flex-row items-center gap-lg p-[12px]"
            >
              <Switch
                onClick={() => onSelectEval(selectedEval.score)}
                checked={selectedEval.checked}
                size="xs"
                value={selectedEval.score}
                className="w-[26px] min-w-[26px]"
              />
              <Text className="text-body-14">{selectedEval.score}</Text>
            </Group>
          ))}
      </PopoverDropdown>
    </Popover>
  );
}

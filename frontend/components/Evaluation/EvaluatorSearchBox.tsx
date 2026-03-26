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

import NewEvaluatorButton from "@/app/(authRoutes)/evaluatorGallery/components/NewEvaluatorButton";
import { EvaluatorCardItem } from "@/types";
import { Combobox, Group, Text, useCombobox } from "@mantine/core";
import { Dispatch, SetStateAction, useRef, useState } from "react";

import MaterialIcon from "../MaterialIcon";

type selectedCardsProps = {
  id: string;
  title: string;
};

type SearchComboProps = {
  data: EvaluatorCardItem[];
  setData: Dispatch<SetStateAction<EvaluatorCardItem[]>>;
  selectedCards: selectedCardsProps[];
  isPage?: boolean;
  setSelectedCards: Dispatch<SetStateAction<selectedCardsProps[]>>;
  onSelect: (
    id: string,
    title: string,
    setSelectedCards: Dispatch<SetStateAction<selectedCardsProps[]>>,
  ) => void;
  initialData: EvaluatorCardItem[];
};

export default function EvaluatorSearchBox({
  isPage,
  selectedCards,
  setSelectedCards,
  onSelect,
  data,
  setData,
  initialData,
}: SearchComboProps) {
  const [searchTerm, setSearchTerm] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  const handlePageEvaluatorsSearch = (
    e: React.ChangeEvent<HTMLInputElement>,
  ) => {
    setSearchTerm(e.target.value);
    if (e.target.value === "") {
      setData([...initialData]);
    } else {
      setData(
        [...initialData].filter((item) =>
          item.name.toLowerCase().includes(e.target.value.toLowerCase().trim()),
        ),
      );
    }
  };

  const combobox = useCombobox({
    onDropdownClose: () => {
      setSearchTerm("");
    },
  });

  const options: selectedCardsProps[] = data.map((item) => {
    return {
      ...item,
      title: item.name,
    };
  });

  const filteredOptions = options.filter(
    (item) =>
      !selectedCards.some((selected) => selected.id === item.id) &&
      item.title.toLowerCase().includes(searchTerm.toLowerCase().trim()),
  );

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
    combobox.openDropdown();
  };

  const handleInputFocus = () => {
    combobox.openDropdown();
  };

  if (isPage) {
    return (
      <Group justify="space-between">
        <div className="flex w-[240px] h-[36px] bg-neutrals-50 cursor-text items-center rounded-sm border border-solid border-borderColor">
          <MaterialIcon
            name="search"
            className="ml-2 flex-shrink-0 text-secondary"
            size={18}
          />
          <input
            ref={inputRef}
            className="h-full w-full border-none bg-transparent outline-none text-title-14 px-2 placeholder-resting"
            placeholder="Search Evaluator"
            value={searchTerm}
            onChange={handlePageEvaluatorsSearch}
          />
        </div>

        <Group gap="xl" className="mr-5">
          <NewEvaluatorButton />
        </Group>
      </Group>
    );
  }

  return (
    <Group justify="space-between">
      <Combobox
        store={combobox}
        shadow="md"
        onOptionSubmit={(titleValue) => {
          const selectedCard = options.find(
            (item) => item.title === titleValue,
          );
          if (selectedCard) {
            onSelect(selectedCard.id, selectedCard.title, setSelectedCards);
          }
          setSearchTerm("");
        }}
      >
        <Combobox.Target>
          <div className="flex w-[240px] h-[36px] cursor-text items-center rounded-sm border border-solid border-borderColor">
            <MaterialIcon
              name="search"
              className="ml-2 flex-shrink-0 text-secondary"
              size={18}
            />
            <input
              ref={inputRef}
              className="h-full w-full border-none bg-transparent outline-none text-title-14 px-2 placeholder-resting"
              placeholder="Search Evaluator"
              value={searchTerm}
              onChange={handleInputChange}
              onFocus={handleInputFocus}
            />
          </div>
        </Combobox.Target>

        <Combobox.Dropdown className="rounded-lg px-4 py-2">
          <Combobox.Options>
            {filteredOptions.length > 0 ? (
              filteredOptions.map((item) => (
                <Combobox.Option key={item.id} value={item.title}>
                  <Text className="text-title-14">{item.title}</Text>
                </Combobox.Option>
              ))
            ) : (
              <Combobox.Empty>No scores found</Combobox.Empty>
            )}
          </Combobox.Options>
        </Combobox.Dropdown>
      </Combobox>
    </Group>
  );
}

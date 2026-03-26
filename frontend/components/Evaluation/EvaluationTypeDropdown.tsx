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
import { EvaluatorCardItem, ProjectType } from "@/types";
import { Menu, UnstyledButton } from "@mantine/core";
import { useCallback, useEffect, useMemo, useState } from "react";

interface EvaluationTypeDropdownProps {
  card: EvaluatorCardItem;
  isCardChecked: boolean;
}

type EvaluationTypeOption = {
  label: string;
  value: ProjectType;
};

export default function EvaluationTypeDropdown({
  card,
  isCardChecked,
}: EvaluationTypeDropdownProps) {
  const evaluationTypeOptions: EvaluationTypeOption[] = [
    { label: "POINTWISE", value: ProjectType.POINTWISE },
    {
      label: "SIDE-BY-SIDE",
      value: ProjectType.SIDE_BY_SIDE,
    },
  ];
  const [options, setOptions] = useState<EvaluationTypeOption[]>([]);

  const defaultValue = useMemo(() => {
    const selectedCard = card?.evaluationTypes?.find(
      (evaluator) => evaluator.id === card?.id,
    );

    if (selectedCard) {
      return evaluationTypeOptions.find(
        (option) => option.value === selectedCard.type,
      );
    }

    return evaluationTypeOptions[0];
  }, []);

  const [selectedOption, setSelectedOption] = useState<EvaluationTypeOption>(
    defaultValue || evaluationTypeOptions[0],
  );

  useEffect(() => {
    const evaluationTypes = card?.evaluationTypes?.map(
      (evaluator) => evaluator.type,
    ) || [ProjectType.POINTWISE];

    const finalOptions = [];
    if (evaluationTypes.includes(ProjectType.POINTWISE)) {
      finalOptions.push({ ...evaluationTypeOptions[0] });
    }
    if (evaluationTypes.includes(ProjectType.SIDE_BY_SIDE)) {
      finalOptions.push({ ...evaluationTypeOptions[1] });
    }

    setOptions([...finalOptions]);
  }, [card?.evaluationTypes]);

  const [opened, setOpened] = useState(false);

  const handleSelect = useCallback(
    (option: EvaluationTypeOption) => {
      const selectedEvaluatorType = card?.evaluationTypes?.find(
        (evaluator) => evaluator.type === option?.value,
      );
      if (selectedEvaluatorType) {
        card.id = selectedEvaluatorType.id;
      }

      setSelectedOption(option);
      setOpened(false);
    },
    [card?.evaluationTypes],
  );

  return (
    <Menu
      opened={opened}
      onChange={() => {
        if (isCardChecked) {
          return;
        }

        setOpened(!opened);
      }}
      width={200}
      position="bottom-start"
      shadow="md"
      classNames={{
        dropdown: "!border-0 !rounded-sm p-1",
      }}
      disabled={isCardChecked}
    >
      <Menu.Target>
        <UnstyledButton
          className={`h-6 min-h-6 ${options.length > 1 ? "w-[105px]" : "w-auto"} pl-2 pr-[6px] py-0 bg-veryLightSilver rounded-sm flex items-center justify-between`}
        >
          <span className="text-title-11 text-secondary uppercase truncate font-normal">
            {selectedOption?.label}
          </span>
          {options.length > 1 && (
            <MaterialIcon
              name="keyboard_arrow_down"
              className="text-secondary"
              size={16}
            />
          )}
        </UnstyledButton>
      </Menu.Target>

      <Menu.Dropdown>
        {options.map((option, key) => (
          <Menu.Item
            key={key}
            onClick={() => handleSelect(option)}
            className={`uppercase text-xs py-1.5 px-3 ${
              option.value === selectedOption?.value ? "bg-gray-50" : ""
            }`}
          >
            {option.label}
          </Menu.Item>
        ))}
      </Menu.Dropdown>
    </Menu>
  );
}

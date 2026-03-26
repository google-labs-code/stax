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

import { NumberInput, Slider, Text } from "@mantine/core";

type SliderNumberInputProps = {
  value?: number;
  setValue: (value: number | undefined) => void;
  onBlur?: () => void;
  min: number;
  max: number;
  placeholder?: string;
  step: number;
  allowDecimal?: boolean;
  decimalScale?: number;
  error?: string;
  disabled?: boolean;
};

export default function SliderNumberInput(props: SliderNumberInputProps) {
  const {
    value,
    setValue,
    min,
    max,
    step,
    allowDecimal,
    decimalScale,
    placeholder,
    error,
    disabled,
    onBlur,
    ...formProps
  } = props;

  const handleChange = (val: number | string) => {
    setValue(val as number);
  };

  return (
    <div className="relative mb-[4px] w-[427px] flex-col">
      <div
        className={`relative flex w-[100%] items-center justify-between gap-4`}
      >
        <Slider
          size="xs"
          disabled={disabled}
          className="flex-1"
          thumbSize="20px"
          min={min}
          max={max}
          step={step}
          value={value ?? 0}
          color={error && "red"}
          classNames={{
            thumb: "border-[10px]",
          }}
          {...formProps}
          onChange={handleChange}
        />
        <NumberInput
          disabled={disabled}
          hideControls
          placeholder={placeholder}
          classNames={{
            input: `w-[56px] h-[40px] rounded-lg text-center ${
              error ? "border-supporting-red" : "border-neutrals-300"
            }`,
          }}
          clampBehavior="strict"
          allowDecimal={allowDecimal}
          decimalScale={decimalScale}
          {...formProps}
          min={min}
          max={max}
          value={value}
          onChange={handleChange}
          onBlur={onBlur}
        />
      </div>
      {error && (
        <Text className="absolute bottom-[-15px] left-[5px] px-[10px] text-supporting-red text-body-10">
          {error}
        </Text>
      )}
    </div>
  );
}

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

import dayjs, { Dayjs } from "@/utils/dayjsSetup";
import {
  ActionIcon,
  Button,
  CloseButton,
  Combobox,
  Divider,
  Group,
  Input,
  InputBase,
  Text,
  UnstyledButton,
  useCombobox,
} from "@mantine/core";
import { Calendar, TimeInput } from "@mantine/dates";
import { useEffect, useRef, useState } from "react";

import MaterialIcon from "./MaterialIcon";

const DATE_TIME_FORMAT = "MMM D, YYYY h:mm A";

type TimeObject = {
  hour: number;
  minute: number;
};

type DateRangeProps = {
  onChange: (value: Dayjs[], label?: string) => void;
  hideFilter?: () => void;
  unstyled?: boolean;
  position?: string;
  defaultValue?: string;
  defaultRange?: Dayjs[];
  clearable?: boolean;
};

function isInSelectedRange(
  date: Dayjs,
  startDate: Dayjs | null,
  endDate: Dayjs | null,
) {
  return date && endDate && startDate
    ? dayjs(date).isBetween(startDate, endDate, undefined, "[]")
    : false;
}

const TimeDropdown = ({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (time: TimeObject) => void;
}) => {
  const ref = useRef<HTMLInputElement>(null);
  const pickerControl = (
    <ActionIcon
      variant="subtle"
      color="gray"
      onClick={() => ref.current?.showPicker()}
    >
      <MaterialIcon name="keyboard_arrow_down" />
    </ActionIcon>
  );

  return (
    <TimeInput
      label={label}
      ref={ref}
      value={value}
      rightSection={pickerControl}
      onChange={(event) => {
        const hoursMinutes = event.currentTarget.value.split(":");
        onChange({
          hour: Number(hoursMinutes[0]),
          minute: Number(hoursMinutes[1]),
        });
      }}
    />
  );
};

const presetOptions = [
  {
    label: "Last 1 hour",
    getStartDate: () => dayjs().subtract(1, "hour"),
  },
  {
    label: "Last 6 hours",
    getStartDate: () => dayjs().subtract(6, "hour"),
  },
  {
    label: "Today",
    getStartDate: () => dayjs().startOf("day"),
  },
  {
    label: "Last 7 days",
    getStartDate: () => dayjs().subtract(7, "day"),
  },
  {
    label: "Last 15 days",
    getStartDate: () => dayjs().subtract(15, "day"),
  },
  {
    label: "Last 30 days",
    getStartDate: () => dayjs().subtract(30, "day"),
  },
  {
    label: "Last 3 months",
    getStartDate: () => dayjs().subtract(3, "month"),
  },
  {
    label: "Last 1 year",
    getStartDate: () => dayjs().subtract(1, "year"),
  },
];

export default function DateRangeInput({
  onChange,
  hideFilter,
  unstyled,
  position = "bottom",
  defaultValue,
  defaultRange,
  clearable,
}: DateRangeProps) {
  const [value, setValue] = useState<string>(defaultValue || "");
  const [startDate, setStartDate] = useState<Dayjs | null>(
    defaultRange?.[0] || null,
  );
  const [endDate, setEndDate] = useState<Dayjs | null>(
    defaultRange?.[1] || null,
  );

  const [startTime, setStartTime] = useState<TimeObject>({
    hour: 0,
    minute: 0,
  });
  const [endTime, setEndTime] = useState<TimeObject>({ hour: 23, minute: 59 });

  const [hovered, setHovered] = useState<Dayjs | null>(null);
  const [showCalendar, setShowCalendar] = useState<boolean>(false);

  const combobox = useCombobox({
    onDropdownClose: () => {
      combobox.resetSelectedOption();
      setShowCalendar(false);
    },
  });

  const today = new Date();
  const currentMonth = today;
  const nextMonth = new Date(today.getFullYear(), today.getMonth() + 1);

  const handleDateChange = (date: Dayjs) => {
    // Use default times if not set
    const startT =
      startTime &&
      typeof startTime.hour === "number" &&
      typeof startTime.minute === "number"
        ? startTime
        : { hour: 0, minute: 0 };
    const endT =
      endTime &&
      typeof endTime.hour === "number" &&
      typeof endTime.minute === "number"
        ? endTime
        : { hour: 23, minute: 59 };

    if (!startDate || (startDate && endDate)) {
      // Start a new selection
      setStartDate(date.set("hour", startT.hour).set("minute", startT.minute));
      setEndDate(null);
      setStartTime(startT);
    } else {
      // Finalize the range selection
      if (startDate.isAfter(date)) {
        setEndDate(startDate);
        setStartDate(
          date.set("hour", startT.hour).set("minute", startT.minute),
        );
        setStartTime(startT);
        setEndTime(endT);
      } else {
        setEndDate(date.set("hour", endT.hour).set("minute", endT.minute));
        setEndTime(endT);
      }
    }
  };

  const getDayProps = (date: Date) => {
    const currentDate = dayjs(date);
    const isStartDate = currentDate.isSame(startDate, "day");
    const isEndDate = currentDate.isSame(endDate, "day");
    const isSelected =
      isStartDate ||
      isEndDate ||
      isInSelectedRange(currentDate, startDate, endDate);
    const isInRange = hovered || isSelected;

    return {
      onMouseEnter: () => setHovered(currentDate),
      onMouseLeave: () => setHovered(null),
      inRange: isInRange,
      firstInRange: isStartDate,
      lastInRange: isEndDate,
      selected: isSelected,
      onClick: () => handleDateChange(currentDate),
    };
  };

  const clearValue = () => {
    if (!value) {
      setStartDate(null);
      setEndDate(null);
      setStartTime({ hour: 0, minute: 0 });
      setEndTime({ hour: 23, minute: 59 });
    }
    setValue("");
    onChange([], "");
  };

  useEffect(() => {
    if (defaultRange) {
      setStartDate(defaultRange[0]);
      setEndDate(defaultRange[1]);
    }
    if (defaultValue) {
      setValue(defaultValue);
    }
  }, [defaultRange, defaultValue]);

  return (
    <Combobox
      className="date-range-picker"
      store={combobox}
      withinPortal
      width={showCalendar ? 570 : 340}
      classNames={{
        dropdown: "rounded-sm p-0",
        option: "pl-3 mx-1",
        header: "pl-[20px] p-5",
        footer: "p-0",
      }}
      position={position}
    >
      <Combobox.Target>
        <InputBase
          variant={unstyled ? "unstyled" : "default"}
          component="button"
          type="button"
          pointer
          rightSection={
            value !== "" && clearable ? (
              <CloseButton
                size="sm"
                onMouseDown={(event) => event.preventDefault()}
                onClick={() => {
                  clearValue();
                  if (hideFilter) hideFilter();
                }}
                aria-label="Clear value"
              />
            ) : (
              <MaterialIcon
                name="keyboard_arrow_down"
                className="cursor-pointer !font-light"
                onClick={() => combobox.toggleDropdown()}
              />
            )
          }
          onClick={() => combobox.toggleDropdown()}
          rightSectionPointerEvents={value === "" ? "none" : "all"}
          classNames={{ input: "rounded-lg" }}
        >
          {value || <Input.Placeholder>PICK DATES</Input.Placeholder>}
        </InputBase>
      </Combobox.Target>

      {showCalendar ? (
        <Combobox.Dropdown>
          <div className="flex px-4 py-2">
            <div className="flex flex-col justify-between">
              <Calendar
                defaultDate={currentMonth}
                withCellSpacing={false}
                getDayProps={getDayProps}
              />

              <TimeDropdown
                label="Start time"
                value={
                  typeof startTime.hour === "number" &&
                  typeof startTime.minute === "number"
                    ? `${String(startTime.hour).padStart(2, "0")}:${String(startTime.minute).padStart(2, "0")}`
                    : "00:00"
                }
                onChange={(time) => {
                  setStartTime(time);
                  if (startDate)
                    setStartDate(
                      startDate
                        .set("hour", time.hour)
                        .set("minute", time.minute),
                    );
                }}
              />
            </div>

            <Divider orientation="vertical" className="m-4" />

            <div className="flex flex-col justify-between">
              <Calendar
                defaultDate={nextMonth}
                withCellSpacing={false}
                getDayProps={getDayProps}
              />
              <TimeDropdown
                label="End time"
                value={
                  typeof endTime.hour === "number" &&
                  typeof endTime.minute === "number"
                    ? `${String(endTime.hour).padStart(2, "0")}:${String(endTime.minute).padStart(2, "0")}`
                    : "23:59"
                }
                onChange={(time) => {
                  setEndTime(time);
                  if (endDate)
                    setEndDate(
                      endDate.set("hour", time.hour).set("minute", time.minute),
                    );
                }}
              />
            </div>
          </div>

          <div className="flex justify-between px-5 py-4">
            {clearable && (
              <Button
                variant="transparent"
                className="!p-0 text-title-12 hover:text-brand"
                onClick={clearValue}
              >
                Reset
              </Button>
            )}
            <Group className="flex flex-1 justify-end gap-4">
              <UnstyledButton
                className="text-title-12"
                onClick={() => combobox.closeDropdown()}
              >
                Cancel
              </UnstyledButton>
              <Button
                disabled={!startDate || !endDate}
                className="text-title-12"
                onClick={() => {
                  const label = "Custom";
                  setValue(label);

                  onChange([startDate, endDate], label);
                  combobox.closeDropdown();
                }}
              >
                Apply
              </Button>
            </Group>
          </div>
        </Combobox.Dropdown>
      ) : (
        <Combobox.Dropdown>
          {value && startDate && endDate && (
            <Combobox.Header>
              <Text className="text-title-15 px-1 text-brand">{value}</Text>
              <Text className="text-title-15 px-1 text-black">
                {startDate.format(DATE_TIME_FORMAT)} -{" "}
                {endDate.format(DATE_TIME_FORMAT)}
              </Text>
            </Combobox.Header>
          )}
          <Combobox.Options>
            {presetOptions.map((option, key) => (
              <Combobox.Option
                key={key}
                value={option.getStartDate().format(DATE_TIME_FORMAT)}
                onClickCapture={() => {
                  const startDate = option.getStartDate();
                  const endDate = dayjs();

                  setValue(option.label);
                  setStartDate(startDate);
                  setEndDate(endDate);
                  setStartTime({
                    hour: startDate.hour(),
                    minute: startDate.minute(),
                  });
                  setEndTime({
                    hour: endDate.hour(),
                    minute: endDate.minute(),
                  });

                  onChange([startDate, endDate], option.label);

                  combobox.closeDropdown();
                }}
                className="text-title-15"
              >
                <Group gap={8}>
                  <MaterialIcon
                    name="check"
                    className={`!text-[16px] ${value !== option.label && "text-transparent"}`}
                  />
                  {option.label}
                </Group>
              </Combobox.Option>
            ))}
          </Combobox.Options>
          <Combobox.Footer>
            <Combobox.Option
              value="custom"
              onClickCapture={() => {
                setShowCalendar(true);
                if (!value) {
                  const today = dayjs();
                  setStartDate(today.subtract(7, "day"));
                  setEndDate(today);

                  setStartTime({ hour: today.hour(), minute: today.minute() });
                  setEndTime({ hour: today.hour(), minute: today.minute() });
                }
              }}
              className="text-title-15 py-5 text-brand"
            >
              Custom
            </Combobox.Option>
          </Combobox.Footer>
        </Combobox.Dropdown>
      )}
    </Combobox>
  );
}

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
import {
  Card,
  CardProps,
  Group,
  SegmentedControl,
  Stack,
  Text,
  Textarea,
} from "@mantine/core";
import { useState } from "react";

import { CircularEvaluatorButton } from "./CircularEvaluatorButton";
import { EditWithSparkIcon } from "./EditWithSparkIcon";
import { GradientSpinner } from "./GradientSpinner";

interface CreateAiEvaluatorCardProps extends Omit<CardProps, "children"> {
  onEditClick?: () => void;
  onTextChange?: (value: string) => void;
  onSendClick?: () => void;
  onCancelClick?: () => void;
  initialValue?: string;
  isLoading?: boolean;
}

type SegmentOption = "edit" | "notes";

export default function CreateAiEvaluatorCard({
  className,
  onEditClick,
  onTextChange,
  onSendClick,
  onCancelClick,
  initialValue = "",
  isLoading = false,
  ...props
}: CreateAiEvaluatorCardProps) {
  const [textValue, setTextValue] = useState(initialValue);
  const [selectedSegment, setSelectedSegment] = useState<SegmentOption>("edit");
  const [isTextareaFocused, setIsTextareaFocused] = useState(false);

  const handleTextChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    if (isLoading) return;

    const newValue = e.target.value;
    setTextValue(newValue);
    if (onTextChange) {
      onTextChange(newValue);
    }
  };

  const handleButtonClick = () => {
    if (isLoading) {
      if (onCancelClick) {
        onCancelClick();
      }
    } else {
      if (onSendClick) {
        onSendClick();
      }
    }
  };

  const handleSegmentChange = (value: string) => {
    if (!isLoading) {
      setSelectedSegment(value as SegmentOption);
    }
  };

  return (
    <Card
      className={`w-[640px] h-[180px] rounded-[32px] bg-neutrals-50 border-default ${className || ""}`}
      padding={24}
      {...props}
    >
      <Stack gap="16px" className="h-full relative">
        <Group gap="8px" align="center">
          {isLoading ? (
            <>
              <GradientSpinner size={20} />
              <Text className="text-title-12">Writing Criterion...</Text>
            </>
          ) : (
            <>
              <div className="cursor-pointer" onClick={onEditClick}>
                <EditWithSparkIcon size={20} />
              </div>
              <Text className="text-title-12">
                Create Evaluator Prompt with AI
              </Text>
            </>
          )}
        </Group>

        <Textarea
          placeholder={
            isTextareaFocused
              ? "Describe the criterion you're trying to add to the rubric.."
              : "AI-generated criterion description goes here"
          }
          className="flex-1 w-full"
          classNames={{
            input: `h-[40px] !text-body-14 !text-secondaryDark 
              ${
                isTextareaFocused
                  ? "placeholder:text-resting placeholder:text-body-14"
                  : "placeholder:bg-gradient-to-r placeholder:from-[#9168C0] placeholder:to-[#1BA1E3] placeholder:bg-clip-text placeholder:text-transparent"
              } 
              !border-0 !ring-0 !outline-none !pl-0`,
            root: "w-full !border-0 !ring-0 !outline-none",
            wrapper:
              "!border-0 !ring-0 !outline-none focus:!border-0 hover:!border-0 active:!border-0 !pl-0",
          }}
          value={textValue}
          onChange={handleTextChange}
          onFocus={() => setIsTextareaFocused(true)}
          onBlur={() => setIsTextareaFocused(false)}
          autosize={false}
          variant="unstyled"
          readOnly={isLoading}
        />

        <div className="absolute bottom-0 left-0">
          <SegmentedControl
            value={selectedSegment}
            onChange={handleSegmentChange}
            data={[
              {
                value: "edit",
                label: (
                  <div className="flex items-center justify-center w-full h-full">
                    <div className={`${isLoading ? "opacity-50" : ""}`}>
                      <EditWithSparkIcon
                        size={20}
                        color={
                          selectedSegment === "edit"
                            ? "rgb(249, 250, 251)"
                            : undefined
                        }
                      />
                    </div>
                  </div>
                ),
              },
              {
                value: "notes",
                label: (
                  <div className="flex items-center justify-center w-full h-full">
                    <MaterialIcon
                      name="notes"
                      size={20}
                      className={`${
                        selectedSegment === "notes"
                          ? isLoading
                            ? "text-secondary"
                            : "text-neutrals-50" // Using Tailwind's white class
                          : "text-secondary"
                      } ${isLoading ? "opacity-50" : ""}`}
                    />
                  </div>
                ),
              },
            ]}
            classNames={{
              root: `w-[76px] h-[40px] bg-veryLightSilver border-0 p-[2px] ${isLoading ? "opacity-70 pointer-events-none" : ""}`,
              control: "border-0 outline-none flex items-center justify-center",
              label:
                "p-0 border-0 w-full h-full flex items-center justify-center",
              indicator: isLoading
                ? "bg-neutrals-300 border-0"
                : "bg-gradient-to-r from-[#9168C0] to-[#1BA1E3] border-0",
            }}
            radius={24}
            disabled={isLoading}
          />
        </div>

        <div className="absolute bottom-0 right-0">
          <CircularEvaluatorButton
            isLoading={isLoading}
            onClick={handleButtonClick}
          />
        </div>
      </Stack>
    </Card>
  );
}

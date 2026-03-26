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

import BadgeWithIcon from "@/components/BadgeWithIcon";
import { CircularEvaluatorButton } from "@/components/CircularEvaluatorButton";
import MaterialIcon from "@/components/MaterialIcon";
import Page from "@/components/Page";
import { useGlobalContext } from "@/hooks/useGlobalContext";
import {
  Box,
  Button,
  Group,
  Input,
  Skeleton,
  Stack,
  Text,
} from "@mantine/core";
import React, { useState } from "react";

export default function Autorater(): React.ReactElement {
  const { userDetails } = useGlobalContext();
  const isNavOpen = false;

  const [inputValue, setInputValue] = useState("");
  const [isLoading, _] = useState(false);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputValue(e.currentTarget.value);
  };

  return (
    <Page
      className="bg-white rounded-0 flex flex-col justify-between !p-[16px]"
      fullHeight
    >
      <Box
        className={`mx-auto mt-16 ${isNavOpen ? "max-w-[774px]" : "max-w-[888px]"}`}
      >
        <div className="text-title-28 text-secondaryDark">
          Hello{" "}
          {!userDetails ? (
            <Skeleton
              height={28}
              width={80}
              radius="md"
              display="inline-block"
              className="align-middle mx-1"
            />
          ) : (
            userDetails.firstName
          )}{" "}
          welcome to <BadgeWithIcon title="Stax" variant="inline" /> - ready to
          turn your vibe tests into a scaleable{" "}
          <BadgeWithIcon title="Autorater" variant="inline" /> ?
        </div>

        <div className="h-12"></div>

        <div className="text-title-28 text-secondaryDark">
          To get started, just tell me about the task you want to evaluate.
          Think of it as outlining your perfect{" "}
          <BadgeWithIcon title="Judge" variant="inline" /> for Al responses. If
          you need a hand, our{" "}
          <BadgeWithIcon title="Quickstart" variant="inline" /> tutorial and{" "}
          <BadgeWithIcon title="Documentation" variant="inline" /> are desgined
          to help you!
        </div>
      </Box>

      <Stack
        className={`mx-auto mb-4 ${isNavOpen ? "max-w-[774px]" : "max-w-[888px]"}`}
        gap="16px"
      >
        <Box className="border-default bg-white rounded-lg p-4">
          <Input
            className="border-0"
            placeholder="Rotating descriptions of evaluation goals go here.."
            classNames={{
              input:
                "placeholder:text-title-22 placeholder:text-resting text-secondaryDark text-title-22 border-0",
            }}
            value={inputValue}
            onChange={handleInputChange}
          />
          <Group className="justify-between mt-4 w-full">
            <Button
              variant="outline"
              className="h-9 min-w-[94px] !rounded-[8px] border border-solid border-default py-md px-xl"
              leftSection={
                <MaterialIcon
                  name="attach_file"
                  size={18}
                  className="text-secondaryDark"
                />
              }
            >
              <Text className="text-title-14 text-secondaryDark whitespace-nowrap ">
                Attach
              </Text>
            </Button>
            <CircularEvaluatorButton
              isLoading={isLoading}
              onClick={() => {}}
              disabled={!inputValue.trim()}
              variant="brand"
            />
          </Group>
        </Box>
        <Group gap={8}>
          <MaterialIcon
            size={20}
            name="info"
            className="text-resting !font-light"
          />
          <Text className="text-body-14 text-resting">
            For superior autorater performance, attach a dataset. Learn more
            about{" "}
            <span className="text-brand cursor-pointer">
              Dataset Structuring
            </span>{" "}
            or download our{" "}
            <span className="text-brand cursor-pointer">Sample Dataset</span>.
          </Text>
        </Group>
      </Stack>
    </Page>
  );
}

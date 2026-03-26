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
import { HumanEvalPassRateData, RatingCounts } from "@/types";
import {
  Box,
  Card,
  Flex,
  Group,
  Loader,
  Stack,
  Text,
  UnstyledButton,
} from "@mantine/core";
import { useEffect, useState } from "react";

import { useProjectContext } from "../../../hooks/useProjectContext";
import SideBySideModelsIcon from "../SideBySideModelsIcon";
import MetricCardHeader from "./MetricCardHeader";

type EvaluatorRateMetricCardProps = HumanEvalPassRateData & {
  evaluatorName?: string;
  providers: string[];
  total?: number;
  ratingCounts?: RatingCounts;
};

export default function EvaluatorRateMetricCard({
  passRate,
  likes,
  dislikes,
  ratingCounts,
  total,
  evaluatorName,
  providers,
}: EvaluatorRateMetricCardProps) {
  const { projectState, isSideBySide } = useProjectContext();
  const [initialLoadComplete, setInitialLoadComplete] = useState(false);

  useEffect(() => {
    if (!projectState?.humanEvalPassRateIsLoading && !initialLoadComplete) {
      setInitialLoadComplete(true);
    }
  }, [projectState?.humanEvalPassRateIsLoading, initialLoadComplete]);

  const modelALikes = ratingCounts?.A_IS_BETTER || 0;
  const modelBLikes = ratingCounts?.B_IS_BETTER || 0;
  const bothAreGood = ratingCounts?.BOTH_ARE_GOOD || 0;
  const bothAreBad = ratingCounts?.BOTH_ARE_BAD || 0;

  const totalLikes = modelALikes + modelBLikes;
  const sxsTotal = total || totalLikes;

  const pointwiseTotal = (likes ?? 0) + (dislikes ?? 0);

  let winRate = 50;
  if (totalLikes > 0) {
    winRate = (modelALikes / (totalLikes + bothAreBad + bothAreGood)) * 100;
  }

  const modelAPercentage =
    totalLikes === 0 ? 0 : (modelALikes / totalLikes) * 100;
  const modelBPercentage = 100 - modelAPercentage;

  const modelAWidthStyle = {
    width:
      modelALikes === 0
        ? "0%"
        : Math.max(modelAPercentage, modelALikes > 0 ? 15 : 0) + "%",
  };

  const modelBWidthStyle = {
    width:
      modelBLikes === 0
        ? "0%"
        : Math.max(modelBPercentage, modelBLikes > 0 ? 15 : 0) + "%",
  };

  const isLoading =
    !initialLoadComplete && projectState?.humanEvalPassRateIsLoading;

  return (
    <Card
      padding="md"
      radius="lg"
      withBorder
      className={`${isSideBySide ? "h-[172px]" : "h-[148px]"} w-[272px] max-w-[100%] bg-neutrals-50`}
    >
      <MetricCardHeader
        title={
          evaluatorName || isSideBySide
            ? "Human Evaluation Win Rate"
            : "Human Evaluation Pass Rate"
        }
        providers={providers}
      />

      <Group gap="0" className="flex-1 flex w-full items-center justify-center">
        {isLoading ? (
          <Loader size="sm" />
        ) : (
          <Group gap="0" className="flex-1 flex w-full items-center">
            {isSideBySide ? (
              <Stack className="w-full gap-2">
                <Group className="w-full">
                  <Box className="flex-1">
                    {totalLikes > 0 ? (
                      <Text className="!font-medium text-title-28">
                        {winRate.toFixed(0)}%
                      </Text>
                    ) : (
                      <Text className="!font-medium text-resting text-title-28">
                        -- %
                      </Text>
                    )}
                  </Box>

                  <Stack className="items-end" gap={2}>
                    <Text className="!font-medium text-resting text-body-11">
                      Total
                    </Text>
                    <Text className="!font-medium text-body-14">
                      {sxsTotal}
                    </Text>
                  </Stack>
                </Group>

                <Box className="relative">
                  <Flex
                    className="overflow-hidden rounded-[4px] border-default"
                    h={32}
                  >
                    {/* Model A Icon */}
                    <Flex
                      h="100%"
                      w={32}
                      align="center"
                      justify="center"
                      className="bg-lightBlue flex-shrink-0 z-10"
                    >
                      <SideBySideModelsIcon
                        variant="first"
                        greyedOut={modelALikes === 0}
                        className="font-medium text-body-11"
                      />
                    </Flex>

                    <Box
                      h="100%"
                      w={1}
                      className="bg-white flex-shrink-0 z-10"
                    />

                    {/* Main bar container - full width */}
                    <Box className="flex-1 relative">
                      {/* Background */}
                      <Box
                        h="100%"
                        className="bg-veryLightSilver border-b border-t border-borderColor w-full absolute left-0"
                      />

                      {/* Model A bar */}
                      <Flex
                        h="100%"
                        style={modelAWidthStyle}
                        className={`border-b border-t border-r-0 border-borderColor absolute left-0 ${
                          modelALikes > 0 ? "bg-lightBlue" : ""
                        } z-0`}
                      />

                      {/* Model B bar */}
                      <Flex
                        h="100%"
                        style={modelBWidthStyle}
                        className={`border-b border-t border-l-0 border-borderColor absolute right-0 ${
                          modelBLikes > 0 ? "bg-borderColor" : ""
                        } z-0`}
                      />

                      {/* Model A count */}
                      <Text
                        className={`ml-2 font-medium ${
                          modelALikes > 0 ? "text-brand" : ""
                        } text-body-14 absolute left-0 top-1/2 transform -translate-y-1/2 z-10`}
                      >
                        {modelALikes}
                      </Text>

                      {/* Model B count */}
                      <Text className="font-medium text-body-14 absolute right-2 top-1/2 transform -translate-y-1/2 z-10">
                        {modelBLikes}
                      </Text>
                    </Box>

                    <Box
                      h="100%"
                      w={1}
                      className="bg-white flex-shrink-0 z-10"
                    />

                    {/* Model B Icon */}
                    <Flex
                      h="100%"
                      w={32}
                      align="center"
                      justify="center"
                      className="bg-borderColor flex-shrink-0 z-10"
                    >
                      <div
                        className={`flex h-5 w-5 items-center justify-center rounded ${
                          modelBLikes === 0
                            ? "bg-transparent"
                            : "bg-borderColor"
                        }`}
                      >
                        <Text
                          size="xs"
                          fw={500}
                          className={`font-medium ${
                            modelBLikes === 0
                              ? "text-black"
                              : "text-secondaryDark"
                          } text-body-11`}
                        >
                          B
                        </Text>
                      </div>
                    </Flex>
                  </Flex>
                </Box>
              </Stack>
            ) : (
              <>
                {passRate || passRate === 0 ? (
                  <Text className="!font-medium text-title-28">
                    {parseFloat((passRate * 100).toFixed(2))} %
                  </Text>
                ) : (
                  <Text className="!font-medium text-resting text-title-28">
                    -- %
                  </Text>
                )}

                <Group gap={0} className="mt-[4px] w-full justify-between">
                  <Group
                    gap={0}
                    className="h-[16px] flex-row items-center gap-lg"
                  >
                    <Group className="h-[16px] flex-row items-center gap-sm">
                      <Text className="p-0 text-resting text-body-11">
                        {likes || 0}
                      </Text>
                      <UnstyledButton className="flex items-center">
                        <MaterialIcon
                          name="thumb_up"
                          size={16}
                          className="cursor-default text-resting"
                        />
                      </UnstyledButton>
                    </Group>
                    <Group className="h-[16px] flex-row items-center gap-sm">
                      <Text className="p-0 text-resting text-body-11">
                        {dislikes || 0}
                      </Text>
                      <UnstyledButton className="flex items-center">
                        <MaterialIcon
                          name="thumb_down"
                          size={16}
                          className="cursor-default text-resting"
                        />
                      </UnstyledButton>
                    </Group>
                  </Group>

                  <Text className="text-resting text-body-11">
                    Total: {pointwiseTotal}
                  </Text>
                </Group>
              </>
            )}
          </Group>
        )}
      </Group>
    </Card>
  );
}

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

import { Model } from "@/queries/types";
import {
  Box,
  Card,
  Divider,
  Group,
  Loader,
  Stack,
  Text,
  Tooltip,
} from "@mantine/core";
import { useEffect, useState } from "react";

import { useProjectContext } from "../../../hooks/useProjectContext";
import SideBySideModelsIcon from "../SideBySideModelsIcon";
import MetricCardHeader from "./MetricCardHeader";

type TokenMetrics = {
  input?: number;
  output?: number;
  total?: number;
};

type TokensMetricCardProps = {
  tokenMetricsA?: TokenMetrics;
  tokenMetricsB?: TokenMetrics | null;
  models?: Model[];
  providers: string[];
};

const FORMAT_THRESHOLDS = {
  THOUSAND: 1000,
  TEN_THOUSAND: 10000,
  HUNDRED_THOUSAND: 100000,
  MILLION: 1000000,
};

export default function TokensMetricCard({
  tokenMetricsA,
  tokenMetricsB,
  models,
  providers,
}: TokensMetricCardProps) {
  const { projectState, isSideBySide } = useProjectContext();
  const [initialLoadComplete, setInitialLoadComplete] = useState(false);

  useEffect(() => {
    if (!projectState?.inferenceMetricsIsLoading && !initialLoadComplete) {
      setInitialLoadComplete(true);
    }
  }, [projectState?.inferenceMetricsIsLoading, initialLoadComplete]);

  const isLoading =
    !initialLoadComplete && projectState?.inferenceMetricsIsLoading;

  const shouldShowOutputDiff =
    isSideBySide &&
    tokenMetricsB?.output !== undefined &&
    tokenMetricsA?.output !== undefined &&
    tokenMetricsB.output !== 0 &&
    tokenMetricsA.output !== 0;

  const shouldShowTotalDiff =
    isSideBySide &&
    tokenMetricsB?.total !== undefined &&
    tokenMetricsA?.total !== undefined &&
    tokenMetricsB.total !== 0 &&
    tokenMetricsA.total !== 0;

  const outputDifference = shouldShowOutputDiff
    ? tokenMetricsB.output! - tokenMetricsA.output!
    : null;

  const totalDifference = shouldShowTotalDiff
    ? tokenMetricsB.total! - tokenMetricsA.total!
    : null;

  const formatTokenValue = (value?: number) => {
    return value === 0 ? "--" : value || 0;
  };

  const formatDifference = (value: number) => {
    const absValue = Math.abs(value);
    const sign = value < 0 ? "-" : "";

    if (absValue >= FORMAT_THRESHOLDS.MILLION) {
      return `${sign}${(absValue / FORMAT_THRESHOLDS.MILLION).toFixed(1)}M`;
    } else if (absValue >= FORMAT_THRESHOLDS.TEN_THOUSAND) {
      return `${sign}${Math.round(absValue / FORMAT_THRESHOLDS.THOUSAND)}K`;
    }

    return `${sign}${absValue}`;
  };

  return (
    <Card
      padding="md"
      radius="lg"
      withBorder
      className={`${isSideBySide ? "h-[172px]" : "h-[148px]"} w-[272px] max-w-[100%]`}
    >
      <MetricCardHeader
        title={
          isSideBySide ? "Output & Total Tokens" : "Total Input & Output Tokens"
        }
        models={models}
        providers={providers}
      />

      <Group gap="0" className="flex-1 flex w-full items-center justify-center">
        {isLoading ? (
          <Loader size="sm" />
        ) : (
          <Group gap="0" className="flex-1 flex w-full items-center">
            {isSideBySide ? (
              <Stack className="w-full gap-0">
                <Group className="w-full items-center" gap={0}>
                  <Box className="w-[30px]">
                    <SideBySideModelsIcon variant="first" />
                  </Box>
                  <Box className="flex flex-1">
                    <Box className="ml-2 w-[70px]">
                      <Text className="text-body-14">
                        {formatTokenValue(tokenMetricsA?.output)}
                      </Text>
                    </Box>
                    <Box className="ml-5 w-[70px]">
                      <Text className="text-body-14">
                        {formatTokenValue(tokenMetricsA?.total)}
                      </Text>
                    </Box>
                  </Box>
                </Group>

                <Box className="my-[3.5px] pl-7">
                  <Divider />
                </Box>

                <Group className="w-full items-center" gap={0}>
                  <Box className="w-[30px]">
                    <SideBySideModelsIcon variant="second" />
                  </Box>
                  <Box className="flex flex-1">
                    <Box className="ml-2 w-[70px]">
                      <Group gap={4} className="flex-nowrap">
                        <Text className="text-body-14">
                          {formatTokenValue(tokenMetricsB?.output)}
                        </Text>
                        {outputDifference !== null &&
                          outputDifference !== 0 && (
                            <Tooltip label={outputDifference}>
                              <Text
                                className={`text-body-14 ${
                                  outputDifference < 0
                                    ? "text-red"
                                    : "text-lime"
                                }`}
                              >
                                {formatDifference(outputDifference)}
                              </Text>
                            </Tooltip>
                          )}
                      </Group>
                    </Box>
                    <Box className="ml-5 w-[70px]">
                      <Group gap={4} className="flex-nowrap">
                        <Text className="text-body-14">
                          {formatTokenValue(tokenMetricsB?.total)}
                        </Text>
                        {totalDifference !== null && totalDifference !== 0 && (
                          <Tooltip label={totalDifference}>
                            <Text
                              className={`text-body-14 ${
                                totalDifference < 0 ? "text-red" : "text-lime"
                              }`}
                            >
                              {formatDifference(totalDifference)}
                            </Text>
                          </Tooltip>
                        )}
                      </Group>
                    </Box>
                  </Box>
                </Group>

                <Box className="mt-1 pl-7">
                  <Box className="flex">
                    <Box className="ml-2 w-[70px]">
                      <Text className="!font-medium text-resting text-body-12">
                        Output
                      </Text>
                    </Box>
                    <Box className="ml-5 w-[70px]">
                      <Text className="!font-medium text-resting text-body-12">
                        Total
                      </Text>
                    </Box>
                  </Box>
                </Box>
              </Stack>
            ) : (
              <Group className="w-full flex-col gap-0">
                <Group className="mb-[4px] w-full flex-row justify-between">
                  <Text className="!font-medium text-resting text-title-12">
                    Input
                  </Text>
                  <Text className="pt-[6px] !font-medium text-body-14">
                    {tokenMetricsA?.input}
                  </Text>
                </Group>
                <Divider className="my-[2px] w-full" />
                <Group className="w-full flex-row justify-between">
                  <Text className="!font-medium text-resting text-title-12">
                    Output
                  </Text>
                  <Text className="pt-[6px] !font-medium text-body-14">
                    {tokenMetricsA?.output}
                  </Text>
                </Group>
              </Group>
            )}
          </Group>
        )}
      </Group>
    </Card>
  );
}

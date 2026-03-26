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

import ActionMenu from "@/components/ActionMenu";
import Card from "@/components/Card";
import DateRangeInput from "@/components/DateRangeInput";
import MaterialIcon from "@/components/MaterialIcon";
import {
  Box,
  Group,
  Loader,
  LoadingOverlay,
  SegmentedControl,
  Stack,
  Text,
} from "@mantine/core";
import { useEffect, useRef, useState } from "react";

import { getInferenceMonitoring } from "../../state/queries";
import {
  AnalyticsData,
  CHART_PROPS_BY_TAB,
  ChartTabInference,
  FilterLabel,
  INFERENCE_CHART_TABS,
  InferenceRaw,
  LineChartItem,
  TOKEN_TYPES,
} from "../../types/charts";
import AverageAnalytics from "../AverageAnalytics";
import LineChart from "../charts/LineChart";
import StackedBarChart from "../charts/StackedBarChart";
import { StackChartItem } from "../charts/types";
import { usePDFExport } from "../charts/utils/hooks";
import { getInferenceMonitoringPayload } from "../utils";
import { InferenceChartProps } from "./types";

export default function InferenceChart({
  selectedFilters,
  selectedDates,
  selectedDatesLabel,
  onDateChange,
  duplicateChart,
  deleteChart,
}: InferenceChartProps) {
  const [isLoading, setLoading] = useState(false);
  const [tab, setTab] = useState<ChartTabInference>(ChartTabInference.LATENCY);
  const [lineChartData, setLineChartData] = useState<LineChartItem[]>([]);
  const [stackedBarData, setStackedBarData] = useState<StackChartItem[]>([]);
  const [analyticsData, setAnalyticsData] = useState<AnalyticsData>({
    averageTime: 0,
    prompts: 0,
    completed: 0,
    total: 0,
  });

  const pdfRef = useRef<HTMLDivElement>(null);
  const { exportPDF, isGenerating } = usePDFExport();

  const handleExport = () => {
    exportPDF(pdfRef, "inference-charts");
  };

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const newAnalyticsData = {
          averageTime: 0,
          prompts: 0,
          completed: 0,
          total: 0,
        };

        const chartPromises = selectedFilters.map((filters) =>
          getInferenceMonitoring(
            getInferenceMonitoringPayload(selectedDates, filters),
          ),
        );
        const results = await Promise.all(chartPromises);
        const stackedBarCurrentData: StackChartItem[] = [];

        const lineChartData = results.flatMap((subArray, index) => {
          newAnalyticsData.averageTime = subArray.overall_average_latency || 0;
          newAnalyticsData.prompts = subArray.overall_prompt_tokens || 0;
          newAnalyticsData.completed = subArray.overall_completion_tokens || 0;
          newAnalyticsData.total = subArray.overall_total_tokens || 0;

          return subArray.time_series_analytics.map((entry: InferenceRaw) => {
            const filter =
              index === 0
                ? chartPromises.length > 1
                  ? FilterLabel.FILTER_A
                  : FilterLabel.ALL_MODELS
                : FilterLabel.FILTER_B;

            TOKEN_TYPES.forEach((tokenType) => {
              stackedBarCurrentData.push({
                windowStart: new Date(entry?.windowStart || ""),
                type:
                  filter === FilterLabel.ALL_MODELS
                    ? tokenType.name
                    : `${filter}: ${tokenType.name}`,
                value: entry[tokenType.value as keyof InferenceRaw] as number,
              });
            });

            return {
              ...entry,
              windowStart: entry.windowStart,
              filter,
            };
          });
        });

        setLineChartData(lineChartData);
        setStackedBarData(stackedBarCurrentData);
        setAnalyticsData(newAnalyticsData);
      } catch {
        // Handle error
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [selectedDates, selectedFilters]);

  return (
    <Stack ref={pdfRef}>
      {isGenerating ? (
        <Loader
          size="xl"
          className="flex h-[470px] w-full flex-col items-center justify-center"
        />
      ) : (
        <>
          <AverageAnalytics
            label="Average Analytics"
            prompts={analyticsData.prompts}
            total={analyticsData.total}
            completed={analyticsData.completed}
            time={analyticsData.averageTime}
            isSecondFilterOn={selectedFilters.length > 1}
          />
          <Card className="rounded-lg">
            <div className="flex justify-between">
              <SegmentedControl
                className="pdf-tabs-transform"
                data={INFERENCE_CHART_TABS}
                radius="xl"
                color="var(--color-brand)"
                size="sm"
                classNames={{
                  control: "min-w-[95px]",
                  innerLabel: "font-medium",
                }}
                onChange={(val) => {
                  setTab(val as ChartTabInference);
                }}
              />

              <Group>
                <DateRangeInput
                  defaultValue={selectedDatesLabel}
                  defaultRange={selectedDates}
                  onChange={onDateChange}
                  unstyled
                  position="bottom-end"
                />

                <ActionMenu
                  menuItems={[
                    {
                      label: "Duplicate",
                      leftSection: (
                        <MaterialIcon
                          name="copy_all"
                          className="text-secondary"
                          size={20}
                        />
                      ),
                      onClick: duplicateChart,
                    },
                    {
                      label: "Export chart",
                      onClick: handleExport,
                      leftSection: (
                        <MaterialIcon
                          name="download"
                          className="text-secondary"
                          size={20}
                        />
                      ),
                    },
                    ...(deleteChart
                      ? [
                          {
                            label: "Delete",
                            leftSection: (
                              <MaterialIcon
                                name="delete"
                                className="text-secondary"
                                size={20}
                              />
                            ),
                            onClick: deleteChart,
                          },
                        ]
                      : []),
                  ]}
                />
              </Group>
            </div>

            <Box pos="relative">
              <LoadingOverlay
                visible={isLoading}
                loaderProps={{ children: <Loader size="md" /> }}
              />
              {tab === ChartTabInference.TOKENS ? (
                <StackedBarChart
                  data={stackedBarData || []}
                  {...CHART_PROPS_BY_TAB[tab]}
                />
              ) : (
                <LineChart
                  isSecondFilterOn={selectedFilters.length > 1}
                  data={lineChartData.map((data) => ({
                    value:
                      tab === ChartTabInference.LATENCY
                        ? Number.parseFloat(
                            (
                              (data.average_avg_chat_latency || 0) / 1000
                            ).toFixed(2),
                          ) || 0
                        : data.totalInferences || 0,
                    windowStart: data.windowStart,
                    filter: data.filter,
                    label:
                      tab === ChartTabInference.LATENCY ? "Latency" : "Queries",
                  }))}
                />
              )}
              {tab === ChartTabInference.LATENCY ? (
                <Text className="text-gray-400 text-body-sans-12 ml-3 absolute right-0 bottom-1">
                  Latency results may differ from production performance.
                </Text>
              ) : null}
            </Box>
          </Card>
        </>
      )}
    </Stack>
  );
}

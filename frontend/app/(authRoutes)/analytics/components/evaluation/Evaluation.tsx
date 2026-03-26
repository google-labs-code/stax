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
import DateRangeInput from "@/components/DateRangeInput";
import MaterialIcon from "@/components/MaterialIcon";
import { useAnalyticsContext } from "@/hooks/useAnalyticsContext";
import { Group } from "@mantine/core";
import dayjs, { Dayjs } from "dayjs";
import { useEffect, useMemo, useRef, useState } from "react";

import { getEvaluationMonitoring } from "../../state/queries";
import "../../styles/main.scss";
import {
  Chart,
  ChartTabEvaluation,
  EvaluationChartDataItem,
  FilterLabel,
  MonitoringStats,
} from "../../types/charts";
import AnalyticsFiltersContainer from "../AnalyticsFiltersContainer";
import { usePDFExport } from "../charts/utils/hooks";
import {
  getDatapoints,
  getEvaluationMonitoringPayload,
  getEvaluators,
} from "../utils";
import EvaluationChart from "./EvaluationChart";
import PopoverEvaluatorMenu from "./EvaluatorsOptions";
import { EvaluatorsOptions } from "./types";

export default function Evaluation() {
  const [selectedEvaluators, setSelectedEvaluators] = useState<
    EvaluatorsOptions[]
  >([]);

  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<EvaluationChartDataItem[] | null>(null);

  const { selectedFilters } = useAnalyticsContext();

  const [chart, setChart] = useState<Chart>({
    selectedDates: [dayjs().subtract(15, "day"), dayjs()],
    datesLabel: "Last 15 days",
  });

  const pdfRef = useRef<HTMLDivElement>(null);
  const { exportPDF, isGenerating } = usePDFExport();

  const handleExport = () => {
    exportPDF(pdfRef, "evaluation-charts");
  };

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const chartPromises = selectedFilters.map((filters) =>
          getEvaluationMonitoring(
            getEvaluationMonitoringPayload(chart.selectedDates, filters),
          ),
        );
        const results = await Promise.all(chartPromises);
        const evaluators = getEvaluators(
          Object.keys(results[0]),
          results[1] ? Object.keys(results[1]) : [],
        );
        setSelectedEvaluators(
          evaluators.map((type) => ({
            score: type,
            checked: true,
            visible: true,
          })),
        );
        const combinedResults: EvaluationChartDataItem[] = evaluators.map(
          (evaluator) => ({
            evaluator,
            tab: ChartTabEvaluation.BAR_CHART,
            monitoring: Object(results[0])[evaluator]
              ?.monitoring as MonitoringStats,
            datapoints: [
              ...getDatapoints(
                FilterLabel.FILTER_A,
                Object(results[0])[evaluator]?.datapoints,
              ),
              ...(getDatapoints(
                FilterLabel.FILTER_B,
                Object(results[1])[evaluator]?.datapoints,
              ) ?? []),
            ],
          }),
        );

        setData(combinedResults);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [chart.selectedDates, selectedFilters]);

  const filteredData = useMemo(() => {
    const currentSelectedEvals = selectedEvaluators
      .filter((d) => d.checked)
      .map((d) => d.score);

    return (
      data?.filter((d) => currentSelectedEvals.includes(d.evaluator)) || null
    );
  }, [selectedEvaluators]);

  return (
    <div className="flex flex-col gap-4">
      <AnalyticsFiltersContainer />
      <Group ref={pdfRef} unstyled>
        <Group className="flex justify-between py-3">
          <PopoverEvaluatorMenu
            selectedEvals={selectedEvaluators}
            setSelectedEvals={setSelectedEvaluators}
          />
          <Group>
            <DateRangeInput
              defaultValue={chart.datesLabel}
              defaultRange={chart.selectedDates}
              onChange={(selectedDates: Dayjs[], datesLabel?: string) =>
                setChart({ selectedDates, datesLabel })
              }
              unstyled
              position="bottom-end"
            />
            <ActionMenu
              menuItems={[
                {
                  label: "Export all charts",
                  onClick: handleExport,
                  leftSection: (
                    <MaterialIcon
                      name="download"
                      className="text-secondary"
                      size={20}
                    />
                  ),
                },
              ]}
            />
          </Group>
        </Group>
        <EvaluationChart
          isLoading={loading || isGenerating}
          data={filteredData}
          isSecondFilterOn={selectedFilters.length > 1}
        />
      </Group>
    </div>
  );
}

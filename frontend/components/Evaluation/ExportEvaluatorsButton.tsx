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
import { TOOLTIPS } from "@/config/constants";
import { EvaluatorCardItem, EvaluatorTab, EvaluatorType } from "@/types";
import { jsonToCsvExport } from "@/utils/jsonToCsvExport";
import { Button, Tooltip } from "@mantine/core";

type ExportEvaluatorsButtonProps = {
  data: EvaluatorCardItem[];
  activeTab: EvaluatorTab;
};

export default function ExportEvaluatorsButton({
  data,
  activeTab,
}: ExportEvaluatorsButtonProps) {
  const filteredData = data.filter((item) =>
    activeTab === EvaluatorTab.DEFAULT
      ? item.type === EvaluatorType.SYSTEM
      : item.type === EvaluatorType.USER,
  );

  const hasData = filteredData.length > 0;

  const handleExport = () => {
    const exportData = filteredData.map((item) => ({
      id: item.id,
      name: item.name,
      description: item?.description || "",
      type: item.type,
      prompt: item?.prompts || "",
    }));

    const tabName =
      activeTab === EvaluatorTab.DEFAULT
        ? "system-evaluators"
        : "custom-evaluators";
    const filename = `${tabName}-${new Date().toISOString().slice(0, 10)}.csv`;

    jsonToCsvExport(exportData, filename);
  };

  const buttonContent = (
    <Button
      variant="subtle"
      size="sm"
      leftSection={<MaterialIcon name="download" size={16} />}
      onClick={handleExport}
      disabled={!hasData}
      className={`
        ${
          hasData
            ? "text-black hover:bg-lightSilver active:bg-veryLightSilver transition-colors"
            : "text-secondary bg-transparent hover:bg-transparent"
        }
      !rounded-sm !px-2`}
    >
      Export Evaluators
    </Button>
  );

  return hasData ? (
    buttonContent
  ) : (
    <Tooltip
      label={TOOLTIPS.NO_EVALUATORS_TO_EXPORT}
      position="bottom"
      withArrow
      withinPortal
    >
      <div>{buttonContent}</div>
    </Tooltip>
  );
}

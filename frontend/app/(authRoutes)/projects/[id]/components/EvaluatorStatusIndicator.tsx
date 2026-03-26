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

import Chip from "@/components/Chip";
import MaterialIcon from "@/components/MaterialIcon";
import { EvaluationScoreStatus } from "@/types";
import { useProcessedColor } from "@/utils/colorUtils";
import { Group, Loader, Tooltip } from "@mantine/core";

type StatusIndicatorProps = {
  status?: EvaluationScoreStatus | string | number | null;
  score?: number | null | string;
  count?: number;
  total?: number;
  toolTipText?: string | null;
  color?: string;
  category?: string;
  chipIconSize?: number;
  chipGroupClassName?: string;
};

export default function EvaluatorStatusIndicator({
  status,
  score = null,
  count,
  total,
  toolTipText,
  color,
  category,
  chipGroupClassName = "",
  chipIconSize = 16,
}: StatusIndicatorProps) {
  const { backgroundColor, textColor } = useProcessedColor(
    color,
    "var(--color-very-light-silver)",
    "var(--color-very-light-silver)",
  );

  const shouldShowTooltip =
    status !== EvaluationScoreStatus.PENDING &&
    status !== EvaluationScoreStatus.IN_PROGRESS &&
    toolTipText;

  if (status === undefined || status === null) {
    return null;
  }

  let label = "";
  let icon = null;
  if (status === EvaluationScoreStatus.SUCCESSFUL) {
    if (score !== null && !Number.isNaN(score)) {
      label = score.toString();
      if (category) {
        label += ` - ${category}`;
      }

      const chipElement = (
        <Chip
          label={label}
          groupStyles={{
            backgroundColor: backgroundColor,
          }}
          groupClassName={chipGroupClassName}
          textStyles={{
            color: textColor,
            fontWeight: 500,
          }}
        />
      );

      if (shouldShowTooltip) {
        return (
          <Tooltip
            label={toolTipText}
            position="bottom"
            className="max-w-[200px]"
            multiline
          >
            <Group gap={0} className="w-full">
              {chipElement}
            </Group>
          </Tooltip>
        );
      } else {
        return chipElement;
      }
    }

    return null;
  } else if (status === EvaluationScoreStatus.IN_PROGRESS) {
    icon = <Loader size="13px" className="text-secondary" />;
    label = "In progress";
  } else if (status === EvaluationScoreStatus.FAILED) {
    icon = (
      <MaterialIcon
        name="error"
        size={chipIconSize}
        className="!font-light text-red"
      />
    );
    label = "Failed";
  } else if (status === EvaluationScoreStatus.PENDING) {
    icon = (
      <MaterialIcon
        name="schedule"
        size={chipIconSize}
        className="!font-light text-secondary"
      />
    );
    label = "Pending";
  }

  if (count !== undefined && total !== undefined) {
    label += `: ${count}/${total}`;
  }

  if (label === "") {
    return null;
  }

  const chipElement = (
    <Chip label={label} icon={icon} groupClassName={chipGroupClassName} />
  );

  if (shouldShowTooltip) {
    return (
      <Tooltip
        label={toolTipText}
        position="bottom"
        className="max-w-[200px]"
        multiline
      >
        <Group gap={0} className="w-full">
          {chipElement}
        </Group>
      </Tooltip>
    );
  }

  return chipElement;
}

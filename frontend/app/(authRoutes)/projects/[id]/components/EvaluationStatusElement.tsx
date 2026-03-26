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
import { EvaluationScoreStatus, LLMEvaluation } from "@/types";
import { Group, ScrollArea, UnstyledButton } from "@mantine/core";

import EvaluatorStatusIndicator from "./EvaluatorStatusIndicator";

export default function EvaluationStatusElement({
  data,
  onRerun,
  onStatusClick,
}: {
  data: LLMEvaluation;
  onRerun: () => void;
  onStatusClick: () => void;
}) {
  const evalScore = data?.score;
  const score =
    typeof evalScore === "string" ? evalScore : parseFloat(evalScore || "0");
  const status = data?.evaluationStatus;

  return (
    <ScrollArea className="w-[100%]" scrollbarSize={5}>
      <Group className="w-full gap-md flex-nowrap flex flex-row">
        <UnstyledButton onClick={onStatusClick}>
          <EvaluatorStatusIndicator
            status={status}
            score={score}
            category={data?.category}
            color={data?.color}
            toolTipText={data?.reasoning}
          />
        </UnstyledButton>
        {status === EvaluationScoreStatus.FAILED && (
          <UnstyledButton
            className="flex h-[24px] w-[24px] items-center justify-center rounded-sm p-[4px] border-default"
            onClick={onRerun}
          >
            <MaterialIcon name="refresh" size={16} className="text-secondary" />
          </UnstyledButton>
        )}
      </Group>
    </ScrollArea>
  );
}

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
import TokenAutoIcon from "@/components/icons/TokenAutoIcon";
import { convertMsToS } from "@/utils/helpers";
import { Group, Text, Tooltip } from "@mantine/core";

import { AverageAnalyticsProps } from "./types";

export default function AverageAnalytics({
  label,
  time,
  prompts,
  completed,
  total,
  isSecondFilterOn,
}: AverageAnalyticsProps) {
  return (
    <Group className="my-[16px] justify-between">
      <Text className="text-secondaryDark text-14 font-medium">{label}</Text>
      <Group className="justify-between gap-x-[24px]">
        {!isSecondFilterOn && (
          <>
            <Group className="gap-x-[8px] cursor-default">
              <MaterialIcon
                name="timer"
                className="!text-20 text-secondary"
                tooltipLabel="Average latency"
              />
              <span className="text-12 text-secondary">
                {convertMsToS(time)}
              </span>
            </Group>
            <Group className="gap-x-[8px]">
              <Tooltip label="Total tokens" position="bottom">
                <Group className="pdf-svg-transform">
                  <TokenAutoIcon />
                </Group>
              </Tooltip>
              <span className="text-12 text-secondary">
                Input: {prompts} • Output: {completed} • Total: {total}
              </span>
            </Group>
          </>
        )}
      </Group>
    </Group>
  );
}

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

import { Card, Group, Text } from "@mantine/core";
import ReactDOM from "react-dom";

import { TooltipContentItem } from "../../../types/charts";

export default function Tooltip({
  position,
  content,
}: {
  position: { x: number; y: number };
  content: TooltipContentItem[];
}) {
  return ReactDOM.createPortal(
    <Card
      className="pointer-events-none absolute z-[1000] -translate-x-1/2 -translate-y-full transform"
      style={{
        left: `${position.x}px`,
        top: `${position.y}px`,
      }}
      classNames={{
        root: "max-w-[314px] rounded-xl border-default bg-white p-4 shadow-md",
      }}
    >
      {content.map((c, index) => (
        <Group className="flex-nowrap items-start gap-0" key={index}>
          <Text className="w-[80px] text-[14px] capitalize leading-[20px] text-secondaryDark">
            {c.label}
          </Text>
          <Text className="flex-1 truncate text-[14px] leading-[20px] text-secondary">
            {c.value}
          </Text>
        </Group>
      ))}
    </Card>,
    document.body,
  );
}

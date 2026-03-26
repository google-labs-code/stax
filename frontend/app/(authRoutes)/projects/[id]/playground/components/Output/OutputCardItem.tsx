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

import { EvaluationScoreStatus } from "@/types";
import GetChatMessageWithMarkup from "@/utils/GetChatMessageWithMarkup";
import { Group, ScrollArea, Text } from "@mantine/core";
import { useMemo } from "react";

import EvaluatorStatusIndicator from "../../../components/EvaluatorStatusIndicator";
import { OutputCardItemProps } from "../../types";
import OutputCardItemHeader from "./OutputCardItemHeader";

export default function OutputCardItem({
  text,
  tokens,
  latency,
  isLoading,
  error,
  promptName,
  hasExpandButton,
}: OutputCardItemProps) {
  const value = useMemo(() => text || "", [text]);

  return (
    <div className="h-full flex-1 flex flex-col justify-start">
      <div className="flex flex-row justify-between mb-4">
        <OutputCardItemHeader
          text={text}
          isLoading={isLoading || false}
          promptName={promptName || ""}
          latency={latency}
          tokens={tokens}
          hasExpandButton={hasExpandButton}
        />
      </div>
      <ScrollArea
        scrollbarSize={10}
        type="auto"
        classNames={{
          viewport: "playground-scroll-area-viewport-output",
        }}
      >
        {error && !value ? (
          <Group className="gap-0 self-start">
            <EvaluatorStatusIndicator
              status={EvaluationScoreStatus.FAILED}
              toolTipText={error}
              chipGroupClassName="!py-0"
              chipIconSize={14}
            />
          </Group>
        ) : value ? (
          <GetChatMessageWithMarkup
            message={value}
            elementClassName="self-start"
          />
        ) : (
          <Text className="self-start text-body-14 break-normal text-resting w-100">
            Model output will appear here.
          </Text>
        )}
      </ScrollArea>
    </div>
  );
}

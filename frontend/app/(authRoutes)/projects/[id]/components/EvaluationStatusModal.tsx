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
import { routes } from "@/config/routes";
import { LLMEvaluation } from "@/types";
import { ActionIcon, Group, Modal, Stack, Text, Tooltip } from "@mantine/core";
import { useState } from "react";

import EvaluatorStatusIndicator from "./EvaluatorStatusIndicator";

type EvaluationStatusModalProps = {
  isOpened: boolean;
  data: LLMEvaluation;
  onClose: () => void;
};

export default function EvaluationStatusModal({
  isOpened,
  onClose,
  data,
}: EvaluationStatusModalProps) {
  const [reasoningCopied, setReasoningCopied] = useState(false);
  const [responseCopied, setResponseCopied] = useState(false);

  const handleCopy = (text: string, setCopied: (value: boolean) => void) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      size="700px"
      title={
        <Text className="flex items-center gap-2 !font-medium text-title-22">
          Evaluator Output Details
        </Text>
      }
      centered
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[25px]",
      }}
    >
      <Stack className="gap-2xl">
        <Group className="w-full justify-evenly">
          <Stack gap={5} className="flex flex-col items-center justify-center">
            <Text className="text-neutrals-600 text-title-12">Score</Text>
            <EvaluatorStatusIndicator
              score={data?.score || null}
              status={data?.evaluationStatus}
              category={data?.category}
              color={data?.color}
              toolTipText={data?.reasoning}
            />
          </Stack>
          <Stack gap={5} className="flex flex-col items-center justify-center">
            <Text className="text-neutrals-600 text-title-12">Status</Text>
            <Text className="text-body-14">
              {data.evaluationStatusText || "-"}
            </Text>
          </Stack>
        </Group>

        <Stack gap={5}>
          <Group align="center" gap={8}>
            <Text className="text-neutrals-600 text-title-12">
              Evaluation reasoning
            </Text>
            {data.reasoning && (
              <Tooltip
                label={reasoningCopied ? "Copied!" : "Copy to clipboard"}
              >
                <ActionIcon
                  size="sm"
                  variant="transparent"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleCopy(data.reasoning as string, setReasoningCopied);
                  }}
                >
                  <MaterialIcon
                    name={reasoningCopied ? "check" : "content_copy"}
                    size={14}
                    className={"text-neutrals-500"}
                  />
                </ActionIcon>
              </Tooltip>
            )}
          </Group>
          <Text className="whitespace-pre-line text-body-14">
            {data.reasoning || "-"}
          </Text>
        </Stack>

        <Stack gap={5}>
          <Group align="center" gap={8}>
            <Text className="text-neutrals-600 text-title-12">
              Full evaluator output
            </Text>
            {data.llmResponse && (
              <Tooltip label={responseCopied ? "Copied!" : "Copy to clipboard"}>
                <ActionIcon
                  size="sm"
                  variant="transparent"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleCopy(data.llmResponse as string, setResponseCopied);
                  }}
                >
                  <MaterialIcon
                    name={responseCopied ? "check" : "content_copy"}
                    size={14}
                    className={"text-neutrals-500"}
                  />
                </ActionIcon>
              </Tooltip>
            )}
          </Group>
          <Text className="whitespace-pre-line text-body-14">
            {data.llmResponse || "-"}
          </Text>
        </Stack>
        <Group className="justify-end">
          <Text
            onClick={(e) => {
              e.stopPropagation();
              window.open(
                `${routes.evaluatorGallery.root}/${data.evaluator_id}/view`,
                "_blank",
              );
            }}
            className="text-brand outline-none text-body-12 cursor-pointer"
          >
            View details
          </Text>
        </Group>
      </Stack>
    </Modal>
  );
}

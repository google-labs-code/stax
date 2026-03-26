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

import ModalFooterButton from "@/components/ModalFooter";
import ModalInput from "@/components/ModalInput";
import ModalInputLabel from "@/components/ModalInputLabel";
import { TOOLTIPS } from "@/config/constants";
import {
  updateLLMEvaluatorQuery,
  updateSxSLLMEvaluatorQuery,
} from "@/queries/clientQueries";
import { EvaluatorFormData, LLMEvaluatorResponse } from "@/queries/types";
import { ProjectType } from "@/types";
import LocalStorage from "@/utils/LocalStorage";
import { Modal, Stack, Text, Textarea } from "@mantine/core";
import { useMutation } from "@tanstack/react-query";
import { useParams } from "next/navigation";
import { Dispatch, SetStateAction, useEffect, useState } from "react";

import { EvaluatorPageAction, STORAGE_NEW_EVALUATOR_DATA } from "../../types";

type EditEvaluatorModalProps = {
  isOpened: boolean;
  onClose: (res: LLMEvaluatorResponse | null, type?: ProjectType) => void;
  data: EvaluatorFormData;
  setData: Dispatch<SetStateAction<EvaluatorFormData>>;
};
export default function EditEvaluatorModal({
  isOpened,
  onClose,
  data,
  setData,
}: EditEvaluatorModalProps) {
  const [name, setName] = useState(data?.name);
  const [description, setDescription] = useState(data?.description);
  const navParams = useParams();
  const isButtonDisabled = !name?.trim();
  const updatePointwiseEvaluator = useMutation({
    mutationFn: () =>
      updateLLMEvaluatorQuery({
        id: data[ProjectType.POINTWISE].id,
        name,
        description,
      }),
    onSuccess: (response) => {
      onClose(response, ProjectType.POINTWISE);
    },
    onError: () => {
      onClose(null);
    },
  });

  const updateSxSEvaluator = useMutation({
    mutationFn: () =>
      updateSxSLLMEvaluatorQuery({
        id: data[ProjectType.SIDE_BY_SIDE].id,
        name,
        description,
      }),
    onSuccess: (response) => {
      onClose(response, ProjectType.SIDE_BY_SIDE);
    },
    onError: () => {
      onClose(null);
    },
  });

  useEffect(() => {
    if (isOpened) {
      setName(data.name);
      setDescription(data.description);
    }
  }, [isOpened]);

  return (
    <Modal
      opened={isOpened}
      onClose={() => onClose(null)}
      closeButtonProps={{ "aria-label": "close button" }}
      size="700px"
      title={
        <Text className="flex items-center gap-2 !font-medium text-title-22">
          Edit evaluator
        </Text>
      }
      centered
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[32px]",
      }}
      data-testid="edit-evaluator-modal"
    >
      <Stack className="modal-form-two-rows-inputs">
        <Stack gap="6px">
          <ModalInputLabel label="Name" isRequired />
          <ModalInput
            placeholder="Evaluator name"
            value={name}
            onChange={(event) => {
              const newValue = event.currentTarget.value;
              setName(newValue);
              setData((prevData) => {
                return {
                  ...prevData,
                  name: newValue,
                };
              });
            }}
          />
        </Stack>

        <Stack gap="6px">
          <ModalInputLabel label="Description" />

          <Textarea
            className="w-[100%]"
            classNames={{
              input: `h-[110px] rounded-lg border-neutrals-300 !placeholder-resting`,
            }}
            value={description}
            placeholder="Optional description for evaluator"
            onChange={(event) => {
              const newValue = event.currentTarget.value;
              setDescription(newValue);
              setData((prevData) => {
                return {
                  ...prevData,
                  description: newValue,
                };
              });
            }}
          />
        </Stack>

        <ModalFooterButton
          label="Update evaluator"
          isDisabled={isButtonDisabled}
          isLoading={
            updatePointwiseEvaluator.isPending || updateSxSEvaluator.isPending
          }
          tooltipLabel={
            isButtonDisabled ? TOOLTIPS.EVALUATOR_NAME_REQUIRED : undefined
          }
          onClick={() => {
            if (navParams?.action === EvaluatorPageAction.EDIT) {
              updatePointwiseEvaluator.mutate();
              updateSxSEvaluator.mutate();
            } else {
              // Is new page
              if (!navParams?.action && !navParams?.id) {
                LocalStorage.set(STORAGE_NEW_EVALUATOR_DATA, {
                  name: data.name,
                  description: data.description,
                });
              }

              onClose(null);
            }
          }}
        />
      </Stack>
    </Modal>
  );
}

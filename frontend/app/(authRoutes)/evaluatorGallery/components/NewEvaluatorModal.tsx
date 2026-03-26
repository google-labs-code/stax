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
import { routes } from "@/config/routes";
import { GAevents } from "@/types";
import LocalStorage from "@/utils/LocalStorage";
import logGAevent from "@/utils/logGAevent";
import { Modal, Stack, Text, Textarea } from "@mantine/core";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

import { STORAGE_NEW_EVALUATOR_DATA } from "../types";

type NewEvaluatorModalProps = {
  isOpened: boolean;
  onClose: () => void;
};
export default function NewEvaluatorModal({
  isOpened,
  onClose,
}: NewEvaluatorModalProps) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const router = useRouter();

  const createEvaluatorAction = () => {
    logGAevent(GAevents.CREATE_CUSTOM_LLM_EVALUATOR);
    LocalStorage.set(STORAGE_NEW_EVALUATOR_DATA, {
      name,
      description,
    });
    router.push(routes.evaluatorGallery.new);
    onClose();
  };

  useEffect(() => {
    if (isOpened === true) {
      setName("");
      setDescription("");
    }
  }, [isOpened]);

  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      closeButtonProps={{ "aria-label": "close button" }}
      size="700px"
      title={
        <Text className="flex items-center gap-2 !font-medium text-title-22">
          New evaluator
        </Text>
      }
      centered
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[32px]",
      }}
    >
      <Stack className="modal-form-two-rows-inputs">
        <Stack gap="6px">
          <ModalInputLabel label="Name" isRequired />
          <ModalInput
            value={name}
            placeholder="Evaluator name"
            onChange={(event) => {
              setName(event.currentTarget.value);
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
              setDescription(event.currentTarget.value);
            }}
          />
        </Stack>

        <ModalFooterButton
          label="Create evaluator"
          isDisabled={!name.trim()}
          onClick={createEvaluatorAction}
        />
      </Stack>
    </Modal>
  );
}

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

import { Button, Modal, Text } from "@mantine/core";
import { TbTrash } from "react-icons/tb";

type DeleteModalProps = {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  isLoading: boolean;
  title: string;
  description: string;
};

export default function DeleteModal(props: DeleteModalProps) {
  const { isOpen, onClose, onConfirm, isLoading, title, description } = props;

  return (
    <Modal
      variant="modern"
      opened={isOpen}
      onClose={onClose}
      centered
      withCloseButton={false}
      closeOnClickOutside={false}
      data-testid="delete-modal"
    >
      <div data-testid="delete-modal-content-container">
        <div className="mb-[16px] flex justify-center">
          <div className="flex h-[80px] w-[80px] items-center justify-center gap-4 rounded-[50%] bg-brand-50">
            <TbTrash size={40} className="text-brand-500" />
          </div>
        </div>
        <div className="space-y-[16px]">
          <Text className="text-center text-primary text-title-20">
            {title}
          </Text>
          <Text className="text-center text-secondary text-body-14">
            {description}
          </Text>
        </div>
        <div className="gap-xl flex justify-between pt-[24px]">
          <Button variant="secondary" onClick={onClose} fullWidth size="lg">
            Close
          </Button>
          <Button
            size="lg"
            variant="danger"
            disabled={isLoading}
            onClick={onConfirm}
            fullWidth
            data-testid="delete-modal-confirm-button"
          >
            Confirm Delete
          </Button>
        </div>
      </div>
    </Modal>
  );
}

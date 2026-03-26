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

import { TOOLTIPS } from "@/config/constants";
import { getSuccessNotificationConfig } from "@/config/notifications";
import { deleteUserDataQuery } from "@/queries/clientQueries";
import { Button, Group, Modal, Text, TextInput, Tooltip } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { useState } from "react";

type DeleteDataModalProps = {
  isOpened: boolean;
  onClose: () => void;
};

export default function DeleteDataModal({
  isOpened,
  onClose,
}: DeleteDataModalProps) {
  const [confirmationText, setConfirmationText] = useState("");

  const deleteUserDataMutation = useMutation({
    mutationFn: deleteUserDataQuery,
    onSuccess: () => {
      notifications.show(
        getSuccessNotificationConfig(
          "All data deleted successfully!",
          "user-data-deleted",
        ),
      );
      onClose();
    },
    onError: () => {
      onClose();
    },
  });

  const handleClose = () => {
    setConfirmationText("");
    onClose();
  };

  const isDeleteEnabled = confirmationText === "delete";

  return (
    <Modal
      opened={isOpened}
      onClose={handleClose}
      centered
      size="700px"
      title={
        <Text className="!font-medium text-title-22">Delete Stax data</Text>
      }
      padding="24px"
      classNames={{
        header: "pb-[32px] min-h-auto",
      }}
    >
      <Text className="text-secondaryDark text-body-16 mb-[24px]">
        You are about to permanently delete your data. This action is
        irreversible and cannot be undone. Once confirmed, all of your
        information will be permanently erased and we will be unable to retrieve
        it.
        <br />
        <br />
        Please be aware that you may be unable to log in or use Stax while the
        deletion process is active.
      </Text>

      <TextInput
        label="Type 'delete' to confirm"
        placeholder="delete"
        value={confirmationText}
        onChange={(event) => setConfirmationText(event.currentTarget.value)}
        className="mb-[32px]"
      />

      <Group className="mt-[32px]" justify="flex-end">
        <Button w={175} size="lg" variant="outlineLight" onClick={handleClose}>
          Cancel
        </Button>

        {!isDeleteEnabled ? (
          <Tooltip
            label={TOOLTIPS.CONFIRM_DELETE_MESSAGE}
            position="bottom"
            withArrow
            withinPortal
          >
            <div>
              <Button w={175} size="lg" variant="brand" disabled={true}>
                Delete
              </Button>
            </div>
          </Tooltip>
        ) : (
          <Button
            w={175}
            size="lg"
            variant="brand"
            loading={deleteUserDataMutation.isPending}
            disabled={deleteUserDataMutation.isPending}
            onClick={() => deleteUserDataMutation.mutate()}
          >
            Delete
          </Button>
        )}
      </Group>
    </Modal>
  );
}

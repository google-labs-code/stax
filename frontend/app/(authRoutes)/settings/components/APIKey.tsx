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

import MaterialIcon from "@/components/MaterialIcon";
import {
  getErrorNotificationConfig,
  getSuccessNotificationConfig,
} from "@/config/notifications";
import { GAevents } from "@/types";
import logGAevent from "@/utils/logGAevent";
import { Button, Group, Stack, Text, TextInput } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import Link from "next/link";
import { ChangeEvent, useCallback, useState } from "react";

import { useModelsContext } from "../../../../hooks/useModelsContext";
import { addUserKeyQuery, deleteUserKeyQuery } from "../state/queries";
import { DeleteKeyData, KeyDetails } from "../types";

type APIKeyProps = {
  keyData: KeyDetails;
  onChange: () => void;
};

export default function APIKey(props: APIKeyProps) {
  const { keyData, onChange } = props;
  const [apiKey, setApiKey] = useState<string>("");
  const queryClient = useQueryClient();
  const { refreshModels } = useModelsContext();

  const changeAPIKey = (e: ChangeEvent<HTMLInputElement>) => {
    setApiKey(e.target.value);
  };

  const addKeyMutation = useMutation({
    mutationFn: addUserKeyQuery,
    onSuccess: (_, variables) => {
      notifications.show(
        getSuccessNotificationConfig(
          "API key added successfully.",
          `user-key-added-${variables.provider}`,
        ),
      );
      setApiKey(`${keyData.label.toLowerCase().replaceAll(" ", "-")}-****`);
      queryClient.invalidateQueries({ queryKey: ["user-api-keys"] });
      refreshModels();
      onChange();
    },
  });

  const deleteKeyMutation = useMutation({
    mutationFn: deleteUserKeyQuery,
    onSuccess: (_, variables) => {
      setApiKey("");
      notifications.show(
        getSuccessNotificationConfig(
          "API key deleted successfully.",
          `user-key-deleted-${variables.provider}`,
        ),
      );
      queryClient.invalidateQueries({ queryKey: ["user-api-keys"] });
      refreshModels();
      onChange();
    },
  });

  const saveAPIKey = () => {
    if (!apiKey.trim()) {
      notifications.show(
        getErrorNotificationConfig(
          "Key cannot be empty!",
          `${keyData.provider}-key-empty`,
        ),
      );

      return;
    }
    const data = {
      provider: keyData.provider,
      key: apiKey,
    };
    logGAevent(GAevents.ADDS_API_KEY, {
      provider: data.provider,
    });
    addKeyMutation.mutate(data);
  };

  const handleEnterPress = useCallback(
    (event: React.KeyboardEvent<HTMLInputElement>) => {
      if (event.key === "Enter") {
        saveAPIKey();
      }
    },
    [saveAPIKey],
  );

  const deleteAPIKey = () => {
    const deleteData: DeleteKeyData = {
      provider: keyData.provider,
    };
    deleteKeyMutation.mutate(deleteData);
  };

  return (
    <Stack
      gap="xs"
      data-testid={`${keyData.provider}-key-container`}
      className="gap-md"
    >
      <Group gap={4}>
        <Text className="text-body-14">{keyData.label}</Text>
      </Group>
      <Group gap="32px">
        <TextInput
          variant="modern"
          className="!h-[40px] grow"
          classNames={{
            wrapper: "!h-[40px] rounded-sm",
            input: `min-h-[32px] py-[8px] ${keyData.isKeyPresent && "bg-neutrals-100 border-solid border border-neutrals-300 opacity-100"}`,
          }}
          placeholder="Add your key"
          disabled={keyData.isKeyPresent}
          value={
            keyData.isKeyPresent
              ? `${keyData.label.toLowerCase().replaceAll(" ", "-")}-****`
              : apiKey
          }
          onKeyDown={handleEnterPress}
          onChange={changeAPIKey}
          data-testid="api-key-input-field"
        />
        {keyData.isKeyPresent ? (
          <Button
            onClick={deleteAPIKey}
            loading={deleteKeyMutation.isPending}
            data-testid="delete-key-button"
            variant="default"
            className="h-[40px] !px-[12px] hover:bg-lightSilver"
            classNames={{
              label: "gap-sm",
            }}
          >
            <MaterialIcon name="delete" className="text-secondaryDark" />
            <Text className="text-secondaryDark text-title-14">Delete key</Text>
          </Button>
        ) : (
          <Button
            onClick={saveAPIKey}
            loading={addKeyMutation.isPending}
            data-testid="add-key-button"
            variant="default"
            className="h-[40px] !px-[12px] hover:bg-lightSilver"
            classNames={{
              label: "gap-sm",
            }}
          >
            <MaterialIcon name="add" className="text-secondaryDark" />
            <Text className="text-secondaryDark text-title-14">Add key</Text>
          </Button>
        )}
      </Group>
      <Group gap={0} className="h-[24px]">
        <Link
          href={keyData.link}
          className="!font-bold text-brand text-body-12"
          target="_blank"
        >
          Get {keyData.label} API Key
        </Link>
      </Group>
    </Stack>
  );
}

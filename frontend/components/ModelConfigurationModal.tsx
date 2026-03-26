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

import FieldMaxTokens from "@/components/Model/FieldMaxTokens";
import FieldTemperature from "@/components/Model/FieldTemperature";
import FieldTopP from "@/components/Model/FieldTopP";
import InputMinMaxPlaceholder from "@/components/Model/InputMinMaxPlaceholder";
import InputRightResetButton from "@/components/Model/InputRightResetButton";
import ModelFieldName from "@/components/ModelFieldName";
import {
  getErrorNotificationConfig,
  getSuccessNotificationConfig,
} from "@/config/notifications";
import { routes } from "@/config/routes";
import { useChatContext } from "@/hooks/useChatContext";
import { useModelsContext } from "@/hooks/useModelsContext";
import { duplicateModelQuery } from "@/queries/clientQueries";
import { Model, ModelFormValues } from "@/queries/types";
import { Chat } from "@/types";
import {
  Button,
  Group,
  Modal,
  NumberInput,
  Stack,
  Text,
  UnstyledButton,
} from "@mantine/core";
import { useForm } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { ReactNode, useEffect } from "react";

type ModelSettingsModalProps = {
  chat?: Chat;
  isOpened: boolean;
  onClose: () => void;
};

const FieldContainer = ({ children }: { children: ReactNode }) => (
  <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
    {children}
  </div>
);

export default function ModelConfigurationModal({
  chat,
  isOpened,
  onClose,
}: ModelSettingsModalProps) {
  const router = useRouter();
  const { openedModel, refreshModels } = useModelsContext();
  const { updateChat } = useChatContext();

  const inputClassNames = "w-[427px] h-[40px] rounded-lg";
  const form = useForm<ModelFormValues>({
    initialValues: {
      id: "",
      temperature: undefined,
      maxTokens: undefined,
      topP: undefined,
      seed: undefined,
    },
  });

  const setDefaultValues = () => {
    if (!openedModel) {
      return;
    }

    const updatedModel: ModelFormValues = {
      id: openedModel.id,
      temperature:
        openedModel?.properties?.temperature ||
        openedModel?.descriptors?.temperature?.defaultValue,
      maxTokens:
        openedModel?.properties?.max_tokens ||
        openedModel?.descriptors?.max_output_tokens?.defaultValue,
      topP:
        openedModel?.properties?.top_p ||
        openedModel?.descriptors?.top_p?.defaultValue,
      seed:
        openedModel?.properties?.seed ||
        openedModel?.descriptors?.seed?.defaultValue,
    };
    form.setInitialValues(updatedModel);
    form.setValues(updatedModel);
  };

  useEffect(() => {
    setDefaultValues();
  }, [openedModel]);

  const duplicateModelMutation = useMutation({
    mutationFn: (data: ModelFormValues) => duplicateModelQuery(data),
    onSuccess: (model: Model) => {
      refreshModels();
      if (chat?.id && model) {
        updateChat(chat.id, "model", model);
      }
      const MODEL_UPDATED_NOTIFICATION_ID = "model-updated-confirmation";

      notifications.show(
        getSuccessNotificationConfig(
          <div className="flex flex-row items-center justify-between gap-xl">
            <Text>
              Model configurations saved under &apos;Custom&apos; in model
              dropdown. Edit the name in Settings &gt; Model Manager.
            </Text>

            <UnstyledButton
              className="!font-medium text-body-14"
              onClick={() => {
                router.push(`${routes.settings}?tab=modelManager`);
                notifications.hide(MODEL_UPDATED_NOTIFICATION_ID);
              }}
            >
              Settings
            </UnstyledButton>
          </div>,
          MODEL_UPDATED_NOTIFICATION_ID,
          4000,
        ),
      );
      onClose();
    },
    onError: (err) => {
      notifications.show(
        getErrorNotificationConfig(err.message, "model-updated"),
      );
      onClose();
    },
  });

  const duplicateModel = () => {
    const validationResult = form.validate();
    if (validationResult.hasErrors) {
      return;
    }
    duplicateModelMutation.mutate(form.values);
  };

  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      size="700px"
      title={
        <Text className="!font-medium text-title-22">{openedModel?.label}</Text>
      }
      centered
      zIndex={500}
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[32px]",
      }}
      data-testid="model-configuration-modal"
    >
      <form id="model-settings">
        <Stack gap="2rem">
          <div className="flex flex-col gap-4 md:gap-2">
            <FieldTemperature
              form={form}
              descriptors={openedModel?.descriptors}
            />
            <FieldMaxTokens
              form={form}
              descriptors={openedModel?.descriptors}
            />

            <FieldTopP form={form} descriptors={openedModel?.descriptors} />

            <FieldContainer>
              <ModelFieldName
                name="Seed"
                tooltip={openedModel?.descriptors?.seed?.description}
              />

              <NumberInput
                classNames={{
                  input: inputClassNames,
                  root: "relative",
                  wrapper: "mb-0",
                  error: "hidden",
                }}
                allowDecimal={false}
                hideControls
                {...form.getInputProps("seed")}
                rightSection={
                  <InputRightResetButton
                    form={form}
                    descriptors={openedModel?.descriptors}
                    descriptorKey="seed"
                    fieldName="seed"
                  />
                }
                rightSectionProps={{
                  className: "mr-[16px]",
                }}
                placeholder={InputMinMaxPlaceholder({
                  hasError: form.errors["seed"] ? true : false,
                  descriptors: openedModel?.descriptors,
                  descriptorKey: "seed",
                })}
                onChange={(value) => {
                  form.setFieldValue("seed", value as number | undefined);
                  if (typeof value === "number") {
                    const min = openedModel?.descriptors?.seed?.minValue;
                    const max = openedModel?.descriptors?.seed?.maxValue;

                    if (min && max && (value < min || value > max)) {
                      form.setFieldError("seed", "error");
                    } else {
                      form.clearFieldError("seed");
                    }
                  } else if (!value) {
                    form.setFieldError("seed", "error");
                  }
                }}
                onBlur={() => {
                  //
                }}
              />
            </FieldContainer>
          </div>

          <Group gap="xs" justify="space-between">
            <Group gap="4px">
              <Text className="mb-[-2px] text-red">*</Text>
              <Text className="text-secondary text-body-11">
                Required information
              </Text>
            </Group>
            <Group>
              <Button
                size="lg"
                variant="transparent"
                className="h-[48px] text-secondaryDark hover:text-brand"
                onClick={() => {
                  setDefaultValues();
                  form.reset();
                }}
              >
                Reset
              </Button>
              <Button
                size="lg"
                className="h-[48px] large-btn"
                loading={duplicateModelMutation.isPending}
                onClick={duplicateModel}
                disabled={!form.isDirty() || !!Object.keys(form.errors).length}
              >
                Save
              </Button>
            </Group>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
}

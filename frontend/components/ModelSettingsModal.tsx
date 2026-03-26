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

import {
  getErrorNotificationConfig,
  getSuccessNotificationConfig,
} from "@/config/notifications";
import { useModelsContext } from "@/hooks/useModelsContext";
import { duplicateModelQuery } from "@/queries/clientQueries";
import {
  Model,
  ModelDescriptors,
  ModelFormValues,
  ModelTypeEnum,
} from "@/queries/types";
import { ModelSettingsModalType } from "@/types";
import { getUniqueModelLabel } from "@/utils/getUniqueModelLabel";
import {
  Button,
  Divider,
  Group,
  Modal,
  NumberInput,
  Select,
  Stack,
  Text,
  TextInput,
  Textarea,
} from "@mantine/core";
import { isNotEmpty, useForm } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { useEffect, useMemo, useState } from "react";

import MaterialIcon from "./MaterialIcon";
import FieldContainer from "./Model/FieldContainer";
import FieldMaxTokens from "./Model/FieldMaxTokens";
import FieldTemperature from "./Model/FieldTemperature";
import FieldTopP from "./Model/FieldTopP";
import InputMinMaxPlaceholder from "./Model/InputMinMaxPlaceholder";
import InputRightResetButton from "./Model/InputRightResetButton";
import ModelFieldName from "./ModelFieldName";

type ModelSettingsModalProps = {
  isOpened: boolean;
  onClose: () => void;
  type: ModelSettingsModalType;
  model: Model | null;
};

const initialValues: ModelFormValues = {
  id: "",
  label: "",
  name: "",
  description: "",
  systemsInstruction: "",
  // tags: [],
  temperature: 0,
  maxTokens: 0,
  presenceFrequency: 0,
  penaltyFrequency: 0,
  topP: 0,
};

export default function ModelSettingsModal({
  isOpened,
  onClose,
  type,
  model,
}: ModelSettingsModalProps) {
  const [descriptors, setDescriptors] = useState<ModelDescriptors>();
  const { allModels, refreshModels, defaultModels } = useModelsContext();

  const inputClassNames = "w-[427px] h-[40px] rounded-lg";
  const form = useForm({
    validateInputOnBlur: true,
    initialValues,
    validate: {
      label: isNotEmpty(""),
      name: isNotEmpty(""),
      maxTokens: isNotEmpty(""),
      temperature: isNotEmpty(""),
    },
  });

  const setDefaultValues = (model: Model) => {
    const parsedModel: ModelFormValues = {
      ...initialValues,
      ...model,
      temperature:
        model.properties?.temperature ||
        model.descriptors?.temperature?.defaultValue ||
        0,
      maxTokens:
        model.properties?.max_tokens ||
        model.descriptors?.max_output_tokens?.defaultValue ||
        0,
      topP:
        model.properties?.top_p || model.descriptors?.top_p?.defaultValue || 0,
      seed: model.properties?.seed || model.descriptors?.seed?.defaultValue,
    };

    form.setInitialValues(parsedModel);
    form.setValues(parsedModel);
    setDescriptors(model?.descriptors || {});
    form.reset();
  };

  useEffect(() => {
    setDefaultValues(model ? model : ({} as Model));
  }, [isOpened]);

  const systemModels = useMemo(() => {
    const filteredModels: string[] = [];
    allModels
      .filter(({ model_type }) => model_type === ModelTypeEnum.SYSTEM)
      .map(({ name }) => name)
      .sort((a, b) => (a.toLocaleLowerCase() < b.toLocaleLowerCase() ? -1 : 1))
      .forEach((model) => {
        if (!filteredModels.includes(model)) {
          filteredModels.push(model);
        }
      });

    return filteredModels;
  }, [allModels]);

  const duplicateModelMutation = useMutation({
    mutationFn: (data: ModelFormValues) => {
      return duplicateModelQuery({
        ...data,
        label: getUniqueModelLabel(data?.label || ""),
      });
    },
    onSuccess: () => {
      refreshModels();
      notifications.show(
        getSuccessNotificationConfig(
          "Model added successfully.",
          "model-added",
        ),
      );
      onClose();
    },
    onError: (err) => {
      notifications.show(
        getErrorNotificationConfig(err.message, "model-added"),
      );
      onClose();
    },
  });

  const submitForm = () => {
    const validationResult = form.validate();
    if (validationResult.hasErrors) {
      return;
    }

    if (
      type === ModelSettingsModalType.DUPLICATE ||
      type === ModelSettingsModalType.ADD
    ) {
      duplicateModelMutation.mutate(form.values);
    }
  };

  return (
    <Modal
      opened={isOpened}
      onClose={() => {
        setDescriptors({});
        form.reset();
        onClose();
      }}
      size="700px"
      title={
        <Group gap={0}>
          <Text className="!font-medium text-title-22">
            Model Configuration
          </Text>
          {type === ModelSettingsModalType.DUPLICATE && form.isDirty() && (
            <Group className="ml-[24px] flex flex-row items-center gap-0 rounded-sm bg-lightBlue px-[8px] py-[4px]">
              <MaterialIcon name="info" size={16} className="text-brand" />
              <Text className="ml-[4px] !font-medium uppercase text-brand text-body-11">
                modified configuration
              </Text>
            </Group>
          )}
        </Group>
      }
      centered
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[32px]",
      }}
      data-testid="model-settings-modal"
    >
      <form id="model-settings">
        <Stack gap="2rem">
          <div className="flex flex-col gap-md">
            <FieldContainer>
              <ModelFieldName name="Model nickname" isRequired={true} />

              <TextInput
                {...form.getInputProps("label")}
                classNames={{
                  input: inputClassNames,
                  wrapper: "mb-0",
                  error: "hidden",
                }}
                value={form.values.label}
                onChange={(event) =>
                  form.setFieldValue("label", event.currentTarget.value)
                }
              />
            </FieldContainer>

            <FieldContainer>
              <ModelFieldName name="Description" />
              <Textarea
                {...form.getInputProps("description")}
                classNames={{
                  input: `${inputClassNames} !h-[80px]`,
                }}
                value={form.values.description}
                onChange={(event) =>
                  form.setFieldValue("description", event.currentTarget.value)
                }
              />
            </FieldContainer>

            {/* <Accordion variant="separate">
              <Accordion.Item value="tags">
                <Accordion.Control>Add Model Tags (optional)</Accordion.Control>
                <Accordion.Panel>
                  <ManageTagsContainer
                    showMyTags={false}
                    onAddAction={(tags) => form.setFieldValue("tags", tags)}
                  />
                </Accordion.Panel>
              </Accordion.Item>
            </Accordion> */}

            <Divider my="md" />

            <FieldContainer>
              <ModelFieldName name="Model version" isRequired={true} />
              <Select
                disabled={
                  type === ModelSettingsModalType.DUPLICATE ||
                  type === ModelSettingsModalType.EDIT
                }
                {...form.getInputProps("name")}
                data={systemModels}
                value={form.values.name}
                classNames={{
                  input: inputClassNames,
                  root: "relative",
                  wrapper: "mb-0",
                  error: "hidden",
                }}
                onChange={(modelName) => {
                  const finalModel = defaultModels.find(
                    ({ name }) => name === modelName,
                  );
                  if (finalModel) {
                    setDefaultValues(finalModel);
                  }
                }}
              />
            </FieldContainer>

            <FieldTemperature form={form} descriptors={descriptors} />

            <FieldMaxTokens form={form} descriptors={descriptors} />

            <FieldTopP form={form} descriptors={descriptors} />

            <FieldContainer>
              <ModelFieldName
                name="Seed"
                tooltip={descriptors?.seed?.description}
              />

              <NumberInput
                {...form.getInputProps("seed")}
                classNames={{
                  input: inputClassNames,
                  wrapper: "mb-0",
                }}
                rightSection={
                  <InputRightResetButton
                    form={form}
                    descriptors={model?.descriptors}
                    descriptorKey="seed"
                    fieldName="seed"
                  />
                }
                rightSectionProps={{
                  className: "mr-[16px]",
                }}
                placeholder={InputMinMaxPlaceholder({
                  hasError: form.errors["seed"] ? true : false,
                  descriptors: model?.descriptors,
                  descriptorKey: "seed",
                })}
                onChange={(value) => {
                  form.setFieldValue("seed", value as number | undefined);
                  if (typeof value === "number") {
                    const min = model?.descriptors?.seed?.minValue;
                    const max = model?.descriptors?.seed?.maxValue;

                    if (min && max && (value < min || value > max)) {
                      form.setFieldError("seed", "error");
                    } else {
                      form.clearFieldError("seed");
                    }
                  } else if (!value) {
                    form.setFieldError("seed", null);
                  }
                }}
              />
            </FieldContainer>
          </div>
          <div className={`flex items-center justify-between`}>
            <Group gap="4px">
              <Text className="mb-[-2px] text-red">*</Text>
              <Text className="text-secondary text-body-11">
                Required information
              </Text>
            </Group>

            <Group className="flex flex-row items-center justify-between gap-md">
              {type === ModelSettingsModalType.DUPLICATE &&
                form.isDirty() &&
                model && (
                  <Button
                    variant="outlineLight"
                    className="!h-[48px] min-w-[160px] px-[24px] py-[12px]"
                    onClick={() => {
                      setDefaultValues(model);
                    }}
                  >
                    Reset
                  </Button>
                )}
              <Button
                size="lg"
                className="h-[48px] large-btn"
                onClick={submitForm}
                loading={duplicateModelMutation.isPending}
                disabled={
                  (!form.isDirty() && type !== ModelSettingsModalType.ADD) ||
                  !!Object.keys(form.errors).length
                }
              >
                {type === ModelSettingsModalType.DUPLICATE && form.isDirty()
                  ? "Save as copy"
                  : model
                    ? "Save"
                    : "Add"}
              </Button>
            </Group>
          </div>
        </Stack>
      </form>
    </Modal>
  );
}

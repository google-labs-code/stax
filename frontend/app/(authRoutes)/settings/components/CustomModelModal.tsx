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
import FieldContainer from "@/components/Model/FieldContainer";
import ModelFieldName from "@/components/ModelFieldName";
import { TOOLTIPS, getModelDetails } from "@/config/constants";
import { getSuccessNotificationConfig } from "@/config/notifications";
import { useModelsContext } from "@/hooks/useModelsContext";
import { addCustomModelQuery, updateModelQuery } from "@/queries/clientQueries";
import { CustomModelPayload, Model, UpdateModelPayload } from "@/queries/types";
import { CustomModelModalType } from "@/types";
import { getUniqueModelLabel } from "@/utils/getUniqueModelLabel";
import {
  Button,
  Divider,
  Group,
  Menu,
  Modal,
  Stack,
  Text,
  TextInput,
  Textarea,
  Tooltip,
  UnstyledButton,
} from "@mantine/core";
import { isNotEmpty, useForm } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { useCallback, useEffect, useMemo, useState } from "react";

import CustomModelModalAdditionalDetails from "./CustomModelModalAdditionalDetails";
import ModelMenuDropdown from "./ModelMenuDropdown";

interface CustomModelEditingModalProps {
  readonly isOpened: boolean;
  readonly onClose: () => void;
  readonly model: Model | null;
  readonly type: CustomModelModalType;
}

export default function CustomModelModal({
  isOpened,
  onClose,
  model,
  type,
}: CustomModelEditingModalProps) {
  const [showAdditionalDetails, setShowAdditionalHeaders] = useState(false);
  const { defaultModels, customModels, refreshModels } = useModelsContext();
  const [additionalHeaders, setAdditionalHeaders] = useState<
    { key: string; value: string }[]
  >([{ key: "", value: "" }]);
  const [menuOpened, setMenuOpened] = useState(false);
  const inputClassNames = "w-[427px] h-[40px] rounded-lg";
  const isLabelUnique = (label: string) =>
    label &&
    !![...defaultModels, ...customModels].find(
      (m) => m.label === label.trim() && m.id !== model?.id,
    );

  const initialValues: CustomModelPayload = useMemo(
    () => ({
      name: model?.name || "",
      label: model?.label || "",
      description: model?.description || "",
      api_key: model?.api_key || "",
      url: model?.url || "",
      supported_provider: model?.provider || "",
      properties: {
        temperature: model?.properties?.temperature,
        seed: model?.properties?.seed,
        max_tokens: model?.properties?.max_tokens,
        top_p: model?.properties?.top_p,
      },
    }),
    [model],
  );

  const form = useForm({
    validateInputOnBlur: true,
    initialValues,
    validate: {
      label: (value: string) => isLabelUnique(value),
      url: (value: string) =>
        /^https?:\/\/(www\.)?[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}(\/[^\s]*)?$/.test(
          value,
        )
          ? null
          : "Invalid API URL",
      supported_provider:
        type === CustomModelModalType.ADD ? isNotEmpty("") : undefined,
    },
  });

  const onResetForm = useCallback(() => {
    form.setValues(initialValues);
  }, [form, initialValues]);

  const onCloseHandle = useCallback(() => {
    form.reset();
    onClose();
    setShowAdditionalHeaders(false);
  }, [onClose, form, showAdditionalDetails]);

  const onSuccess = () => {
    notifications.show(
      getSuccessNotificationConfig("Model added successfully.", "model-added"),
    );
    refreshModels();
    onCloseHandle();
  };

  const addCustomModelMutation = useMutation({
    mutationFn: addCustomModelQuery,
    onSuccess: onSuccess,
    onError: () => {
      onCloseHandle();
    },
  });

  const updateCustomModelMutation = useMutation({
    mutationFn: (payload: UpdateModelPayload) =>
      updateModelQuery(payload, model?.id || ""),
    onSuccess: () => {
      form.reset();
      onSuccess();
    },
    onError: () => {
      onCloseHandle();
    },
  });

  const submitForm = useCallback(() => {
    const validationResult = form.validate();
    if (validationResult.hasErrors) {
      return;
    }

    const headersObject = additionalHeaders.reduce<{ [key: string]: string }>(
      (acc, header) => {
        if (header.key && header.value) {
          acc[header.key] = header.value;
        }

        return acc;
      },
      {},
    );

    const formData: CustomModelPayload = {
      ...form.values,
      label: form.values.label?.trim(),
      api_key: form.values.api_key ? form.values.api_key : undefined,
      additional_headers: headersObject,
    };
    if (
      type === CustomModelModalType.EDIT ||
      type === CustomModelModalType.SHOW
    ) {
      updateCustomModelMutation.mutate(formData);
    } else if (type === CustomModelModalType.ADD) {
      addCustomModelMutation.mutate(formData);
    }
  }, [additionalHeaders, form]);

  // const handleDeleteHeader = (index: number) => {
  //   setAdditionalHeaders((prevHeaders) => {
  //     return prevHeaders.filter((_, i) => i !== index);
  //   });
  //   setAdditionalHeadersTouched(true);
  // };

  // const handleHeaderChange = (
  //   index: number,
  //   field: "key" | "value",
  //   value: string,
  // ) => {
  //   setAdditionalHeaders((prevHeaders) => {
  //     const updatedHeaders = [...prevHeaders];
  //     updatedHeaders[index] = { ...updatedHeaders[index], [field]: value };

  //     return updatedHeaders;
  //   });

  //   if (type !== CustomModelModalType.ADD) {
  //     setAdditionalHeadersTouched(true);
  //   }
  // };

  const isSubmitDisabled = useMemo(
    () =>
      !form.isValid() ||
      JSON.stringify(initialValues) == JSON.stringify(form.values),
    [form, initialValues],
  );

  const ModelIcon = useMemo(
    () =>
      getModelDetails(model?.provider || form.values.supported_provider || "")
        ?.icon,
    [form],
  );

  useEffect(() => {
    if (model?.additional_headers) {
      setAdditionalHeaders(
        Object.entries(model?.additional_headers)?.map(([key, value]) => ({
          key,
          value,
        })),
      );
    }
  }, [model?.additional_headers]);

  useEffect(() => {
    if (!isOpened || !model) {
      return;
    }

    form.setValues({
      label:
        type === CustomModelModalType.ADD
          ? getUniqueModelLabel(model?.label)
          : model?.label,
      description: model?.description || "",
      api_key: model?.api_key || "",
      url: model?.url || "",
      supported_provider: model?.provider || "",
      properties: {
        temperature: model?.properties?.temperature,
        seed: model?.properties?.seed,
        max_tokens: model?.properties?.max_tokens,
        top_p: model?.properties?.top_p,
      },
    });
  }, [isOpened, model]);

  const title = useMemo(() => {
    if (type === CustomModelModalType.EDIT) {
      return "Edit custom model";
    } else if (type === CustomModelModalType.SHOW) {
      return "Custom model details";
    } else if (type === CustomModelModalType.ADD) {
      return model ? "Customize copied model" : "Add custom model";
    }
  }, [type, model]);

  return (
    <Modal
      opened={isOpened}
      onClose={onCloseHandle}
      size="700px"
      title={
        <Group gap={0}>
          <Text className="!font-medium text-title-22">{title}</Text>
          <MaterialIcon
            name="info"
            size={20}
            className="ml-[5px] cursor-pointer"
            tooltipClassName="max-w-[300px]"
            tooltipLabel="Add your own fine-tuned AI models or specialized agents by providing an API url. The custom model must return data in the same schema as chosen model provider."
          />
        </Group>
      }
      centered
      padding={24}
      classNames={{
        header: "p-5",
      }}
    >
      <form id="model-settings">
        <Stack gap="2rem">
          <div className="flex flex-col gap-md">
            <FieldContainer>
              <ModelFieldName
                name="Model Nickname"
                isRequired={true}
                tooltip="A unique name for your model that will appear throughout Stax."
              />
              <Group className="flex-col" gap={4}>
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
                {form.errors["label"] && (
                  <Group gap={2} className="self-start ml-1">
                    <span className="text-red">*</span>
                    <Text className="text-secondary text-body-11">
                      Label &quot;{form.values.label}&quot; already exists.
                    </Text>
                  </Group>
                )}
              </Group>
            </FieldContainer>
            <FieldContainer>
              <ModelFieldName
                name="API url"
                isRequired={true}
                tooltip="The endpoint URL where your hosted model can be accessed."
              />
              <Group className="flex-col" gap={4}>
                <TextInput
                  {...form.getInputProps("url")}
                  classNames={{
                    input: inputClassNames,
                    wrapper: "mb-0",
                    error: "hidden",
                  }}
                  value={form.values.url}
                  onChange={(event) =>
                    form.setFieldValue("url", event.currentTarget.value)
                  }
                />
                {form.errors["url"] && form.values.url && (
                  <Group gap={2} className="self-start ml-1">
                    <span className="text-red">*</span>
                    <Text className="text-secondary text-body-11">
                      {form.errors["url"]}
                    </Text>
                  </Group>
                )}
              </Group>
            </FieldContainer>
            <FieldContainer>
              <ModelFieldName
                name="API Key"
                tooltip="The authentication key needed to make authorized calls to your model's endpoint."
              />
              <TextInput
                {...form.getInputProps("api_key")}
                classNames={{
                  input: inputClassNames,
                  wrapper: "mb-0",
                  error: "hidden",
                }}
                value={form.values.api_key}
                onChange={(event) =>
                  form.setFieldValue("api_key", event.currentTarget.value)
                }
              />
            </FieldContainer>
            <FieldContainer>
              <ModelFieldName
                name="Model Provider"
                isRequired={type === CustomModelModalType.ADD}
                tooltip="Select a provider (e.g., OpenAI, Anthropic) whose output format your model's responses match. This is required for compatibility."
              />
              {type === CustomModelModalType.ADD ? (
                <Menu
                  position="bottom-end"
                  shadow="md"
                  opened={menuOpened}
                  onChange={setMenuOpened}
                >
                  <Menu.Target>
                    <Group
                      h={40}
                      w={427}
                      className="rounded-sm border border-solid border-lightSilver justify-between items-center px-3 cursor-pointer flex-row"
                    >
                      <Group className="justify-start items-center" gap={8}>
                        <Text w={24}>{ModelIcon && <ModelIcon />}</Text>
                        <Text className="text-title-14">
                          {
                            getModelDetails(
                              form.values?.supported_provider || "",
                            )?.name
                          }
                        </Text>
                      </Group>
                      <MaterialIcon name="keyboard_arrow_down" size={20} />
                    </Group>
                  </Menu.Target>
                  <ModelMenuDropdown
                    width={427}
                    onClick={(provider: string) => {
                      form.setFieldValue("supported_provider", provider);
                      setMenuOpened(false);
                    }}
                  />
                </Menu>
              ) : (
                <Tooltip
                  label={"The provider cannot be changed in edit mode."}
                  className="max-w-[250px]"
                  multiline
                >
                  <Group
                    h={44}
                    w={427}
                    className="rounded-sm border border-solid border-lightSilver px-3 cursor-not-allowed"
                    gap={8}
                  >
                    <Text w={24}>{ModelIcon && <ModelIcon size={20} />}</Text>
                    <Text className="text-title-14 text-secondary">
                      {getModelDetails(model?.provider || "")?.name}
                    </Text>
                  </Group>
                </Tooltip>
              )}
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

            {type !== CustomModelModalType.EDIT && (
              <>
                <Group className="flex-row justify-center my-2">
                  <Divider my="md" className="flex-1" />
                  <UnstyledButton
                    className="flex flex-1 justify-center items-center text-title-14"
                    onClick={() => setShowAdditionalHeaders((prev) => !prev)}
                  >
                    {showAdditionalDetails ? "Hide" : "Show"} additional details
                    <MaterialIcon
                      name={
                        showAdditionalDetails
                          ? "keyboard_arrow_up"
                          : "keyboard_arrow_down"
                      }
                      size={20}
                    />
                  </UnstyledButton>
                  <Divider my="md" className="flex-1" />
                </Group>
                <div
                  className={`transition-all duration-300 ease-in-out overflow-hidden ${
                    showAdditionalDetails
                      ? "max-h-[2000px] opacity-100"
                      : "max-h-0 opacity-0"
                  }`}
                >
                  {/* <Group className="flex-col" gap={8}>
                <Group className="justify-start w-full flex-row flex-nowrap">
                  <Group className="flex-1">
                    <Text className="!text-secondaryDark !font-medium text-body-14">
                      Header Name
                    </Text>
                  </Group>
                  <Group className="flex-1">
                    <Text className="!text-secondaryDark !font-medium text-body-14">
                      Header Value
                    </Text>
                  </Group>
                  <Group w={20} />
                </Group>
                {additionalHeaders.map((header, index) => (
                  <Group key={index} className="w-full flex-row flex-nowrap">
                    <Group className="flex-1">
                      <TextInput
                        classNames={{
                          root: "w-full",
                          input: "h-[40px] rounded-lg",
                          wrapper: "mb-0",
                          error: "hidden",
                        }}
                        value={header.key}
                        onChange={(e) =>
                          handleHeaderChange(index, "key", e.target.value)
                        }
                      />
                    </Group>
                    <Group className="flex-1">
                      <TextInput
                        classNames={{
                          root: "w-full",
                          input: "h-[40px] rounded-lg",
                          wrapper: "mb-0",
                          error: "hidden",
                        }}
                        value={header.value}
                        onChange={(e) =>
                          handleHeaderChange(index, "value", e.target.value)
                        }
                      />
                    </Group>
                    <Group>
                      <MaterialIcon
                        name="delete"
                        className="cursor-pointer"
                        size={20}
                        onClick={() => handleDeleteHeader(index)}
                      />
                    </Group>
                  </Group>
                ))}
                <Group className="w-full justify-start">
                  <Button
                    variant="outlineLight"
                    className="w-min-[120px] text-secondaryDark !p-0 !font-medium !text-body-14"
                    onClick={() => {
                      setAdditionalHeaders([
                        ...additionalHeaders,
                        { key: "", value: "" },
                      ]);
                    }}
                  >
                    <MaterialIcon name="add" />
                    Add Header
                  </Button>
                </Group>
              </Group> */}
                  {type === CustomModelModalType.ADD && (
                    <CustomModelModalAdditionalDetails
                      isDisabled={false}
                      form={form}
                      inputClassNames={inputClassNames}
                    />
                  )}

                  {type === CustomModelModalType.SHOW && (
                    <Tooltip label="The properties cannot be changed in show mode.">
                      <Group className="w-full gap-0">
                        <CustomModelModalAdditionalDetails
                          isDisabled={true}
                          form={form}
                          inputClassNames={inputClassNames}
                        />
                      </Group>
                    </Tooltip>
                  )}
                </div>
              </>
            )}
          </div>
          <div className={`flex items-center justify-between`}>
            <Group gap={2}>
              <span className="text-red">*</span>
              <Text className="text-secondary text-body-11">
                Required information
              </Text>
            </Group>

            <Group className="flex flex-row items-center justify-between gap-md">
              {!isSubmitDisabled && type !== CustomModelModalType.ADD && (
                <Button
                  variant="outlineLight"
                  className="!h-[48px] min-w-[160px] px-[24px] py-[12px]"
                  onClick={onResetForm}
                >
                  Reset
                </Button>
              )}

              {isSubmitDisabled ? (
                <Tooltip
                  label={
                    !form.isValid()
                      ? TOOLTIPS.CUSTOM_MODEL_VALIDATION_ERROR
                      : TOOLTIPS.CUSTOM_MODEL_NO_CHANGES
                  }
                  position="top"
                  withArrow
                  zIndex={1000}
                  withinPortal
                >
                  <Button
                    size="lg"
                    h={48}
                    className="large-btn"
                    disabled={true}
                  >
                    {type === CustomModelModalType.ADD
                      ? "Add Custom Model"
                      : "Update Model"}
                  </Button>
                </Tooltip>
              ) : (
                <Button
                  size="lg"
                  h={48}
                  className="large-btn"
                  onClick={submitForm}
                  loading={
                    updateCustomModelMutation.isPending ||
                    addCustomModelMutation.isPending
                  }
                  disabled={isSubmitDisabled}
                >
                  {type === CustomModelModalType.ADD
                    ? "Add Custom Model"
                    : "Update Model"}
                </Button>
              )}
            </Group>
          </div>
        </Stack>
      </form>
    </Modal>
  );
}

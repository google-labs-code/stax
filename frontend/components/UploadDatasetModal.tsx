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

import { Dataset } from "@/app/(authRoutes)/datasets/types";
import Card from "@/components/Card";
import MaterialIcon from "@/components/MaterialIcon";
import {
  SAMPLE_DATASET_LINK,
  UPLOAD_DATASET_INITIAL_VALUES,
  UPLOAD_DATASET_TOOLTIP_MESSAGES,
} from "@/config/constants";
import { getErrorNotificationConfig } from "@/config/notifications";
import { createDatasetQuery } from "@/queries/clientQueries";
import { DatasetPayload } from "@/queries/types";
import {
  DatasetUploadFormData,
  ProjectType,
  UploadDatasetModalSource,
} from "@/types";
import {
  Button,
  Group,
  Modal,
  ScrollArea,
  Stack,
  Text,
  Textarea,
  UnstyledButton,
} from "@mantine/core";
import { Dropzone, FileWithPath } from "@mantine/dropzone";
import { useForm } from "@mantine/form";
import { useDisclosure } from "@mantine/hooks";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import Papa, { ParseResult } from "papaparse";
import { useEffect, useMemo, useRef, useState } from "react";

import ImportDatasetModal from "./ImportDatasetModal";
import ModalFooterButton from "./ModalFooter";
import ModalInput from "./ModalInput";
import ModalInputLabel from "./ModalInputLabel";

type UploadDatasetModalProps = {
  isOpened: boolean;
  onClose: () => void;
  onSuccess: (dataset: Dataset) => void;
  dataset?: Dataset;
  tooltipLabel: string;
  initialFile?: FileWithPath | null;
  initialParsedResults?: ParseResult<any> | null;
  onDatasetCreated?: (dataset: Dataset) => void;
  source: UploadDatasetModalSource;
  projectType: ProjectType;
};

export const UploadDatasetModal = ({
  isOpened,
  onClose,
  onSuccess,
  dataset,
  tooltipLabel,
  initialFile = null,
  initialParsedResults = null,
  onDatasetCreated,
  source,
  projectType,
}: UploadDatasetModalProps) => {
  const MAX_ALLOWED_ROWS = 10000;
  const maxFileSize = 30000000; //30mb
  const maxFileSizeMB = Math.floor(maxFileSize / 1000000); // Convert to MB for display

  const [
    isImportModalOpened,
    { open: openImportDatasetModal, close: closeImportDatasetModal },
  ] = useDisclosure(false);

  const [finalDataset, setFinalDataset] = useState<Dataset | null>(null);
  const [file, setFile] = useState<FileWithPath | null>(null);
  const [parsedFileResults, setParsedFileResults] =
    useState<ParseResult<any> | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [fileTouched, setFileTouched] = useState(false);
  const initialPropsProcessed = useRef(false);

  const getTooltipMessage = () => {
    if (!file) {
      return UPLOAD_DATASET_TOOLTIP_MESSAGES.NO_FILE;
    }
    if (isMaxRowsExceeded) {
      return UPLOAD_DATASET_TOOLTIP_MESSAGES.MAX_ROWS_EXCEEDED(
        MAX_ALLOWED_ROWS,
      );
    }
    if (form.errors.name) {
      return UPLOAD_DATASET_TOOLTIP_MESSAGES.NAME_REQUIRED;
    }
    if (!form.isValid()) {
      return UPLOAD_DATASET_TOOLTIP_MESSAGES.INVALID_FORM;
    }

    return "";
  };

  const form = useForm<DatasetUploadFormData>({
    initialValues: {
      ...UPLOAD_DATASET_INITIAL_VALUES,
      name: finalDataset?.name || "",
      description: finalDataset?.description || "",
    },
    validate: {
      name: (val) => {
        if (!val && !finalDataset && projectType !== ProjectType.SIDE_BY_SIDE) {
          return "Name is required.";
        }
      },
    },
  });

  const createDatasetMutation = useMutation({
    mutationFn: (data: DatasetPayload) => {
      return createDatasetQuery(data);
    },
    onSuccess: (res) => {
      onDatasetCreated && onDatasetCreated(res);
      setFinalDataset(res);
      onClose();
      openImportDatasetModal();
    },
  });

  const addDataset = () => {
    const validationResult = form.validate();
    if (validationResult.hasErrors || !file) {
      setFileTouched(true);

      return;
    }

    if (projectType === ProjectType.SIDE_BY_SIDE) {
      onClose();
      openImportDatasetModal();

      return;
    }

    if (!finalDataset?.id) {
      createDatasetMutation.mutate({
        name: form.values.name,
        description: form.values.description,
      });
    } else {
      onClose();
      openImportDatasetModal();
    }
  };

  useEffect(() => {
    if (isOpened && !initialPropsProcessed.current) {
      if (initialFile) {
        setFile(initialFile);
        form.setFieldValue("file", initialFile);
      }
      if (initialParsedResults) {
        setParsedFileResults(initialParsedResults);
      }
      initialPropsProcessed.current = true;
    } else if (!isOpened) {
      initialPropsProcessed.current = false;
    }
  }, [isOpened, initialFile, initialParsedResults]);

  useEffect(() => {
    if (isOpened === true) {
      if (!initialFile) {
        setFile(null);
        setParsedFileResults(null);
        form.reset();
      }
      setIsLoading(false);
      setFileTouched(false);
    }
  }, [isOpened, initialFile]);

  useEffect(() => {
    if (isOpened === true) {
      setFinalDataset(dataset || null);
    }
  }, [isOpened]);

  const isMaxRowsExceeded = useMemo(() => {
    return (
      typeof parsedFileResults?.data.length === "number" &&
      parsedFileResults?.data.length > MAX_ALLOWED_ROWS
    );
  }, [parsedFileResults]);

  const isNextButtonDisabled = !file || !form.isValid() || isMaxRowsExceeded;

  return (
    <Stack gap={0}>
      {parsedFileResults && (
        <ImportDatasetModal
          isOpened={isImportModalOpened}
          onClose={closeImportDatasetModal}
          file={file}
          parsedFileResults={parsedFileResults}
          formData={form.values}
          onSuccess={onSuccess}
          dataset={finalDataset || undefined}
          tooltipLabel={tooltipLabel}
          source={source}
          projectType={projectType}
        />
      )}

      <Modal
        opened={isOpened}
        onClose={onClose}
        data-testid="upload-dataset-modal"
        title={
          <Group
            justify="space-between"
            align="center"
            className="w-full mb-[32px]"
          >
            <Text className="text-title-22">
              {projectType === ProjectType.SIDE_BY_SIDE
                ? "Upload data"
                : "Upload dataset"}
            </Text>
            <UnstyledButton
              onClick={() => window.open(SAMPLE_DATASET_LINK, "_blank")}
              className="gap-sm mr-3 flex items-center justify-center py-[4px] px-[8px] rounded-md hover:bg-veryLightSilver"
            >
              <MaterialIcon name="help_outline" size={20} />
              <Text className="text-title-12">CSV Guidelines</Text>
            </UnstyledButton>
          </Group>
        }
        centered
        padding="24px"
        size="700px"
        classNames={{
          header: "p-[24px] pb-0",
          title: "w-full",
          body: "flex flex-col flex-1",
          content: "flex flex-col",
        }}
      >
        <Stack>
          <ScrollArea.Autosize
            offsetScrollbars="y"
            scrollbarSize={10}
            classNames={{
              root: "max-h-[50vh]",
            }}
          >
            <Stack className="base-modal-form-two-rows-inputs">
              {!finalDataset && projectType !== ProjectType.SIDE_BY_SIDE && (
                <Stack gap={6}>
                  <ModalInputLabel label="Dataset name" isRequired />
                  <ModalInput
                    placeholder="Name"
                    error={!!form.errors.name}
                    value={form.values.name}
                    onChange={(e) =>
                      form.setFieldValue("name", e.currentTarget.value)
                    }
                  />
                </Stack>
              )}
              {!finalDataset && projectType !== ProjectType.SIDE_BY_SIDE && (
                <Stack gap={6}>
                  <ModalInputLabel label="Dataset description" />
                  <Textarea
                    classNames={{
                      input:
                        "px-[16px] py-[8px] min-h-[85px] rounded-md placeholder:!text-resting",
                    }}
                    placeholder="Optional description for your dataset"
                    value={form.values.description}
                    error={form.errors.description}
                    onChange={(e) =>
                      form.setFieldValue("description", e.currentTarget.value)
                    }
                  />
                </Stack>
              )}
              <Stack gap={6}>
                <ModalInputLabel label="Upload file" isRequired />
                <Card
                  shadow="none"
                  className={`${fileTouched && !file ? "border-1 border-solid border-red-500" : "rounded-md border-[2px] border-dashed border-neutrals-300"}`}
                >
                  <Dropzone
                    className="flex w-full justify-center"
                    classNames={{
                      root: "flex justify-center",
                    }}
                    loading={isLoading}
                    onDrop={(acceptedFiles) => {
                      Papa.parse(acceptedFiles[0] as any, {
                        header: true,
                        skipEmptyLines: true,
                        complete: (results: ParseResult<any>) => {
                          form.setFieldValue("inputColumn", null);
                          form.setFieldValue("outputColumn", null);
                          form.setFieldValue("expectedOutput", null);
                          setFile(acceptedFiles[0]);
                          form.setFieldValue("file", acceptedFiles[0]);
                          setIsLoading(false);
                          setParsedFileResults(results);
                          if (results.data.length > MAX_ALLOWED_ROWS) {
                            notifications.show(
                              getErrorNotificationConfig(
                                `Maximum of ${MAX_ALLOWED_ROWS} rows exceeded.`,
                              ),
                            );
                          }
                        },
                      });
                    }}
                    onReject={(file) => {
                      setIsLoading(false);

                      if (file?.[0]?.file?.size > maxFileSize) {
                        notifications.show(
                          getErrorNotificationConfig(
                            "File size exceeds the maximum allowed size.",
                          ),
                        );
                      } else {
                        notifications.show(
                          getErrorNotificationConfig(
                            "Invalid file type. Upload a .csv file.",
                          ),
                        );
                      }
                    }}
                    onFileDialogOpen={() => {
                      setIsLoading(true);
                      setFileTouched(true);
                    }}
                    onFileDialogCancel={() => {
                      setIsLoading(false);
                    }}
                    accept={{ "text/csv": [".csv"] }}
                    maxSize={maxFileSize}
                  >
                    <Stack
                      gap={16}
                      className="pointer-events-none my-[10px] flex items-center px-[20px]"
                    >
                      <Stack gap={4} className="items-center">
                        <Text className="max-w-[350px] text-center text-resting text-body-16">
                          {file
                            ? "File selected. Click to change."
                            : "Drag and drop your file or click to upload"}
                        </Text>
                        <Text className="text-center text-resting text-body-12">
                          Maximum of {MAX_ALLOWED_ROWS.toLocaleString()} rows
                          and {maxFileSizeMB}MB allowed
                        </Text>
                      </Stack>
                      <Button
                        variant="default"
                        className="max-w-[130px] !cursor-pointer hover:bg-veryLightSilver"
                        size="md"
                        leftSection={
                          <MaterialIcon
                            name="cloud_upload"
                            className="cursor-pointer"
                            size={18}
                          />
                        }
                      >
                        Upload CSV
                      </Button>
                    </Stack>
                  </Dropzone>
                  {file && (
                    <div style={{ marginTop: 20 }}>
                      <Text mb="xs">Uploaded file:</Text>
                      <ul>
                        <li>
                          <Text>{file.name}</Text>
                        </li>
                      </ul>
                    </div>
                  )}
                </Card>
              </Stack>
            </Stack>
          </ScrollArea.Autosize>

          <ModalFooterButton
            onClick={addDataset}
            label="Next"
            isDisabled={isNextButtonDisabled}
            tooltipLabel={
              isNextButtonDisabled ? getTooltipMessage() : undefined
            }
          />
        </Stack>
      </Modal>
    </Stack>
  );
};

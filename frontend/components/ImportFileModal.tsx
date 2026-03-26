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

import Card from "@/components/Card";
import MaterialIcon from "@/components/MaterialIcon";
import { getErrorNotificationConfig } from "@/config/notifications";
import {
  uploadDatasetFileQuery,
  uploadProjectFileQuery,
} from "@/queries/clientQueries";
import { Button, Group, Modal, ScrollArea, Stack, Text } from "@mantine/core";
import { Dropzone, FileWithPath } from "@mantine/dropzone";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { useEffect, useState } from "react";

import ModalFooterButton from "./ModalFooter";
import ModalInputLabel from "./ModalInputLabel";

type ImportFileModalProps = {
  isOpened: boolean;
  onClose: () => void;
  onSuccess: (file: FileWithPath) => void;
  datasetId?: string;
  projectId?: string;
};

export const ImportFileModal = ({
  isOpened,
  onClose,
  onSuccess,
  datasetId,
  projectId,
}: ImportFileModalProps) => {
  const maxFileSize = 30000000; //30mb
  const [file, setFile] = useState<FileWithPath | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [fileTouched, setFileTouched] = useState(false);

  const uploadDatasetFileMutation = useMutation({
    mutationFn: (file: FileWithPath) => {
      return uploadDatasetFileQuery(datasetId || "", file);
    },
    onSuccess: (_, file) => {
      onClose();
      onSuccess(file);
    },
  });

  const uploadProjectFileMutation = useMutation({
    mutationFn: (file: FileWithPath) => {
      return uploadProjectFileQuery(projectId || "", file);
    },
    onSuccess: (_, file) => {
      onClose();
      onSuccess(file);
    },
  });

  const onImport = () => {
    if (!file) {
      return;
    }

    if (datasetId) {
      uploadDatasetFileMutation.mutate(file);
    } else if (projectId) {
      uploadProjectFileMutation.mutate(file);
    }
  };

  useEffect(() => {
    if (isOpened === true) {
      setFile(null);
      setIsLoading(false);
      setFileTouched(false);
    }
  }, [isOpened]);

  return (
    <Stack gap={0}>
      <Modal
        opened={isOpened}
        onClose={onClose}
        title={<Text className="text-title-22">Import file</Text>}
        centered
        padding="24px"
        size="700px"
        classNames={{
          header: "p-[24px] pb-0 mb-[32px]",
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
                      setFile(acceptedFiles[0]);
                      setIsLoading(false);
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
                      <Text className="max-w-[200px] text-center text-resting text-body-16">
                        {file
                          ? "File selected. Click to change."
                          : "Drag and drop your file or click to upload"}
                      </Text>
                      <Button
                        variant="default"
                        className="max-w-[130px] !cursor-pointer"
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
                    <Group className="mt-4 gap-0">
                      <Text mb="xs">Uploaded file:</Text>
                      <ul>
                        <li>
                          <Text>{file.name}</Text>
                        </li>
                      </ul>
                    </Group>
                  )}
                </Card>
              </Stack>
            </Stack>
          </ScrollArea.Autosize>

          <ModalFooterButton
            onClick={onImport}
            label="Import"
            isDisabled={!file}
          />
        </Stack>
      </Modal>
    </Stack>
  );
};

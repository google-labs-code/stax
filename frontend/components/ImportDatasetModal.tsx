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

import { Dataset } from "@/app/(authRoutes)/datasets/types";
import MaterialIcon from "@/components/MaterialIcon";
import {
  ApiMappedColumnOption,
  ApiMappedColumnValue,
  TOOLTIPS,
} from "@/config/constants";
import {
  getErrorNotificationConfig,
  getSuccessNotificationConfig,
} from "@/config/notifications";
import {
  dataTransferAllQuery,
  importDatasetQuery,
  importSxsProjectQuery,
} from "@/queries/clientQueries";
import {
  DatasetUploadFormData,
  ProjectType,
  SourceTypeEnum,
  UploadDatasetModalSource,
} from "@/types";
import {
  createInitialSelectedState,
  getColumnConfiguration,
} from "@/utils/uploadCSV";
import {
  Button,
  Checkbox,
  Divider,
  Group,
  Modal,
  Popover,
  PopoverDropdown,
  PopoverTarget,
  ScrollArea,
  Text,
  Tooltip,
  UnstyledButton,
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { useParams } from "next/navigation";
import { ParseResult } from "papaparse";
import { useCallback, useEffect, useState } from "react";

import TruncatedTextWithPopover from "./TruncatedTextWithPopover";

type ImportDatasetModalProps = {
  isOpened: boolean;
  onClose: () => void;
  file: any;
  parsedFileResults: ParseResult<any> | null;
  formData: DatasetUploadFormData;
  onSuccess: (dataset: Dataset) => void;
  dataset?: Dataset;
  tooltipLabel: string;
  source: UploadDatasetModalSource;
  projectType: ProjectType;
};

type DataColumn = {
  name: string;
  mappedDataColumn: ApiMappedColumnOption | null;
};

export default function ImportDatasetModal({
  isOpened,
  onClose,
  file,
  parsedFileResults,
  formData,
  onSuccess,
  dataset,
  tooltipLabel,
  source,
  projectType,
}: ImportDatasetModalProps) {
  const MAX_VARIABLES_COLUMNS = 10;
  const [isChatFormat, setIsChatFormat] = useState(false);
  const [selectedColumns, setSelectedColumns] = useState<{
    [key: string]: boolean;
  }>(createInitialSelectedState());
  const [columns, setColumns] = useState<DataColumn[]>([]);
  const maxWidthPercentage = columns.length <= 4 ? 100 : 98;
  let cellWidth = "";
  const params = useParams<{ id: string }>();
  const projectId = params?.id;

  if (columns.length <= 6) {
    cellWidth = (maxWidthPercentage / columns.length).toString() + "%";
  } else {
    cellWidth = "160px";
  }

  const initialConfig = getColumnConfiguration(projectType, isChatFormat);
  const [apiColumns, setApiColumns] = useState<ApiMappedColumnOption[]>(
    initialConfig.columns,
  );
  const [requiredColumn, setRequiredColumn] = useState<ApiMappedColumnValue>(
    initialConfig.requiredColumn,
  );
  const [variablesColumnsCount, setVariablesColumnsCount] = useState(0);
  const [columnId, setColumnId] = useState(-1);

  const dataTransferMutation = useMutation({
    mutationFn: dataTransferAllQuery,
    onSuccess: () => {
      notifications.show(
        getSuccessNotificationConfig(
          "Dataset imported successfully to project.",
          "dataset-imported-success",
        ),
      );
      onSuccess(dataset as Dataset);
      onClose();
    },
  });

  useEffect(() => {
    setVariablesColumnsCount(0);
    setSelectedColumns(createInitialSelectedState());

    setColumns(
      parsedFileResults?.meta.fields?.map((field) => ({
        name: field,
        mappedDataColumn: null,
      })) || [],
    );

    const config = getColumnConfiguration(projectType, isChatFormat);
    setApiColumns(config.columns);
    setRequiredColumn(config.requiredColumn);
  }, [isChatFormat, projectType, parsedFileResults]);

  const uploadDatasetCSVMutation = useMutation({
    mutationFn: (params: DatasetUploadFormData) => {
      const variables = [];
      for (const column of columns) {
        if (
          column.mappedDataColumn?.value ===
          ApiMappedColumnValue.variablesColumn
        ) {
          variables.push(column.name);
        }
      }

      if (projectType === ProjectType.SIDE_BY_SIDE) {
        return importSxsProjectQuery(projectId || "", {
          file,
          input_column_name: params.inputColumn || "",
          chat_a_column_name: params.chatAColumn || "",
          chat_b_column_name: params.chatBColumn || "",
          output_a_column_name: params.outputColumnA || "",
          output_b_column_name: params.outputColumnB || "",
          system_instruction_a_column_name:
            params.systemInstructionColumnA || "",
          system_instruction_b_column_name:
            params.systemInstructionColumnB || "",
          model_label_a_column_name: params.modelLabelColumnA || "",
          model_label_b_column_name: params.modelLabelColumnB || "",
          llm_evaluations_a_column_name: params.llmEvaluationsColumnA || "",
          llm_evaluations_b_column_name: params.llmEvaluationsColumnB || "",
          inference_analytics_a_column_name:
            params.inferenceAnalyticsColumnA || "",
          inference_analytics_b_column_name:
            params.inferenceAnalyticsColumnB || "",
          tags_column_name: params.tagsColumn || "",
          variables_column_name: params.variablesColumn || "",
          expected_output_column_name: params.expectedOutput || "",
          human_sxs_rating_column_name: params.humanEvalScoreColumn || "",
          human_sxs_notes_column_name: params.humanEvalScoreNotesColumn || "",
        });
      }

      return importDatasetQuery(dataset?.id || "", {
        file,
        input_column_name: params.inputColumn || "",
        output_column_name: params.outputColumn || "",
        expected_output_name: params.expectedOutput || "",
        tags_column_name: params.tagsColumn || "",
        system_instruction_column_name: params.systemInstructionColumn || "",
        model_label_column_name: params.modelLabelColumn || "",
        human_eval_score_column_name: params.humanEvalScoreColumn || "",
        human_eval_score_notes_column_name:
          params.humanEvalScoreNotesColumn || "",
        chat_column_name: params.chatColumn || "",
        variables_column_names: variables.join(","),
        llm_evaluations_column_name: params.llmEvaluationsColumn || "",
        inference_analytics_column_name: params.inferenceAnalyticsColumn || "",
      });
    },
    onSuccess: (response) => {
      if (response.errorMessages && response.errorMessages.length > 0) {
        const displayMessages = response.errorMessages.slice(0, 3);
        const remainingCount = response.errorMessages.length - 3;

        let errorMessageText = "";

        displayMessages.forEach((msg, index) => {
          errorMessageText += `• ${msg}`;
          if (index < displayMessages.length - 1) {
            errorMessageText += "\n\n";
          }
        });

        if (remainingCount > 0) {
          errorMessageText += `\n\n• ...and ${remainingCount} more errors`;
        }
        notifications.show(
          getErrorNotificationConfig(
            errorMessageText,
            "dataset-import-errors",
            10000,
          ),
        );
      }

      if (projectType === ProjectType.SIDE_BY_SIDE) {
        if (response.successfulRows > 0) {
          onSuccess(dataset as Dataset);
          onClose();
        }
      } else {
        if (response.successfulRows > 0) {
          if (dataset?.id && source === UploadDatasetModalSource.PROJECT) {
            dataTransferMutation.mutate({
              source_id: dataset.id,
              source_type: SourceTypeEnum.DATASET,
              target_id: projectId,
              target_type: SourceTypeEnum.PROJECT,
            });
          } else {
            onSuccess(dataset as Dataset);
            onClose();
          }
        }
      }
    },
  });

  const addDataset = () => {
    const payload: DatasetUploadFormData = {
      ...formData,
      file,
    };

    columns.forEach((column) => {
      if (column.mappedDataColumn) {
        payload[column.mappedDataColumn.value] = column.name;
      }
    });

    uploadDatasetCSVMutation.mutate(payload);
  };

  const isApiOptionDisabled = (apiColumn: ApiMappedColumnOption) => {
    if (
      apiColumn.value === ApiMappedColumnValue.variablesColumn &&
      projectType !== ProjectType.SIDE_BY_SIDE
    ) {
      return variablesColumnsCount >= MAX_VARIABLES_COLUMNS;
    }

    let isDisabled = false;

    columns.forEach((column) => {
      if (column.mappedDataColumn?.value === apiColumn.value) {
        isDisabled = true;
      }
    });

    return isDisabled;
  };

  const isApiOptionSelected = (
    column: DataColumn,
    apiColumn: ApiMappedColumnOption,
  ) => {
    return (
      column?.mappedDataColumn?.value &&
      column?.mappedDataColumn?.value === apiColumn.value
    );
  };

  const maxRowsCount = columns.length < 5 ? 3 : 8;
  let showOverlay = false;
  if (
    parsedFileResults?.data &&
    parsedFileResults?.data?.length > maxRowsCount
  ) {
    showOverlay = true;
  }

  const filteredResults = [...(parsedFileResults?.data || [])].splice(
    0,
    maxRowsCount,
  );

  const isMapColumnDisabled = Object.values(selectedColumns).every(
    (value) => value === true,
  );

  useEffect(() => {
    if (isOpened) {
      setSelectedColumns(createInitialSelectedState());

      setColumns(
        parsedFileResults?.meta.fields?.map((field) => ({
          name: field,
          mappedDataColumn: null,
        })) || [],
      );

      const config = getColumnConfiguration(projectType, isChatFormat);
      setApiColumns(config.columns);
      setRequiredColumn(config.requiredColumn);
    }
  }, [isOpened, projectType, isChatFormat, parsedFileResults]);

  const onSettingColumn = useCallback(
    (
      columnKey: number,
      apiColumn: ApiMappedColumnOption,
      column?: DataColumn,
    ) => {
      setColumns((prev) => {
        const newColumns = [...prev];
        newColumns[columnKey].mappedDataColumn = apiColumn;

        return newColumns;
      });

      setSelectedColumns((prevData) => {
        return {
          ...prevData,
          [column?.mappedDataColumn?.value as string]: true,
        };
      });

      const timer = setTimeout(() => {
        setColumnId(-1);
      }, 300);

      return () => clearTimeout(timer);
    },
    [setColumns, setSelectedColumns],
  );

  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      title={
        <Text className="!font-medium text-title-22">Import a dataset</Text>
      }
      centered
      padding="24px"
      size={columns?.length >= 5 ? "1008px" : "768px"}
      classNames={{
        header: "p-[24px] pb-0 mb-4",
        body: `${columns.length >= 5 ? "pr-[5px]" : "pr-[22px]"} overflow-y-hidden overflow-x-hidden`,
        content: "overflow-y-hidden overflow-x-hidden max-h-[90vh]",
        inner: "overflow-x-hidden",
        root: "overflow-x-hidden",
      }}
    >
      <div className="overflow-x-hidden w-full">
        <Group className="mb-[32px] gap-4">
          <Button
            className="h-[32px] bg-veryLightSilver !px-[12px] border-default"
            classNames={{ label: "text-secondaryDark !text-title-14 gap-1" }}
            disabled
          >
            <MaterialIcon name="draft" className="!font-light" size={20} />{" "}
            {file?.path}
          </Button>
        </Group>

        <Divider my="24px" />

        <Group gap={0} className="overflow-y-hidden w-full">
          <div className="mb-[24px] flex w-full flex-col justify-between gap-1 text-secondaryDark sm:flex-row pr-6">
            <Text className="flex flex-row items-center gap-sm text-title-16">
              Map your dataset
              <MaterialIcon
                name="info"
                className="cursor-pointer !font-light"
                size={20}
                tooltipLabel={tooltipLabel}
                tooltipClassName="min-w-[210px]"
              />
            </Text>

            <Checkbox
              checked={isChatFormat}
              onChange={() => setIsChatFormat(!isChatFormat)}
              label={
                <Tooltip
                  position="bottom"
                  classNames={{
                    tooltip:
                      "max-w-[200px] whitespace-normal break-words text-wrap",
                  }}
                  label={TOOLTIPS.CHAT_FORMAT_TOOLTIP}
                >
                  <Text>Chat format</Text>
                </Tooltip>
              }
            />
          </div>

          <div className="w-full overflow-hidden">
            <ScrollArea
              onScrollPositionChange={() => setColumnId(-1)}
              scrollbarSize={8}
              scrollbars="x"
              offsetScrollbars
              classNames={{ root: "w-[100%] max-w-full" }}
            >
              <Group className="mb-[16px] w-full flex-nowrap gap-0 justify-evenly">
                {columns?.map((column, columnKey) => (
                  <div
                    key={columnKey}
                    style={{
                      width: cellWidth,
                      minWidth: cellWidth,
                      maxWidth: cellWidth,
                    }}
                    className={`px-1`}
                  >
                    <Popover
                      position="bottom-start"
                      closeOnClickOutside
                      opened={columnId === columnKey}
                    >
                      <div
                        onClick={() =>
                          setColumnId(columnId === columnKey ? -1 : columnKey)
                        }
                      >
                        <PopoverTarget>
                          {column.mappedDataColumn ? (
                            <UnstyledButton className="flex h-[34px] w-[100%] flex-row items-center justify-between rounded-sm px-[12px] py-[8px] border-default">
                              <Text className="font-medium text-body-12">
                                {column.mappedDataColumn.name}
                              </Text>
                              <MaterialIcon
                                name="keyboard_arrow_up"
                                className="cursor-pointer !font-light"
                                size={20}
                              />
                            </UnstyledButton>
                          ) : (
                            <Button fullWidth disabled={isMapColumnDisabled}>
                              Map column
                            </Button>
                          )}
                        </PopoverTarget>
                      </div>
                      <PopoverDropdown className="max-h-[200px] min-w-[240px] overflow-auto rounded-lg px-0 py-[8px]">
                        {apiColumns.map((apiColumn, key) => {
                          return (
                            <UnstyledButton
                              key={key}
                              className={`flex w-[100%] flex-row items-center justify-between p-[12px] ${!isApiOptionSelected(column, apiColumn) && "disabled:opacity-30"}`}
                              disabled={isApiOptionDisabled(apiColumn)}
                              onClick={() => {
                                if (
                                  apiColumn.value ===
                                  ApiMappedColumnValue.variablesColumn
                                ) {
                                  setVariablesColumnsCount(
                                    variablesColumnsCount + 1,
                                  );
                                }

                                onSettingColumn(columnKey, apiColumn, column);
                              }}
                            >
                              <Group className="flex" gap={0}>
                                <Text className="text-body-14">
                                  {apiColumn.name}
                                </Text>
                                {apiColumn.isRequired && (
                                  <Text className="mb-[-2px] ml-2 text-red text-body-14">
                                    *
                                  </Text>
                                )}
                              </Group>
                              {isApiOptionSelected(column, apiColumn) && (
                                <MaterialIcon
                                  name="check"
                                  className="cursor-pointer !font-light"
                                  size={20}
                                />
                              )}
                            </UnstyledButton>
                          );
                        })}

                        {column.mappedDataColumn && (
                          <Group className="gap-0 p-[12px] pb-0">
                            <Button
                              fullWidth
                              className="self-center bg-supporting-red-500"
                              onClick={() => {
                                if (
                                  column?.mappedDataColumn?.value ===
                                  ApiMappedColumnValue.variablesColumn
                                ) {
                                  setVariablesColumnsCount(
                                    variablesColumnsCount - 1,
                                  );
                                }

                                setSelectedColumns((prevData) => {
                                  return {
                                    ...prevData,
                                    [column?.mappedDataColumn?.value as string]:
                                      false,
                                  };
                                });

                                setColumns((prev) => {
                                  const newColumns = [...prev];
                                  newColumns[columnKey].mappedDataColumn = null;

                                  return newColumns;
                                });
                                setColumnId(-1);
                              }}
                            >
                              Remove mapping
                            </Button>
                          </Group>
                        )}
                      </PopoverDropdown>
                    </Popover>
                  </div>
                ))}
              </Group>
              <div className="w-full flex-nowrap gap-0 relative">
                <table
                  cellPadding={0}
                  cellSpacing={0}
                  className={`w-[${maxWidthPercentage}%] table-fixed`}
                >
                  <thead>
                    <tr>
                      {columns.map((column, key) => {
                        return (
                          <th
                            key={key}
                            style={{
                              width: cellWidth,
                              minWidth: cellWidth,
                              maxWidth: cellWidth,
                            }}
                            className={`border-collapse`}
                          >
                            <Text
                              className={`!h-[44px] overflow-hidden border-b-0 border-r-0 bg-veryLightSilver p-[12px] text-left !font-medium uppercase text-secondary text-title-11 border-default ${key === 0 && "rounded-tl-[16px]"} ${key === columns.length - 1 && "rounded-tr-[16px] border-r-[1px]"}`}
                            >
                              {column.name}
                            </Text>
                          </th>
                        );
                      })}
                    </tr>
                  </thead>
                  <tbody>
                    {filteredResults?.map((row, rowKey) => {
                      return (
                        <tr key={rowKey} className="p-[12px] text-title-11">
                          {columns.map((column, columnKey) => {
                            return (
                              <td
                                key={columnKey}
                                style={{
                                  width: cellWidth,
                                  minWidth: cellWidth,
                                  maxWidth: cellWidth,
                                }}
                                className={`border-collapse overflow-hidden px-[7px] py-[12px] border-default ${columnKey === columns.length - 1 ? "border-r-[1px]" : "border-r-0"} ${rowKey === filteredResults.length - 1 ? "border-b-[1px]" : "border-b-0"} ${rowKey === filteredResults.length - 1 && columnKey === columns.length - 1 && "rounded-br-[16px]"} ${rowKey === filteredResults.length - 1 && columnKey === 0 && "rounded-bl-[16px]"}`}
                              >
                                <TruncatedTextWithPopover
                                  text={row[column.name]}
                                  additionalClassName="h-[15px] overflow-hidden whitespace-normal !leading-[15px] text-secondaryDark text-body-12"
                                />
                              </td>
                            );
                          })}
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
                {showOverlay && (
                  <div
                    className="pointer-events-none absolute bottom-[8px] left-0 z-10 h-[100%] w-full"
                    style={{
                      background:
                        "linear-gradient(to top, #fff 0%, rgba(255,255,255,0) 60%)",
                    }}
                  />
                )}
              </div>
            </ScrollArea>
          </div>

          {showOverlay && (
            <Group className="mt-[8px] flex w-full flex-row justify-center gap-md">
              <MaterialIcon
                name="info"
                className="cursor-unset !font-light"
                size={20}
              />
              <div className="flex flex-row items-center gap-sm font-medium text-body-14">
                Your dataset contains
                {columns.length > 6 && (
                  <>
                    <Text className="text-brand">
                      {columns?.length} columns
                    </Text>{" "}
                    and
                  </>
                )}
                <Text className="text-brand">
                  {parsedFileResults?.data?.length} rows
                </Text>{" "}
                in total
              </div>
            </Group>
          )}
        </Group>

        <Group
          justify="end"
          className={`mr-4 mt-[32px] w-[${maxWidthPercentage}%]`}
        >
          <Button
            className="h-[48px] small-btn"
            onClick={addDataset}
            loading={uploadDatasetCSVMutation.isPending}
            disabled={
              selectedColumns?.[requiredColumn] === false ||
              uploadDatasetCSVMutation.isPending ||
              dataTransferMutation.isPending
            }
          >
            Add dataset
          </Button>
        </Group>
      </div>
    </Modal>
  );
}

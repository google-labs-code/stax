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

import BreadcrumbSegment from "@/components/BreadcrumbSegment";
import EvaluatorDuplicateButton from "@/components/Evaluation/EvaluatorDuplicateButton";
import MaterialIcon from "@/components/MaterialIcon";
import ModelCombobox from "@/components/ModelComboBox";
import Page from "@/components/Page";
import PageHeader from "@/components/PageHeader";
import { MainConfig } from "@/config/config";
import { DEFAULT_EVALUATOR_MODEL } from "@/config/constants";
import { routes } from "@/config/routes";
import { useModelsContext } from "@/hooks/useModelsContext";
import {
  getAllEvaluatorsQuery,
  getLLMEvaluatorQuery,
  getSxSLLMEvaluatorQuery,
} from "@/queries/clientQueries";
import {
  EvaluatorFormData,
  EvaluatorFormDataItem,
  LLMEvaluatorResponse,
  Model,
} from "@/queries/types";
import { ProjectType, Provider } from "@/types";
import LocalStorage from "@/utils/LocalStorage";
import {
  Box,
  Button,
  Group,
  Loader,
  SegmentedControl,
  Stack,
  Text,
  TextInput,
  Textarea,
} from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { useMutation } from "@tanstack/react-query";
import { useParams } from "next/navigation";
import { ChangeEvent, useEffect, useMemo, useState } from "react";

import ModelConfigurationModal from "../../../../../components/ModelConfigurationModal";
import { EvaluatorPageAction, STORAGE_NEW_EVALUATOR_DATA } from "../../types";
import CreateOrSaveEvaluatorButton from "../components/CreateOrSaveEvaluatorButton";
import EditEvaluatorModal from "../components/EditEvaluatorModal";
import MetricPrompts from "../components/MetricPrompts";
import {
  defaultOutputCategories,
  defaultSxSOutputCategories,
  promptDefaultText,
  promptDefaultTextSxS,
  promptHelperText,
} from "../config/Constants";

type GetEvaluatorDataParams = {
  id: string;
  totalEvaluators: number;
};

// NOTE: Undo/Redo functionality will be added in the future
// type HistoryState = {
//   past: EvaluatorFormData[];
//   current: EvaluatorFormData;
//   future: EvaluatorFormData[];
// };

export default function EditEvaluatorPage() {
  const defaultBaseData = {
    id: "",
    variables: [
      {
        name: "output",
        required: false,
      },
    ],
    model_id: "",
    output_format_type: "Choices",
  };

  const emptyFormData: EvaluatorFormData = {
    selectedType: ProjectType.POINTWISE,
    name: "",
    description: "",
    [ProjectType.POINTWISE]: {
      ...defaultBaseData,
      output_categories: defaultOutputCategories,
      prompt: promptDefaultText,
    } as EvaluatorFormDataItem,
    [ProjectType.SIDE_BY_SIDE]: {
      ...defaultBaseData,
      output_categories: defaultSxSOutputCategories,
      prompt: promptDefaultTextSxS,
    } as EvaluatorFormDataItem,
  };

  const [isSideBySide, setIsSideBySide] = useState(false);
  const [data, setData] = useState<EvaluatorFormData>(emptyFormData);
  const { allModels, setOpenedModel, isLoadingModels } = useModelsContext();
  const [evaluationType, setEvaluationType] = useState<ProjectType>(
    ProjectType.POINTWISE,
  );
  const isUndoRedoEnabled = MainConfig.isEvaluatorUndoRedoEnabled;

  const [history, setHistory] = useState<EvaluatorFormData[]>([]);
  const [historyIndex, setHistoryIndex] = useState<number>(-1);
  const [canUndo, setCanUndo] = useState<boolean>(false);
  const [canRedo, setCanRedo] = useState<boolean>(false);

  const navParams = useParams();
  const evaluatorId = navParams?.id?.toString();
  const pageAction = navParams?.action;

  const isNewPage = !pageAction && !evaluatorId;
  const isViewPage = pageAction === EvaluatorPageAction.VIEW;
  const isEditPage = pageAction === EvaluatorPageAction.EDIT;
  const isDuplicatePage = pageAction === EvaluatorPageAction.DUPLICATE;
  const [closeDropdownTrigger, setCloseDropdownTrigger] =
    useState<boolean>(false);

  const selectedModel = useMemo(() => {
    const foundModel = allModels.find(
      (model) => model.id === data?.[data.selectedType].model_id,
    );

    return foundModel;
  }, [data?.[data.selectedType]?.model_id]);

  const setFormDataFromResponse = (
    response: LLMEvaluatorResponse,
    type: ProjectType,
    totalEvaluators?: number,
  ): void => {
    const formData: EvaluatorFormDataItem = {
      id: response.id,
      output_categories: response?.output_categories,
      variables: response?.variables,
      prompt: response?.prompts[0]?.text,
      model_id: response?.model?.id,
      output_format_type: "Choices",
    };

    const finalData = {
      ...data,
      name:
        isDuplicatePage && totalEvaluators
          ? `${response.name} ${totalEvaluators}`
          : response.name,
      description: response?.description,
      [ProjectType.POINTWISE]:
        type === ProjectType.POINTWISE
          ? formData
          : data?.[ProjectType.POINTWISE],
      [ProjectType.SIDE_BY_SIDE]:
        type === ProjectType.SIDE_BY_SIDE
          ? formData
          : data?.[ProjectType.SIDE_BY_SIDE],
    };

    setData(finalData);

    if (isUndoRedoEnabled) {
      setHistory([finalData]);
      setHistoryIndex(0);
    }
  };

  useEffect(() => {
    if (isUndoRedoEnabled) {
      setCanUndo(historyIndex > 0);
      setCanRedo(historyIndex < history.length - 1);
    }
  }, [historyIndex, history]);

  const handleUndo = (): void => {
    if (historyIndex > 0) {
      const newIndex = historyIndex - 1;
      const prevData = { ...history[newIndex] };
      setData(prevData);
      setHistoryIndex(newIndex);
    }
  };

  const handleRedo = (): void => {
    if (historyIndex < history.length - 1) {
      const newIndex = historyIndex + 1;
      const nextData = { ...history[newIndex] };
      setData(nextData);
      setHistoryIndex(newIndex);
    }
  };

  // Add to history when changes are made
  const addToHistory = (newData: EvaluatorFormData): void => {
    // If we've gone back in history and make a new change,
    // discard the future history
    if (historyIndex < history.length - 1) {
      setHistory((prev) => prev.slice(0, historyIndex + 1));
    }

    setHistory((prev) => [...prev, { ...newData }]);
    setHistoryIndex((prev) => prev + 1);
  };

  const getPointwiseEvaluatorData = useMutation({
    mutationFn: (params: GetEvaluatorDataParams) =>
      getLLMEvaluatorQuery(params.id),
    onSuccess: (response, params) => {
      setFormDataFromResponse(
        response,
        ProjectType.POINTWISE,
        params.totalEvaluators,
      );
    },
  });

  const getSxSEvaluatorData = useMutation({
    mutationFn: (params: GetEvaluatorDataParams) =>
      getSxSLLMEvaluatorQuery(params.id),
    onSuccess: (response, params) => {
      setFormDataFromResponse(
        response,
        ProjectType.SIDE_BY_SIDE,
        params.totalEvaluators,
      );
    },
  });

  const headerDisabled = [EvaluatorPageAction.VIEW].includes(
    pageAction as EvaluatorPageAction,
  );

  const getAllEvaluators = useMutation({
    mutationFn: () => getAllEvaluatorsQuery(),
    onSuccess: (response, id: string) => {
      const openedEvaluator = response.llm.find(
        (evaluator) => evaluator.id === id,
      );

      const totalEvaluators = response.llm.length;
      const openedEvaluatorPair = response.llm.find(
        (evaluator) =>
          evaluator.name === openedEvaluator?.name &&
          evaluator.evaluation_type !== openedEvaluator.evaluation_type,
      );

      if (openedEvaluator) {
        if (openedEvaluator.evaluation_type === ProjectType.POINTWISE) {
          getPointwiseEvaluatorData.mutate({
            id: openedEvaluator.id,
            totalEvaluators,
          });
        } else if (
          openedEvaluator.evaluation_type === ProjectType.SIDE_BY_SIDE
        ) {
          getSxSEvaluatorData.mutate({
            id: openedEvaluator.id,
            totalEvaluators,
          });
        }
      }

      if (openedEvaluatorPair) {
        if (openedEvaluatorPair.evaluation_type === ProjectType.POINTWISE) {
          getPointwiseEvaluatorData.mutate({
            id: openedEvaluatorPair.id,
            totalEvaluators,
          });
        } else if (
          openedEvaluatorPair.evaluation_type === ProjectType.SIDE_BY_SIDE
        ) {
          getSxSEvaluatorData.mutate({
            id: openedEvaluatorPair.id,
            totalEvaluators,
          });
        }
      }
    },
  });

  const [
    isSettingsModalOpened,
    { open: openSettingsModal, close: closeSettingsModal },
  ] = useDisclosure(false);
  const [
    isEditEvaluatorModalOpened,
    { open: openEditEvaluatorModal, close: closeEditEvaluatorModal },
  ] = useDisclosure(false);

  useEffect(() => {
    if (evaluatorId && allModels.length > 0) {
      const selectedType = evaluatorId.includes("pairwise")
        ? ProjectType.SIDE_BY_SIDE
        : ProjectType.POINTWISE;
      setIsSideBySide(selectedType === ProjectType.SIDE_BY_SIDE);
      setEvaluationType(selectedType);
      setData((prevData) => {
        return {
          ...prevData,
          selectedType,
        };
      });

      getAllEvaluators.mutate(evaluatorId);
    }
  }, [allModels]);

  useEffect(() => {
    const newEvaluatorData: string | null | undefined = LocalStorage.get(
      STORAGE_NEW_EVALUATOR_DATA,
    );
    if (isNewPage && newEvaluatorData) {
      const parsedData: { name: string; description: string } =
        JSON.parse(newEvaluatorData);
      setData((prevData) => {
        return {
          ...prevData,
          name: parsedData.name,
          description: parsedData.description,
          [ProjectType.POINTWISE]: {
            ...prevData[ProjectType.POINTWISE],
            prompt: promptDefaultText,
            output_categories: defaultOutputCategories,
          },
          [ProjectType.SIDE_BY_SIDE]: {
            ...prevData[ProjectType.SIDE_BY_SIDE],
            prompt: promptDefaultTextSxS,
            output_categories: defaultSxSOutputCategories,
          },
        };
      });
    }
  }, []);

  const handleEvaluationTypeChange = (value: string) => {
    const selectedType = value as ProjectType;
    setEvaluationType(selectedType);
    setData((prevData) => {
      return {
        ...prevData,
        selectedType,
      };
    });
  };

  const setDefaultModel = (model: Model): void => {
    setData((prevData) => {
      const newData = {
        ...prevData,
        [ProjectType.POINTWISE]: {
          ...prevData[ProjectType.POINTWISE],
          model_id: prevData[ProjectType.POINTWISE]?.model_id
            ? prevData[ProjectType.POINTWISE].model_id
            : model.id,
        },
        [ProjectType.SIDE_BY_SIDE]: {
          ...prevData[ProjectType.SIDE_BY_SIDE],
          model_id: prevData[ProjectType.SIDE_BY_SIDE]?.model_id
            ? prevData[ProjectType.SIDE_BY_SIDE].model_id
            : model.id,
        },
      };

      if (isUndoRedoEnabled) {
        addToHistory(newData);
      }

      return newData;
    });
  };

  useEffect(() => {
    if (allModels.length > 0) {
      const geminiDefaultModel = allModels.find(
        (model) =>
          model.name === DEFAULT_EVALUATOR_MODEL && model.is_api_key_present,
      );
      if (geminiDefaultModel) {
        setDefaultModel(geminiDefaultModel);
      } else {
        const firstModelWithApiKey = allModels.find(
          (model) => model.is_api_key_present,
        );

        if (firstModelWithApiKey) {
          setDefaultModel(firstModelWithApiKey);
        } else {
          setDefaultModel(
            allModels.find((model) => model.provider === Provider.GOOGLE) ||
              allModels[0],
          );
        }
      }
    }
  }, [allModels]);

  const handleNameChange = (e: ChangeEvent<HTMLInputElement>): void => {
    const newName = e.target.value;

    setData((prevData) => {
      const newData = {
        ...prevData,
        name: newName,
      };
      if (isUndoRedoEnabled) {
        addToHistory(newData);
      }

      return newData;
    });
  };

  const handlePromptChange = (val: ChangeEvent<HTMLTextAreaElement>): void => {
    setData((prevData) => {
      const newData = {
        ...prevData,
        [prevData.selectedType]: {
          ...prevData[prevData.selectedType],
          prompt: val.target.value,
        },
      };

      if (isUndoRedoEnabled) {
        addToHistory(newData);
      }

      return newData;
    });
  };

  const handleModelSelect = (model: Model | null): void => {
    setData((prevData) => {
      const newData = {
        ...prevData,
        [prevData.selectedType]: {
          ...prevData[prevData.selectedType],
          model_id: model?.id || "",
        },
      };

      if (isUndoRedoEnabled) {
        addToHistory(newData);
      }

      return newData;
    });
  };

  return (
    <Page fullHeight className="bg-neutrals-50 h-max">
      <Group>
        <PageHeader
          title={data?.name || ""}
          breadcrumbs={
            <BreadcrumbSegment
              label="Evaluator Gallery"
              routes={routes.evaluatorGallery.root}
            />
          }
          iconButton={
            !headerDisabled
              ? {
                  onClick: openEditEvaluatorModal,
                  icon: "edit",
                }
              : undefined
          }
          rightSection={
            isViewPage ? (
              <EvaluatorDuplicateButton
                id={evaluatorId}
                size="md"
                className="bg-white"
              />
            ) : isEditPage || isDuplicatePage || isNewPage ? (
              <Group gap="md" data-testid="right-section-not-view-page">
                {isUndoRedoEnabled && (
                  <Button
                    variant="subtle"
                    size="md"
                    onClick={handleUndo}
                    disabled={!canUndo}
                    className="flex-shrink-0 bg-transparent hover:bg-neutrals-50 disabled:bg-transparent"
                  >
                    <MaterialIcon
                      name="undo"
                      size={24}
                      className={
                        canUndo ? "text-secondaryDark" : "text-neutrals-400"
                      }
                    />
                  </Button>
                )}

                {isUndoRedoEnabled && (
                  <Button
                    variant="subtle"
                    size="md"
                    onClick={handleRedo}
                    disabled={!canRedo}
                    className="flex-shrink-0 bg-transparent hover:bg-neutrals-50 disabled:bg-transparent"
                  >
                    <MaterialIcon
                      name="redo"
                      size={24}
                      className={
                        canRedo ? "text-secondaryDark" : "text-neutrals-400"
                      }
                    />
                  </Button>
                )}

                {/* Only show Duplicate button in edit mode, not in duplicate mode */}
                {isEditPage && (
                  <EvaluatorDuplicateButton
                    id={evaluatorId}
                    size="md"
                    className="bg-white"
                  />
                )}
              </Group>
            ) : null
          }
        />
      </Group>
      {data && (
        <EditEvaluatorModal
          setData={setData}
          data={data}
          isOpened={isEditEvaluatorModalOpened}
          onClose={(response, type) => {
            if (response && type) {
              setFormDataFromResponse(response, type);
            }
            closeEditEvaluatorModal();
          }}
        />
      )}

      <ModelConfigurationModal
        isOpened={isSettingsModalOpened}
        onClose={closeSettingsModal}
      />

      {getPointwiseEvaluatorData.isPending ||
      getSxSEvaluatorData.isPending ||
      getAllEvaluators.isPending ? (
        <Loader size="md" className="flex-row self-center" />
      ) : (
        <Stack gap="lg">
          <Stack gap="xs">
            <Group className="gap-sm flex-row items-center">
              <Text className="text-title-16">Name</Text>
              <MaterialIcon
                name="info"
                size={18}
                className="cursor-pointer text-resting"
                tooltipLabel="Name of the evaluator"
                tooltipClassName="min-w-[180px]"
              />
            </Group>
            <TextInput
              className="w-full"
              value={data?.name}
              onChange={handleNameChange}
              disabled={headerDisabled}
              placeholder="Enter evaluator name"
              classNames={{
                root: "w-full",
                wrapper: `${headerDisabled ? "!bg-veryLightSilver border-default" : "!bg-white"} rounded-md`,
                input: `h-10 pl-3 pr-3 !text-body-14 ${
                  headerDisabled
                    ? "!bg-veryLightSilver !text-secondary"
                    : "!bg-white !text-secondaryDark"
                }`,
              }}
            />
          </Stack>
          <Stack gap="xs" className="mb-4">
            <Group className="gap-sm flex-row items-center">
              <Text className="text-title-16">Prompt</Text>
              <MaterialIcon
                name="info"
                size={18}
                className="cursor-pointer text-resting"
                tooltipLabel="Define the prompt sent to a 'judge' model to evaluate AI output."
                tooltipClassName="min-w-[300px]"
              />
            </Group>

            <Text className="text-body-14 text-secondary">
              {promptHelperText[evaluationType]}
            </Text>

            <div className="flex justify-start">
              <SegmentedControl
                value={evaluationType}
                onChange={handleEvaluationTypeChange}
                data={[
                  { value: ProjectType.POINTWISE, label: "Pointwise" },
                  {
                    value: ProjectType.SIDE_BY_SIDE,
                    label: "Side-by-side",
                  },
                ]}
                disabled={isViewPage && !isSideBySide}
                size="md"
                radius="xl"
                w={248}
                h={44}
                classNames={{
                  root: "bg-neutrals-100 [&_[data-active]]:text-white",
                  indicator: "bg-brand",
                  label: "text-title-14 font-medium",
                  control:
                    isViewPage && !isSideBySide ? "cursor-not-allowed" : "",
                }}
              />
            </div>

            <Stack
              className={`w-full border-default rounded-md p-6 gap-4xl ${
                headerDisabled ? "bg-veryLightSilver" : "bg-white"
              }`}
            >
              {evaluationType === ProjectType.SIDE_BY_SIDE && (
                <div className="flex flex-row items-center justify-between gap-0 bg-lightBlue px-6 py-3 rounded-sm">
                  <Group className="gap-xl">
                    <MaterialIcon
                      name="info"
                      className="text-secondaryDark"
                      size={16}
                    />
                    <Text className="text-secondaryDark">
                      The prompt must output one of the categories
                      &quot;A&quot;, &quot;Tie&quot; or &quot;B&quot; to
                      properly show up in projects.
                    </Text>
                  </Group>
                  {/* <MaterialIcon
                  name="close"
                  className="text-secondaryDark"
                  size={16}
                /> */}
                </div>
              )}

              <Stack gap="xs">
                <Group className="gap-sm flex-row items-center">
                  <Text className="text-title-16">Model</Text>
                  <MaterialIcon
                    name="info"
                    size={18}
                    className="cursor-pointer text-resting"
                    tooltipLabel="Select model to perform evaluation"
                    tooltipClassName="min-w-[210px]"
                  />
                </Group>
                <Group
                  gap={0}
                  className="flex w-[100%] flex-row flex-nowrap justify-between"
                >
                  <Box className="w-full">
                    <ModelCombobox
                      openedModel={selectedModel}
                      isExpanded={false}
                      isDisabled={headerDisabled}
                      isLoadingModels={isLoadingModels}
                      onSelect={handleModelSelect}
                      setDropdownTrigger={setCloseDropdownTrigger}
                      closeDropdownTrigger={closeDropdownTrigger}
                      className={`${headerDisabled ? "!bg-veryLightSilver" : "!bg-white"} border border-borderColor rounded-md`}
                      inputClassName={`!border-0 !text-body-14 ${
                        headerDisabled
                          ? "!bg-veryLightSilver !text-secondary"
                          : "!bg-white !text-secondaryDark"
                      }`}
                      iconSize={18}
                    />
                  </Box>
                  <MaterialIcon
                    name="tune"
                    className="ml-[8px] cursor-pointer"
                    onClick={() => {
                      if (selectedModel && !headerDisabled) {
                        setOpenedModel(selectedModel);
                        openSettingsModal && openSettingsModal();
                      }
                    }}
                    disabled={!selectedModel || headerDisabled}
                  />
                </Group>
              </Stack>

              <Textarea
                autosize
                minRows={6}
                disabled={headerDisabled}
                value={data?.[data.selectedType]?.prompt}
                className="w-full"
                classNames={{
                  root: "w-full",
                  wrapper: `${headerDisabled ? "!bg-veryLightSilver" : "!bg-white"} !rounded-md`,
                  input: `!text-code-14 !p-0 !border-0 !rounded-md placeholder:!text-resting placeholder:!font-normal !font-normal ${
                    headerDisabled
                      ? "!bg-veryLightSilver !text-secondary"
                      : "!bg-white !text-secondaryDark"
                  }`,
                }}
                onChange={handlePromptChange}
              />

              {(isEditPage || isDuplicatePage || isNewPage) && (
                <CreateOrSaveEvaluatorButton data={data} />
              )}
            </Stack>
          </Stack>

          {evaluationType === ProjectType.POINTWISE && (
            <MetricPrompts data={data} setData={setData} />
          )}
        </Stack>
      )}
    </Page>
  );
}

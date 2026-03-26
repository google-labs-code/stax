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

import { getSuccessNotificationConfig } from "@/config/notifications";
import { routes } from "@/config/routes";
import {
  createCustomLLMEvaluatorQuery,
  createSxSLLMEvaluatorQuery,
  updateLLMEvaluatorQuery,
  updateSxSLLMEvaluatorQuery,
} from "@/queries/clientQueries";
import {
  EvaluatorFormData,
  EvaluatorFormDataItem,
  LLMEvaluatorPayload,
} from "@/queries/types";
import { EvaluatorTab, EvaluatorType, GAevents, ProjectType } from "@/types";
import extractEvaluatorsVariables from "@/utils/extractEvaluatorsVariables";
import logGAevent from "@/utils/logGAevent";
import { Button, Group } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useMutation } from "@tanstack/react-query";
import { useParams, useRouter } from "next/navigation";

import { EvaluatorPageAction } from "../../types";

export default function CreateOrSaveEvaluatorButton({
  data,
}: {
  data: EvaluatorFormData;
}) {
  const router = useRouter();
  const navParams = useParams();
  const isNewPage = !navParams?.action && !navParams?.id;
  const activeData = data?.[data.selectedType];

  const createEvaluator = useMutation({
    mutationFn: (params: {
      payload: LLMEvaluatorPayload;
      type: ProjectType;
    }) => {
      if (params.type === ProjectType.SIDE_BY_SIDE) {
        return createSxSLLMEvaluatorQuery(params.payload);
      }

      return createCustomLLMEvaluatorQuery(params.payload);
    },
    onSuccess: () => {
      router.push(
        routes.evaluatorGallery.root + "?tab=" + EvaluatorTab.MY_EVALUATORS,
      );
      notifications.show(
        getSuccessNotificationConfig("Evaluator created.", "evaluator-created"),
      );
    },
  });

  const createEvaluatorAction = (
    payload: EvaluatorFormDataItem,
    variables: any,
    type: ProjectType,
  ) => {
    createEvaluator.mutate({
      payload: {
        name: data?.name,
        description: data?.description,
        model_id: payload?.model_id,
        output_categories: payload?.output_categories,
        output_format_type: payload?.output_format_type,
        prompts: [
          {
            role: EvaluatorType.USER,
            text: payload?.prompt,
          },
        ],
        variables,
      },
      type,
    });
  };

  const updateEvaluatorAction = (
    payload: EvaluatorFormDataItem,
    variables: any,
    type: ProjectType,
  ) => {
    updateEvaluator.mutate({
      payload: {
        id: payload?.id,
        name: data?.name,
        output_format_type: payload?.output_format_type,
        description: data?.description,
        variables,
        output_categories: payload?.output_categories,
        model_id: payload?.model_id,
        prompts: [
          {
            role: EvaluatorType.USER,
            text: payload?.prompt,
          },
        ],
      },
      type,
    });
  };

  const saveEvaluator = () => {
    const extractedVariables = extractEvaluatorsVariables(
      activeData?.prompt || "",
    );

    const existingVarNames = new Set(
      (activeData?.variables || []).map((v) => v.name),
    );

    const combinedVariables = [
      ...(activeData?.variables || []).map((v) => ({
        ...v,
        required: false,
      })),
      ...extractedVariables.filter((v) => !existingVarNames.has(v.name)),
    ];

    if (navParams?.action === EvaluatorPageAction.EDIT) {
      // Create SxS and Pointwise evaluator
      for (const evaluatorType of [
        ProjectType.POINTWISE,
        ProjectType.SIDE_BY_SIDE,
      ]) {
        if (data?.[evaluatorType]?.id) {
          updateEvaluatorAction(
            {
              ...data?.[evaluatorType],
            },
            combinedVariables,
            evaluatorType,
          );
        } else {
          createEvaluatorAction(
            {
              ...data?.[evaluatorType],
            },
            combinedVariables,
            evaluatorType,
          );
        }
      }
    } else if (
      navParams?.action === EvaluatorPageAction.DUPLICATE ||
      isNewPage
    ) {
      if (isNewPage) {
        logGAevent(GAevents.SAVE_CUSTOM_LLM_EVALUATOR);
      }

      // Create SxS and Pointwise evaluator
      for (const evaluatorType of [
        ProjectType.POINTWISE,
        ProjectType.SIDE_BY_SIDE,
      ]) {
        createEvaluatorAction(
          {
            ...data?.[evaluatorType],
          },
          combinedVariables,
          evaluatorType,
        );
      }
    }
  };

  const updateEvaluator = useMutation({
    mutationFn: (params: {
      payload: LLMEvaluatorPayload;
      type: ProjectType;
    }) => {
      if (params.type === ProjectType.SIDE_BY_SIDE) {
        return updateSxSLLMEvaluatorQuery(params.payload);
      }

      return updateLLMEvaluatorQuery(params.payload);
    },

    onSuccess: () => {
      router.push(
        routes.evaluatorGallery.root + "?tab=" + EvaluatorTab.MY_EVALUATORS,
      );
      notifications.show(
        getSuccessNotificationConfig("Evaluator updated.", "evaluator-updated"),
      );
    },
  });

  if (navParams?.action === EvaluatorPageAction.VIEW) {
    return null;
  }

  const outputCategories = activeData?.output_categories ?? [];
  const hasIncorrectScoreMappings = outputCategories.some((entry) => {
    const score = parseFloat(entry.value);

    return score < 0 || score > 1;
  });

  return (
    <Group gap="16px" className="flex flex-row justify-end">
      <Button
        disabled={
          !activeData?.model_id ||
          !activeData?.prompt ||
          activeData?.output_categories?.length === 0 ||
          hasIncorrectScoreMappings ||
          updateEvaluator.isPending ||
          createEvaluator.isPending
        }
        loading={updateEvaluator.isPending || createEvaluator.isPending}
        onClick={saveEvaluator}
        data-testid="create-or-save-button"
      >
        Save
      </Button>
    </Group>
  );
}

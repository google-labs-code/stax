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

import { AllEvaluatorsResponse, EvaluatorCardItem, ProjectType } from "@/types";

export const getGroupedEvaluatorsList = (data: AllEvaluatorsResponse) => {
  const finalData: EvaluatorCardItem[] = [];
  const sideBySideIdPrefix = "pairwise-";

  const pointwiseEvaluators = data.llm.filter(
    (item) => !item?.id.includes(sideBySideIdPrefix),
  );
  const sideBySideEvaluators = data.llm.filter((item) =>
    item.id.includes(sideBySideIdPrefix),
  );

  for (const pointwiseEvaluator of pointwiseEvaluators) {
    finalData.push({
      ...pointwiseEvaluator,
      evaluationTypes: [
        { id: pointwiseEvaluator.id, type: ProjectType.POINTWISE },
      ],
    });
  }

  for (const sideBySideEvaluator of sideBySideEvaluators) {
    const pointwiseEval = finalData.find(
      (item) => item.name === sideBySideEvaluator.name,
    );

    if (pointwiseEval) {
      if (pointwiseEval.output_categories) {
        pointwiseEval.output_categories = [
          ...pointwiseEval.output_categories,
          ...sideBySideEvaluator.output_categories,
        ];
      } else {
        pointwiseEval.output_categories = [
          ...sideBySideEvaluator.output_categories,
        ];
      }

      pointwiseEval.evaluationTypes = [
        { id: pointwiseEval.id, type: ProjectType.POINTWISE },
        { id: sideBySideEvaluator.id, type: ProjectType.SIDE_BY_SIDE },
      ];
      pointwiseEval.id = sideBySideEvaluator.id;
    } else {
      finalData.push({
        ...sideBySideEvaluator,
        evaluationTypes: [
          { id: sideBySideEvaluator.id, type: ProjectType.SIDE_BY_SIDE },
        ],
      });
    }
  }

  return finalData;
};

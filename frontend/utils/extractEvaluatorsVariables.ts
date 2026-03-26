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

import _ from "lodash";

export const extractEvaluatorsVariables = (
  prompt: string,
): { name: string; required: boolean }[] => {
  if (!prompt) return [];

  const regex = /\{\{([^{}]+)\}\}/g;
  const matches = Array.from(prompt.matchAll(regex));

  const variableNames: string[] = _(matches)
    .map((match) => match[1].trim())
    .filter(Boolean)
    .uniq()
    .value();

  return variableNames.map((name) => ({ name, required: false }));
};

export default extractEvaluatorsVariables;

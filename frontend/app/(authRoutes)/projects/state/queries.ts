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

import { backendEndpoints } from "@/config/endpoints";
import { Project } from "@/types";
import { deleteRequest } from "@/utils/apiClient";

export const deleteProjectQuery = async (id: string) =>
  (await deleteRequest(backendEndpoints.PROJECTS.INDEX + "/" + id)) as Project;

export const deleteWorkbookRowsQuery = async (
  id: string,
  chatIds: string[],
) => {
  const chatIdss = {
    chat_ids: chatIds,
  };

  return await deleteRequest(
    `workbook/${backendEndpoints.PROJECTS.INDEX}/${id}/delete-rows`,
    chatIdss,
  );
};

export const deleteSxSRowsQuery = async (id: string, pairIds: string[]) => {
  const pairIdsObj = {
    pair_ids: pairIds,
  };

  return await deleteRequest(
    `sxs/${backendEndpoints.CONTAINERS}/${id}`,
    pairIdsObj,
  );
};

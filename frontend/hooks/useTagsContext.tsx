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

import { getTagsQuery } from "@/queries/clientQueries";
import { TagRaw, TagsAPIResponse } from "@/types";
import { useQuery } from "@tanstack/react-query";
import { PropsWithChildren, createContext, useContext } from "react";

type TagsContextType = {
  userTags: TagRaw[];
  modelTags: TagRaw[];
  datasetTags: TagRaw[];
  allTags: TagsAPIResponse;
  isLoadingTags: boolean;
  refreshTags: () => void;
};

const TagsContext = createContext<TagsContextType | null>(null);

export const TagsProvider = (props: PropsWithChildren) => {
  const { children } = props;
  const {
    data,
    isLoading: isLoadingTags,
    refetch,
    isRefetching,
  } = useQuery({
    queryKey: ["tags-list"],
    queryFn: getTagsQuery,
  });

  const tagsContext = {
    userTags: data?.user_tags || [],
    modelTags: data?.model_tags || [],
    datasetTags: data?.dataset_tags || [],
    allTags: data || {
      user_tags: [],
      model_tags: [],
      dataset_tags: [],
    },
    refreshTags: refetch,
    isLoadingTags: isLoadingTags || isRefetching,
  };

  return (
    <TagsContext.Provider value={tagsContext}>{children}</TagsContext.Provider>
  );
};

export function useTagsContext() {
  return useContext(TagsContext) as TagsContextType;
}

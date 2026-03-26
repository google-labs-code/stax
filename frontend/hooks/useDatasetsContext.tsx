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
import { Datasets } from "@/types";
import { useQuery } from "@tanstack/react-query";
import { PropsWithChildren, createContext, useContext, useMemo } from "react";

import { getDatasetsQuery } from "../queries/clientQueries";

type DatasetsContextType = {
  allDatasets: Dataset[];
  refreshDatasets: () => void;
  isLoadingDatasets: boolean;
  isPendingDatasets: boolean;
};

const DatasetsContext = createContext<DatasetsContextType | null>(null);

export const DatasetsProvider = (props: PropsWithChildren) => {
  const { children } = props;

  const {
    data,
    isLoading: isLoadingDatasets,
    isPending: isPendingDatasets,
    refetch,
    isRefetching,
  } = useQuery<Datasets>({
    queryKey: ["datasets"],
    queryFn: getDatasetsQuery,
  });

  const allDatasets = useMemo(() => {
    if (!data || !data.user_data_sets) return [];

    return [...data.user_data_sets].sort((a, b) => {
      const dateA = new Date(a.updated_at);
      const dateB = new Date(b.updated_at);

      return dateB.getTime() - dateA.getTime();
    });
  }, [data]);

  const datasetsContext = {
    refreshDatasets: refetch,
    allDatasets,
    isLoadingDatasets: isLoadingDatasets || isRefetching,
    isPendingDatasets,
  };

  return (
    <DatasetsContext.Provider value={datasetsContext}>
      {children}
    </DatasetsContext.Provider>
  );
};

export function useDatasetsContext() {
  return useContext(DatasetsContext) as DatasetsContextType;
}

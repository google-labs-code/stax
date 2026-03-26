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

import {
  getModelProvidersQuery,
  getModelsQuery,
} from "@/queries/clientQueries";
import { Model, ModelTypeEnum } from "@/queries/types";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Dispatch,
  PropsWithChildren,
  SetStateAction,
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";

type ModelsContextType = {
  openedModel: Model | null;
  setOpenedModel: Dispatch<SetStateAction<Model | null>>;
  allModels: Model[];
  isLoadingModels: boolean;
  refreshModels: () => void;
  customModels: Model[];
  defaultModels: Model[];
  providers: Record<string, string>;
};

const ModelsContext = createContext<ModelsContextType | null>(null);

export const ModelsProvider = (props: PropsWithChildren) => {
  const { children } = props;
  const [openedModel, setOpenedModel] = useState<Model | null>(null);
  const [providers, setProviders] = useState<Record<string, string>>({});

  const {
    data,
    isLoading: isLoadingModels,
    refetch,
    isRefetching,
  } = useQuery({
    queryKey: ["models"],
    queryFn: getModelsQuery,
  });

  const { mutate: fetchProviders } = useMutation({
    mutationFn: getModelProvidersQuery,
    onSuccess: (response) => {
      setProviders(response);
    },
  });

  useEffect(() => {
    fetchProviders();
  }, []);

  const modelsContext = {
    openedModel,
    setOpenedModel,
    allModels: data || [],
    refreshModels: refetch,
    isLoadingModels: isLoadingModels || isRefetching,
    customModels:
      data?.filter((model) => model.model_type === ModelTypeEnum.USER) || [],
    defaultModels:
      data?.filter((model) => model.model_type === ModelTypeEnum.SYSTEM) || [],
    providers,
  };

  return (
    <ModelsContext.Provider value={modelsContext}>
      {children}
    </ModelsContext.Provider>
  );
};

export function useModelsContext() {
  return useContext(ModelsContext) as ModelsContextType;
}

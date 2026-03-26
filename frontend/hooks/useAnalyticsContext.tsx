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

import { Model } from "@/queries/types";
import React, { createContext, useContext, useState } from "react";

export type FilterType = {
  project: string;
  model: Model | null;
  tags: string[];
};

type TabType = "inference" | "evaluation";

type FilterContextType = {
  selectedFilters: FilterType[];
  setSelectedFilters: React.Dispatch<React.SetStateAction<FilterType[]>>;
  activeTab: TabType;
  setActiveTab: React.Dispatch<React.SetStateAction<TabType>>;
};
const defaultFilters: FilterType[] = [
  {
    project: "",
    model: null,
    tags: [],
  },
];

const FilterContext = createContext<FilterContextType | undefined>(undefined);

export const AnalyticsFilterProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const [selectedFilters, setSelectedFilters] =
    useState<FilterType[]>(defaultFilters);

  const [activeTab, setActiveTab] = useState<TabType>("inference");

  return (
    <FilterContext.Provider
      value={{ selectedFilters, setSelectedFilters, activeTab, setActiveTab }}
    >
      {children}
    </FilterContext.Provider>
  );
};

export const useAnalyticsContext = () => {
  const context = useContext(FilterContext);
  if (!context) {
    throw new Error("useFilters must be used within a FilterProvider");
  }

  return context;
};

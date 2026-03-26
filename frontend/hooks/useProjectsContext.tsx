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

import { useQuery } from "@tanstack/react-query";
import {
  Dispatch,
  PropsWithChildren,
  SetStateAction,
  createContext,
  useContext,
  useMemo,
  useState,
} from "react";

import { getProjectsQuery } from "../queries/clientQueries";
import { Project } from "../types";

type ProjectsContextType = {
  allProjects: Project[];
  refreshProjects: () => void;
  isLoadingProjects: boolean;
  isPendingProjects: boolean;
  defaultProjectId: string;
  setDefaultProjectId: Dispatch<SetStateAction<string>>;
};

const ProjectsContext = createContext<ProjectsContextType | null>(null);

export const ProjectsProvider = (props: PropsWithChildren) => {
  const { children } = props;
  const [defaultProjectId, setDefaultProjectId] = useState("");

  const {
    data,
    isLoading: isLoadingProjects,
    isPending: isPendingProjects,
    refetch,
    isRefetching,
  } = useQuery({
    queryKey: ["projects"],
    queryFn: () =>
      getProjectsQuery({
        include: "modelProviders",
      }),
  });

  const sortedProjects = useMemo(() => {
    if (!data?.["projects"]) {
      return [] as Project[];
    }

    const defaultProject = data?.["projects"]?.find(
      (project) => project.is_default_project,
    );

    const restProjects = data?.["projects"]
      .filter((project) => !project.is_default_project)
      .sort((a, b) => {
        const dateA = new Date(a.updated_at);
        const dateB = new Date(b.updated_at);

        return dateB.getTime() - dateA.getTime();
      });

    if (defaultProject) {
      return [defaultProject, ...restProjects] as Project[];
    }

    return [...restProjects] as Project[];
  }, [data]);

  const projectsContext = {
    refreshProjects: refetch,
    allProjects: sortedProjects,
    isLoadingProjects: isLoadingProjects || isRefetching,
    isPendingProjects,
    defaultProjectId,
    setDefaultProjectId,
  };

  return (
    <ProjectsContext.Provider value={projectsContext}>
      {children}
    </ProjectsContext.Provider>
  );
};

export function useProjectsContext() {
  return useContext(ProjectsContext) as ProjectsContextType;
}

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

import Page from "@/components/Page";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import { ProjectType } from "@/types";
import { ProjectsOnboardingStorage } from "@/utils/projectsOnboardingStorage";
import { Stack } from "@mantine/core";
import { useParams } from "next/navigation";
import React, { useEffect, useState } from "react";

import ProjectHeader from "../components/ProjectHeader";
import { useProjectContext } from "../hooks/useProjectContext";
import ProjectMetrics from "./components/ProjectMetrics";
import ProjectModals from "./components/ProjectModals";
import ProjectWorkbook from "./components/ProjectWorkbook";
import "./styles/main.scss";

export default function Project(): React.ReactElement {
  const params = useParams<{ id: string }>();
  const { isLoadingProjects } = useProjectsContext();
  const { projectState, projectActions, projectQueries, projectModals } =
    useProjectContext();

  const [hideTooltips, setHideTooltips] = useState(true);

  const handleTooltipVisibilityChange = (show: boolean) => {
    setHideTooltips(!show);
  };

  useEffect(() => {
    if (isLoadingProjects || !projectState?.project) return;

    projectQueries.refetchProject();
    projectQueries.startPolling();
  }, [isLoadingProjects, projectState?.project]);

  useEffect(() => {
    return () => {
      projectQueries.stopPolling();
      projectQueries.resetMetrics();
      projectModals.closeGetStartedModal();
    };
  }, []);

  useEffect(() => {
    if (projectState.isLoadingProject) return;

    let projectHasNoData = false;
    if (projectState?.project?.type === ProjectType.POINTWISE) {
      projectHasNoData = projectState.projectData?.workbook_rows?.length === 0;
    } else {
      projectHasNoData = projectState.projectData?.sxs_rows?.length === 0;
    }

    if (
      projectState?.project?.project_id &&
      !ProjectsOnboardingStorage.get(projectState?.project?.project_id) &&
      projectHasNoData
    ) {
      ProjectsOnboardingStorage.set(projectState?.project?.project_id, true);
      projectModals.openGetStartedModal();
      setHideTooltips(true);
    }
  }, [
    projectState.project,
    projectState.projectData,
    projectState.isLoadingProject,
  ]);

  return (
    <Page fullHeight className="bg-white">
      <Stack className="h-full bg-white" gap="32px">
        <ProjectHeader
          isLoadingProjects={projectState.isLoadingProjects}
          project={projectState.project}
        />

        <ProjectMetrics providers={projectState.project?.providers || []} />

        <ProjectWorkbook
          projectData={projectState.projectData}
          totalSize={projectState.totalSize}
          page={projectState.page}
          pageSize={projectState.pageSize}
          setPage={projectActions.setPage}
          setPageSize={projectActions.setPageSize}
          setRefreshHumanPassRate={projectActions.setRefreshHumanPassRate}
          deleteWorkbookRowMutation={projectQueries.deleteWorkbookRowMutation}
          refetchProject={projectQueries.refetchProject}
          projectError={projectQueries.projectError}
          projectId={params.id}
          hideTooltips={hideTooltips}
        />
      </Stack>

      <ProjectModals
        projectId={projectState.project?.project_id || ""}
        importedDataset={projectState.importedDataset}
        setImportedDataset={projectActions.setImportedDataset}
        refetchProject={projectQueries.refetchProject}
        onTooltipVisibilityChange={handleTooltipVisibilityChange}
      />
    </Page>
  );
}

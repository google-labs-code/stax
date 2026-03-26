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

import BreadcrumbSegment from "@/components/BreadcrumbSegment";
import PageHeader from "@/components/PageHeader";
import { routes } from "@/config/routes";
import { Project } from "@/types";
import { Box, Loader } from "@mantine/core";

import { useProjectContext } from "../hooks/useProjectContext";

type ProjectHeaderProps = {
  isLoadingProjects: boolean;
  project?: Project | null;
};

export default function ProjectHeader({
  isLoadingProjects,
  project,
}: ProjectHeaderProps) {
  const { projectModals } = useProjectContext();

  return (
    <Box>
      {!isLoadingProjects ? (
        <PageHeader
          title={project?.name || "Evaluation Projects"}
          iconButton={{
            icon: "edit",
            onClick: () => projectModals.openEditProjectModal(),
          }}
          breadcrumbs={
            <BreadcrumbSegment
              label="Evaluation Projects"
              routes={routes.projects}
            />
          }
        />
      ) : (
        <Loader size="sm" />
      )}
    </Box>
  );
}

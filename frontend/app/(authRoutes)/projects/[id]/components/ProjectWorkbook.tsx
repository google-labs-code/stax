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

import Workbook from "@/app/(authRoutes)/projects/[id]/components/Workbook";
import { Workbook as WorkbookType } from "@/app/(authRoutes)/projects/[id]/types";
import MaterialIcon from "@/components/MaterialIcon";
import { TOOLTIPS } from "@/config/constants";
import { GAevents } from "@/types";
import logGAevent from "@/utils/logGAevent";
import { Box, Group, Text } from "@mantine/core";
import { QueryObserverResult, RefetchOptions } from "@tanstack/query-core";
import { UseMutationResult } from "@tanstack/react-query";
import { Dispatch, SetStateAction } from "react";

import { useProjectContext } from "../../hooks/useProjectContext";

type ProjectWorkbookProps = {
  projectData?: WorkbookType | null;
  totalSize?: number;
  page: number;
  pageSize: number;
  setPage: Dispatch<SetStateAction<number>>;
  setPageSize: (size: number) => void;
  setRefreshHumanPassRate: React.Dispatch<React.SetStateAction<boolean>>;
  deleteWorkbookRowMutation: UseMutationResult<
    unknown,
    Error,
    string[],
    unknown
  >;
  refetchProject: (
    options?: RefetchOptions,
  ) => Promise<QueryObserverResult<WorkbookType | null, Error>>;
  projectError: Error | null;
  projectId: string;
  hideTooltips: boolean;
};

export default function ProjectWorkbook({
  projectData,
  totalSize,
  page,
  pageSize,
  setPage,
  setPageSize,
  setRefreshHumanPassRate,
  deleteWorkbookRowMutation,
  refetchProject,
  projectError,
  projectId,
  hideTooltips,
}: ProjectWorkbookProps) {
  const { projectModals } = useProjectContext();

  return (
    <Box>
      <Group gap={4}>
        <Text className="text-title-16">Project benchmark</Text>
        <MaterialIcon
          name="info"
          className="text-secondary"
          size={20}
          tooltipClassName="min-w-[260px]"
          tooltipLabel={TOOLTIPS.PROJECT_BENCHMARK}
        />
      </Group>
      <Workbook
        customClassName="!border-0 !p-0"
        data={projectData}
        onRowDelete={deleteWorkbookRowMutation}
        projectError={projectError}
        refetchProject={refetchProject}
        projectId={projectId}
        onAddData={() => {
          logGAevent(GAevents.ADD_DATA);
          projectModals.openAddDatasetModal();
        }}
        setPageSize={setPageSize}
        setPage={setPage}
        setRefreshHumanPassRate={setRefreshHumanPassRate}
        page={page}
        totalSize={totalSize}
        pageSize={pageSize}
        hideTooltips={hideTooltips}
      />
    </Box>
  );
}

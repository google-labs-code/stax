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

import Chip from "@/components/Chip";
import MaterialIcon from "@/components/MaterialIcon";
import { JobDetailStatus, JobStatus, Project } from "@/types";
import {
  Group,
  HoverCard,
  HoverCardDropdown,
  HoverCardTarget,
  Loader,
  Text,
} from "@mantine/core";

import JobDetailsContent from "../../components/JobDetailsContent";

type StatusIndicatorProps = {
  firstNonCompletedJob?: JobStatus;
  project: Project;
};

export default function ProjectStatusIndicator({
  firstNonCompletedJob,
  project,
}: StatusIndicatorProps) {
  const status = firstNonCompletedJob?.status;
  const jobs = firstNonCompletedJob ? [firstNonCompletedJob] : [];
  const total = project.total_job_tasks;
  const finished = project.finished_job_tasks;

  if (status === undefined || status === null) {
    return null;
  }

  let label = "";
  let icon = null;
  if (finished !== undefined && total !== undefined) {
    label += `${finished}/${total}`;
  }

  if (status === JobDetailStatus.COMPLETED) {
    icon = (
      <MaterialIcon
        name="check_circle"
        size={16}
        className="!font-light text-green"
      />
    );
  } else if (status === JobDetailStatus.IN_PROGRESS) {
    icon = <Loader size="13px" className="text-secondary" />;
  } else if (status === JobDetailStatus.FAILED) {
    icon = (
      <MaterialIcon name="error" size={16} className="!font-light text-red" />
    );
  } else if (status === JobDetailStatus.PENDING) {
    icon = (
      <MaterialIcon
        name="schedule"
        size={16}
        className="!font-light text-secondary"
      />
    );
  }

  if (label === "") {
    return null;
  }

  return (
    <HoverCard width={370} shadow="md" position="bottom" withArrow>
      <HoverCardTarget>
        <div>
          <Chip label={label} icon={icon} />
        </div>
      </HoverCardTarget>
      <HoverCardDropdown className="rounded-xl p-[24px]">
        <Group className="flex flex-col gap-xl">
          <Group className="flex w-[100%] flex-row justify-between">
            <Text className="flex items-center gap-2 !font-medium text-title-16">
              Job Details
            </Text>
          </Group>
          <JobDetailsContent jobs={jobs} showTitle={false} />
        </Group>
      </HoverCardDropdown>
    </HoverCard>
  );
}

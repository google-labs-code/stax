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

import { JobDetailStatus, Project } from "@/types";
import { Modal, Text } from "@mantine/core";

import JobDetailsContent from "./JobDetailsContent";

type JobDetailsModalProps = {
  isOpened: boolean;
  onClose: () => void;
  projectData: Project | null;
};

export default function JobDetailsModal({
  isOpened,
  onClose,
  projectData,
}: JobDetailsModalProps) {
  if (!projectData) return null;

  const pendingJobs = projectData.job_statuses.filter(
    (job) => job.status === JobDetailStatus.PENDING,
  );
  const inProgressJobs = projectData.job_statuses.filter(
    (job) => job.status === JobDetailStatus.IN_PROGRESS,
  );
  const failedJobs = projectData.job_statuses.filter(
    (job) => job.status === JobDetailStatus.FAILED,
  );

  const jobsToDisplay = [...pendingJobs, ...inProgressJobs, ...failedJobs];

  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      closeButtonProps={{
        "aria-label": "close button",
        className: "text-resting",
      }}
      size="470px"
      title={
        <Text className="flex items-center gap-2 !font-medium text-title-16">
          Job Details
        </Text>
      }
      centered
      padding="24px"
      classNames={{
        header: "p-[24px] pb-0 mb-[16px]",
      }}
    >
      <JobDetailsContent jobs={jobsToDisplay} />
    </Modal>
  );
}

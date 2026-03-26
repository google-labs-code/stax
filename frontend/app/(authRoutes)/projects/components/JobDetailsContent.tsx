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
import FormatDate from "@/components/FormatDate";
import { JobStatus } from "@/types";
import {
  getJobTypeDisplayText,
  getStatusDisplayText,
} from "@/utils/projectStatus";
import { Divider, Group, Stack, Text } from "@mantine/core";

type JobDetailsContentProps = {
  jobs: JobStatus[];
  showTitle?: boolean;
};

export default function JobDetailsContent({
  jobs,
  showTitle = true,
}: JobDetailsContentProps) {
  return (
    <Stack>
      {showTitle && jobs.length > 0 && (
        <Text className="text-secondaryDark w-[145px] text-title-14">
          Job status
        </Text>
      )}
      <Stack gap="24px">
        {jobs.length > 0 ? (
          jobs.map((job, index) => {
            return (
              <Stack key={job.job_id} gap="8px">
                {index > 0 && <Divider size="1px" className="w-[100%]" />}
                <Group className="gap-sm flex h-[32px] flex-row">
                  <Text className="w-[145px] text-body-14">Job started</Text>
                  <Text className="text-secondary text-body-14">
                    <FormatDate date={job.start_time} />
                  </Text>
                </Group>
                {job.end_time && (
                  <Group className="gap-sm flex h-[32px] flex-row">
                    <Text className="w-[145px] text-body-14">Job ended</Text>
                    <Text className="text-secondary text-body-14">
                      <FormatDate date={job.end_time} />
                    </Text>
                  </Group>
                )}
                <Group className="gap-sm flex h-[32px] flex-row">
                  <Text className="w-[145px] text-body-14">Job type</Text>
                  <Chip label={getJobTypeDisplayText(job.type)} />
                </Group>
                <Group className="gap-sm flex h-[32px] flex-row">
                  <Text className="w-[145px] text-body-14">Job status</Text>
                  <Chip label={getStatusDisplayText(job)} />
                </Group>
                <Group className="gap-sm flex h-[32px] flex-row">
                  <Text className="w-[145px] text-body-14">Total tasks</Text>
                  <Text className="text-secondary text-body-14">
                    {job.total}
                  </Text>
                </Group>
              </Stack>
            );
          })
        ) : (
          <div className="flex h-[60px] items-center justify-center">
            <Text className="text-secondary text-body-14">
              No pending, in-progress, or failed jobs found.
            </Text>
          </div>
        )}
      </Stack>
    </Stack>
  );
}

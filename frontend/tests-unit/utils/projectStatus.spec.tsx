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

import { JobDetailStatus, JobStatus } from "@/types";
import {
  getJobTypeDisplayText,
  getStatusDisplayText,
} from "@/utils/projectStatus";

describe("projectStatus utils", () => {
  describe("getStatusDisplayText", () => {
    it("formats IN_PROGRESS status correctly", () => {
      const job: JobStatus = {
        status: JobDetailStatus.IN_PROGRESS,
        job_id: "job-123",
        type: "import",
        start_time: "2023-01-01T00:00:00Z",
        input_ids: ["id1", "id2"],
        pending: 10,
        in_progress: 42,
        failed: 5,
        total: 100,
      };

      expect(getStatusDisplayText(job)).toBe("in-progress: 42");
    });

    it("formats FAILED status correctly", () => {
      const job: JobStatus = {
        status: JobDetailStatus.FAILED,
        job_id: "job-456",
        type: "export",
        start_time: "2023-01-02T00:00:00Z",
        end_time: "2023-01-02T01:00:00Z",
        input_ids: ["id3"],
        pending: 0,
        in_progress: 0,
        failed: 7,
        total: 7,
      };

      expect(getStatusDisplayText(job)).toBe("failed: 7");
    });

    it("formats PENDING status correctly", () => {
      const job: JobStatus = {
        status: JobDetailStatus.PENDING,
        job_id: "job-789",
        type: "eval",
        start_time: "2023-01-03T00:00:00Z",
        input_ids: [],
        pending: 15,
        in_progress: 0,
        total: 15,
      };

      expect(getStatusDisplayText(job)).toBe("pending: 15");
    });

    it("returns status directly for other statuses", () => {
      const completedJob: JobStatus = {
        status: JobDetailStatus.COMPLETED,
        job_id: "job-101",
        type: "scoring",
        start_time: "2023-01-04T00:00:00Z",
        end_time: "2023-01-04T02:00:00Z",
        input_ids: ["id4", "id5"],
        pending: 0,
        in_progress: 0,
        failed: 0,
        total: 50,
      };

      expect(getStatusDisplayText(completedJob)).toBe(
        JobDetailStatus.COMPLETED,
      );

      const successfulJob: JobStatus = {
        status: JobDetailStatus.SUCCESSFUL,
        job_id: "job-202",
        type: "import",
        start_time: "2023-01-05T00:00:00Z",
        end_time: "2023-01-05T01:30:00Z",
        input_ids: ["id6"],
        pending: 0,
        in_progress: 0,
        total: 20,
      };

      expect(getStatusDisplayText(successfulJob)).toBe(
        JobDetailStatus.SUCCESSFUL,
      );
    });
  });

  describe("getJobTypeDisplayText", () => {
    it('returns "Evaluation" for "eval" type', () => {
      expect(getJobTypeDisplayText("eval")).toBe("Evaluation");
    });

    it("capitalizes first letter of other job types", () => {
      expect(getJobTypeDisplayText("import")).toBe("Import");
      expect(getJobTypeDisplayText("export")).toBe("Export");
      expect(getJobTypeDisplayText("scoring")).toBe("Scoring");
      expect(getJobTypeDisplayText("processing")).toBe("Processing");
    });

    it("handles empty strings", () => {
      expect(getJobTypeDisplayText("")).toBe("");
    });

    it("handles already capitalized strings", () => {
      expect(getJobTypeDisplayText("Export")).toBe("Export");
    });

    it("handles single character job types", () => {
      expect(getJobTypeDisplayText("x")).toBe("X");
    });
  });
});

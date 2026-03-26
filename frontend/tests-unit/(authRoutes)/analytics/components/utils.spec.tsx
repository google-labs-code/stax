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

import {
  generateRange,
  getDatapoints,
  getEvaluationMonitoringPayload,
  getEvaluators,
  getFilterName,
  getInferenceMonitoringPayload,
  roundUpToEvenHundredth,
} from "@/app/(authRoutes)/analytics/components/utils";
import { FilterType } from "@/hooks/useAnalyticsContext";
import dayjs from "@/utils/dayjsSetup";

describe("monitoring utils", () => {
  describe("getEvaluationMonitoringPayload", () => {
    it("returns correct payload with all fields", () => {
      const selectedDates = [dayjs("2023-01-01"), dayjs("2023-01-02")];
      const filters = {
        project: "project1",
        model: { id: "model1" },
        tags: ["tag1", "tag2"],
      };
      const scorerNames = ["BLEU", "ROUGE"];

      const payload = getEvaluationMonitoringPayload(
        selectedDates,
        filters as FilterType,
        scorerNames,
      );

      expect(payload).toEqual({
        startTime: "2023-01-01T00:00:00+02:00",
        endTime: "2023-01-02T00:00:00+02:00",
        modelIds: ["model1"],
        tagIds: ["tag1", "tag2"],
        projectId: "project1",
        scorerNames,
      });
    });

    it("handles missing model and tags", () => {
      const selectedDates = [dayjs("2023-01-01"), dayjs("2023-01-02")];
      const filters = {
        project: "project1",
        model: null,
        tags: [],
      };

      const payload = getEvaluationMonitoringPayload(selectedDates, filters);

      expect(payload.modelIds).toBeUndefined();
      expect(payload.tagIds).toEqual([]);
      expect(payload.projectId).toBe("project1");
    });
  });

  describe("getInferenceMonitoringPayload", () => {
    it("builds correct payload", () => {
      const selectedDates = [dayjs("2023-01-01"), dayjs("2023-01-02")];
      const filters = {
        project: "project1",
        model: { id: "model1", provider: "OPENAI" },
        tags: ["tag1"],
      };

      const payload = getInferenceMonitoringPayload(
        selectedDates,
        filters as FilterType,
      );

      expect(payload).toEqual({
        start_time: "2023-01-01T00:00:00+02:00",
        end_time: "2023-01-02T00:00:00+02:00",
        aggregateWindow: "daily",
        projectId: "project1",
        modelId: "model1",
        modelProvider: "OPENAI",
        tagIds: "tag1",
      });
    });

    it("handles missing model and tags", () => {
      const selectedDates = [dayjs("2023-01-01"), dayjs("2023-01-02")];
      const filters = {
        project: "",
        model: null,
        tags: [],
      };

      const payload = getInferenceMonitoringPayload(selectedDates, filters);

      expect(payload).toEqual({
        aggregateWindow: "daily",
        end_time: "2023-01-02T00:00:00+02:00",
        start_time: "2023-01-01T00:00:00+02:00",
        tagIds: "",
      });
    });
  });

  describe("getFilterName", () => {
    it("returns FILTER_A for first index if multiple filters", () => {
      expect(getFilterName(0, 2)).toBe("Filter A");
    });

    it("returns ALL_MODELS if only one filter", () => {
      expect(getFilterName(0, 1)).toBe("All models");
    });

    it("returns FILTER_B for second filter", () => {
      expect(getFilterName(1, 2)).toBe("Filter B");
    });
  });

  describe("getEvaluators", () => {
    it("merges two lists and removes duplicates", () => {
      const result = getEvaluators(["BLEU", "ROUGE"], ["ROUGE", "METEOR"]);
      expect(result).toEqual(["BLEU", "ROUGE", "METEOR"]);
    });
  });

  describe("getDatapoints", () => {
    it("adds filter label to datapoints", () => {
      const datapoints = [{ id: 1 }, { id: 2 }] as any;
      const result = getDatapoints("Filter A", datapoints);
      expect(result).toEqual([
        { id: 1, filter: "Filter A" },
        { id: 2, filter: "Filter A" },
      ]);
    });

    it("returns empty array if datapoints is falsy", () => {
      expect(getDatapoints("Filter A", null as any)).toEqual([]);
    });
  });

  describe("generateRange", () => {
    it("generates range with step", () => {
      const result = generateRange(0, 1, 0.25);
      expect(result).toEqual([0, 0.25, 0.5, 0.75, 1]);
    });

    it("includes end value with floating precision fix", () => {
      const result = generateRange(0, 0.3, 0.1);
      expect(result).toEqual([0, 0.1, 0.2, 0.3]);
    });
  });

  describe("roundUpToEvenHundredth", () => {
    it("rounds up to nearest even hundredth", () => {
      expect(roundUpToEvenHundredth("0.511")).toBe(0.52);
      expect(roundUpToEvenHundredth("0.512")).toBe(0.52);
      expect(roundUpToEvenHundredth("0.513")).toBe(0.52);
      expect(roundUpToEvenHundredth("0.515")).toBe(0.52);
      expect(roundUpToEvenHundredth("0.516")).toBe(0.52);
      expect(roundUpToEvenHundredth("0.517")).toBe(0.52);
    });

    it("returns 0 for invalid input", () => {
      expect(roundUpToEvenHundredth("not-a-number")).toBe(0);
    });

    it("returns input if already even hundredth", () => {
      expect(roundUpToEvenHundredth("0.50")).toBe(0.5);
      expect(roundUpToEvenHundredth("0.52")).toBe(0.52);
    });
  });
});

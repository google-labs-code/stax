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

import EvaluatorsGallery from "@/app/(authRoutes)/evaluatorGallery/page";
import { getAllEvaluatorsQuery } from "@/queries/clientQueries";
import { testRender } from "@/tests-unit/render";
import {
  AllEvaluatorsResponse,
  EvaluatorType,
  LLMEvaluatorItem,
} from "@/types";
import { fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("next/navigation", () => ({
  useSearchParams: () => ({
    get: () => null,
  }),
  useRouter: () => ({
    push: jest.fn(),
  }),
}));

jest.mock("@/queries/clientQueries");

const mockSystemEvaluators: AllEvaluatorsResponse = {
  llm: [
    {
      id: "system-1",
      name: "System Evaluator A",
      type: EvaluatorType.SYSTEM,
      description: "System desc",
    },
    {
      id: "user-1",
      name: "User Evaluator Z",
      type: EvaluatorType.USER,
      description: "User desc",
    },
  ] as LLMEvaluatorItem[],
  heuristic: [],
};

describe("EvaluatorsGallery - Realistic Flow", () => {
  beforeEach(() => {
    (getAllEvaluatorsQuery as jest.Mock).mockResolvedValue(
      mockSystemEvaluators,
    );
  });

  it("shows loader on initial render, then displays evaluators", async () => {
    testRender(<EvaluatorsGallery />);
    await waitFor(() => {
      expect(screen.getByText("System Evaluator A")).toBeInTheDocument();
      expect(screen.queryByText("User Evaluator Z")).toBeInTheDocument();
    });
  });

  it("switches tabs and shows custom evaluators in 'My Evaluators'", async () => {
    testRender(<EvaluatorsGallery />);

    await waitFor(() => {
      expect(screen.getByText("System Evaluator A")).toBeInTheDocument();
    });

    const myEvaluatorsTab = screen.getByText("My Evaluators");
    fireEvent.click(myEvaluatorsTab);

    await waitFor(() => {
      expect(screen.getByText("User Evaluator Z")).toBeInTheDocument();
      expect(screen.queryByText("System Evaluator A")).toBeInTheDocument();
    });
  });

  it("filters evaluators using search box input", async () => {
    testRender(<EvaluatorsGallery />);

    await waitFor(() => {
      expect(screen.getByText("System Evaluator A")).toBeInTheDocument();
    });

    const searchInput = screen.getByRole("textbox");
    fireEvent.change(searchInput, { target: { value: "Z" } });

    await waitFor(() => {
      expect(screen.queryByText("System Evaluator A")).not.toBeInTheDocument();
    });

    fireEvent.change(searchInput, { target: { value: "" } });

    await waitFor(() => {
      expect(screen.getByText("System Evaluator A")).toBeInTheDocument();
    });
  });

  it("does not crash with empty API responses", async () => {
    (getAllEvaluatorsQuery as jest.Mock).mockResolvedValue([]);

    testRender(<EvaluatorsGallery />);

    await waitFor(() => {
      expect(screen.queryByText("System Evaluator A")).not.toBeInTheDocument();
      expect(
        screen.getByText("Create your first custom LLM evaluator"),
      ).toBeInTheDocument();
    });
  });
});

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

import CreateOrSaveEvaluatorButton from "@/app/(authRoutes)/evaluatorGallery/[id]/components/CreateOrSaveEvaluatorButton";
import { testRender } from "@/tests-unit/render";
import { GAevents, ProjectType } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/queries/clientQueries", () => ({
  createCustomLLMEvaluatorQuery: jest.fn(),
  updateLLMEvaluatorQuery: jest.fn(),
}));
jest.mock("@/utils/logGAevent", () => jest.fn());
jest.mock("@/config/notifications", () => ({
  getSuccessNotificationConfig: jest.fn(() => ({})),
}));
jest.mock("@/config/routes", () => ({
  routes: { evaluatorGallery: { root: "/gallery" } },
}));
jest.mock("@mantine/notifications", () => ({
  notifications: { show: jest.fn() },
}));

const pushMock = jest.fn();
const useParamsMock = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: pushMock }),
  useParams: () => useParamsMock(),
}));

const defaultData = {
  name: "Test Eval",
  description: "desc",
  selectedType: ProjectType.POINTWISE,
  [ProjectType.POINTWISE]: {
    id: "1",
    model_id: "model-1",
    output_categories: [
      { name: "cat", color: "#fff", value: "1", color_name: "white" },
    ],
    variables: [],
    prompt: "Prompt text",
    output_format_type: "Json" as const,
  },
  [ProjectType.SIDE_BY_SIDE]: {
    id: "2",
    model_id: "model-2",
    output_categories: [
      { name: "cat", color: "#fff", value: "1", color_name: "white" },
    ],
    variables: [],
    prompt: "Prompt text 2",
    output_format_type: "Json" as const,
  },
};

describe("CreateOrSaveEvaluatorButton", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders and enables button for valid data", () => {
    useParamsMock.mockReturnValue({});
    testRender(<CreateOrSaveEvaluatorButton data={defaultData} />);
    const button = screen.getByRole("button", { name: /save/i });
    expect(button).toBeInTheDocument();
    expect(button).not.toBeDisabled();
  });

  it("disables button if no selectedModel", () => {
    useParamsMock.mockReturnValue({});
    testRender(
      <CreateOrSaveEvaluatorButton
        data={{
          ...defaultData,
          [ProjectType.POINTWISE]: {
            ...defaultData[ProjectType.POINTWISE],
            model_id: "",
          },
        }}
      />,
    );
    expect(screen.getByRole("button")).toBeDisabled();
  });

  it("calls create mutation and logs GA event on new page save", () => {
    useParamsMock.mockReturnValue({});
    const {
      createCustomLLMEvaluatorQuery,
    } = require("@/queries/clientQueries");
    const logGAevent = require("@/utils/logGAevent");
    createCustomLLMEvaluatorQuery.mockImplementation(() => Promise.resolve());
    testRender(<CreateOrSaveEvaluatorButton data={defaultData} />);
    fireEvent.click(screen.getByRole("button"));
    expect(logGAevent).toHaveBeenCalledWith(GAevents.SAVE_CUSTOM_LLM_EVALUATOR);
  });
});

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

import NewEvaluatorPage from "@/app/(authRoutes)/evaluatorGallery/new/page";
import { screen } from "@testing-library/react";

import { testRender } from "../../../render";

// Mock the EvaluatorPage component that NewEvaluatorPage renders
jest.mock("@/app/(authRoutes)/evaluatorGallery/[id]/[action]/page", () => {
  const MockEvaluatorPage = () => <div data-testid="EvaluatorPage" />;
  MockEvaluatorPage.displayName = "MockEvaluatorPage";

  return MockEvaluatorPage;
});

describe("NewEvaluatorPage", () => {
  it("renders EvaluatorPage component", () => {
    testRender(<NewEvaluatorPage />);

    expect(screen.getByTestId("EvaluatorPage")).toBeInTheDocument();
  });

  it("renders without crashing", () => {
    expect(() => {
      testRender(<NewEvaluatorPage />);
    }).not.toThrow();
  });
});

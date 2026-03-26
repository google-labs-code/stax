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

import NewEvaluatorButton from "@/app/(authRoutes)/evaluatorGallery/components/NewEvaluatorButton";
import { testRender } from "@/tests-unit/render";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: jest.fn(),
    replace: jest.fn(),
    prefetch: jest.fn(),
    back: jest.fn(),
    forward: jest.fn(),
    refresh: jest.fn(),
  }),
  usePathname: () => "/",
  useSearchParams: () => new URLSearchParams(),
}));

describe("NewEvaluatorButton", () => {
  it("renders the button and opens the modal on click", () => {
    testRender(<NewEvaluatorButton />);
    // Check button is rendered
    const button = screen.getByRole("button", { name: /add evaluator/i });
    expect(button).toBeInTheDocument();
    // Modal should not be visible initially
    expect(screen.queryByText(/create evaluator/i)).not.toBeInTheDocument();
    // Click the button
    fireEvent.click(button);
    // Modal should now be visible (assuming modal has a heading or label with 'Create Evaluator')
    expect(screen.getByText(/create evaluator/i)).toBeInTheDocument();
  });
});

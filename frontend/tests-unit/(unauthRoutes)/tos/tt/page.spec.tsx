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

import { testRender } from "@/tests-unit/render";
import { screen } from "@testing-library/react";

import TTTOSPage from "../../../../app/(unauthRoutes)/tos/tt/page";

describe("TTTOSPage", () => {
  it("renders the Terms of Service title", () => {
    testRender(<TTTOSPage />);
    expect(screen.getByText(/Google Terms of Service/i)).toBeInTheDocument();
  });

  it("renders all key sections", () => {
    testRender(<TTTOSPage />);
    expect(screen.getByText(/Things to Know/i)).toBeInTheDocument();
    expect(screen.getByText(/Privacy Notice/i)).toBeInTheDocument();
    expect(screen.getByText(/Trusted Tester Terms/i)).toBeInTheDocument();
  });

  it("renders the Google Privacy Policy link", () => {
    testRender(<TTTOSPage />);
    const link = screen.getByRole("link", { name: /Google Privacy Policy/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute("href", "https://policies.google.com/privacy");
  });

  it("renders the Google Terms of Service link", () => {
    testRender(<TTTOSPage />);
    const link = screen.getByRole("link", {
      name: /Google Terms of Service/i,
    });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute("href", "https://policies.google.com/terms");
  });
});

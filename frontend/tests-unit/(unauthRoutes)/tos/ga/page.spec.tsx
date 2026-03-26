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

import { render, screen } from "@testing-library/react";
import React from "react";

import GATOSPage from "../../../../app/(unauthRoutes)/tos/ga/page";

// Mock Mantine Card to avoid style issues
jest.mock("@mantine/core", () => ({
  Card: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

describe("GATOSPage", () => {
  it("renders the GA Terms of Service heading", () => {
    render(<GATOSPage />);
    expect(screen.getByText(/GA Terms of Service/i)).toBeInTheDocument();
  });

  it("renders the GA TOS paragraph", () => {
    render(<GATOSPage />);
    expect(
      screen.getByText(/This is the Terms of Service for GA\./i),
    ).toBeInTheDocument();
  });
});

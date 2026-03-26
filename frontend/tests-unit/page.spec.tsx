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

import RootPage from "@/app/page";
import { routes } from "@/config/routes";
import LocalStorage from "@/utils/LocalStorage";
import "@testing-library/jest-dom";
import { screen, waitFor } from "@testing-library/react";
import { useRouter } from "next/navigation";

import { testRender } from "./render";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));

jest.mock("@/utils/LocalStorage", () => ({
  get: jest.fn(),
}));

describe("RootPage", () => {
  const pushMock = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue({ push: pushMock });
  });

  it("redirects to /projects if JWT token exists", async () => {
    (LocalStorage.get as jest.Mock).mockReturnValue("mock-token");

    testRender(<RootPage />);

    expect(screen.getByTestId("loading-spinner")).toBeInTheDocument();

    await waitFor(() => {
      expect(pushMock).toHaveBeenCalledWith(routes.projects);
    });
  });

  it("redirects to /signin if JWT token is missing", async () => {
    (LocalStorage.get as jest.Mock).mockReturnValue(null);

    testRender(<RootPage />);

    expect(screen.getByTestId("loading-spinner")).toBeInTheDocument();

    await waitFor(() => {
      expect(pushMock).toHaveBeenCalledWith(routes.signin);
    });
  });
});

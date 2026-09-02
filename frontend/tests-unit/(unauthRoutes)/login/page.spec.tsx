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

import SignInPage from "@/app/(unauthRoutes)/login/page";
import { signInWithGoogle } from "@/app/(unauthRoutes)/login/state/queries";
import { MainConfig } from "@/config/config";
import { testRender } from "@/tests-unit/render";
import { notifications } from "@mantine/notifications";
import { screen, waitFor } from "@testing-library/react";
import { useRouter } from "next/navigation";
import React from "react";

jest.mock("@/hooks/useGlobalContext", () => ({
  useGlobalContext: () => ({
    setUserDetails: jest.fn(),
  }),
}));
jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));
jest.mock("@/utils/apiClient", () => ({
  __esModule: true,
  default: "mockedDefaultExport",
  postRequest: jest.fn().mockReturnValue({}),
}));

jest.mock("../../../app/(unauthRoutes)/login/state/queries", () => ({
  acceptTos: jest.fn(),
  signInWithGoogle: jest.fn(),
}));

describe("SignInPage", () => {
  const mockPush = jest.fn();

  beforeEach(() => {
    (useRouter as jest.Mock).mockImplementation(() => ({
      push: mockPush,
    }));
  });

  it("renders loading component initially", () => {
    testRender(<SignInPage />);

    expect(screen.getByTestId("loading-spinner")).toBeInTheDocument();
  });

  it("handles sign-in success and navigates to quickCompare page", async () => {
    const token = "valid-token";
    const location = {
      ...window.location,
      search: "?code=test-code123",
    };
    Object.defineProperty(window, "location", {
      writable: true,
      value: location,
    });

    (signInWithGoogle as jest.Mock).mockReturnValue(
      Promise.resolve({ token, tosId: null, tosContent: null }),
    );

    testRender(<SignInPage />);

    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith("/projects");
      expect(screen.queryByTestId("welcome-modal")).not.toBeInTheDocument();
    });
  });

  if (MainConfig.isAuthEnabled) {
    it("shows an error when there is an error query param", async () => {
      const location = {
        ...window.location,
        search: "?error=test-error",
      };
      Object.defineProperty(window, "location", {
        writable: true,
        value: location,
      });

      const showNotifications = jest.spyOn(notifications, "show");
      testRender(<SignInPage />);

      await waitFor(() => {
        expect(screen.queryByTestId("loading-spinner")).toBeNull();
        expect(screen.queryByTestId("tos-page")).toBeNull();
        expect(showNotifications).toHaveBeenCalled();
        // Check that the notification message contains the error string
        const notificationArg = showNotifications.mock.calls[0][0];
        if (
          React.isValidElement(notificationArg.message) &&
          notificationArg.message.props?.children
        ) {
          expect(notificationArg.message.props.children).toBe("test-error");
        } else if (
          React.isValidElement(notificationArg.message) &&
          notificationArg.message.props?.dangerouslySetInnerHTML
        ) {
          expect(
            notificationArg.message.props.dangerouslySetInnerHTML.__html,
          ).toBe("test-error");
        } else if (typeof notificationArg.message === "string") {
          expect(notificationArg.message).toBe("test-error");
        } else {
          // fallback: check stringification
          expect(String(notificationArg.message)).toContain("test-error");
        }
      });
    });
  }
});

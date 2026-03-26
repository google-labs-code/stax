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

import UserButton from "@/components/SideNav/UserButton";
import { useGlobalContext } from "@/hooks/useGlobalContext";
import LocalStorage from "@/utils/LocalStorage";
import { UserDetails } from "@/utils/userDetailsStorage";
import { MantineProvider } from "@mantine/core";
import { useQueryClient } from "@tanstack/react-query";
import { fireEvent, render, screen } from "@testing-library/react";
import { useRouter } from "next/navigation";

jest.mock("@/hooks/useGlobalContext", () => ({
  useGlobalContext: jest.fn(),
}));

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));

jest.mock("@tanstack/react-query", () => ({
  useQueryClient: jest.fn(),
}));

jest.mock("@/utils/LocalStorage", () => ({
  clear: jest.fn(),
}));

jest.mock("@/utils/userDetailsStorage", () => ({
  UserDetails: {
    remove: jest.fn(),
  },
}));

const mockPush = jest.fn();
const mockClear = jest.fn();

const mockUser = {
  firstName: "Test",
  lastName: "User",
  email: "test@example.com",
};

const renderWithMantine = (ui: React.ReactNode) => {
  return render(<MantineProvider>{ui}</MantineProvider>);
};

describe("UserButton", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue({ push: mockPush });
    (useQueryClient as jest.Mock).mockReturnValue({ clear: mockClear });
  });

  it("does not render if userDetails is null", () => {
    (useGlobalContext as jest.Mock).mockReturnValue({ userDetails: null });

    const { container } = renderWithMantine(
      <UserButton isSidenavOpen={true} />,
    );
    expect(container.querySelector("li")).toBeNull();
  });

  it("renders user icon and email when userDetails is present and sidenav is open", () => {
    (useGlobalContext as jest.Mock).mockReturnValue({ userDetails: mockUser });

    renderWithMantine(<UserButton isSidenavOpen={true} />);
    expect(screen.getByText(mockUser.email)).toBeInTheDocument();
  });

  it("does not show email when sidenav is closed", () => {
    (useGlobalContext as jest.Mock).mockReturnValue({ userDetails: mockUser });

    renderWithMantine(<UserButton isSidenavOpen={false} />);
    expect(screen.queryByText(mockUser.email)).not.toBeInTheDocument();
  });

  it("logs out user when logout button is clicked", async () => {
    (useGlobalContext as jest.Mock).mockReturnValue({ userDetails: mockUser });

    renderWithMantine(<UserButton isSidenavOpen={true} />);

    fireEvent.click(screen.getByRole("listitem"));

    const logoutButton = await screen.findByTestId("sign-out-button");
    fireEvent.click(logoutButton);

    expect(UserDetails.remove).toHaveBeenCalled();
    expect(LocalStorage.clear).toHaveBeenCalled();
    expect(mockClear).toHaveBeenCalled();
    expect(mockPush).toHaveBeenCalledWith("/login");
  });

  it("prevents default action when user button is clicked", () => {
    (useGlobalContext as jest.Mock).mockReturnValue({ userDetails: mockUser });

    renderWithMantine(<UserButton isSidenavOpen={true} />);

    const liElement = screen.getByRole("listitem");

    const preventDefault = jest.fn();
    fireEvent.click(liElement, { preventDefault });

    expect(liElement).toBeInTheDocument();
  });

  it("displays user's full name in dropdown", async () => {
    (useGlobalContext as jest.Mock).mockReturnValue({ userDetails: mockUser });

    renderWithMantine(<UserButton isSidenavOpen={true} />);

    fireEvent.click(screen.getByRole("listitem"));

    const fullName = await screen.findByText(
      `${mockUser.firstName} ${mockUser.lastName}`,
    );
    expect(fullName).toBeInTheDocument();
  });

  it("renders the transition component correctly", () => {
    (useGlobalContext as jest.Mock).mockReturnValue({ userDetails: mockUser });

    const { rerender } = renderWithMantine(<UserButton isSidenavOpen={true} />);
    expect(screen.getByText(mockUser.email)).toBeInTheDocument();

    rerender(
      <MantineProvider>
        <UserButton isSidenavOpen={false} />
      </MantineProvider>,
    );
    expect(screen.queryByText(mockUser.email)).not.toBeInTheDocument();
  });
});

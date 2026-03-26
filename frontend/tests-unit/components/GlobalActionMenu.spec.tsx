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

import GlobalActionMenu from "@/components/GlobalActionMenu";
import { MainConfig } from "@/config/config";
import { PRIVACY_POLICY, TOS_GOOGLE_LINK } from "@/config/constants";
import HatsApi from "@/utils/hatsApi";
import { useDisclosure } from "@mantine/hooks";
import { fireEvent, render, screen } from "@testing-library/react";

jest.mock("@/hooks/useGlobalContext");

jest.mock("@/config/config", () => ({
  MainConfig: {
    isAuthEnabled: true,
  },
}));

jest.mock("@/utils/hatsApi", () => ({
  getInstance: jest.fn().mockReturnValue({
    requestSurvey: jest.fn(),
  }),
}));

jest.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        sendFeedback: "Send Feedback",
        termsOfService: "Terms of Service",
        satisfactionSurvey: "Satisfaction Survey",
        privacyPolicy: "Privacy Policy",
        deleteStaxData: "Delete My Data",
      };
      
      return translations[key] || key;
    },
  }),
}));

jest.mock("@/components/ActionMenu", () => ({
  __esModule: true,
  default: ({
    menuItems,
  }: {
    menuItems: Array<{ label: string; onClick: () => void }>;
  }) => (
    <div data-testid="action-menu">
      {menuItems.map((item, index) => (
        <button
          key={index}
          onClick={item.onClick}
          data-testid={`menu-item-${index}`}
        >
          {item.label}
        </button>
      ))}
    </div>
  ),
}));

jest.mock("@/components/DeleteDataModal", () => ({
  __esModule: true,
  default: ({
    isOpened,
    onClose,
  }: {
    isOpened: boolean;
    onClose: () => void;
  }) =>
    isOpened ? (
      <div data-testid="delete-data-modal" role="dialog">
        <button onClick={onClose}>Cancel</button>
      </div>
    ) : null,
}));

jest.mock("@/components/MaterialIcon", () => ({
  __esModule: true,
  default: ({ name }: { name: string }) => (
    <span data-testid={`icon-${name}`}>{name}</span>
  ),
}));

jest.mock("@mantine/hooks", () => ({
  useDisclosure: jest.fn(),
}));

const mockOpen = jest.fn();
const mockStartFeedback = jest.fn();

global.window = Object.assign(global.window || {}, {
  open: mockOpen,
  userfeedback: {
    api: {
      startFeedback: mockStartFeedback,
    },
  },
});

const originalProcess = global.process;
global.process = {
  ...originalProcess,
  env: {
    ...originalProcess.env,
    NODE_ENV: "test",
    NEXT_PUBLIC_FEEDBACK_PRODUCT_ID: "test-id",
  },
};

describe("GlobalActionMenu", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders when auth is enabled", () => {
    MainConfig.isAuthEnabled = true;
    const mockUseDisclosure = [false, { open: jest.fn(), close: jest.fn() }];
    (useDisclosure as jest.Mock).mockReturnValue(mockUseDisclosure);

    render(<GlobalActionMenu />);
    expect(screen.getByTestId("action-menu")).toBeInTheDocument();
  });

  it("does not render when auth is disabled", () => {
    MainConfig.isAuthEnabled = false;
    const { container } = render(<GlobalActionMenu />);
    expect(container.firstChild).toBeNull();
  });

  it("calls startFeedback when Send Feedback is clicked", () => {
    MainConfig.isAuthEnabled = true;
    const openDeleteDataModal = jest.fn();
    const closeDeleteDataModal = jest.fn();
    const mockUseDisclosure = [
      false,
      { open: openDeleteDataModal, close: closeDeleteDataModal },
    ];
    (useDisclosure as jest.Mock).mockReturnValue(mockUseDisclosure);

    render(<GlobalActionMenu />);

    fireEvent.click(screen.getByTestId("menu-item-0"));

    expect(mockStartFeedback).toHaveBeenCalledWith({
      productId: "test-id",
    });
  });

  it("opens Terms of Service link when clicked", () => {
    MainConfig.isAuthEnabled = true;
    const openDeleteDataModal = jest.fn();
    const closeDeleteDataModal = jest.fn();
    const mockUseDisclosure = [
      false,
      { open: openDeleteDataModal, close: closeDeleteDataModal },
    ];
    (useDisclosure as jest.Mock).mockReturnValue(mockUseDisclosure);

    render(<GlobalActionMenu />);

    fireEvent.click(screen.getByTestId("menu-item-1"));

    expect(mockOpen).toHaveBeenCalledWith(TOS_GOOGLE_LINK, "_blank");
  });

  it("calls HatsApi requestSurvey when Satisfaction Survey is clicked", () => {
    MainConfig.isAuthEnabled = true;
    const openDeleteDataModal = jest.fn();
    const closeDeleteDataModal = jest.fn();
    const mockUseDisclosure = [
      false,
      { open: openDeleteDataModal, close: closeDeleteDataModal },
    ];
    (useDisclosure as jest.Mock).mockReturnValue(mockUseDisclosure);

    render(<GlobalActionMenu />);

    fireEvent.click(screen.getByTestId("menu-item-2"));

    const mockHatsApi = HatsApi.getInstance(window);
    expect(mockHatsApi.requestSurvey).toHaveBeenCalled();
  });

  it("opens Privacy Policy link when clicked", () => {
    MainConfig.isAuthEnabled = true;
    const openDeleteDataModal = jest.fn();
    const closeDeleteDataModal = jest.fn();
    const mockUseDisclosure = [
      false,
      { open: openDeleteDataModal, close: closeDeleteDataModal },
    ];
    (useDisclosure as jest.Mock).mockReturnValue(mockUseDisclosure);

    render(<GlobalActionMenu />);

    fireEvent.click(screen.getByTestId("menu-item-3"));

    expect(mockOpen).toHaveBeenCalledWith(PRIVACY_POLICY, "_blank");
  });

  it("opens Delete Data Modal when Delete My Data is clicked", () => {
    MainConfig.isAuthEnabled = true;
    const openDeleteDataModal = jest.fn();
    const closeDeleteDataModal = jest.fn();
    const mockUseDisclosure = [
      false,
      { open: openDeleteDataModal, close: closeDeleteDataModal },
    ];
    (useDisclosure as jest.Mock).mockReturnValue(mockUseDisclosure);

    render(<GlobalActionMenu />);

    fireEvent.click(screen.getByTestId("menu-item-4"));

    expect(openDeleteDataModal).toHaveBeenCalled();
  });

  it("shows DeleteDataModal when isDeleteDataModalOpened is true", () => {
    MainConfig.isAuthEnabled = true;
    const openDeleteDataModal = jest.fn();
    const closeDeleteDataModal = jest.fn();
    const mockUseDisclosure = [
      true,
      { open: openDeleteDataModal, close: closeDeleteDataModal },
    ];
    (useDisclosure as jest.Mock).mockReturnValue(mockUseDisclosure);

    render(<GlobalActionMenu />);

    expect(screen.getByTestId("delete-data-modal")).toBeInTheDocument();
  });

  it("closes Delete Data Modal when onClose is called", () => {
    MainConfig.isAuthEnabled = true;
    const openDeleteDataModal = jest.fn();
    const closeDeleteDataModal = jest.fn();
    const mockUseDisclosure = [
      true,
      { open: openDeleteDataModal, close: closeDeleteDataModal },
    ];
    (useDisclosure as jest.Mock).mockReturnValue(mockUseDisclosure);

    render(<GlobalActionMenu />);

    expect(screen.getByTestId("delete-data-modal")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /cancel/i }));

    expect(closeDeleteDataModal).toHaveBeenCalled();
  });
});

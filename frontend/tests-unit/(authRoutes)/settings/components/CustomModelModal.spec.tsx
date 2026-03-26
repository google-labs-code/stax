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

import CustomModelModal from "@/app/(authRoutes)/settings/components/CustomModelModal";
import { useModelsContext } from "@/hooks/useModelsContext";
import { testRender } from "@/tests-unit/render";
import { CustomModelModalType } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

jest.mock("@/queries/clientQueries", () => ({
  addCustomModelQuery: jest.fn(),
  updateModelQuery: jest.fn(),
}));

jest.mock("@/utils/getUniqueModelLabel", () => ({
  getUniqueModelLabel: (label: string) => `unique-${label}`,
}));

jest.mock("@/config/notifications", () => ({
  getSuccessNotificationConfig: jest.fn((msg, key) => ({ message: msg, key })),
}));

jest.mock("@mantine/notifications", () => ({
  notifications: { show: jest.fn() },
}));

describe("CustomModelModal", () => {
  const mockRefreshModels = jest.fn();
  const onClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useModelsContext as jest.Mock).mockReturnValue({
      defaultModels: [],
      customModels: [],
      refreshModels: mockRefreshModels,
      providers: ["Gemini"],
    });
  });

  function setup(opts?: {
    isOpened?: boolean;
    model?: any;
    type?: CustomModelModalType;
  }) {
    testRender(
      <CustomModelModal
        isOpened={opts?.isOpened ?? true}
        onClose={onClose}
        model={opts?.model ?? null}
        type={opts?.type ?? CustomModelModalType.ADD}
      />,
    );
  }

  it("renders the modal when isOpened is true", () => {
    setup({ isOpened: true });
    expect(screen.getByText(/Model Nickname/i)).toBeInTheDocument();
  });

  it("does not render when isOpened is false", () => {
    setup({ isOpened: false });
    expect(screen.queryByText(/Model Nickname/i)).toBeNull();
  });

  it("toggles additional details section", () => {
    setup();
    const toggle = screen.getByRole("button", {
      name: /Show additional details/i,
    });
    fireEvent.click(toggle);
    expect(screen.getByText(/Hide additional details/i)).toBeInTheDocument();
  });
});

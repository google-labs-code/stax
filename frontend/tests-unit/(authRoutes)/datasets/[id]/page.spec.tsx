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

import DatasetPage from "@/app/(authRoutes)/datasets/[id]/page";
import * as clientQueries from "@/queries/clientQueries";
import { MantineProvider } from "@mantine/core";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen } from "@testing-library/react";
import React from "react";

jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

jest.mock("next/navigation", () => ({
  useParams: () => ({ id: "dataset-id" }),
  useRouter: () => ({
    push: jest.fn(),
  }),
}));

const renderWithClient = (ui: React.ReactElement) => {
  const queryClient = new QueryClient();

  return render(
    <MantineProvider>
      <QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>
    </MantineProvider>,
  );
};

jest.mock("@/queries/clientQueries", () => ({
  getDatasetQuery: jest.fn(),
  getDatasetRowsQuery: jest.fn(),
  createDatasetRowQuery: jest.fn(),
  updateDatasetRowQuery: jest.fn(),
  deleteDatasetRowsBulkQuery: jest.fn(),
  getChatsExportQuery: jest.fn(),
}));

jest.mock("@/hooks/useTagsContext", () => ({
  useTagsContext: () => ({
    allTags: [],
    refreshTags: jest.fn(),
  }),
}));

jest.mock("@/hooks/useChatContext", () => ({
  useChatContext: () => ({
    chats: [],
  }),
}));

jest.mock("@/hooks/useDatasetsContext", () => ({
  useDatasetsContext: () => ({
    allDatasets: [],
    isLoadingDatasets: false,
  }),
}));

jest.mock("@/hooks/useProjectsContext", () => ({
  useProjectsContext: () => ({
    allProjects: [
      {
        project_id: "1",
        name: "Project 1",
        description: "Project 1 description",
        created_at: "2025-03-13T08:33:41.025+00:00",
        updated_at: "2025-03-13T08:33:41.025+00:00",
      },
    ],
  }),
}));

describe("DatasetPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders the DatasetPage with fetched data", async () => {
    (clientQueries.getDatasetQuery as jest.Mock).mockResolvedValue({
      id: "dataset-id",
      name: "Test Dataset",
    });

    (clientQueries.getDatasetRowsQuery as jest.Mock).mockResolvedValue({
      workbook_rows: [],
      next_page_token: null,
      total_size: 0,
    });

    renderWithClient(<DatasetPage />);
    expect(await screen.findByText("Test Dataset")).toBeInTheDocument();
  });

  it("opens the 'Add data' dropdown", async () => {
    (clientQueries.getDatasetQuery as jest.Mock).mockResolvedValue({
      id: "dataset-id",
      name: "Test Dataset",
    });

    (clientQueries.getDatasetRowsQuery as jest.Mock).mockResolvedValue({
      workbook_rows: [],
      next_page_token: null,
      total_size: 0,
    });

    renderWithClient(<DatasetPage />);

    const addButton = await screen.findByRole("button", {
      name: /add data/i,
    });

    fireEvent.click(addButton);

    expect(
      await screen.findByText((_, el) => el?.textContent === "Add row"),
    ).toBeInTheDocument();
    expect(
      screen.getByText((_, el) => el?.textContent === "Import dataset"),
    ).toBeInTheDocument();
  });
});

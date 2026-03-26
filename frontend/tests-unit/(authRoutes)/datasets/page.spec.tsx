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

import Datasets from "@/app/(authRoutes)/datasets/page";
import * as clientQueries from "@/queries/clientQueries";
import { MantineProvider } from "@mantine/core";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as router from "next/navigation";

jest.mock("@/queries/clientQueries");
jest.mock("next/navigation", () => ({ useRouter: jest.fn() }));

const mockDatasets = [
  {
    id: "1",
    name: "Dataset One",
    description: "First",
    updated_at: new Date().toISOString(),
  },
];

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

jest.mock("@/hooks/useDatasetsContext", () => ({
  useDatasetsContext: () => ({
    allDatasets: [
      {
        id: "1",
        name: "Dataset One",
        description: "First",
        created_at: "2025-03-13T08:33:41.025+00:00",
        updated_at: "2025-03-13T08:33:41.025+00:00",
      },
    ],
    isLoadingDatasets: false,
  }),
}));

jest.mock("@/hooks/useChatContext", () => ({
  useChatContext: () => ({
    chats: [],
  }),
}));

describe("Datasets page", () => {
  const queryClient = new QueryClient();

  beforeEach(() => {
    (clientQueries.getDatasetsQuery as jest.Mock).mockResolvedValue({
      user_data_sets: mockDatasets,
    });

    (router.useRouter as jest.Mock).mockReturnValue({ push: jest.fn() });
  });

  function renderComponent() {
    return render(
      <QueryClientProvider client={queryClient}>
        <MantineProvider>
          <Datasets />
        </MantineProvider>
      </QueryClientProvider>,
    );
  }

  it("displays dataset in table after load", async () => {
    renderComponent();
    await screen.findByText("Dataset One");
    expect(screen.getByText("Dataset One")).toBeInTheDocument();
  });

  it("uploads dataset opens UploadModal", async () => {
    renderComponent();
    await screen.findByText("Dataset One");

    userEvent.click(
      screen.getByRole("button", { name: /create empty dataset/i }),
    );

    expect(screen.getByText(/upload/i)).toBeInTheDocument();
  });
});

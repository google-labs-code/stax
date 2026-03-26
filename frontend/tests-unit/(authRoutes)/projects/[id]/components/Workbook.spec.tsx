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

import Workbook from "@/app/(authRoutes)/projects/[id]/components/Workbook";
import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import { useDatasetsContext } from "@/hooks/useDatasetsContext";
import { useHumanEvalsContext } from "@/hooks/useHumanEvalsContext";
import { useModelsContext } from "@/hooks/useModelsContext";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { useTagsContext } from "@/hooks/useTagsContext";
import { testRender } from "@/tests-unit/render";
import { Provider } from "@/types";
import { useDisclosure } from "@mantine/hooks";
import { useMutation } from "@tanstack/react-query";
import { fireEvent, screen, waitFor } from "@testing-library/react";

jest.mock("next/navigation", () => ({
  useParams: () => ({ id: "test-project-id" }),
  usePathname: () => "/projects/test-project-id",
  useRouter: () => ({
    push: jest.fn(),
    replace: jest.fn(),
    prefetch: jest.fn(),
    back: jest.fn(),
    forward: jest.fn(),
    refresh: jest.fn(),
  }),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock("@/hooks/useChatContext", () => ({
  useChatContext: () => ({
    updateChat: jest.fn(),
    setEditableMessage: jest.fn(),
    fetchChats: jest.fn(),
    instructions: { input: "", isSync: false, activeChatId: "" },
    setSelectedChatTurnIds: jest.fn(),
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

jest.mock("@/hooks/useTagsContext", () => ({
  useTagsContext: jest.fn(),
}));
jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

jest.mock("@/app/(authRoutes)/projects/hooks/useProjectContext", () => ({
  useProjectContext: jest.fn(),
}));

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: jest.fn(),
}));

jest.mock("@/hooks/useHumanEvalsContext", () => ({
  useHumanEvalsContext: jest.fn(),
}));

jest.mock("@/hooks/useDatasetsContext", () => ({
  useDatasetsContext: jest.fn(),
}));

jest.mock("@mantine/hooks", () => ({
  ...jest.requireActual("@mantine/hooks"),
  useDisclosure: jest.fn(),
}));

jest.mock("@tanstack/react-query", () => {
  const actual = jest.requireActual("@tanstack/react-query");

  return {
    ...actual,
    useMutation: jest.fn((options) => ({
      mutate: jest.fn((variables, mutateOptions) => {
        const fakeResult = { llm: [] };
        mutateOptions?.onSuccess?.(fakeResult);
        options?.onSuccess?.(fakeResult);
      }),
      isPending: false,
    })),
  };
});

const testWorkbookItems = [
  {
    id: "chat-1",
    chat_id: "chat-1",
    chat_turn_id: "chat-turn-1",
    input: "test input 1",
    raw_input: "test input 1",
    output: "test output 1",
    expected_output: "test expected output 1",
    system_instructions: "test system instructions 1",
    model_provider: Provider.OPENAI,
    model_id: "model-1",
  },
  {
    id: "chat-2",
    chat_id: "chat-2",
    chat_turn_id: "chat-turn-2",
    input: "test input 2",
    raw_input: "test input 2",
    output: "test output 2",
    expected_output: "test expected output 2",
    system_instructions: "test system instructions 2",
    model_provider: Provider.GROK,
    model_id: "model-1",
  },
];

const testWorkbookItem = testWorkbookItems[0];

const enableAndClickMoreButtonInBulkActionMenu = () => {
  // select the first row to enable the "More" button
  const checkboxes = screen.getAllByRole("checkbox");
  fireEvent.click(checkboxes[1]);

  const moreBtn = screen.getByTestId("bulk-action-menu");
  expect(moreBtn).toBeInTheDocument();
  fireEvent.click(moreBtn);
};

describe("Workbook", () => {
  const mockAddData = jest.fn();
  const mockOnRowDelete = { mutate: jest.fn() };
  const mockSetPageSize = jest.fn();
  const mockSetPage = jest.fn();
  const mockRefetchProject = jest.fn(() => Promise.resolve(null));
  const projectId = "test-project-id";
  const page = 1;
  const pageSize = 10;
  const totalSize = 0;

  const renderWorkbook = (props?: any) => {
    return testRender(
      <Workbook
        onAddData={mockAddData}
        onRowDelete={mockOnRowDelete as any}
        data={
          props?.data || {
            workbook_rows: [],
            total_size: 0,
            next_page_token: 0,
            empty_columns: [],
          }
        }
        projectError={null}
        refetchProject={mockRefetchProject as any}
        projectId={projectId}
        setPageSize={mockSetPageSize}
        setPage={mockSetPage}
        page={page}
        pageSize={pageSize}
        totalSize={totalSize}
        hideTooltips={false}
        setRefreshHumanPassRate={() => {}}
      />,
    );
  };

  const mockOpenModal = jest.fn();
  const mockCloseModal = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    (useDisclosure as jest.Mock).mockImplementation(() => {
      return [
        false,
        {
          open: mockOpenModal,
          close: mockCloseModal,
        },
      ];
    });

    (useModelsContext as jest.Mock).mockImplementation(() => ({
      allModels: [],
    }));

    (useHumanEvalsContext as jest.Mock).mockImplementation(() => ({
      allModels: [],
    }));

    (useDatasetsContext as jest.Mock).mockImplementation(() => ({
      allDatasets: [],
    }));

    (useProjectContext as jest.Mock).mockImplementation(() => ({
      projectActions: {
        resetPagination: jest.fn(),
        setSorting: jest.fn(),
        setDisplayedFilters: jest.fn(),
      },
      projectState: {
        project: {
          project_id: "project-1365d713-6422-47f0-b995-480ada786d4e",
          name: "Quick Compare History",
          type: "POINTWISE",
          description:
            "This project will by default contain all quick compare history.",
          created_at: "2025-07-22T08:19:43.128+00:00",
          updated_at: "2025-07-22T08:19:43.128+00:00",
          is_default_project: true,
          providers: ["GOOGLE"],
        },
        activeStep: 1,
        metricsData: {
          total_inferences: 283,
          average_turn_time_taken: 6421.614840989399,
          total_prompt_tokens: 124073,
          total_completion_tokens: 84074,
          total_tokens: 364625,
        },
        humanEvalPassRate: {
          passRate: 0.6666666666666666,
          likes: 6,
          dislikes: 3,
        },
        importedDataset: null,
        pageSize: 15,
        page: 1,
        isLoadingProjects: false,
        isLoadingProject: false,
        evalAnalyticsScores: [],
        projectData: null,
        humanEvalPassRateIsLoading: true,
        inferenceMetricsIsLoading: false,
        evaluatorMetricsIsLoading: false,
        sorting: [],
        displayedFilters: {},
      },
    }));

    (useTagsContext as jest.Mock).mockImplementation(() => ({
      useTagsContext: jest.fn(() => ({
        userTags: [
          {
            tag_id: "1",
            name: "tag1",
            description: "tag1 description",
            created_at: "2025-03-13T08:33:41.025+00:00",
            updated_at: "2025-03-13T08:33:41.025+00:00",
          },
        ],
        modelTags: [],
        datasetTags: [],
        allTags: {
          user_tags: [
            {
              tag_id: "1",
              name: "tag1",
              description: "tag1 description",
              created_at: "2025-03-13T08:33:41.025+00:00",
              updated_at: "2025-03-13T08:33:41.025+00:00",
            },
          ],
          model_tags: [],
          dataset_tags: [],
        },
        isLoadingTags: false,
        refreshTags: jest.fn(),
      })),
    }));

    (usePlaygroundContext as jest.Mock).mockImplementation(() => ({
      setIsOutputExpanded: jest.fn(),
      isOutputExpanded: false,
      setSelectedChatIds: jest.fn(),
      selectedChatIds: [],
      setInputs: jest.fn(),
      inputs: [],
      setSystemInstructions: jest.fn(),
      systemInstructions: [],
      setPairId: jest.fn(),
      pairId: "",
      setHumanEvalReady: jest.fn(),
      isHumanEvalReady: false,
      setVariables: jest.fn(),
      variables: null,
      setIsManageVariablesModalOpened: jest.fn(),
      isManageVariablesModalOpened: false,
      setIsModelChanged: jest.fn(),
      isModelChanged: false,
      setIsLoading: jest.fn(),
      isLoading: [],
      setIsSaving: jest.fn(),
      isSaving: false,
      setIsSaved: jest.fn(),
      isSaved: false,
      setLastChatTurnId: jest.fn(),
      lastChatTurnId: "",
      setExpandedInputCard: jest.fn(),
      expandedInputCard: undefined,
      setOutputs: jest.fn(),
      outputs: [],
      setIsOriginalEntryDeleted: jest.fn(),
      isOriginalEntryDeleted: false,
      setOnboardingIndex: jest.fn(),
      onboardingIndex: 0,
      setHasShownCleaningBar: jest.fn(),
      hasShownCleaningBar: false,
      setHasRemainingJobs: jest.fn(),
      hasRemainingJobs: false,
      setRefreshHumanPassRate: jest.fn(),
      refreshHumanPassRate: jest.fn(),
      setSelectedChatTurnIds: jest.fn(),
      selectedChatTurnIds: [],
      setModels: jest.fn(),
      models: [],
    }));
  });

  it("should render the content correctly", async () => {
    renderWorkbook();
    const addDataElements = await screen.findAllByText("Add data");
    expect(addDataElements.length).toBeGreaterThan(0);
    expect(addDataElements[0]).toBeInTheDocument();
    expect(await screen.findByText("Columns")).toBeInTheDocument();
  });

  it("renders table with correct headers", () => {
    renderWorkbook();
    // Verify headers are rendered
    expect(screen.getByText("Input")).toBeInTheDocument();
    expect(screen.getByText("Output")).toBeInTheDocument();
    expect(screen.getByText("Human evaluation")).toBeInTheDocument();
  });

  it("renders table with correct row data", async () => {
    const props = {
      data: {
        workbook_rows: [testWorkbookItem],
        total_size: 1,
        next_page_token: 0,
        empty_columns: [],
      },
    };

    renderWorkbook(props);

    expect(screen.getByText(testWorkbookItem.input)).toBeInTheDocument();
    expect(screen.getByText(testWorkbookItem.raw_input)).toBeInTheDocument();
    expect(screen.getByText(testWorkbookItem.output)).toBeInTheDocument();
    expect(
      screen.getByText(testWorkbookItem.system_instructions),
    ).toBeInTheDocument();
    expect(
      screen.getByText(testWorkbookItem.expected_output),
    ).toBeInTheDocument();
  });

  it("calls openAddToProjectDatasetModal when 'Add to project' btn is clicked", async () => {
    const props = {
      data: {
        workbook_rows: [testWorkbookItem],
        total_size: 1,
        next_page_token: 0,
        empty_columns: [],
      },
    };

    renderWorkbook(props);

    enableAndClickMoreButtonInBulkActionMenu();

    const addToProjectBtn = screen.getByText("Add to project");
    expect(addToProjectBtn).toBeInTheDocument();

    fireEvent.click(addToProjectBtn);

    expect(mockOpenModal).toHaveBeenCalledTimes(1);

    // open the popups
    (useDisclosure as jest.Mock).mockImplementation(() => {
      return [
        true,
        {
          open: mockOpenModal,
          close: mockCloseModal,
        },
      ];
    });

    await waitFor(() => {
      const createNewProjectBtn = screen.getAllByText("Create new project")[0];
      expect(createNewProjectBtn).toBeInTheDocument();

      fireEvent.click(createNewProjectBtn);
    });

    expect(mockCloseModal).toHaveBeenCalledTimes(1);
    expect(mockOpenModal).toHaveBeenCalledTimes(2);
  });

  it("calls openAddToProjectDatasetModal when 'Add to dataset' btn is clicked", async () => {
    const props = {
      data: {
        workbook_rows: [testWorkbookItem],
        total_size: 1,
        next_page_token: 0,
        empty_columns: [],
      },
    };

    renderWorkbook(props);

    enableAndClickMoreButtonInBulkActionMenu();

    const addToDatasetBtn = screen.getByText("Add to dataset");
    expect(addToDatasetBtn).toBeInTheDocument();

    fireEvent.click(addToDatasetBtn);

    expect(mockOpenModal).toHaveBeenCalledTimes(1);
  });

  it("selects and diselects one or more rows successfully", async () => {
    const props = {
      data: {
        workbook_rows: testWorkbookItems,
        total_size: testWorkbookItems.length,
        next_page_token: 0,
        empty_columns: [],
      },
    };

    renderWorkbook(props);

    const checkboxes = screen.getAllByRole("checkbox");

    // select all the rows
    fireEvent.click(checkboxes[0]);

    expect((checkboxes[0] as HTMLInputElement).checked).toBe(true);
    expect((checkboxes[1] as HTMLInputElement).checked).toBe(true);
    expect((checkboxes[2] as HTMLInputElement).checked).toBe(true);

    // unselect the first selected row
    fireEvent.click(checkboxes[1]);
    expect((checkboxes[1] as HTMLInputElement).checked).toBe(false);

    // unselect the second selected row
    fireEvent.click(checkboxes[2]);
    expect((checkboxes[2] as HTMLInputElement).checked).toBe(false);
  });

  it.only("adds a new empty row to the table", async () => {
    (useMutation as jest.Mock).mockImplementation((options) => ({
      mutate: jest.fn((variables, mutateOptions) => {
        const fakeResult = {
          id: "chat-3",
          chat_id: "chat-3",
          chat_turn_id: "chat-turn-3",
          input: "",
          raw_input: "",
          output: "",
          expected_output: "",
          system_instructions: "",
          model_provider: null,
          model_id: null,
        };
        mutateOptions?.onSuccess?.(fakeResult);
        options?.onSuccess?.(fakeResult);
      }),
      isPending: false,
    }));

    const { container } = renderWorkbook();

    const getBodyRows = () => container.querySelectorAll("tbody tr");

    const beforeCount = getBodyRows().length;

    expect(beforeCount).toBe(1);

    // click the "Add row" to add a new row
    const rows = screen.getAllByRole("row");
    const lastRow = rows[rows.length - 1];
    fireEvent.click(lastRow);

    const afterCount = getBodyRows().length;

    expect(afterCount).toBe(2);

    // click the "Add row" to add another new row
    const rows2 = screen.getAllByRole("row");
    const lastRow2 = rows2[rows2.length - 1];
    fireEvent.click(lastRow2);
  });
});

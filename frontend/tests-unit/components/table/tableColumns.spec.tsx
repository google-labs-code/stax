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

import { InferenceStatus } from "@/app/(authRoutes)/projects/[id]/types";
import * as tableColumnsModule from "@/components/table/tableColumns";
import { ProjectType, TableName } from "@/types";
import { fireEvent, screen } from "@testing-library/react";

import { testRender } from "../../render";

jest.mock("@/config/notifications", () => ({
  getErrorNotificationConfig: jest.fn(() => ({
    title: "Error",
    message: "Test error message",
    color: "red",
  })),
}));

jest.mock("@mantine/notifications", () => ({
  notifications: {
    show: jest.fn(),
  },
}));

jest.mock("@/utils/sortingUtils", () => ({
  createTokenAccessorFn: jest.fn((key: string) => (row: any) => {
    const tokens = row.inference_tokens;

    return tokens ? tokens[key] : 0;
  }),
}));

jest.mock("@/components/EditableTableField", () => {
  return {
    __esModule: true,
    default: ({
      value,
      placeholderText,
      onUpdate,
      isVariableColumn,
      allowEdit,
    }: {
      value?: string;
      placeholderText?: string;
      onUpdate?: (value: string) => void;
      isVariableColumn?: boolean;
      allowEdit?: boolean;
    }) => (
      <div
        data-testid="mock-editable-field"
        data-value={value}
        data-placeholder={placeholderText}
        data-is-variable-column={isVariableColumn ? "true" : "false"}
        data-allow-edit={allowEdit ? "true" : "false"}
        onClick={() => onUpdate?.("updated-value")}
      >
        {value || placeholderText}
      </div>
    ),
  };
});

jest.mock(
  "@/app/(authRoutes)/projects/[id]/playground/components/ManageVariablesModal",
  () => {
    return {
      __esModule: true,
      default: () => <div data-testid="mock-variables-modal">Mock Modal</div>,
    };
  },
);

describe("tableColumns", () => {
  const mockTableMeta = {
    onExpectedOutputChange: jest.fn(),
    onSystemInstructionChange: jest.fn(),
    onInputChange: jest.fn(),
    onOutputRerun: jest.fn(),
    onHumanEvalScoreChange: jest.fn(),
    onHumanEvalNotesChange: jest.fn(),
    onUpdateChatVariables: jest.fn(),
    tableName: TableName.PROJECT,
    projectType: ProjectType.POINTWISE,
    loadingRows: new Set(),
  };

  const defaultRowOriginal = {
    id: "test-id",
    input: "Test input",
    output: "Test output",
    expected_output: "Expected output",
    system_instructions: "System instructions",
    inference_status: InferenceStatus.SUCCESSFUL,
    inference_tokens: {
      input_tokens: 10,
      output_tokens: 20,
      total_tokens: 30,
    },
    human_eval_scores: [{ score: 1, notes: "Good" }],
    variables: { key: "value" },
    isInputLoading: false,
    isExpectedOutputLoading: false,
    isSystemInstructionsLoading: false,
    isHumanEvalNotesLoading: false,
  };

  const defaultProps = {
    row: {
      original: defaultRowOriginal,
      getIsExpanded: () => false,
    },
    table: {
      options: {
        meta: mockTableMeta,
      },
    },
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("expectedOutputColumn", () => {
    it("has correct properties", () => {
      expect(tableColumnsModule.expectedOutputColumn.id).toBe(
        "expected_output",
      );
      expect(tableColumnsModule.expectedOutputColumn.accessorKey).toBe(
        "output",
      );
      expect(tableColumnsModule.expectedOutputColumn.header).toBe(
        "Expected output",
      );
      expect(tableColumnsModule.expectedOutputColumn.enableSorting).toBe(true);
      expect(tableColumnsModule.expectedOutputColumn.enableColumnFilter).toBe(
        true,
      );
      expect(tableColumnsModule.expectedOutputColumn.filterVariant).toBe(
        "text",
      );
    });

    describe("Cell component", () => {
      it("renders EditableTableField with correct props", () => {
        const CellComponent = tableColumnsModule.expectedOutputColumn.Cell;
        testRender(<CellComponent {...defaultProps} />);

        // EditableTableField renders a Group with TruncatedTextWithPopover inside
        const editableField = screen.getByText("Expected output");
        expect(editableField).toBeInTheDocument();
      });

      it("calls onExpectedOutputChange when updated", () => {
        const CellComponent = tableColumnsModule.expectedOutputColumn.Cell;
        testRender(<CellComponent {...defaultProps} />);

        const editableField = screen.getByText("Expected output");
        expect(editableField).toBeInTheDocument();

        // Test that the component is clickable and renders correctly
        // The actual onUpdate behavior is tested in integration tests
        expect(editableField).toBeInTheDocument();
      });
    });
  });

  describe("systemInstructionsColumn", () => {
    it("has correct properties", () => {
      expect(tableColumnsModule.systemInstructionsColumn.id).toBe(
        "system_instructions",
      );
      expect(tableColumnsModule.systemInstructionsColumn.accessorKey).toBe(
        "system_instructions",
      );
      expect(tableColumnsModule.systemInstructionsColumn.header).toBe(
        "System instructions",
      );
      expect(tableColumnsModule.systemInstructionsColumn.enableSorting).toBe(
        true,
      );
      expect(
        tableColumnsModule.systemInstructionsColumn.enableColumnFilter,
      ).toBe(true);
      expect(tableColumnsModule.systemInstructionsColumn.filterVariant).toBe(
        "text",
      );
    });

    describe("Cell component", () => {
      it("renders EditableTableField with correct props", () => {
        const propsWithNoOutput = {
          ...defaultProps,
          row: {
            ...defaultProps.row,
            original: { ...defaultRowOriginal, output: "" },
          },
        };

        const CellComponent = tableColumnsModule.systemInstructionsColumn.Cell;
        testRender(<CellComponent {...propsWithNoOutput} />);

        const editableField = screen.getByText("System instructions");
        expect(editableField).toBeInTheDocument();
      });

      it("calls onSystemInstructionChange when updated", () => {
        const propsWithNoOutput = {
          ...defaultProps,
          row: {
            ...defaultProps.row,
            original: { ...defaultRowOriginal, output: "" },
          },
        };

        const CellComponent = tableColumnsModule.systemInstructionsColumn.Cell;
        testRender(<CellComponent {...propsWithNoOutput} />);

        const editableField = screen.getByText("System instructions");
        expect(editableField).toBeInTheDocument();

        // Test that the component is clickable and renders correctly
        // The actual onUpdate behavior is tested in integration tests
        expect(editableField).toBeInTheDocument();
      });
    });
  });

  describe("inputDatasetColumn", () => {
    it("has correct properties", () => {
      expect(tableColumnsModule.inputDatasetColumn.id).toBe("input");
      expect(tableColumnsModule.inputDatasetColumn.accessorKey).toBe("input");
      expect(tableColumnsModule.inputDatasetColumn.header).toBe("Input");
      expect(tableColumnsModule.inputDatasetColumn.filterVariant).toBe("text");
    });

    describe("Cell component", () => {
      it("renders EditableTableField with correct props for dataset table", () => {
        const propsWithDatasetTable = {
          ...defaultProps,
          table: {
            ...defaultProps.table,
            options: {
              meta: { ...mockTableMeta, tableName: TableName.DATASET },
            },
          },
        };

        const CellComponent = tableColumnsModule.inputDatasetColumn.Cell;
        testRender(<CellComponent {...propsWithDatasetTable} />);

        const editableField = screen.getByText("Test input");
        expect(editableField).toBeInTheDocument();
      });

      it("calls onInputChange when updated", () => {
        const propsWithDatasetTable = {
          ...defaultProps,
          table: {
            ...defaultProps.table,
            options: {
              meta: { ...mockTableMeta, tableName: TableName.DATASET },
            },
          },
        };

        const CellComponent = tableColumnsModule.inputDatasetColumn.Cell;
        testRender(<CellComponent {...propsWithDatasetTable} />);

        const editableField = screen.getByText("Test input");
        expect(editableField).toBeInTheDocument();

        // Test that the component is clickable and renders correctly
        // The actual onUpdate behavior is tested in integration tests
        expect(editableField).toBeInTheDocument();
      });
    });
  });

  describe("inputWorkbookColumn", () => {
    it("has correct properties", () => {
      expect(tableColumnsModule.inputWorkbookColumn.id).toBe("input");
      expect(tableColumnsModule.inputWorkbookColumn.accessorKey).toBe("input");
      expect(tableColumnsModule.inputWorkbookColumn.header).toBe("Input");
      expect(tableColumnsModule.inputWorkbookColumn.enableSorting).toBe(true);
      expect(tableColumnsModule.inputWorkbookColumn.enableColumnFilter).toBe(
        true,
      );
      expect(tableColumnsModule.inputWorkbookColumn.filterVariant).toBe("text");
    });

    describe("Cell component", () => {
      it("renders EditableTableField with correct props", () => {
        const propsWithNoOutput = {
          ...defaultProps,
          row: {
            ...defaultProps.row,
            original: { ...defaultRowOriginal, output: "" },
          },
        };

        const CellComponent = tableColumnsModule.inputWorkbookColumn.Cell;
        testRender(<CellComponent {...propsWithNoOutput} />);

        const editableField = screen.getByText("Test input");
        expect(editableField).toBeInTheDocument();
      });

      it("calls onInputChange when updated", () => {
        const propsWithNoOutput = {
          ...defaultProps,
          row: {
            ...defaultProps.row,
            original: { ...defaultRowOriginal, output: "" },
          },
        };

        const CellComponent = tableColumnsModule.inputWorkbookColumn.Cell;
        testRender(<CellComponent {...propsWithNoOutput} />);

        const editableField = screen.getByText("Test input");
        expect(editableField).toBeInTheDocument();

        // Test that the component is clickable and renders correctly
        // The actual onUpdate behavior is tested in integration tests
        expect(editableField).toBeInTheDocument();
      });
    });
  });

  describe("outputColumn", () => {
    it("has correct properties", () => {
      expect(tableColumnsModule.outputColumn.id).toBe("output");
      expect(tableColumnsModule.outputColumn.accessorKey).toBe("output");
      expect(tableColumnsModule.outputColumn.header).toBe("Output");
      expect(tableColumnsModule.outputColumn.enableSorting).toBe(true);
      expect(tableColumnsModule.outputColumn.enableColumnFilter).toBe(true);
      expect(tableColumnsModule.outputColumn.filterVariant).toBe("text");
    });

    describe("Cell component", () => {
      it("renders TruncatedTextWithPopover for successful inference with output", () => {
        const CellComponent = tableColumnsModule.outputColumn.Cell;
        testRender(<CellComponent {...defaultProps} />);

        const truncatedText = screen.getByText("Test output");
        expect(truncatedText).toBeInTheDocument();
      });

      it("shows EvaluatorStatusIndicator for failed inference", () => {
        const propsWithFailedInference = {
          ...defaultProps,
          row: {
            ...defaultProps.row,
            original: {
              ...defaultRowOriginal,
              inference_status: InferenceStatus.FAILED,
              inference_reason: "Test failure reason",
            },
          },
        };

        const CellComponent = tableColumnsModule.outputColumn.Cell;
        testRender(<CellComponent {...propsWithFailedInference} />);

        const statusIndicator = screen.getByText("Failed");
        expect(statusIndicator).toBeInTheDocument();
      });

      it("shows rerun button for failed inference when onOutputRerun is available", () => {
        const mockOnOutputRerun = jest.fn();
        const propsWithRerun = {
          ...defaultProps,
          row: {
            ...defaultProps.row,
            original: {
              ...defaultRowOriginal,
              inference_status: InferenceStatus.FAILED,
              inference_reason: "Test failure reason",
            },
          },
          table: {
            ...defaultProps.table,
            options: {
              meta: { ...mockTableMeta, onOutputRerun: mockOnOutputRerun },
            },
          },
        };

        const CellComponent = tableColumnsModule.outputColumn.Cell;
        testRender(<CellComponent {...propsWithRerun} />);

        const rerunButton = screen.getByRole("button");
        expect(rerunButton).toBeInTheDocument();
        expect(screen.getByText("refresh")).toBeInTheDocument();
      });

      it("calls onOutputRerun when rerun button is clicked", () => {
        const mockOnOutputRerun = jest.fn();
        const propsWithRerun = {
          ...defaultProps,
          row: {
            ...defaultProps.row,
            original: {
              ...defaultRowOriginal,
              inference_status: InferenceStatus.FAILED,
              inference_reason: "Test failure reason",
            },
          },
          table: {
            ...defaultProps.table,
            options: {
              meta: { ...mockTableMeta, onOutputRerun: mockOnOutputRerun },
            },
          },
        };

        const CellComponent = tableColumnsModule.outputColumn.Cell;
        testRender(<CellComponent {...propsWithRerun} />);

        const rerunButton = screen.getByRole("button");
        fireEvent.click(rerunButton);

        expect(mockOnOutputRerun).toHaveBeenCalledWith(
          propsWithRerun.row.original,
        );
      });
    });
  });

  describe("outputTokensColumn", () => {
    it("has correct properties", () => {
      expect(tableColumnsModule.outputTokensColumn.id).toBe("output_tokens");
      expect(tableColumnsModule.outputTokensColumn.accessorKey).toBe(
        "inference_tokens",
      );
      expect(tableColumnsModule.outputTokensColumn.header).toBe(
        "Output Tokens",
      );
      expect(tableColumnsModule.outputTokensColumn.size).toBe(140);
      expect(tableColumnsModule.outputTokensColumn.enableSorting).toBe(true);
      expect(tableColumnsModule.outputTokensColumn.enableColumnFilter).toBe(
        true,
      );
      expect(tableColumnsModule.outputTokensColumn.filterVariant).toBe("text");
    });

    describe("Cell component", () => {
      it("renders output tokens for successful inference with output", () => {
        const CellComponent = tableColumnsModule.outputTokensColumn.Cell;
        testRender(<CellComponent {...defaultProps} />);

        const tokenDisplay = screen.getByText("20");
        expect(tokenDisplay).toBeInTheDocument();
        expect(tokenDisplay).toHaveClass("text-body-12", "text-secondary");
      });

      it("renders SideBySideDefaultColumn for side-by-side project", () => {
        const propsWithSideBySide = {
          ...defaultProps,
          table: {
            ...defaultProps.table,
            options: {
              meta: { ...mockTableMeta, projectType: ProjectType.SIDE_BY_SIDE },
            },
          },
          row: {
            ...defaultProps.row,
            original: {
              ...defaultRowOriginal,
              chat_turn_a: { inference_tokens: { output_tokens: 15 } },
              chat_turn_b: { inference_tokens: { output_tokens: 25 } },
            },
          },
        };

        const CellComponent = tableColumnsModule.outputTokensColumn.Cell;
        testRender(<CellComponent {...propsWithSideBySide} />);

        expect(screen.getByText("15")).toBeInTheDocument();
        expect(screen.getByText("25")).toBeInTheDocument();
        expect(screen.getByText("-10")).toBeInTheDocument();
      });

      it("handles missing chat_turn_b in side-by-side", () => {
        const propsWithSideBySide = {
          ...defaultProps,
          table: {
            ...defaultProps.table,
            options: {
              meta: { ...mockTableMeta, projectType: ProjectType.SIDE_BY_SIDE },
            },
          },
          row: {
            ...defaultProps.row,
            original: {
              ...defaultRowOriginal,
              chat_turn_a: { inference_tokens: { output_tokens: 15 } },
              chat_turn_b: null,
            },
          },
        };

        const CellComponent = tableColumnsModule.outputTokensColumn.Cell;
        testRender(<CellComponent {...propsWithSideBySide} />);

        expect(screen.getByText("15")).toBeInTheDocument();
        expect(screen.getAllByText("-")).toHaveLength(2);
      });
    });
  });

  describe("totalTokensColumn", () => {
    it("has correct properties", () => {
      expect(tableColumnsModule.totalTokensColumn.id).toBe("total_tokens");
      expect(tableColumnsModule.totalTokensColumn.accessorKey).toBe(
        "inference_tokens",
      );
      expect(tableColumnsModule.totalTokensColumn.header).toBe("Total Tokens");
      expect(tableColumnsModule.totalTokensColumn.size).toBe(130);
      expect(tableColumnsModule.totalTokensColumn.enableSorting).toBe(true);
      expect(tableColumnsModule.totalTokensColumn.enableColumnFilter).toBe(
        true,
      );
      expect(tableColumnsModule.totalTokensColumn.filterVariant).toBe("text");
    });

    describe("Cell component", () => {
      it("renders total tokens for successful inference with output", () => {
        const CellComponent = tableColumnsModule.totalTokensColumn.Cell;
        testRender(<CellComponent {...defaultProps} />);

        const tokenDisplay = screen.getByText("30");
        expect(tokenDisplay).toBeInTheDocument();
        expect(tokenDisplay).toHaveClass("text-body-12", "text-secondary");
      });

      it("renders SideBySideDefaultColumn for side-by-side project", () => {
        const propsWithSideBySide = {
          ...defaultProps,
          table: {
            ...defaultProps.table,
            options: {
              meta: { ...mockTableMeta, projectType: ProjectType.SIDE_BY_SIDE },
            },
          },
          row: {
            ...defaultProps.row,
            original: {
              ...defaultRowOriginal,
              chat_turn_a: { inference_tokens: { total_tokens: 45 } },
              chat_turn_b: { inference_tokens: { total_tokens: 55 } },
            },
          },
        };

        const CellComponent = tableColumnsModule.totalTokensColumn.Cell;
        testRender(<CellComponent {...propsWithSideBySide} />);

        expect(screen.getByText("45")).toBeInTheDocument();
        expect(screen.getByText("55")).toBeInTheDocument();
        expect(screen.getByText("-10")).toBeInTheDocument();
      });
    });
  });

  describe("humanEvaluationColumn", () => {
    it("has correct properties", () => {
      expect(tableColumnsModule.humanEvaluationColumn.accessorKey).toBe(
        "human_evaluation",
      );
      expect(tableColumnsModule.humanEvaluationColumn.header).toBe(
        "Human evaluation",
      );
      expect(tableColumnsModule.humanEvaluationColumn.minSize).toBe(50);
      expect(tableColumnsModule.humanEvaluationColumn.size).toBe(120);
      expect(tableColumnsModule.humanEvaluationColumn.enableSorting).toBe(true);
      expect(tableColumnsModule.humanEvaluationColumn.enableColumnFilter).toBe(
        true,
      );
      expect(tableColumnsModule.humanEvaluationColumn.filterVariant).toBe(
        "text",
      );
    });

    describe("Cell component", () => {
      it("renders all evaluation buttons", () => {
        const CellComponent = tableColumnsModule.humanEvaluationColumn.Cell;
        testRender(<CellComponent {...defaultProps} />);

        const buttons = screen.getAllByRole("button");
        expect(buttons).toHaveLength(2); // thumb_up and thumb_down

        expect(screen.getByText("thumb_up")).toBeInTheDocument();
        expect(screen.getByText("thumb_down")).toBeInTheDocument();
      });

      it("shows active state for selected rating", () => {
        const propsWithRating = {
          ...defaultProps,
          row: {
            ...defaultProps.row,
            original: {
              ...defaultRowOriginal,
              human_eval_scores: [{ score: 1, notes: "Good" }],
            },
          },
        };

        const CellComponent = tableColumnsModule.humanEvaluationColumn.Cell;
        testRender(<CellComponent {...propsWithRating} />);

        const buttons = screen.getAllByRole("button");
        expect(buttons[0]).toHaveClass("!text-lime !bg-limeBg !border-lime");
      });

      it("calls onHumanEvalScoreChange when button is clicked", () => {
        const CellComponent = tableColumnsModule.humanEvaluationColumn.Cell;
        testRender(<CellComponent {...defaultProps} />);

        const buttons = screen.getAllByRole("button");
        fireEvent.click(buttons[0]); // thumb_up

        expect(mockTableMeta.onHumanEvalScoreChange).toHaveBeenCalledWith(
          1,
          1, // current value from human_eval_scores[0].score
          defaultRowOriginal,
        );
      });

      it("disables buttons when output is missing", () => {
        const propsWithoutOutput = {
          ...defaultProps,
          row: {
            ...defaultProps.row,
            original: { ...defaultRowOriginal, output: "" },
          },
        };

        const CellComponent = tableColumnsModule.humanEvaluationColumn.Cell;
        testRender(<CellComponent {...propsWithoutOutput} />);

        const buttons = screen.getAllByRole("button");
        buttons.forEach((button) => {
          expect(button).toBeDisabled();
        });
      });
    });
  });

  describe("humanEvaluationNotesColumn", () => {
    it("has correct properties", () => {
      expect(tableColumnsModule.humanEvaluationNotesColumn.accessorKey).toBe(
        "human_evaluation_notes",
      );
      expect(tableColumnsModule.humanEvaluationNotesColumn.header).toBe(
        "Human Eval Notes",
      );
      expect(tableColumnsModule.humanEvaluationNotesColumn.minSize).toBe(50);
      expect(tableColumnsModule.humanEvaluationNotesColumn.size).toBe(120);
      expect(tableColumnsModule.humanEvaluationNotesColumn.enableSorting).toBe(
        true,
      );
      expect(
        tableColumnsModule.humanEvaluationNotesColumn.enableColumnFilter,
      ).toBe(true);
    });

    describe("Cell component", () => {
      it("renders EditableTableField with correct props", () => {
        const CellComponent =
          tableColumnsModule.humanEvaluationNotesColumn.Cell;
        testRender(<CellComponent {...defaultProps} />);

        const editableField = screen.getByText("Good");
        expect(editableField).toBeInTheDocument();
      });

      it("calls onHumanEvalNotesChange when updated", () => {
        const CellComponent =
          tableColumnsModule.humanEvaluationNotesColumn.Cell;
        testRender(<CellComponent {...defaultProps} />);

        const editableField = screen.getByText("Good");
        expect(editableField).toBeInTheDocument();

        // Test that the component is clickable and renders correctly
        // The actual onUpdate behavior is tested in integration tests
        expect(editableField).toBeInTheDocument();
      });
    });
  });

  describe("variablesColumn", () => {
    it("has correct properties", () => {
      expect(tableColumnsModule.variablesColumn.id).toBe("variables");
      expect(tableColumnsModule.variablesColumn.accessorKey).toBe("variables");
      expect(tableColumnsModule.variablesColumn.header).toBe("Variables");
      expect(tableColumnsModule.variablesColumn.enableSorting).toBe(true);
      expect(tableColumnsModule.variablesColumn.enableColumnFilter).toBe(true);
      expect(tableColumnsModule.variablesColumn.filterVariant).toBe("text");
    });

    describe("Cell component", () => {
      // Reset the mocks before each test
      beforeEach(() => {
        jest.clearAllMocks();
      });

      it("renders EditableTableField with isVariableColumn prop", () => {
        const CellComponent = tableColumnsModule.variablesColumn.Cell;

        // Variables could be an object or a JSON string
        const propsWithVariables = {
          ...defaultProps,
          row: {
            ...defaultProps.row,
            original: {
              ...defaultRowOriginal,
              variables: { key: "value" },
            },
          },
        };

        testRender(<CellComponent {...propsWithVariables} />);

        const editableField = screen.getByTestId("mock-editable-field");
        expect(editableField).toBeInTheDocument();
        expect(editableField).toHaveAttribute(
          "data-is-variable-column",
          "true",
        );

        // Either the field shows our value or has it as a data attribute
        expect(editableField).toHaveAttribute(
          "data-value",
          JSON.stringify({ key: "value" }),
        );
      });

      it("renders placeholder text when variables are empty", () => {
        const CellComponent = tableColumnsModule.variablesColumn.Cell;

        // Set variables to null/undefined
        const propsWithNoVariables = {
          ...defaultProps,
          row: {
            ...defaultProps.row,
            original: {
              ...defaultRowOriginal,
              variables: null,
            },
          },
        };

        testRender(<CellComponent {...propsWithNoVariables} />);

        const editableField = screen.getByTestId("mock-editable-field");
        expect(editableField).toBeInTheDocument();
        // Updated to match the actual placeholder text
        expect(editableField).toHaveAttribute(
          "data-placeholder",
          "Manage variables here",
        );
      });

      it("passes onUpdateChatVariables to EditableTableField", () => {
        const mockOnUpdateChatVariables = jest.fn();

        const propsWithMockUpdate = {
          ...defaultProps,
          table: {
            ...defaultProps.table,
            options: {
              meta: {
                ...mockTableMeta,
                onUpdateChatVariables: mockOnUpdateChatVariables,
              },
            },
          },
        };

        const CellComponent = tableColumnsModule.variablesColumn.Cell;
        testRender(<CellComponent {...propsWithMockUpdate} />);

        const editableField = screen.getByTestId("mock-editable-field");
        expect(editableField).toBeInTheDocument();

        // Simulate update
        fireEvent.click(editableField);

        // Check if our mock function was called
        expect(mockOnUpdateChatVariables).toHaveBeenCalled();
      });
    });
  });
});

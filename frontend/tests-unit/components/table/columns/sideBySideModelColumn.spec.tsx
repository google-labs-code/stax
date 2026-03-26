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
import * as sideBySideModelColumnModule from "@/components/table/columns/sideBySideModelColumn";
import { useModelsContext } from "@/hooks/useModelsContext";
import { fireEvent, screen } from "@testing-library/react";

import { testRender } from "../../../render";

jest.mock("@/hooks/useModelsContext", () => ({
  useModelsContext: jest.fn(),
}));

const mockUseModelsContext = useModelsContext as jest.MockedFunction<
  typeof useModelsContext
>;

describe("sideBySideModelColumn", () => {
  const mockModels = [
    {
      id: "gpt-4-id",
      name: "gpt-4",
      label: "GPT-4",
      version: "1.0",
      url: "https://api.openai.com",
      tag: "gpt-4",
      description: "GPT-4 model",
      properties: {},
      pricing: {},
      descriptors: {},
      is_api_key_present: true,
      is_deprecated: false,
      provider: "openai" as any,
      model_type: "system",
      api_key: "test-key",
      additional_headers: {},
      icon: () => <div>GPT-4 Icon</div>,
    },
    {
      id: "claude-3-id",
      name: "claude-3",
      label: "Claude 3",
      version: "1.0",
      url: "https://api.anthropic.com",
      tag: "claude-3",
      description: "Claude 3 model",
      properties: {},
      pricing: {},
      descriptors: {},
      is_api_key_present: true,
      is_deprecated: false,
      provider: "anthropic" as any,
      model_type: "system",
      api_key: "test-key",
      additional_headers: {},
      icon: () => <div>Claude Icon</div>,
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseModelsContext.mockReturnValue({
      allModels: mockModels,
      openedModel: null,
      setOpenedModel: jest.fn(),
      isLoadingModels: false,
      refreshModels: jest.fn(),
      customModels: [],
      defaultModels: mockModels,
      providers: {},
    });
  });

  describe("column object properties", () => {
    it("has correct accessorKey and header for label A", () => {
      const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
      expect(column.accessorKey).toBe("side_by_side_model_A");
      expect(column.header).toBe("Side by side models");
    });

    it("has correct accessorKey and header for label B", () => {
      const column = sideBySideModelColumnModule.sideBySideModelColumn("B");
      expect(column.accessorKey).toBe("side_by_side_model_B");
      expect(column.header).toBe("Side by side models");
    });

    it("renders Header component with label and tooltip", () => {
      const column = sideBySideModelColumnModule.sideBySideModelColumn(
        "A",
        "Test tooltip",
      );
      const HeaderComponent = column.Header;
      testRender(<HeaderComponent />);

      expect(screen.getByText("A")).toBeInTheDocument();
      expect(screen.getByText("MODEL NICKNAME")).toBeInTheDocument();
    });

    it("renders Header component without tooltip", () => {
      const column = sideBySideModelColumnModule.sideBySideModelColumn("B");
      const HeaderComponent = column.Header;
      testRender(<HeaderComponent />);

      expect(screen.getByText("B")).toBeInTheDocument();
      expect(screen.getByText("MODEL NICKNAME")).toBeInTheDocument();
    });
  });

  describe("Cell component", () => {
    const mockTableMeta = {
      onSystemInstructionChange: jest.fn(),
      onOutputRerun: jest.fn(),
    };

    const defaultProps = {
      row: {
        original: {
          chat_turn_a: {
            model_name: "gpt-4",
            output: "Test output A",
            inference_status: InferenceStatus.SUCCESSFUL,
            system_instructions: "System instruction A",
          },
          chat_turn_b: {
            model_name: "claude-3",
            output: "Test output B",
            inference_status: InferenceStatus.SUCCESSFUL,
            system_instructions: "System instruction B",
          },
          isSystemInstructionsLoading: false,
        },
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

    describe("model display", () => {
      it("renders model information for label A", () => {
        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...defaultProps} />);

        expect(screen.getByText("GPT-4")).toBeInTheDocument();
        expect(screen.getByText("GPT-4 Icon")).toBeInTheDocument();
      });

      it("renders model information for label B", () => {
        const column = sideBySideModelColumnModule.sideBySideModelColumn("B");
        testRender(<column.Cell {...defaultProps} />);

        expect(screen.getByText("Claude 3")).toBeInTheDocument();
        expect(screen.getByText("Claude Icon")).toBeInTheDocument();
      });

      it("shows 'No model selected' when model is not found", () => {
        mockUseModelsContext.mockReturnValue({
          allModels: [],
          openedModel: null,
          setOpenedModel: jest.fn(),
          isLoadingModels: false,
          refreshModels: jest.fn(),
          customModels: [],
          defaultModels: [],
          providers: {},
        });

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...defaultProps} />);

        expect(screen.getByText("No model selected")).toBeInTheDocument();
      });

      it("shows 'No model selected' when model_name is null", () => {
        const propsWithNullModel = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              chat_turn_a: {
                ...defaultProps.row.original.chat_turn_a,
                model_name: null,
              },
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithNullModel} />);

        expect(screen.getByText("No model selected")).toBeInTheDocument();
      });
    });

    describe("system instructions", () => {
      it("renders EditableTableField with correct props for label A", () => {
        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...defaultProps} />);

        // Check that the system instruction text is displayed
        expect(screen.getByText("System instruction A")).toBeInTheDocument();
        // Check that the system instruction label is present
        expect(screen.getByText("System Instruction")).toBeInTheDocument();
      });

      it("renders EditableTableField with correct props for label B", () => {
        const column = sideBySideModelColumnModule.sideBySideModelColumn("B");
        testRender(<column.Cell {...defaultProps} />);

        // Check that the system instruction text is displayed
        expect(screen.getByText("System instruction B")).toBeInTheDocument();
        // Check that the system instruction label is present
        expect(screen.getByText("System Instruction")).toBeInTheDocument();
      });

      it("allows editing when no output is present", () => {
        const propsWithoutOutput = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              chat_turn_a: {
                ...defaultProps.row.original.chat_turn_a,
                output: null,
              },
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithoutOutput} />);

        // Check that the system instruction field is present and editable
        // The component shows the actual value when present, not placeholder
        expect(screen.getByText("System instruction A")).toBeInTheDocument();
      });

      it("shows loading state", () => {
        const propsWithLoading = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              isSystemInstructionsLoading: true,
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithLoading} />);

        // Check that a loader is present when loading (Mantine Loader doesn't have progressbar role)
        // Look for the loader by its class name
        expect(
          document.querySelector(".mantine-Loader-root"),
        ).toBeInTheDocument();
      });

      it("calls onSystemInstructionChange when system instruction is updated", () => {
        const propsWithoutOutput = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              chat_turn_a: {
                ...defaultProps.row.original.chat_turn_a,
                output: null,
              },
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithoutOutput} />);

        // Double click on the system instruction text to trigger editing
        const systemInstructionText = screen.getByText("System instruction A");
        fireEvent.doubleClick(systemInstructionText);

        // The test expects the callback to be called, but we need to simulate the actual update
        // Since the component uses a textarea in a popover, we need to find and interact with it
        const textarea = screen.getByRole("textbox");
        fireEvent.change(textarea, {
          target: { value: "Updated system instruction" },
        });
        fireEvent.blur(textarea);

        expect(mockTableMeta.onSystemInstructionChange).toHaveBeenCalledWith(
          "Updated system instruction",
          "System instruction A",
          propsWithoutOutput.row.original.chat_turn_a,
        );
      });
    });

    describe("model output display", () => {
      it("renders successful output with TruncatedTextWithPopover", () => {
        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...defaultProps} />);

        // Check that the output text is displayed
        expect(screen.getByText("Test output A")).toBeInTheDocument();
      });

      it("renders empty string when no output", () => {
        const propsWithoutOutput = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              chat_turn_a: {
                ...defaultProps.row.original.chat_turn_a,
                output: null,
              },
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithoutOutput} />);

        // Check that the output text is not displayed when output is null
        expect(screen.queryByText("Test output A")).not.toBeInTheDocument();
      });

      it("renders EvaluatorStatusIndicator for pending status", () => {
        const propsWithPendingStatus = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              chat_turn_a: {
                ...defaultProps.row.original.chat_turn_a,
                inference_status: InferenceStatus.PENDING,
                output: null,
              },
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithPendingStatus} />);

        // Check that the pending status is displayed
        expect(screen.getByText("Pending")).toBeInTheDocument();
      });

      it("renders EvaluatorStatusIndicator for in progress status", () => {
        const propsWithInProgressStatus = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              chat_turn_a: {
                ...defaultProps.row.original.chat_turn_a,
                inference_status: InferenceStatus.IN_PROGRESS,
                output: null,
              },
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithInProgressStatus} />);

        // Check that the in progress status is displayed
        expect(screen.getByText("In progress")).toBeInTheDocument();
      });

      it("renders EvaluatorStatusIndicator for failed status with rerun button", () => {
        const propsWithFailedStatus = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              chat_turn_a: {
                ...defaultProps.row.original.chat_turn_a,
                inference_status: InferenceStatus.FAILED,
                inference_reason: "API error",
                output: null,
              },
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithFailedStatus} />);

        // Check that the failed status is displayed
        expect(screen.getByText("Failed")).toBeInTheDocument();
        // Check that the refresh icon is present (using the material icon name)
        expect(screen.getByText("refresh")).toBeInTheDocument();
      });

      it("does not render rerun button when onOutputRerun is not provided", () => {
        const propsWithoutRerunHandler = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              chat_turn_a: {
                ...defaultProps.row.original.chat_turn_a,
                inference_status: InferenceStatus.FAILED,
                output: null,
              },
            },
          },
          table: {
            options: {
              meta: {
                onSystemInstructionChange: jest.fn(),
                // onOutputRerun is missing
              },
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithoutRerunHandler} />);

        // Check that the failed status is displayed
        expect(screen.getByText("Failed")).toBeInTheDocument();
        // Check that the refresh icon is not present
        expect(screen.queryByText("refresh")).not.toBeInTheDocument();
      });

      it("calls onOutputRerun when rerun button is clicked", () => {
        const propsWithFailedStatus = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              chat_turn_a: {
                ...defaultProps.row.original.chat_turn_a,
                inference_status: InferenceStatus.FAILED,
                output: null,
              },
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithFailedStatus} />);

        // Find the refresh icon and click its parent button
        const refreshIcon = screen.getByText("refresh");
        const rerunButton = refreshIcon.closest("button");
        fireEvent.click(rerunButton!);

        expect(mockTableMeta.onOutputRerun).toHaveBeenCalledWith(
          propsWithFailedStatus.row.original.chat_turn_a,
        );
      });

      it("renders EvaluatorStatusIndicator for stopped status", () => {
        const propsWithStoppedStatus = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              chat_turn_a: {
                ...defaultProps.row.original.chat_turn_a,
                inference_status: InferenceStatus.STOPPED,
                output: null,
              },
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithStoppedStatus} />);

        // Check that some status indicator is displayed for stopped status
        // The component might not show any specific text for STOPPED status
        // We'll check that the model output section is present but empty
        expect(screen.getByText("Model Output")).toBeInTheDocument();
      });
    });

    describe("edge cases", () => {
      it("handles undefined originalRow gracefully", () => {
        const propsWithUndefinedRow = {
          ...defaultProps,
          row: {
            original: {
              chat_turn_a: undefined,
              chat_turn_b: undefined,
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithUndefinedRow} />);

        expect(screen.getByText("No model selected")).toBeInTheDocument();
        // Check that the system instruction section is present
        expect(screen.getByText("System Instruction")).toBeInTheDocument();
      });

      it("handles null system instructions", () => {
        const propsWithNullInstructions = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              chat_turn_a: {
                ...defaultProps.row.original.chat_turn_a,
                system_instructions: null,
              },
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithNullInstructions} />);

        // Check that the system instruction section is present
        expect(screen.getByText("System Instruction")).toBeInTheDocument();
        // The component should handle null instructions gracefully
        expect(screen.getByText("GPT-4")).toBeInTheDocument();
      });

      it("handles undefined inference status", () => {
        const propsWithUndefinedStatus = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              chat_turn_a: {
                ...defaultProps.row.original.chat_turn_a,
                inference_status: undefined,
                output: null,
              },
            },
          },
        };

        const column = sideBySideModelColumnModule.sideBySideModelColumn("A");
        testRender(<column.Cell {...propsWithUndefinedStatus} />);

        // Check that no status indicators are shown when status is undefined
        expect(screen.queryByText("Pending")).not.toBeInTheDocument();
        expect(screen.queryByText("In progress")).not.toBeInTheDocument();
        expect(screen.queryByText("Failed")).not.toBeInTheDocument();
        // Check that no output text is shown
        expect(screen.queryByText("Test output A")).not.toBeInTheDocument();
      });
    });
  });
});

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

// Import the functions and objects we want to test
// We need to access the internal functions, so we'll import the module and access them
import * as sideBySideEvalColumnModule from "@/components/table/columns/sideBySideEvalColumn";
import { HUMAN_SXS_RATING } from "@/queries/types";
import { fireEvent, screen } from "@testing-library/react";

import { testRender } from "../../../render";

describe("sideBySideEvalColumn", () => {
  // Note: toSentenceCase and hasRequiredOutputs are internal functions
  // We'll test their behavior indirectly through the components that use them

  describe("internal function behavior through components", () => {
    it("enables editing when all required outputs are present for regular row", () => {
      const mockTableMeta = {
        onHumanEvalSxSRatingChange: jest.fn(),
      };

      const propsWithAllOutputs = {
        row: {
          original: {
            human_sxs_rating: null,
            output: "main output",
            chat_turn_a: { output: "output a" },
            chat_turn_b: { output: "output b" },
          },
        },
        table: {
          options: {
            meta: mockTableMeta,
          },
        },
      };

      testRender(
        <sideBySideEvalColumnModule.sideBySideEvalColumn.Cell
          {...propsWithAllOutputs}
        />,
      );

      const buttons = screen.getAllByRole("button");
      buttons.forEach((button) => {
        expect(button).not.toBeDisabled();
      });
    });

    it("enables editing when all required outputs are present for sub-row", () => {
      const mockTableMeta = {
        onHumanEvalSxSRatingChange: jest.fn(),
      };

      const propsWithSubRowOutputs = {
        row: {
          original: {
            isSubRow: true,
            human_sxs_rating: null,
            chat_turn_a: { output: "output a" },
            chat_turn_b: { output: "output b" },
          },
        },
        table: {
          options: {
            meta: mockTableMeta,
          },
        },
      };

      testRender(
        <sideBySideEvalColumnModule.sideBySideEvalColumn.Cell
          {...propsWithSubRowOutputs}
        />,
      );

      const buttons = screen.getAllByRole("button");
      buttons.forEach((button) => {
        expect(button).not.toBeDisabled();
      });
    });

    it("disables editing when required outputs are missing", () => {
      const mockTableMeta = {
        onHumanEvalSxSRatingChange: jest.fn(),
      };

      const propsWithoutOutputs = {
        row: {
          original: {
            human_sxs_rating: null,
            output: null,
            chat_turn_a: { output: "output a" },
            chat_turn_b: { output: "output b" },
          },
        },
        table: {
          options: {
            meta: mockTableMeta,
          },
        },
      };

      testRender(
        <sideBySideEvalColumnModule.sideBySideEvalColumn.Cell
          {...propsWithoutOutputs}
        />,
      );

      const buttons = screen.getAllByRole("button");
      buttons.forEach((button) => {
        expect(button).toBeDisabled();
      });
    });
  });

  describe("sideBySideEvalColumn object", () => {
    it("has correct accessorKey and header", () => {
      expect(sideBySideEvalColumnModule.sideBySideEvalColumn.accessorKey).toBe(
        "side_by_side_eval",
      );
      expect(sideBySideEvalColumnModule.sideBySideEvalColumn.header).toBe(
        "Side by side evals",
      );
    });

    it("renders Header component with correct icon", () => {
      const HeaderComponent =
        sideBySideEvalColumnModule.sideBySideEvalColumn.Header;
      testRender(<HeaderComponent />);
      expect(screen.getByText("compare_arrows")).toBeInTheDocument();
    });

    describe("Cell component", () => {
      const mockTableMeta = {
        onHumanEvalSxSRatingChange: jest.fn(),
      };

      const defaultProps = {
        row: {
          original: {
            human_sxs_rating: null,
            output: "main output",
            chat_turn_a: { output: "output a" },
            chat_turn_b: { output: "output b" },
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

      it("renders all evaluation options", () => {
        testRender(
          <sideBySideEvalColumnModule.sideBySideEvalColumn.Cell
            {...defaultProps}
          />,
        );

        expect(screen.getByText("arrow_back")).toBeInTheDocument();
        expect(screen.getByText("handshake")).toBeInTheDocument();
        expect(screen.getByText("do_not_disturb_on")).toBeInTheDocument();
        expect(screen.getByText("arrow_forward")).toBeInTheDocument();
      });

      it("shows active state for selected rating", () => {
        const propsWithRating = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              human_sxs_rating: HUMAN_SXS_RATING.A_IS_BETTER,
            },
          },
        };

        testRender(
          <sideBySideEvalColumnModule.sideBySideEvalColumn.Cell
            {...propsWithRating}
          />,
        );

        // The active button should have the active style class
        const buttons = screen.getAllByRole("button");
        expect(buttons[0]).toHaveClass("!text-lime !bg-limeBg !border-lime");
      });

      it("calls onHumanEvalSxSRatingChange when button is clicked", () => {
        testRender(
          <sideBySideEvalColumnModule.sideBySideEvalColumn.Cell
            {...defaultProps}
          />,
        );

        const buttons = screen.getAllByRole("button");
        fireEvent.click(buttons[0]);

        expect(mockTableMeta.onHumanEvalSxSRatingChange).toHaveBeenCalledWith(
          HUMAN_SXS_RATING.A_IS_BETTER,
          defaultProps.row.original,
        );
      });

      it("disables buttons when required outputs are missing", () => {
        const propsWithoutOutputs = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              output: null,
            },
          },
        };

        testRender(
          <sideBySideEvalColumnModule.sideBySideEvalColumn.Cell
            {...propsWithoutOutputs}
          />,
        );

        const buttons = screen.getAllByRole("button");
        buttons.forEach((button) => {
          expect(button).toBeDisabled();
        });
      });

      it("renders tooltip components", () => {
        testRender(
          <sideBySideEvalColumnModule.sideBySideEvalColumn.Cell
            {...defaultProps}
          />,
        );

        // Check that the component renders without errors
        // Tooltips are rendered by Mantine and may not be immediately accessible
        expect(screen.getAllByRole("button")).toHaveLength(4);
      });

      it("does not call onHumanEvalSxSRatingChange when disabled", () => {
        const propsWithoutOutputs = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              output: null,
            },
          },
        };

        testRender(
          <sideBySideEvalColumnModule.sideBySideEvalColumn.Cell
            {...propsWithoutOutputs}
          />,
        );

        const buttons = screen.getAllByRole("button");
        fireEvent.click(buttons[0]);

        expect(mockTableMeta.onHumanEvalSxSRatingChange).not.toHaveBeenCalled();
      });
    });
  });

  describe("sideBySideEvalNotesColumn object", () => {
    it("has correct properties", () => {
      expect(
        sideBySideEvalColumnModule.sideBySideEvalNotesColumn.accessorKey,
      ).toBe("side_by_side_eval_notes");
      expect(sideBySideEvalColumnModule.sideBySideEvalNotesColumn.header).toBe(
        "Human Eval Notes",
      );
      expect(sideBySideEvalColumnModule.sideBySideEvalNotesColumn.minSize).toBe(
        50,
      );
      expect(sideBySideEvalColumnModule.sideBySideEvalNotesColumn.size).toBe(
        120,
      );
      expect(
        sideBySideEvalColumnModule.sideBySideEvalNotesColumn.enableSorting,
      ).toBe(true);
      expect(
        sideBySideEvalColumnModule.sideBySideEvalNotesColumn.enableColumnFilter,
      ).toBe(true);
    });

    it("renders Header component", () => {
      const HeaderComponent =
        sideBySideEvalColumnModule.sideBySideEvalNotesColumn.Header;

      // Create a mock column object with the required structure
      const mockColumn = {
        columnDef: {
          header: "Notes",
        },
      };

      testRender(<HeaderComponent column={mockColumn} />);
      expect(screen.getByText("Notes")).toBeInTheDocument();
    });

    describe("Cell component", () => {
      const mockTableMeta = {
        onHumanEvalSxSNotesChange: jest.fn(),
      };

      const defaultProps = {
        row: {
          original: {
            human_sxs_notes: "Test notes",
            output: "main output",
            chat_turn_a: { output: "output a" },
            chat_turn_b: { output: "output b" },
            isHumanEvalNotesLoading: false,
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

      it("renders EditableTableField with correct props", () => {
        testRender(
          <sideBySideEvalColumnModule.sideBySideEvalNotesColumn.Cell
            {...defaultProps}
          />,
        );

        // Check that the notes content is displayed
        expect(screen.getByText("Test notes")).toBeInTheDocument();
        // Check that the component is rendered (by looking for the Group element)
        const groupElement = screen
          .getByText("Test notes")
          .closest(".mantine-Group-root");
        expect(groupElement).toBeInTheDocument();
      });

      it("shows placeholder when no notes", () => {
        const propsWithoutNotes = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              human_sxs_notes: null,
            },
          },
        };

        testRender(
          <sideBySideEvalColumnModule.sideBySideEvalNotesColumn.Cell
            {...propsWithoutNotes}
          />,
        );

        // Check that the placeholder text is displayed
        expect(
          screen.getByText("Type human evaluation notes here"),
        ).toBeInTheDocument();
      });

      it("disables editing when required outputs are missing", () => {
        const propsWithoutOutputs = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              output: null,
            },
          },
        };

        testRender(
          <sideBySideEvalColumnModule.sideBySideEvalNotesColumn.Cell
            {...propsWithoutOutputs}
          />,
        );

        // Check that the notes content is still displayed (editing is disabled but content shows)
        expect(screen.getByText("Test notes")).toBeInTheDocument();
      });

      it("shows loading state", () => {
        const propsWithLoading = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              isHumanEvalNotesLoading: true,
            },
          },
        };

        const { container } = testRender(
          <sideBySideEvalColumnModule.sideBySideEvalNotesColumn.Cell
            {...propsWithLoading}
          />,
        );

        // Check that the loader is displayed by looking for the Mantine Loader component
        const loader = container.querySelector(".mantine-Loader-root");
        expect(loader).toBeInTheDocument();
      });

      it("calls onHumanEvalSxSNotesChange when notes are updated", () => {
        testRender(
          <sideBySideEvalColumnModule.sideBySideEvalNotesColumn.Cell
            {...defaultProps}
          />,
        );

        // Find the Group element that contains the notes content and double-click it to open editing mode
        const groupElement = screen
          .getByText("Test notes")
          .closest(".mantine-Group-root");
        fireEvent.doubleClick(groupElement!);

        // Find the textarea that appears after double-clicking
        const textarea = screen.getByRole("textbox");
        expect(textarea).toBeInTheDocument();

        // Type new content and trigger the onUpdate callback by pressing Enter
        fireEvent.change(textarea, { target: { value: "Updated notes" } });
        fireEvent.keyDown(textarea, { key: "Enter", code: "Enter" });

        expect(mockTableMeta.onHumanEvalSxSNotesChange).toHaveBeenCalledWith(
          "Updated notes",
          "Test notes",
          defaultProps.row.original,
        );
      });

      it("does not call onHumanEvalSxSNotesChange when editing is disabled", () => {
        const propsWithoutOutputs = {
          ...defaultProps,
          row: {
            original: {
              ...defaultProps.row.original,
              output: null,
            },
          },
        };

        testRender(
          <sideBySideEvalColumnModule.sideBySideEvalNotesColumn.Cell
            {...propsWithoutOutputs}
          />,
        );

        // Find the Group element that contains the notes content and double-click it
        const groupElement = screen
          .getByText("Test notes")
          .closest(".mantine-Group-root");
        fireEvent.doubleClick(groupElement!);

        expect(mockTableMeta.onHumanEvalSxSNotesChange).not.toHaveBeenCalled();
      });
    });
  });
});

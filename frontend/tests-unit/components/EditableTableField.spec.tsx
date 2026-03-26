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

import EditableTableField from "@/components/EditableTableField";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { fireEvent, screen } from "@testing-library/react";

import { testRender } from "../render";

jest.mock(
  "@/app/(authRoutes)/projects/[id]/playground/components/ManageVariablesModal",
  () => ({
    __esModule: true,
    default: ({
      isOpen,
      onClose,
      variables,
      onUpdate,
    }: {
      isOpen: boolean;
      onClose: () => void;
      variables: Record<string, string>;
      onUpdate: (variables: Record<string, string>) => void;
    }) =>
      isOpen ? (
        <div data-testid="variables-modal">
          <button data-testid="close-modal" onClick={onClose}>
            Close
          </button>
          <button
            data-testid="update-variables"
            onClick={() => onUpdate({ newVar: "updated" })}
          >
            Update Variables
          </button>
          <div>Variables: {JSON.stringify(variables)}</div>
        </div>
      ) : null,
  }),
);

jest.mock("@/components/TruncatedTextWithPopover", () => ({
  __esModule: true,
  default: ({
    text,
    additionalClassName,
    isJsonFormat,
  }: {
    text: string;
    additionalClassName?: string;
    isJsonFormat?: boolean;
  }) => (
    <div
      data-testid="truncated-text"
      data-json={isJsonFormat ? "true" : "false"}
      className={additionalClassName}
    >
      {text}
    </div>
  ),
}));

const mockPush = jest.fn();
jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: mockPush,
  }),
  useParams: () => ({
    id: "mock-project-id",
  }),
}));

const mockSetPairId = jest.fn();
const mockSetHumanEvaluator = jest.fn();
const mockSetSelectedChatTurnIds = jest.fn();
const mockSetSelectedChatIds = jest.fn();

jest.mock("@/hooks/usePlaygroundContext", () => ({
  usePlaygroundContext: jest.fn(),
}));

jest.mock("@/hooks/useChatContext", () => ({
  useChatContext: () => ({
    chats: [],
    setCurrentChatId: jest.fn(),
    setCurrentChatTurnId: jest.fn(),
    setPairId: jest.fn(),
    setCurrentChatIdB: jest.fn(),
  }),
}));

describe("EditableTableField", () => {
  const defaultProps = {
    value: "Sample Text",
    placeholderText: "Type something...",
    onUpdate: jest.fn(),
    isEnabled: true,
    allowEdit: true,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (usePlaygroundContext as jest.Mock).mockReturnValue(null);
  });

  it("renders with value", () => {
    testRender(<EditableTableField {...defaultProps} />);
    expect(screen.getByTestId("truncated-text")).toHaveTextContent(
      "Sample Text",
    );
  });

  it("shows placeholder when no value is given", () => {
    testRender(<EditableTableField {...defaultProps} value="" />);
    expect(screen.getByText("Type something...")).toBeInTheDocument();
  });

  it("enters edit mode on double-click", () => {
    testRender(<EditableTableField {...defaultProps} />);

    const textElement = screen.getByTestId("truncated-text").parentElement;
    if (textElement) {
      fireEvent.doubleClick(textElement);
    }

    const textarea = screen.getByRole("textbox");
    expect(textarea).toBeInTheDocument();
  });

  it("calls onUpdate on blur", () => {
    const onUpdate = jest.fn();
    testRender(
      <EditableTableField {...defaultProps} value="Old" onUpdate={onUpdate} />,
    );

    const textElement = screen.getByTestId("truncated-text").parentElement;
    if (textElement) {
      fireEvent.doubleClick(textElement);
    }

    const textarea = screen.getByRole("textbox");
    fireEvent.change(textarea, { target: { value: "New Value" } });
    fireEvent.blur(textarea);

    expect(onUpdate).toHaveBeenCalledWith("New Value");
  });

  it("calls onUpdate on Enter key press", () => {
    const onUpdate = jest.fn();
    testRender(
      <EditableTableField {...defaultProps} value="Old" onUpdate={onUpdate} />,
    );

    const textElement = screen.getByTestId("truncated-text").parentElement;
    if (textElement) {
      fireEvent.doubleClick(textElement);
    }

    const textarea = screen.getByRole("textbox");
    fireEvent.change(textarea, { target: { value: "Updated!" } });
    fireEvent.keyDown(textarea, {
      key: "Enter",
      nativeEvent: { shiftKey: false },
    });

    expect(onUpdate).toHaveBeenCalledWith("Updated!");
  });

  it("shows loading state", () => {
    testRender(<EditableTableField {...defaultProps} isLoading={true} />);
    expect(
      screen.getByText(
        (content, element) =>
          element?.tagName.toLowerCase() === "span" &&
          element.classList.contains("mantine-Loader-root"),
      ),
    ).toBeInTheDocument();
  });

  it("applies custom text class name", () => {
    testRender(
      <EditableTableField {...defaultProps} textClassName="custom-class" />,
    );
    expect(screen.getByTestId("truncated-text")).toHaveAttribute(
      "class",
      "custom-class",
    );
  });

  describe("with playground context", () => {
    beforeEach(() => {
      (usePlaygroundContext as jest.Mock).mockReturnValue({
        setPairId: mockSetPairId,
        setHumanEvaluator: mockSetHumanEvaluator,
        setSelectedChatTurnIds: mockSetSelectedChatTurnIds,
        setSelectedChatIds: mockSetSelectedChatIds,
      });
    });

    it("shows playground button on hover", async () => {
      testRender(
        <EditableTableField
          {...defaultProps}
          hasHoverPlaygroundButton={true}
          rowOriginal={{ id: "test-id" }}
        />,
      );

      const group = screen.getByTestId("truncated-text").parentElement;
      if (group) {
        fireEvent.mouseEnter(group);
      }

      expect(screen.getByText("Open")).toBeInTheDocument();
    });

    it("opens playground with chat IDs when button clicked", () => {
      testRender(
        <EditableTableField
          {...defaultProps}
          hasHoverPlaygroundButton={true}
          rowOriginal={{
            id: "pair-id",
            chat_id: "chat-id",
            chat_id_b: "chat-id-b",
          }}
        />,
      );

      const group = screen.getByTestId("truncated-text").parentElement;
      if (group) {
        fireEvent.mouseEnter(group);
      }

      const openButton = screen.getByText("Open").parentElement;
      if (openButton) {
        fireEvent.click(openButton);
      }

      expect(mockSetPairId).toHaveBeenCalledWith("pair-id");
      expect(mockSetSelectedChatIds).toHaveBeenCalledWith([
        "chat-id",
        "chat-id-b",
      ]);
      expect(mockPush).toHaveBeenCalledWith(
        "/projects/mock-project-id/playground",
      );
    });

    it("handles sub-rows with chat turn IDs", () => {
      testRender(
        <EditableTableField
          {...defaultProps}
          hasHoverPlaygroundButton={true}
          rowOriginal={{
            id: "pair-id",
            isSubRow: true,
            isTheLastOne: false,
            chat_turn_id: "turn-id-a",
            chat_turn_b: { chat_turn_id: "turn-id-b" },
          }}
        />,
      );

      const group = screen.getByTestId("truncated-text").parentElement;
      if (group) {
        fireEvent.mouseEnter(group);
      }

      const openButton = screen.getByText("Open").parentElement;
      if (openButton) {
        fireEvent.click(openButton);
      }

      expect(mockSetSelectedChatTurnIds).toHaveBeenCalledWith([
        "turn-id-a",
        "turn-id-b",
      ]);
    });

    it("sets human evaluator data", () => {
      testRender(
        <EditableTableField
          {...defaultProps}
          hasHoverPlaygroundButton={true}
          rowOriginal={{
            id: "pair-id",
            human_sxs_rating: 5,
            human_sxs_notes: "Test notes",
          }}
        />,
      );

      const group = screen.getByTestId("truncated-text").parentElement;
      if (group) {
        fireEvent.mouseEnter(group);
      }

      const openButton = screen.getByText("Open").parentElement;
      if (openButton) {
        fireEvent.click(openButton);
      }

      expect(mockSetHumanEvaluator).toHaveBeenCalledWith({
        value: 5,
        notes: "Test notes",
      });
    });
  });

  describe("with isVariableColumn", () => {
    it("renders variable column with JSON data", () => {
      const variables = { var1: "value1", var2: "value2" };

      testRender(
        <EditableTableField
          {...defaultProps}
          value={JSON.stringify(variables)}
          isVariableColumn={true}
        />,
      );

      expect(screen.getByTestId("truncated-text")).toHaveAttribute(
        "data-json",
        "true",
      );
      expect(screen.getByTestId("truncated-text")).toHaveTextContent(
        JSON.stringify(variables),
      );
    });

    it("opens variables modal when clicked", () => {
      const variables = { var1: "value1", var2: "value2" };

      testRender(
        <EditableTableField
          {...defaultProps}
          value={JSON.stringify(variables)}
          isVariableColumn={true}
        />,
      );

      const variablesElement =
        screen.getByTestId("truncated-text").parentElement;
      if (variablesElement) {
        fireEvent.click(variablesElement);
      }

      expect(screen.getByTestId("variables-modal")).toBeInTheDocument();
    });

    it("updates variables when modal updates them", () => {
      const onUpdate = jest.fn();
      const variables = { var1: "value1", var2: "value2" };

      testRender(
        <EditableTableField
          {...defaultProps}
          value={JSON.stringify(variables)}
          isVariableColumn={true}
          onUpdate={onUpdate}
        />,
      );

      const variablesElement =
        screen.getByTestId("truncated-text").parentElement;
      if (variablesElement) {
        fireEvent.click(variablesElement);
      }

      const updateButton = screen.getByTestId("update-variables");
      fireEvent.click(updateButton);

      expect(onUpdate).toHaveBeenCalledWith(
        JSON.stringify({ newVar: "updated" }),
      );
    });

    it("closes variables modal properly", () => {
      const variables = { var1: "value1", var2: "value2" };

      testRender(
        <EditableTableField
          {...defaultProps}
          value={JSON.stringify(variables)}
          isVariableColumn={true}
        />,
      );

      const variablesElement =
        screen.getByTestId("truncated-text").parentElement;
      if (variablesElement) {
        fireEvent.click(variablesElement);
      }

      const closeButton = screen.getByTestId("close-modal");
      fireEvent.click(closeButton);

      expect(screen.queryByTestId("variables-modal")).not.toBeInTheDocument();
    });

    it("handles invalid JSON gracefully", () => {
      testRender(
        <EditableTableField
          {...defaultProps}
          value="invalid json"
          isVariableColumn={true}
        />,
      );

      expect(screen.getByTestId("truncated-text")).toHaveTextContent(
        "invalid json",
      );

      const variablesElement =
        screen.getByTestId("truncated-text").parentElement;
      if (variablesElement) {
        fireEvent.click(variablesElement);
      }

      expect(screen.getByText("Variables: {}")).toBeInTheDocument();
    });

    it("shows placeholder when no variables are set", () => {
      testRender(
        <EditableTableField
          {...defaultProps}
          value=""
          placeholderText="Add variables..."
          isVariableColumn={true}
        />,
      );

      expect(screen.getByText("Add variables...")).toBeInTheDocument();
    });
  });

  it("opens empty text field when clicking on empty cell", () => {
    const onUpdate = jest.fn();
    testRender(
      <EditableTableField
        {...defaultProps}
        value=""
        onUpdate={onUpdate}
        isEnabled={true}
      />,
    );

    const emptyCell = screen.getByText("Type something...").parentElement;
    if (emptyCell) {
      fireEvent.click(emptyCell);
    }

    expect(screen.getByRole("textbox")).toBeInTheDocument();
  });

  it("prevents event propagation when clicking on variables modal button", () => {
    const mockEvent = {
      stopPropagation: jest.fn(),
      preventDefault: jest.fn(),
    };

    testRender(
      <EditableTableField
        {...defaultProps}
        isVariableColumn={true}
        allowEdit={true}
      />,
    );

    const variablesElement = screen.getByTestId("truncated-text").parentElement;
    if (variablesElement) {
      fireEvent.click(variablesElement, mockEvent);
      expect(screen.getByTestId("variables-modal")).toBeInTheDocument();
    }
  });

  it("handles alternate data formats in rowOriginal", () => {
    (usePlaygroundContext as jest.Mock).mockReturnValue({
      setPairId: mockSetPairId,
      setHumanEvaluator: mockSetHumanEvaluator,
      setSelectedChatTurnIds: mockSetSelectedChatTurnIds,
      setSelectedChatIds: mockSetSelectedChatIds,
    });

    testRender(
      <EditableTableField
        {...defaultProps}
        hasHoverPlaygroundButton={true}
        rowOriginal={{
          chat_turn_a: { chat_id: "chat-id", chat_turn_id: "turn-id" },
          human_eval_scores: [{ score: 4, notes: "Alternative notes" }],
        }}
      />,
    );

    const group = screen.getByTestId("truncated-text").parentElement;
    if (group) {
      fireEvent.mouseEnter(group);
    }

    const openButton = screen.getByText("Open").parentElement;
    if (openButton) {
      fireEvent.click(openButton);
    }

    expect(mockSetHumanEvaluator).toHaveBeenCalledWith({
      value: 4,
      notes: "Alternative notes",
    });
  });
});

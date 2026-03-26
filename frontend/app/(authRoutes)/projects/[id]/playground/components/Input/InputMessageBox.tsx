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

import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import Card from "@/components/Card";
import CustomSelectOption from "@/components/CustomSelectOption";
import MaterialIcon from "@/components/MaterialIcon";
import { MainConfig } from "@/config/config";
import { PLAYGROUND_CUSTOM_INPUT_NAME } from "@/config/constants";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { InferenceChatCompletionPromptRole } from "@/types";
import { handleCopy } from "@/utils/helpers";
import { EditorSelection } from "@codemirror/state";
import { EditorView, ViewUpdate } from "@codemirror/view";
import {
  ComboboxItem,
  ComboboxLikeRenderOptionInput,
  Divider,
  Group,
  Menu,
  ScrollArea,
  Select,
  Text,
  Tooltip,
  UnstyledButton,
} from "@mantine/core";
import CodeMirror, { ReactCodeMirrorRef } from "@uiw/react-codemirror";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

import { ROLE_OPTIONS_MAP } from "../../consts";
import { InputMessageBoxProps, InputsType } from "../../types";
import {
  getEditorView,
  useDebouncedEffect,
  variablesHighlighter,
} from "../../utils";
import InputMessageBoxAttachment from "./InputMessageBoxAttachment";

export default function InputMessageBox({
  id,
  role,
  text = "",
  isSystemInstruction,
  onInstructionsChange,
  filteredInputs,
  onDeleteInput,
  attachment,
  isExpandCard,
  isClosing = false,
  onCollapse = null,
}: InputMessageBoxProps) {
  const isAttachmentEnabled = MainConfig.isPlaygroundAttachmentEnabled;
  const { isSideBySide } = useProjectContext();
  const {
    variables,
    expandedInputCard,
    setExpandedInputCard,
    setInputs,
    isPlaygroundModified,
    setIsPlaygroundModified,
    setIsNewChat,
    isNewChat,
  } = usePlaygroundContext();
  const editorRef = useRef<ReactCodeMirrorRef>(null);
  const [variablesOptions, setVariablesOptions] = useState<string[]>([]);
  const [filteredVariablesOptions, setFilteredVariablesOptions] = useState<
    string[]
  >([]);
  const [highlightedIndex, setHighlightedIndex] = useState(0);
  const [showVariablesOptions, setShowVariablesOptions] = useState(false);
  const [dropdownPosition, setDropdownPosition] = useState({ top: 0, left: 0 });
  const [editorValue, setEditorValue] = useState(text);
  const [cursorPosition, setCursorPosition] = useState(0);
  const debounceOnChangeInputTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const debouncedOnChangeInput = useCallback((newInput: InputsType) => {
    if (debounceOnChangeInputTimeoutRef.current) {
      clearTimeout(debounceOnChangeInputTimeoutRef.current);
    }

    debounceOnChangeInputTimeoutRef.current = setTimeout(() => {
      onChangeInput(newInput);
    }, 100);
  }, []);

  const debounceOnChangeInstructionsTimeoutRef = useRef<NodeJS.Timeout | null>(
    null,
  );
  const debouncedOnChangeInstructions = useCallback((newInput: string) => {
    if (debounceOnChangeInstructionsTimeoutRef.current) {
      clearTimeout(debounceOnChangeInstructionsTimeoutRef.current);
    }

    debounceOnChangeInstructionsTimeoutRef.current = setTimeout(() => {
      onInstructionsChange?.(newInput);
    }, 100);
  }, []);

  const onChange = (newInput: InputsType) => {
    // Do not change the state if it's already true
    if (!isPlaygroundModified) {
      setIsPlaygroundModified(true);
    }

    if (onInstructionsChange) {
      // Do not change the state if it's already true
      if (!isNewChat) {
        setIsNewChat(true);
      }

      debouncedOnChangeInstructions(newInput.text || "");
    } else {
      // If original user input is changed set new chat
      // Do not change the state if it's already true
      if (!id.includes(PLAYGROUND_CUSTOM_INPUT_NAME) && !isNewChat) {
        setIsNewChat(true);
      }

      debouncedOnChangeInput(newInput);
    }
  };

  const isAssistantDisabled = useMemo(() => {
    const currentInputIndex = filteredInputs?.findIndex((i) => i.id === id);
    if (!currentInputIndex) {
      return true;
    }

    const filteredInputsLength = filteredInputs?.length ?? 0;
    const nextInputHaveAssistant =
      filteredInputs?.[currentInputIndex + 1]?.role ===
      InferenceChatCompletionPromptRole.ASSISTANT;

    const prevInputHaveAssistant =
      filteredInputs?.[currentInputIndex - 1]?.role ===
      InferenceChatCompletionPromptRole.ASSISTANT;

    if (
      currentInputIndex === 0 || // Disable assistant for the first input
      currentInputIndex === filteredInputsLength - 1 || // Disable assistant for the last input
      prevInputHaveAssistant || // Disable assistant if the previous input is assistant
      nextInputHaveAssistant // Disable assistant if the next input is assistant
    ) {
      return true;
    }

    return false;
  }, [filteredInputs]);

  const handleRoleChange = useCallback(
    (value: string | null) => {
      if (
        !value ||
        (value?.toUpperCase() === InferenceChatCompletionPromptRole.ASSISTANT &&
          isAssistantDisabled)
      ) {
        return;
      }

      setIsNewChat(true);
      onChange({
        id,
        role: value?.toUpperCase() as InferenceChatCompletionPromptRole,
      });
    },
    [id, onChange, isAssistantDisabled],
  );

  const onChangeInput = (newInput: InputsType) => {
    setInputs((prevState) =>
      prevState.map((input) => {
        if (input.id === newInput.id) {
          input.role = newInput.role ?? input.role;
          input.text = newInput.text ?? input.text;
        }

        return input;
      }),
    );
  };

  useEffect(() => {
    if (variables) {
      const options = Object.keys(variables);
      setVariablesOptions(options);
      setFilteredVariablesOptions(options);
    }
  }, [variables]);

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (debounceOnChangeInputTimeoutRef.current) {
        clearTimeout(debounceOnChangeInputTimeoutRef.current);
      }
      if (debounceOnChangeInstructionsTimeoutRef.current) {
        clearTimeout(debounceOnChangeInstructionsTimeoutRef.current);
      }
    };
  }, []);

  const handleEditorChange = useCallback(
    (value: string, viewUpdate: ViewUpdate) => {
      setEditorValue(value);
      onChange({ id, text: value, role });
      setCursorPosition(viewUpdate.state.selection.main.head);
    },
    [id, onChange, role],
  );

  useDebouncedEffect(
    () => {
      const before = editorValue.slice(0, cursorPosition);
      const match = before.match(/{{([^\s}]*)$/);
      if (match) {
        const filterText = match[1];
        setShowVariablesOptions(true);
        const filtered = variablesOptions.filter((opt) =>
          opt.toLowerCase().includes(filterText.toLowerCase()),
        );
        setFilteredVariablesOptions(filtered);
        setHighlightedIndex(0);
        const coords = editorRef.current?.view?.coordsAtPos(cursorPosition);
        if (coords) {
          setDropdownPosition({ top: coords.bottom, left: coords.left + 5 });
        }
      } else {
        setShowVariablesOptions(false);
      }
    },
    [editorValue, cursorPosition],
    200,
  );

  useEffect(() => {
    const editorView = editorRef.current?.view?.dom;
    if (!editorView) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (!showVariablesOptions || filteredVariablesOptions.length === 0)
        return;

      if (e.key === "ArrowDown") {
        e.preventDefault();
        setHighlightedIndex((prev) =>
          Math.min(prev + 1, filteredVariablesOptions.length - 1),
        );
      } else if (e.key === "ArrowUp") {
        e.preventDefault();
        setHighlightedIndex((prev) => Math.max(prev - 1, 0));
      } else if (e.key === "Enter" || e.key === "Tab") {
        const selected = filteredVariablesOptions[highlightedIndex];
        if (selected) {
          e.preventDefault();
          insertVariables(selected);
        }
      }
    };

    editorView.addEventListener("keydown", handleKeyDown);

    return () => editorView.removeEventListener("keydown", handleKeyDown);
  }, [filteredVariablesOptions, highlightedIndex, showVariablesOptions]);

  const insertVariables = (optionKey: string) => {
    const view = editorRef.current?.view;
    if (!view) return;
    const state = view.state;
    const pos = state.selection.main.head;
    const before = text.slice(0, pos);
    const match = before.match(/{{([^\s}]*)$/);
    if (!match) return;
    const start = pos - match[0].length;
    const insertText = `{{${optionKey}}}`;
    view.dispatch({
      changes: { from: start, to: pos, insert: insertText },
      selection: EditorSelection.cursor(start + insertText.length),
    });
    view.focus();
    setShowVariablesOptions(false);
  };

  const isExpanded = useMemo(
    () => expandedInputCard?.id === id,
    [expandedInputCard, id],
  );

  const cardClassNames = useMemo(() => {
    let defaultClassNames =
      "border-borderColor rounded-lg p-3 pb-2 shadow-none overflow-hidden transition-all duration-1000 ease-in-out ";

    if (isExpanded && isExpandCard && !isClosing) {
      defaultClassNames += "w-full h-[calc(100vh-100px)]";
    } else {
      defaultClassNames += "w-full !mt-3 ";

      if (
        role === InferenceChatCompletionPromptRole.USER &&
        isAttachmentEnabled
      ) {
        defaultClassNames += "h-[154px]";
      } else {
        defaultClassNames += "h-[125px]";
      }
    }

    return defaultClassNames;
  }, [isExpanded, role, isAttachmentEnabled, isExpandCard, isClosing]);

  const isDeleteButtonDisabled = useMemo(() => {
    const currentInputIndex = filteredInputs?.findIndex((i) => i.id === id);
    if (currentInputIndex === undefined) {
      return true;
    }

    const visibleInputs = filteredInputs?.filter((i) => !i.hidden);
    const nextInputHaveAssistant =
      filteredInputs?.[currentInputIndex + 1]?.role ===
      InferenceChatCompletionPromptRole.ASSISTANT;

    return (
      (currentInputIndex === 0 && nextInputHaveAssistant) ||
      (currentInputIndex === 0 && visibleInputs?.length === 1)
    );
  }, [filteredInputs]);

  const RoleOption = useCallback(
    ({ option }: { option: ComboboxLikeRenderOptionInput<ComboboxItem> }) => {
      const isOptionDisabled =
        option.option.label.toUpperCase() ===
          InferenceChatCompletionPromptRole.ASSISTANT && isAssistantDisabled;

      if (isOptionDisabled) {
        return (
          <Tooltip
            label="You can't start and end conversation with Assistant or have 2 assistants in a row"
            position="top"
          >
            <Group gap={4} className="w-full !cursor-not-allowed">
              <MaterialIcon
                name="check"
                className={`!text-[16px] ${!option.checked && "text-transparent"}`}
              />
              <Text className="text-disabled text-title-12">
                {option.option.label}
              </Text>
            </Group>
          </Tooltip>
        );
      }

      return <CustomSelectOption option={option} />;
    },
    [isAssistantDisabled],
  );

  const handleExpandCollapse = () => {
    if (isExpanded && !isClosing) {
      if (onCollapse) {
        onCollapse();
      } else {
        setExpandedInputCard(undefined);
      }
    } else if (!isExpanded) {
      setExpandedInputCard({ id, role, text });
    }
  };

  return (
    <Card className={cardClassNames}>
      <Group className="w-full justify-between">
        <Group className="min-w-[160px]">
          {(isExpandCard && isExpanded) || isSystemInstruction ? (
            <Text className="w-[160px] h-[20px] p-1 text-title-12">
              {ROLE_OPTIONS_MAP[role].label}
            </Text>
          ) : (
            <Select
              data={Object.values(ROLE_OPTIONS_MAP).filter(
                (o) => o.value !== "System",
              )}
              value={ROLE_OPTIONS_MAP[role].value}
              renderOption={(opt) => <RoleOption option={opt} />}
              onChange={handleRoleChange}
              classNames={{
                input:
                  "w-[160px] h-[36px] border border-neutrals-300 rounded-sm text-title-12",
                option: "px-1",
              }}
              rightSection={
                <MaterialIcon
                  name="keyboard_arrow_down"
                  className="cursor-pointer !font-light"
                  dataTestId="dropdown-trigger"
                />
              }
            />
          )}
        </Group>
        <Group className="self-start text-secondary">
          {onDeleteInput && (
            <MaterialIcon
              name={isSideBySide ? "delete_sweep" : "delete"}
              size={24}
              tooltipClassName="max-w-[200px]"
              tooltipLabel={
                isDeleteButtonDisabled
                  ? ""
                  : isSideBySide
                    ? "Delete both"
                    : "Delete row"
              }
              disabled={isDeleteButtonDisabled}
              onClick={() => {
                if (isDeleteButtonDisabled) return;

                setExpandedInputCard(undefined);
                id && onDeleteInput && onDeleteInput(id);
              }}
            />
          )}

          <MaterialIcon
            name="content_copy"
            className={
              !text ? "!cursor-default text-disabled" : "cursor-pointer"
            }
            size={24}
            onClick={() => !!text && handleCopy(text)}
            tooltipLabel="Copy"
            dataTestId="copy-button"
          />
          <MaterialIcon
            name={!isExpanded ? "expand_content" : "collapse_content"}
            className="cursor-pointer text-secondary"
            size={24}
            onClick={handleExpandCollapse}
            tooltipLabel={!isExpanded ? "Expand" : "Collapse"}
            dataTestId="expand-button"
          />
        </Group>
      </Group>
      <ScrollArea
        scrollbarSize={10}
        type="auto"
        className={`w-full mt-2 ${isExpanded ? "h-[calc(100%-80px)]" : isSystemInstruction ? "h-[66px]" : "h-[40px]"}`}
      >
        <CodeMirror
          ref={editorRef}
          value={text}
          placeholder={ROLE_OPTIONS_MAP[role].placehodler}
          extensions={[
            variablesHighlighter,
            EditorView.lineWrapping,
            EditorView.theme(getEditorView()),
          ]}
          onChange={handleEditorChange}
          basicSetup={false}
          theme="none"
          className="border-none !text-body-14"
        />
        {showVariablesOptions && filteredVariablesOptions.length > 0 && (
          <Menu
            opened={true}
            closeOnClickOutside={false}
            closeOnEscape={false}
            trapFocus={false}
            classNames={{
              dropdown:
                "max-w-[220px] min-w-[100px] max-h-[300px] overflow-y-auto p-0 flex justify-start flex-col rounded-sm gap-0 fixed z-[9999]",
            }}
            styles={{
              dropdown: {
                top: dropdownPosition.top,
                left: dropdownPosition.left,
              },
            }}
          >
            <Menu.Target>
              <span />
            </Menu.Target>
            <Menu.Dropdown>
              <Text className="text-code-14 p-2 text-no-wrap">
                Imported variables
              </Text>
              <Divider />
              {filteredVariablesOptions.map((opt, index) => (
                <UnstyledButton
                  key={opt}
                  className={`p-2 text-code-14 pl-5 ${
                    index === highlightedIndex ? "bg-lightSilver" : ""
                  }`}
                  onMouseDown={(e) => e.preventDefault()}
                  onClick={() => insertVariables(opt)}
                >
                  {opt}
                </UnstyledButton>
              ))}
            </Menu.Dropdown>
          </Menu>
        )}
      </ScrollArea>

      {isAttachmentEnabled && (
        <InputMessageBoxAttachment
          id={id}
          role={role}
          attachment={attachment}
        />
      )}
    </Card>
  );
}

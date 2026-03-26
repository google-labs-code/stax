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

import ManageVariablesModal from "@/app/(authRoutes)/projects/[id]/playground/components/ManageVariablesModal";
import { routes } from "@/config/routes";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import {
  Group,
  Loader,
  Popover,
  PopoverDropdown,
  PopoverTarget,
  Text,
  Textarea,
  Tooltip,
  UnstyledButton,
} from "@mantine/core";
import { useParams, useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";

import MaterialIcon from "./MaterialIcon";
import TruncatedTextWithPopover from "./TruncatedTextWithPopover";

type EditableTableFieldProps = {
  value: string;
  placeholderText: string;
  onUpdate: (value: string) => void;
  isEnabled?: boolean;
  textClassName?: string;
  isLoading?: boolean;
  allowEdit?: boolean;
  hasHoverPlaygroundButton?: boolean;
  rowOriginal?: any;
  isVariableColumn?: boolean;
};

export default function EditableTableField({
  value,
  placeholderText,
  onUpdate,
  isEnabled = true,
  textClassName,
  isLoading,
  allowEdit = false,
  hasHoverPlaygroundButton = false,
  rowOriginal,
  isVariableColumn = false,
}: EditableTableFieldProps) {
  const playgroundContext = usePlaygroundContext();

  const chatId = rowOriginal?.chat_id || rowOriginal?.chat_turn_a?.chat_id;
  const chatTurnId =
    rowOriginal?.chat_turn_id || rowOriginal?.chat_turn_a?.chat_turn_id;
  const isSubRow = rowOriginal?.isSubRow;
  const isTheLastOne = rowOriginal?.isTheLastOne;

  const chatIdB = rowOriginal?.chat_turn_b?.chat_id || rowOriginal?.chat_id_b;
  const chatTurnIdB = rowOriginal?.chat_turn_b?.chat_turn_id;
  const pairId = rowOriginal?.pairId ?? rowOriginal?.id;

  const [isOpenDropdown, setOpenDropdown] = useState(false);
  const [localValue, setLocalValue] = useState("");
  const [showPopoverText, setShowPopoverText] = useState(true);
  const [isHovering, setIsHovering] = useState(false);
  const [isEditing, setIsEditing] = useState(false);

  const [isVariablesModalOpen, setVariablesModalOpen] = useState(false);
  const [localVariables, setLocalVariables] = useState<Record<string, string>>(
    {},
  );

  const router = useRouter();
  const params = useParams();

  const shouldShowPlaygroundButton =
    hasHoverPlaygroundButton && !!playgroundContext;

  useEffect(() => {
    if (isOpenDropdown) {
      setLocalValue(value);
    }
  }, [isOpenDropdown, value]);

  useEffect(() => {
    if (isVariableColumn && value) {
      try {
        const parsed = JSON.parse(value);
        setLocalVariables(parsed);
      } catch {
        setLocalVariables({});
      }
    }
  }, [value, isVariableColumn]);

  const onOpenPlayground = useCallback(() => {
    if (!playgroundContext) return;

    playgroundContext.setPairId(pairId);
    playgroundContext.setHumanEvaluator({
      value:
        rowOriginal?.human_sxs_rating ||
        rowOriginal?.human_eval_scores?.[0]?.score ||
        null,
      notes:
        rowOriginal?.human_sxs_notes ||
        rowOriginal?.human_eval_scores?.[0]?.notes ||
        null,
    });
    if (isSubRow && !isTheLastOne) {
      playgroundContext.setSelectedChatTurnIds([
        chatTurnId ?? null,
        chatTurnIdB ?? null,
      ]);
    }
    if (chatId) {
      playgroundContext.setSelectedChatIds([chatId, chatIdB]);
    }

    if (params?.id) {
      router.push(`${routes.projects}/${params?.id}/playground`);
    }
  }, [
    params,
    router,
    chatId,
    chatIdB,
    chatTurnId,
    chatTurnIdB,
    isTheLastOne,
    isSubRow,
    pairId,
    playgroundContext,
    rowOriginal,
  ]);

  const handleOpenVariablesModal = (e: React.MouseEvent) => {
    e.stopPropagation();
    e.preventDefault();

    setOpenDropdown(false);
    setIsEditing(false);

    setVariablesModalOpen(true);
  };

  const handleVariablesUpdate = (updatedVariables: Record<string, string>) => {
    const variablesString = JSON.stringify(updatedVariables);
    onUpdate(variablesString);
    setLocalVariables(updatedVariables);
  };

  if (isVariableColumn) {
    return (
      <>
        <Group
          onMouseEnter={() => {
            if (shouldShowPlaygroundButton) {
              setIsHovering(true);
            }
          }}
          onMouseLeave={() => {
            if (shouldShowPlaygroundButton) {
              setIsHovering(false);
            }
          }}
          onDoubleClick={allowEdit ? handleOpenVariablesModal : undefined}
          onClick={allowEdit ? handleOpenVariablesModal : undefined}
          className="flex w-full flex-row flex-nowrap justify-between gap-0 cursor-pointer"
        >
          {value ? (
            <div className="w-full">
              <TruncatedTextWithPopover
                text={String(value)}
                popoverWidth={300}
                additionalClassName={textClassName}
                isJsonFormat={true}
              />
            </div>
          ) : (
            <div className="w-full text-resting text-body-12">
              {placeholderText}
            </div>
          )}

          {isHovering && playgroundContext && (
            <Tooltip label="Open in playground">
              <UnstyledButton
                onClick={(e) => {
                  e.stopPropagation();
                  onOpenPlayground();
                }}
                className="border-default gap-sm absolute right-[8px] flex flex-row items-center rounded-sm bg-white px-[8px] py-[2px] text-secondary shadow-sm"
              >
                <Text className="!font-medium text-secondary text-body-12">
                  Open
                </Text>
                <MaterialIcon
                  name="open_in_new"
                  size={18}
                  className="text-secondary"
                />
              </UnstyledButton>
            </Tooltip>
          )}
        </Group>

        <ManageVariablesModal
          isOpen={isVariablesModalOpen}
          onClose={() => setVariablesModalOpen(false)}
          variables={localVariables}
          onUpdate={handleVariablesUpdate}
          chatIds={[chatId].filter(Boolean)}
        />
      </>
    );
  }

  return (
    <Popover
      opened={isOpenDropdown}
      onChange={setOpenDropdown}
      position="bottom-start"
      offset={-40}
      floatingStrategy="absolute"
      closeOnClickOutside
      withinPortal
      classNames={{
        dropdown: "p-0 ml-[-12px] !mt-[8px] !w-[240px] !min-h-[43px] border-0",
      }}
    >
      <PopoverTarget>
        {isLoading ? (
          <Loader size="xs" />
        ) : (
          <Group
            onMouseEnter={() => {
              if (shouldShowPlaygroundButton && !isEditing) {
                setOpenDropdown(false);
                setIsHovering(true);
              }
            }}
            onMouseLeave={() => {
              if (shouldShowPlaygroundButton) {
                setIsHovering(false);
              }
            }}
            onClick={() => {
              if (!value && isEnabled) {
                setOpenDropdown((o) => !o);
                setShowPopoverText(true);
              }
            }}
            onDoubleClick={() => {
              if (allowEdit) {
                setIsHovering(false);
                setIsEditing(true);
                setOpenDropdown(true);
                setLocalValue(value);
                setShowPopoverText(false);
              }
            }}
            className="flex w-full cursor-pointer flex-row flex-nowrap justify-between gap-0"
          >
            {value && showPopoverText ? (
              <TruncatedTextWithPopover
                text={String(value)}
                popoverWidth={300}
                additionalClassName={textClassName}
                isJsonFormat={false}
              />
            ) : (
              <div className="flex justify-center text-resting text-body-12">
                {placeholderText}
              </div>
            )}
            {isHovering && playgroundContext && (
              <Tooltip label="Open in playground">
                <UnstyledButton
                  onClick={(e) => {
                    e.stopPropagation();
                    onOpenPlayground();
                  }}
                  className="border-default gap-sm absolute right-[8px] flex flex-row items-center rounded-sm bg-white px-[8px] py-[2px] text-secondary shadow-sm"
                >
                  <Text className="!font-medium text-secondary text-body-12">
                    Open
                  </Text>
                  <MaterialIcon
                    name="open_in_new"
                    size={18}
                    className="text-secondary"
                  />
                </UnstyledButton>
              </Tooltip>
            )}
          </Group>
        )}
      </PopoverTarget>

      <PopoverDropdown>
        <Textarea
          classNames={{
            input:
              "focus:!border-borderColor max-h-[466px] shadow-lg rounded-sm !border-default !min-h-[43px] !py-[12px] text-body-12 !font-normal",
          }}
          autoFocus
          resize="vertical"
          autosize
          minRows={1}
          maxRows={22}
          value={localValue}
          onChange={(e) => setLocalValue(e.target.value)}
          onBlur={(e) => {
            e.preventDefault();
            onUpdate?.(e.currentTarget.value);
            setOpenDropdown(false);
            setShowPopoverText(true);
            setIsEditing(false);
          }}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.nativeEvent.shiftKey) {
              e.preventDefault();
              onUpdate?.(e.currentTarget.value);
              setOpenDropdown(false);
              setShowPopoverText(true);
              setIsEditing(false);
            }
          }}
        />
      </PopoverDropdown>
    </Popover>
  );
}

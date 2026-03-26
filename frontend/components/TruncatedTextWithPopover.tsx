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

import GetChatMessageWithMarkup from "@/utils/GetChatMessageWithMarkup";
import { formatJsonString } from "@/utils/jsonUtils";
import { ReplaceVariablesValueMarkupWithValue } from "@/utils/textMarkup";
import { Popover, ScrollArea, Text } from "@mantine/core";
import { useEffect, useRef, useState } from "react";

import JsonCodeMirror from "./JsonCodeMirror";

interface TruncatedTextWithPopoverProps {
  text: string;
  maxHeight?: string;
  additionalClassName?: string;
  popoverWidth?: number;
  popoverMaxHeight?: number;
  isJsonFormat?: boolean;
}

export default function TruncatedTextWithPopover({
  text,
  maxHeight = "max-h-[24px]",
  additionalClassName = "text-body-12",
  popoverWidth = 400,
  popoverMaxHeight = 500,
  isJsonFormat = false,
}: TruncatedTextWithPopoverProps) {
  const [opened, setOpened] = useState(false);
  const contentRef = useRef<HTMLDivElement>(null);
  const popoverContentRef = useRef<HTMLDivElement>(null);
  const [dynamicMaxHeight, setDynamicMaxHeight] = useState(popoverMaxHeight);

  if (!text) return null;

  let formattedText;
  let popoverText;

  if (isJsonFormat) {
    try {
      const parsed = JSON.parse(text);
      formattedText = JSON.stringify(parsed);
      popoverText = formatJsonString(text);
    } catch {
      formattedText = text;
      popoverText = text;
    }
  } else {
    formattedText = ReplaceVariablesValueMarkupWithValue(text);
    popoverText = text;
  }

  const calculateMaxHeight = () => {
    if (contentRef.current) {
      const rect = contentRef.current.getBoundingClientRect();
      const viewportHeight = window.innerHeight;

      const spaceBelow = viewportHeight - rect.bottom - 50;
      const spaceAbove = rect.top - 50;

      const availableSpace = Math.max(spaceBelow, spaceAbove);
      setDynamicMaxHeight(Math.min(availableSpace, popoverMaxHeight));
    }
  };

  useEffect(() => {
    const handleScroll = (event: Event) => {
      if (
        popoverContentRef.current &&
        popoverContentRef.current.contains(event.target as Node)
      ) {
        return;
      }
      setOpened(false);
    };
    window.addEventListener("scroll", handleScroll, true);

    return () => {
      window.removeEventListener("scroll", handleScroll, true);
    };
  }, [setOpened]);

  useEffect(() => {
    const checkForOpenModal = () => {
      const modalOverlays = document.querySelectorAll(".mantine-Modal-overlay");
      if (modalOverlays.length > 0) {
        setOpened(false);
      }
    };

    const observer = new MutationObserver(() => {
      checkForOpenModal();
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ["class"],
    });

    const handleClick = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (
        target.closest(".mantine-Modal-root") &&
        !contentRef.current?.contains(target)
      ) {
        setOpened(false);
      }
    };

    document.addEventListener("mousedown", handleClick);

    return () => {
      observer.disconnect();
      document.removeEventListener("mousedown", handleClick);
    };
  }, []);

  const handleOpenPopover = (e: React.MouseEvent) => {
    e.stopPropagation();
    calculateMaxHeight();
    setOpened(true);
  };

  const handlePopoverClick = (e: React.MouseEvent) => {
    e.stopPropagation();
  };

  return (
    <Popover
      opened={opened}
      onChange={(value) => setOpened(value)}
      position="bottom-start"
      withArrow={false}
      shadow="md"
      closeOnClickOutside
      closeOnEscape
      middlewares={{ flip: true, shift: true }}
      offset={{ mainAxis: 5, crossAxis: 0 }}
      classNames={{
        dropdown: "rounded-md border-default shadow-md",
      }}
    >
      <Popover.Target>
        <div
          ref={contentRef}
          onClick={handleOpenPopover}
          className={` ${maxHeight} w-full cursor-pointer overflow-hidden hover:text-brand break-all`}
        >
          <Text
            className={`${additionalClassName} line-clamp-1 truncate text-secondary text-body-12`}
          >
            {formattedText || " "}
          </Text>
        </div>
      </Popover.Target>

      <Popover.Dropdown className="rounded-lg px-[15px] py-[15px]">
        <div
          className={`${additionalClassName} h-auto max-w-[900px] resize overflow-auto`}
          ref={popoverContentRef}
          onClick={handlePopoverClick}
          style={{
            width: popoverWidth,
            maxHeight: dynamicMaxHeight,
          }}
        >
          <ScrollArea.Autosize
            offsetScrollbars
            scrollbarSize="5px"
            classNames={{
              viewport: "p-0",
            }}
            mah={dynamicMaxHeight}
          >
            {isJsonFormat ? (
              <JsonCodeMirror
                value={popoverText}
                readOnly={true}
                className="w-full focus:!border-borderColor rounded-sm font-mono"
              />
            ) : (
              <GetChatMessageWithMarkup
                message={text}
                elementClassName={additionalClassName}
              />
            )}
          </ScrollArea.Autosize>
        </div>
      </Popover.Dropdown>
    </Popover>
  );
}

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

import MaterialIcon from "@/components/MaterialIcon";
import { GetFormattedChildrenMarkup } from "@/utils/textMarkup";
import { Group, Text, UnstyledButton } from "@mantine/core";
import { memo } from "react";
import ReactMarkdown from "react-markdown";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";

const GetChatMessageWithMarkup = memo(function GetChatMessageWithMarkup({
  message,
  elementClassName,
  codeGroupClassName,
}: {
  message: string;
  elementClassName?: string;
  codeGroupClassName?: string;
}) {
  const orangeColor = "var(--color-orange)";
  const blueColor = "var(--color-blue)";
  const blackColor = "var(--color-black)";
  const planckStyleTheme = {
    'code[class*="language-"]': {
      color: blackColor,
      background: "none",
      fontFamily: "Google Sans Mono",
      whiteSpace: "pre-line",
    },
    comment: {
      color: "var(--color-grey)",
    },
    prolog: {
      color: orangeColor,
    },
    doctype: {
      color: orangeColor,
    },
    cdata: {
      color: orangeColor,
    },
    punctuation: {
      color: orangeColor,
    },
    property: {
      color: "var(--color-orange)",
    },
    tag: {
      color: orangeColor,
    },
    constant: {
      color: blackColor,
    },
    symbol: {
      color: blackColor,
    },
    deleted: {
      color: blackColor,
    },
    boolean: {
      color: blackColor,
    },
    number: {
      color: blackColor,
    },
    string: {
      color: blackColor,
    },
    url: {
      color: blueColor,
    },
    variable: {
      color: blackColor,
    },
    function: {
      color: "var(--color-purple)",
    },
    keyword: {
      color: orangeColor,
    },
    regex: {
      color: orangeColor,
    },
  };

  return (
    <ReactMarkdown
      components={{
        pre: ({ children }) => {
          return (
            <pre className={`w-[100%]`}>
              {" "}
              {GetFormattedChildrenMarkup(children)}
            </pre>
          );
        },
        p: ({ children }) => {
          return (
            <p className={`my-2 w-[100%] ${elementClassName}`}>
              {GetFormattedChildrenMarkup(children)}
            </p>
          );
        },
        ul: ({ children }) => {
          return (
            <ul
              className={`w-[100%] list-inside list-disc ${elementClassName}`}
            >
              {GetFormattedChildrenMarkup(children)}
            </ul>
          );
        },
        li: ({ children }) => {
          return (
            <li className={`chat-markup-li my-2 ml-5 ${elementClassName}`}>
              {GetFormattedChildrenMarkup(children)}
            </li>
          );
        },
        ol: ({ children }) => {
          return (
            <ol
              className={`w-[100%] list-inside list-decimal ${elementClassName}`}
            >
              {GetFormattedChildrenMarkup(children)}
            </ol>
          );
        },
        code({ className, children }) {
          const match = /language-(\w+)/.exec(className || "");

          return match ? (
            <Group
              className={`z-[99999] flex w-[100%] flex-col ${codeGroupClassName}`}
              gap={0}
            >
              <SyntaxHighlighter
                language={match[1]}
                showLineNumbers={true}
                customStyle={{
                  backgroundColor: "var(--color-very-light-silver)",
                  padding: 10,
                  paddingLeft: 0,
                  margin: 0,
                  width: "100%",
                  whiteSpace: "pre-line",
                }}
                style={planckStyleTheme as any}
              >
                {GetFormattedChildrenMarkup(children) as string}
              </SyntaxHighlighter>
              <Group
                gap={0}
                className="mb-[10px] flex w-[100%] flex-row items-center justify-between rounded-bl-[16px] rounded-br-[16px] bg-borderColor pl-[24px] pr-[12px]"
              >
                <Text
                  className={`py-[12px] ${className ? "text-code-12" : "text-code-14"} !font-normal`}
                >
                  Use code with caution
                </Text>
                <UnstyledButton
                  className="flex flex-row items-center p-0"
                  onClick={() => {
                    navigator.clipboard.writeText(children as string);
                  }}
                >
                  <MaterialIcon
                    name="content_copy"
                    size={18}
                    tooltipLabel="Copy"
                  />
                </UnstyledButton>
              </Group>
            </Group>
          ) : (
            <code
              className={`${className ? "text-body-12" : "text-body-14"} whitespace-pre-line !font-bold italic ${className}`}
            >
              {GetFormattedChildrenMarkup(children)}
            </code>
          );
        },
      }}
    >
      {message}
    </ReactMarkdown>
  );
});

export default GetChatMessageWithMarkup;

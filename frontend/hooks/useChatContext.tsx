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

"use client";

import { getErrorNotificationConfig } from "@/config/notifications";
import { getChatHistory } from "@/queries/clientQueries";
import {
  EvaluationChatTurnPair,
  EvaluationChatTurnsResponse,
  EvaluationScoreStatus,
  LLMEvaluations,
} from "@/types";
import { notifications } from "@mantine/notifications";
import {
  Dispatch,
  PropsWithChildren,
  SetStateAction,
  createContext,
  useContext,
  useRef,
  useState,
} from "react";

import { Chat } from "../types";

type ChatContextType = {
  chats: Chat[];
  setChats: Dispatch<SetStateAction<Chat[]>>;
  updateChat: (
    chatId: string,
    chatKey: keyof Chat,
    data: Chat[keyof Chat],
  ) => void;
  selectedChatTurnIds: string[];
  setSelectedChatTurnIds: (ids: string[]) => void;
  selectedPairs: EvaluationChatTurnPair[];
  setSelectedPairs: (pairs: EvaluationChatTurnPair[]) => void;
  startEvaluationResultsPooling: (data: EvaluationChatTurnsResponse) => void;
};

const ChatContext = createContext<ChatContextType | null>(null);

export const ChatProvider = (props: PropsWithChildren) => {
  const { children } = props;
  const generateRandomNumber = () => {
    return Date.now() + Math.random();
  };

  const chatsInitialData = Array.from({ length: 2 }, (_) => {
    return {
      id: generateRandomNumber().toString(),
      model: null,
      messages: [],
    };
  });
  const [chats, setChats] = useState<Chat[]>(chatsInitialData);
  const [selectedChatTurnIds, setSelectedChatTurnIds] = useState([""]);
  const [selectedPairs, setSelectedPairs] = useState<EvaluationChatTurnPair[]>(
    [],
  );
  const [, setPoolingRequestsCount] = useState(1);
  const longPoolingFnRef = useRef<NodeJS.Timeout>();
  const pendingEvaluationTurnIdsRef = useRef<Set<string>>(new Set());
  const completedEvaluationsRef = useRef<{ [key: string]: LLMEvaluations }>({});
  const [, setIsPolling] = useState(false);
  const activePollingRequests = useRef<{ [id: string]: NodeJS.Timeout }>({});

  const updateChatTurnEvaluationResults = (
    chatTurnId: string,
    evaluations: LLMEvaluations,
  ) => {
    setChats((prevChats) =>
      prevChats.map((chat) => ({
        ...chat,
        messages: chat.messages.map((message) => {
          if (message.chat_turn_id === chatTurnId) {
            const scores = Object.keys(evaluations).map((name) => ({
              name,
              value: evaluations[name]?.score || null,
            }));

            return {
              ...message,
              evaluationResults: {
                data: scores,
                inProgress: false,
              },
            };
          }

          return message;
        }),
      })),
    );
  };

  const findChatIdForTurn = (chatTurnId: string) => {
    for (const chat of chats) {
      for (const message of chat.messages) {
        if (message.chat_turn_id === chatTurnId && chat.id.match("chat-")) {
          return chat.id;
        }
      }
    }

    return null;
  };

  const pollChatTurn = async (chatId: string, turnId: string) => {
    const response = await getChatHistory(chatId);

    const matchingChatTurn = (response.chat_turns || []).find(
      (turn) => turn.chat_turn_id === turnId,
    );

    if (!matchingChatTurn) {
      scheduleRetryForTurn(chatId, turnId);

      return;
    }

    let allEvaluationsComplete = true;
    const evaluations = matchingChatTurn.llm_evaluations || {};

    if (Object.keys(evaluations).length === 0) {
      allEvaluationsComplete = false;
    } else {
      for (const evalName in evaluations) {
        const status = evaluations[evalName]?.evaluationStatus;

        if (
          ![
            EvaluationScoreStatus.SUCCESSFUL,
            EvaluationScoreStatus.FAILED,
          ].includes(status)
        ) {
          allEvaluationsComplete = false;
          break;
        }
      }
    }

    if (allEvaluationsComplete) {
      completedEvaluationsRef.current[turnId] = evaluations;
      pendingEvaluationTurnIdsRef.current.delete(turnId);
      updateChatTurnEvaluationResults(turnId, evaluations);

      if (pendingEvaluationTurnIdsRef.current.size === 0) {
        setIsPolling(false);
      }
    } else {
      scheduleRetryForTurn(chatId, turnId);
    }
  };
  const scheduleRetryForTurn = (chatId: string, turnId: string) => {
    if (!pendingEvaluationTurnIdsRef.current.has(turnId)) return;

    activePollingRequests.current[turnId] = setTimeout(() => {
      pollChatTurn(chatId, turnId);
    }, 5000);
  };

  const pollAllPendingTurns = () => {
    const pendingIds = Array.from(pendingEvaluationTurnIdsRef.current);

    if (pendingIds.length === 0) {
      setIsPolling(false);

      return;
    }

    Object.values(activePollingRequests.current).forEach((timer) => {
      clearTimeout(timer);
    });
    activePollingRequests.current = {};

    pendingIds.forEach((turnId) => {
      const chatId = findChatIdForTurn(turnId);
      if (chatId) {
        pollChatTurn(chatId, turnId);
      } else {
        pendingEvaluationTurnIdsRef.current.delete(turnId);
      }
    });

    if (pendingEvaluationTurnIdsRef.current.size > 0) {
      longPoolingFnRef.current = setTimeout(() => {
        setPoolingRequestsCount((prev) => {
          const newCount = prev + 1;
          if (newCount > 20) {
            notifications.show(
              getErrorNotificationConfig(
                "Evaluation exceeded the number of retries to get results.",
                "evaluation-retries-limit",
              ),
            );
            setIsPolling(false);

            return prev;
          }
          pollAllPendingTurns();

          return newCount;
        });
      }, 2000);
    }
  };

  const context = {
    chats,
    setChats,
    updateChat: (
      chatId: string,
      chatKey: keyof Chat,
      data: Chat[keyof Chat],
    ) => {
      const newChats = chats.map((chat) => {
        if (chat?.id !== chatId) {
          return { ...chat };
        }

        (chat as Record<typeof chatKey, typeof data>)[chatKey] = data;

        return chat;
      });

      setChats(newChats);
    },
    selectedChatTurnIds,
    setSelectedChatTurnIds,
    selectedPairs,
    setSelectedPairs,
    startEvaluationResultsPooling: (data: EvaluationChatTurnsResponse) => {
      if (longPoolingFnRef.current) {
        clearTimeout(longPoolingFnRef.current);
      }

      Object.values(activePollingRequests.current).forEach((timer) => {
        clearTimeout(timer);
      });
      activePollingRequests.current = {};

      completedEvaluationsRef.current = {};
      pendingEvaluationTurnIdsRef.current = new Set(data.chat_turn_ids);
      setPoolingRequestsCount(1);
      setIsPolling(true);

      setChats((prevChats) =>
        prevChats.map((chat) => ({
          ...chat,
          messages: chat.messages.map((message) => {
            if (
              message.chat_turn_id &&
              data.chat_turn_ids.includes(message.chat_turn_id)
            ) {
              return {
                ...message,
                evaluationResults: {
                  data: [],
                  inProgress: true,
                },
              };
            }

            return message;
          }),
        })),
      );

      pollAllPendingTurns();
    },
  };

  return (
    <ChatContext.Provider value={context}>{children}</ChatContext.Provider>
  );
};

export function useChatContext() {
  return useContext(ChatContext) as ChatContextType;
}

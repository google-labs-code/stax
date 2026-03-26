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

import { getHumanEvaluatorsQuery } from "@/queries/clientQueries";
import { Model } from "@/queries/types";
import { HumanEvaluator } from "@/types";
import { useQuery } from "@tanstack/react-query";
import {
  PropsWithChildren,
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";

type HumanEvalsContextType = {
  feedbackEvalId: string | undefined;
  allHumanEvals: Model[];
  isLoadingHumanEvals: boolean;
  refreshHumanEvals: () => void;
};

const HumanEvalsContext = createContext<HumanEvalsContextType | null>(null);

export const HumanEvalsProvider = (props: PropsWithChildren) => {
  const { children } = props;
  const [feedbackEvalId, setFeedbackEvalId] = useState<string>();

  const {
    data,
    isLoading: isLoadingHumanEvals,
    refetch,
    isRefetching,
  } = useQuery({
    queryKey: ["human-evaluators"],
    queryFn: () => getHumanEvaluatorsQuery(),
  });

  useEffect(() => {
    data?.map((humanEval: HumanEvaluator) => {
      if (humanEval.name === "Thumbs Up/Down") {
        setFeedbackEvalId(humanEval.id);
      }
    });
  }, [data]);

  const humanEvalsContext = {
    feedbackEvalId,
    allHumanEvals: data || [],
    refreshHumanEvals: refetch,
    isLoadingHumanEvals: isLoadingHumanEvals || isRefetching,
  };

  return (
    <HumanEvalsContext.Provider value={humanEvalsContext}>
      {children}
    </HumanEvalsContext.Provider>
  );
};

export function useHumanEvalsContext() {
  return useContext(HumanEvalsContext) as HumanEvalsContextType;
}

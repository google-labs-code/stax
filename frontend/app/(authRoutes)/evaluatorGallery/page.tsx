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

import EvaluatorSearchBox from "@/components/Evaluation/EvaluatorSearchBox";
import EvaluatorTabs from "@/components/Evaluation/EvaluatorTabs";
import Page from "@/components/Page";
import PageHeader from "@/components/PageHeader";
import { getAllEvaluatorsQuery } from "@/queries/clientQueries";
import { EvaluatorCardItem, EvaluatorTab, ScorerCardsProps } from "@/types";
import { getGroupedEvaluatorsList } from "@/utils/evaluators";
import sortByName from "@/utils/sortByName";
import { toggleCardSelection } from "@/utils/toggleScorerCardSelection";
import { Group } from "@mantine/core";
import { useMutation } from "@tanstack/react-query";
import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function EvaluatorsGallery() {
  const [selectedCards, setSelectedCards] = useState<ScorerCardsProps[]>([]);
  const [data, setData] = useState<EvaluatorCardItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState(EvaluatorTab.DEFAULT);
  const searchParams = useSearchParams();
  const [initialData, setInitialData] = useState<EvaluatorCardItem[]>([]);

  useEffect(() => {
    if (searchParams.get("tab")) {
      setActiveTab(searchParams.get("tab") as EvaluatorTab);
    }
  }, [searchParams]);

  const getAllEvaluators = useMutation({
    mutationFn: () => getAllEvaluatorsQuery(),
    onSuccess: (response) => {
      const finalData = getGroupedEvaluatorsList(response);

      setData((prevData) => {
        return sortByName([...prevData, ...finalData]);
      });
      setInitialData((prevData) => {
        return sortByName([...prevData, ...finalData]);
      });
      setIsLoading(false);
    },
  });

  useEffect(() => {
    setIsLoading(true);
    getAllEvaluators.mutate();
  }, []);

  const searchBoxComponent = (
    <EvaluatorSearchBox
      initialData={initialData}
      data={data}
      setData={setData}
      selectedCards={selectedCards}
      isPage={true}
      setSelectedCards={setSelectedCards}
      onSelect={toggleCardSelection}
    />
  );

  return (
    <Page fullHeight>
      <Group justify="space-between" className="w-full">
        <PageHeader
          title="Evaluator Gallery"
          rightSection={searchBoxComponent}
        />
      </Group>

      <EvaluatorTabs
        isPage
        isLoading={isLoading}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        setSelectedCards={setSelectedCards}
        data={data}
        setData={setData}
        selectedCards={selectedCards}
        toggleCardSelection={toggleCardSelection}
      />
    </Page>
  );
}

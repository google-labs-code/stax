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

import Page from "@/components/Page";
import PageHeader from "@/components/PageHeader";
import { useAnalyticsContext } from "@/hooks/useAnalyticsContext";
import { Card, Tabs } from "@mantine/core";

import EvaluationAnalytics from "./components/evaluation/Evaluation";
import InferenceAnalytics from "./components/inference/Inference";
import "./styles/main.scss";

export default function AnalyticsPage() {
  const { activeTab, setActiveTab } = useAnalyticsContext();

  return (
    <Page fullHeight>
      <PageHeader title="Analytics" />
      <Tabs
        value={activeTab}
        onChange={(val) => setActiveTab(val as "inference" | "evaluation")}
        classNames={{
          tab: "mx-[12px] px-0 pb-[6px] text-secondary",
        }}
        keepMounted={false}
      >
        <Tabs.List className="mb-[24px] before:border-transparent">
          <Tabs.Tab value="inference">Inference</Tabs.Tab>
          <Tabs.Tab value="evaluation">Evaluation</Tabs.Tab>
        </Tabs.List>
        <Card
          classNames={{ root: "!p-[24px]" }}
          className="gap-4xl border-lightSilver mb-5 !rounded-xl border border-solid"
          shadow="md"
        >
          <Tabs.Panel value="inference">
            <InferenceAnalytics />
          </Tabs.Panel>
          <Tabs.Panel value="evaluation">
            <EvaluationAnalytics />
          </Tabs.Panel>
        </Card>
      </Tabs>
    </Page>
  );
}

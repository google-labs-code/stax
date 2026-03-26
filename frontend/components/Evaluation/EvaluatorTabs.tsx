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

import NewEvaluatorButton from "@/app/(authRoutes)/evaluatorGallery/components/NewEvaluatorButton";
import { EvaluatorPageAction } from "@/app/(authRoutes)/evaluatorGallery/types";
import EvaluationCard from "@/components/Evaluation/EvaluationCard";
import MaterialIcon from "@/components/MaterialIcon";
import { routes } from "@/config/routes";
import {
  deleteLLMEvaluatorQuery,
  deleteSxSLLMEvaluatorQuery,
} from "@/queries/clientQueries";
import {
  EvaluatorCardItem,
  EvaluatorCardItemType,
  EvaluatorTab,
  EvaluatorType,
  ProjectType,
  ScorerCardsProps,
} from "@/types";
import {
  Box,
  Flex,
  Group,
  Loader,
  ScrollArea,
  Stack,
  Tabs,
  Text,
} from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { Dispatch, SetStateAction, useState } from "react";

import DeleteModal from "../DeleteModal";
import EvaluatorSearchBox from "./EvaluatorSearchBox";
import EvaluatorSortButton from "./EvaluatorSortButton";
import ExportEvaluatorsButton from "./ExportEvaluatorsButton";

type ScorerTabsProps = {
  data: EvaluatorCardItem[];
  setData: Dispatch<SetStateAction<EvaluatorCardItem[]>>;
  initialData?: EvaluatorCardItem[];
  isPage?: boolean;
  setSelectedCards: Dispatch<SetStateAction<ScorerCardsProps[]>>;
  selectedCards: ScorerCardsProps[];
  toggleCardSelection: (
    id: string,
    title: string,
    setSelectedCards: Dispatch<SetStateAction<ScorerCardsProps[]>>,
  ) => void;
  isLoading: boolean;
  activeTab: EvaluatorTab;
  setActiveTab: Dispatch<SetStateAction<EvaluatorTab>>;
};

export default function EvaluatorTabs({
  data,
  initialData = [],
  selectedCards,
  isPage,
  setSelectedCards,
  toggleCardSelection,
  isLoading,
  activeTab,
  setActiveTab,
  setData,
}: ScorerTabsProps) {
  const tabs = [EvaluatorTab.DEFAULT, EvaluatorTab.MY_EVALUATORS];
  const [selectedEvaluator, setSelectedEvaluator] =
    useState<EvaluatorCardItem>();
  const router = useRouter();
  const [
    isDeleteModalOpened,
    { open: openDeleteModal, close: closeDeleteModal },
  ] = useDisclosure(false);

  const getTabClasses = (tabValue: EvaluatorTab) => `
  px-0 pb-[6px] inline-block cursor-pointer ${isPage ? "bg-veryLightSilver" : "bg-neutrals-50"} border-0 text-title-16 border-b-[3px]
  ${tabValue === activeTab ? "border-brand-400 text-secondaryDark" : " border-transparent text-secondary"}
`;

  const handleTabChange = (value: string | null) => {
    if (value !== null) {
      setActiveTab(value as EvaluatorTab);
    }
  };

  const deleteEvaluatorMutation = useMutation({
    mutationFn: (data: EvaluatorCardItemType) => {
      if (data.type === ProjectType.SIDE_BY_SIDE) {
        return deleteSxSLLMEvaluatorQuery(data.id);
      }

      return deleteLLMEvaluatorQuery(data.id);
    },
    onSuccess: async (_, params) => {
      setData((prevData) => {
        return prevData.filter((ev) => ev?.id !== params?.id);
      });
    },
  });

  const onOpenDeleteModal = (evaluator: EvaluatorCardItem) => {
    setSelectedEvaluator(evaluator);
    openDeleteModal();
  };

  const renderTabs = (tab: EvaluatorTab) => {
    const systemData = data.filter((d) => d.type === EvaluatorType.SYSTEM);
    const userData = data.filter((d) => d.type === EvaluatorType.USER);

    const showEmptyState =
      tab === EvaluatorTab.MY_EVALUATORS && userData.length === 0;

    const tabPanelClass = isPage
      ? "h-full bg-veryLightSilver"
      : `[@media_(min-height:400px)]:h-[130px] [@media_(min-height:500px)]:h-[180px] [@media_(min-height:600px)]:h-[270px] [@media_(min-height:700px)]:h-[370px] [@media_(min-height:800px)]:h-[480px] [@media_(min-height:900px)]:h-[550px]  border border-solid border-neutrals-300 bg-neutrals-100 ${
          !isPage && showEmptyState ? "flex justify-center items-center" : ""
        }`;

    return (
      <Tabs.Panel value={tab} key={tab} className={tabPanelClass}>
        {isPage ? (
          <div className="p-4 w-full">
            {showEmptyState ? (
              <div className="flex items-center justify-center h-[calc(100vh-200px)]">
                <div className="text-center">
                  <div className="mb-6 text-title-24">
                    Create your first custom LLM evaluator
                  </div>
                  <NewEvaluatorButton />
                </div>
              </div>
            ) : (
              <div className="grid grid-cols-1 gap-4 w-full">
                {tab === EvaluatorTab.DEFAULT && isLoading ? (
                  <Loader size="xs" className="mt-[25px] flex self-center" />
                ) : (
                  (tab === EvaluatorTab.DEFAULT ? systemData : userData).map(
                    (card, key: number) => (
                      <div key={key} className="rounded-lg">
                        <EvaluationCard
                          isPage={isPage}
                          tab={activeTab}
                          card={card}
                          setSelectedCards={setSelectedCards}
                          isCardChecked={selectedCards.some(
                            (selectedCard) => selectedCard.id === card.id,
                          )}
                          openDeleteModal={() => {
                            onOpenDeleteModal(card);
                          }}
                          onClick={
                            isPage
                              ? () => {
                                  router.push(
                                    routes.evaluatorGallery.root +
                                      "/" +
                                      card.id +
                                      "/" +
                                      (card.type === EvaluatorType.SYSTEM
                                        ? EvaluatorPageAction.VIEW
                                        : EvaluatorPageAction.EDIT),
                                  );
                                }
                              : toggleCardSelection
                          }
                        />
                      </div>
                    ),
                  )
                )}
              </div>
            )}
          </div>
        ) : (
          <ScrollArea.Autosize
            classNames={{ scrollbar: "my-3" }}
            mah="100%"
            scrollbarSize={6}
            offsetScrollbars
          >
            <Stack className="p-4" gap="md">
              {showEmptyState ? (
                <Box
                  w="100%"
                  className="flex flex-col items-center justify-center"
                >
                  <Text className="text-secondary text-center">
                    You haven&apos;t created any evaluators yet.
                  </Text>
                  <Box mt={2}>
                    <Flex justify="center" align="center" gap={4}>
                      <Text className="text-secondary">Open the</Text>
                      <Text
                        component="span"
                        className="text-brand cursor-pointer"
                        onClick={() =>
                          router.push(routes.evaluatorGallery.root)
                        }
                      >
                        Evaluator Gallery
                      </Text>
                      <MaterialIcon
                        name="open_in_new"
                        className="text-brand"
                        size={16}
                      />
                      <Text className="text-secondary">to get started.</Text>
                    </Flex>
                  </Box>
                </Box>
              ) : (
                <>
                  {tab === EvaluatorTab.DEFAULT && isLoading ? (
                    <Loader size="xs" className="mt-[25px] flex self-center" />
                  ) : (
                    (tab === EvaluatorTab.DEFAULT ? systemData : userData).map(
                      (card, key: number) => (
                        <EvaluationCard
                          key={key}
                          isPage={isPage}
                          tab={activeTab}
                          card={card}
                          setSelectedCards={setSelectedCards}
                          isCardChecked={selectedCards.some(
                            (selectedCard) => selectedCard.id === card.id,
                          )}
                          openDeleteModal={() => {
                            onOpenDeleteModal(card);
                          }}
                          onClick={toggleCardSelection}
                        />
                      ),
                    )
                  )}
                </>
              )}
            </Stack>
          </ScrollArea.Autosize>
        )}
      </Tabs.Panel>
    );
  };

  return (
    <Tabs
      unstyled
      defaultValue={EvaluatorTab.DEFAULT}
      value={activeTab}
      onChange={handleTabChange}
      className={`${isPage ? "h-full bg-veryLightSilver" : "h-[90%]"} bg-transparent`}
    >
      {isPage && (
        <DeleteModal
          isOpen={isDeleteModalOpened}
          onClose={closeDeleteModal}
          onConfirm={() => {
            if (selectedEvaluator && selectedEvaluator?.evaluationTypes) {
              for (const evaluator of selectedEvaluator.evaluationTypes) {
                deleteEvaluatorMutation.mutate(evaluator);
              }
            }
            closeDeleteModal();
          }}
          isLoading={deleteEvaluatorMutation.isPending}
          title="Delete Evaluator"
          description="Are you sure you want to delete this evaluator? This cannot be undone."
        />
      )}

      <Tabs.List
        className={
          isPage ? "bg-veryLightSilver mb-[24px]" : "bg-neutrals-50 mb-3"
        }
      >
        <Group px="sm" justify="space-between" w="full">
          <Group gap={24} className="ml-2">
            <Tabs.Tab
              value={EvaluatorTab.DEFAULT}
              className={getTabClasses(EvaluatorTab.DEFAULT)}
              onClick={() => {
                if (isPage) {
                  router.push(
                    routes.evaluatorGallery.root +
                      "?tab=" +
                      EvaluatorTab.DEFAULT,
                  );
                }
              }}
            >
              <Text className="text-title-14">Default</Text>
            </Tabs.Tab>
            <Tabs.Tab
              value={EvaluatorTab.MY_EVALUATORS}
              className={getTabClasses(EvaluatorTab.MY_EVALUATORS)}
              onClick={() => {
                if (isPage) {
                  router.push(
                    routes.evaluatorGallery.root +
                      "?tab=" +
                      EvaluatorTab.MY_EVALUATORS,
                  );
                }
              }}
            >
              <Text className="text-title-14">My Evaluators</Text>
            </Tabs.Tab>
          </Group>

          <Group gap="sm">
            {!isPage && (
              <EvaluatorSearchBox
                initialData={initialData}
                data={data}
                setData={setData}
                selectedCards={selectedCards}
                setSelectedCards={setSelectedCards}
                onSelect={toggleCardSelection}
              />
            )}

            {isPage && (
              <>
                <EvaluatorSortButton
                  data={data}
                  setData={setData}
                  activeTab={activeTab}
                />
                <ExportEvaluatorsButton data={data} activeTab={activeTab} />
              </>
            )}
            {!isPage && <EvaluatorSortButton data={data} setData={setData} />}
          </Group>
        </Group>
      </Tabs.List>

      {tabs.map((tab) => renderTabs(tab))}
    </Tabs>
  );
}

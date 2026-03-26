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

import { useProjectContext } from "@/app/(authRoutes)/projects/hooks/useProjectContext";
import Card from "@/components/Card";
import { routes } from "@/config/routes";
import {
  EvaluatorCardItem,
  EvaluatorCategory,
  EvaluatorTab,
  EvaluatorType,
  ProjectType,
  ScorerCardsProps,
} from "@/types";
import { Box, Flex, Group, Text } from "@mantine/core";
import { Dispatch, SetStateAction, useCallback, useMemo } from "react";

import ViewDetailsButton from "../ViewDetailsButton";
import EvaluationTypeDropdown from "./EvaluationTypeDropdown";
import EvaluatorCardType from "./EvaluatorCardType";
import EvaluatorDuplicateButton from "./EvaluatorDuplicateButton";
import OutputCategories from "./OutputCategories";
import TabsActionMenu from "./TabsActionMenu";

type EvaluationCardProps = {
  card: EvaluatorCardItem;
  isPage?: boolean;
  tab: string | null;
  isCardChecked: boolean;
  setSelectedCards: Dispatch<SetStateAction<ScorerCardsProps[]>>;
  onClick: (
    id: string,
    title: string,
    setSelectedCards: Dispatch<SetStateAction<ScorerCardsProps[]>>,
  ) => void;
  openDeleteModal: () => void;
};

function EvaluationCard({
  card,
  isPage,
  tab,
  setSelectedCards,
  isCardChecked,
  onClick,
  openDeleteModal,
}: EvaluationCardProps) {
  const { id, name, description, type, output_categories } = card;
  const projectContext = useProjectContext();
  let projectType = undefined;
  if (projectContext) {
    projectType = projectContext.projectType;
  }

  const cardStyle = useMemo(
    () => `rounded-xl cursor-pointer px-6 py-4 border border-solid
    ${
      isCardChecked
        ? "bg-brand-50 border-brand-500"
        : "border-transparent hover:border-brand-500"
    }`,
    [isCardChecked],
  );

  const isSystemEvaluator = useMemo(
    () => type === EvaluatorType.SYSTEM || tab === EvaluatorTab.DEFAULT,
    [type, tab],
  );

  const handleCardClick = useCallback(() => {
    onClick(card.id, card.name, setSelectedCards);
  }, [card.id, card.name, onClick, setSelectedCards]);

  const handleViewDetailsClick = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      window.open(`${routes.evaluatorGallery.root}/${id}/view`, "_blank");
    },
    [id],
  );

  const preventCardSelection = useCallback((e: React.MouseEvent) => {
    e.stopPropagation();
  }, []);

  const textClass = useMemo(
    () =>
      `whitespace-pre-line text-body-14 mb-1 text-secondary
     ${isPage ? "max-w-[87%]" : "max-w-[100%]"}`,
    [isPage],
  );

  return (
    <Card
      key={id}
      className={cardStyle}
      shadow="none"
      onClick={handleCardClick}
    >
      <Flex direction="column" gap={4} h="100%">
        <Group justify="space-between" align="flex-start">
          <Text className="text-title-16 pr-4">{name}</Text>
          {isPage &&
            (isSystemEvaluator ? (
              <EvaluatorDuplicateButton id={id} size="xs" />
            ) : (
              <TabsActionMenu
                tab={tab}
                dataId={card.id}
                openDeleteModal={openDeleteModal}
              />
            ))}
        </Group>

        <Group className="gap-xl flex-row flex-1 justify-between">
          <Text className={textClass}>{description}</Text>

          <Group className="gap-0 w-full">
            {isPage ? (
              <Group gap="md">
                <OutputCategories categories={output_categories || []} />
                <EvaluatorCardType
                  category={EvaluatorCategory.LLM}
                  evaluationTypes={
                    card.evaluationTypes?.map((evaluator) => evaluator.type) ||
                    []
                  }
                />
              </Group>
            ) : (
              <Group className="gap-0 flex-row justify-between w-full">
                <Box onClick={preventCardSelection}>
                  {projectType === ProjectType.SIDE_BY_SIDE && (
                    <EvaluationTypeDropdown
                      card={card}
                      isCardChecked={isCardChecked}
                    />
                  )}
                </Box>
                <ViewDetailsButton onClick={handleViewDetailsClick} />
              </Group>
            )}
          </Group>
        </Group>
      </Flex>
    </Card>
  );
}

export default EvaluationCard;

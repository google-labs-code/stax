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

import MaterialIcon from "@/components/MaterialIcon";
import { routes } from "@/config/routes";
import { useProjectsContext } from "@/hooks/useProjectsContext";
import { createProjectQuery } from "@/queries/clientQueries";
import { GAevents, Project, ProjectType } from "@/types";
import logGAevent from "@/utils/logGAevent";
import { Button, Card, Group, Modal, Text } from "@mantine/core";
import { useMutation } from "@tanstack/react-query";
import Link from "next/link";
import { useRouter } from "next/navigation";

import { useProjectContext } from "../hooks/useProjectContext";

type CreateProjectModalProps = {
  isOpened: boolean;
  onClose: () => void;
};

export default function CreateProjectModal({
  isOpened,
  onClose,
}: CreateProjectModalProps) {
  const { refreshProjects } = useProjectsContext();
  const { projectActions } = useProjectContext();
  const router = useRouter();
  const cards = [
    {
      icon: "linear_scale",
      iconLabel: "Pointwise Evaluation",
      description:
        "Get a metric score for AI performance. Use this to evaluate absolute quality.",
      type: ProjectType.POINTWISE,
    },
    {
      icon: "split_scene",
      iconLabel: "Side-by-Side Comparison",
      description:
        "Get a preference between two outputs. Compare models or prompts to choose the winner.",
      type: ProjectType.SIDE_BY_SIDE,
    },
  ];

  const createProjectMutation = useMutation({
    mutationFn: createProjectQuery,
    onSuccess: async (project: Project) => {
      await refreshProjects();
      projectActions.resetProjectData();
      setTimeout(() => {
        router.push(`${routes.projects}/${project.project_id}`);
      });
    },
  });

  return (
    <Modal
      opened={isOpened}
      onClose={onClose}
      withCloseButton={true}
      size="700px"
      centered
      padding="0"
      classNames={{
        header: "w-full pr-[24px] pt-[20px] bg-veryLightSilver",
        content: "rounded-xl flex flex-col items-center",
        body: "!h-[345px] w-[100%]",
        root: "!p-0",
      }}
    >
      <Card className="bg-veryLightSilver flex h-[100%] flex-col items-center justify-start py-0">
        <div className="mb-[32px] flex w-full flex-col items-center justify-center gap-2">
          <Text className="text-title-36">Create a new evaluation project</Text>
          <Text className="text-secondary text-title-22">
            Choose between a single or side-by-side LLM evaluation
          </Text>
          <Text className="flex gap-1 text-secondary text-body-14">
            View
            <Link
              href={`${process.env.NEXT_PUBLIC_APP_BASE_URL}/docs`}
              target="_blank"
              className="text-brand outline-none text-body-14"
            >
              documentation
              <MaterialIcon
                name="open_in_new"
                className="align-middle !text-[16px] !font-extralight text-secondary"
              />
            </Link>
            for reference
          </Text>
        </div>

        <div className="space-between gap-3xl flex flex-row sm:flex-row">
          {cards.map((card, index) => (
            <Card
              key={index}
              shadow="md"
              data-testid="action-card"
              className={`flex min-h-[172px] w-[310px] max-w-[100%] flex-col justify-between rounded-lg p-[16px]`}
            >
              <div>
                <Group gap={0} className="mb-[16px] gap-xl">
                  <Group className="bg-lightBlue gap-0 justify-center flex flex-row flex-nowrap rounded-sm px-[4px] py-[2px]">
                    <MaterialIcon
                      name={card.icon}
                      size={16}
                      className="text-brand"
                    />
                  </Group>
                  <Text className="text-black text-body-14 !font-medium">
                    {card.iconLabel}
                  </Text>
                </Group>

                <Group gap={0} className="gap-lg flex flex-col items-start">
                  <Group gap={0} className="gap-xs flex flex-col items-start">
                    <Text className="text-body-14 text-secondary">
                      {card.description}
                    </Text>
                  </Group>
                </Group>
              </div>

              <Button
                className="flex !w-auto self-end px-[16px] py-[8px] !rounded-sm mt-auto"
                classNames={{
                  label: "text-body-12 rounded-sm",
                }}
                loading={createProjectMutation.isPending}
                disabled={createProjectMutation.isPending}
                onClick={() => {
                  logGAevent(GAevents.CREATE_PROJECT, {
                    type: card.type,
                  });
                  createProjectMutation.mutate({
                    type: card.type,
                  } as Project);
                }}
              >
                Create project
              </Button>
            </Card>
          ))}
        </div>
      </Card>
    </Modal>
  );
}

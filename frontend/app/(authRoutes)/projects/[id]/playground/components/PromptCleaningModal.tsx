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

import CompletedIcon from "@/assets/shape_completed.svg";
import MaterialIcon from "@/components/MaterialIcon";
import { routes } from "@/config/routes";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import LocalStorage from "@/utils/LocalStorage";
import { Button, Group, Modal, Text } from "@mantine/core";
import Image from "next/image";
import { useParams, useRouter } from "next/navigation";
import { useCallback } from "react";

import { useProjectContext } from "../../../hooks/useProjectContext";
import { SHOW_PLAYGROUND_INFO_BAR } from "../consts";
import { PromptCleaningModalProps } from "../types";

export default function PromptCleaningModal({
  isOpened,
  onClose,
}: PromptCleaningModalProps) {
  const hasSeen = LocalStorage.get(SHOW_PLAYGROUND_INFO_BAR);
  const { projectState } = useProjectContext();
  const params = useParams<{ id: string }>();
  const activeProjectId =
    projectState?.projectId ||
    projectState?.project?.project_id ||
    (params?.id as string) ||
    "";
  const { resetPlayground } = usePlaygroundContext();
  const router = useRouter();
  const onNavigateToProject = useCallback(() => {
    router.push(`${routes.projects}/${activeProjectId}`);
  }, [router, activeProjectId]);

  const handleClose = useCallback(() => {
    onClose();
    LocalStorage.set(SHOW_PLAYGROUND_INFO_BAR, "true");
  }, [onClose]);

  return (
    <Modal
      opened={isOpened && !hasSeen}
      onClose={handleClose}
      size="496px"
      title={
        <Group className="mt-5 w-[440px] flex-col justify-center">
          <Group className="relative flex h-[88px] w-[88px] items-center justify-center">
            <Image
              alt="Completed Icon"
              src={CompletedIcon}
              width={88}
              height={88}
            />
            <MaterialIcon
              name="check"
              className="left-50 top-50 absolute !text-[40px] text-white"
            />
          </Group>
          <Text className="mt-3 !font-medium text-title-24">
            Test case saved to project!
          </Text>
        </Group>
      }
      centered
      zIndex={500}
      padding="24px"
      classNames={{
        header: "p-[16px] pb-0 mb-3",
      }}
    >
      <Group className="flex flex-col items-center justify-center gap-6">
        <Text className="text-center leading-6 text-secondary text-body-16">
          You’re building a dataset for AI evaluation. Add more test cases to
          build a robust dataset so you can compare AI performance.
        </Text>
        <Group className="mb-4 flex-col" gap={8}>
          <Button
            w="194px"
            h="48px"
            className="mt-4"
            classNames={{ label: "!text-title-14" }}
            onClick={() => {
              resetPlayground();
              LocalStorage.set(SHOW_PLAYGROUND_INFO_BAR, "true");
            }}
          >
            Add another test case
          </Button>
          <Text
            className="mt-4 cursor-pointer text-secondary !text-title-14"
            onClick={() => {
              handleClose();
              onNavigateToProject();
            }}
          >
            Return to project
          </Text>
        </Group>
      </Group>
    </Modal>
  );
}

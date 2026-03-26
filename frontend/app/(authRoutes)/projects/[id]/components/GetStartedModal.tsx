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
import { routes } from "@/config/routes";
import { Button, Group, Image, Modal, Text } from "@mantine/core";
import { useRouter } from "next/navigation";

import { useProjectContext } from "../../hooks/useProjectContext";

type GetStartedModalProps = {
  isOpened: boolean;
  onClose: () => void;
  projectId?: string | null;
  openAddDatasetModal: () => void;
  redirectToPlayground: () => void;
};

export default function GetStartedModal({
  isOpened,
  onClose,
  projectId,
  openAddDatasetModal,
  redirectToPlayground,
}: GetStartedModalProps) {
  const { isSideBySide } = useProjectContext();
  const router = useRouter();

  const options = [
    {
      title: "Test on your data",
      description:
        "See how your AI handles your use case by easily importing production datasets or the prompts your team is testing.",
    },
    {
      title: "Know for sure if you're improving",
      description:
        "Replace guesswork with clear, reliable metrics from built-in or custom evaluators so you know exactly how good your AI is.",
    },
    {
      title: "Ship better AI, faster",
      description:
        "Create a reusable benchmark to make evaluating new AI iterations a fast, repeatable process.",
    },
  ];

  const onClickAction = () => {
    onClose();
  };

  return (
    <Modal
      opened={isOpened}
      onClose={() => {
        onClickAction();
      }}
      closeButtonProps={{ "aria-label": "close button" }}
      size="1100px"
      title={
        <Text className="flex items-center !font-medium text-title-22">
          Get started evaluating your AI
        </Text>
      }
      centered
      classNames={{
        header: "p-[32px] pb-0 mb-[8px]",
        body: "p-0 pl-[32px]",
      }}
      data-testid="get-started-modal"
    >
      <Group className="gap-2xl flex flex-col items-start overflow-hidden">
        <Text className="!font-medium text-secondary text-body-16 mb-[8px]">
          Great AI products aren&apos;t built on luck. They&apos;re built on
          proof.
        </Text>
        <Group className="gap-md flex flex-col items-start">
          {options.map((option, index) => (
            <Group key={index} className="flex flex-row gap-0">
              <MaterialIcon
                name="check_circle"
                size={20}
                className="mr-[12px] text-brand"
              />
              <Text className="!font-bold text-secondary text-body-14">
                {option.title}:
              </Text>
              <Text className="ml-[3px] text-secondary text-body-14">
                {option.description}
              </Text>
            </Group>
          ))}
        </Group>
        <Group className="gap-2xl flex flex-row mt-[16px]">
          <Button
            color="primary"
            onClick={() => {
              redirectToPlayground();
              router.push(`${routes.projects}/${projectId}/playground`);
            }}
          >
            Open prompt playground
          </Button>
          <Button
            variant="outlineLight"
            className="!h-auto px-[16px] py-[8px]"
            onClick={() => {
              onClickAction();
              openAddDatasetModal();
            }}
          >
            <MaterialIcon name="cloud_upload" size={20} className="mr-[4px]" />
            Import dataset
          </Button>
        </Group>

        <Group className="h-[308px] w-[1040px] gap-0 !overflow-hidden">
          <Group className="h-[480px] w-[1040px] gap-0 !overflow-hidden">
            <Image
              alt="workbook table"
              src={isSideBySide ? "/SxS.png" : "/Pointwise.png"}
            />
          </Group>
        </Group>
      </Group>
    </Modal>
  );
}

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
import { Anchor, Text } from "@mantine/core";
import { NotificationData } from "@mantine/notifications";
import Link from "next/link";
import { ReactElement } from "react";
import { PiWarningFill } from "react-icons/pi";

import { routes } from "./routes";

export const NOTIFICATIONS_IDS = {
  ALL_MODELS_DISABLED: "all-models-disabled",
};

export const getSuccessNotificationConfig = (
  message: string | ReactElement,
  id: string = "success",
  autoClose: number = 3000,
): NotificationData => ({
  id,
  withCloseButton: true,
  icon: <MaterialIcon name="info" size={20} className="text-secondaryDark" />,
  autoClose,
  message,
  color: "transparent",
  classNames: {
    description: "text-secondaryDark text-body-14",
    closeButton: "text-secondaryDark bg-[transparent]",
  },
  className:
    "px-[24px] py-[12px] bg-lightBlue rounded-sm max-w-[100%] mb-[10px]",
});

export const getErrorNotificationConfig = (
  error: string,
  id: string = "form-error",
  autoClose: number = 5000,
): NotificationData => ({
  id,
  withCloseButton: true,
  icon: <MaterialIcon name="warning" size={20} className="text-darkRed" />,
  autoClose,
  message: <div dangerouslySetInnerHTML={{ __html: error }} />,
  color: "transparent",
  classNames: {
    description: "text-darkRed text-body-14",
    closeButton: "text-darkRed bg-[transparent]",
  },
  className:
    "px-[24px] py-[12px] bg-veryLightRed rounded-sm max-w-[100%] mb-[10px]",
});

export const pendingNotificationConfig: NotificationData = {
  id: "run-pending",
  withCloseButton: true,
  title: "Getting results",
  autoClose: false,
  message: "Please wait while we run your prompt..",
  color: "blue",
  loading: true,
};

export const disabledModelsNotificationConfig: NotificationData = {
  id: "all-models-disabled",
  withCloseButton: true,
  icon: <PiWarningFill fill="orange" size="md" />,
  title: "Please add a key to continue!",
  autoClose: 10000,
  message: (
    <Text size="sm">
      You don&apos;t have any active keys.{" "}
      <Anchor component={Link} href={routes.settings}>
        Add keys here.
      </Anchor>
    </Text>
  ),
  color: "transparent",
};

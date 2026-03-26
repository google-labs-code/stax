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

import { MainConfig } from "@/config/config";
import { PRIVACY_POLICY, TOS_GOOGLE_LINK } from "@/config/constants";
import HatsApi from "@/utils/hatsApi";
import { useDisclosure } from "@mantine/hooks";
import { useTranslation } from "react-i18next";

import ActionMenu from "./ActionMenu";
import DeleteDataModal from "./DeleteDataModal";
import MaterialIcon from "./MaterialIcon";

export default function GlobalActionMenu() {
  const [
    isDeleteDataModalOpened,
    { open: openDeleteDataModal, close: closeDeleteDataModal },
  ] = useDisclosure(false);
  const { t } = useTranslation();

  if (!MainConfig.isAuthEnabled) {
    return;
  }

  return (
    <>
      <ActionMenu
        menuItems={[
          {
            label: t("sendFeedback"),
            leftSection: <MaterialIcon name="flag" size={20} />,
            onClick: () => {
              window.userfeedback.api.startFeedback({
                productId: process?.env?.NEXT_PUBLIC_FEEDBACK_PRODUCT_ID,
              });
            },
          },
          {
            label: t("termsOfService"),
            leftSection: <MaterialIcon name="assignment" size={20} />,
            onClick: () => {
              window.open(TOS_GOOGLE_LINK, "_blank");
            },
          },
          {
            label: t("satisfactionSurvey"),
            leftSection: <MaterialIcon name="quiz" size={20} />,
            onClick: () => {
              HatsApi.getInstance(window).requestSurvey();
            },
          },
          {
            label: t("privacyPolicy"),
            leftSection: <MaterialIcon name="shield_person" size={20} />,
            onClick: () => {
              window.open(PRIVACY_POLICY, "_blank");
            },
          },
          {
            label: t("deleteStaxData"),
            leftSection: <MaterialIcon name="delete_forever" size={20} />,
            onClick: openDeleteDataModal,
          },
        ]}
      />
      <DeleteDataModal
        isOpened={isDeleteDataModalOpened}
        onClose={closeDeleteDataModal}
      />
    </>
  );
}

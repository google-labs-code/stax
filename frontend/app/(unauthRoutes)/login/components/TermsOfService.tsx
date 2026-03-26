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

import Card from "@/components/Card";
import {
  Button,
  Checkbox,
  Divider,
  Group,
  ScrollAreaAutosize,
  Stack,
  Text,
} from "@mantine/core";
import Link from "next/link";
import { useState } from "react";

import { consentLabel, privacyLink } from "../utils/constants";

type TermsOfServiceProps = {
  onContinueAction: () => void;
  onCancelAction: () => void;
  content: string;
};

export default function TermsOfService({
  onContinueAction,
  onCancelAction,
  content,
}: TermsOfServiceProps) {
  const [isConsentCheckboxChecked, setIsConsentCheckboxChecked] =
    useState<boolean>(false);

  return (
    <div
      className="flex w-full h-full justify-center items-center"
      data-testid="tos-page"
    >
      <div className="flex flex-col gap-2 h-screen sm:h-[70vh] sm:w-1/2  justify-center">
        <Card className="text-body-18">
          <Text
            component="span"
            className="text-title-18 pb-3"
            data-testid="page-title"
          >
            Terms of service
          </Text>
          <Divider size="xs" />
          <ScrollAreaAutosize>
            <div
              id="tos-welcome-content"
              className="text-body-14 p-4 whitespace-pre-wrap"
              dangerouslySetInnerHTML={{
                __html: content,
              }}
            />
          </ScrollAreaAutosize>
          <Divider />
          <Stack className="">
            <Checkbox
              className="mt-6"
              classNames={{ label: "text-body-18", body: "items-center" }}
              data-testid="accept-tos-checkbox"
              label={
                <Group gap={4}>
                  {consentLabel}
                  <Link
                    className="text-brand"
                    target="_blank"
                    href={privacyLink}
                  >
                    Google Privacy Policy
                  </Link>
                </Group>
              }
              onClick={() => {
                setIsConsentCheckboxChecked(!isConsentCheckboxChecked);
              }}
            />
            {/* <Checkbox
              classNames={{ label: "text-body-18", body: "items-center" }}
              label={researchInvitationLabel}
            />
            <Checkbox
              classNames={{ label: "text-body-18", body: "items-center" }}
              label={emailUpdatesLabel}
            /> */}
          </Stack>
          <Group mt="md" gap="xs" justify="end">
            <Button
              classNames={{ inner: "text-primary" }}
              variant="transparent"
              onClick={onCancelAction}
              data-testid="btn-cancel"
            >
              Cancel
            </Button>
            <Button
              color="blue"
              disabled={!isConsentCheckboxChecked}
              onClick={() => {
                if (isConsentCheckboxChecked) {
                  onContinueAction();
                }
              }}
            >
              Continue
            </Button>
          </Group>
        </Card>
      </div>
    </div>
  );
}

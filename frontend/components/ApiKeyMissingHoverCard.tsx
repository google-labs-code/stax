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

import { Group, HoverCard, Stack, Text } from "@mantine/core";
import Link from "next/link";
import React, { ReactNode, forwardRef } from "react";

interface ApiKeyMissingHoverCardProps {
  children: ReactNode;
  show: boolean;
}

const ApiKeyMissingHoverCard = forwardRef<
  HTMLDivElement,
  ApiKeyMissingHoverCardProps
>(({ children, show }, ref) => {
  return (
    <HoverCard
      width={200}
      position="bottom"
      shadow="md"
      openDelay={200}
      classNames={{
        dropdown: "rounded-lg p-[16px]",
      }}
      withinPortal={true}
    >
      <HoverCard.Target>
        <div ref={ref}>{children}</div>
      </HoverCard.Target>

      {show && (
        <HoverCard.Dropdown>
          <div
            onMouseDown={(e) => e.stopPropagation()}
            onClick={(e) => e.stopPropagation()}
          >
            <Stack gap={4}>
              <Text className="text-title-14">Missing API key.</Text>
              <Text className="text-secondary text-body-14">
                Go to your settings to add API key to start using this
                functionality.
              </Text>
              <Group justify="flex-end">
                <Link
                  href="/settings?tab=apiKeys"
                  className="cursor-pointer text-brand text-body-14"
                >
                  Settings
                </Link>
              </Group>
            </Stack>
          </div>
        </HoverCard.Dropdown>
      )}
    </HoverCard>
  );
});

ApiKeyMissingHoverCard.displayName = "ApiKeyMissingHoverCard";

export default ApiKeyMissingHoverCard;

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

import { Anchor, Group, Stack, Text } from "@mantine/core";
import Link from "next/link";

export default function NotFound() {
  return (
    <Stack className="text-center" gap="xs">
      <Group>
        <span className="text-2xl font-medium align-top  border-r pr-[23px] border-gray-300">
          404
        </span>
        <div className="inline-block">
          <Text size="sm">This page could not be found.</Text>
        </div>
      </Group>
      <div>
        <Anchor href="/" component={Link}>
          Go back
        </Anchor>
      </div>
    </Stack>
  );
}

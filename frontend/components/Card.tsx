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

import { CardProps, Card as MantineCard } from "@mantine/core";
import { PolymorphicComponentProps } from "@mantine/core/lib/core/factory/create-polymorphic-component";

export default function Card(
  props: PolymorphicComponentProps<"div", CardProps>,
) {
  const { children, ...rest } = props;

  return (
    <MantineCard shadow="sm" padding="lg" radius="md" withBorder {...rest}>
      {children}
    </MantineCard>
  );
}

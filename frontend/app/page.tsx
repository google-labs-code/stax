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

import { MainConfig } from "@/config/config";
import { JWT_TOKEN_KEY } from "@/config/constants";
import { routes } from "@/config/routes";
import LocalStorage from "@/utils/LocalStorage";
import { Center, Loader } from "@mantine/core";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function RootPage() {
  const router = useRouter();

  useEffect(() => {
    const hasJwtToken = LocalStorage.get(JWT_TOKEN_KEY);
    if (!MainConfig.isAuthEnabled || hasJwtToken) {
      router.push(routes.projects);
    } else {
      router.push(routes.signin);
    }
  }, []);

  return (
    <Center className="h-full w-full">
      <Loader size="xl" data-testid="loading-spinner" />
    </Center>
  );
}

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

import { UserKeysAPIResponse } from "@/types";
import { Stack } from "@mantine/core";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";

import { InitialKeys } from "../config/initialKeys";
import { getUserKeysQuery } from "../state/queries";
import { KeyDetails } from "../types";
import APIKey from "./APIKey";

export default function APIKeyList() {
  const [keys, setKeys] = useState<KeyDetails[]>();

  const { data, refetch } = useQuery({
    queryKey: ["user-keys"],
    queryFn: getUserKeysQuery,
  });

  useEffect(() => {
    if (data) {
      setKeys(
        InitialKeys.map((key) => {
          key.isKeyPresent = (data as UserKeysAPIResponse)[key.apiKey];

          return key;
        }),
      );
    }
  }, [data]);

  return (
    <>
      <Stack gap="16px" className="w-[100%]" data-testid="api-key-list">
        {keys?.map((keyType, index: number) => (
          <APIKey key={index} keyData={keyType} onChange={refetch} />
        ))}
      </Stack>
    </>
  );
}

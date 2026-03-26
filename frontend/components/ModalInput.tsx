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

import { TextInput } from "@mantine/core";


type ModalInputProps = {
  placeholder?: string;
  value?: string;
  error?: boolean;
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
};

export default function ModalInput({
  placeholder,
  value,
  error,
  onChange,
}: ModalInputProps) {
  return (
    <TextInput
      classNames={{
        error: "hidden",
        input:
          "px-[16px] py-[8px] rounded-md h-[40px] placeholder:text-title-14 border-neutrals-300 border-[1px]",
      }}
      placeholder={placeholder}
      value={value}
      error={error}
      onChange={onChange}
    />
  );
}

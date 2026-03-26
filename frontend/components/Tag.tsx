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
import { Chip, Group, Text } from "@mantine/core";

type TagProps = {
  tag: string;
  checked?: boolean;
  notClickable?: boolean;
  onClick?: () => void;
  withCloseIcon?: boolean;
  withEditIcon?: boolean;
};

export default function Tag({
  tag,
  checked,
  onClick,
  notClickable,
  withCloseIcon,
  withEditIcon,
}: TagProps) {
  return withCloseIcon || withEditIcon ? (
    <Chip
      classNames={{
        input: "p-2",
        iconWrapper: "hidden",
        label: `px-[8px] py-[4px] h-[24px] uppercase border-resting rounded-sm hover:bg-neutrals-50
       ${withEditIcon ? "cursor-pointer" : "cursor-default"}`,
      }}
      size="xs"
      variant="outline"
    >
      <Group gap={6} className="w-full justify-between">
        <Text
          className={`max-w-[130px] truncate !font-medium text-secondary text-body-11`}
        >
          {tag}
        </Text>
        <MaterialIcon
          name={withCloseIcon ? "close" : "edit"}
          className={` ${withEditIcon ? "!text-[18px]" : "!text-[14px]"} flex-shrink-0 !text-secondary`}
          onClick={onClick}
        />
      </Group>
    </Chip>
  ) : (
    <Chip
      onChange={onClick}
      classNames={{
        input: "p-2",
        iconWrapper: "hidden",
        label: `${notClickable && "pointer-events-none"}
        ${checked && "bg-lightBlue border-default"}
        px-[8px] py-[4px] h-[24px] uppercase rounded-sm `,
      }}
      size="xs"
      variant="outline"
    >
      <Text
        className={`text-body-11 ${checked ? "text-brand" : "text-secondary"} max-w-[130px] truncate !font-medium`}
      >
        {tag}
      </Text>
    </Chip>
  );
}

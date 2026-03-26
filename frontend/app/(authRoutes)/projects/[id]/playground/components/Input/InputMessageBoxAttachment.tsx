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
import PdfIcon from "@/components/icons/PdfIcon";
import { usePlaygroundContext } from "@/hooks/usePlaygroundContext";
import { InferenceChatCompletionPromptRole } from "@/types";
import {
  Button,
  FileButton,
  Group,
  Image,
  Text,
  UnstyledButton,
} from "@mantine/core";
import { useCallback } from "react";

import { InputMessageBoxAttachmentProps } from "../../types";

export default function InputMessageBoxAttachment({
  id,
  role,
  attachment,
}: InputMessageBoxAttachmentProps) {
  const { setInputs, setIsPlaygroundModified, setIsNewChat } =
    usePlaygroundContext();
  const ALLOWED_MIMETYPES =
    "image/png,image/jpeg,image/jpg,image/gif,image/webp,application/pdf";

  const DeleteAttachmentButton = useCallback(() => {
    return (
      <UnstyledButton
        onClick={() => {
          setIsPlaygroundModified(true);
          setIsNewChat(true);
          setInputs((prevState) =>
            prevState.map((input) => {
              if (input.id === id) {
                input.attachment = null;
              }

              return input;
            }),
          );
        }}
        className="absolute top-[4px] right-[4px] w-[20px] h-[20px] flex justify-center items-center bg-pinkBg rounded-xs"
      >
        <MaterialIcon name="delete" size={16} className="text-pink" />
      </UnstyledButton>
    );
  }, [id]);

  const AttachmentImageThumbnail = useCallback(() => {
    if (!attachment) return null;

    return (
      <div className="flex justify-start relative !w-[142px] !h-[100px]">
        <Image
          src={URL.createObjectURL(attachment)}
          alt={attachment.name}
          className="object-contain rounded-sm "
        />
        <DeleteAttachmentButton />
      </div>
    );
  }, [attachment]);

  const AttachmentPdfThumbnail = useCallback(() => {
    if (!attachment) return null;

    return (
      <div className="flex justify-start relative !w-[160px] !w-auto !h-[68px] bg-veryLightSilver rounded-sm p-3 pr-[30px]">
        <Group className="flex-col flex justify-between items-center gap-0 flex-nowrap truncate">
          <Text className="text-body-12 !font-bold w-full truncate">
            {attachment.name}
          </Text>

          <Group className="flex-row flex flex-start items-center gap-2 w-full">
            <PdfIcon size={16} />
            <Text className="text-body-12 text-secondary !font-bold">PDF</Text>
          </Group>
        </Group>
        <DeleteAttachmentButton />
      </div>
    );
  }, [attachment]);

  return (
    <>
      {role === InferenceChatCompletionPromptRole.USER && !attachment && (
        <FileButton
          onChange={(file) => {
            setIsNewChat(true);
            setIsPlaygroundModified(true);
            setInputs((prevState) =>
              prevState.map((input) => {
                if (input.id === id) {
                  input.attachment = file;
                }

                return input;
              }),
            );
          }}
          accept={ALLOWED_MIMETYPES}
        >
          {(props) => (
            <Button
              {...props}
              variant="outlineLight"
              className="!w-[80px] min-w-[80px] mt-4 py-[8px] flex-row flex justify-between items-center pl-[6px] pr-[12px] !min-h-[36px] !h-[36px]"
            >
              <MaterialIcon name="attach_file" size={20} />
              <Text className="text-body-12 ml-[4px]">Attach</Text>
            </Button>
          )}
        </FileButton>
      )}
      {attachment && (
        <div className="mt-4 relative w-full">
          {attachment.type.includes("image") && <AttachmentImageThumbnail />}
          {attachment.type.includes("pdf") && <AttachmentPdfThumbnail />}
        </div>
      )}
    </>
  );
}

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

import { Card } from "@mantine/core";
import React from "react";

export default function DFTOSPage() {
  return (
    <Card>
      <div className="flex flex-col items-center justify-center p-8">
        <h1 className="mb-4 text-title-36">Terms of Service</h1>
        <div className="text-left text-lg text-body-14">
          <div
            id="tos-welcome-content"
            className="whitespace-pre-wrap p-4 text-body-14"
          >
            <p>By using Stax, you agree to the following:</p>
            <ul>
              <li>
                <strong>Stax is not a finished product</strong>; the dogfood
                version is available only to select internal users, and Stax may
                output content that is harmful, offensive, or biased.
              </li>
              <li>Your participation in the dogfood is voluntary.</li>
              <li>
                <strong>Stax is confidential</strong>; do not share any
                information about Stax, including output or reports generated in
                Stax, outside of Alphabet.
              </li>
              <li>
                You must comply with all applicable company policies when using
                Stax, including in connection with obtaining or using any
                third-party API keys.
              </li>
              <li>
                When you use Stax in connection with an API, use of that API is
                governed by the terms and privacy policies applicable to that
                service.
              </li>
              <li>
                You acknowledge that you have read and understand{" "}
                <a href="http://go/epp">
                  Google&apos;s Employee Privacy Policy
                </a>
                . Your interactions with Stax may be used to provide, improve,
                and develop Google products and services and machine learning
                technologies.
              </li>
              <li>
                Please also refer to the{" "}
                <a href="http://go/using-genai-internally">
                  Using GenAI Internally policy
                </a>
                .
              </li>
            </ul>
            <p>
              Please reach out to the team at{" "}
              <a href="mailto:stax@google.com">stax@google.com</a> with any
              questions or suggestions.
            </p>
          </div>
        </div>
      </div>
    </Card>
  );
}

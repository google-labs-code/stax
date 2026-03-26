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

import type { Config } from "jest";
import nextJest from "next/jest.js";

const createJestConfig = nextJest({
  // Provide the path to your Next.js app to load next.config.js and .env files in your test environment
  dir: "./",
});

// Add any custom config to be passed to Jest
const config: Config = {
  coverageProvider: "v8",
  testEnvironment: "jsdom",
  testMatch: ["<rootDir>/tests-unit/**/*.spec.tsx"],
  transform: {
    "^.+\\.(js|jsx|ts|tsx)$": "ts-jest",
  },
  setupFilesAfterEnv: ["<rootDir>/tests-unit/setup.js"],
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/$1",
    "\\.svg$": "<rootDir>/test-tools/svg.js",
  },
  coverageDirectory: "<rootDir>/jest-unit-coverage/",
  collectCoverageFrom: [
    "<rootDir>/app/**/*.tsx",
    "<rootDir>/components/**/*.tsx",
   "<rootDir>/utils/**/*.{ts,tsx}",
    "!<rootDir>/components/icons/**",
  ],
  testPathIgnorePatterns: ["<rootDir>/components/icons/"],
  coveragePathIgnorePatterns: ["types.ts"],
  coverageThreshold: {
    global: {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80,
    },
  },
  // Add test timeout and other configurations
  testTimeout: 10000,
  // Suppress console warnings during tests
  silent: false,
  verbose: true,
};

async function finalJestConfig() {
  // createJestConfig is exported this way to ensure that next/jest can load the Next.js config which is async
  const nextJestConfig: any = await createJestConfig(config)();

  // /node_modules/ is the first pattern, so overwrite it with the correct version
  nextJestConfig.transformIgnorePatterns[0] =
    "/node_modules/(?!(react-markdown))/";

  return nextJestConfig;
}

export default finalJestConfig;

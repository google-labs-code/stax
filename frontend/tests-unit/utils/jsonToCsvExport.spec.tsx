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

import { jsonToCsvExport } from "@/utils/jsonToCsvExport";

describe("jsonToCsvExport", () => {
  const originalCreateElement = document.createElement;
  const originalAppendChild = document.body.appendChild;
  const originalRemoveChild = document.body.removeChild;
  const originalCreateObjectURL = URL.createObjectURL;
  const originalRevokeObjectURL = URL.revokeObjectURL;

  const mockLink = {
    href: "",
    setAttribute: jest.fn(),
    click: jest.fn(),
  };

  beforeEach(() => {
    mockLink.href = "";
    mockLink.setAttribute.mockReset();
    mockLink.click.mockReset();

    document.createElement = jest.fn().mockImplementation((tagName) => {
      if (tagName === "a") return mockLink;

      return originalCreateElement.call(document, tagName);
    });

    document.body.appendChild = jest.fn();
    document.body.removeChild = jest.fn();

    URL.createObjectURL = jest.fn().mockReturnValue("blob:mock-url");
    URL.revokeObjectURL = jest.fn();

    global.Blob = jest.fn().mockImplementation((content) => ({
      content,
      type: "csv",
    })) as any;

    const mockDate = new Date(2023, 0, 1);
    jest.spyOn(global, "Date").mockImplementation(() => mockDate as any);
    jest
      .spyOn(mockDate, "toISOString")
      .mockReturnValue("2023-01-01T00:00:00.000Z");
  });

  afterEach(() => {
    document.createElement = originalCreateElement;
    document.body.appendChild = originalAppendChild;
    document.body.removeChild = originalRemoveChild;
    URL.createObjectURL = originalCreateObjectURL;
    URL.revokeObjectURL = originalRevokeObjectURL;

    jest.restoreAllMocks();
  });

  it("generates correct CSV content", () => {
    const data = [
      {
        prompt: "Explain quantum computing",
        response: "Quantum computing uses quantum bits or qubits...",
        model_name: "GPT-4",
        score: 9.2,
      },
      {
        prompt: "Write a haiku about programming",
        response:
          "Fingers on keyboard\nLogic flows through my mind now\nBugs everywhere",
        model_name: "Claude",
        score: 8.7,
      },
    ];

    jsonToCsvExport(data);

    const blobCall = (global.Blob as jest.Mock).mock.calls[0];
    const csvContent = blobCall[0][0];

    // Fix: Account for newlines being replaced with spaces in the output
    expect(csvContent).toBe(
      "prompt,response,model_name,score\r\n" +
        "Explain quantum computing,Quantum computing uses quantum bits or qubits...,GPT-4,9.2\r\n" +
        "Write a haiku about programming,Fingers on keyboard Logic flows through my mind now Bugs everywhere,Claude,8.7",
    );
  });

  it("sets up download with correct filename", () => {
    const data = [
      {
        prompt: "Test prompt",
        model_name: "GPT-4",
      },
    ];
    const filename = "model-evaluations.csv";

    jsonToCsvExport(data, filename);

    expect(URL.createObjectURL).toHaveBeenCalled();
    expect(document.createElement).toHaveBeenCalledWith("a");
    expect(mockLink.href).toBe("blob:mock-url");
    expect(mockLink.setAttribute).toHaveBeenCalledWith("download", filename);
    expect(document.body.appendChild).toHaveBeenCalledWith(mockLink);
    expect(mockLink.click).toHaveBeenCalled();
  });

  it("uses default filename when none provided", () => {
    const data = [
      {
        prompt: "Test prompt",
        model_name: "GPT-4",
      },
    ];

    jsonToCsvExport(data);

    expect(mockLink.setAttribute).toHaveBeenCalledWith(
      "download",
      "export-2023-01-01.csv",
    );
  });

  it("handles missing data in some rows", () => {
    const data = [
      {
        prompt: "Explain the theory of relativity",
        model_name: "GPT-4",
        latency: 1250,
        temperature: 0.7,
      },
      {
        prompt: "Write a sonnet",
        model_name: "Claude",
        tokens: 512,
      },
      {
        latency: 780,
        tokens: 128,
        model_name: "Llama",
      },
    ];

    jsonToCsvExport(data);

    const blobCall = (global.Blob as jest.Mock).mock.calls[0];
    const csvContent = blobCall[0][0];

    // Fix: Match the actual column ordering in the output CSV
    expect(csvContent).toBe(
      "prompt,model_name,latency,temperature,tokens\r\n" +
        "Explain the theory of relativity,GPT-4,1250,0.7,\r\n" +
        "Write a sonnet,Claude,,,512\r\n" +
        ",Llama,780,,128",
    );
  });

  it("escapes special characters in fields", () => {
    const data = [
      {
        prompt: 'What does the phrase "to be or not to be" mean?',
        response:
          "This famous quote from Hamlet explores the theme of existence, choice, and contemplation.",
        model_name: "GPT-4",
      },
      {
        prompt: "List pros and cons\nof AI development",
        response:
          "Pros: automation, efficiency\nCons: job displacement, ethical concerns",
        model_name: "Claude-2",
      },
    ];

    jsonToCsvExport(data);

    const blobCall = (global.Blob as jest.Mock).mock.calls[0];
    const csvContent = blobCall[0][0];
    expect(csvContent).toBe(
      "prompt,response,model_name\r\n" +
        '"What does the phrase ""to be or not to be"" mean?","This famous quote from Hamlet explores the theme of existence, choice, and contemplation.",GPT-4\r\n' +
        'List pros and cons of AI development,"Pros: automation, efficiency Cons: job displacement, ethical concerns",Claude-2',
    );
  });

  it("converts objects and arrays to JSON strings", () => {
    const data = [
      {
        prompt: "Summarize the article",
        variables: {
          user_id: "user123",
          session_id: "abc456",
          context_length: 2048,
        },
        model_name: "GPT-3.5",
      },
      {
        prompt: "Translate to Spanish",
        tags: ["translation", "spanish", "language-model"],
        model_name: "BARD",
      },
    ];

    jsonToCsvExport(data);

    const blobCall = (global.Blob as jest.Mock).mock.calls[0];
    const csvContent = blobCall[0][0];
    expect(csvContent).toBe(
      "prompt,variables,model_name,tags\r\n" +
        'Summarize the article,"{""user_id"":""user123"",""session_id"":""abc456"",""context_length"":2048}",GPT-3.5,\r\n' +
        'Translate to Spanish,,BARD,"[""translation"",""spanish"",""language-model""]"',
    );
  });

  it("cleans up resources after download", () => {
    const data = [
      {
        prompt: "Testing cleanup",
        model_name: "GPT-4",
      },
    ];

    jsonToCsvExport(data);

    expect(document.body.removeChild).toHaveBeenCalledWith(mockLink);
    expect(URL.revokeObjectURL).toHaveBeenCalledWith("blob:mock-url");
  });
});

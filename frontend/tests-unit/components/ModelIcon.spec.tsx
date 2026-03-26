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

import { ModelIcon } from "@/components/ModelIcon";
import { Provider } from "@/types";
import { render, screen } from "@testing-library/react";

jest.mock("@/components/icons/GoogleIcon", () => {
  const MockComponent = () => <div>GoogleIcon</div>;
  MockComponent.displayName = "GoogleIcon";

  return MockComponent;
});

jest.mock("@/components/icons/OpenAiIconNew", () => {
  const MockComponent = () => <div>OpenAiIconNew</div>;
  MockComponent.displayName = "OpenAiIconNew";

  return MockComponent;
});

jest.mock("@/components/icons/AnthropicsAiIcon", () => {
  const MockComponent = () => <div>AnthropicsAiIcon</div>;
  MockComponent.displayName = "AnthropicsAiIcon";

  return MockComponent;
});

jest.mock("@/components/icons/MistralAiIcon", () => {
  const MockComponent = () => <div>MistralAiIcon</div>;
  MockComponent.displayName = "MistralAiIcon";

  return MockComponent;
});

jest.mock("@/components/icons/GrokAiIcon", () => {
  const MockComponent = () => <div>GrokAiIcon</div>;
  MockComponent.displayName = "GrokAiIcon";

  return MockComponent;
});

jest.mock("@/components/icons/DeepseekAiIcon", () => {
  const MockComponent = () => <div>DeepseekAiIcon</div>;
  MockComponent.displayName = "DeepseekAiIcon";

  return MockComponent;
});

jest.mock("@/components/icons/MetaIcon", () => {
  const MockComponent = () => <div>MetaIcon</div>;
  MockComponent.displayName = "MetaIcon";

  return MockComponent;
});

jest.mock("@/components/icons/OllamaAiIcon", () => {
  const MockComponent = () => <div>OllamaAiIcon</div>;
  MockComponent.displayName = "OllamaAiIcon";

  return MockComponent;
});

jest.mock("@/components/icons/HugginFaceAiIcon", () => {
  const MockComponent = () => <div>HugginFaceAiIcon</div>;
  MockComponent.displayName = "HugginFaceAiIcon";

  return MockComponent;
});

describe("ModelIcon", () => {
  it("renders GoogleIcon when provider is GOOGLE", () => {
    render(<ModelIcon provider={Provider.GOOGLE} />);
    expect(screen.getByText("GoogleIcon")).toBeInTheDocument();
  });

  it("renders OpenAiIconNew when provider is OPENAI", () => {
    render(<ModelIcon provider={Provider.OPENAI} />);
    expect(screen.getByText("OpenAiIconNew")).toBeInTheDocument();
  });

  it("renders AnthropicsAiIcon when provider is ANTHROPIC", () => {
    render(<ModelIcon provider={Provider.ANTHROPIC} />);
    expect(screen.getByText("AnthropicsAiIcon")).toBeInTheDocument();
  });

  it("renders MistralAiIcon when provider is MISTRAL", () => {
    render(<ModelIcon provider={Provider.MISTRAL} />);
    expect(screen.getByText("MistralAiIcon")).toBeInTheDocument();
  });

  it("renders GrokAiIcon when provider is GROK", () => {
    render(<ModelIcon provider={Provider.GROK} />);
    expect(screen.getByText("GrokAiIcon")).toBeInTheDocument();
  });

  it("renders DeepseekAiIcon when provider is DEEPSEEK", () => {
    render(<ModelIcon provider={Provider.DEEPSEEK} />);
    expect(screen.getByText("DeepseekAiIcon")).toBeInTheDocument();
  });

  it("renders MetaIcon when provider is LLAMA", () => {
    render(<ModelIcon provider={Provider.LLAMA} />);
    expect(screen.getByText("MetaIcon")).toBeInTheDocument();
  });

  it("renders OllamaAiIcon when provider is OLLAMA", () => {
    render(<ModelIcon provider={Provider.OLLAMA} />);
    expect(screen.getByText("OllamaAiIcon")).toBeInTheDocument();
  });

  it("renders HugginFaceAiIcon when provider is HUGGINGFACE", () => {
    render(<ModelIcon provider={Provider.HUGGINGFACE} />);
    expect(screen.getByText("HugginFaceAiIcon")).toBeInTheDocument();
  });

  it("renders nothing when provider is undefined or unmatched", () => {
    const { container } = render(<ModelIcon provider={undefined as any} />);
    expect(container).toBeEmptyDOMElement();
  });
});

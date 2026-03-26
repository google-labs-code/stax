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

import CreateAiEvaluatorCard from "@/components/CreateAiEvaluatorCard";
import { fireEvent, screen } from "@testing-library/react";

import { testRenderLite } from "../render";

describe("CreateAiEvaluatorCard", () => {
  it("renders default state", () => {
    testRenderLite(<CreateAiEvaluatorCard />);
    expect(
      screen.getByText("Create Evaluator Prompt with AI"),
    ).toBeInTheDocument();
    expect(
      screen.getByPlaceholderText(
        "AI-generated criterion description goes here",
      ),
    ).toBeInTheDocument();
  });

  it("renders loading state", () => {
    testRenderLite(<CreateAiEvaluatorCard isLoading />);
    expect(screen.getByText("Writing Criterion...")).toBeInTheDocument();
    expect(screen.getByRole("textbox")).toHaveAttribute("readonly");
  });

  it("calls onTextChange", () => {
    const handleTextChange = jest.fn();
    testRenderLite(<CreateAiEvaluatorCard onTextChange={handleTextChange} />);
    fireEvent.change(screen.getByRole("textbox"), {
      target: { value: "test" },
    });
    expect(handleTextChange).toHaveBeenCalledWith("test");
  });

  it("calls onSendClick", () => {
    const handleSendClick = jest.fn();
    testRenderLite(<CreateAiEvaluatorCard onSendClick={handleSendClick} />);
    const button = screen.getByRole("button");
    fireEvent.click(button);
    expect(handleSendClick).toHaveBeenCalled();
  });

  it("calls onCancelClick when loading", () => {
    const handleCancelClick = jest.fn();
    testRenderLite(
      <CreateAiEvaluatorCard isLoading onCancelClick={handleCancelClick} />,
    );
    fireEvent.click(screen.getByRole("button"));
    expect(handleCancelClick).toHaveBeenCalled();
  });

  it("disables segment control when loading", () => {
    testRenderLite(<CreateAiEvaluatorCard isLoading />);
    const control = screen.getByRole("radiogroup");
    expect(control).toHaveClass("pointer-events-none");
  });

  it("calls onEditClick when edit icon is clicked", () => {
    const handleEditClick = jest.fn();
    const { container } = testRenderLite(<CreateAiEvaluatorCard onEditClick={handleEditClick} />);
    
    const editIconContainer = container.querySelector(".cursor-pointer");
    if (editIconContainer) {
      fireEvent.click(editIconContainer);
      expect(handleEditClick).toHaveBeenCalled();
    }
  });

  it("changes placeholder text on focus and blur", () => {
    testRenderLite(<CreateAiEvaluatorCard />);
    const textarea = screen.getByRole("textbox");
    
    expect(textarea).toHaveAttribute(
      "placeholder", 
      "AI-generated criterion description goes here"
    );
    
    fireEvent.focus(textarea);
    expect(textarea).toHaveAttribute(
      "placeholder", 
      "Describe the criterion you're trying to add to the rubric.."
    );
    
    fireEvent.blur(textarea);
    expect(textarea).toHaveAttribute(
      "placeholder", 
      "AI-generated criterion description goes here"
    );
  });

  it("blocks text changes when loading", () => {
    const handleTextChange = jest.fn();
    testRenderLite(
      <CreateAiEvaluatorCard isLoading onTextChange={handleTextChange} />
    );
    
    const textarea = screen.getByRole("textbox");
    fireEvent.change(textarea, { target: { value: "test change during loading" } });
    
    expect(handleTextChange).not.toHaveBeenCalled();
  });

  it("initializes with the provided initialValue", () => {
    const initialText = "Initial criterion text";
    testRenderLite(<CreateAiEvaluatorCard initialValue={initialText} />);
    
    const textarea = screen.getByRole("textbox");
    expect(textarea).toHaveValue(initialText);
  });

  it("changes segment when clicked", () => {
    testRenderLite(<CreateAiEvaluatorCard />);
    
    const notesSegment = screen.getByRole("radio", { name: "" });
    fireEvent.click(notesSegment);
    
    expect(notesSegment).toBeChecked();
  });
});
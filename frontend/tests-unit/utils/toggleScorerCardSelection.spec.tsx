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

import { ScorerCardsProps } from "@/types";
import { toggleCardSelection } from "@/utils/toggleScorerCardSelection";

describe("toggleCardSelection", () => {
  it("adds a card when it's not already selected", () => {
    const initialSelectedCards: ScorerCardsProps[] = [
      { id: "card1", title: "Card 1" },
    ];
    const setSelectedCards = jest.fn();

    toggleCardSelection("card2", "Card 2", setSelectedCards);

    expect(setSelectedCards).toHaveBeenCalled();
    const updateFunction = setSelectedCards.mock.calls[0][0];

    const result = updateFunction(initialSelectedCards);
    expect(result).toEqual([
      { id: "card1", title: "Card 1" },
      { id: "card2", title: "Card 2" },
    ]);
  });

  it("removes a card when it's already selected", () => {
    const initialSelectedCards: ScorerCardsProps[] = [
      { id: "card1", title: "Card 1" },
      { id: "card2", title: "Card 2" },
    ];
    const setSelectedCards = jest.fn();

    toggleCardSelection("card1", "Card 1", setSelectedCards);

    expect(setSelectedCards).toHaveBeenCalled();
    const updateFunction = setSelectedCards.mock.calls[0][0];
    const result = updateFunction(initialSelectedCards);
    expect(result).toEqual([{ id: "card2", title: "Card 2" }]);
  });

  it("adds a card to an empty selected cards array", () => {
    const initialSelectedCards: ScorerCardsProps[] = [];
    const setSelectedCards = jest.fn();

    toggleCardSelection("card1", "Card 1", setSelectedCards);

    expect(setSelectedCards).toHaveBeenCalled();
    const updateFunction = setSelectedCards.mock.calls[0][0];
    const result = updateFunction(initialSelectedCards);
    expect(result).toEqual([{ id: "card1", title: "Card 1" }]);
  });

  it("preserves other cards when removing a card", () => {
    const initialSelectedCards: ScorerCardsProps[] = [
      { id: "card1", title: "Card 1" },
      { id: "card2", title: "Card 2" },
      { id: "card3", title: "Card 3" },
    ];
    const setSelectedCards = jest.fn();

    toggleCardSelection("card2", "Card 2", setSelectedCards);

    expect(setSelectedCards).toHaveBeenCalled();
    const updateFunction = setSelectedCards.mock.calls[0][0];
    const result = updateFunction(initialSelectedCards);
    expect(result).toEqual([
      { id: "card1", title: "Card 1" },
      { id: "card3", title: "Card 3" },
    ]);
  });

  it("preserves other cards when adding a card", () => {
    const initialSelectedCards: ScorerCardsProps[] = [
      { id: "card1", title: "Card 1" },
      { id: "card3", title: "Card 3" },
    ];
    const setSelectedCards = jest.fn();

    toggleCardSelection("card4", "Card 4", setSelectedCards);

    expect(setSelectedCards).toHaveBeenCalled();
    const updateFunction = setSelectedCards.mock.calls[0][0];
    const result = updateFunction(initialSelectedCards);
    expect(result).toEqual([
      { id: "card1", title: "Card 1" },
      { id: "card3", title: "Card 3" },
      { id: "card4", title: "Card 4" },
    ]);
  });

  it("ignores title when card with same ID is already selected", () => {
    const initialSelectedCards: ScorerCardsProps[] = [
      { id: "card1", title: "Original Title" },
    ];
    const setSelectedCards = jest.fn();

    toggleCardSelection("card1", "New Title", setSelectedCards);

    expect(setSelectedCards).toHaveBeenCalled();
    const updateFunction = setSelectedCards.mock.calls[0][0];
    const result = updateFunction(initialSelectedCards);

    expect(result).toEqual([]);
  });
});

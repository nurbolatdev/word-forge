-- Denormalize lemma onto user_cards for display without JOIN across modules
ALTER TABLE user_cards ADD COLUMN lemma TEXT NOT NULL DEFAULT '';

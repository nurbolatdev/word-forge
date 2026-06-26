-- Links each review to the quiz round it belongs to (for progress tracking)
ALTER TABLE reviews ADD COLUMN round_id BIGINT REFERENCES quiz_rounds(id);

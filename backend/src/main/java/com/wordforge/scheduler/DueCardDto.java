package com.wordforge.scheduler;

import java.time.OffsetDateTime;

record DueCardDto(
        Long cardId,
        Long userId,
        String aspectScope,
        Double stability,
        Double difficulty,
        OffsetDateTime nextDueAt,
        int reps
) {
    static DueCardDto from(CardMemoryState s) {
        return new DueCardDto(
                s.getCardId(), s.getUserId(), s.getAspectScope(),
                s.getStability(), s.getFsrsDifficulty(), s.getNextDueAt(), s.getReps()
        );
    }
}

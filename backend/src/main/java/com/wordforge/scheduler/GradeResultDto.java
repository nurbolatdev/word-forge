package com.wordforge.scheduler;

import java.time.OffsetDateTime;

public record GradeResultDto(
        Long cardId,
        int grade,
        boolean correct,
        double stability,
        double difficulty,
        OffsetDateTime nextDueAt,
        int reps
) {
    static GradeResultDto from(CardMemoryState s, int grade, boolean correct) {
        return new GradeResultDto(
                s.getCardId(), grade, correct,
                s.getStability(), s.getFsrsDifficulty(),
                s.getNextDueAt(), s.getReps()
        );
    }
}

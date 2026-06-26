package com.wordforge.quiz;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

record QuizRoundDto(
        Long id,
        Long userId,
        List<Long> cardIds,
        int totalCards,
        int answeredCards,
        boolean finished,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt
) {
    static QuizRoundDto from(QuizRound round, int answeredCards) {
        return new QuizRoundDto(
                round.getId(), round.getUserId(),
                Arrays.asList(round.getCardIds()),
                round.getCardIds().length,
                answeredCards,
                round.getFinishedAt() != null,
                round.getStartedAt(), round.getFinishedAt()
        );
    }
}

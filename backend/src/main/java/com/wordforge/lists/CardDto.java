package com.wordforge.lists;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

record CardDto(
        Long id,
        Long listId,
        Long userId,
        Long wordId,
        String lemma,
        List<Long> chosenTranslationIds,
        String status,
        OffsetDateTime createdAt
) {
    static CardDto from(UserCard card) {
        return new CardDto(
                card.getId(),
                card.getListId(),
                card.getUserId(),
                card.getWordId(),
                card.getLemma(),
                card.getChosenTranslationIds() == null
                        ? List.of()
                        : Arrays.asList(card.getChosenTranslationIds()),
                card.getStatus(),
                card.getCreatedAt()
        );
    }
}

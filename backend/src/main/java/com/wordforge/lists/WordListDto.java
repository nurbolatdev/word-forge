package com.wordforge.lists;

import java.time.OffsetDateTime;

record WordListDto(
        Long id,
        Long userId,
        String title,
        String sourceLang,
        String targetLang,
        int wordCount,
        OffsetDateTime createdAt
) {
    static WordListDto from(WordList list, int wordCount) {
        return new WordListDto(
                list.getId(), list.getUserId(), list.getTitle(),
                list.getSourceLang(), list.getTargetLang(),
                wordCount, list.getCreatedAt()
        );
    }
}

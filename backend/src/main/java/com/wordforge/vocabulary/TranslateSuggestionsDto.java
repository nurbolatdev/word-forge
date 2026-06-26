package com.wordforge.vocabulary;

import java.util.List;

record TranslateSuggestionsDto(
        Long wordId,
        String lemma,
        String sourceLang,
        String targetLang,
        List<TranslationOptionDto> suggestions
) {
    record TranslationOptionDto(Long id, String text, String provider) {}
}

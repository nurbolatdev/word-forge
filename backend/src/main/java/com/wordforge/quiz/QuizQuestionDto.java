package com.wordforge.quiz;

import java.util.List;

record QuizQuestionDto(
        Long cardId,
        String lemma,
        String promptText,
        String direction,
        int questionIndex,
        int totalCards,
        String modality,
        String clozeText,
        List<OptionDto> options
) {
    record OptionDto(Long translationId, String text) {}
}

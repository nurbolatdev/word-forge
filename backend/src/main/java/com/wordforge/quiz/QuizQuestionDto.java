package com.wordforge.quiz;

import java.util.List;

record QuizQuestionDto(
        Long cardId,
        String lemma,
        int questionIndex,
        int totalCards,
        List<OptionDto> options
) {
    record OptionDto(Long translationId, String text) {}
}

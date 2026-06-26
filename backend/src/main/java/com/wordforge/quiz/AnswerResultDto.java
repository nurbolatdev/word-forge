package com.wordforge.quiz;

record AnswerResultDto(
        Long cardId,
        boolean correct,
        int grade,
        Long correctTranslationId,
        String correctTranslationText,
        boolean roundFinished,
        QuizQuestionDto nextQuestion
) {}

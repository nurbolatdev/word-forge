package com.wordforge.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz/rounds")
class QuizController {

    private final QuizService service;

    QuizController(QuizService service) {
        this.service = service;
    }

    @PostMapping
    QuizRoundDto startRound(@RequestAttribute Long userId,
                            @Valid @RequestBody StartRoundRequest req) {
        String modality = req.modality() != null ? req.modality() : "MCQ";
        String direction = req.direction() != null ? req.direction() : "EN_RU";
        return service.startRound(userId, req.cardIds(), modality, direction);
    }

    @GetMapping("/{roundId}/question")
    QuizQuestionDto getQuestion(@PathVariable Long roundId, @RequestAttribute Long userId) {
        return service.getQuestion(roundId, userId);
    }

    @PostMapping("/{roundId}/answer")
    AnswerResultDto submitAnswer(@PathVariable Long roundId,
                                @RequestAttribute Long userId,
                                @Valid @RequestBody AnswerRequest req) {
        return service.submitAnswer(roundId, userId, req.cardId(),
                req.chosenTranslationId(), req.typedAnswer(), req.responseTimeMs());
    }

    record StartRoundRequest(
            @NotEmpty List<Long> cardIds,
            String modality,
            String direction
    ) {}

    record AnswerRequest(
            @NotNull Long cardId,
            Long chosenTranslationId,
            String typedAnswer,
            @NotNull @Min(0) Long responseTimeMs
    ) {}
}

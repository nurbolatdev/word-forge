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
    QuizRoundDto startRound(@RequestParam Long userId,
                            @Valid @RequestBody StartRoundRequest req) {
        return service.startRound(userId, req.cardIds());
    }

    @GetMapping("/{roundId}/question")
    QuizQuestionDto getQuestion(@PathVariable Long roundId, @RequestParam Long userId) {
        return service.getQuestion(roundId, userId);
    }

    @PostMapping("/{roundId}/answer")
    AnswerResultDto submitAnswer(@PathVariable Long roundId,
                                @RequestParam Long userId,
                                @Valid @RequestBody AnswerRequest req) {
        return service.submitAnswer(roundId, userId, req.cardId(),
                req.chosenTranslationId(), req.responseTimeMs());
    }

    record StartRoundRequest(@NotEmpty List<Long> cardIds) {}

    record AnswerRequest(
            @NotNull Long cardId,
            @NotNull Long chosenTranslationId,
            @NotNull @Min(0) Long responseTimeMs
    ) {}
}

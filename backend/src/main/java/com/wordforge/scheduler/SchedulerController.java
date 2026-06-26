package com.wordforge.scheduler;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
class SchedulerController {

    private final SchedulerService service;

    SchedulerController(SchedulerService service) {
        this.service = service;
    }

    @GetMapping("/due")
    List<DueCardDto> getDue(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return service.getDue(userId, limit);
    }

    @PostMapping("/grade")
    GradeResultDto grade(@RequestAttribute Long userId,
                         @Valid @RequestBody GradeRequest req) {
        return service.grade(req.cardId(), userId, req.correct(), req.responseTimeMs());
    }

    record GradeRequest(
            @NotNull Long cardId,
            @NotNull Boolean correct,
            @NotNull @Min(0) Long responseTimeMs
    ) {}
}

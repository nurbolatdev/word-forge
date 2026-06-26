package com.wordforge.scheduler;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SchedulerService {

    private final CardMemoryStateRepository stateRepo;
    private final FsrsAlgorithm fsrs;
    private final GradingService grading;

    SchedulerService(CardMemoryStateRepository stateRepo,
                     FsrsAlgorithm fsrs,
                     GradingService grading) {
        this.stateRepo = stateRepo;
        this.fsrs = fsrs;
        this.grading = grading;
    }

    List<DueCardDto> getDue(Long userId, int limit) {
        return stateRepo
                .findByUserIdAndNextDueAtLessThanEqualOrderByNextDueAtAsc(userId, OffsetDateTime.now())
                .stream()
                .limit(limit)
                .map(DueCardDto::from)
                .toList();
    }

    @Transactional
    public GradeResultDto grade(Long cardId, Long userId, boolean correct, long responseTimeMs) {
        int g = grading.grade(correct, responseTimeMs);

        CardMemoryState state = stateRepo
                .findByCardIdAndAspectScope(cardId, "ALL")
                .orElseGet(() -> stateRepo.save(new CardMemoryState(cardId, userId, OffsetDateTime.now())));

        FsrsAlgorithm.SchedulingResult result;
        if (state.getReps() == 0) {
            result = fsrs.scheduleNew(g);
        } else {
            long elapsedDays = Math.max(0,
                    ChronoUnit.DAYS.between(state.getLastReviewAt(), OffsetDateTime.now()));
            double r = fsrs.retrievability(
                    state.getStability() == null ? 1.0 : state.getStability(), elapsedDays);
            result = fsrs.scheduleReview(
                    state.getStability() == null ? 1.0 : state.getStability(),
                    state.getFsrsDifficulty() == null ? 5.0 : state.getFsrsDifficulty(),
                    r, g);
        }

        state.setStability(result.stability());
        state.setFsrsDifficulty(result.difficulty());
        state.setNextDueAt(result.nextDueAt());
        state.setLastReviewAt(OffsetDateTime.now());
        state.setReps(state.getReps() + 1);
        state.setFsrsParamsVersion(FsrsAlgorithm.FSRS_PARAMS_VERSION);
        stateRepo.save(state);

        return GradeResultDto.from(state, g, correct);
    }
}

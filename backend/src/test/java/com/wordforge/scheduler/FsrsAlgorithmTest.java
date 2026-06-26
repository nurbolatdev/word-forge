package com.wordforge.scheduler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FsrsAlgorithmTest {

    private final FsrsAlgorithm fsrs = new FsrsAlgorithm();

    @Test
    void newCardGrade4HasHigherStabilityThanGrade1() {
        var easy  = fsrs.scheduleNew(4);
        var again = fsrs.scheduleNew(1);
        assertThat(easy.stability()).isGreaterThan(again.stability());
    }

    @Test
    void newCardNextDueIsInFuture() {
        var result = fsrs.scheduleNew(3);
        assertThat(result.nextDueAt()).isAfter(java.time.OffsetDateTime.now());
    }

    @Test
    void reviewAfterRecallIncreasesStability() {
        var first  = fsrs.scheduleNew(3);
        var second = fsrs.scheduleReview(first.stability(), first.difficulty(), 0.9, 3);
        assertThat(second.stability()).isGreaterThan(first.stability());
    }

    @Test
    void retrievabilityDecreasesOverTime() {
        double s = 10.0;
        double r1 = fsrs.retrievability(s, 1);
        double r10 = fsrs.retrievability(s, 10);
        assertThat(r1).isGreaterThan(r10);
    }
}

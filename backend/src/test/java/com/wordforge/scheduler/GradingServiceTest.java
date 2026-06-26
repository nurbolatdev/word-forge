package com.wordforge.scheduler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GradingServiceTest {

    private final GradingService grading = new GradingService();

    @Test
    void incorrectAnswerIsAlways1() {
        assertThat(grading.grade(false, 500)).isEqualTo(1);
        assertThat(grading.grade(false, 15_000)).isEqualTo(1);
    }

    @Test
    void fastCorrectAnswerIsEasy() {
        assertThat(grading.grade(true, 1_000)).isEqualTo(4);
        assertThat(grading.grade(true, 2_999)).isEqualTo(4);
    }

    @Test
    void normalCorrectAnswerIsGood() {
        assertThat(grading.grade(true, 3_000)).isEqualTo(3);
        assertThat(grading.grade(true, 10_000)).isEqualTo(3);
    }

    @Test
    void slowCorrectAnswerIsHard() {
        assertThat(grading.grade(true, 10_001)).isEqualTo(2);
        assertThat(grading.grade(true, 30_000)).isEqualTo(2);
    }
}

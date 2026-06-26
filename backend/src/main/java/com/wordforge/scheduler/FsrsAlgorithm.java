package com.wordforge.scheduler;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * FSRS-4.5 algorithm implementation.
 * Reference: https://github.com/open-spaced-repetition/fsrs4anki/wiki/The-Algorithm
 */
@Component
class FsrsAlgorithm {

    // Default weights w[0..16] from FSRS-4.5 paper
    private static final double[] W = {
        0.4, 0.6, 2.4, 5.8,
        4.93, 0.94, 0.86, 0.01,
        1.49, 0.14, 0.94, 2.18,
        0.05, 0.34, 1.26, 0.29, 2.61
    };

    static final double TARGET_RETENTION = 0.9;
    static final int FSRS_PARAMS_VERSION = 1;

    record SchedulingResult(double stability, double difficulty, OffsetDateTime nextDueAt) {}

    /** Called when a card is seen for the first time (reps == 0). */
    SchedulingResult scheduleNew(int grade) {
        double d = initDifficulty(grade);
        double s = initStability(grade);
        return new SchedulingResult(s, d, nextDue(s));
    }

    /** Called on subsequent reviews (reps >= 1). */
    SchedulingResult scheduleReview(double prevStability, double prevDifficulty,
                                    double retrievability, int grade) {
        double d = nextDifficulty(prevDifficulty, grade);
        double s;
        if (grade == 1) {
            s = stabilityAfterForgetting(prevStability);
        } else {
            s = stabilityAfterRecall(prevStability, d, retrievability, grade);
        }
        return new SchedulingResult(s, d, nextDue(s));
    }

    double retrievability(double stability, long elapsedDays) {
        return Math.pow(1 + elapsedDays / (9 * stability), -1);
    }

    private double initDifficulty(int grade) {
        return clampD(W[4] - W[5] * (grade - 3));
    }

    private double initStability(int grade) {
        return Math.max(W[grade - 1], 0.1);
    }

    private double nextDifficulty(double d, int grade) {
        double delta = -W[6] * (grade - 3);
        double dPrime = d + delta * ((10 - d) / 9.0);
        return clampD(meanReversion(initDifficulty(4), dPrime));
    }

    private double stabilityAfterRecall(double s, double d, double r, int grade) {
        double hardPenalty = (grade == 2) ? W[15] : 1.0;
        double easyBonus  = (grade == 4) ? W[16] : 1.0;
        return s * (Math.exp(W[8]) * (11 - d)
                * Math.pow(s, -W[9])
                * (Math.exp((1 - r) * W[10]) - 1)
                * hardPenalty * easyBonus + 1);
    }

    private double stabilityAfterForgetting(double s) {
        return W[11] * Math.pow(s, -W[12]) * (Math.exp((1 - TARGET_RETENTION) * W[13]) - 1);
    }

    private double meanReversion(double init, double current) {
        return W[7] * init + (1 - W[7]) * current;
    }

    private OffsetDateTime nextDue(double stability) {
        long days = Math.max(1, Math.round(stability * Math.log(TARGET_RETENTION) / Math.log(1 + 1.0 / (9 * stability))));
        return OffsetDateTime.now().plusDays(days);
    }

    private double clampD(double d) {
        return Math.max(1.0, Math.min(10.0, d));
    }
}

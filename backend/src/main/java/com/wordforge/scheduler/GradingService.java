package com.wordforge.scheduler;

import org.springframework.stereotype.Service;

@Service
class GradingService {

    private static final long EASY_THRESHOLD_MS  = 3_000;
    private static final long GOOD_THRESHOLD_MS  = 10_000;

    /**
     * Converts quiz signals into an FSRS grade (1–4).
     *
     * 1 = Again  — wrong answer
     * 2 = Hard   — correct but slow (> 10 s)
     * 3 = Good   — correct, normal speed (3–10 s)
     * 4 = Easy   — correct and fast (< 3 s)
     */
    int grade(boolean correct, long responseTimeMs) {
        if (!correct) return 1;
        if (responseTimeMs < EASY_THRESHOLD_MS)  return 4;
        if (responseTimeMs <= GOOD_THRESHOLD_MS) return 3;
        return 2;
    }
}

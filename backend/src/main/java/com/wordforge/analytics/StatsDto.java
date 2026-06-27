package com.wordforge.analytics;

record StatsDto(
        int totalWords,
        int dueToday,
        int reviewedToday,
        int streak,
        int totalReviews
) {}

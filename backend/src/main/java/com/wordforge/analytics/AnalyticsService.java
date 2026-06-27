package com.wordforge.analytics;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
class AnalyticsService {

    private final JdbcTemplate jdbc;

    AnalyticsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    StatsDto getStats(Long userId) {
        int totalWords = count(
                "SELECT COUNT(*) FROM user_cards WHERE user_id = ?", userId);

        int dueToday = count(
                "SELECT COUNT(*) FROM card_memory_states WHERE user_id = ? AND next_due_at <= NOW()", userId);

        int reviewedToday = count(
                "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND DATE(reviewed_at) = CURRENT_DATE", userId);

        int totalReviews = count(
                "SELECT COUNT(*) FROM reviews WHERE user_id = ?", userId);

        int streak = computeStreak(userId);

        return new StatsDto(totalWords, dueToday, reviewedToday, streak, totalReviews);
    }

    private int count(String sql, Long userId) {
        Integer result = jdbc.queryForObject(sql, Integer.class, userId);
        return result != null ? result : 0;
    }

    private int computeStreak(Long userId) {
        // Count consecutive days with at least one review, starting from today
        String sql = """
                WITH daily AS (
                    SELECT DISTINCT DATE(reviewed_at) AS d
                    FROM reviews
                    WHERE user_id = ?
                ),
                numbered AS (
                    SELECT d, ROW_NUMBER() OVER (ORDER BY d DESC) AS rn
                    FROM daily
                )
                SELECT COUNT(*) FROM numbered
                WHERE d = (CURRENT_DATE - CAST(rn - 1 AS INT))
                """;
        Integer result = jdbc.queryForObject(sql, Integer.class, userId);
        return result != null ? result : 0;
    }
}

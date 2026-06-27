package com.wordforge.analytics;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

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
        int dailyGoal = queryInt(
                "SELECT daily_goal FROM users WHERE id = ?", userId);

        return new StatsDto(totalWords, dueToday, reviewedToday, streak, totalReviews, dailyGoal);
    }

    List<ForecastDay> getForecast(Long userId) {
        String sql = """
                SELECT TO_CHAR(DATE(next_due_at), 'YYYY-MM-DD') AS due_date,
                       COUNT(*) AS card_count
                FROM card_memory_states
                WHERE user_id = ?
                  AND next_due_at >= CURRENT_DATE
                  AND next_due_at < CURRENT_DATE + INTERVAL '14 days'
                GROUP BY due_date
                ORDER BY due_date
                """;
        return jdbc.query(sql, (rs, i) ->
                new ForecastDay(rs.getString("due_date"), rs.getInt("card_count")), userId);
    }

    void updateGoal(Long userId, int dailyGoal) {
        if (dailyGoal < 1 || dailyGoal > 200) throw new IllegalArgumentException("Goal must be 1–200");
        jdbc.update("UPDATE users SET daily_goal = ? WHERE id = ?", dailyGoal, userId);
    }

    private int count(String sql, Long userId) {
        Integer result = jdbc.queryForObject(sql, Integer.class, userId);
        return result != null ? result : 0;
    }

    private int queryInt(String sql, Long userId) {
        Integer result = jdbc.queryForObject(sql, Integer.class, userId);
        return result != null ? result : 20;
    }

    private int computeStreak(Long userId) {
        String sql = """
                WITH daily AS (
                    SELECT DISTINCT DATE(reviewed_at) AS d
                    FROM reviews WHERE user_id = ?
                ),
                numbered AS (
                    SELECT d, ROW_NUMBER() OVER (ORDER BY d DESC) AS rn FROM daily
                )
                SELECT COUNT(*) FROM numbered
                WHERE d = (CURRENT_DATE - CAST(rn - 1 AS INT))
                """;
        Integer result = jdbc.queryForObject(sql, Integer.class, userId);
        return result != null ? result : 0;
    }
}

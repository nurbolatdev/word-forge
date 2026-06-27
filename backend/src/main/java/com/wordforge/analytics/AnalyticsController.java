package com.wordforge.analytics;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController {

    private final AnalyticsService service;

    AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/stats")
    StatsDto getStats(@RequestAttribute Long userId) {
        return service.getStats(userId);
    }

    @GetMapping("/forecast")
    List<ForecastDay> getForecast(@RequestAttribute Long userId) {
        return service.getForecast(userId);
    }

    @PatchMapping("/goal")
    void updateGoal(@RequestAttribute Long userId,
                    @RequestBody GoalRequest req) {
        service.updateGoal(userId, req.dailyGoal());
    }

    record GoalRequest(@Min(1) @Max(200) int dailyGoal) {}
}

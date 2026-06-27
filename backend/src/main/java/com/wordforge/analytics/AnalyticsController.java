package com.wordforge.analytics;

import org.springframework.web.bind.annotation.*;

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
}

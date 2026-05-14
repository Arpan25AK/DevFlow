package com.dev.analytics_service.controller;

import com.dev.analytics_service.dto.UserActivityRequest;
import com.dev.analytics_service.entity.UserActivityEvent;
import com.dev.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/users")
@RequiredArgsConstructor
public class UserActivityController {

    private final AnalyticsService analyticsService;

    @PostMapping("/activity")
    public ResponseEntity<String> recordActivity(@RequestBody UserActivityRequest request) {
        analyticsService.recordUserActivity(request);
        return ResponseEntity.ok("User activity recorded");
    }

    @GetMapping("/{userEmail}/activity")
    public ResponseEntity<List<UserActivityEvent>> getUserActivity(@PathVariable String userEmail) {
        return ResponseEntity.ok(analyticsService.getUserActivity(userEmail));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getUserActivitySummary() {
        return ResponseEntity.ok(analyticsService.getUserActivitySummary());
    }
}
package com.dev.analytics_service.controller;

import com.dev.analytics_service.dto.CodeReviewStatsRequest;
import com.dev.analytics_service.entity.CodeReviewStatsEvent;
import com.dev.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/reviews")
@RequiredArgsConstructor
public class CodeReviewStatsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/stats")
    public ResponseEntity<String> recordStats(@RequestBody CodeReviewStatsRequest request) {
        analyticsService.recordCodeReviewStats(request);
        return ResponseEntity.ok("Code review stats recorded");
    }

    @GetMapping("/{repoName}")
    public ResponseEntity<List<CodeReviewStatsEvent>> getByRepo(@PathVariable String repoName) {
        return ResponseEntity.ok(analyticsService.getCodeReviewsByRepo(repoName));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getSummary() {
        return ResponseEntity.ok(analyticsService.getCodeReviewSummary());
    }
}
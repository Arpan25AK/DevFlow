package com.dev.analytics_service.controller;

import com.dev.analytics_service.dto.PipelineStatsRequest;
import com.dev.analytics_service.entity.PipelineStatsEvent;
import com.dev.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/pipelines")
@RequiredArgsConstructor
public class PipelineStatsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/stats")
    public ResponseEntity<String> recordStats(@RequestBody PipelineStatsRequest request) {
        analyticsService.recordPipelineStats(request);
        return ResponseEntity.ok("Pipeline stats recorded");
    }

    @GetMapping("/{repoName}")
    public ResponseEntity<List<PipelineStatsEvent>> getByRepo(@PathVariable String repoName) {
        return ResponseEntity.ok(analyticsService.getPipelineStatsByRepo(repoName));
    }

    @GetMapping("/{repoName}/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable String repoName) {
        return ResponseEntity.ok(analyticsService.getPipelineSummary(repoName));
    }
}
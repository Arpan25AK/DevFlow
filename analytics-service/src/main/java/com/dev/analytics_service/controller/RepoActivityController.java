package com.dev.analytics_service.controller;

import com.dev.analytics_service.dto.RepoActivityRequest;
import com.dev.analytics_service.entity.RepoActivityEvent;
import com.dev.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/repos")
@RequiredArgsConstructor
public class RepoActivityController {

    private final AnalyticsService analyticsService;

    @PostMapping("/activity")
    public ResponseEntity<String> recordActivity(@RequestBody RepoActivityRequest request) {
        analyticsService.recordRepoActivity(request);
        return ResponseEntity.ok("Repo activity recorded");
    }

    @GetMapping("/owner/{ownerEmail}")
    public ResponseEntity<List<RepoActivityEvent>> getByOwner(@PathVariable String ownerEmail) {
        return ResponseEntity.ok(analyticsService.getRepoActivityByOwner(ownerEmail));
    }

    @GetMapping("/{repoName}")
    public ResponseEntity<List<RepoActivityEvent>> getByRepo(@PathVariable String repoName) {
        return ResponseEntity.ok(analyticsService.getRepoActivityByRepo(repoName));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getSummary() {
        return ResponseEntity.ok(analyticsService.getRepoActivitySummary());
    }
}
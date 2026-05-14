package com.dev.analytics_service.service;

import com.dev.analytics_service.dto.*;
import com.dev.analytics_service.entity.*;
import com.dev.analytics_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserActivityEventRepository userActivityRepo;
    private final RepoActivityEventRepository repoActivityRepo;
    private final PipelineStatsEventRepository pipelineStatsRepo;
    private final CodeReviewStatsEventRepository codeReviewStatsRepo;

    // --- User Activity ---
    public void recordUserActivity(UserActivityRequest request) {
        userActivityRepo.save(UserActivityEvent.builder()
                .userEmail(request.getUserEmail())
                .action(request.getAction())
                .timestamp(LocalDateTime.now())
                .build());
    }

    public List<UserActivityEvent> getUserActivity(String userEmail) {
        return userActivityRepo.findByUserEmailOrderByTimestampDesc(userEmail);
    }

    public Map<String, Long> getUserActivitySummary() {
        Map<String, Long> summary = new HashMap<>();
        summary.put("LOGIN", userActivityRepo.countByAction("LOGIN"));
        summary.put("LOGOUT", userActivityRepo.countByAction("LOGOUT"));
        summary.put("CREATE_REPO", userActivityRepo.countByAction("CREATE_REPO"));
        summary.put("UPLOAD_FILE", userActivityRepo.countByAction("UPLOAD_FILE"));
        return summary;
    }

    // --- Repo Activity ---
    public void recordRepoActivity(RepoActivityRequest request) {
        repoActivityRepo.save(RepoActivityEvent.builder()
                .ownerEmail(request.getOwnerEmail())
                .repoName(request.getRepoName())
                .action(request.getAction())
                .timestamp(LocalDateTime.now())
                .build());
    }

    public List<RepoActivityEvent> getRepoActivityByOwner(String ownerEmail) {
        return repoActivityRepo.findByOwnerEmailOrderByTimestampDesc(ownerEmail);
    }

    public List<RepoActivityEvent> getRepoActivityByRepo(String repoName) {
        return repoActivityRepo.findByRepoNameOrderByTimestampDesc(repoName);
    }

    public Map<String, Long> getRepoActivitySummary() {
        Map<String, Long> summary = new HashMap<>();
        summary.put("CREATED", repoActivityRepo.countByAction("CREATED"));
        summary.put("PUSH", repoActivityRepo.countByAction("PUSH"));
        summary.put("FILE_UPLOADED", repoActivityRepo.countByAction("FILE_UPLOADED"));
        summary.put("DELETED", repoActivityRepo.countByAction("DELETED"));
        return summary;
    }

    // --- Pipeline Stats ---
    public void recordPipelineStats(PipelineStatsRequest request) {
        pipelineStatsRepo.save(PipelineStatsEvent.builder()
                .ownerEmail(request.getOwnerEmail())
                .repoName(request.getRepoName())
                .status(request.getStatus())
                .durationSeconds(request.getDurationSeconds())
                .timestamp(LocalDateTime.now())
                .build());
    }

    public List<PipelineStatsEvent> getPipelineStatsByRepo(String repoName) {
        return pipelineStatsRepo.findByRepoNameOrderByTimestampDesc(repoName);
    }

    public Map<String, Object> getPipelineSummary(String repoName) {
        List<PipelineStatsEvent> pipelines = pipelineStatsRepo.findByRepoNameOrderByTimestampDesc(repoName);

        double avg = pipelines.stream()
                .mapToLong(PipelineStatsEvent::getDurationSeconds)
                .average()
                .orElse(0.0);

        Map<String, Object> summary = new HashMap<>();
        summary.put("SUCCESS", pipelineStatsRepo.countByStatus("SUCCESS"));
        summary.put("FAILED", pipelineStatsRepo.countByStatus("FAILED"));
        summary.put("RUNNING", pipelineStatsRepo.countByStatus("RUNNING"));
        summary.put("avgDurationSeconds", avg);
        return summary;
    }

    // --- Code Review Stats ---
    public void recordCodeReviewStats(CodeReviewStatsRequest request) {
        codeReviewStatsRepo.save(CodeReviewStatsEvent.builder()
                .repoName(request.getRepoName())
                .reviewerEmail(request.getReviewerEmail())
                .status(request.getStatus())
                .timestamp(LocalDateTime.now())
                .build());
    }

    public List<CodeReviewStatsEvent> getCodeReviewsByRepo(String repoName) {
        return codeReviewStatsRepo.findByRepoNameOrderByTimestampDesc(repoName);
    }

    public Map<String, Long> getCodeReviewSummary() {
        Map<String, Long> summary = new HashMap<>();
        summary.put("OPEN", codeReviewStatsRepo.countByStatus("OPEN"));
        summary.put("APPROVED", codeReviewStatsRepo.countByStatus("APPROVED"));
        summary.put("REJECTED", codeReviewStatsRepo.countByStatus("REJECTED"));
        summary.put("MERGED", codeReviewStatsRepo.countByStatus("MERGED"));
        return summary;
    }
}
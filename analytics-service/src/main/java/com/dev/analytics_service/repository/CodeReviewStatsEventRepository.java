package com.dev.analytics_service.repository;

import com.dev.analytics_service.entity.CodeReviewStatsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodeReviewStatsEventRepository extends JpaRepository<CodeReviewStatsEvent, Long> {
    List<CodeReviewStatsEvent> findByRepoNameOrderByTimestampDesc(String repoName);
    List<CodeReviewStatsEvent> findByReviewerEmailOrderByTimestampDesc(String reviewerEmail);
    Long countByStatus(String status);
}
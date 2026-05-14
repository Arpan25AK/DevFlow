package com.dev.analytics_service.repository;

import com.dev.analytics_service.entity.PipelineStatsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PipelineStatsEventRepository extends JpaRepository<PipelineStatsEvent, Long> {
    List<PipelineStatsEvent> findByOwnerEmailOrderByTimestampDesc(String ownerEmail);
    List<PipelineStatsEvent> findByRepoNameOrderByTimestampDesc(String repoName);
    Long countByStatus(String status);
}
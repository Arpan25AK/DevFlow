package com.dev.analytics_service.repository;

import com.dev.analytics_service.entity.RepoActivityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepoActivityEventRepository extends JpaRepository<RepoActivityEvent, Long> {
    List<RepoActivityEvent> findByOwnerEmailOrderByTimestampDesc(String ownerEmail);
    List<RepoActivityEvent> findByRepoNameOrderByTimestampDesc(String repoName);
    Long countByAction(String action);
}
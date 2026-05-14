package com.dev.analytics_service.repository;

import com.dev.analytics_service.entity.UserActivityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserActivityEventRepository extends JpaRepository<UserActivityEvent, Long> {
    List<UserActivityEvent> findByUserEmailOrderByTimestampDesc(String userEmail);
    Long countByAction(String action);
}
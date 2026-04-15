package com.dev.ci_cd_service.repo;

import com.dev.ci_cd_service.entity.PipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, UUID> {
    List<PipelineRun> findByOwnerEmailAndNameOrderByTimeStampDesc(String ownerEmail, String name);
}

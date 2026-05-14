package com.dev.analytics_service.dto;

import lombok.Data;

@Data
public class PipelineStatsRequest {
    private String ownerEmail;
    private String repoName;
    private String status;
    private Long durationSeconds;
}
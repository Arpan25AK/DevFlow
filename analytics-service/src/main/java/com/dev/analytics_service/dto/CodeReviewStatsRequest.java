package com.dev.analytics_service.dto;

import lombok.Data;

@Data
public class CodeReviewStatsRequest {
    private String repoName;
    private String reviewerEmail;
    private String status;
}
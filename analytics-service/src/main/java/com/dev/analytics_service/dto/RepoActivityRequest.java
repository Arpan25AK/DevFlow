package com.dev.analytics_service.dto;

import lombok.Data;

@Data
public class RepoActivityRequest {
    private String ownerEmail;
    private String repoName;
    private String action;
}
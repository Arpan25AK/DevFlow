package com.dev.analytics_service.dto;

import lombok.Data;

@Data
public class UserActivityRequest {
    private String userEmail;
    private String action;
}
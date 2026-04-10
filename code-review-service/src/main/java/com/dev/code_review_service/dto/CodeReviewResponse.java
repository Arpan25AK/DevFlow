package com.dev.code_review_service.dto;

import com.dev.code_review_service.entity.ReviewStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CodeReviewResponse (
        UUID id,
        UUID repositoryId,
        UUID pullrequestId,
        UUID reviewId,
        UUID authorId,
        String comments,
        ReviewStatus status,
        LocalDateTime createdAt
){
}

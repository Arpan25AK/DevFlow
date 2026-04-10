package com.dev.code_review_service.dto;

import java.util.UUID;

public record CodeReviewRequest(
        Long repositoryId,
        UUID pullrequestId,
        UUID authorId,
        String comments
) {
}

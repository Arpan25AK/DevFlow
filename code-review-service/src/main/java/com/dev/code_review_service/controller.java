package com.dev.code_review_service;

import com.dev.code_review_service.entity.CodeReview;
import com.dev.code_review_service.entity.ReviewStatus;
import com.dev.code_review_service.service.CodeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class controller {

    private final CodeReviewService codeReviewService;

    @PostMapping
    public ResponseEntity<CodeReview> createReview(@PathVariable CodeReview review) {
        CodeReview createdReview = codeReviewService.createReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    public ResponseEntity<List<CodeReview>> getReviewsByReviewerId(@PathVariable UUID reviewerId) {
        List<CodeReview> reviews = codeReviewService.getByReviewId(reviewerId);
        return ResponseEntity.ok(reviews);
    }

    public ResponseEntity<List<CodeReview>> getReviewsByPR(@PathVariable String pullrequestId) {
        List<CodeReview> reviews = codeReviewService.getByPullrequestId(pullrequestId);
        return ResponseEntity.ok(reviews);
    }

    public ResponseEntity<CodeReview> updateByReviewId(@PathVariable UUID reviewId, ReviewStatus newStatus){
        CodeReview updatedReview = codeReviewService.updateReview(reviewId, newStatus);
        return ResponseEntity.ok(updatedReview);
    }

}

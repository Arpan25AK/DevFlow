package com.dev.code_review_service.service;

import com.dev.code_review_service.entity.CodeReview;
import com.dev.code_review_service.entity.ReviewStatus;
import com.dev.code_review_service.repo.CodeServiceRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CodeReviewService {
    private final CodeServiceRepo codeServiceRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String REVIEW_TOPIC = "code-review-events";

    public CodeReview createReview(CodeReview review){
        if(review.getStatus() == null) review.setStatus(ReviewStatus.PENDING);

        CodeReview savedReview = codeServiceRepo.save(review);
        log.info("Saved new code review with ID: {}", savedReview.getId());

        kafkaTemplate.send(REVIEW_TOPIC, "New review created for PR: " + savedReview.getPullrequestId());

        return savedReview;
    }

    public List<CodeReview> getByReviewId(UUID reviewerID){
        return codeServiceRepo.findByReviewerId(reviewerID);
    }

    public List<CodeReview> getByPullrequestId(String pullrequestId){
        return codeServiceRepo.findByPullrequestId(pullrequestId);
    }

    public CodeReview updateReview(UUID reviewId, ReviewStatus newStatus){
        CodeReview existingReview = codeServiceRepo.findById(reviewId)
                .orElseThrow(() ->
                        new RuntimeException("\"Code review not found with ID: \" + reviewId"));

        existingReview.setStatus(newStatus);
        CodeReview updatedReview = codeServiceRepo.save(existingReview);

        log.info("Updated code review {} status to {}", reviewId, newStatus);
        kafkaTemplate.send(REVIEW_TOPIC, "Review " + reviewId + " status updated to " + newStatus);

        return updatedReview;

    }
}

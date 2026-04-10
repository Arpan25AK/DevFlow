package com.dev.code_review_service.service;

import com.dev.code_review_service.client.RepositoryServiceClient;
import com.dev.code_review_service.dto.CodeReviewRequest;
import com.dev.code_review_service.dto.CodeReviewResponse;
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
    private final RepositoryServiceClient serviceClient;

    private static final String REVIEW_TOPIC = "code-review-events";

    public CodeReviewResponse createReview(CodeReviewRequest request, UUID reviewId){
        boolean exists = serviceClient.checkRepositoryExists(request.repositoryId());

        if(!exists){
            throw new RuntimeException("repository error : no repo was found connected to the id");
        }

        CodeReview newReview = CodeReview.builder()
            .repositoryId(request.repositoryId()).
                pullrequestId(request.pullrequestId()).
                authorId(request.authorId()).
                reviewerId(reviewId).
                comments(request.comments()).
                status(ReviewStatus.PENDING).
                build();



        CodeReview savedReview = codeServiceRepo.save(newReview);
        log.info("Saved new code review with ID: {}", savedReview.getId());

        kafkaTemplate.send(REVIEW_TOPIC, "New review created for PR: " + savedReview.getPullrequestId());

        return new CodeReviewResponse(
                savedReview.getId(),
                savedReview.getRepositoryId(),
                savedReview.getPullrequestId(),
                savedReview.getReviewerId(),
                savedReview.getAuthorId(),
                savedReview.getComments(),
                savedReview.getStatus(),
                savedReview.getCreatedAt()
        );
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

package com.dev.code_review_service.repo;

import com.dev.code_review_service.entity.CodeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CodeServiceRepo extends JpaRepository<CodeReview, UUID> {

    List<CodeReview> getByRepositoryId(Long repositoryId);

    List<CodeReview> findByReviewerId(UUID reviewerId);

    List<CodeReview> findByPullrequestId(String pullrequestId);


}

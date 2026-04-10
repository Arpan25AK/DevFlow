package com.dev.code_review_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.UUID;

@Table(name="code_reviews")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CodeReview {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,name = "repository_id")
    private Long repositoryId;

    @Column(nullable = false, name = "reviewer_id")
    private UUID reviewerId;

    @Column(nullable = false, name=  "pullrequest_id")
    private UUID pullrequestId;

    @Column(nullable = false, name = "author_id")
    private UUID authorId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReviewStatus status;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

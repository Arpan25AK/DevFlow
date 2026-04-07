package com.dev.code_review_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name="code-reviews")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CodeReview {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,name = "repository-id")
    private UUID repositoryId;

    @Column(nullable = false, name = "reviewer-id")
    private UUID reviewerId;

    @Column(nullable = false, name=  "pullrequest-id")
    private UUID pullrequestId;

    @Column(nullable = false, name = "author-id")
    private UUID authorId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReviewStatus status;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @CreationTimestamp
    @Column(updatable = false, name = "created-at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated-at")
    private LocalDateTime updatedAt;
}

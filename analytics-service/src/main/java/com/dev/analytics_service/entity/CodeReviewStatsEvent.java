package com.dev.analytics_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_review_stats_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeReviewStatsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String repoName;
    private String reviewerEmail;
    private String status;
    private LocalDateTime timestamp;
}
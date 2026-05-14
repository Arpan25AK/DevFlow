package com.dev.analytics_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "repo_activity_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoActivityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ownerEmail;

    private String repoName;

    private String action;

    private LocalDateTime timestamp;
}
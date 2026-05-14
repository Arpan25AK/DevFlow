package com.dev.analytics_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_stats_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStatsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ownerEmail;
    private String repoName;
    private String status;
    private Long durationSeconds;
    private LocalDateTime timestamp;
}
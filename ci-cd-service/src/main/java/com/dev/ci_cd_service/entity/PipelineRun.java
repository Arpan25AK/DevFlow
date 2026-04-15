package com.dev.ci_cd_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name="pipeline_runs")
@NoArgsConstructor
@AllArgsConstructor
public class PipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String ownerEmail;

    private String name;

    private String fileName;

    @Enumerated(EnumType.STRING)
    private BuildStatus status;

    @Column(columnDefinition = "TEXT")
    private String buildLogs;

    private LocalDateTime timeStamp;

    @PrePersist
    protected void onCreate(){
        this.timeStamp = LocalDateTime.now();
    }

}

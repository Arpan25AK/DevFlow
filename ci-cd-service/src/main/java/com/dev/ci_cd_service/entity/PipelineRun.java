package com.dev.ci_cd_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="pipeline_runs")
@NoArgsConstructor
@AllArgsConstructor
public class PipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

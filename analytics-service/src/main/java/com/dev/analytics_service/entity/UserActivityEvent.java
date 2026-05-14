package com.dev.analytics_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    private String action;

    private LocalDateTime timestamp;
}
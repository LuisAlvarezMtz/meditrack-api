package com.meditrack.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "alarms")
@Data
public class Alarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alarm_config_id", nullable = false)
    private AlarmConfig alarmConfig;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    private AlarmStatus status;

    private boolean notified;

    private LocalDateTime createdAt;
}


package com.meditrack.dto.alarmconfig;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AlarmConfigResponseDto {

    private Long id;
    private Long patientId;
    private Long medicineId;
    private String medicineName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int frequencyHours;
    private boolean active;
    private LocalDateTime createdAt;
}

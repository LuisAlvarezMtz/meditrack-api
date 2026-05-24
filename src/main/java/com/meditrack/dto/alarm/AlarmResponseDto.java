package com.meditrack.dto.alarm;

import java.time.LocalDateTime;

import com.meditrack.model.AlarmStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmResponseDto {
    private Long id;
    private Long alarmConfigId;
    private Long medicineId;
    private String medicineName;
    private String dosageForm;
    private LocalDateTime scheduledAt;
    private AlarmStatus status;
    private boolean notified;
}
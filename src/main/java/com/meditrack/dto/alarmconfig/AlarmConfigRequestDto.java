package com.meditrack.dto.alarmconfig;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AlarmConfigRequestDto {

    private Long medicineId;
    private Long patientId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int frequencyHours;
}



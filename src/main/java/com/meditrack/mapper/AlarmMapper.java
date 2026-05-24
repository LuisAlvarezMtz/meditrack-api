package com.meditrack.mapper;

import com.meditrack.dto.alarm.AlarmResponseDto;
import com.meditrack.model.Alarm;

public class AlarmMapper {

    private AlarmMapper() {}

    public static AlarmResponseDto toResponseDto(Alarm entity) {
        AlarmResponseDto dto = new AlarmResponseDto();
        dto.setId(entity.getId());
        dto.setAlarmConfigId(entity.getAlarmConfig().getId());
        dto.setMedicineId(entity.getMedicine().getId());
        dto.setMedicineName(entity.getMedicine().getName());
        dto.setDosageForm(entity.getMedicine().getDosageForm());
        dto.setScheduledAt(entity.getScheduledAt());
        dto.setStatus(entity.getStatus());
        dto.setNotified(entity.isNotified());
        return dto;
    }
}
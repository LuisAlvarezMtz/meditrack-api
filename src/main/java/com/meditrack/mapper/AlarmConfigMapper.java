package com.meditrack.mapper;

import com.meditrack.dto.alarmconfig.AlarmConfigRequestDto;
import com.meditrack.dto.alarmconfig.AlarmConfigResponseDto;
import com.meditrack.model.AlarmConfig;
import com.meditrack.model.Medicine;

import java.time.LocalDateTime;

public class AlarmConfigMapper {

    private AlarmConfigMapper() {}

    public static AlarmConfig toEntity(
            AlarmConfigRequestDto dto,
            Medicine medicine
    ) {
        AlarmConfig config = new AlarmConfig();
        config.setMedicine(medicine);
        config.setStartDate(dto.getStartDate());
        config.setEndDate(dto.getEndDate());
        config.setFrequencyHours(dto.getFrequencyHours());
        config.setActive(true);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return config;
    }

    public static AlarmConfigResponseDto toResponseDto(AlarmConfig entity) {
        AlarmConfigResponseDto dto = new AlarmConfigResponseDto();
        dto.setId(entity.getId());
        dto.setPatientId(entity.getPatient().getId());
        dto.setMedicineId(entity.getMedicine().getId());
        dto.setMedicineName(entity.getMedicine().getName());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setFrequencyHours(entity.getFrequencyHours());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}